package com.hubspot.oauth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hubspot.oauth.repository.HubSpotWebhookEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class HubSpotWebhookServiceTest {
    @Mock
    private HubSpotWebhookEventRepository repository;

    @InjectMocks
    private HubSpotWebhookService service;

    @BeforeEach
    void setUp() {
        service = new HubSpotWebhookService(repository);
    }

    @Test
    void testProcessWebhookEventsWithContactCreation() throws JsonProcessingException {
        // JSON contendo dois eventos, um com "contact.creation" e outro com tipo diferente
        String json = "[\n" +
                "  {\"eventId\": 1, \"subscriptionType\": \"contact.creation\"},\n" +
                "  {\"eventId\": 2, \"subscriptionType\": \"other.type\"}\n" +
                "]";
        service.processWebhookEvents(json);
        verify(repository, times(1)).save(Mockito.any());
    }

    @Test
    void testProcessWebhookEventsWithNoMatchingEvents() throws JsonProcessingException {
        // JSON sem nenhum evento do tipo "contact.creation"
        String json = "[\n" +
                "  {\"eventId\": 1, \"subscriptionType\": \"not.creation\"}\n" +
                "]";
        service.processWebhookEvents(json);

        verify(repository, times(0)).save(Mockito.any());
    }

    @Test
    void testProcessWebhookEventsWithInvalidJson() {
        String invalidJson = "json inválido";
        assertThrows(JsonProcessingException.class, () -> service.processWebhookEvents(invalidJson));
    }

}
