package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.practicum.shareit.exception.AccessException;
import ru.practicum.shareit.exception.NoFoundObjectException;
import ru.practicum.shareit.item.comment.CommentRequest;
import ru.practicum.shareit.item.comment.CommentResponse;
import ru.practicum.shareit.item.dto.ItemRequest;
import ru.practicum.shareit.item.dto.ItemResponse;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {
    @Autowired
    MockMvc mvc;

    @MockBean
    ItemService itemService;

    @Autowired
    ObjectMapper objectMapper;

    String userIdHeader = "X-Sharer-User-Id";

    @Test
    void getAllItemsByUserId_statusOk_ifUserExistAndItemsDontExist() throws Exception {
        when(itemService.getAllItemsByUserId(anyLong(), any()))
                .thenReturn(List.of());

        mvc.perform(MockMvcRequestBuilders.get("/items")
                        .header(userIdHeader, 1))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").doesNotExist());
    }

    @Test
    void getAllItemsByUserId_statusNotFound_ifUserDontExist() throws Exception {
        doThrow(NoFoundObjectException.class)
                .when(itemService)
                .getAllItemsByUserId(anyLong(), any());

        mvc.perform(MockMvcRequestBuilders.get("/items")
                        .header(userIdHeader, 1))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void getItem_statusNotFound_ifItemDontExist() throws Exception {
        doThrow(NoFoundObjectException.class)
                .when(itemService)
                .getItemById(anyLong(), anyLong());

        mvc.perform(MockMvcRequestBuilders.get("/items/1")
                        .header(userIdHeader, 1))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void getItem_statusIsOk_ifItemExist() throws Exception {
        ItemResponse itemResponse = ItemResponse.builder()
                .id(1L)
                .name("Book")
                .description("Good old book")
                .build();

        when(itemService.getItemById(anyLong(), anyLong()))
                .thenReturn(itemResponse);

        mvc.perform(MockMvcRequestBuilders.get("/items/1")
                        .header(userIdHeader, 1))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Book"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description").value("Good old book"));
    }

    @Test
    void updateItem_statusNotFound_ifItemNotFound() throws Exception {
        ItemRequest itemRequest = ItemRequest.builder()
                .id(1L)
                .name("Book")
                .description("Good old book")
                .build();


        doThrow(NoFoundObjectException.class)
                .when(itemService)
                .updateItemById(any(ItemRequest.class), anyLong(), anyLong());

        mvc.perform(patch("/items/1")
                        .header(userIdHeader, 1)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(itemRequest)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void updateItem_statusIsForbidden_ifUserIsNotOwnerItem() throws Exception {
        ItemRequest itemRequest = ItemRequest.builder()
                .id(1L)
                .name("Book")
                .description("Good old book")
                .build();

        doThrow(AccessException.class)
                .when(itemService)
                .updateItemById(any(ItemRequest.class), anyLong(), anyLong());

        mvc.perform(patch("/items/1")
                        .header(userIdHeader, 1)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(itemRequest)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    void updateItem_statusIsOk_ifUserIsNotOwnerItem() throws Exception {
        ItemRequest itemRequest = ItemRequest.builder()
                .id(1L)
                .name("Book")
                .description("Good old book")
                .build();

        ItemResponse itemResponse = ItemResponse.builder()
                .id(1L)
                .name("Book")
                .description("Good old book")
                .build();

        when(itemService.updateItemById(any(ItemRequest.class), anyLong(), anyLong()))
                .thenReturn(itemResponse);

        mvc.perform(patch("/items/1")
                        .header(userIdHeader, 1)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(itemRequest)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void createItem_statusOk_ifItemExist() throws Exception {
        ItemRequest itemRequest = ItemRequest.builder()
                .id(1L)
                .name("Book")
                .description("Good old book")
                .available(true)
                .build();

        ItemResponse itemResponse = ItemResponse.builder()
                .id(1L)
                .name("Book")
                .description("Good old book")
                .build();

        when(itemService.createItem(any(ItemRequest.class), anyLong()))
                .thenReturn(itemResponse);

        mvc.perform(post("/items")
                        .header(userIdHeader, 1)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(itemRequest)))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Book"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description").value("Good old book"));
    }

    @Test
    void searchByText_statusOk_ifTextIsNotEmpty() throws Exception {
        when(itemService.searchItemByText(anyString()))
                .thenReturn(List.of());

        mvc.perform(MockMvcRequestBuilders.get("/items/search")
                        .param("text", ""))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").doesNotExist());
    }

    @Test
    void searchByText_statusOk_ifTextIsEmpty() throws Exception {
        ItemResponse itemResponse = ItemResponse.builder()
                .id(1L)
                .name("Book")
                .description("Good old book")
                .build();

        when(itemService.searchItemByText(anyString()))
                .thenReturn(List.of(itemResponse));

        mvc.perform(MockMvcRequestBuilders.get("/items/search")
                        .param("text", "book"))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value("Book"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].description").value("Good old book"));
    }

    @Test
    void createComment_statusOk_ifItemExist() throws Exception {
        CommentRequest commentRequest = CommentRequest.builder()
                .text("Good item")
                .build();

        CommentResponse commentResponse = CommentResponse.builder()
                .id(1L)
                .authorName("User001")
                .text("Good item")
                .created(LocalDateTime.of(2023, 5, 11, 17, 10, 36))
                .build();

        when(itemService.createComment(any(CommentRequest.class), anyLong(), anyLong()))
                .thenReturn(commentResponse);

        mvc.perform(post("/items/1/comment")
                        .header(userIdHeader, 1)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(commentRequest)))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.text").value("Good item"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.created").value(commentResponse.getCreated().toString()));
    }

    @Test
    void createComment_statusForbidden_ifItemDontExist() throws Exception {
        CommentRequest commentRequest = CommentRequest.builder()
                .text("Good item")
                .build();

        doThrow(AccessException.class)
                .when(itemService)
                .createComment(any(CommentRequest.class), anyLong(), anyLong());

        mvc.perform(post("/items/1/comment")
                        .header(userIdHeader, 1)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(commentRequest)))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").doesNotExist());
    }
}