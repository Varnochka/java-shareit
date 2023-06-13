package ru.practicum.shareit.booking;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBookerIdAndEndIsBefore(Long bookerId, LocalDateTime end, Pageable pageable);

    List<Booking> findAllByBookerId(Long bookerId, Pageable pageable);

    List<Booking> findByBookerIdAndStartIsBeforeAndEndIsAfter(Long bookerId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    List<Booking> findByBookerIdAndStartIsAfter(Long bookerId, LocalDateTime start, Pageable pageable);

    List<Booking> findAllByItemIdIn(List<Long> itemId, Pageable pageable);

    List<Booking> findByItemIdInAndStartIsBeforeAndEndIsAfter(List<Long> itemId, LocalDateTime date, LocalDateTime date1, Pageable pageable);

    List<Booking> findByItemIdInAndEndIsBefore(List<Long> itemId, LocalDateTime date, Pageable pageable);

    List<Booking> findByItemIdInAndStartIsAfterAndStatusIs(List<Long> itemId, LocalDateTime date, Status bookingStatus, Pageable pageable);

    List<Booking> findByBookerIdAndStartIsAfterAndStatusIs(Long userId, LocalDateTime date, Status bookingStatus, Pageable pageable);

    List<Booking> findByItemIdInAndStartIsAfter(List<Long> itemIdList, LocalDateTime date, Pageable pageable);

    List<Booking> findAllByItemIdIn(List<Long> itemId);

    List<Booking> findByItemIdAndEndIsBefore(Long itemId, LocalDateTime date);

    List<Booking> findAllByItemId(Long id);
}
