package com.example.highload;

import com.example.highload.model.inner.Image;
import com.example.highload.model.inner.ImageObject;
import com.example.highload.model.inner.Profile;
import com.example.highload.model.inner.User;
import com.example.highload.model.network.ImageDto;
import com.example.highload.model.network.JwtRequest;
import com.example.highload.model.network.JwtResponse;
import com.example.highload.repos.ProfileRepository;
import com.example.highload.repos.UserRepository;
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
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;

@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ImageObjectAPITests {

    @LocalServerPort
    private Integer port;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProfileRepository profileRepository;

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
    @Order(1)
    public void addImagesToProfile() {
         

        User artist1 = userRepository.findByLogin("artist1").orElseThrow();
        User client1 = userRepository.findByLogin("client1").orElseThrow();

        // create profiles using repo

        Profile artistProfile = new Profile();
        artistProfile.setUser(artist1);
        artistProfile.setName("Artist1");
        artistProfile.setMail("artist1@gmail.com");

        Profile artistProfileWithId = profileRepository.save(artistProfile);

        Profile clientProfile = new Profile();
        clientProfile.setUser(client1);
        clientProfile.setName("Client1");
        clientProfile.setMail("client1@gmail.com");

        Profile clientProfileWithId = profileRepository.save(clientProfile);

        // create dtos

        ImageDto imageDto1 = new ImageDto();
        imageDto1.setUrl("first");

        ImageDto imageDto2 = new ImageDto();
        imageDto2.setUrl("second");

        List<ImageDto> imageDtoList = new ArrayList<>();
        imageDtoList.add(imageDto1);
        imageDtoList.add(imageDto2);

        // get token

        String tokenResponse1 = getToken("artist1");
        String tokenResponse2 = getToken("client1");

        // add to artist profile

        ExtractableResponse<Response> response1 =
                given()
                        .header("Authorization", "Bearer " + tokenResponse1)
                        .header("Content-type", "application/json")
                        .and()
                        .body(imageDtoList)
                        .when()
                        .post("/api/image/add/profile")
                        .then()
                        .extract();

        String responseBody = response1.body().asString();
        Assertions.assertAll(
                () -> Assertions.assertEquals("Images added", response1.body().asString()),
                () -> Assertions.assertEquals(HttpStatus.OK.value(), response1.statusCode())
        );

        Profile profile1 = profileRepository.findById(artistProfileWithId.getId()).orElseThrow();
        List<ImageObject> imageObjects = profile1.getImages();
        List<Image> images = imageObjects.stream().map(ImageObject::getImage).toList();

        Assertions.assertAll(
                () -> Assertions.assertEquals(2, images.size()),
                () -> Assertions.assertEquals("first", images.get(1).getUrl()),
                () -> Assertions.assertEquals("second", images.get(0).getUrl())
        );

        // add to client profile (forbidden)

        ExtractableResponse<Response> response2 =
                given()
                        .header("Authorization", "Bearer " + tokenResponse2)
                        .header("Content-type", "application/json")
                        .and()
                        .body(imageDtoList)
                        .when()
                        .post("/api/image/add/profile")
                        .then()
                        .extract();

        Assertions.assertAll(
                () -> Assertions.assertEquals(HttpStatus.FORBIDDEN.value(), response2.statusCode())
        );

    }

    @Test
    @Order(2)
    public void changeMainImageOfProfile() {
         

        Profile artistProfile = userRepository.findByLogin("artist1").orElseThrow().getProfile();

        ImageDto imageDto = new ImageDto();
        imageDto.setUrl("main");

        // get token

        String tokenResponse1 = getToken("artist1");

        // add to artist profile

        ExtractableResponse<Response> response1 =
                given()
                        .header("Authorization", "Bearer " + tokenResponse1)
                        .header("Content-type", "application/json")
                        .and()
                        .body(imageDto)
                        .when()
                        .post("/api/image/change/profile")
                        .then()
                        .extract();

        Profile artistProfileUpdated = userRepository.findByLogin("artist1").orElseThrow().getProfile();

        Assertions.assertAll(
                () -> Assertions.assertEquals("Main image changed", response1.body().asString()),
                () -> Assertions.assertEquals(HttpStatus.OK.value(), response1.statusCode()),
                ()-> Assertions.assertNotNull(artistProfileUpdated.getImage()),
                ()-> Assertions.assertEquals("main", artistProfileUpdated.getImage().getUrl())
        );

    }

    @Test
    @Order(3)
    public void removeImageForProfile() {
         

        Profile artistProfile = userRepository.findByLogin("artist1").orElseThrow().getProfile();
        Profile clientProfile = userRepository.findByLogin("client1").orElseThrow().getProfile();

        int imageId = artistProfile.getImages().get(0).getImage().getId();

        // get token

        String tokenResponse1 = getToken("artist1");
        String tokenResponse2 = getToken("client1");

        // remove from artist profile

        ExtractableResponse<Response> response1 =
                given()
                        .header("Authorization", "Bearer " + tokenResponse1)
                        .header("Content-type", "application/json")
                        .when()
                        .post("/api/image/remove/profile/" + imageId)
                        .then()
                        .extract();

        Profile artistProfileUpdated = userRepository.findByLogin("artist1").orElseThrow().getProfile();


        Assertions.assertAll(
                () -> Assertions.assertEquals("Image removed", response1.body().asString()),
                () -> Assertions.assertEquals(HttpStatus.OK.value(), response1.statusCode()),
                ()-> Assertions.assertEquals(1, artistProfileUpdated.getImages().size()),
                ()-> Assertions.assertEquals("first", artistProfileUpdated.getImages().get(0).getImage().getUrl())
        );

        // remove from client profile (forbidden)

        ExtractableResponse<Response> response2 =
                given()
                        .header("Authorization", "Bearer " + tokenResponse2)
                        .header("Content-type", "application/json")
                        .when()
                        .post("/api/image/remove/profile/" + imageId)
                        .then()
                        .extract();

        Assertions.assertAll(
                () -> Assertions.assertEquals(HttpStatus.FORBIDDEN.value(), response2.statusCode())
        );

    }

}
