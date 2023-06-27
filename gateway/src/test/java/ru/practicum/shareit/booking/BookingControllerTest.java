package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookItemRequest;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {
    @MockBean
    private BookingClient bookingClient;

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
    void validationBooking_correctSizeValidationList_completelyIncorrectItem() {
        BookItemRequest test = new BookItemRequest();

        assertEquals(3, validator.validate(test).size());
    }

    @Test
    void validationItem_correctValidationListSize_itemIdIsNull() {
        BookItemRequest test = BookItemRequest.builder()
                .itemId(null)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(1))
                .build();

        List<ConstraintViolation<BookItemRequest>> validationSet = new ArrayList<>(validator.validate(test));
        assertAll(
                () -> assertEquals(1, validationSet.size()),
                () -> assertEquals("ItemId cannot be empty or null", validationSet.get(0).getMessage())
        );
    }

    @Test
    void validationItem_correctValidationListSize_startInThePast() {
        BookItemRequest test = BookItemRequest.builder()
                .itemId(1L)
                .start(LocalDateTime.now().minusDays(1))
                .end(LocalDateTime.now().plusDays(1))
                .build();

        List<ConstraintViolation<BookItemRequest>> validationSet = new ArrayList<>(validator.validate(test));
        assertAll(
                () -> assertEquals(1, validationSet.size()),
                () -> assertEquals("Start time can be only in present or future", validationSet.get(0).getMessage())
        );
    }

    @Test
    void validationItem_correctValidationListSize_startIsNull() {
        BookItemRequest test = BookItemRequest.builder()
                .itemId(1L)
                .start(null)
                .end(LocalDateTime.now().plusDays(1))
                .build();

        List<ConstraintViolation<BookItemRequest>> validationSet = new ArrayList<>(validator.validate(test));
        assertAll(
                () -> assertEquals(1, validationSet.size()),
                () -> assertEquals("Start date cannot be empty or null", validationSet.get(0).getMessage())
        );
    }

    @Test
    void validationItem_correctValidationListSize_endIsNull() {
        BookItemRequest test = BookItemRequest.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(null)
                .build();

        List<ConstraintViolation<BookItemRequest>> validationSet = new ArrayList<>(validator.validate(test));
        assertAll(
                () -> assertEquals(1, validationSet.size()),
                () -> assertEquals("End date cannot be empty or null", validationSet.get(0).getMessage())
        );
    }

    @Test
    void validationItem_correctValidationListSize_endTimeInThePast() {
        BookItemRequest test = BookItemRequest.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().minusDays(1))
                .build();

        List<ConstraintViolation<BookItemRequest>> validationSet = new ArrayList<>(validator.validate(test));
        assertAll(
                () -> assertEquals(1, validationSet.size()),
                () -> assertEquals("End time can be only in future", validationSet.get(0).getMessage())
        );
    }

    @Test
    void createBooking_statusBabRequest_endDateIsYearlyStartDate() throws Exception {
        BookItemRequest requestDto = BookItemRequest.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(10))
                .end(LocalDateTime.now().plusDays(1))
                .build();

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(requestDto))
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void createBooking_statusIsOk_requestIsCorrect() throws Exception {
        BookItemRequest requestDto = BookItemRequest.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(10))
                .end(LocalDateTime.now().plusDays(11))
                .build();
        Long bookerId = 1L;

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", bookerId)
                        .content(mapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void getBookingsById_statusIsOk_requestIsCorrect() throws Exception {
        Integer bookingId = 1;
        Integer userId = 1;

        mvc.perform(get("/bookings/{id}", bookingId)
                        .header("X-Sharer-User-Id", userId))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void updateBooking_statusIsOk_requestIsCorrect() throws Exception {
        Integer bookingId = 1;
        Integer userId = 1;

        mvc.perform(patch("/bookings/{bookingId}?approved=true", bookingId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk());
    }

    @Test
    public void updateBooking_statusIsOk_requestIsCorrectAndBookingsUpdateWithApprovedFalse() throws Exception {
        Integer bookingId = 1;
        Integer userId = 1;

        mvc.perform(patch("/bookings/{bookingId}?approved=false", bookingId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk());

    }

    @Test
    public void getAllBooking_statusIsOk_requestIsCorrect() throws Throwable {
        Integer userId = 2;

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void getAllBookingOwner_statusIsOk_requestIsCorrect() throws Throwable {
        Integer userId = 2;

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userId))
                .andDo(print())
                .andExpect(status().isOk());
    }

}