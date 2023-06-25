package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.practicum.shareit.booking.dto.BookingRequest;
import ru.practicum.shareit.booking.dto.BookingResponse;
import ru.practicum.shareit.exception.NoCorrectRequestException;
import ru.practicum.shareit.exception.NoFoundObjectException;
import ru.practicum.shareit.exception.NoValidArgumentException;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@WebMvcTest(BookingController.class)
class BookingControllerTest {
    @Autowired
    MockMvc mvc;

    @MockBean
    BookingService bookingService;

    @Autowired
    ObjectMapper objectMapper;

    String userIdHeader = "X-Sharer-User-Id";

    @Test
    void createBooking_statusNotFound_itemDontExist() throws Exception {
        BookingRequest bookingRequest = BookingRequest.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(10))
                .end(LocalDateTime.now().plusDays(30))
                .build();

        doThrow(NoFoundObjectException.class)
                .when(bookingService).createBooking(anyLong(), any(BookingRequest.class));

        mvc.perform(post("/bookings")
                        .header(userIdHeader, 1)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(bookingRequest)))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").doesNotExist());
    }

    @Test
    void createBooking_statusBadRequest_itemAvailableIsFalse() throws Exception {
        BookingRequest bookingRequest = BookingRequest.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(10))
                .end(LocalDateTime.now().plusDays(30))
                .build();

        doThrow(NoCorrectRequestException.class)
                .when(bookingService).createBooking(anyLong(), any(BookingRequest.class));

        mvc.perform(post("/bookings")
                        .header(userIdHeader, 1)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(bookingRequest)))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").doesNotExist());
    }

    @Test
    void createBooking_statusBadRequest_timeIncorrect() throws Exception {
        BookingRequest bookingRequest = BookingRequest.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(10))
                .end(LocalDateTime.now().minusDays(5))
                .build();

        doThrow(NoCorrectRequestException.class)
                .when(bookingService).createBooking(anyLong(), any(BookingRequest.class));

        mvc.perform(post("/bookings")
                        .header(userIdHeader, 1)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(bookingRequest)))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").doesNotExist());
    }

    @Test
    void createBooking_statusOk_itemExist() throws Exception {
        BookingRequest bookingRequest = BookingRequest.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(10))
                .end(LocalDateTime.now().plusDays(30))
                .build();

        BookingResponse bookingResponse = BookingResponse.builder()
                .id(10L)
                .start(LocalDateTime.now().plusDays(10))
                .end(LocalDateTime.now().plusDays(30))
                .booker(new BookingResponse.Booker(1L, "Mike"))
                .item(new BookingResponse.Item(1L, "Book"))
                .build();

        when(bookingService.createBooking(anyLong(), any(BookingRequest.class)))
                .thenReturn(bookingResponse);

        mvc.perform(post("/bookings")
                        .header(userIdHeader, 1)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(bookingRequest)))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.booker.name").value("Mike"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.item.name").value("Book"));
    }

    @Test
    void changeBookingStatus_statusNotFound_itemDontExist() throws Exception {

        doThrow(NoFoundObjectException.class)
                .when(bookingService).updateStatusById(anyLong(), anyBoolean(), anyLong());

        mvc.perform(patch("/bookings/1")
                        .header(userIdHeader, 1)
                        .param("approved", "true"))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").doesNotExist());
    }

    @Test
    void changeBookingStatus_statusBadRequest_statusBookingIsNotWaiting() throws Exception {

        doThrow(NoCorrectRequestException.class)
                .when(bookingService).updateStatusById(anyLong(), anyBoolean(), anyLong());

        mvc.perform(patch("/bookings/1")
                        .header(userIdHeader, 1)
                        .param("approved", "true"))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").doesNotExist());
    }

    @Test
    void changeBookingStatus_statusOk_statusBookingIsNotWaiting() throws Exception {
        BookingResponse bookingResponse = BookingResponse.builder()
                .id(10L)
                .start(LocalDateTime.now().plusDays(10))
                .end(LocalDateTime.now().plusDays(30))
                .status(BookingStatus.APPROVED)
                .booker(new BookingResponse.Booker(1L, "Mike"))
                .item(new BookingResponse.Item(1L, "Book"))
                .build();

        when(bookingService.updateStatusById(anyLong(), anyBoolean(), anyLong()))
                .thenReturn(bookingResponse);

        mvc.perform(patch("/bookings/1")
                        .header(userIdHeader, 1)
                        .param("approved", "true"))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(BookingStatus.APPROVED.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.booker.name").value("Mike"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.item.name").value("Book"));
    }

    @Test
    void getBookingInfo_statusOk_bookingExist() throws Exception {
        BookingResponse bookingResponse = BookingResponse.builder()
                .id(10L)
                .start(LocalDateTime.now().plusDays(10))
                .end(LocalDateTime.now().plusDays(30))
                .status(BookingStatus.APPROVED)
                .booker(new BookingResponse.Booker(1L, "Mike"))
                .item(new BookingResponse.Item(1L, "Book"))
                .build();

        when(bookingService.getBookingById(anyLong(), anyLong()))
                .thenReturn(bookingResponse);

        mvc.perform(MockMvcRequestBuilders.get("/bookings/1")
                        .header(userIdHeader, 1))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(BookingStatus.APPROVED.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.booker.name").value("Mike"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.item.name").value("Book"));
    }

    @Test
    void getBookingInfo_statusNotFound_bookingDontExist() throws Exception {
        doThrow(NoFoundObjectException.class)
                .when(bookingService).getBookingById(anyLong(), anyLong());

        mvc.perform(MockMvcRequestBuilders.get("/bookings/1")
                        .header(userIdHeader, 1))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").doesNotExist());
    }

    @Test
    void getAllBookingsBooker_statusOkAndEmptyList_bookerExist() throws Exception {
        BookingResponse bookingResponse = BookingResponse.builder()
                .id(10L)
                .start(LocalDateTime.now().plusDays(10))
                .end(LocalDateTime.now().plusDays(30))
                .status(BookingStatus.APPROVED)
                .booker(new BookingResponse.Booker(1L, "Mike"))
                .item(new BookingResponse.Item(1L, "Book"))
                .build();

        when(bookingService.getBookingsByBookerId(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(bookingResponse));

        mvc.perform(MockMvcRequestBuilders.get("/bookings")
                        .header(userIdHeader, 1)
                        .param("from", "1")
                        .param("size", "1")
                        .param("state", "ALL"))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].status").value(BookingStatus.APPROVED.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].booker.name").value("Mike"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].item.name").value("Book"));
    }

    @Test
    void getAllBookingsBooker_statusOkAndEmptyList_requestParamIncorrect() throws Exception {
        doThrow(NoValidArgumentException.class)
                .when(bookingService)
                .getBookingsByBookerId(anyLong(), anyString(), anyInt(), anyInt());

        mvc.perform(MockMvcRequestBuilders.get("/bookings")
                        .header(userIdHeader, 1)
                        .param("from", "-1")
                        .param("size", "-1")
                        .param("state", "ALL"))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0]").doesNotExist());
    }

    @Test
    void getAllBookingsOwner_statusOkAndEmptyList_bookerExist() throws Exception {
        BookingResponse bookingResponse = BookingResponse.builder()
                .id(10L)
                .start(LocalDateTime.now().plusDays(10))
                .end(LocalDateTime.now().plusDays(30))
                .status(BookingStatus.APPROVED)
                .booker(new BookingResponse.Booker(1L, "Mike"))
                .item(new BookingResponse.Item(1L, "Book"))
                .build();

        when(bookingService.getBookingsByOwnerId(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(bookingResponse));

        mvc.perform(MockMvcRequestBuilders.get("/bookings/owner")
                        .header(userIdHeader, 1)
                        .param("from", "1")
                        .param("size", "1")
                        .param("state", "ALL"))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].status").value(BookingStatus.APPROVED.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].booker.name").value("Mike"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].item.name").value("Book"));
    }

    @Test
    void getAllBookingsOwner_statusOkAndEmptyList_requestParamIncorrect() throws Exception {
        doThrow(NoValidArgumentException.class)
                .when(bookingService)
                .getBookingsByOwnerId(anyLong(), anyString(), anyInt(), anyInt());

        mvc.perform(MockMvcRequestBuilders.get("/bookings/owner")
                        .header(userIdHeader, 1)
                        .param("from", "-1")
                        .param("size", "-1")
                        .param("state", "ALL"))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0]").doesNotExist());
    }
}