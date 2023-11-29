package com.example.highload;

import com.example.highload.model.inner.Notification;
import com.example.highload.model.inner.User;
import com.example.highload.model.network.JwtRequest;
import com.example.highload.model.network.JwtResponse;
import com.example.highload.model.network.NotificationDto;
import com.example.highload.repos.NotificationRepository;
import com.example.highload.repos.UserRepository;
import com.example.highload.utils.DataTransformer;
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

import java.time.LocalDateTime;
import java.util.List;

import static io.restassured.RestAssured.given;

@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class NotificationControllerTests {

    @LocalServerPort
    private Integer port;

    @Autowired
    UserRepository userRepository;
    @Autowired
    NotificationRepository notificationRepository;
    @Autowired
    DataTransformer dataTransformer;

    private static final String adminLogin = "admin1";
    private static final String adminPassword = "admin1";
    private static final String adminRole = "ADMIN";
    private static final String clientLogin = "client1";
    private static final String clientPassword = "client1";
    private static final String clientRole = "CLIENT";
    private static final String artistLogin = "artist1";
    private static final String artistPassword = "artist1";
    private static final String artistRole = "ARTIST";

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

    // post setRead /update/{id}
    // get getAllQueries /all/{userId}/{page}
    // get getNewQueries /new/{userId}/{page}

    @Test
    @Order(1)
    void saveAPICorrect() {
        User sender = userRepository.findByLogin(clientLogin).orElseThrow();
        User receiver = userRepository.findByLogin(artistLogin).orElseThrow();
        String senderEmail = "sender@mail.ru";
        String tokenResponse = getToken(clientLogin, clientPassword, clientRole);

        NotificationDto notificationDto = new NotificationDto();
        notificationDto.setRead(false);
        notificationDto.setReceiverId(receiver.getId());
        notificationDto.setSenderId(sender.getId());
        notificationDto.setSenderMail(senderEmail);
        notificationDto.setTime(LocalDateTime.now());

        Pageable pageable = PageRequest.of(0, 50);

        ExtractableResponse<Response> response =
                given()
                        .header("Content-type", "application/json")
                        .header("Authorization", "Bearer " + tokenResponse)
                        .and()
                        .body(notificationDto)
                        .when()
                        .post("/api/app/notification/save")
                        .then()
                        .extract();
        Assertions.assertAll(
                () -> Assertions.assertEquals(HttpStatus.OK.value(), response.response().getStatusCode()),
                () -> {
                    Notification notification = notificationRepository.findAllBySenderProfile_Id(sender.getId(), pageable)
                            .stream().findFirst().orElseThrow();
                    Assertions.assertEquals(senderEmail, notification.getSenderProfile().getMail());
                }
        );
    }

    @Test
    @Order(2)
    void saveAPIBadData() {
        String tokenResponse = getToken(clientLogin, clientPassword, clientRole);

        ExtractableResponse<Response> response =
                given()
                        .header("Content-type", "application/json")
                        .header("Authorization", "Bearer " + tokenResponse)
                        .and()
                        .body(new NotificationDto())
                        .when()
                        .post("/api/app/notification/save")
                        .then()
                        .extract();
        Assertions.assertAll(
                () -> Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response.response().getStatusCode()),
                () -> Assertions.assertEquals("Request body validation failed!", response.body().asString())
        );
    }

    @Test
    @Order(3)
    void setReadAPICorrect() {
        User sender = userRepository.findByLogin(clientLogin).orElseThrow();
        Pageable pageable = PageRequest.of(0, 50);
        String tokenResponse = getToken(artistLogin, artistPassword, artistRole);

        Notification notification = notificationRepository.findAllBySenderProfile_Id(sender.getId(), pageable)
                .stream().findFirst().orElseThrow();

        ExtractableResponse<Response> response =
                given()
                        .header("Content-type", "application/json")
                        .header("Authorization", "Bearer " + tokenResponse)
                        .when()
                        .post("/api/app/notification/update/" + notification.getId())
                        .then()
                        .extract();

        Assertions.assertAll(
                () -> Assertions.assertEquals(HttpStatus.OK.value(), response.response().getStatusCode()),
                () -> {
                    Notification notification1 = notificationRepository.findAllBySenderProfile_Id(sender.getId(), pageable)
                            .stream().findFirst().orElseThrow();
                    Assertions.assertTrue(notification1.getIsRead());
                }
        );
    }

    @Test
    @Order(4)
    void setReadAPIWrongPath() {
        User sender = userRepository.findByLogin(clientLogin).orElseThrow();
        Pageable pageable = PageRequest.of(0, 50);
        String tokenResponse = getToken(artistLogin, artistPassword, artistRole);

        Notification notification = notificationRepository.findAllBySenderProfile_Id(sender.getId(), pageable)
                .stream().findFirst().orElseThrow();
        notification.setIsRead(false);
        notificationRepository.save(notification);

        ExtractableResponse<Response> response =
                given()
                        .header("Content-type", "application/json")
                        .header("Authorization", "Bearer " + tokenResponse)
                        .when()
                        .post("/api/app/notification/update/" + notification.getId() + 3)
                        .then()
                        .extract();

        Assertions.assertAll(
                () -> Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response.response().getStatusCode()),
                () -> Assertions.assertEquals("Wrong ids in path!", response.body().asString())
        );
    }

    @Test
    @Order(5)
    void setReadAPISenderRead() {
        User sender = userRepository.findByLogin(clientLogin).orElseThrow();
        Pageable pageable = PageRequest.of(0, 50);
        String tokenResponse = getToken(clientLogin, clientLogin, clientLogin);

        Notification notification = notificationRepository.findAllBySenderProfile_Id(sender.getId(), pageable)
                .stream().findFirst().orElseThrow();
        notification.setIsRead(false);
        notificationRepository.save(notification);

        ExtractableResponse<Response> response =
                given()
                        .header("Content-type", "application/json")
                        .header("Authorization", "Bearer " + tokenResponse)
                        .when()
                        .post("/api/app/notification/update/" + notification.getId())
                        .then()
                        .extract();

        Assertions.assertAll(
                () -> Assertions.assertEquals(HttpStatus.FORBIDDEN.value(), response.response().getStatusCode())
        );
    }

    @Test
    @Order(6)
    void getAllQueriesAPICorrect() {
        User sender = userRepository.findByLogin(clientLogin).orElseThrow();
        User receiver = userRepository.findByLogin(artistLogin).orElseThrow();
        Pageable pageable = PageRequest.of(0, 50);
        String tokenResponse = getToken(artistLogin, artistPassword, artistRole);

        Notification notification = notificationRepository.findAllBySenderProfile_Id(sender.getId(), pageable)
                .stream().findFirst().orElseThrow();

        ExtractableResponse<Response> response =
                given()
                        .header("Content-type", "application/json")
                        .header("Authorization", "Bearer " + tokenResponse)
                        .when()
                        .get("/api/app/notification/all/" + receiver.getId() + "/" + 0)
                        .then()
                        .extract();

        List<NotificationDto> notificationDtoList = response.body().as(List.class);
        List<Notification> notificationList = dataTransformer.notificationListFromDto(notificationDtoList);

        Assertions.assertAll(
                () -> Assertions.assertEquals(HttpStatus.OK.value(), response.response().getStatusCode()),
                () -> Assertions.assertEquals(notification.getSenderProfile().getMail(),
                        notificationList.get(0).getSenderProfile().getMail())
        );
    }

    @Test
    @Order(7)
    void getNewQueriesAPICorrect() {
        User sender2 = userRepository.findByLogin(adminLogin).orElseThrow();
        User receiver = userRepository.findByLogin(artistLogin).orElseThrow();
        String tokenResponse = getToken(artistLogin, artistPassword, artistRole);

        String senderEmail = "admin@mail.ru";
        NotificationDto notificationDto = new NotificationDto();
        notificationDto.setRead(false);
        notificationDto.setReceiverId(receiver.getId());
        notificationDto.setSenderId(sender2.getId());
        notificationDto.setSenderMail(senderEmail);
        notificationDto.setTime(LocalDateTime.now());
        Notification notification1 = dataTransformer.notificationFromDto(notificationDto);
        notificationRepository.save(notification1);

        ExtractableResponse<Response> response =
                given()
                        .header("Content-type", "application/json")
                        .header("Authorization", "Bearer " + tokenResponse)
                        .when()
                        .get("/api/app/notification/new/" + receiver.getId() + "/" + 0)
                        .then()
                        .extract();

        List<NotificationDto> notificationDtoList = response.body().as(List.class);
        List<Notification> notificationList = dataTransformer.notificationListFromDto(notificationDtoList);

        Assertions.assertAll(
                () -> Assertions.assertEquals(HttpStatus.OK.value(), response.response().getStatusCode()),
                () -> Assertions.assertEquals(2, notificationList.size()),
                () -> Assertions.assertEquals(senderEmail, notificationList.get(1).getSenderProfile().getMail())
        );
    }

}
