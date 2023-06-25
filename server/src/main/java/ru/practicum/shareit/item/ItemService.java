package ru.practicum.shareit.item;

import ru.practicum.shareit.item.comment.CommentRequest;
import ru.practicum.shareit.item.dto.ItemResponse;
import ru.practicum.shareit.item.comment.CommentResponse;
import ru.practicum.shareit.item.dto.ItemRequest;

import java.util.List;
import java.util.Set;

public interface ItemService {
    ItemResponse createItem(ItemRequest itemRequest, Long userId);

    ItemResponse getItemById(Long id, Long userId);

    ItemResponse updateItemById(ItemRequest itemRequest, Long id, Long userId);

    List<ItemResponse> getAllItemsByUserId(Long userId);

    List<ItemResponse> searchItemByText(String text);

    CommentResponse createComment(CommentRequest request, Long userId, Long itemId);

    List<Item> getAllByRequestIds(Set<Long> collect);

    Item getItemByRequestId(Long requestId);
}
