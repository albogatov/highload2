package com.example.highload;

import com.example.highload.model.inner.Profile;
import com.example.highload.model.inner.Review;
import com.example.highload.model.inner.User;
import com.example.highload.model.network.*;
import com.example.highload.repos.ProfileRepository;
import com.example.highload.repos.ReviewRepository;
import com.example.highload.repos.UserRepository;
import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.domain.Page;
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
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ReviewControllerTest {

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

    @Test
    @Order(1)
    void saveAPICorrect() {

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
                        .post("/api/app/user/profile/add/" + userId)
                        .then()
                        .extract();

        String text = "Cat is not a Dog!";
        Profile profile = profileRepository.findByUser_Id(userId).orElseThrow();
        Pageable pageable = PageRequest.of(0, 50);
        ReviewDto reviewDto = new ReviewDto();
        reviewDto.setProfileId(profile.getId());
        reviewDto.setText(text);
        reviewDto.setUserName(clientLogin);

        ExtractableResponse<Response> response2 =
                given()
                        .header("Content-type", "application/json")
                        .header("Authorization", "Bearer " + tokenResponse)
                        .and()
                        .body(reviewDto)
                        .when()
                        .post("/api/app/review/save")
                        .then()
                        .extract();
        Assertions.assertAll(
                () -> Assertions.assertEquals(HttpStatus.OK.value(), response2.response().getStatusCode()),
                () -> {
                    Page<Review> review = reviewRepository.findAllByProfile_Id(profile.getId(), pageable);
                    Assertions.assertEquals(1, review.getNumberOfElements());
                }
        );
    }

    @Test
    @Order(2)
    void getAllByProfileAPICorrect() {
        User user = userRepository.findByLogin(clientLogin).orElseThrow();
        String tokenResponse = getToken(clientLogin, clientPassword, clientRole);
        Integer userId = user.getId();

        Profile profile = profileRepository.findByUser_Id(userId).orElseThrow();

        ExtractableResponse<Response> response =
                given()
                        .header("Content-type", "application/json")
                        .header("Authorization", "Bearer " + tokenResponse)
                        .when()
                        .get("/api/app/review/all/" + profile.getId() + "/" + 0)
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
    @Order(3)
    void getAllByProfileAPIWrongPath() {
        User user = userRepository.findByLogin(clientLogin).orElseThrow();
        String tokenResponse = getToken(clientLogin, clientPassword, clientRole);
        Integer userId = user.getId();

        Profile profile = profileRepository.findByUser_Id(userId).orElseThrow();

        ExtractableResponse<Response> response =
                given()
                        .header("Content-type", "application/json")
                        .header("Authorization", "Bearer " + tokenResponse)
                        .when()
                        .get("/api/app/review/all/" + profile.getId() + 10 + "/" + 0)
                        .then()
                        .extract();

        Assertions.assertAll(
                () -> Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response.response().getStatusCode()),
                () -> Assertions.assertEquals("Wrong ids in path!", response.body().asString())
        );
    }

    @Test
    @Order(4)
    void getByIdAPICorrect() {
        User user = userRepository.findByLogin(clientLogin).orElseThrow();
        String tokenResponse = getToken(clientLogin, clientPassword, clientRole);
        Integer userId = user.getId();

        Pageable pageable = PageRequest.of(0, 50);
        Profile profile = profileRepository.findByUser_Id(userId).orElseThrow();
        Review review = reviewRepository.findAllByProfile_Id(profile.getId(), pageable).stream().findFirst().orElseThrow();

        ExtractableResponse<Response> response =
                given()
                        .header("Content-type", "application/json")
                        .header("Authorization", "Bearer " + tokenResponse)
                        .when()
                        .get("/api/app/review/single/" + review.getId())
                        .then()
                        .extract();

        Assertions.assertAll(
                () -> Assertions.assertEquals(HttpStatus.OK.value(), response.response().getStatusCode()),
                () -> {
                    ReviewDto reviewDto = response.body().as(ReviewDto.class);
                    Assertions.assertEquals(review.getText(), reviewDto.getText());
                }
        );
    }
}
