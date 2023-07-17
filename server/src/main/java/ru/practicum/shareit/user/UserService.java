package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto createUser(UserDto userRequest);

    List<UserDto> getAllUsers();

    UserDto updateUserById(UserDto userRequest, Long userId);

    UserDto getUserById(Long userId);

    User findUserById(Long userId);

    void deleteUserById(Long id);

    void checkExistUserById(Long userId);
}
