package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exception.AccessException;
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

import static org.junit.jupiter.api.Assertions.*;
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
    User user3;
    Item item;
    Booking bookingUser2;
    Booking bookingUser3;
    Comment comment;

    @BeforeEach
    void prepare() {
        user1 = User.builder().id(1L).name("Nikita").email("nikita@mail.ru").build();
        user2 = User.builder().id(2L).name("Mike").email("mike@mail.ru").build();
        user3 = User.builder().id(3L).name("Sam").email("sam@mail.ru").build();

        item = Item.builder()
                .id(1L)
                .name("Book")
                .description("Good old book")
                .owner(user1)
                .request(new ru.practicum.shareit.request.ItemRequest(10L, "I need book", user2, LocalDateTime.now()))
                .available(true)
                .build();

        bookingUser2 = Booking.builder()
                .id(1L)
                .item(item)
                .start(LocalDateTime.now().minusDays(10))
                .end(LocalDateTime.now().minusDays(5))
                .booker(user2)
                .status(BookingStatus.APPROVED)
                .build();

        bookingUser3 = Booking.builder()
                .id(3L)
                .item(item)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(15))
                .booker(user3)
                .status(BookingStatus.APPROVED)
                .build();

        comment = Comment.builder()
                .id(1L)
                .text("good book")
                .author(user2)
                .item(item)
                .created(LocalDateTime.now().minusDays(2))
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

        verify(itemRepository, times(1)).save(any(Item.class));
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
    void createItem_successfulCreatedWithNullRequest_requestIsCorrectAndUserExist() {
        ItemRequest request = ItemRequest.builder()
                .name("Book")
                .description("Good old book")
                .available(true)
                .requestId(55L)
                .build();

        item.setRequest(null);

        when(userService.findUserById(anyLong())).thenReturn(user1);
        when(itemRequestRepository.findById(anyLong())).thenReturn(Optional.empty());
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemResponse result = underTest.createItem(request, 1L);

        verify(itemRepository, times(1)).save(any(Item.class));
        assertEquals(0, result.getRequestId());
    }

    @Test
    void createItem_successfulCreatedWithNotNullRequest_requestIsCorrectAndUserExist() {
        ru.practicum.shareit.request.ItemRequest ir = ru.practicum.shareit.request.ItemRequest.builder()
                .id(10L)
                .description("I need book")
                .build();
        ItemRequest request = ItemRequest.builder()
                .name("Book")
                .description("Good old book")
                .available(true)
                .requestId(10L)
                .build();

        when(userService.findUserById(anyLong())).thenReturn(user1);
        when(itemRequestRepository.findById(anyLong())).thenReturn(Optional.of(ir));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemResponse result = underTest.createItem(request, 1L);

        verify(itemRepository, times(1)).save(any(Item.class));
        assertEquals(10L, result.getRequestId());
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
                .thenReturn(List.of(bookingUser2));

        when(commentService.getAllCommentsByItemId(anyLong()))
                .thenReturn(CommentMapper.objectsToDto(List.of(comment)));

        underTest.getItemById(1L, 1L);

        verify(itemRepository, times(1)).findById(anyLong());
    }

    @Test
    void getItemById_correctResultWithBookings_itemExist() {
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.of(item));

        when(bookingService.getAllByItemId(anyLong()))
                .thenReturn(List.of(bookingUser2, bookingUser3));

        when(commentService.getAllCommentsByItemId(anyLong()))
                .thenReturn(CommentMapper.objectsToDto(List.of(comment)));

        ItemResponse result = underTest.getItemById(1L, 1L);

        verify(itemRepository, times(1)).findById(anyLong());
        assertEquals(bookingUser2.getBooker().getId(), result.getLastBooking().getBookerId());
        assertEquals(bookingUser3.getBooker().getId(), result.getNextBooking().getBookerId());
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

        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.of(item));

        assertThrows(AccessException.class, () -> underTest.updateItemById(request, 1L, 2L));
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

        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    void getAllItemsByUserId_notFoundObjectException_userNotExist() {
        doThrow(NoFoundObjectException.class)
                .when(userService).checkExistUserById(anyLong());

        assertThrows(NoFoundObjectException.class, () -> underTest.getAllItemsByUserId(1L, PageRequest.of(0, 20)));
    }

    @Test
    void getAllItemsByUserId_correctResult_userAndItemsExist() {
        doNothing()
                .when(userService)
                .checkExistUserById(anyLong());

        when(itemRepository.findAllByOwnerId(anyLong(), any()))
                .thenReturn(new PageImpl<>(List.of(item)));

        when(bookingService.getAllByItemId(anyLong()))
                .thenReturn(List.of(bookingUser2));

        underTest.getAllItemsByUserId(1L, PageRequest.of(0, 20));

        verify(itemRepository, times(1)).findAllByOwnerId(anyLong(), any());
    }

    @Test
    void searchItemByText_notEmptyList_itemExist() {
        String text = "book";

        when(itemRepository.findByText(anyString()))
                .thenReturn(List.of(item));

        List<ItemResponse> result = underTest.searchItemByText(text);


        verify(itemRepository, times(1)).findByText(anyString());

        assertFalse(result.isEmpty());
    }

    @Test
    void searchItemByText_emptyList_itemExist() {
        String text = "";

        List<ItemResponse> result = underTest.searchItemByText(text);

        verify(itemRepository, times(0)).findByText(anyString());

        assertTrue(result.isEmpty());
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
    void createComment_successfullyCreated_requestCreateIsCorrect() {
        CommentRequest request = CommentRequest.builder()
                .text("good book")
                .build();

        when(userService.findUserById(anyLong()))
                .thenReturn(user2);

        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.of(item));

        when(bookingService.getAllByItemAndEndBeforeDate(anyLong(), any()))
                .thenReturn(List.of(bookingUser2));

        when(commentService.createComment(any(Comment.class)))
                .thenReturn(comment);

        underTest.createComment(request, 2L, 2L);

        verify(commentService, times(1)).createComment(any(Comment.class));
    }

    @Test
    void getAllByRequestIds_emptyList_requestsDontExist() {
        when(itemRepository.findAllByRequestIdIn(any()))
                .thenReturn(List.of());

        underTest.getAllByRequestIds(Set.of(11L, 22L));

        verify(itemRepository, times(1)).findAllByRequestIdIn(any());
    }

    @Test
    void getItemByRequestId_nullResult_requestDontExist() {
        underTest.getItemByRequestId(11L);

        verify(itemRepository, times(1)).findByRequestId(anyLong());
    }

}