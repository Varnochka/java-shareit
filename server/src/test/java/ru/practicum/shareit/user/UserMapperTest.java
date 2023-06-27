package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    @Test
    void objectToDto() {
        User user = User.builder()
                .id(1L)
                .name("Mike")
                .email("mike@mail.ru")
                .build();
        UserDto result =  UserMapper.objectToDto(user);

        assertEquals(user.getName(), result.getName());
        assertEquals(user.getEmail(), result.getEmail());
        assertEquals(user.getId(), result.getId());
    }

    @Test
    void dtoToObject() {
        UserDto request = UserDto.builder()
                .name("Mike")
                .email("mike@mail.ru")
                .build();
        User result =  UserMapper.dtoToObject(request);

        assertEquals(request.getName(), result.getName());
        assertEquals(request.getEmail(), result.getEmail());
        assertEquals(request.getId(), result.getId());
    }

    @Test
    void testObjectToDto() {
        User user1 = User.builder()
                .id(1L)
                .name("Mike")
                .email("mike@mail.ru")
                .build();

        User user2 = User.builder()
                .id(2L)
                .name("Tom")
                .email("tom@mail.ru")
                .build();

        List<UserDto> result = UserMapper.objectToDto(List.of(user1, user2));

        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
        assertEquals(user1.getName(), result.get(0).getName());

    }
}