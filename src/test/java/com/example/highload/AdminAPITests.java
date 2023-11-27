package com.example.highload;

import com.example.highload.model.enums.RoleType;
import com.example.highload.model.inner.Role;
import com.example.highload.model.inner.User;
import com.example.highload.model.network.JwtRequest;
import com.example.highload.model.network.JwtResponse;
import com.example.highload.model.network.UserDto;
import com.example.highload.repos.RoleRepository;
import com.example.highload.repos.UserRepository;
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
    RoleRepository roleRepository;

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

    boolean testUsersPreparedFlag = false;

    @Container
    private static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("highload")
            .withUsername("high_user")
            .withPassword("high_user");

//    @BeforeEach
//    public void initRolesAndUsersIfNotAlready() {
//        if (!testUsersPreparedFlag) {
//
//            Role admin = new Role();
//            admin.setName(RoleType.ADMIN);
//            roleRepository.save(admin);
//            Role artist = new Role();
//            artist.setName(RoleType.ARTIST);
//            roleRepository.save(artist);
//            Role client = new Role();
//            client.setName(RoleType.CLIENT);
//            roleRepository.save(client);
//
//            User admin1 = new User();
//            admin1.setLogin("admin1");
//            admin1.setHashPassword(bCryptPasswordEncoder.encode("admin1"));
//            admin1.setRole(admin);
//            admin1.setIsActual(true);
//            userRepository.save(admin1);
//
//            User artist1 = new User();
//            artist1.setLogin("artist1");
//            artist1.setHashPassword(bCryptPasswordEncoder.encode("artist1"));
//            artist1.setRole(artist);
//            artist1.setIsActual(true);
//            userRepository.save(artist1);
//
//            User client1 = new User();
//            client1.setLogin("client1");
//            client1.setHashPassword(bCryptPasswordEncoder.encode("client1"));
//            client1.setRole(client);
//            client1.setIsActual(true);
//            userRepository.save(client1);
//
//            testUsersPreparedFlag = true;
//        }
//    }

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
    public void addUser() {
        User admin = userRepository.findByLogin("admin1").orElseThrow();
        String tokenResponse =
                given()
                        .header("Content-type", "application/json")
                        .and()
                        .body(new JwtRequest(admin.getLogin(), "admin1", admin.getRole().getName().toString()))
                        .when()
                        .post("/api/app/user/login")
                        .then()
                        .extract().body().as(JwtResponse.class).getToken();
        UserDto userDto = new UserDto();
        userDto.setLogin("test_client1");
        userDto.setPassword("test_client1");
        userDto.setRole(RoleType.CLIENT);

        /*add correct user*/

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
                ()->Assertions.assertEquals( "User added", response1.body().asString()),
                ()->Assertions.assertEquals( HttpStatus.OK.value(), response1.statusCode())
        );

        /*add existing user*/

        ResponseEntity response2 =
                given()
                        .header("Authorization", "Bearer " + tokenResponse)
                        .header("Content-type", "application/json")
                        .and()
                        .body(userDto)
                        .when()
                        .post("/api/app/admin/user/add")
                        .then()
                        .extract().body().as(ResponseEntity.class);
        Assertions.assertAll(
                ()->Assertions.assertTrue(response2.hasBody()),
                ()->Assertions.assertEquals(response2.getStatusCode(), HttpStatusCode.valueOf(400)),
                ()->Assertions.assertEquals(response2.getBody(), "User already exists!")
        );

        /*TODO : add user with wrong name (empty) */
    }

}

