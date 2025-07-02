package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.model.User;
import org.example.service.UserService;
import org.example.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 用户认证控制器
 * <p>
 * 负责处理用户的注册和登录请求。
 * 所有在此控制器下的端点都是公开的，无需身份验证即可访问。
 * </p>
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "用户认证接口", description = "提供用户注册和登录功能")
public class AuthController {

    /**
     * Spring Security 的核心认证管理器，用于处理登录验证。
     */
    @Autowired
    private AuthenticationManager authenticationManager;

    /**
     * Spring Security 的用户详情服务，用于根据用户名加载用户数据。
     */
    @Autowired
    private UserDetailsService userDetailsService;

    /**
     * 自定义的用户服务，处理用户注册等业务逻辑。
     */
    @Autowired
    private UserService userService;

    /**
     * JWT 工具类，用于生成和验证 Token。
     */
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户注册接口。
     *
     * @param user 包含用户名、邮箱、密码的用户注册信息。
     * @return 注册成功后的用户信息 (密码已被加密，不会返回)。
     */
    @Operation(summary = "用户注册", description = "接收用户信息并创建一个新账户")
    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) {
        User registeredUser = userService.register(user);
        return ResponseEntity.ok(registeredUser);
    }

    /**
     * 用户登录接口。
     * <p>
     * 接收用户名和密码，验证成功后返回一个 JWT (JSON Web Token)。
     * 前端需要保存此 Token，并在后续所有需要认证的请求头中携带 (Authorization: Bearer <token>)。
     * </p>
     *
     * @param user 包含用户名 (username) 和密码 (password) 的登录请求体。
     * @return 包含生成的 JWT 的响应体。
     */
    @Operation(summary = "用户登录", description = "验证用户凭证并返回 JWT")
    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody User user) {
        // 1. 使用 AuthenticationManager 对用户名和密码进行认证
        // 如果认证失败，这里会抛出异常，由全局异常处理器捕获
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword())
        );

        // 2. 如果认证成功，根据用户名加载用户的详细信息
        final UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());

        // 3. 使用 JWT 工具类为该用户生成一个新的 Token
        final String jwt = jwtUtil.generateToken(userDetails);

        // 4. 将生成的 Token 放入一个 Map 中，以 JSON 格式返回给前端
        return ResponseEntity.ok(Map.of("token", jwt));
    }
}