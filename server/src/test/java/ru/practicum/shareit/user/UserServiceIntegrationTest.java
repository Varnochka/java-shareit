package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import ru.practicum.shareit.exception.NoFoundObjectException;
import ru.practicum.shareit.user.dto.UserDto;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserServiceIntegrationTest {
    private final UserService userService;

    @Test
    void createUser_shouldException_createUserWithNotUniqueEmail() {
        UserDto userRequest1 = UserDto.builder().name("username").email("uniquemail@mail.mail").build();
        UserDto userRequest2 = UserDto.builder().name("secondUser").email("uniquemail@mail.mail").build();

        UserDto user1 = userService.createUser(userRequest1);

        assertThrows(DataIntegrityViolationException.class, () -> userService.createUser(userRequest2));

        User foundUser1 = userService.findUserById(1L);
        assertEquals("uniquemail@mail.mail", user1.getEmail());
        assertNotNull(foundUser1);

        assertThrows(NoFoundObjectException.class, () -> userService.findUserById(2L));
    }

}