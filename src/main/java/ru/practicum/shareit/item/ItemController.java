package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.impl.ItemServiceImpl;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private final ItemServiceImpl itemService;

    @GetMapping
    public List<ItemDto> getAllByUserId(@RequestHeader(name = USER_ID_HEADER) Long userId) {
        return itemService.getAllByUserId(userId);
    }

    @GetMapping("/{itemId}")
    public ItemDto getItem(@RequestHeader(name = USER_ID_HEADER) Long userId, @PathVariable Long itemId) {
        return itemService.getItemById(itemId);
    }

    @PostMapping
    public ItemDto addItem(@RequestHeader(name = USER_ID_HEADER) Long userId,
                           @Valid @RequestBody ItemDto itemDto) {
        return itemService.create(itemDto, userId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestHeader(name = USER_ID_HEADER) Long userId,
                              @PathVariable Long itemId, @RequestBody ItemDto itemDto) {
        return itemService.updateById(itemDto, itemId, userId);
    }

    @GetMapping("/search")
    public List<ItemDto> searchByText(@RequestHeader(name = USER_ID_HEADER) Long userId,
                                      @RequestParam String text) {
        return itemService.searchByText(text);
    }
}
