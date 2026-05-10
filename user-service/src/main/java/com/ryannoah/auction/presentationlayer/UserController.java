package com.ryannoah.auction.presentationlayer;

import com.ryannoah.auction.businesslogiclayer.UserApplicationService;
import com.ryannoah.auction.datamappinglayer.UserMapper;
import com.ryannoah.auction.presentationlayer.dto.CreateUserRequestDTO;
import com.ryannoah.auction.presentationlayer.dto.UpdateUserRequestDTO;
import com.ryannoah.auction.presentationlayer.dto.UserResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserApplicationService userApplicationService;
    private final UserMapper userMapper;

    public UserController(UserApplicationService userApplicationService, UserMapper userMapper) {
        this.userApplicationService = userApplicationService;
        this.userMapper = userMapper;
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody CreateUserRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userMapper.toResponseDTO(userApplicationService.createUser(userMapper.toCreateCommand(request))));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable String userId, @Valid @RequestBody UpdateUserRequestDTO request) {
        return ResponseEntity.ok(userMapper.toResponseDTO(userApplicationService.updateUser(userId, userMapper.toUpdateCommand(request))));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
        userApplicationService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> getUser(@PathVariable String userId) {
        return ResponseEntity.ok(userMapper.toResponseDTO(userApplicationService.getUser(userId)));
    }

    @GetMapping
    public ResponseEntity<java.util.List<UserResponseDTO>> listUsers() {
        return ResponseEntity.ok(userApplicationService.listUsers().stream().map(userMapper::toResponseDTO).toList());
    }
}
