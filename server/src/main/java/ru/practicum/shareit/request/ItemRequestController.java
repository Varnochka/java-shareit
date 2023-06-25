package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestDto createRequest(@RequestHeader(name = USER_ID_HEADER) Long userId,
                                        @RequestBody ItemRequestDto request) {
        return itemRequestService.createRequest(request, userId);
    }

    @GetMapping
    public List<ItemRequestDto> getOwnerRequestsByUser(@RequestHeader(name = USER_ID_HEADER) Long userId) {
        return itemRequestService.getOwnerRequestByUserId(userId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getRequestByUser(@RequestHeader(name = USER_ID_HEADER) Long userId,
                                           @PathVariable(name = "requestId") Long requestId) {
        return itemRequestService.getRequestByUserId(userId, requestId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllRequestOtherUsers(@RequestHeader(name = USER_ID_HEADER) Long userId,
                                                        @RequestParam(value = "from",
                                                                defaultValue = "0") @PositiveOrZero Integer from,
                                                        @RequestParam(value = "size",
                                                                defaultValue = "10") @Positive Integer size) {
        return itemRequestService.getRequestsOtherUsers(userId, from, size);
    }

}
