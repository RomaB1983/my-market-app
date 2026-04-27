package ru.yandex.practicum.shop;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.shop.client.model.BalanceResponse;
import ru.yandex.practicum.shop.client.model.PaymentRequest;
import ru.yandex.practicum.shop.client.model.PaymentResponse;
import ru.yandex.practicum.shop.client.model.PaymentStatus;
import ru.yandex.practicum.shop.config.TestCommonConfig;
import ru.yandex.practicum.shop.model.Item;
import ru.yandex.practicum.shop.model.User;
import ru.yandex.practicum.shop.repository.ItemRepository;
import ru.yandex.practicum.shop.repository.UserRepository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@ContextConfiguration(initializers = {IntegrationTest.Initializer.class})
@Import(TestCommonConfig.class)
@ActiveProfiles("test")
public  class IntegrationTest {

    static {
        System.setProperty("docker.client.strategy", "org.testcontainers.dockerclient.DockerDesktopClientProviderStrategy");
        System.setProperty("DOCKER_HOST", "npipe:////./pipe/docker_cli");
    }

    static PostgreSQLContainer<?> shopDb = new PostgreSQLContainer<>("postgres:18")
            .withDatabaseName("shop_test")
            .withUsername("test")
            .withPassword("test");

//    static PostgreSQLContainer<?> paymentDb = new PostgreSQLContainer<>("postgres:13")
//            .withDatabaseName("payment_test")
//            .withUsername("test")
//            .withPassword("test");

    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:6-alpine"))
            .withExposedPorts(6379);



    static {
        shopDb.start();
      //  paymentDb.start();
        redis.start();
    }

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext context) {
            TestPropertyValues values = TestPropertyValues.of(
                    "spring.r2dbc.url=" + shopDb.getJdbcUrl().replace("jdbc:", "r2dbc:"),
                    "spring.r2dbc.username=" + shopDb.getUsername(),
                    "spring.r2dbc.password=" + shopDb.getPassword(),
                    "app.payment.service.url=http://localhost:8081",// + paymentDb.getMappedPort(8081),
                    "spring.redis.host=" + redis.getHost(),
                    "spring.redis.port=" + redis.getFirstMappedPort()
//                    "spring.security.oauth2.resourceserver.jwt.issuer-uri=" + keycloak.getAuthServerUrl() + "/realms/test",
//                    "keycloak.url=" + keycloak.getAuthServerUrl()
            );
            values.applyTo(context);
        }
    }


    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @BeforeEach
    void setUp() {
        userRepository.deleteAll().block();
        itemRepository.deleteAll().block();

        User testUser = User.builder()
                .username("testuser")
                .password(passwordEncoder.encode("password"))
                .role("ROLE_USER")
                .build();
        userRepository.save(testUser).block();

        Item testItem = Item.builder()
                .title("Test Item")
                .description("Description")
                .price(100L)
                .imgPath("image.jpg")
                .build();
        itemRepository.save(testItem).block();
    }
//
//    @Test
//    @WithMockUser(username = "testuser", roles = {"USER"})
//    void getCartItems_ShouldReturnCartWithBalance_WhenUserHasItems() {
//        cartService.modifyItem("testuser", 1L, 2).block();
//
//        webTestClient.get()
//                .uri("/cart/items")
//                .exchange()
//                .expectStatus().isOk()
//                .expectBody()
//                .consumeWith(result -> {
//                    String response = result.getResponseBody().toString();
//                    assertThat(response).contains("cart");
//                    assertThat(response).contains("Test Item");
//                    assertThat(response).contains("200");
//                    assertThat(response).contains("isOkBalance");
//                });
//    }

    @Test
    @WithMockUser(username = "testuser", authorities = {"SCOPE_payments.read"})
    void getBalance_ShouldBeAccessibleWithReadScope() {
        webTestClient.get()
                .uri("/api/balance")
                .headers(jwt -> jwt.setBearerAuth("test-token"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(BalanceResponse.class)
                .value(response -> assertThat(response.getSaldo()).isNotNull());
    }

    @Test
    @WithMockUser(username = "testuser", authorities = {"SCOPE_payments.write"})
    void createPayment_ShouldBeAccessibleWithWriteScope() {
        PaymentRequest request = new PaymentRequest();
        request.setTotalSum(100L);

        webTestClient.post()
                .uri("/api/pay")
                .body(Mono.just(request), PaymentRequest.class)
                .headers(jwt -> jwt.setBearerAuth("test-token"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(PaymentResponse.class)
                .value(response -> {
                    assertThat(response.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
                    assertThat(response.getDescription()).contains("Успешное выполнение оплаты");
                });
    }

    @Test
    @WithMockUser(username = "testuser", authorities = {"SCOPE_invalid.scope"})
    void getBalance_ShouldReturnForbiddenWithInvalidScope() {
        webTestClient.get()
                .uri("/api/balance")
                .headers(jwt -> jwt.setBearerAuth("test-token"))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void getCartItems_ShouldRedirectToLogin_WhenUnauthenticated() {
        webTestClient.get()
                .uri("/cart/items")
                .exchange()
                .expectStatus().isSeeOther()
                .expectHeader().valueEquals("Location", "/login");
    }
//
//    @Test
//    @WithMockUser(username = "testuser")
//    void paymentFlow_ShouldCompleteSuccessfully_WhenBalanceSufficient() {
//        cartService.modifyItem("testuser", 1L, 3).block();
//
//        webTestClient.get()
//                .uri("/cart/items")
//                .exchange()
//                .expectStatus().isOk()
//                .expectBody()
//                .consumeWith(result -> {
//                    String response = result.getResponseBody().toString();
//                    assertThat(response).contains("300"); // total price
//                    assertThat(response).contains("isOkBalance");
//                });
//
//        PaymentRequest paymentRequest = new PaymentRequest();
//        paymentRequest.setTotalSum(300L);
//
//        webTestClient.post()
//                .uri("/api/pay")
//                .body(Mono.just(paymentRequest), PaymentRequest.class)
//                .headers(jwt -> jwt.setBearerAuth("test-token"))
//                .exchange()
//                .expectStatus().isOk()
//                .expectBody(PaymentResponse.class)
//                .value(response ->
//                        assertThat(response.getStatus()).isEqualTo(PaymentStatus.SUCCESS)
//                );
//    }

}