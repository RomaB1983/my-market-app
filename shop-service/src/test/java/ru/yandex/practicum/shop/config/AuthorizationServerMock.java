package ru.yandex.practicum.shop.config;

import com.github.tomakehurst.wiremock.WireMockServer;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public class AuthorizationServerMock {
    private static WireMockServer authServer;

    public static void start() {
        authServer = new WireMockServer(options().port(3000));
        authServer.start();

        authServer.stubFor(post(urlEqualTo("/oauth2/token"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"access_token\": \"user-token\", \"token_type\": \"Bearer\", \"expires_in\": 3600}")
                ));

        authServer.stubFor(get(urlEqualTo("/oauth2/jwks"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(generateTestJWK())
                ));
    }

    private static String generateTestJWK() {
        return "{\"keys\": [{\"kty\": \"RSA\", \"use\": \"sig\", \"kid\": \"test-key\", \"n\": \"modulus\", \"e\": \"AQAB\"}]}";
    }

    public static void stop() {
        if (authServer != null) {
            authServer.stop();
        }
    }
}
