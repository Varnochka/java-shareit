package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users")
public class UserController {
    private final UserService userService;

    @GetMapping
    public List<UserDto> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{userId}")
    public UserDto getUser(@PathVariable(name = "userId") Long userId) {
        return userService.getUserById(userId);
    }

    @PostMapping
    public UserDto createUser(@RequestBody UserDto userRequest) {
        return userService.createUser(userRequest);
    }

    @PatchMapping("/{userId}")
    public UserDto updateUser(@RequestBody UserDto userRequest, @PathVariable(name = "userId") Long userId) {
        return userService.updateUserById(userRequest, userId);
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable(name = "userId") Long userId) {
        userService.deleteUserById(userId);
    }
}
