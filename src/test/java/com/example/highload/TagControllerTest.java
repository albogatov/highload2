package com.example.highload;

import com.example.highload.model.inner.Tag;
import com.example.highload.model.inner.User;
import com.example.highload.model.network.JwtRequest;
import com.example.highload.model.network.JwtResponse;
import com.example.highload.model.network.OrderDto;
import com.example.highload.model.network.TagDto;
import com.example.highload.repos.OrderRepository;
import com.example.highload.repos.TagRepository;
import com.example.highload.repos.UserRepository;
import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    @Autowired
    UserRepository userRepository;
    @Autowired
    OrderRepository orderRepository;

    private static final String adminLogin = "admin1";
    private static final String adminPassword = "admin1";
    private static final String adminRole = "ADMIN";
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

    // /remove/{orderId}/{tagId}

    @Test
    @Order(1)
    void saveAPICorrect() {
        String tokenResponse = getToken(adminLogin, adminPassword, adminRole);

        String tagName = "Programmer";
        TagDto tagDto = new TagDto();
        tagDto.setName(tagName);

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
        String tokenResponse = getToken(adminLogin, adminPassword, adminRole);

        String tagName = "Programmer";
        TagDto tagDto = new TagDto();
        tagDto.setName(tagName);

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
                () -> Assertions.assertEquals("Request body validation failed!", response.body().asString())
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
                () -> Assertions.assertEquals("Wrong ids in path!", response.body().asString())
        );
    }

    @Test
    @Order(6)
    void removeTagFromOrderAPICorrect() {
        User user = userRepository.findByLogin(clientLogin).orElseThrow();
        String tokenResponse = getToken(clientLogin, clientPassword, clientRole);
        Integer userId = user.getId();

        String tagName = "Programmer";
        TagDto tagDto = new TagDto();
        tagDto.setName(tagName);

        OrderDto orderDto = new OrderDto();
        orderDto.setUserId(userId);
        orderDto.setDescription("Description");
        orderDto.setPrice(100);
        orderDto.setUserName(user.getUsername());
        orderDto.setTags(List.of(tagDto));

        ExtractableResponse<Response> response0 =
                given()
                        .header("Content-type", "application/json")
                        .header("Authorization", "Bearer " + tokenResponse)
                        .and()
                        .body(orderDto)
                        .when()
                        .post("/api/app/order/save")
                        .then()
                        .extract();

        int tagId = tagRepository.findByName(tagName).orElseThrow().getId();
        Pageable pageable = PageRequest.of(1, 50);
        int orderId = orderRepository.findAllByTags_Id(tagId, pageable).get().findFirst().orElseThrow().getId();

        ExtractableResponse<Response> response =
                given()
                        .header("Content-type", "application/json")
                        .header("Authorization", "Bearer " + tokenResponse)
                        .when()
                        .post("/api/app/tag/remove/" + orderId + "/" + tagId)
                        .then()
                        .extract();
        Assertions.assertAll(
                () -> Assertions.assertEquals(HttpStatus.OK.value(), response.response().getStatusCode()),
                () -> {
                    com.example.highload.model.inner.Order order = orderRepository.findAllByTags_Name(tagName, pageable).stream().findFirst().orElseThrow();
                    Assertions.assertEquals(0, order.getTags().size());
                }
        );
    }

    @Test
    @Order(7)
    void removeTagFromOrderAPIWrongPath() {
        User user = userRepository.findByLogin(clientLogin).orElseThrow();
        String tokenResponse = getToken(clientLogin, clientPassword, clientRole);
        Integer userId = user.getId();

        String tagName = "Programmer";
        TagDto tagDto = new TagDto();
        tagDto.setName(tagName);

        OrderDto orderDto = new OrderDto();
        orderDto.setUserId(userId);
        orderDto.setDescription("Description");
        orderDto.setPrice(100);
        orderDto.setUserName(user.getUsername());
        orderDto.setTags(List.of(tagDto));

        ExtractableResponse<Response> response0 =
                given()
                        .header("Content-type", "application/json")
                        .header("Authorization", "Bearer " + tokenResponse)
                        .and()
                        .body(orderDto)
                        .when()
                        .post("/api/app/order/save")
                        .then()
                        .extract();

        int tagId = tagRepository.findByName(tagName).orElseThrow().getId();
        Pageable pageable = PageRequest.of(1, 50);
        int orderId = orderRepository.findAllByTags_Id(tagId, pageable).get().findFirst().orElseThrow().getId();

        ExtractableResponse<Response> response =
                given()
                        .header("Content-type", "application/json")
                        .header("Authorization", "Bearer " + tokenResponse)
                        .when()
                        .post("/api/app/tag/remove/" + orderId + "/" + tagId + 1)
                        .then()
                        .extract();
        Assertions.assertAll(
                () -> Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response.response().getStatusCode()),
                () -> Assertions.assertEquals("Wrong ids in path!", response.body().asString())
        );
    }

}
