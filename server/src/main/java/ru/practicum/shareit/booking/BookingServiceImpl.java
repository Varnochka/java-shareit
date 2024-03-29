package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public BookingResponse createBooking(Long userId, BookingRequest request) {
        User user = userService.findUserById(userId);

        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new NoFoundObjectException(String.format("Item with id='%s' not found",
                        request.getItemId())));

        if (!item.getAvailable()) {
            throw new NoCorrectRequestException("Item is not available for booking");
        }

        if (Objects.equals(item.getOwner().getId(), userId)) {
            throw new NoFoundObjectException("You cannot book your item");
        }

        Booking booking = BookingMapper.dtoToObject(request);
        booking.setBooker(user);
        booking.setItem(item);
        booking.setStatus(BookingStatus.WAITING);

        Booking savedBooking = bookingRepository.save(booking);
        return BookingMapper.objectToDto(savedBooking);
    }

    @Override
    @Transactional
    public BookingResponse updateStatusById(Long id, Boolean approved, Long userId) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NoFoundObjectException(String.format("Booking with id='%s' not found", id)));

        if (!Objects.equals(booking.getItem().getOwner().getId(), userId))
            throw new NoFoundObjectException(String.format("User with id='%s' can not change status item with id='%s",
                    userId, id));

        if (!booking.getStatus().equals(BookingStatus.WAITING))
            throw new NoCorrectRequestException("Booker status must be WAITING.");

        if (approved) {
            booking.setStatus(BookingStatus.APPROVED);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
        }

        Booking savedBooking = bookingRepository.save(booking);
        return BookingMapper.objectToDto(savedBooking);
    }

    @Override
    public BookingResponse getBookingById(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoFoundObjectException(String.format("Booking with id='%s' not found", bookingId)));

        if (!Objects.equals(booking.getBooker().getId(), userId) && !Objects.equals(booking.getItem().getOwner().getId(), userId)) {
            throw new NoFoundObjectException(String.format("Only booker or item owner can get booking with id='%s'", bookingId));
        }

        return BookingMapper.objectToDto(booking);
    }

    @Override
    public List<BookingResponse> getBookingsByBookerId(Long userId, String state, int from, int size) {
        userService.checkExistUserById(userId);

        if (from < 0 || size <= 0) {
            throw new NoValidArgumentException("The request parameters from b size are invalid and cannot be negative");
        }

        LocalDateTime dateNow = LocalDateTime.now();

        int page = from == 0 ? 0 : (from / size);
        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        Pageable pageable = PageRequest.of(page, size, sort);

        BookingState bookingState = BookingState.from(state)
                .orElseThrow(() -> new NoCorrectRequestException("Unknown state: " + state));

        List<Booking> bookings = new ArrayList<>();
        switch (bookingState) {
            case CURRENT:
                bookings = bookingRepository.findByBookerIdAndStartIsBeforeAndEndIsAfter(userId, dateNow, dateNow, pageable);
                break;
            case PAST:
                bookings = bookingRepository.findByBookerIdAndEndIsBefore(userId, dateNow, pageable);
                break;
            case FUTURE:
                bookings = bookingRepository.findByBookerIdAndStartIsAfter(userId, dateNow, pageable);
                break;
            case WAITING:
                bookings = bookingRepository.findByBookerIdAndStartIsAfterAndStatusIs(userId, dateNow, BookingStatus.WAITING, pageable);
                break;
            case REJECTED:
                bookings = bookingRepository.findByBookerIdAndStartIsAfterAndStatusIs(userId, dateNow, BookingStatus.REJECTED, pageable);
                break;
            case ALL:
                bookings = bookingRepository.findAllByBookerId(userId, pageable);
        }
        return BookingMapper.objectToDto(bookings);
    }

    @Override
    public List<BookingResponse> getBookingsByOwnerId(Long userId, String state, int from, int size) {
        User user = userService.findUserById(userId);

        if (from < 0 || size <= 0) {
            throw new NoValidArgumentException("The request parameters from b size are invalid and cannot be negative");
        }

        LocalDateTime dateNow = LocalDateTime.now();
        int page = from == 0 ? 0 : (from / size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("start").descending());
        BookingState bookingState = BookingState.from(state)
                .orElseThrow(() -> new NoCorrectRequestException("Unknown state: " + state));
        List<Long> itemIdList = itemRepository.findAllByOwnerId(user.getId(), PageRequest.of(0, 20))
                .stream()
                .map(Item::getId)
                .collect(Collectors.toList());

        List<Booking> bookings = new ArrayList<>();
        switch (bookingState) {
            case CURRENT:
                bookings = bookingRepository.findByItemIdInAndStartIsBeforeAndEndIsAfter(itemIdList, dateNow, dateNow, pageable);
                break;
            case PAST:
                bookings = bookingRepository.findByItemIdInAndEndIsBefore(itemIdList, dateNow, pageable);
                break;
            case FUTURE:
                bookings = bookingRepository.findByItemIdInAndStartIsAfter(itemIdList, dateNow, pageable);
                break;
            case WAITING:
                bookings = bookingRepository.findByItemIdInAndStartIsAfterAndStatusIs(itemIdList, dateNow, BookingStatus.WAITING, pageable);
                break;
            case REJECTED:
                bookings = bookingRepository.findByItemIdInAndStartIsAfterAndStatusIs(itemIdList, dateNow, BookingStatus.REJECTED, pageable);
                break;
            case ALL:
                bookings = bookingRepository.findAllByItemIdIn(itemIdList, pageable);
        }
        return BookingMapper.objectToDto(bookings);
    }

    @Override
    public List<Booking> getAllByItemId(Long id) {
        return bookingRepository.findAllByItemId(id);
    }

    @Override
    public List<Booking> getAllByItemIdIn(List<Long> itemsId) {
        return bookingRepository.findAllByItemIdIn(itemsId);
    }

    @Override
    public List<Booking> getAllByItemAndEndBeforeDate(Long itemId, LocalDateTime created) {
        return bookingRepository.findByItemIdAndEndIsBefore(itemId, created);
    }
}