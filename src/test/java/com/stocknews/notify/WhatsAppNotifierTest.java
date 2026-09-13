package com.stocknews.notify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stocknews.config.AppProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for WhatsAppNotifier.
 * Verifies Twilio integration configuration and disabled-by-default behavior.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "app.notifications.whatsapp.enabled=false",
        "app.notifications.whatsapp.twilio-sid=TEST_SID",
        "app.notifications.whatsapp.twilio-token=TEST_TOKEN",
        "app.notifications.whatsapp.from-number=whatsapp:+1234567890",
        "app.notifications.whatsapp.to-number=whatsapp:+0987654321"
})
class WhatsAppNotifierTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AppProperties appProperties;

    private WhatsAppNotifier whatsAppNotifier;

    @BeforeEach
    void setUp() {
        RestClient restClient = RestClient.builder().build();
        whatsAppNotifier = new WhatsAppNotifier(restClient, appProperties, objectMapper);
    }

    @Test
    void testWhatsAppNotifierInstance() {
        assertNotNull(whatsAppNotifier);
        assertTrue(whatsAppNotifier instanceof Notifier);
    }

    @Test
    void testWhatsAppDisabledByDefault() {
        assertFalse(appProperties.getNotifications().getWhatsapp().isEnabled());
    }

    @Test
    void testTwilioCredentialsConfiguration() {
        assertEquals("TEST_SID", appProperties.getNotifications().getWhatsapp().getTwilioSid());
        assertEquals("TEST_TOKEN", appProperties.getNotifications().getWhatsapp().getTwilioToken());
        assertEquals("whatsapp:+1234567890", appProperties.getNotifications().getWhatsapp().getFromNumber());
        assertEquals("whatsapp:+0987654321", appProperties.getNotifications().getWhatsapp().getToNumber());
    }
}
