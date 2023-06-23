package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NoFoundObjectException;
import ru.practicum.shareit.exception.NoValidArgumentException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserService userService;
    private final ItemService itemService;

    @Override
    public ItemRequest getRequestById(Long id) {
        return itemRequestRepository.findById(id)
                .orElseThrow(() -> new NoFoundObjectException(String.format("ItemRequest with id='%s' not found", id)));
    }

    @Override
    public ItemRequestDto createRequest(ItemRequestDto request, Long userId) {
        User user = userService.findUserById(userId);

        ItemRequest itemRequest = ItemRequestMapper.dtoToObject(request);
        itemRequest.setRequestor(user);
        itemRequest.setCreated(LocalDateTime.now());

        return ItemRequestMapper.objectToDto(itemRequestRepository.save(itemRequest));
    }

    @Override
    public List<ItemRequestDto> getOwnerRequestByUserId(Long userId) {
        userService.checkExistUserById(userId);

        List<ItemRequest> requests = itemRequestRepository.findAllByRequestorId(userId);

        List<Item> items = itemService.getAllByRequestIds(requests
                .stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toSet()));

        Map<Long, ItemRequestDto> requestDtos = ItemRequestMapper.objectToDto(requests).stream()
                .collect(Collectors.toMap(ItemRequestDto::getId, request -> request));

        items.forEach(item -> {
            Long id = item.getRequest().getId();
            requestDtos.get(id).getItems().add(ItemMapper.objectToDto(item));
        });

        return new ArrayList<>(requestDtos.values());
    }

    @Override
    public ItemRequestDto getRequestByUserId(Long userId, Long requestId) {
        userService.checkExistUserById(userId);

        ItemRequest itemRequest = getRequestById(requestId);

        Item item = itemService.getItemByRequestId(requestId);

        ItemRequestDto itemRequestDto = ItemRequestMapper.objectToDto(itemRequest);
        itemRequestDto.setItems(List.of(ItemMapper.objectToDto(item)));

        return itemRequestDto;
    }

    @Override
    public List<ItemRequestDto> getRequestsOtherUsers(Long userId, Integer from, Integer size) {
        userService.checkExistUserById(userId);

        if (from < 0 || size <= 0) {
            throw new NoValidArgumentException("The request parameters from b size are invalid and cannot be negative");
        }

        int page = from == 0 ? 0 : (from / size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("created").descending());

        List<ItemRequest> requests = itemRequestRepository.findAllByOwnerId(userId, pageable);

        Map<Long, ItemRequestDto> requestDtos = ItemRequestMapper.objectToDto(requests).stream()
                .collect(Collectors.toMap(ItemRequestDto::getId, requestDto -> requestDto));

        List<Item> items = itemService.getAllByRequestIds(requests
                .stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toSet()));

        items.forEach(item -> {
            Long id = item.getRequest().getId();
            requestDtos.get(id).getItems().add(ItemMapper.objectToDto(item));
        });

        return new ArrayList<>(requestDtos.values());
    }

}
