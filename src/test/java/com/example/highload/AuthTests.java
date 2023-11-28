package com.example.highload;

import com.example.highload.model.inner.User;
import com.example.highload.model.network.JwtRequest;
import com.example.highload.model.network.JwtResponse;
import com.example.highload.repos.RoleRepository;
import com.example.highload.security.jwt.JwtUtil;
import com.example.highload.services.AuthenticationService;
import com.example.highload.services.UserService;
import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.stream.Stream;

import static io.restassured.RestAssured.given;

@Testcontainers
@EnableConfigurationProperties
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthTests {

    @LocalServerPort
    private Integer port;

    @Autowired
    public AuthenticationService authenticationService;

    @Autowired
    public UserService userService;

    @Autowired
    public RoleRepository roleRepository;

    @Autowired
    public JwtUtil jwtUtil;

    @Value("${admin.login}")
    private static String adminLogin;
    @Value("${admin.password}")
    private static String adminPassword;
    @Value("${admin.role}")
    private static String adminRole;
    @Value("${artist.login}")
    private static String artistLogin;
    @Value("${artist.password}")
    private static String artistPassword;
    @Value("${artist.role}")
    private static String artistRole;
    @Value("${client.login}")
    private static String clientLogin;
    @Value("${client.password}")
    private static String clientPassword;
    @Value("${client.role}")
    private static String clientRole;


    @Container
    @ServiceConnection
    private static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("highload")
            .withUsername("high_user")
            .withPassword("high_user");

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.driver-class-name", postgreSQLContainer::getDriverClassName);
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
    }

    @BeforeAll
    static void pgStart() {
        postgreSQLContainer.start();
    }

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost:" + port;
        RestAssured.defaultParser = Parser.JSON;
    }

    @AfterAll
    static void pgStop() {
        postgreSQLContainer.stop();
    }

    @Test
    void authAdminCorrect() {
        String adminJwt = authenticationService.authProcess(adminLogin, adminPassword, adminRole);
        User user = userService.findByLogin(adminLogin);
        Assertions.assertAll(
                () -> Assertions.assertDoesNotThrow(() -> jwtUtil.getLoginFromJwtToken(adminJwt)),
                () -> Assertions.assertEquals(jwtUtil.getLoginFromJwtToken(adminJwt), user.getLogin()),
                () -> Assertions.assertTrue(jwtUtil.getRoleFromJwtToken(adminJwt).contains(user.getRole().getName().toString()))
        );
    }

    @ParameterizedTest
    @MethodSource("loginProvider")
    void authAdminBadLogin(String login) {
        Assertions.assertThrows(BadCredentialsException.class, () ->
                authenticationService.authProcess(login, adminPassword, adminRole)
        );
    }

    @ParameterizedTest
    @MethodSource("passwordProvider")
    void authAdminBadPassword(String password) {
        Assertions.assertThrows(BadCredentialsException.class, () ->
                authenticationService.authProcess(adminLogin, password, adminRole)
        );
    }

    @ParameterizedTest
    @MethodSource("userProvider")
    void authRESTCorrect(String login, String password, String role) {
        ExtractableResponse<Response> response =
                given()
                        .header("Content-type", "application/json")
                        .and()
                        .body(new JwtRequest(login, password, role))
                        .when()
                        .post("/api/app/user/login")
                        .then()
                        .extract();
        User user = userService.findByLogin(login);
        String tokenResponse = response.body().as(JwtResponse.class).getToken();
        Assertions.assertAll(
                () -> Assertions.assertDoesNotThrow(() -> jwtUtil.getLoginFromJwtToken(tokenResponse)),
                () -> Assertions.assertEquals(jwtUtil.getLoginFromJwtToken(tokenResponse), user.getLogin()),
                () -> Assertions.assertTrue(jwtUtil.getRoleFromJwtToken(tokenResponse).contains(user.getRole().getName().toString())),
                () -> Assertions.assertEquals(response.response().getStatusCode(), HttpStatus.OK.value())
        );
    }

    @Test
    void registerAPICorrect() {
        ExtractableResponse<Response> response =
                given()
                        .header("Content-type", "application/json")
                        .and()
                        .body(new JwtRequest("client2", "client2", "CLIENT"))
                        .when()
                        .post("/api/app/user/register")
                        .then()
                        .extract();
        Assertions.assertEquals(response.response().getStatusCode(), HttpStatus.OK.value());

        authRESTCorrect("client2", "client2", "CLIENT");

//        ExtractableResponse<Response> response2 =
//                given()
//                        .header("Content-type", "application/json")
//                        .and()
//                        .body(new JwtRequest("client2", "client2", "CLIENT"))
//                        .when()
//                        .post("/api/app/user/login")
//                        .then()
//                        .extract();
//        User user = userService.findByLogin("client2");
//        String tokenResponse = response2.body().as(JwtResponse.class).getToken();
//        Assertions.assertAll(
//                () -> Assertions.assertDoesNotThrow(() -> jwtUtil.getLoginFromJwtToken(tokenResponse)),
//                () -> Assertions.assertEquals(jwtUtil.getLoginFromJwtToken(tokenResponse), user.getLogin()),
//                () -> Assertions.assertTrue(jwtUtil.getRoleFromJwtToken(tokenResponse).contains(user.getRole().getName().toString())),
//                () -> Assertions.assertEquals(response2.response().getStatusCode(), HttpStatus.OK.value())
//        );
    }

    @ParameterizedTest
    @MethodSource("userProvider")
    void authRESTBad(String login, String password, String role) {
        ExtractableResponse<Response> response =
                given()
                        .header("Content-type", "application/json")
                        .and()
                        .body(new JwtRequest(login + "1", password, role))
                        .when()
                        .post("/api/app/user/login")
                        .then()
                        .extract();
        Assertions.assertEquals(response.response().getStatusCode(), HttpStatus.UNAUTHORIZED.value());
    }

    private static Stream<String> loginProvider() {
        return Stream.of(
                clientLogin,
                artistLogin,
                adminPassword,
                adminLogin + "1"
        );
    }

    private static Stream<String> passwordProvider() {
        return Stream.of(
                clientPassword,
                artistPassword,
                adminLogin,
                adminPassword + "1"
        );
    }

    private static Stream<?> userProvider() {
        return Stream.of(
                Arguments.of(adminLogin, adminPassword, adminRole),
                Arguments.of(artistLogin, artistPassword, artistRole),
                Arguments.of(clientLogin, clientPassword, clientRole)
        );
    }


}
