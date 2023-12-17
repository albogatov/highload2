package com.example.highload;

import com.example.highload.model.enums.OrderStatus;
import com.example.highload.model.inner.ClientOrder;
import com.example.highload.model.inner.User;
import com.example.highload.model.network.JwtRequest;
import com.example.highload.model.network.JwtResponse;
import com.example.highload.model.network.OrderDto;
import com.example.highload.model.network.ResponseDto;
import com.example.highload.repos.OrderRepository;
import com.example.highload.repos.ResponseRepository;
import com.example.highload.repos.TagRepository;
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
import org.springframework.data.domain.Page;
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
public class ResponseAPITests {

    @LocalServerPort
    private Integer port;

    @Autowired
    UserRepository userRepository;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    ResponseRepository responseRepository;

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
    @Order(2)
    public void addResponse() {
         
        User client1 = userRepository.findByLogin("client1").orElseThrow();
        User artist1 = userRepository.findByLogin("artist1").orElseThrow();

        ClientOrder clientOrder1 = new ClientOrder();
        clientOrder1.setStatus(OrderStatus.OPEN);
        clientOrder1.setDescription("1o");
        clientOrder1.setPrice(1);
        clientOrder1.setUser(client1);
        clientOrder1.setCreated(LocalDateTime.now());
        ClientOrder clientOrder1WithId = orderRepository.save(clientOrder1);

        // get token

        String artistTokenResponse = getToken("artist1");

        // save valid

        ResponseDto responseDto = new ResponseDto();
        responseDto.setApproved(false);
        responseDto.setText("-");
        responseDto.setUserId(artist1.getId());
        responseDto.setUserName(artist1.getLogin());
        responseDto.setOrderId(clientOrder1WithId.getId());

        ExtractableResponse<Response> response1 =
                given()
                        .header("Authorization", "Bearer " + artistTokenResponse)
                        .header("Content-type", "application/json")
                        .and()
                        .body(responseDto)
                        .when()
                        .post("/api/response/save")
                        .then()
                        .extract();

        Pageable pageable = PageRequest.of(0, 50);
        Page<com.example.highload.model.inner.Response> result = responseRepository
                .findAllByOrder_Id(clientOrder1WithId.getId(), pageable).orElse(Page.empty());

        Assertions.assertAll(
                () -> Assertions.assertEquals("Response added", response1.body().asString()),
                () -> Assertions.assertEquals(HttpStatus.OK.value(), response1.statusCode()),
                () -> Assertions.assertEquals(1, result.getTotalElements()),
                () -> Assertions.assertEquals(1, result.getTotalPages()),
                () -> Assertions.assertEquals("artist1", result.getContent().get(0).getUser().getLogin())
        );
    }

    @Test
    @Order(2)
    public void getAllByOrder() {
         

//        User client1 = userRepository.findByLogin("client1").orElseThrow();
        ClientOrder clientOrder = orderRepository.findById(1).orElseThrow();

        // get token

        String tokenResponse = getToken("client1");

        // get all

        ExtractableResponse<Response> response1 =
                given()
                        .header("Authorization", "Bearer " + tokenResponse)
                        .header("Content-type", "application/json")
                        .when()
                        .get("/api/response/all/order/" + clientOrder.getId() + "/0")
                        .then()
                        .extract();


        Assertions.assertAll(
                () -> Assertions.assertEquals(HttpStatus.OK.value(), response1.statusCode()),
                () -> Assertions.assertEquals("false", response1.header("app-page-has-next")),
                () -> Assertions.assertEquals("0", response1.header("app-current-page-num"))
        );

        List<ResponseDto> responseDtos = response1.body().jsonPath().getList(".", ResponseDto.class);

        Assertions.assertAll(
                () -> Assertions.assertEquals(1, responseDtos.size()),
                () -> Assertions.assertEquals("artist1", responseDtos.get(0).getUserName())
        );


    }


    @Test
    @Order(3)
    public void getAllByUser() {
         

        User artist1 = userRepository.findByLogin("artist1").orElseThrow();
        ClientOrder clientOrder = orderRepository.findById(1).orElseThrow();

        // get token

        String tokenResponse = getToken("artist1");

        // get all

        ExtractableResponse<Response> response1 =
                given()
                        .header("Authorization", "Bearer " + tokenResponse)
                        .header("Content-type", "application/json")
                        .when()
                        .get("/api/response/all/user/" + artist1.getId() + "/0")
                        .then()
                        .extract();


        Assertions.assertAll(
                () -> Assertions.assertEquals(HttpStatus.OK.value(), response1.statusCode()),
                () -> Assertions.assertEquals("1", response1.header("app-total-page-num")),
                () -> Assertions.assertEquals("1", response1.header("app-total-items-num")),
                () -> Assertions.assertEquals("0", response1.header("app-current-page-num")),
                () -> Assertions.assertEquals("1", response1.header("app-current-items-num"))
        );

        List<ResponseDto> responseDtos = response1.body().jsonPath().getList(".", ResponseDto.class);

        Assertions.assertAll(
                () -> Assertions.assertEquals(1, responseDtos.size()),
                () -> Assertions.assertEquals("-", responseDtos.get(0).getText())
        );


    }
}
