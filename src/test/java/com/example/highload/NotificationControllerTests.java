package com.example.highload;

import com.example.highload.model.network.JwtRequest;
import com.example.highload.model.network.JwtResponse;
import com.example.highload.repos.ProfileRepository;
import com.example.highload.repos.ReviewRepository;
import com.example.highload.repos.UserRepository;
import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static io.restassured.RestAssured.given;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class NotificationControllerTests {

    @LocalServerPort
    private Integer port;

    @Autowired
    UserRepository userRepository;
    @Autowired
    ProfileRepository profileRepository;
    @Autowired
    ReviewRepository reviewRepository;

    private static final String clientLogin = "client1";
    private static final String clientPassword = "client1";
    private static final String clientRole = "CLIENT";

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

    private String getToken(String login, String password, String role) {
        return given()
                .header("Content-type", "application/json")
                .and()
                .body(new JwtRequest(login, password, role))
                .when()
                .post("/api/app/user/login")
                .then()
                .extract().body().as(JwtResponse.class).getToken();
    }

//    @Test
//    @Order(1)
//    void saveAPICorrect() {
//
//    }
//
//    @Test
//    @Order(2)
//    void saveAPIBadData() {
//
//    }

}
