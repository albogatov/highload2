package com.example.highload;

import com.example.highload.model.enums.OrderStatus;
import com.example.highload.model.inner.ClientOrder;
import com.example.highload.model.inner.Tag;
import com.example.highload.model.inner.User;
import com.example.highload.model.network.*;
import com.example.highload.repos.*;
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
public class ClientOrderAPITests {

    @LocalServerPort
    private Integer port;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TagRepository tagRepository;

    @Autowired
    OrderRepository orderRepository;

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
                .post("/api/app/user/login")
                .then()
                .extract().body().as(JwtResponse.class).getToken();
    }


    @Test
    @Order(1)
    public void addOrder() {
        /* TODO: RUN */

        User client1 = userRepository.findByLogin("client1").orElseThrow();
        // create order

        Tag tag1 = new Tag();
        tag1.setName("1t");
        tag1 = tagRepository.save(tag1);
        TagDto tagDto1 = dataTransformer.tagToDto(tag1);

        Tag tag2 = new Tag();
        tag2.setName("2t");
        tag2 = tagRepository.save(tag2);
        TagDto tagDto2 = dataTransformer.tagToDto(tag2);

        Tag tag3 = new Tag();
        tag3.setName("3t");
        tag3 = tagRepository.save(tag3);
        TagDto tagDto3 = dataTransformer.tagToDto(tag3);

        OrderDto orderDto = new OrderDto();
        orderDto.setDescription("1o");
        orderDto.setPrice(1);
        orderDto.setStatus(OrderStatus.OPEN);
        orderDto.setCreated(LocalDateTime.now());
        orderDto.setUserName(client1.getLogin());
        orderDto.setUserId(client1.getId());
        orderDto.setTags(List.of(tagDto1, tagDto2, tagDto3));

        // get token

        String clientTokenResponse = getToken("client1");

        // save valid

        ExtractableResponse<Response> response1 =
                given()
                        .header("Authorization", "Bearer " + clientTokenResponse)
                        .header("Content-type", "application/json")
                        .and()
                        .body(orderDto)
                        .when()
                        .post("/api/app/order/save")
                        .then()
                        .extract();

        Pageable pageable = PageRequest.of(0, 50);
        Page<ClientOrder> result = orderRepository.findAllByUser_Id(client1.getId(), pageable);

        Assertions.assertAll(
                () -> Assertions.assertEquals("Order saved", response1.body().asString()),
                () -> Assertions.assertEquals(HttpStatus.OK.value(), response1.statusCode()),
                () -> Assertions.assertEquals(1, result.getTotalElements()),
                () -> Assertions.assertEquals(1, result.getTotalPages()),
                () -> Assertions.assertEquals("1o", result.getContent().get(0).getDescription())
        );

        // try save invalid

        orderDto.setDescription("");
        orderDto.setStatus(null);

        ExtractableResponse<Response> response2 =
                given()
                        .header("Authorization", "Bearer " + clientTokenResponse)
                        .header("Content-type", "application/json")
                        .and()
                        .body(orderDto)
                        .when()
                        .post("/api/app/order/save")
                        .then()
                        .extract();

        Assertions.assertAll(
                () -> Assertions.assertEquals("Request body validation failed!", response2.body().asString()),
                () -> Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response2.statusCode())
        );

        // try save not as client
        User artist1 = userRepository.findByLogin("artist1").orElseThrow();
        String artistTokenResponse = getToken("artist1");

        orderDto.setDescription("o1-1");
        orderDto.setStatus(OrderStatus.OPEN);
        orderDto.setUserId(artist1.getId());
        orderDto.setUserName(artist1.getLogin());
        ExtractableResponse<Response> response3 =
                given()
                        .header("Authorization", "Bearer " + artistTokenResponse)
                        .header("Content-type", "application/json")
                        .and()
                        .body(orderDto)
                        .when()
                        .post("/api/app/order/save")
                        .then()
                        .extract();
        Assertions.assertAll(
                () -> Assertions.assertEquals(HttpStatus.FORBIDDEN.value(), response3.statusCode())
        );
    }

    @Test
    @Order(2)
    public void updateOrder() {
        /* TODO: RUN */
        User client1 = userRepository.findByLogin("client1").orElseThrow();

        Tag tag1 = tagRepository.findByName("1t").orElseThrow();
        Tag tag2 = tagRepository.findByName("2t").orElseThrow();

        ClientOrder clientOrder1 = new ClientOrder();
        clientOrder1.setStatus(OrderStatus.OPEN);
        clientOrder1.setDescription("2o");
        clientOrder1.setPrice(2);
        clientOrder1.setUser(client1);
        clientOrder1.setTags(List.of(tag1, tag2));
        clientOrder1.setCreated(LocalDateTime.now());
        ClientOrder clientOrder1WithId = orderRepository.save(clientOrder1);

        // get token

        String clientTokenResponse = getToken("client1");

        // update valid

        OrderDto orderDto = dataTransformer.orderToDto(clientOrder1WithId);
        orderDto.setDescription("2o_upd");

        ExtractableResponse<Response> response1 =
                given()
                        .header("Authorization", "Bearer " + clientTokenResponse)
                        .header("Content-type", "application/json")
                        .and()
                        .body(orderDto)
                        .when()
                        .post("/api/app/order/update/" + clientOrder1WithId.getId())
                        .then()
                        .extract();
        Pageable pageable = PageRequest.of(0, 50);
        Page<ClientOrder> result = orderRepository.findAllByUser_Id(client1.getId(), pageable);

        Assertions.assertAll(
                () -> Assertions.assertEquals("Order updated", response1.body().asString()),
                () -> Assertions.assertEquals(HttpStatus.OK.value(), response1.statusCode()),
                () -> Assertions.assertEquals(2, result.getTotalElements()),
                () -> Assertions.assertEquals(1, result.getTotalPages()),
                () -> Assertions.assertEquals("2o_upd", result.getContent().get(1).getDescription())
        );

        // try update invalid

        orderDto.setDescription("");
        orderDto.setStatus(null);

        ExtractableResponse<Response> response2 =
                given()
                        .header("Authorization", "Bearer " + clientTokenResponse)
                        .header("Content-type", "application/json")
                        .and()
                        .body(orderDto)
                        .when()
                        .post("/api/app/order/update/" + clientOrder1WithId.getId())
                        .then()
                        .extract();

        Assertions.assertAll(
                () -> Assertions.assertEquals("Request body validation failed!", response2.body().asString()),
                () -> Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response2.statusCode())
        );
    }

    @Test
    @Order(3)
    public void getAllUserOpenOrders() {
        /* TODO: RUN */

        User client1 = userRepository.findByLogin("client1").orElseThrow();

        // get token

        String tokenResponse = getToken("client1");

        // get all

        ExtractableResponse<Response> response1 =
                given()
                        .header("Authorization", "Bearer " + tokenResponse)
                        .header("Content-type", "application/json")
                        .when()
                        .get("/api/app/order/open/user/" + client1.getId() + "/0")
                        .then()
                        .extract();


        Assertions.assertAll(
                () -> Assertions.assertEquals(HttpStatus.OK.value(), response1.statusCode()),
                () -> Assertions.assertEquals("false", response1.header("app-page-has-next")),
                () -> Assertions.assertEquals("0", response1.header("app-current-page-num"))
        );

        List<OrderDto> orderDtos = response1.body().jsonPath().getList(".", OrderDto.class);

        Assertions.assertAll(
                () -> Assertions.assertEquals(2, orderDtos.size()),
                () -> Assertions.assertEquals("1o", orderDtos.get(0).getDescription()),
                () -> Assertions.assertEquals("2o_upd", orderDtos.get(1).getDescription()),
                () -> Assertions.assertEquals(OrderStatus.OPEN, orderDtos.get(0).getStatus()),
                () -> Assertions.assertEquals(OrderStatus.OPEN, orderDtos.get(1).getStatus())
        );


    }


    @Test
    @Order(4)
    public void addTagsToOrder() {
        /* TODO: RUN */
        User client1 = userRepository.findByLogin("client1").orElseThrow();

        Tag tag1 = tagRepository.findByName("1t").orElseThrow();
//        Tag tag2 = tagRepository.findByName("2t").orElseThrow();
        Tag tag3 = tagRepository.findByName("3t").orElseThrow();

        ClientOrder clientOrder1 = new ClientOrder();
        clientOrder1.setStatus(OrderStatus.OPEN);
        clientOrder1.setDescription("3o");
        clientOrder1.setPrice(3);
        clientOrder1.setUser(client1);
        clientOrder1.setTags(List.of(tag1));
        clientOrder1.setCreated(LocalDateTime.now());
        ClientOrder clientOrder1WithId = orderRepository.save(clientOrder1);


        TagDto tagDtoExisting = dataTransformer.tagToDto(tag3);

        TagDto tagDtoNotExisting = new TagDto();
        tagDtoNotExisting.setName("4t_ne");

        // get token

        String clientTokenResponse = getToken("client1");

        // add existing

        ExtractableResponse<Response> response1 =
                given()
                        .header("Authorization", "Bearer " + clientTokenResponse)
                        .header("Content-type", "application/json")
                        .and()
                        .body(List.of(tagDtoExisting.getId()))
                        .when()
                        .get("/api/app/order/single/" + clientOrder1WithId.getId() + "/tags/add")
                        .then()
                        .extract();

        OrderDto orderDto = response1.body().as(OrderDto.class);
        Assertions.assertAll(
                () -> Assertions.assertEquals(HttpStatus.OK.value(), response1.statusCode()),
                () -> Assertions.assertEquals(2, orderDto.getTags().size()),
                () -> Assertions.assertEquals("3t", orderDto.getTags().get(1).getName())
        );

        // add not existing

        ExtractableResponse<Response> response2 =
                given()
                        .header("Authorization", "Bearer " + clientTokenResponse)
                        .header("Content-type", "application/json")
                        .and()
                        .body(List.of(tagDtoNotExisting))
                        .when()
                        .get("/api/app/order/single/" + clientOrder1WithId.getId() + "/tags/add")
                        .then()
                        .extract();

        Assertions.assertAll(
                () -> Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response2.statusCode())
        );
    }

    @Test
    @Order(5)
    public void getAllOrdersByTags() {
        /* TODO: implement, RUN */
    }


}
