package org.example.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import java.nio.charset.StandardCharsets;

/**
 * JWT (JSON Web Token) 工具类。
 * <p>
 * 负责 JWT 的生成、解析、验证和相关信息的提取。
 * </p>
 */
@Component
public class JwtUtil {

    /**
     * JWT 签名密钥，从系统环境变量中注入。
     * 用于对 JWT 进行签名和验证，确保 Token 的完整性和真实性。
     * 这是一个敏感信息，应妥善保管。
     */
    @Value("#{systemEnvironment['JWT_SECRET']}")
    private String secret;

    /**
     * 获取用于签名的密钥。
     * <p>
     * 使用 HMAC-SHA 算法生成密钥。
     * </p>
     *
     * @return {@link SecretKey} 对象。
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 从 JWT Token 中提取用户名 (Subject)。
     *
     * @param token JWT 字符串。
     * @return 提取到的用户名。
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * 从 JWT Token 中提取过期时间。
     *
     * @param token JWT 字符串。
     * @return 提取到的过期时间 {@link Date} 对象。
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * 从 JWT Token 中提取指定类型的 Claim (声明)。
     *
     * @param token        JWT 字符串。
     * @param claimsResolver 用于从 {@link Claims} 对象中解析出特定值的函数。
     * @param <T>          Claim 的类型。
     * @return 提取到的 Claim 值。
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 解析 JWT Token，获取所有 Claims (声明)。
     *
     * @param token JWT 字符串。
     * @return 包含所有声明的 {@link Claims} 对象。
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
    }

    /**
     * 判断 JWT Token 是否已过期。
     *
     * @param token JWT 字符串。
     * @return 如果 Token 已过期则返回 true，否则返回 false。
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * 根据用户详细信息生成 JWT Token。
     *
     * @param userDetails 包含用户信息的 {@link UserDetails} 对象。
     * @return 生成的 JWT 字符串。
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        // 可以在这里添加额外的 claims，例如用户角色等
        return createToken(claims, userDetails.getUsername());
    }

    /**
     * 创建 JWT Token 的核心方法。
     *
     * @param claims  自定义的声明 (如用户角色)。
     * @param subject Token 的主题，通常是用户名。
     * @return 生成的 JWT 字符串。
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims) // 设置自定义声明
                .subject(subject) // 设置主题 (通常是用户名)
                .issuedAt(new Date(System.currentTimeMillis())) // 设置签发时间
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 设置过期时间 (这里是 10 小时)
                .signWith(getSigningKey(), Jwts.SIG.HS256) // 使用密钥和 HS256 算法签名
                .compact(); // 压缩并生成最终的 JWT 字符串
    }

    /**
     * 验证 JWT Token 是否有效。
     * <p>
     * 检查用户名是否匹配且 Token 未过期。
     * </p>
     *
     * @param token       JWT 字符串。
     * @param userDetails 包含用户信息的 {@link UserDetails} 对象。
     * @return 如果 Token 有效则返回 true，否则返回 false。
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}
