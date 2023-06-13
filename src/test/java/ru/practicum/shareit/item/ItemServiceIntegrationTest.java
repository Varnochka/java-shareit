package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.item.dto.ItemRequest;
import ru.practicum.shareit.item.dto.ItemResponse;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserRequest;
import ru.practicum.shareit.user.dto.UserResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemServiceIntegrationTest {
    private final ItemService itemService;
    private final UserService userService;

    @Test
    void shouldCreateItemsAndGetByUserId() {
        ItemRequest itemRequest = createItemDto(true, null);

        UserRequest userRequest1 = UserRequest.builder().name("test name").email("test1@test.test").build();
        UserRequest userRequest2 = UserRequest.builder().name("test name2").email("test2@test.test").build();

        UserResponse user1 = userService.createUser(userRequest1);
        UserResponse user2 = userService.createUser(userRequest2);

        itemService.createItem(itemRequest, user1.getId());
        itemService.createItem(itemRequest, user1.getId());

        itemService.createItem(itemRequest, user2.getId());

        List<ItemResponse> results = itemService.getAllItemsByUserId(user1.getId());

        assertThat(results).hasSize(2);
    }

    public static ItemRequest createItemDto(boolean available, Long requestId) {
        return ItemRequest.builder()
                .name("Test name")
                .description("Test description")
                .available(available)
                .requestId(requestId)
                .build();
    }
}