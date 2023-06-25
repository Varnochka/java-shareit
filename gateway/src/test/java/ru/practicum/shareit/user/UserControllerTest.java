package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
class UserControllerTest {
    @MockBean
    private UserClient userClient;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    Validator validator;

    @BeforeEach
    void prepare() {
        try (ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
            validator = validatorFactory.getValidator();
        }
    }

    @Test
    void createItem_correctSizeValidationList_completelyIncorrectItem() {
        UserDto test = new UserDto();

        assertEquals(2, validator.validate(test).size());
    }

    @Test
    void createUser_notValidName_nameIsEmpty() {
        UserDto test = UserDto.builder().name("").email("test@mail.ru").build();

        List<ConstraintViolation<UserDto>> validationSet = new ArrayList<>(validator.validate(test));
        assertAll(
                () -> assertEquals(1, validationSet.size()),
                () -> assertEquals("Name cannot be empty or null", validationSet.get(0).getMessage())
        );
    }

    @Test
    void createUser_notValidName_nameIsNull() {
        UserDto test = UserDto.builder().name(null).email("test@mail.ru").build();

        List<ConstraintViolation<UserDto>> validationSet = new ArrayList<>(validator.validate(test));
        assertAll(
                () -> assertEquals(1, validationSet.size()),
                () -> assertEquals("Name cannot be empty or null", validationSet.get(0).getMessage())
        );
    }

    @Test
    void createUser_notValidEmail_emailIsEmpty() {
        UserDto test = UserDto.builder().name("Test").email("").build();

        List<ConstraintViolation<UserDto>> validationSet = new ArrayList<>(validator.validate(test));
        assertEquals(2, validationSet.size());
    }

    @Test
    void createUser_notValidEmail_emailIsNull() {
        UserDto test = UserDto.builder().name("Test").email(null).build();

        List<ConstraintViolation<UserDto>> validationSet = new ArrayList<>(validator.validate(test));
        assertAll(
                () -> assertEquals(1, validationSet.size()),
                () -> assertEquals("Email cannot empty or null", validationSet.get(0).getMessage())
        );
    }


    @Test
    void createUser_notValidEmail_incorrectEmail1() {
        UserDto test = UserDto.builder().name("Test").email("email").build();

        List<ConstraintViolation<UserDto>> validationSet = new ArrayList<>(validator.validate(test));
        assertAll(
                () -> assertEquals(1, validationSet.size()),
                () -> assertEquals("Email is not format as email (email@email.com)", validationSet.get(0).getMessage())
        );
    }

    @Test
    void createUser_notValidEmail_incorrectEmail2() {
        UserDto test = UserDto.builder().name("Test").email("email@").build();

        List<ConstraintViolation<UserDto>> validationSet = new ArrayList<>(validator.validate(test));
        assertAll(
                () -> assertEquals(1, validationSet.size()),
                () -> assertEquals("Email is not format as email (email@email.com)", validationSet.get(0).getMessage())
        );
    }

    @Test
    void createUser_notValidEmail_incorrectEmail3() {
        UserDto test = UserDto.builder().name("Test").email("email@.").build();

        List<ConstraintViolation<UserDto>> validationSet = new ArrayList<>(validator.validate(test));
        assertAll(
                () -> assertEquals(1, validationSet.size()),
                () -> assertEquals("Email is not format as email (email@email.com)", validationSet.get(0).getMessage())
        );
    }

    @Test
    void createUser_notValidEmail_incorrectEmail4() {
        UserDto test = UserDto.builder().name("Test").email("email@com").build();

        List<ConstraintViolation<UserDto>> validationSet = new ArrayList<>(validator.validate(test));
        assertAll(
                () -> assertEquals(1, validationSet.size()),
                () -> assertEquals("Email is not format as email (email@email.com)", validationSet.get(0).getMessage())
        );
    }

    @Test
    void createUser_notValidEmail_incorrectEmail5() {
        UserDto test = UserDto.builder().name("Test").email("email@com.").build();

        List<ConstraintViolation<UserDto>> validationSet = new ArrayList<>(validator.validate(test));
        assertAll(
                () -> assertEquals(1, validationSet.size()),
                () -> assertEquals("Email is not format as email (email@email.com)", validationSet.get(0).getMessage())
        );
    }

    @Test
    void createUser_notValidEmail_incorrectEmail6() {
        UserDto test = UserDto.builder().name("Test").email("email@com.r").build();

        List<ConstraintViolation<UserDto>> validationSet = new ArrayList<>(validator.validate(test));
        assertAll(
                () -> assertEquals(1, validationSet.size()),
                () -> assertEquals("Email is not format as email (email@email.com)", validationSet.get(0).getMessage())
        );
    }

    @Test
    void createUser_statusBadRequest_emailIsIncorrect() throws Exception {
        UserDto request = UserDto.builder().name("Test").email("email@com.r").build();

        mvc.perform(MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(userClient, never()).createUser(any());
    }

    @Test
    void createUser_statusBadRequest_nameIsNull() throws Exception {
        UserDto request = UserDto.builder().email("email@com.ru").build();

        mvc.perform(MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(userClient, never()).createUser(any());
    }

    @Test
    void createUser_statusBadRequest_emailIsNull() throws Exception {
        UserDto request = UserDto.builder().name("Test").build();

        mvc.perform(MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(userClient, never()).createUser(any());
    }

    @Test
    void createUser_statusOk_requestIsCorrect() throws Exception {
        UserDto request = UserDto.builder().name("Test").email("test@mail.ru").build();

        mvc.perform(MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(userClient, times(1)).createUser(any());
    }

    @Test
    void getAllUsers_statusOk_requestIsCorrect() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/users"))
                .andExpect(status().isOk());

        verify(userClient, times(1)).getAllUsers();
    }

    @Test
    void getUserById_statusOk_requestIsCorrect() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/users/1"))
                .andExpect(status().isOk());

        verify(userClient, times(1)).getUserById(anyLong());
    }

    @Test
    void deleteUser_statusOk_requestIsCorrect() throws Exception {
        mvc.perform(MockMvcRequestBuilders.delete("/users/1"))
                .andExpect(status().isOk());

        verify(userClient, times(1)).deleteUser(anyLong());
    }
}