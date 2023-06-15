package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.exception.NoFoundObjectException;
import ru.practicum.shareit.item.comment.CommentRequest;
import ru.practicum.shareit.item.dto.ItemRequest;
import ru.practicum.shareit.item.dto.ItemResponse;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserRequest;
import ru.practicum.shareit.user.dto.UserResponse;

import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemServiceIntegrationTest {
    private final ItemService itemService;
    private final UserService userService;
    private final EntityManager entityManager;

    @BeforeEach
    void prepare() {
        entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY = 0; " +
                "TRUNCATE TABLE comments, items, users RESTART IDENTITY;" +
                "SET REFERENTIAL_INTEGRITY = 1;");
    }

    @Test
    void createItemsAndGetByUserId_createItemAndReturnNotEmptyList() {
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

    @Test
    void createItemsAndGetByUserId_noFoundObjectException_userNotExist() {
        ItemRequest itemRequest = createItemDto(true, null);

        NoFoundObjectException exception = assertThrows(NoFoundObjectException.class,
                () -> itemService.createItem(itemRequest, 22L));

        assertEquals("User with id='22' not found", exception.getMessage());
    }

    @Test
    void createComment_noFoundObjectException_userExistAndItemNotExist() {
        UserRequest userRequest1 = UserRequest.builder().name("test name").email("test@test.test").build();
        UserResponse user = userService.createUser(userRequest1);
        CommentRequest commentRequest = createCommentRequest();

        NoFoundObjectException exception = assertThrows(NoFoundObjectException.class,
                () -> itemService.createComment(commentRequest, user.getId(), 100L));

        assertEquals("Item with id='100' not found", exception.getMessage());
    }


    public static ItemRequest createItemDto(boolean available, Long requestId) {
        return ItemRequest.builder()
                .name("Test name")
                .description("Test description")
                .available(available)
                .requestId(requestId)
                .build();
    }

    public static CommentRequest createCommentRequest() {
        return CommentRequest.builder()
                .text("some text of comment")
                .build();
    }
}