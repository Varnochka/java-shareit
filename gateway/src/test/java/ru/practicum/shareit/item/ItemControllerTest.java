package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.comment.CommentRequest;
import ru.practicum.shareit.item.dto.ItemRequest;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {
    @MockBean
    private ItemClient itemClient;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    private Validator validator;

    @BeforeEach
    void prepare() {
        try (ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
            validator = validatorFactory.getValidator();
        }
    }

    @Test
    void validationItem_correctSizeValidationList_completelyIncorrectItem() {
        ItemRequest test = new ItemRequest();

        assertEquals(3, validator.validate(test).size());
    }

    @Test
    void validationItem_notValidName_nameIsEmpty() {
        ItemRequest test = ItemRequest.builder().name("").description("description").available(true).build();

        List<ConstraintViolation<ItemRequest>> validationSet = new ArrayList<>(validator.validate(test));
        assertAll(
                () -> assertEquals(1, validationSet.size()),
                () -> assertEquals("Name cannot be empty or null", validationSet.get(0).getMessage())
        );
    }

    @Test
    void validationItem_notValidName_nameIsNull() {
        ItemRequest test = ItemRequest.builder().name(null).description("description").available(true).build();

        List<ConstraintViolation<ItemRequest>> validationSet = new ArrayList<>(validator.validate(test));
        assertAll(
                () -> assertEquals(1, validationSet.size()),
                () -> assertEquals("Name cannot be empty or null", validationSet.get(0).getMessage())
        );
    }

    @Test
    void validationItem_notValidName_descriptionIsEmpty() {
        ItemRequest test = ItemRequest.builder().name("name").description("").available(true).build();

        List<ConstraintViolation<ItemRequest>> validationSet = new ArrayList<>(validator.validate(test));
        assertAll(
                () -> assertEquals(1, validationSet.size()),
                () -> assertEquals("Description cannot be empty or null", validationSet.get(0).getMessage())
        );
    }

    @Test
    void validationItem_notValidName_descriptionIsNull() {
        ItemRequest test = ItemRequest.builder().name("name").description(null).available(true).build();

        List<ConstraintViolation<ItemRequest>> validationSet = new ArrayList<>(validator.validate(test));
        assertAll(
                () -> assertEquals(1, validationSet.size()),
                () -> assertEquals("Description cannot be empty or null", validationSet.get(0).getMessage())
        );
    }

    @Test
    void validationItem_notValidName_availableIsNull() {
        ItemRequest test = ItemRequest.builder().name("name").description("description").available(null).build();

        List<ConstraintViolation<ItemRequest>> validationSet = new ArrayList<>(validator.validate(test));
        assertAll(
                () -> assertEquals(1, validationSet.size()),
                () -> assertEquals("Available cannot be null", validationSet.get(0).getMessage())
        );
    }

    @Test
    void validationComment_notValidTest_availableIsNull() {
        CommentRequest test = CommentRequest.builder().text(null).build();

        List<ConstraintViolation<CommentRequest>> validationSet = new ArrayList<>(validator.validate(test));
        assertAll(
                () -> assertEquals(1, validationSet.size()),
                () -> assertEquals("Text cannot be null or empty", validationSet.get(0).getMessage())
        );
    }

    @Test
    void createComment_statusBadRequest_textIsEmpty() throws Exception {
        CommentRequest request = CommentRequest.builder().text("").build();

        mvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", "1")
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).createComment(anyLong(), anyLong(), any());
    }

}