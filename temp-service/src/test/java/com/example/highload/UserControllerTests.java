package com.example.highload;

import com.example.highload.model.inner.User;
import com.example.highload.model.network.ImageDto;
import com.example.highload.model.network.JwtRequest;
import com.example.highload.model.network.JwtResponse;
import com.example.highload.model.network.ProfileDto;
import com.example.highload.repos.RoleRepository;
import com.example.highload.repos.UserRepository;
import com.example.highload.security.jwt.JwtUtil;
import com.example.highload.services.AuthenticationService;
import com.example.highload.services.ProfileService;
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

    private static final String adminLogin = "admin1";
    private static final String adminPassword = "admin1";
    private static final String adminRole = "ADMIN";
    private static final String artistLogin = "artist1";
    private static final String artistPassword = "artist1";
    private static final String artistRole = "ARTIST";
    private static final String clientLogin = "client1";
    private static final String clientPassword = "client1";
    private static final String clientRole = "CLIENT";
    private static final String newClientLogin = "client2";
    private static final String newClientPassword = "client2";


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
        return given()
                .header("Content-type", "application/json")
                .and()
                .body(new JwtRequest(login, password, role))
                .when()
                .post("/api/user/login")
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
        ExtractableResponse<Response> response = getResponse("/api/user/login", login, password, role);
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
        ExtractableResponse<Response> response = getResponse("/api/user/login", login + "1", password, role);
        Assertions.assertEquals(response.response().getStatusCode(), HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    @Order(6)
    void registerAPICorrect() {
        ExtractableResponse<Response> response = getResponse("/api/user/register", newClientLogin, newClientPassword, clientRole);
        Assertions.assertEquals(HttpStatus.OK.value(), response.response().getStatusCode());
    }

    @Test
    @Order(7)
    void registerAPIBad() {
        ExtractableResponse<Response> response = getResponse("/api/user/register", newClientLogin, newClientPassword, clientRole);
        Assertions.assertEquals(response.response().getStatusCode(), HttpStatus.CONFLICT.value());
    }

    @Test
    @Order(8)
    void addProfileAPICorrect() {
        User user = userRepository.findByLogin(clientLogin).orElseThrow();
        String tokenResponse = getToken(clientLogin, clientPassword, clientRole);
        Integer userId = user.getId();

        ProfileDto profileDto = new ProfileDto();
        profileDto.setUserId(user.getId());
        profileDto.setMail("client@gmail.com");
        profileDto.setName(clientLogin + "Profile");
        profileDto.setEducation("ITMO");
        profileDto.setExperience("ITMO logo");

        ExtractableResponse<Response> response =
                given()
                        .header("Content-type", "application/json")
                        .header("Authorization", "Bearer " + tokenResponse)
                        .and()
                        .body(profileDto)
                        .when()
                        .post("/api/user/profile/add/" + userId)
                        .then()
                        .extract();
        Assertions.assertAll(
                () -> Assertions.assertEquals(response.response().getStatusCode(), HttpStatus.OK.value()),
                () -> Assertions.assertEquals(
                        profileService.findByUserIdElseNull(userId).getName(),
                        dataTransformer.profileFromDto(profileDto).getName())
        );
    }

    @Test
    @Order(9)
    void addProfileAPIAlreadyExist() {

        User user = userRepository.findByLogin(clientLogin).orElseThrow();
        String tokenResponse = getToken(clientLogin, clientPassword, clientRole);
        Integer userId = user.getId();

        ProfileDto profileDto = new ProfileDto();
        ImageDto imageDto = new ImageDto();
        imageDto.setUrl("http");
        profileDto.setUserId(user.getId());
        profileDto.setMail("client@gmail.com");
        profileDto.setName(clientLogin + "Profile");
        profileDto.setEducation("ITMO");
        profileDto.setExperience("ITMO logo");
        profileDto.setImage(imageDto);

        ExtractableResponse<Response> response =
                given()
                        .header("Content-type", "application/json")
                        .header("Authorization", "Bearer " + tokenResponse)
                        .and()
                        .body(profileDto)
                        .when()
                        .post("/api/user/profile/add/" + userId)
                        .then()
                        .extract();

        Assertions.assertEquals(response.response().getStatusCode(), HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @Order(10)
    void addProfileAPIBad() {
        User user = userRepository.findByLogin(clientLogin).orElseThrow();
        String tokenResponse = getToken(clientLogin, clientPassword, clientRole);
        Integer userId = user.getId();

        ProfileDto profileDto = new ProfileDto();
        ImageDto imageDto = new ImageDto();
        imageDto.setUrl("http");
        profileDto.setUserId(user.getId());
        profileDto.setMail("BADEMAIL");
        profileDto.setName(clientLogin + "Profile");
        profileDto.setEducation("ITMO");
        profileDto.setExperience("ITMO logo");
        profileDto.setImage(imageDto);

        ExtractableResponse<Response> response =
                given()
                        .header("Content-type", "application/json")
                        .header("Authorization", "Bearer " + tokenResponse)
                        .and()
                        .body(profileDto)
                        .when()
                        .post("/api/user/profile/add/" + userId)
                        .then()
                        .extract();

        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response.response().getStatusCode());
    }

    @Test
    @Order(11)
    void deactivateIdAPICorrect() {
        User user = userRepository.findByLogin(clientLogin).orElseThrow();
        String tokenResponse = getToken(clientLogin, clientPassword, clientRole);
        Integer userId = user.getId();

        ExtractableResponse<Response> response =
                given()
                        .header("Content-type", "application/json")
                        .header("Authorization", "Bearer " + tokenResponse)
                        .when()
                        .post("/api/user/deactivate/" + userId)
                        .then()
                        .extract();
        Assertions.assertAll(
                () -> Assertions.assertEquals(HttpStatus.OK.value(), response.response().getStatusCode()),
                () -> {
                    User user2 = userRepository.findByLogin(clientLogin).orElseThrow();
                    Assertions.assertFalse(user2.getIsActual());
                }
        );
    }

    private static Stream<String> loginProvider() {
        return Stream.of(
                clientLogin,
                artistLogin,
                "Bogatov",
                "client2",
                adminLogin + "1"
        );
    }

    private static Stream<String> passwordProvider() {
        return Stream.of(
                clientPassword,
                artistPassword,
                "ABCDEFG",
                "client2",
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
