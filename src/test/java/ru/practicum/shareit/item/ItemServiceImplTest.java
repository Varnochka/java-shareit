package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.exception.NoCorrectRequestException;
import ru.practicum.shareit.exception.NoFoundObjectException;
import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.comment.CommentMapper;
import ru.practicum.shareit.item.comment.CommentRequest;
import ru.practicum.shareit.item.comment.CommentService;
import ru.practicum.shareit.item.dto.ItemRequest;
import ru.practicum.shareit.item.dto.ItemResponse;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@SpringBootTest
class ItemServiceImplTest {
    @Autowired
    ItemServiceImpl underTest;

    @MockBean
    ItemRepository itemRepository;

    @MockBean
    BookingService bookingService;

    @MockBean
    UserService userService;

    @MockBean
    CommentService commentService;

    @MockBean
    ItemRequestRepository itemRequestRepository;

    User user1;
    User user2;
    Item item;
    Booking booking;
    Comment comment;

    @BeforeEach
    void prepare() {
        user1 = User.builder().id(1L).name("Nikita").email("nikita@mail.ru").build();
        user2 = User.builder().id(2L).name("Mike").email("mike@mail.ru").build();

        item = Item.builder()
                .id(1L)
                .name("Book")
                .description("Good old book")
                .owner(user1)
                .available(true)
                .build();

        booking = Booking.builder()
                .id(1L)
                .item(item)
                .start(LocalDateTime.of(2023, 2, 10, 17, 10, 5))
                .end(LocalDateTime.of(2023, 2, 10, 17, 10, 5).plusDays(15))
                .booker(user2)
                .status(Status.WAITING)
                .build();

        comment = Comment.builder()
                .id(1L)
                .text("good book")
                .author(user2)
                .item(item)
                .created(LocalDateTime.of(2023, 2, 10, 17, 10, 5).plusDays(10))
                .build();
    }

    @Test
    void createItem_successfulCreated_requestIsCorrectAndUserExist() {
        ItemRequest request = ItemRequest.builder()
                .name("Book")
                .description("Good old book")
                .available(true)
                .build();

        when(userService.findUserById(anyLong())).thenReturn(user1);
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        underTest.createItem(request, 1L);

        verify(itemRepository, atLeast(1)).save(any(Item.class));
    }

    @Test
    void createItem_notFoundObjectException_userNotExist() {
        ItemRequest request = ItemRequest.builder()
                .name("Book")
                .description("Good old book")
                .available(true)
                .build();

        doThrow(NoFoundObjectException.class)
                .when(userService).findUserById(anyLong());

        assertThrows(NoFoundObjectException.class, () -> underTest.createItem(request, 100L));
    }

    @Test
    void getItemById_notFoundObjectException_itemNotExist() {
        doThrow(NoFoundObjectException.class)
                .when(itemRepository).findById(anyLong());

        assertThrows(NoFoundObjectException.class, () -> underTest.getItemById(1L, 1L));
    }

    @Test
    void getItemById_correctResult_itemExist() {
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.of(item));

        when(bookingService.getAllByItemId(anyLong()))
                .thenReturn(List.of(booking));

        when(commentService.getAllCommentsByItemId(anyLong()))
                .thenReturn(CommentMapper.objectsToDto(List.of(comment)));

        underTest.getItemById(1L, 1L);

        verify(itemRepository, atLeast(1)).findById(anyLong());
    }

    @Test
    void updateItemById_notFoundObjectException_itemNotExist() {
        ItemRequest request = ItemRequest.builder()
                .name("Update title Book")
                .description("Good old book")
                .available(true)
                .build();

        doThrow(NoFoundObjectException.class)
                .when(itemRepository).findById(anyLong());

        assertThrows(NoFoundObjectException.class, () -> underTest.updateItemById(request, 1L, 1L));
    }

    @Test
    void updateItemById_accessException_userIsNotOwnerItem() {
        ItemRequest request = ItemRequest.builder()
                .name("Update title Book")
                .description("Good old book")
                .available(true)
                .build();

        assertThrows(NoFoundObjectException.class, () -> underTest.updateItemById(request, 1L, 2L));
    }

    @Test
    void updateItemById_successfullyUpdated_userExist() {
        ItemRequest request = ItemRequest.builder()
                .name("Update title Book")
                .description("Good old book")
                .available(true)
                .build();

        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.of(item));

        item.setName(request.getName());

        when(itemRepository.save(any(Item.class))).thenReturn(item);

        underTest.updateItemById(request, 1L, 1L);

        verify(itemRepository, atLeast(1)).save(any(Item.class));
    }

    @Test
    void getAllItemsByUserId_notFoundObjectException_userNotExist() {
        doThrow(NoFoundObjectException.class)
                .when(userService).checkExistUserById(anyLong());

        assertThrows(NoFoundObjectException.class, () -> underTest.getAllItemsByUserId(1L));
    }

    @Test
    void getAllItemsByUserId_correctResult_userAndItemsExist() {
        doNothing()
                .when(userService)
                .checkExistUserById(anyLong());

        when(itemRepository.findAllByOwnerId(anyLong()))
                .thenReturn(List.of(item));

        when(bookingService.getAllByItemId(anyLong()))
                .thenReturn(List.of(booking));

        underTest.getAllItemsByUserId(1L);

        verify(itemRepository, atLeast(1)).findAllByOwnerId(anyLong());
    }

    @Test
    void searchItemByText_notEmptyList_itemExist() {
        String text = "book";

        when(itemRepository.findByText(anyString()))
                .thenReturn(List.of(item));

        List<ItemResponse> result = underTest.searchItemByText(text);



        verify(itemRepository, atLeast(1)).findByText(anyString());

        assertFalse(result.isEmpty());
    }

    @Test
    void createComment_notFoundObjectException_userNotExist() {
        CommentRequest request = CommentRequest.builder()
                .text("good book")
                .build();

        doThrow(NoFoundObjectException.class)
                .when(userService).findUserById(anyLong());

        assertThrows(NoFoundObjectException.class, () -> underTest.createComment(request, 2L, 1L));
    }

    @Test
    void createComment_notFoundObjectException_itemNotExist() {
        CommentRequest request = CommentRequest.builder()
                .text("good book")
                .build();

        when(userService.findUserById(anyLong()))
                .thenReturn(user2);

        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.empty());


        assertThrows(NoFoundObjectException.class, () -> underTest.createComment(request, 2L, 2L));
    }

    @Test
    void createComment_noCorrectRequestException_userHasNotBookingItem() {
        CommentRequest request = CommentRequest.builder()
                .text("good book")
                .build();

        when(userService.findUserById(anyLong()))
                .thenReturn(user2);

        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.of(item));

        when(bookingService.getAllByItemAndEndBeforeDate(anyLong(), any()))
                .thenReturn(List.of());

        assertThrows(NoCorrectRequestException.class, () -> underTest.createComment(request, 2L, 2L));
    }

    @Test
    void createComment_successfullyCreated_requestIsCorrect() {
        CommentRequest request = CommentRequest.builder()
                .text("good book")
                .build();

        when(userService.findUserById(anyLong()))
                .thenReturn(user2);

        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.of(item));

        when(bookingService.getAllByItemAndEndBeforeDate(anyLong(), any()))
                .thenReturn(List.of(booking));

        when(commentService.createComment(any(Comment.class)))
                .thenReturn(comment);

        underTest.createComment(request, 2L, 2L);

        verify(commentService, atLeast(1)).createComment(any(Comment.class));
    }

    @Test
    void getAllByRequestIds_emptyList_requestsDontExist() {
        when(itemRepository.findAllByRequestIdIn(any()))
                .thenReturn(List.of());

        underTest.getAllByRequestIds(Set.of(11L, 22L));

        verify(itemRepository, atLeast(1)).findAllByRequestIdIn(any());
    }

    @Test
    void getItemByRequestId_nullResult_requestDontExist() {
        underTest.getItemByRequestId(11L);

        verify(itemRepository, atLeast(1)).findByRequestId(anyLong());
    }

}