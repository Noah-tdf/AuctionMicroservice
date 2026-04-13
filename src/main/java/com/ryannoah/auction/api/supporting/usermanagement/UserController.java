package com.ryannoah.auction.api.supporting.usermanagement;

import com.ryannoah.auction.application.supporting.usermanagement.UserApplicationService;
import com.ryannoah.auction.domain.supporting.usermanagement.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserApplicationService userApplicationService;

    public UserController(UserApplicationService userApplicationService) {
        this.userApplicationService = userApplicationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EntityModel<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        return toResponse(userApplicationService.createUser(toCreateCommand(request)));
    }

    @PutMapping("/{userId}")
    public EntityModel<UserResponse> updateUser(@PathVariable String userId, @Valid @RequestBody UpdateUserRequest request) {
        return toResponse(userApplicationService.updateUser(userId, toUpdateCommand(request)));
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable String userId) {
        userApplicationService.deleteUser(userId);
    }

    @GetMapping("/{userId}")
    public EntityModel<UserResponse> getUser(@PathVariable String userId) {
        return toResponse(userApplicationService.getUser(userId));
    }

    @GetMapping
    public CollectionModel<EntityModel<UserResponse>> listUsers() {
        return CollectionModel.of(
                userApplicationService.listUsers().stream().map(this::toResponse).toList(),
                linkTo(methodOn(UserController.class).listUsers()).withSelfRel(),
                linkTo(methodOn(UserController.class).createUser(null)).withRel("create")
        );
    }

    private UserApplicationService.CreateUserCommand toCreateCommand(CreateUserRequest request) {
        return new UserApplicationService.CreateUserCommand(
                request.username(),
                request.email(),
                request.isVerified(),
                request.address().street(),
                request.address().city(),
                request.address().zipCode(),
                request.address().country()
        );
    }

    private UserApplicationService.UpdateUserCommand toUpdateCommand(UpdateUserRequest request) {
        return new UserApplicationService.UpdateUserCommand(
                request.username(),
                request.email(),
                request.isVerified(),
                request.address().street(),
                request.address().city(),
                request.address().zipCode(),
                request.address().country()
        );
    }

    private EntityModel<UserResponse> toResponse(User user) {
        UserResponse response = new UserResponse(
                user.getUserId().value(),
                user.getUsername(),
                user.getEmail().address(),
                user.getRegistrationDate(),
                user.isVerified(),
                user.getRating().rating(),
                user.getRating().totalReviews(),
                new AddressResponse(
                        user.getAddress().street(),
                        user.getAddress().city(),
                        user.getAddress().zipCode(),
                        user.getAddress().country()
                )
        );
        return EntityModel.of(
                response,
                linkTo(methodOn(UserController.class).getUser(response.userId())).withSelfRel(),
                linkTo(methodOn(UserController.class).createUser(null)).withRel("create"),
                linkTo(methodOn(UserController.class).updateUser(response.userId(), null)).withRel("update"),
                linkTo(UserController.class).slash(response.userId()).withRel("delete")
        );
    }

    public record CreateUserRequest(
            @NotBlank String username,
            @Email @NotBlank String email,
            boolean isVerified,
            @Valid @NotNull AddressRequest address
    ) {
    }

    public record UpdateUserRequest(
            @NotBlank String username,
            @Email @NotBlank String email,
            boolean isVerified,
            @Valid @NotNull AddressRequest address
    ) {
    }

    public record AddressRequest(
            @NotBlank String street,
            @NotBlank String city,
            @NotBlank String zipCode,
            @NotBlank String country
    ) {
    }

    public record UserResponse(
            String userId,
            String username,
            String email,
            java.time.LocalDateTime registrationDate,
            boolean isVerified,
            java.math.BigDecimal rating,
            int totalReviews,
            AddressResponse address
    ) {
    }

    public record AddressResponse(
            String street,
            String city,
            String zipCode,
            String country
    ) {
    }
}
