package com.example.highload;

import com.example.highload.model.inner.User;
import com.example.highload.model.network.JwtRequest;
import com.example.highload.model.network.JwtResponse;
import com.example.highload.model.network.ProfileDto;
import com.example.highload.repos.RoleRepository;
import com.example.highload.repos.UserRepository;
import com.example.highload.security.jwt.JwtUtil;
import com.example.highload.services.AuthenticationService;
import com.example.highload.services.ProfileService;
import com.example.highload.services.UserService;
import com.example.highload.utils.DataTransformer;
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
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserControllerTests {

    @LocalServerPort
    private Integer port;

    @Autowired
    public AuthenticationService authenticationService;
    @Autowired
    public UserRepository userRepository;
    @Autowired
    public RoleRepository roleRepository;
    @Autowired
    public JwtUtil jwtUtil;
    @Autowired
    public ProfileService profileService;
    @Autowired
    public DataTransformer dataTransformer;

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

    private String getToken(String login, String password, String role) {
        User user = userRepository.findByLogin(login).orElseThrow();
        return given()
                .header("Content-type", "application/json")
                .and()
                .body(new JwtRequest(login, password, role))
                .when()
                .post("/api/app/user/login")
                .then()
                .extract().body().as(JwtResponse.class).getToken();
    }

    private ExtractableResponse<Response> getResponse(String postUrl, String login, String password, String role) {
        return given()
                .header("Content-type", "application/json")
                .and()
                .body(new JwtRequest(login, password, role))
                .when()
                .post(postUrl)
                .then()
                .extract();
    }

    @Test
    @Order(1)
    void authAdminCorrect() {
        String adminJwt = authenticationService.authProcess(adminLogin, adminPassword, adminRole);
        User user = userRepository.findByLogin(adminLogin).orElseThrow();
        Assertions.assertAll(
                () -> Assertions.assertDoesNotThrow(() -> jwtUtil.getLoginFromJwtToken(adminJwt)),
                () -> Assertions.assertEquals(jwtUtil.getLoginFromJwtToken(adminJwt), user.getLogin()),
                () -> Assertions.assertTrue(jwtUtil.getRoleFromJwtToken(adminJwt).contains(user.getRole().getName().toString()))
        );
    }

    @ParameterizedTest
    @MethodSource("loginProvider")
    @Order(2)
    void authAdminBadLogin(String login) {
        Assertions.assertThrows(BadCredentialsException.class, () ->
                authenticationService.authProcess(login, adminPassword, adminRole)
        );
    }

    @ParameterizedTest
    @MethodSource("passwordProvider")
    @Order(3)
    void authAdminBadPassword(String password) {
        Assertions.assertThrows(BadCredentialsException.class, () ->
                authenticationService.authProcess(adminLogin, password, adminRole)
        );
    }

    @ParameterizedTest
    @MethodSource("userProvider")
    @Order(4)
    void authRESTCorrect(String login, String password, String role) {
//        ExtractableResponse<Response> response =
//                given()
//                        .header("Content-type", "application/json")
//                        .and()
//                        .body(new JwtRequest(login, password, role))
//                        .when()
//                        .post("/api/app/user/login")
//                        .then()
//                        .extract();
        ExtractableResponse<Response> response = getResponse("/api/app/user/login", login, password, role);
        User user = userRepository.findByLogin(login).orElseThrow();
        String tokenResponse = response.body().as(JwtResponse.class).getToken();
        Assertions.assertAll(
                () -> Assertions.assertEquals(jwtUtil.getLoginFromJwtToken(tokenResponse), user.getLogin()),
                () -> Assertions.assertTrue(jwtUtil.getRoleFromJwtToken(tokenResponse).contains(user.getRole().getName().toString())),
                () -> Assertions.assertEquals(response.response().getStatusCode(), HttpStatus.OK.value())
        );
    }

    @ParameterizedTest
    @MethodSource("userProvider")
    @Order(5)
    void authRESTBad(String login, String password, String role) {
//        ExtractableResponse<Response> response =
//                given()
//                        .header("Content-type", "application/json")
//                        .and()
//                        .body(new JwtRequest(login + "1", password, role))
//                        .when()
//                        .post("/api/app/user/login")
//                        .then()
//                        .extract();
        ExtractableResponse<Response> response = getResponse("/api/app/user/login", login + "1", password, role);
        Assertions.assertEquals(response.response().getStatusCode(), HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    @Order(6)
    void registerAPICorrect() {
//        ExtractableResponse<Response> response =
//                given()
//                        .header("Content-type", "application/json")
//                        .and()
//                        .body(new JwtRequest("client2", "client2", "CLIENT"))
//                        .when()
//                        .post("/api/app/user/register")
//                        .then()
//                        .extract();
        ExtractableResponse<Response> response = getResponse("/api/app/user/register", "client2", "client2", "CLIENT");
        Assertions.assertEquals(response.response().getStatusCode(), HttpStatus.OK.value());

        ExtractableResponse<Response> response2 = getResponse("/api/app/user/login", "client2", "client2", "CLIENT");
        Assertions.assertEquals(response.response().getStatusCode(), HttpStatus.OK.value());

//        authRESTCorrect("client2", "client2", "CLIENT");

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

    @Test
    @Order(7)
    void registerAPIBad() {
//        ExtractableResponse<Response> response =
//                given()
//                        .header("Content-type", "application/json")
//                        .and()
//                        .body(new JwtRequest("client2", "client2", "CLIENT"))
//                        .when()
//                        .post("/api/app/user/register")
//                        .then()
//                        .extract();
        ExtractableResponse<Response> response = getResponse("/api/app/user/register", "client2", "client2", "CLIENT");
        Assertions.assertEquals(response.response().getStatusCode(), HttpStatus.CONFLICT.value());
    }

    @Test
    @Order(8)
    void addProfileAPICorrect() {
        User user = userRepository.findByLogin(clientLogin).orElseThrow();
        String tokenResponse = getToken(user.getLogin(), user.getPassword(), user.getRole().getName().toString());

        ProfileDto profileDto = new ProfileDto();
        profileDto.setUserId( user.getId());
        profileDto.setMail("client@gmail.com");
        profileDto.setName(clientLogin + "Profile");

        ExtractableResponse<Response> response =
                given()
                        .header("Content-type", "application/json")
                        .header("Authorization", "Bearer " + tokenResponse)
                        .and()
                        .body(profileDto)
                        .when()
                        .post("/api/app/user/profile/add/")
                        .then()
                        .extract();
        Assertions.assertAll(
                () -> Assertions.assertEquals(response.response().getStatusCode(), HttpStatus.OK.value()),
                () -> Assertions.assertEquals(
                        profileService.findByUserId(userRepository.findByLogin(clientLogin).orElseThrow().getId()),
                        dataTransformer.profileFromDto(profileDto))
        );
    }

    @Test
    @Order(8)
    void addProfileAPIAlreadyExist() {

        User user = userRepository.findByLogin(clientLogin).orElseThrow();
        String tokenResponse = getToken(user.getLogin(), user.getPassword(), user.getRole().getName().toString());

        ProfileDto profileDto = new ProfileDto();
        profileDto.setUserId( user.getId());
        profileDto.setMail("client@gmail.com");
        profileDto.setName(clientLogin + "Profile");

        ExtractableResponse<Response> response =
                given()
                        .header("Content-type", "application/json")
                        .header("Authorization", "Bearer " + tokenResponse)
                        .and()
                        .body(profileDto)
                        .when()
                        .post("/api/app/user/profile/add/")
                        .then()
                        .extract();

        Assertions.assertEquals(response.response().getStatusCode(), HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @Order(8)
    void addProfileAPIBad() {

        User user = userRepository.findByLogin(artistLogin).orElseThrow();
        String tokenResponse = getToken(user.getLogin(), user.getPassword(), user.getRole().getName().toString());

        ProfileDto profileDto = new ProfileDto();
        profileDto.setUserId( user.getId());
        profileDto.setMail(artistLogin);
        profileDto.setName(artistLogin + "Profile");

        ExtractableResponse<Response> response =
                given()
                        .header("Content-type", "application/json")
                        .header("Authorization", "Bearer " + tokenResponse)
                        .and()
                        .body(profileDto)
                        .when()
                        .post("/api/app/user/profile/add/")
                        .then()
                        .extract();

        Assertions.assertEquals(response.response().getStatusCode(), HttpStatus.BAD_REQUEST.value());
    }







//    @Test
//    void deactivateAPICorrect() {
//        ExtractableResponse<Response> response =
//                given()
//                        .header("Content-type", "application/json")
//                        .and()
//                        .body(new JwtRequest("client1", "client2", "CLIENT"))
//                        .when()
//                        .post("/api/app/user/register")
//                        .then()
//                        .extract();
//    }
//
//    @Test
//    void deactivateAPIBad() {
//        ExtractableResponse<Response> response =
//                given()
//                        .header("Content-type", "application/json")
//                        .and()
//                        .body(new JwtRequest("client1", "client2", "CLIENT"))
//                        .when()
//                        .post("/api/app/user/register")
//                        .then()
//                        .extract();
//    }

    private static Stream<String> loginProvider() {
        return Stream.of(
                clientLogin,
                artistLogin,
                adminPassword,
                "client2",
                adminLogin + "1"
        );
    }

    private static Stream<String> passwordProvider() {
        return Stream.of(
                clientPassword,
                artistPassword,
                adminLogin,
                "client2",
                adminPassword + "1"
        );
    }

    private static Stream<?> userProvider() {
        return Stream.of(
                Arguments.of(adminLogin, adminPassword, adminRole),
                Arguments.of(artistLogin, artistPassword, artistRole),
                Arguments.of(clientLogin, clientPassword, clientRole),
                Arguments.of("client2","client2","CLIENT")
        );
    }


}
