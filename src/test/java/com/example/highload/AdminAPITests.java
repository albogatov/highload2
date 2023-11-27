package com.example.highload;

import com.example.highload.model.enums.RoleType;
import com.example.highload.model.inner.Role;
import com.example.highload.model.inner.User;
import com.example.highload.model.inner.UserRequest;
import com.example.highload.model.network.JwtRequest;
import com.example.highload.model.network.JwtResponse;
import com.example.highload.model.network.UserDto;
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

    boolean testUsersPreparedFlag = false;

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
                        .post("/api/app/user/login")
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
                        .post("/api/app/admin/user/add")
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
                        .post("/api/app/admin/user/add")
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
                        .post("/api/app/admin/user/add")
                        .then()
                        .extract();
        Assertions.assertAll(
                () -> Assertions.assertEquals("Request body validation failed!", response3.body().asString()),
                () -> Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response3.statusCode())
        );
    }


    @Test
    public void approveUser() { /*TODO: RUN*/
        // create user request using repo

        Role clientRole = roleRepository.findByName(RoleType.CLIENT).orElseThrow();

        UserRequest userRequest = new UserRequest();
        userRequest.setLogin("admin_test_client2");
        userRequest.setHashPassword(bCryptPasswordEncoder.encode("admin_test_client2"));
        userRequest.setRole(clientRole);

        UserRequest userRequestWithId = userRequestRepository.save(userRequest);

        // get token

        String tokenResponse = getToken("admin1");

        // approve existing

        String id = userRequestWithId.getId().toString();

        ExtractableResponse<Response> response1 =
                given()
                        .header("Authorization", "Bearer " + tokenResponse)
                        .header("Content-type", "application/json")
                        .when()
                        .post("/api/app/admin/user-request/approve/" + id)
                        .then()
                        .extract();
        Assertions.assertAll(
                () -> Assertions.assertEquals("User approved", response1.body().asString()),
                () -> Assertions.assertEquals(HttpStatus.OK.value(), response1.statusCode())
        );

        // approve not existing (on prev step user request was deleted when accepted)

        ExtractableResponse<Response> response2 =
                given()
                        .header("Authorization", "Bearer " + tokenResponse)
                        .header("Content-type", "application/json")
                        .when()
                        .post("/api/app/admin/user-request/approve/" + id)
                        .then()
                        .extract();
        Assertions.assertAll(
                () -> Assertions.assertEquals("Wrong ids in path!", response2.body().asString()),
                () -> Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response2.statusCode())
        );

    }


    @Test
    public void deleteUser() { /*TODO: RUN*/
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
                        .post("/api/app/admin/user/delete/" + id)
                        .then()
                        .extract();
        Assertions.assertAll(
                () -> Assertions.assertEquals("User deleted", response1.body().asString()),
                () -> Assertions.assertEquals(HttpStatus.OK.value(), response1.statusCode())
        );

        // delete not existing (on prev step user was deleted)

        ExtractableResponse<Response> response2 =
                given()
                        .header("Authorization", "Bearer " + tokenResponse)
                        .header("Content-type", "application/json")
                        .when()
                        .post("/api/app/admin/user/delete/" + id)
                        .then()
                        .extract();
        Assertions.assertAll(
                () -> Assertions.assertEquals("Wrong ids in path!", response2.body().asString()),
                () -> Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response2.statusCode())
        );

    }
}

