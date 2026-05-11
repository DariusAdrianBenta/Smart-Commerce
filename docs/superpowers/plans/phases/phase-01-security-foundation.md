# Phase 01 — Security Foundation

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` or `superpowers:executing-plans`.

**Goal:** Añadir las dependencias necesarias, crear la entidad `User`, configurar Spring Security con JWT stateless y sembrar el administrador inicial.

**Architecture:** Esta fase sienta las bases de seguridad para todas las demás. No tiene endpoints propios — solo infraestructura. La entidad `User` implementa `UserDetails` de Spring Security. El filtro JWT intercepta cada request y autentica al usuario antes de llegar al controller.

**Tech Stack:** jjwt 0.12.6 · BCryptPasswordEncoder · Spring Security 7 · Lombok · JPA

**Pre-requisitos:** Ninguno. Es la primera fase.

**Dependen de esta fase:** Todas las demás.

---

## Archivos a crear / modificar

| Acción | Archivo |
|--------|---------|
| Modificar | `pom.xml` |
| Modificar | `src/main/resources/application.properties` |
| Crear | `src/main/java/com/smartcommerce/user/entity/Role.java` |
| Crear | `src/main/java/com/smartcommerce/user/entity/User.java` |
| Crear | `src/main/java/com/smartcommerce/user/repository/UserRepository.java` |
| Crear | `src/main/java/com/smartcommerce/security/UserDetailsServiceImpl.java` |
| Crear | `src/main/java/com/smartcommerce/security/JwtService.java` |
| Crear | `src/main/java/com/smartcommerce/security/JwtAuthFilter.java` |
| Crear | `src/main/java/com/smartcommerce/security/SecurityConfig.java` |
| Crear | `src/main/java/com/smartcommerce/config/DataInitializer.java` |
| Crear | `src/test/java/com/smartcommerce/security/JwtServiceTest.java` |

---

## Task 1: Añadir dependencias al pom.xml

### Task 1.1: Añadir JWT, Stripe y Spring Mail

- [ ] Abrir `pom.xml` y añadir dentro de `<dependencies>`:

```xml
<!-- JWT -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.6</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>

<!-- Stripe -->
<dependency>
    <groupId>com.stripe</groupId>
    <artifactId>stripe-java</artifactId>
    <version>25.3.0</version>
</dependency>

<!-- Spring Mail -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
```

- [ ] Verificar que compila:

```bash
./mvnw compile -DskipTests
```

Resultado esperado: `BUILD SUCCESS`

- [ ] Commit:

```bash
git add pom.xml
git commit -m "chore: add jwt, stripe and mail dependencies"
```

---

## Task 2: Actualizar application.properties

- [ ] Añadir al final de `src/main/resources/application.properties`:

```properties
# JWT
jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
jwt.expiration=3600000

# Admin seed
app.admin.email=admin@smartcommerce.com
app.admin.password=Admin1234!

# Stripe (reemplazar con tus claves de test de https://dashboard.stripe.com)
stripe.secret.key=sk_test_REPLACE_WITH_YOUR_KEY
stripe.publishable.key=pk_test_REPLACE_WITH_YOUR_KEY

# Spring Mail (Gmail SMTP)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

> **Nota:** El `jwt.secret` es una cadena Base64 de 256 bits. Puedes generar otra con:
> `openssl rand -base64 32`

- [ ] Commit:

```bash
git add src/main/resources/application.properties
git commit -m "chore: add jwt, stripe and mail configuration properties"
```

---

## Task 3: Crear Role enum

- [ ] Crear `src/main/java/com/smartcommerce/user/entity/Role.java`:

```java
package com.smartcommerce.user.entity;

public enum Role {
    ADMIN,
    USER
}
```

---

## Task 4: Crear entidad User

- [ ] Crear `src/main/java/com/smartcommerce/user/entity/User.java`:

```java
package com.smartcommerce.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private LocalDate birthDate;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private boolean active;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return active;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
```

---

## Task 5: Crear UserRepository

- [ ] Crear `src/main/java/com/smartcommerce/user/repository/UserRepository.java`:

```java
package com.smartcommerce.user.repository;

import com.smartcommerce.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
```

---

## Task 6: Crear UserDetailsServiceImpl

- [ ] Crear `src/main/java/com/smartcommerce/security/UserDetailsServiceImpl.java`:

```java
package com.smartcommerce.security;

import com.smartcommerce.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }
}
```

---

## Task 7: Crear JwtService

- [ ] Crear `src/main/java/com/smartcommerce/security/JwtService.java`:

```java
package com.smartcommerce.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expirationMs;

    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }
}
```

- [ ] **Escribir el test** `src/test/java/com/smartcommerce/security/JwtServiceTest.java`:

```java
package com.smartcommerce.security;

import com.smartcommerce.user.entity.Role;
import com.smartcommerce.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;
    private User testUser;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey",
                "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(jwtService, "expirationMs", 3600000L);

        testUser = User.builder()
                .id(1L)
                .email("user@test.com")
                .password("encoded")
                .role(Role.USER)
                .active(true)
                .build();
    }

    @Test
    void generateToken_shouldReturnNonNullToken() {
        String token = jwtService.generateToken(testUser);
        assertThat(token).isNotNull().isNotBlank();
    }

    @Test
    void extractUsername_shouldReturnUserEmail() {
        String token = jwtService.generateToken(testUser);
        String username = jwtService.extractUsername(token);
        assertThat(username).isEqualTo("user@test.com");
    }

    @Test
    void isTokenValid_withValidToken_shouldReturnTrue() {
        String token = jwtService.generateToken(testUser);
        assertThat(jwtService.isTokenValid(token, testUser)).isTrue();
    }

    @Test
    void isTokenValid_withWrongUser_shouldReturnFalse() {
        String token = jwtService.generateToken(testUser);
        User otherUser = User.builder().email("other@test.com").active(true).role(Role.USER).build();
        assertThat(jwtService.isTokenValid(token, otherUser)).isFalse();
    }
}
```

- [ ] Ejecutar el test:

```bash
./mvnw test -Dtest=JwtServiceTest
```

Resultado esperado: `Tests run: 4, Failures: 0, Errors: 0`

---

## Task 8: Crear JwtAuthFilter

- [ ] Crear `src/main/java/com/smartcommerce/security/JwtAuthFilter.java`:

```java
package com.smartcommerce.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        String email = jwtService.extractUsername(token);

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            if (jwtService.isTokenValid(token, userDetails)) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
```

---

## Task 9: Crear SecurityConfig

- [ ] Crear `src/main/java/com/smartcommerce/security/SecurityConfig.java`:

```java
package com.smartcommerce.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/products/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/products/*/reviews").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/payments/test-cards").permitAll()
                .requestMatchers(
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/swagger-ui.html"
                ).permitAll()
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}
```

---

## Task 10: Crear DataInitializer (seed del admin)

- [ ] Crear `src/main/java/com/smartcommerce/config/DataInitializer.java`:

```java
package com.smartcommerce.config;

import com.smartcommerce.user.entity.Role;
import com.smartcommerce.user.entity.User;
import com.smartcommerce.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmail(adminEmail)) {
            User admin = User.builder()
                    .firstName("Admin")
                    .lastName("SmartCommerce")
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .phone("000000000")
                    .birthDate(LocalDate.of(1990, 1, 1))
                    .role(Role.ADMIN)
                    .active(true)
                    .build();
            userRepository.save(admin);
            log.info("Admin user created: {}", adminEmail);
        }
    }
}
```

---

## Task 11: Verificar arranque

- [ ] Levantar Docker:

```bash
docker-compose up -d
```

- [ ] Arrancar la aplicación:

```bash
./mvnw spring-boot:run
```

Resultado esperado en los logs:
```
Admin user created: admin@smartcommerce.com
Started SmartcommerceApplication
```

- [ ] Commit final de la fase:

```bash
git add src/main/java/com/smartcommerce/user/ \
        src/main/java/com/smartcommerce/security/ \
        src/main/java/com/smartcommerce/config/ \
        src/test/java/com/smartcommerce/security/
git commit -m "feat(security): add JWT auth filter, SecurityConfig, User entity and admin seed"
```
