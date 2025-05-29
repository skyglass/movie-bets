package net.skycomposer.moviebets.movie.controller;

import java.security.Principal;
import java.util.Optional;

import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.skycomposer.moviebets.common.dto.movie.UserExtraRequest;
import net.skycomposer.moviebets.movie.dao.entity.UserExtra;
import net.skycomposer.moviebets.movie.service.UserExtraService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/userextras")
public class UserExtraController {

    private final UserExtraService userExtraService;

    @GetMapping("/me")
    public UserExtra getUserExtra(Principal principal) {
        return userExtraService.validateAndGetUserExtra(principal.getName());
    }

    @PostMapping("/me")
    public UserExtra saveUserExtra(@Valid @RequestBody UserExtraRequest updateUserExtraRequest,
                                   Principal principal) {
        Optional<UserExtra> userExtraOptional = userExtraService.getUserExtra(principal.getName());
        UserExtra userExtra = userExtraOptional.orElseGet(() -> new UserExtra(principal.getName()));
        userExtra.setAvatar(updateUserExtraRequest.getAvatar());
        return userExtraService.saveUserExtra(userExtra);
    }
}
