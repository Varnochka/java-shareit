package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingRequest;
import ru.practicum.shareit.booking.dto.BookingResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingService {
    BookingResponse createBooking(Long userId, BookingRequest bookingRequest);

    BookingResponse updateStatusById(Long bookingId, Boolean approved, Long userId);

    BookingResponse getBookingById(Long bookingId, Long userId);

    List<BookingResponse> getAllByBookerId(Long userId, String state, int from, int size);

    List<BookingResponse> getAllByOwnerId(Long userId, String state,  int from, int size);

    List<Booking> getAllByItemId(Long id);

    List<Booking> getAllByItemIdIn(List<Long> itemsId);

    List<Booking> getAllByItemAndEndBeforeDate(Long itemId, LocalDateTime created);
}

