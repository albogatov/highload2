package com.example.highload;

import com.example.highload.model.enums.ImageObjectType;
import com.example.highload.model.inner.Image;
import com.example.highload.model.inner.ImageObject;
import com.example.highload.model.inner.Profile;
import com.example.highload.model.inner.User;
import com.example.highload.model.network.*;
import com.example.highload.repos.ImageObjectRepository;
import com.example.highload.repos.ImageRepository;
import com.example.highload.repos.ProfileRepository;
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
public class ProfileAPITests {

    @LocalServerPort
    private Integer port;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProfileRepository profileRepository;

    @Autowired
    ImageRepository imageRepository;

    @Autowired
    ImageObjectRepository imageObjectRepository;

    @Autowired
    DataTransformer dataTransformer;

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
    public void editProfile() {

        User artist1 = userRepository.findByLogin("artist1").orElseThrow();


        Profile artistProfile = new Profile();
        artistProfile.setUser(artist1);
        artistProfile.setName("Artist1");
        artistProfile.setMail("artist1@gmail.com");

        Profile artistProfileWithId = profileRepository.save(artistProfile);

        // get token

        String tokenResponse = getToken("artist1");

        // edit

        ProfileDto artistProfileDto = dataTransformer.profileToDto(artistProfileWithId);
        artistProfileDto.setName("Artist1Updated");
        artistProfileDto.setMail("artist1updated@gmail.com");

        ExtractableResponse<Response> response1 =
                given()
                        .header("Authorization", "Bearer " + tokenResponse)
                        .header("Content-type", "application/json")
                        .and()
                        .body(artistProfileDto)
                        .when()
                        .post("/api/profile/edit")
                        .then()
                        .extract();

        Profile result = profileRepository.findById(artistProfileWithId.getId()).orElseThrow();

        Assertions.assertAll(
                () -> Assertions.assertEquals("Profile edited", response1.body().asString()),
                () -> Assertions.assertEquals(HttpStatus.OK.value(), response1.statusCode()),
                () -> Assertions.assertEquals("Artist1Updated", result.getName()),
                () -> Assertions.assertEquals("artist1updated@gmail.com", result.getMail())
        );

        // edit with wrong data
        artistProfileDto.setName("");
        artistProfileDto.setMail("lala");

        ExtractableResponse<Response> response2 =
                given()
                        .header("Authorization", "Bearer " + tokenResponse)
                        .header("Content-type", "application/json")
                        .and()
                        .body(artistProfileDto)
                        .when()
                        .post("/api/profile/edit")
                        .then()
                        .extract();

        Assertions.assertAll(
                () -> Assertions.assertEquals("Request body validation failed!", response2.body().asString()),
                () -> Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response2.statusCode())
        );

    }

    @Test
    @Order(2)
    public void getProfile() {  

        // create profile using repo

        User client1 = userRepository.findByLogin("client1").orElseThrow();

        Profile clientProfile = new Profile();
        clientProfile.setUser(client1);
        clientProfile.setName("Client1");
        clientProfile.setMail("client1@gmail.com");

        Profile clientProfileWithId = profileRepository.save(clientProfile);

        // get token

        String tokenResponse = getToken("client1");

        // get existing profile

        ExtractableResponse<Response> response1 =
                given()
                        .header("Authorization", "Bearer " + tokenResponse)
                        .header("Content-type", "application/json")
                        .when()
                        .get("/api/profile/single/" + clientProfileWithId.getId().toString())
                        .then()
                        .extract();

        ProfileDto result = response1.body().as(ProfileDto.class);

        Assertions.assertAll(
                () -> Assertions.assertEquals(HttpStatus.OK.value(), response1.statusCode()),
                () -> Assertions.assertEquals(clientProfileWithId.getId(), result.getId()),
                () -> Assertions.assertEquals(clientProfileWithId.getName(), result.getName())
        );

        // get not existing profile

        int badId = clientProfileWithId.getId() + 1;

        ExtractableResponse<Response> response2 =
                given()
                        .header("Authorization", "Bearer " + tokenResponse)
                        .header("Content-type", "application/json")
                        .when()
                        .get("/api/profile/single/" + badId)
                        .then()
                        .extract();

        Assertions.assertAll(
                () -> Assertions.assertEquals("Wrong ids in path!", response2.body().asString()),
                () -> Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response2.statusCode())
        );
    }

    @Test
    @Order(3)
    public void getAllProfiles() {  

        // get token

        String tokenResponse = getToken("client1");

        // get all

        ExtractableResponse<Response> response1 =
                given()
                        .header("Authorization", "Bearer " + tokenResponse)
                        .header("Content-type", "application/json")
                        .when()
                        .get("/api/profile/all/0")
                        .then()
                        .extract();

        Assertions.assertAll(
                () -> Assertions.assertEquals(HttpStatus.OK.value(), response1.statusCode()),
                () -> Assertions.assertEquals("1", response1.header("app-total-page-num")),
                () -> Assertions.assertEquals("2", response1.header("app-total-items-num")),
                () -> Assertions.assertEquals("0", response1.header("app-current-page-num")),
                () -> Assertions.assertEquals("2", response1.header("app-current-items-num"))
        );

        List<ProfileDto> profileDtos = response1.body().jsonPath().getList(".", ProfileDto.class);

        Assertions.assertAll(
                () -> Assertions.assertEquals(2, profileDtos.size()),
                () -> Assertions.assertEquals("Artist1Updated", profileDtos.get(0).getName()),
                () -> Assertions.assertEquals("Client1", profileDtos.get(1).getName())
        );
    }

    @Test
    @Order(4)
    public void getProfileImages() {  

        Profile artistProfile = userRepository.findByLogin("artist1").orElseThrow().getProfile();

        // add images to profile using repos

        Image image1 = new Image();
        image1.setUrl("first");
        image1 = imageRepository.save(image1);

        ImageObject imageObject1 = new ImageObject();
        imageObject1.setImage(image1);
        imageObject1.setProfile(artistProfile);
        imageObject1.setType(ImageObjectType.PROFILE_IMAGE);

        ImageObject imageObject1WithId = imageObjectRepository.save(imageObject1);

        Image image2 = new Image();
        image2.setUrl("second");
        image2 = imageRepository.save(image2);

        ImageObject imageObject2 = new ImageObject();
        imageObject2.setImage(image2);
        imageObject2.setProfile(artistProfile);
        imageObject2.setType(ImageObjectType.PROFILE_IMAGE);

        ImageObject imageObject2WithId = imageObjectRepository.save(imageObject2);

        // get token

        String tokenResponse = getToken("artist1");

        // get profile images

        ExtractableResponse<Response> response1 =
                given()
                        .header("Authorization", "Bearer " + tokenResponse)
                        .header("Content-type", "application/json")
                        .when()
                        .get("/api/profile/single/" + artistProfile.getId() + "/images/0")
                        .then()
                        .extract();

        Assertions.assertAll(
                () -> Assertions.assertEquals(HttpStatus.OK.value(), response1.statusCode()),
                () -> Assertions.assertEquals("1", response1.header("app-total-page-num")),
                () -> Assertions.assertEquals("2", response1.header("app-total-items-num")),
                () -> Assertions.assertEquals("0", response1.header("app-current-page-num")),
                () -> Assertions.assertEquals("2", response1.header("app-current-items-num"))
        );

        List<ImageDto> imageDtos = response1.body().jsonPath().getList(".", ImageDto.class);

        Assertions.assertAll(
                () -> Assertions.assertEquals(2, imageDtos.size()),
                () -> Assertions.assertEquals("first", imageDtos.get(0).getUrl()),
                () -> Assertions.assertEquals("second", imageDtos.get(1).getUrl())
        );

        // get not existing profile images

        ExtractableResponse<Response> response2 =
                given()
                        .header("Authorization", "Bearer " + tokenResponse)
                        .header("Content-type", "application/json")
                        .when()
                        .get("/api/profile/single/" + (artistProfile.getId() + 2) + "/images/0")
                        .then()
                        .extract();

        Assertions.assertAll(
                () -> Assertions.assertEquals("Wrong ids in path!", response2.body().asString()),
                () -> Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response2.statusCode())
        );

    }

}
