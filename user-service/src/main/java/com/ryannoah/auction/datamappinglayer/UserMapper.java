package com.ryannoah.auction.datamappinglayer;

import com.ryannoah.auction.businesslogiclayer.UserApplicationService;
import com.ryannoah.auction.domain.User;
import com.ryannoah.auction.presentationlayer.dto.AddressResponseDTO;
import com.ryannoah.auction.presentationlayer.dto.CreateUserRequestDTO;
import com.ryannoah.auction.presentationlayer.dto.UpdateUserRequestDTO;
import com.ryannoah.auction.presentationlayer.dto.UserResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserApplicationService.CreateUserCommand toCreateCommand(CreateUserRequestDTO request) {
        return new UserApplicationService.CreateUserCommand(
                request.username(),
                request.email(),
                request.verified(),
                request.address().street(),
                request.address().city(),
                request.address().zipCode(),
                request.address().country()
        );
    }

    public UserApplicationService.UpdateUserCommand toUpdateCommand(UpdateUserRequestDTO request) {
        return new UserApplicationService.UpdateUserCommand(
                request.username(),
                request.email(),
                request.verified(),
                request.address().street(),
                request.address().city(),
                request.address().zipCode(),
                request.address().country()
        );
    }

    public UserResponseDTO toResponseDTO(User user) {
        return new UserResponseDTO(
                user.getUserId().value(),
                user.getUsername(),
                user.getEmail().address(),
                user.getRegistrationDate(),
                user.isVerified(),
                user.getRating().rating(),
                user.getRating().totalReviews(),
                new AddressResponseDTO(
                        user.getAddress().street(),
                        user.getAddress().city(),
                        user.getAddress().zipCode(),
                        user.getAddress().country()
                )
        );
    }
}
