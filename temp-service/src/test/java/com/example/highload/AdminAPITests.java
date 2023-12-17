package com.example.highload;

import com.example.highload.model.enums.RoleType;
import com.example.highload.model.inner.Role;
import com.example.highload.model.inner.User;
import com.example.highload.model.inner.UserRequest;
import com.example.highload.model.network.JwtRequest;
import com.example.highload.model.network.JwtResponse;
import com.example.highload.model.network.UserDto;
import com.example.highload.model.network.UserRequestDto;
import com.example.highload.repos.RoleRepository;
import com.example.highload.repos.UserRepository;
import com.example.highload.repos.UserRequestRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.event.annotation.BeforeTestExecution;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import static io.restassured.RestAssured.given;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AdminAPITests {

    @LocalServerPort
    private Integer port;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserRequestRepository userRequestRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

    @Container
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

    private String getToken(String userName) {
        User user = userRepository.findByLogin(userName).orElseThrow();
        return given()
                .header("Content-type", "application/json")
                .and()
                .body(new JwtRequest(userName, userName, user.getRole().getName().toString()))
                .when()
                .post("/api/user/login")
                .then()
                .extract().body().as(JwtResponse.class).getToken();
    }

    @Test
    public void addUser() {

        String tokenResponse = getToken("admin1");

        /*add correct user*/

        UserDto userDto = new UserDto();
        userDto.setLogin("admin_test_client1");
        userDto.setPassword("admin_test_client1");
        userDto.setRole(RoleType.CLIENT);

        ExtractableResponse<Response> response1 =
                given()
                        .header("Authorization", "Bearer " + tokenResponse)
                        .header("Content-type", "application/json")
                        .and()
                        .body(userDto)
                        .when()
                        .post("/api/admin/user/add")
                        .then()
                        .extract();
        Assertions.assertAll(
                () -> Assertions.assertEquals("User added", response1.body().asString()),
                () -> Assertions.assertEquals(HttpStatus.OK.value(), response1.statusCode())
        );

        /*add existing user*/

        ExtractableResponse<Response> response2 =
                given()
                        .header("Authorization", "Bearer " + tokenResponse)
                        .header("Content-type", "application/json")
                        .and()
                        .body(userDto)
                        .when()
                        .post("/api/admin/user/add")
                        .then()
                        .extract();
        Assertions.assertAll(
                () -> Assertions.assertEquals("User already exists!", response2.body().asString()),
                () -> Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response2.statusCode())
        );

        /* add user with wrong name (empty) */

        UserDto wrongUserDto = new UserDto();
        wrongUserDto.setLogin("");
        wrongUserDto.setPassword("-");
        wrongUserDto.setRole(RoleType.CLIENT);

        ExtractableResponse<Response> response3 =
                given()
                        .header("Authorization", "Bearer " + tokenResponse)
                        .header("Content-type", "application/json")
                        .and()
                        .body(wrongUserDto)
                        .when()
                        .post("/api/admin/user/add")
                        .then()
                        .extract();
        Assertions.assertAll(
                () -> Assertions.assertEquals("Request body validation failed!", response3.body().asString()),
                () -> Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response3.statusCode())
        );
    }


    @Test
    public void approveUser() {

        Role clientRole = roleRepository.findByName(RoleType.CLIENT).orElseThrow();

        UserRequest userRequest = new UserRequest();
        userRequest.setLogin("admin_test_client2");
        userRequest.setHashPassword(bCryptPasswordEncoder.encode("admin_test_client2"));
        userRequest.setRole(clientRole);

        UserRequest userRequestWithId = userRequestRepository.save(userRequest);

        String tokenResponse = getToken("admin1");

        String id = userRequestWithId.getId().toString();

        ExtractableResponse<Response> response1 =
                given()
                        .header("Authorization", "Bearer " + tokenResponse)
                        .header("Content-type", "application/json")
                        .when()
                        .post("/api/admin/user-request/approve/" + id)
                        .then()
                        .extract();
        Assertions.assertAll(
                () -> Assertions.assertEquals("User approved", response1.body().asString()),
                () -> Assertions.assertEquals(HttpStatus.OK.value(), response1.statusCode()),
                () -> Assertions.assertThrows(NoSuchElementException.class, () -> userRequestRepository.findByLogin("admin_test_client2").orElseThrow()),
                () -> Assertions.assertDoesNotThrow(() -> userRepository.findByLogin("admin_test_client2").orElseThrow())
        );

        ExtractableResponse<Response> response2 =
                given()
                        .header("Authorization", "Bearer " + tokenResponse)
                        .header("Content-type", "application/json")
                        .when()
                        .post("/api/admin/user-request/approve/" + id)
                        .then()
                        .extract();
        Assertions.assertAll(
                () -> Assertions.assertEquals("Wrong ids in path!", response2.body().asString()),
                () -> Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response2.statusCode())
        );

    }


    @Test
    public void deleteUser() {
        // create user using repo

        Role clientRole = roleRepository.findByName(RoleType.CLIENT).orElseThrow();

        User user = new User();
        user.setLogin("admin_test_client3");
        user.setHashPassword(bCryptPasswordEncoder.encode("admin_test_client3"));
        user.setRole(clientRole);
        user.setIsActual(true);

        User userWithId = userRepository.save(user);

        // get token

        String tokenResponse = getToken("admin1");

        // delete existing

        String id = userWithId.getId().toString();

        ExtractableResponse<Response> response1 =
                given()
                        .header("Authorization", "Bearer " + tokenResponse)
                        .header("Content-type", "application/json")
                        .when()
                        .post("/api/admin/user/delete/" + id)
                        .then()
                        .extract();
        Assertions.assertAll(
                () -> Assertions.assertEquals("User deleted", response1.body().asString()),
                () -> Assertions.assertEquals(HttpStatus.OK.value(), response1.statusCode()),
                () -> Assertions.assertThrows(NoSuchElementException.class, () -> userRepository.findByLogin("admin_test_client3").orElseThrow())
        );

        // delete not existing (on prev step user was deleted)

        ExtractableResponse<Response> response2 =
                given()
                        .header("Authorization", "Bearer " + tokenResponse)
                        .header("Content-type", "application/json")
                        .when()
                        .post("/api/admin/user/delete/" + id)
                        .then()
                        .extract();
        Assertions.assertAll(
                () -> Assertions.assertEquals("Wrong ids in path!", response2.body().asString()),
                () -> Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response2.statusCode())
        );

    }

    @Test
    public void getAllUserRequests() {

        // create user request1 using repo

        Role clientRole = roleRepository.findByName(RoleType.CLIENT).orElseThrow();

        UserRequest userRequest1 = new UserRequest();
        userRequest1.setLogin("admin_test_client4");
        userRequest1.setHashPassword(bCryptPasswordEncoder.encode("admin_test_client4"));
        userRequest1.setRole(clientRole);

        UserRequest userRequest1WithId = userRequestRepository.save(userRequest1);

        // create user request2 using repo

        UserRequest userRequest2 = new UserRequest();
        userRequest2.setLogin("admin_test_client5");
        userRequest2.setHashPassword(bCryptPasswordEncoder.encode("admin_test_client5"));
        userRequest2.setRole(clientRole);

        UserRequest userRequest2WithId = userRequestRepository.save(userRequest2);

        // get token

        String tokenResponse = getToken("admin1");

        // get all user requests

        ExtractableResponse<Response> response1 =
                given()
                        .header("Authorization", "Bearer " + tokenResponse)
                        .header("Content-type", "application/json")
                        .when()
                        .get("/api/admin/user-request/all/0")
                        .then()
                        .extract();

        Assertions.assertAll(
                () -> Assertions.assertEquals(HttpStatus.OK.value(), response1.statusCode()),
                () -> Assertions.assertEquals("1",response1.header("app-total-page-num")),
                () -> Assertions.assertEquals("2",response1.header("app-total-items-num")),
                () -> Assertions.assertEquals("0",response1.header("app-current-page-num")),
                () -> Assertions.assertEquals("2",response1.header("app-current-items-num"))
        );

        List<UserRequestDto> userRequestDtos = response1.body().jsonPath().getList(".", UserRequestDto.class);

        Assertions.assertAll(
                () -> Assertions.assertEquals(2, userRequestDtos.size()),
                () -> Assertions.assertEquals("admin_test_client4", userRequestDtos.get(0).getLogin()),
                () -> Assertions.assertEquals("admin_test_client5", userRequestDtos.get(1).getLogin())
        );

        // delete by id using repo

        userRequestRepository.deleteById(userRequest1WithId.getId());
        userRequestRepository.deleteById(userRequest2WithId.getId());

    }

    @Test
    public void deleteAllExpiredUserDeletedAccounts() {

        Role clientRole = roleRepository.findByName(RoleType.CLIENT).orElseThrow();

        // create logically deleted account using repo

        User user = new User();
        user.setLogin("admin_test_client6");
        user.setHashPassword(bCryptPasswordEncoder.encode("admin_test_client6"));
        user.setRole(clientRole);
        user.setIsActual(false);
        user.setWhenDeletedTime(LocalDateTime.now());

        userRepository.save(user);

        // get token

        String tokenResponse = getToken("admin1");

        // delete with 0 days param

        ExtractableResponse<Response> response1 =
                given()
                        .header("Authorization", "Bearer " + tokenResponse)
                        .header("Content-type", "application/json")
                        .when()
                        .post("/api/admin/user/all/delete-expired/0")
                        .then()
                        .extract();

        Assertions.assertAll(
                () -> Assertions.assertEquals("Users deleted", response1.body().asString()),
                () -> Assertions.assertEquals(HttpStatus.OK.value(), response1.statusCode()),
                () -> Assertions.assertThrows(NoSuchElementException.class, () -> userRepository.findByLogin("admin_test_client6").orElseThrow())
        );

    }
}

