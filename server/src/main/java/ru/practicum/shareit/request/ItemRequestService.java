package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequest getRequestById(Long id);

    ItemRequestDto createRequest(ItemRequestDto request, Long userId);

    List<ItemRequestDto> getOwnerRequestByUserId(Long userId);

    ItemRequestDto getRequestByUserId(Long userId, Long requestId);

    List<ItemRequestDto> getRequestsOtherUsers(Long userId, Integer from, Integer size);
}
