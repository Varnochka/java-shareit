package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.comment.CommentRequest;
import ru.practicum.shareit.item.comment.CommentResponse;
import ru.practicum.shareit.item.dto.ItemRequest;
import ru.practicum.shareit.item.dto.ItemResponse;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private final ItemService itemService;

    @GetMapping
    public List<ItemResponse> getAllItemsByUserId(@RequestHeader(name = USER_ID_HEADER) Long userId,
                                                  @RequestParam(defaultValue = "0") int from,
                                                  @RequestParam(defaultValue = "20") int size) {
        return itemService.getAllItemsByUserId(userId, PageRequest.of(from, size));
    }

    @PostMapping
    public ItemResponse createItem(@RequestHeader(name = USER_ID_HEADER) Long userId,
                                   @RequestBody ItemRequest itemRequest) {
        return itemService.createItem(itemRequest, userId);
    }

    @GetMapping("/{itemId}")
    public ItemResponse getItem(@RequestHeader(name = USER_ID_HEADER) Long userId,
                                @PathVariable(name = "itemId") Long itemId) {
        return itemService.getItemById(itemId, userId);
    }

    @PatchMapping("/{itemId}")
    public ItemResponse updateItem(@RequestHeader(name = USER_ID_HEADER) Long userId,
                                   @PathVariable(name = "itemId") Long itemId,
                                   @RequestBody ItemRequest itemRequest) {
        return itemService.updateItemById(itemRequest, itemId, userId);
    }

    @GetMapping("/search")
    public List<ItemResponse> searchByText(@RequestParam(name = "text") String text) {
        return itemService.searchItemByText(text);
    }

    @PostMapping("/{itemId}/comment")
    public CommentResponse createComment(@RequestHeader(name = USER_ID_HEADER) Long userId, @PathVariable Long itemId,
                                         @RequestBody CommentRequest request) {
        return itemService.createComment(request, userId, itemId);
    }
}
