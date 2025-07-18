package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.model.User;
import org.example.service.UserService;
import org.example.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 用户认证控制器。
 * <p>
 * 负责处理用户的注册和登录请求。
 * 所有在此控制器下的端点都是公开的，无需身份验证即可访问。
 * </p>
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "用户认证接口", description = "提供用户注册和登录功能")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户注册接口。
     * <p>
     * 接收邮箱、用户名和密码，并创建一个新账户。
     * 密码会在 {@link UserService} 中进行加密处理。
     * </p>
     *
     * @param user 包含邮箱、用户名和密码的用户注册信息。
     * @return 注册成功后的用户信息 (密码已被加密，不会返回)。
     */
    @Operation(summary = "用户注册", description = "接收用户信息并创建一个新账户")
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        // 检查邮箱是否已存在
        if (userService.findByEmail(user.getEmail()) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("该邮箱已被注册");
        }
        // 检查用户名是否已存在
        if (userService.findByUsername(user.getUsername()) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("该用户名已被使用");
        }

        User registeredUser = userService.register(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(registeredUser);
    }

    /**
     * 用户登录接口。
     * <p>
     * 接收邮箱和密码，验证成功后返回一个 JWT (JSON Web Token)。
     * 前端需要保存此 Token，并在后续所有需要认证的请求头中携带 (Authorization: Bearer <token>)。
     * </p>
     *
     * @param loginRequest 包含邮箱 (email) 和密码 (password) 的登录请求体。
     * @return 包含生成的 JWT 的响应体。
     */
    @Operation(summary = "用户登录", description = "验证用户凭证并返回 JWT")
    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody Map<String, String> loginRequest) {
        String email = loginRequest.get("email");
        String password = loginRequest.get("password");

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("邮箱或密码不正确");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("登录失败: " + e.getMessage());
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        final String jwt = jwtUtil.generateToken(userDetails);

        return ResponseEntity.ok(Map.of("token", jwt));
    }
}