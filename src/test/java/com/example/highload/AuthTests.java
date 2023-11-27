package com.example.highload;

import com.example.highload.model.inner.User;
import com.example.highload.model.network.JwtRequest;
import com.example.highload.repos.RoleRepository;
import com.example.highload.security.jwt.JwtUtil;
import com.example.highload.services.AuthenticationService;
import com.example.highload.services.UserService;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.stream.Stream;
import static io.restassured.RestAssured.given;

@Testcontainers
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
    @Value("${artist.login}")
    private static String artistLogin;
    @Value("${artist.password}")
    private static String artistPassword;
    @Value("${client.login}")
    private static String clientLogin;
    @Value("${client.password}")
    private static String clientPassword;


    @Container
    private static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("highload")
            .withUsername("high_user")
            .withPassword("high_user")
            .withInitScript("/Users/abogatov/IdeaProjects/highload/src/main/resources/db/changelog/v1/changelog.yaml");

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
    }

    @AfterAll
    static void pgStop() {
        postgreSQLContainer.stop();
    }

    @Test
    void authAdminCorrect() {
        String adminJwt = authenticationService.authProcess(adminLogin, adminPassword, "ADMIN");
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
        Assertions.assertThrows(BadCredentialsException.class, () -> {
            authenticationService.authProcess(login, adminPassword, "ADMIN");
        });
    }

    @ParameterizedTest
    @MethodSource("passwordProvider")
    void authAdminBadPassword(String password) {
        Assertions.assertThrows(BadCredentialsException.class, () -> {
            authenticationService.authProcess(adminLogin, password, "ADMIN");
        });
    }

    //                .header("Authorization", "Bearer " + adminToken)

//    @Test
//    void authCorrect() {
//        ExtractableResponse<Response> response =
//                given().header("Content-type", "application/json").and().body(new JwtRequest(adminLogin, adminPassword))
//                .when().post("/api/app/user/login")
//                .then().extract();
//        Assertions.assertAll(
//                () ->
//                () ->
//                () ->
//        );
//    }

    @Test
    void authBad() {

    }



    @Test
    void authAdmin() {
        String adminJwt = authenticationService.authProcess(adminLogin, adminPassword, "ADMIN");
        User user = userService.findByLogin(adminLogin);
        Assertions.assertAll(
                () -> Assertions.assertDoesNotThrow(() -> jwtUtil.getLoginFromJwtToken(adminJwt)),
                () -> Assertions.assertEquals(jwtUtil.getLoginFromJwtToken(adminJwt), adminLogin),
                () -> Assertions.assertTrue(jwtUtil.getRoleFromJwtToken(adminJwt).contains(user.getRole().getName().toString()))
        );
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


}
