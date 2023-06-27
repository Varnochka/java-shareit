package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exception.AccessException;
import ru.practicum.shareit.exception.NoCorrectRequestException;
import ru.practicum.shareit.exception.NoFoundObjectException;
import ru.practicum.shareit.item.comment.*;
import ru.practicum.shareit.item.dto.ItemResponse;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.item.dto.ItemRequest;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserService userService;
    private final BookingService bookingService;
    private final CommentService commentService;
    private final ItemRequestRepository itemRequestRepository;

    @Override
    @Transactional
    public ItemResponse createItem(ItemRequest request, Long userId) {
        User user = userService.findUserById(userId);

        Item item = ItemMapper.dtoToObject(request);
        item.setOwner(user);

        if (request.getRequestId() != null) {
            item.setRequest(itemRequestRepository
                    .findById(request.getRequestId()).orElse(null));
        }

        Item savedItem = itemRepository.save(item);
        return ItemMapper.objectToItemResponseDto(savedItem);
    }

    @Override
    public ItemResponse getItemById(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NoFoundObjectException(String.format("Item with id='%s' not found", itemId)));

        ItemResponse itemResponse = ItemMapper.objectToItemResponseDto(item);

        if (Objects.equals(userId, item.getOwner().getId())) {
            List<Booking> bookingList = bookingService.getAllByItemId(itemId);
            setLastAndNextBookings(bookingList, itemResponse);
        }

        List<CommentResponse> comments = commentService.getAllCommentsByItemId(itemId);
        itemResponse.setComments(comments);

        return itemResponse;
    }

    @Override
    @Transactional
    public ItemResponse updateItemById(ItemRequest request, Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NoFoundObjectException(String.format("Item with id='%s' not found", itemId)));

        if (!Objects.equals(item.getOwner().getId(), userId)) {
            throw new AccessException(String.format("Owner of item with id='%s' is another", itemId));
        }

        if (StringUtils.hasLength(request.getName())) {
            item.setName(request.getName());
        }

        if (StringUtils.hasLength(request.getDescription())) {
            item.setDescription(request.getDescription());
        }

        if ((request.getAvailable() != null)) {
            item.setAvailable(request.getAvailable());
        }

        Item savedItem = itemRepository.save(item);
        return ItemMapper.objectToItemResponseDto(savedItem);
    }

    @Override
    public List<ItemResponse> getAllItemsByUserId(Long id, Pageable pageable) {
        userService.checkExistUserById(id);

        List<ItemResponse> items = itemRepository.findAllByOwnerId(id, pageable).stream()
                .map(ItemMapper::objectToItemResponseDto)
                .collect(Collectors.toList());

        List<Long> itemsId = items.stream()
                .map(ItemResponse::getId)
                .collect(Collectors.toList());

        List<Booking> bookingList = bookingService.getAllByItemIdIn(itemsId);

        items = items
                .stream()
                .map(itemsDto -> setLastAndNextBookings(bookingList, itemsDto))
                .collect(Collectors.toList());

        return items.stream().sorted(Comparator.comparing(ItemResponse::getId)).collect(Collectors.toList());
    }

    @Override
    public List<ItemResponse> searchItemByText(String text) {
        if (!StringUtils.hasLength(text)) {
            return List.of();
        }
        return ItemMapper.objectToItemResponseDto(itemRepository.findByText(text));
    }

    @Override
    @Transactional
    public CommentResponse createComment(CommentRequest request, Long userId, Long itemId) {
        Comment comment = CommentMapper.dtoToObject(request);

        User author = userService.findUserById(userId);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NoFoundObjectException(String.format("Item with id='%s' not found", itemId)));

        List<Booking> bookings = bookingService.getAllByItemAndEndBeforeDate(itemId, comment.getCreated())
                .stream()
                .filter(booking -> Objects.equals(booking.getBooker().getId(), userId))
                .collect(Collectors.toList());

        if (bookings.isEmpty()) {
            throw new NoCorrectRequestException(String.format("User with id='%s' cannot leave a review of this thing",
                    userId));
        }

        comment.setAuthor(author);
        comment.setItem(item);
        Comment savedComment = commentService.createComment(comment);

        return CommentMapper.dtoToObject(savedComment);
    }

    @Override
    public List<Item> getAllByRequestIds(Set<Long> ids) {
        return itemRepository.findAllByRequestIdIn(ids);
    }

    @Override
    public Item getItemByRequestId(Long requestId) {
        return itemRepository.findByRequestId(requestId);
    }

    private ItemResponse setLastAndNextBookings(List<Booking> bookingList, ItemResponse itemResponse) {
        LocalDateTime dateTime = LocalDateTime.now();

        bookingList
                .stream()
                .filter(booking -> Objects.equals(booking.getItem().getId(), itemResponse.getId()))
                .sorted(Comparator.comparing(Booking::getEnd).reversed())
                .filter(booking -> booking.getStatus().equals(BookingStatus.APPROVED))
                .filter(booking -> booking.getStart().isBefore(dateTime))
                .limit(1)
                .findAny()
                .ifPresent(booking -> itemResponse.setLastBooking(BookingDto.builder()
                        .id(booking.getId())
                        .bookerId(booking.getBooker().getId())
                        .build()));

        bookingList
                .stream()
                .filter(booking -> Objects.equals(booking.getItem().getId(), itemResponse.getId()))
                .sorted(Comparator.comparing(Booking::getStart))
                .filter(booking -> booking.getStatus().equals(BookingStatus.APPROVED))
                .filter(booking -> booking.getStart().isAfter(dateTime))
                .limit(1)
                .findAny()
                .ifPresent(booking -> itemResponse.setNextBooking(BookingDto.builder()
                        .id(booking.getId())
                        .bookerId(booking.getBooker().getId())
                        .build()));

        return itemResponse;
    }
}
