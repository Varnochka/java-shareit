package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.dto.BookingRequest;
import ru.practicum.shareit.booking.dto.BookingResponse;
import ru.practicum.shareit.exception.NoCorrectRequestException;
import ru.practicum.shareit.exception.NoFoundObjectException;
import ru.practicum.shareit.exception.NoValidArgumentException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class BookingServiceImplTest {

    @Autowired
    BookingServiceImpl underTest;

    @MockBean
    BookingRepository bookingRepository;

    @MockBean
    UserService userService;

    @MockBean
    ItemRepository itemRepository;

    User user;
    User user2;

    Item item;

    Booking booking;

    @BeforeEach
    void prepare() {
        user = User.builder().id(1L).name("Nikita").email("nikita@mail.ru").build();
        user2 = User.builder().id(2L).name("Mike").email("mike@mail.ru").build();

        item = Item.builder().id(1L)
                .name("Book")
                .description("Good old book")
                .owner(user)
                .available(true)
                .build();

        booking = Booking.builder()
                .id(1L)
                .item(item)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(15))
                .booker(user2)
                .build();
    }

    @Test
    void getAllByItemId_notEmptyList_bookingsExist() {
        when(bookingRepository.findAllByItemId(anyLong())).thenReturn(List.of(booking));
        List<Booking> result = underTest.getAllByItemId(1L);
        verify(bookingRepository, times(1)).findAllByItemId(anyLong());
        assertFalse(result.isEmpty());
    }

    @Test
    void getAllByItemId_emptyList_bookingsDesNotExist() {
        when(bookingRepository.findAllByItemId(anyLong())).thenReturn(List.of());
        List<Booking> result = underTest.getAllByItemId(1L);
        verify(bookingRepository, times(1)).findAllByItemId(anyLong());
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllByItemIdIn_notEmptyList_bookingsExist() {
        when(bookingRepository.findAllByItemIdIn(anyList())).thenReturn(List.of(booking));
        List<Booking> result = underTest.getAllByItemIdIn(List.of(1L, 2L));
        verify(bookingRepository, times(1)).findAllByItemIdIn(anyList());
        assertFalse(result.isEmpty());
    }

    @Test
    void getAllByItemIdIn_emptyList_bookingsDoesNottExist() {
        when(bookingRepository.findAllByItemIdIn(anyList())).thenReturn(List.of());
        List<Booking> result = underTest.getAllByItemIdIn(List.of(1L, 2L));
        verify(bookingRepository, times(1)).findAllByItemIdIn(anyList());
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllByItemAndEndBeforeDate_notEmptyList_bookingsExist() {
        when(bookingRepository.findByItemIdAndEndIsBefore(anyLong(), any(LocalDateTime.class)))
                .thenReturn(List.of(booking));
        List<Booking> result = underTest.getAllByItemAndEndBeforeDate(1L, LocalDateTime.now());
        verify(bookingRepository, times(1)).findByItemIdAndEndIsBefore(anyLong(), any());
        assertFalse(result.isEmpty());
    }

    @Test
    void getAllByItemAndEndBeforeDate_emptyList_bookingsDesNotExist() {
        when(bookingRepository.findByItemIdAndEndIsBefore(anyLong(), any(LocalDateTime.class)))
                .thenReturn(List.of());
        List<Booking> result = underTest.getAllByItemAndEndBeforeDate(1L, LocalDateTime.now());
        verify(bookingRepository, times(1)).findByItemIdAndEndIsBefore(anyLong(), any());
        assertTrue(result.isEmpty());
    }

    @Test
    void createBooking_noFoundObjectException_userIdDoesNotExist() {
        BookingRequest request = BookingRequest.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(15))
                .build();

        doThrow(NoFoundObjectException.class)
                .when(userService).findUserById(anyLong());

        assertThrows(NoFoundObjectException.class, () -> underTest.createBooking(1L, request));
    }

    @Test
    void createBooking_noCorrectRequestException_availableIsFalse() {
        item.setAvailable(false);

        BookingRequest request = BookingRequest.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(15))
                .build();

        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        assertThrows(NoCorrectRequestException.class, () -> underTest.createBooking(1L, request));
    }

    @Test
    void createBooking_noFoundObjectException_ownerAndRequestorIsSame() {
        BookingRequest request = BookingRequest.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(15))
                .build();

        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        assertThrows(NoFoundObjectException.class, () -> underTest.createBooking(1L, request));
    }

    @Test
    void createBooking_successfulCreate_requestIsCorrect() {
        BookingRequest request = BookingRequest.builder()
                .itemId(1L)
                .start(LocalDateTime.of(2023, 2, 10, 17, 10, 5))
                .end(LocalDateTime.of(2023, 2, 10, 17, 10, 5).plusDays(15))
                .build();

        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.of(item));

        when(bookingRepository.save(any(Booking.class)))
                .thenReturn(booking);

        underTest.createBooking(2L, request);

        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    void updateStatusById_noFoundObjectException_bookingDoesNotExist() {
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(NoFoundObjectException.class, () -> underTest.updateStatusById(1L, true, 1L));
    }

    @Test
    void updateStatusById_noFoundObjectException_userIsNotOwnerBooking() {
        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.of(booking));

        assertThrows(NoFoundObjectException.class, () -> underTest.updateStatusById(1L, true, 2L));
    }

    @Test
    void updateStatusById_noCorrectRequestException_statusIsNotWaiting() {
        booking.setStatus(BookingStatus.REJECTED);

        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.of(booking));

        assertThrows(NoCorrectRequestException.class, () -> underTest.updateStatusById(1L, true, 1L));
    }

    @Test
    void updateStatusById_successfullyUpdatedWithStatusApproved_correctRequest() {
        boolean approved = true;
        booking.setStatus(BookingStatus.WAITING);

        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.of(booking));

        when(bookingRepository.save(any(Booking.class)))
                .thenReturn(booking);

        BookingResponse bookingResponse = underTest.updateStatusById(1L, approved, 1L);

        verify(bookingRepository, times(1)).save(any(Booking.class));
        assertEquals(BookingStatus.APPROVED, bookingResponse.getStatus());
    }

    @Test
    void updateStatusById_successfullyUpdatedWithStatusRejected_correctRequest() {
        boolean approved = false;
        booking.setStatus(BookingStatus.WAITING);

        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.of(booking));

        when(bookingRepository.save(any(Booking.class)))
                .thenReturn(booking);

        BookingResponse bookingResponse = underTest.updateStatusById(1L, approved, 1L);

        verify(bookingRepository, times(1)).save(any(Booking.class));
        assertEquals(BookingStatus.REJECTED, bookingResponse.getStatus());
    }

    @Test
    void getBookingById_noFoundObjectException_bookingDontExist() {
        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(NoFoundObjectException.class, () -> underTest.getBookingById(1L, 1L));
    }

    @Test
    void getBookingById_noFoundObjectException_userIsNotItemOwnerOrBooker() {
        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.of(booking));

        assertThrows(NoFoundObjectException.class, () -> underTest.getBookingById(1L, 33L));
    }

    @Test
    void getBookingById_correctResult_requestIsCorrect() {
        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.of(booking));

        underTest.getBookingById(1L, 1L);

        verify(bookingRepository, times(1)).findById(anyLong());
    }

    @Test
    void getAllByBookerId_noFoundObjectException_userDontExist() {
        doThrow(NoFoundObjectException.class)
                .when(userService).checkExistUserById(anyLong());

        assertThrows(NoFoundObjectException.class, () -> underTest.getBookingsByBookerId(1L, "ALL", 0, 10));
    }

    @Test
    void getAllByBookerId_noValidArgumentException_sizeAndFromNotCorrect() {
        int from = -1;
        int size = -1;
        doNothing()
                .when(userService).checkExistUserById(anyLong());

        assertThrows(NoValidArgumentException.class, () -> underTest.getBookingsByBookerId(1L, "ALL", from, size));
    }

    @Test
    void getAllByBookerId_noCorrectRequestException_stateNotCorrect() {
        String state = "NONE";
        doNothing()
                .when(userService).checkExistUserById(anyLong());

        assertThrows(NoCorrectRequestException.class, () -> underTest.getBookingsByBookerId(1L, state, 0, 10));
    }

    @Test
    void getAllByBookerId_correctResultAndEmptyList_requestIsCorrectWithStateAll() {
        String state = "ALL";
        int from = 0;
        int size = 10;
        int page = 0;

        doNothing()
                .when(userService).checkExistUserById(anyLong());

        Pageable pageable = PageRequest.of(page, size, Sort.by("start").descending());

        underTest.getBookingsByBookerId(1L, state, from, size);

        verify(bookingRepository, times(1))
                .findAllByBookerId(1L, pageable);
    }

    @Test
    void getAllByBookerId_correctResultAndEmptyList_requestIsCorrectWithStateCurrent() {
        String state = "CURRENT";
        int from = 0;
        int size = 10;

        doNothing()
                .when(userService).checkExistUserById(anyLong());

        underTest.getBookingsByBookerId(1L, state, from, size);

        verify(bookingRepository, times(1))
                .findByBookerIdAndStartIsBeforeAndEndIsAfter(anyLong(), any(LocalDateTime.class),
                        any(LocalDateTime.class), any(Pageable.class));
    }

    @Test
    void getAllByBookerId_correctResultAndEmptyList_requestIsCorrectWithStatePast() {
        String state = "PAST";
        int from = 0;
        int size = 10;

        doNothing()
                .when(userService).checkExistUserById(anyLong());

        underTest.getBookingsByBookerId(1L, state, from, size);

        verify(bookingRepository, atLeast(1))
                .findByBookerIdAndEndIsBefore(anyLong(), any(LocalDateTime.class), any(Pageable.class));
    }

    @Test
    void getAllByBookerId_correctResultAndEmptyList_requestIsCorrectWithStateFuture() {
        String state = "FUTURE";
        int from = 0;
        int size = 10;

        doNothing()
                .when(userService).checkExistUserById(anyLong());

        underTest.getBookingsByBookerId(1L, state, from, size);

        verify(bookingRepository, times(1))
                .findByBookerIdAndStartIsAfter(anyLong(), any(LocalDateTime.class), any(Pageable.class));
    }

    @Test
    void getAllByBookerId_correctResultAndEmptyList_requestIsCorrectWithStateWaiting() {
        String state = "WAITING";
        int from = 0;
        int size = 10;

        doNothing()
                .when(userService).checkExistUserById(anyLong());

        underTest.getBookingsByBookerId(1L, state, from, size);

        verify(bookingRepository, times(1))
                .findByBookerIdAndStartIsAfterAndStatusIs(anyLong(), any(LocalDateTime.class),
                        any(BookingStatus.class), any(Pageable.class));
    }

    @Test
    void getAllByBookerId_correctResultAndEmptyList_requestIsCorrectWithStateRejected() {
        String state = "REJECTED";
        int from = 0;
        int size = 10;

        doNothing()
                .when(userService).checkExistUserById(anyLong());

        underTest.getBookingsByBookerId(1L, state, from, size);

        verify(bookingRepository, times(1))
                .findByBookerIdAndStartIsAfterAndStatusIs(anyLong(), any(LocalDateTime.class),
                        any(BookingStatus.class), any(Pageable.class));
    }

    @Test
    void getAllByOwnerId_noFoundObjectException_userDoNotExist() {
        doThrow(NoFoundObjectException.class)
                .when(userService).findUserById(anyLong());

        assertThrows(NoFoundObjectException.class,
                () -> underTest.getBookingsByOwnerId(1L, "ALL", 0, 10));
    }

    @Test
    void getAllByOwnerId_noValidArgumentException_sizeAndFromNotCorrect() {
        int from = -1;
        int size = -1;

        when(userService.findUserById(anyLong())).thenReturn(user);

        assertThrows(NoValidArgumentException.class,
                () -> underTest.getBookingsByOwnerId(1L, "ALL", from, size));
    }

    @Test
    void getAllByOwnerId_correctResultAndEmptyList_requestUsCorrectAndStateAll() {
        String state = "ALL";
        int from = 0;
        int size = 10;
        int page = 0;

        when(userService.findUserById(anyLong()))
                .thenReturn(user);

        when(itemRepository.findAllByOwnerId(anyLong()))
                .thenReturn(List.of(item));

        Pageable pageable = PageRequest.of(page, size, Sort.by("start").descending());

        underTest.getBookingsByOwnerId(1L, state, from, size);

        verify(bookingRepository, times(1))
                .findAllByItemIdIn(List.of(item.getId()), pageable);
    }

    @Test
    void getAllByOwnerId_correctResultAndEmptyList_requestUsCorrectAndStateCurrent() {
        String state = "CURRENT";
        int from = 0;
        int size = 10;

        when(userService.findUserById(anyLong()))
                .thenReturn(user);

        when(itemRepository.findAllByOwnerId(anyLong()))
                .thenReturn(List.of(item));


        underTest.getBookingsByOwnerId(1L, state, from, size);

        verify(bookingRepository, times(1))
                .findByItemIdInAndStartIsBeforeAndEndIsAfter(anyList(), any(LocalDateTime.class),
                        any(LocalDateTime.class), any(Pageable.class));
    }

    @Test
    void getAllByOwnerId_correctResultAndEmptyList_requestUsCorrectAndStatePast() {
        String state = "PAST";
        int from = 0;
        int size = 10;

        when(userService.findUserById(anyLong()))
                .thenReturn(user);

        when(itemRepository.findAllByOwnerId(anyLong()))
                .thenReturn(List.of(item));


        underTest.getBookingsByOwnerId(1L, state, from, size);

        verify(bookingRepository, times(1))
                .findByItemIdInAndEndIsBefore(anyList(), any(LocalDateTime.class), any(Pageable.class));
    }

    @Test
    void getAllByOwnerId_correctResultAndEmptyList_requestUsCorrectAndStateFuture() {
        String state = "FUTURE";
        int from = 0;
        int size = 10;

        when(userService.findUserById(anyLong()))
                .thenReturn(user);

        when(itemRepository.findAllByOwnerId(anyLong()))
                .thenReturn(List.of(item));


        underTest.getBookingsByOwnerId(1L, state, from, size);

        verify(bookingRepository, times(1))
                .findByItemIdInAndStartIsAfter(anyList(), any(LocalDateTime.class), any(Pageable.class));
    }

    @Test
    void getAllByOwnerId_correctResultAndEmptyList_requestUsCorrectAndStateWaiting() {
        String state = "WAITING";
        int from = 0;
        int size = 10;

        when(userService.findUserById(anyLong()))
                .thenReturn(user);

        when(itemRepository.findAllByOwnerId(anyLong()))
                .thenReturn(List.of(item));


        underTest.getBookingsByOwnerId(1L, state, from, size);

        verify(bookingRepository, times(1))
                .findByItemIdInAndStartIsAfterAndStatusIs(anyList(), any(LocalDateTime.class),
                        any(BookingStatus.class), any(Pageable.class));
    }

    @Test
    void getAllByOwnerId_correctResultAndEmptyList_requestUsCorrectAndStateRejected() {
        String state = "REJECTED";
        int from = 0;
        int size = 10;

        when(userService.findUserById(anyLong()))
                .thenReturn(user);

        when(itemRepository.findAllByOwnerId(anyLong()))
                .thenReturn(List.of(item));


        underTest.getBookingsByOwnerId(1L, state, from, size);

        verify(bookingRepository, times(1))
                .findByItemIdInAndStartIsAfterAndStatusIs(anyList(), any(LocalDateTime.class),
                        any(BookingStatus.class), any(Pageable.class));
    }
}