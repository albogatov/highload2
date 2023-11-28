package com.example.highload;

import com.example.highload.model.network.JwtRequest;
import com.example.highload.model.network.JwtResponse;
import com.example.highload.model.network.TagDto;
import com.example.highload.model.inner.Tag;
import com.example.highload.repos.TagRepository;
import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static io.restassured.RestAssured.given;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TagControllerTest {

    @LocalServerPort
    private Integer port;

    @Autowired
    public TagRepository tagRepository;

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

    // /remove/{orderId}/{tagId}

    @Test
    @Order(1)
    void saveAPICorrect() {
        String tagName = "Programmer";
        TagDto tagDto = new TagDto();
        tagDto.setName(tagName);

        String tokenResponse = getToken(adminLogin, adminPassword, adminRole);

        ExtractableResponse<Response> response =
                given()
                        .header("Content-type", "application/json")
                        .header("Authorization", "Bearer " + tokenResponse)
                        .and()
                        .body(tagDto)
                        .when()
                        .post("/api/app/tag/save")
                        .then()
                        .extract();
        Assertions.assertAll(
                () -> Assertions.assertEquals(HttpStatus.OK.value(), response.response().getStatusCode()),
                () -> {
                    Tag tag = tagRepository.findByName(tagName).orElseThrow();
                    Assertions.assertEquals(tag.getName(), tagName);
                }
        );
    }

    @Test
    @Order(2)
    void saveAPIAlreadyExist() {
        String tagName = "Programmer";
        TagDto tagDto = new TagDto();
        tagDto.setName(tagName);

        String tokenResponse = getToken(adminLogin, adminPassword, adminRole);

        ExtractableResponse<Response> response =
                given()
                        .header("Content-type", "application/json")
                        .header("Authorization", "Bearer " + tokenResponse)
                        .and()
                        .body(tagDto)
                        .when()
                        .post("/api/app/tag/save")
                        .then()
                        .extract();
        Assertions.assertAll(
                () -> Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response.response().getStatusCode()),
                () -> Assertions.assertEquals("Request body validation failed!", response.body().toString())
        );
    }

    @Test
    @Order(3)
    void saveAPIFromNotAdmin() {
        String tagName = "Cats";
        TagDto tagDto = new TagDto();
        tagDto.setName(tagName);

        String tokenResponse = getToken(clientLogin, clientPassword, clientRole);

        ExtractableResponse<Response> response =
                given()
                        .header("Content-type", "application/json")
                        .header("Authorization", "Bearer " + tokenResponse)
                        .and()
                        .body(tagDto)
                        .when()
                        .post("/api/app/tag/save")
                        .then()
                        .extract();
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED.value(), response.response().getStatusCode());
    }

    @Test
    @Order(4)
    void getAllAPICorrect() {
        String tokenResponse = getToken(adminLogin, adminPassword, adminRole);

        ExtractableResponse<Response> response =
                given()
                        .header("Content-type", "application/json")
                        .header("Authorization", "Bearer " + tokenResponse)
                        .when()
                        .post("/api/app/tag/all/1")
                        .then()
                        .extract();
        Assertions.assertAll(
                () -> Assertions.assertEquals(HttpStatus.OK.value(), response.response().getStatusCode()),
                () -> {
                    int size = response.body().as(List.class).size();
                    Assertions.assertEquals(1, size);
                }
        );
    }

    @Test
    @Order(5)
    void getAllAPIWrongPath() {
        String tokenResponse = getToken(adminLogin, adminPassword, adminRole);

        ExtractableResponse<Response> response =
                given()
                        .header("Content-type", "application/json")
                        .header("Authorization", "Bearer " + tokenResponse)
                        .when()
                        .post("/api/app/tag/all/BLABLABLA")
                        .then()
                        .extract();
        Assertions.assertAll(
                () -> Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response.response().getStatusCode()),
                () -> Assertions.assertEquals("Wrong ids in path!", response.body().toString())
        );
    }

    @Test
    @Order(6)
    void removeTagFromOrderAPICorrect() {
        String tokenResponse = getToken(adminLogin, adminPassword, adminRole);

        ExtractableResponse<Response> response =
                given()
                        .header("Content-type", "application/json")
                        .header("Authorization", "Bearer " + tokenResponse)
                        .when()
                        .post("/api/app/tag/all/1")
                        .then()
                        .extract();
        Assertions.assertAll(
                () -> Assertions.assertEquals(HttpStatus.OK.value(), response.response().getStatusCode()),
                () -> {
                    int size = response.body().as(List.class).size();
                    Assertions.assertEquals(1, size);
                }
        );
    }

}
