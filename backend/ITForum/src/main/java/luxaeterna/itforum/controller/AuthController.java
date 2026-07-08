package luxaeterna.itforum.controller;

import jakarta.validation.Valid;
import luxaeterna.itforum.common.Result;
import luxaeterna.itforum.dto.request.LoginRequest;
import luxaeterna.itforum.dto.request.RefreshRequest;
import luxaeterna.itforum.dto.request.RegisterRequest;
import luxaeterna.itforum.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public Result<?> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success(userService.register(request));
    }

    @PostMapping("/login")
    public Result<?> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(userService.login(request));
    }

    @PostMapping("/refresh")
    public Result<?> refresh(@Valid @RequestBody RefreshRequest request) {
        return Result.success(userService.refreshToken(request));
    }
}
