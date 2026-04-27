package ru.yandex.practicum.shop;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.shop.client.api.PaymentApi;
import ru.yandex.practicum.shop.client.model.BalanceResponse;
import ru.yandex.practicum.shop.config.AuthorizationServerMock;
import ru.yandex.practicum.shop.config.BaseIntegrationTest;
import ru.yandex.practicum.shop.model.CartItem;
import ru.yandex.practicum.shop.model.Item;
import ru.yandex.practicum.shop.model.User;
import ru.yandex.practicum.shop.repository.CartItemRepository;
import ru.yandex.practicum.shop.repository.ItemRepository;
import ru.yandex.practicum.shop.repository.UserRepository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class ShopIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private PaymentApi paymentApiMock;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @BeforeEach
    void setUp() {
        AuthorizationServerMock.start();

        // Подготовка данных в БД
        User user = new User(1L, "testuser", "password", "USER");
        Item item = new Item(1L, "Test Item", "Desc", "/img", 100L);
        CartItem cartItem = new CartItem(1L, "testuser", 1, 2L);

        userRepository.save(user).block();
        itemRepository.save(item).block();
        cartItemRepository.save(cartItem).block();

        // Мок баланса платежа
        BalanceResponse balanceResponse = new BalanceResponse();
        balanceResponse.setSaldo(500L);
        when(paymentApiMock.getBalance("testuser"))
                .thenReturn(Mono.just(balanceResponse));
    }

    @AfterEach
    void tearDown() {
        AuthorizationServerMock.stop();

        // Очистка БД после каждого теста
        cartItemRepository.deleteAll().block();
        itemRepository.deleteAll().block();
        userRepository.deleteAll().block();
    }

    @Test
    @WithMockUser(username = "testuser", authorities = {"SCOPE_payments.read"})
    void testGetCartWithBalance() {
        webTestClient.get()
                .uri("/cart/items")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(response -> {
                    String content = response.getResponseBody().toString();
                    assertThat(content).contains("Test Item");
                    assertThat(content).contains("200"); // total
                    assertThat(content).contains("500"); // balance
                });
    }
}
