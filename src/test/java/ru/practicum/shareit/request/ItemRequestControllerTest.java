package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.practicum.shareit.exception.NoFoundObjectException;
import ru.practicum.shareit.exception.NoValidArgumentException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {
    @Autowired
    MockMvc mvc;

    @MockBean
    ItemRequestService itemRequestService;

    @Autowired
    ObjectMapper objectMapper;

    String USER_ID_HEADER = "X-Sharer-User-Id";

    @Test
    void createItem_statusOk_ifRequestIsCorrect() throws Exception {
        ItemDto itemDto = ItemDto.builder()
                .id(10L)
                .owner(1L)
                .name("Book")
                .description("good old book")
                .available(true)
                .requestId(1L)
                .build();

        ItemRequestDto request = ItemRequestDto.builder()
                .description("I need a book")
                .build();

        ItemRequestDto response = ItemRequestDto.builder()
                .description("I need a book")
                .requestorId(1L)
                .created(LocalDateTime.now().plusDays(10))
                .items(List.of(itemDto))
                .build();

        when(itemRequestService.createRequest(any(ItemRequestDto.class), anyLong()))
                .thenReturn(response);

        mvc.perform(post("/requests")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.description").value("I need a book"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.items[0].name").value("Book"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.requestorId").value(1L));
    }

    @Test
    void getOwnerRequestsByUser_statusOk_ifRequestsNotExist() throws Exception {
        when(itemRequestService.getOwnerRequestByUserId(anyLong()))
                .thenReturn(List.of());

        mvc.perform(get("/requests")
                        .header(USER_ID_HEADER, 1))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isEmpty());

    }

    @Test
    void getOwnerRequestsByUser_statusNotFound_ifUserNotExist() throws Exception {
        doThrow(NoFoundObjectException.class)
                .when(itemRequestService)
                .getOwnerRequestByUserId(anyLong());

        mvc.perform(get("/requests")
                        .header(USER_ID_HEADER, 1))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void getOwnerRequestsByUser_statusOk_ifRequestsExist() throws Exception {
        ItemDto itemDto = ItemDto.builder()
                .id(10L)
                .owner(1L)
                .name("Book")
                .description("good old book")
                .available(true)
                .requestId(1L)
                .build();

        ItemRequestDto response = ItemRequestDto.builder()
                .description("I need a book")
                .requestorId(1L)
                .created(LocalDateTime.now().plusDays(10))
                .items(List.of(itemDto))
                .build();

        when(itemRequestService.getOwnerRequestByUserId(anyLong()))
                .thenReturn(List.of(response));

        mvc.perform(get("/requests")
                        .header(USER_ID_HEADER, 1))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].description").value("I need a book"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].items[0].name").value("Book"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].requestorId").value(1L));
    }

    @Test
    void getRequestByUser_statusOk_ifRequestExist() throws Exception {
        ItemDto itemDto = ItemDto.builder()
                .id(10L)
                .owner(1L)
                .name("Book")
                .description("good old book")
                .available(true)
                .requestId(1L)
                .build();

        ItemRequestDto response = ItemRequestDto.builder()
                .description("I need a book")
                .requestorId(1L)
                .created(LocalDateTime.now().plusDays(10))
                .items(List.of(itemDto))
                .build();

        when(itemRequestService.getRequestByUserId(anyLong(), anyLong()))
                .thenReturn(response);

        mvc.perform(get("/requests/1")
                        .header(USER_ID_HEADER, 1))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.description").value("I need a book"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.items[0].name").value("Book"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.requestorId").value(1L));
    }

    @Test
    void getRequestByUser_statusNotFound_ifUserNotExist() throws Exception {
        doThrow(NoFoundObjectException.class)
                .when(itemRequestService)
                .getRequestByUserId(anyLong(), anyLong());

        mvc.perform(get("/requests/1")
                        .header(USER_ID_HEADER, 1))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void getAllRequestOtherUsers_statusBadRequest_ifNoValidParameterRequest() throws Exception {
        doThrow(NoValidArgumentException.class)
                .when(itemRequestService)
                .getRequestsOtherUsers(anyLong(), anyInt(), anyInt());

        mvc.perform(get("/requests/all")
                        .header(USER_ID_HEADER, 1)
                        .param("from", "-1")
                        .param("size", "-1"))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void getAllRequestOtherUsers_statusOkAndEmptyList_ifRequestsDontExist() throws Exception {
        when(itemRequestService.getRequestsOtherUsers(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of());

        mvc.perform(get("/requests/all")
                        .header(USER_ID_HEADER, 1)
                        .param("from", "-1")
                        .param("size", "-1"))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isEmpty());
    }

    @Test
    void getAllRequestOtherUsers_statusOkAndNotEmptyList_ifRequestsExist() throws Exception {
        ItemDto itemDto = ItemDto.builder()
                .id(10L)
                .owner(1L)
                .name("Book")
                .description("good old book")
                .available(true)
                .requestId(1L)
                .build();

        ItemRequestDto response = ItemRequestDto.builder()
                .description("I need a book")
                .requestorId(1L)
                .created(LocalDateTime.now().plusDays(10))
                .items(List.of(itemDto))
                .build();

        when(itemRequestService.getRequestsOtherUsers(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(response));

        mvc.perform(get("/requests/all")
                        .header(USER_ID_HEADER, 1)
                        .param("from", "-1")
                        .param("size", "-1"))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].description").value("I need a book"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].items[0].name").value("Book"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].requestorId").value(1L));
    }

    @Nested
    class ValidationUserTest {

        Validator validator;

        @BeforeEach
        void prepare() {
            try (ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
                validator = validatorFactory.getValidator();
            }
        }

        @Test
        void createRequest_correctSizeValidationList_completelyIncorrectRequest() {
            ItemRequestDto test = new ItemRequestDto();

            assertEquals(1, validator.validate(test).size());
        }

        @Test
        void createUser_notValidName_nameIsEmpty() {
            ItemRequestDto test = ItemRequestDto.builder().description("").build();

            List<ConstraintViolation<ItemRequestDto>> validationSet = new ArrayList<>(validator.validate(test));
            assertAll(
                    () -> assertEquals(1, validationSet.size()),
                    () -> assertEquals("Description cannot be empty or null", validationSet.get(0).getMessage())
            );
        }
    }
}