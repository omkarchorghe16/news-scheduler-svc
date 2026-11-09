package com.stocknews.client;

import com.stocknews.config.FmpProperties;
import com.stocknews.model.FmpApiCall;
import com.stocknews.model.FmpStockProfile;
import com.stocknews.repository.FmpApiCallRepository;
import com.stocknews.repository.FmpStockProfileRepository;
import com.stocknews.service.FmpProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FmpProfileServiceTest {
    @Mock
    private FmpProfileClient client;

    @Mock
    private FmpStockProfileRepository profileRepository;

    @Mock
    private FmpApiCallRepository apiCallRepository;

    private FmpProfileService service;

    @BeforeEach
    void setUp() {
        FmpProperties properties = new FmpProperties();
        properties.setApiKey("test-api-key");
        service = new FmpProfileService(client, profileRepository, apiCallRepository, properties);
    }

    @Test
    void fetchesAndPersistsUniqueSymbolsAndTracksQuota() {
        FmpStockProfile profile = new FmpStockProfile();
        profile.setSymbol("AAPL");
        when(apiCallRepository.countByCalledAtAfter(any())).thenReturn(0L);
        when(profileRepository.findAllBySymbolIn(List.of("AAPL"))).thenReturn(List.of());
        when(client.fetchProfile("AAPL")).thenReturn(profile);
        when(profileRepository.saveAllAndFlush(any())).thenReturn(List.of(profile));

        List<FmpStockProfile> saved = service.fetchAndSave(List.of(" aapl ", "AAPL"));

        assertEquals(List.of(profile), saved);
        verify(apiCallRepository).saveAndFlush(any(FmpApiCall.class));
        verify(profileRepository).saveAllAndFlush(any());
    }

    @Test
    void rejectsRequestsThatWouldExceedTheConfiguredDailyQuota() {
        when(apiCallRepository.countByCalledAtAfter(any())).thenReturn(249L);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.fetchAndSave(List.of("AAPL", "MSFT")));

        assertEquals(429, exception.getStatusCode().value());
        verify(client, never()).fetchProfile(any());
    }

    @Test
    void rejectsIndianSymbolsOnTheBasicPlan() {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.fetchAndSave(List.of("RELIANCE.NS")));

        assertEquals(400, exception.getStatusCode().value());
        verify(apiCallRepository, never()).countByCalledAtAfter(any());
    }

    @Test
    void returnsRecentPostgresProfileWithoutCallingFmpOrSpendingQuota() {
        FmpStockProfile cachedProfile = new FmpStockProfile();
        cachedProfile.setSymbol("AAPL");
        cachedProfile.setUpdatedAt(LocalDateTime.now().minusHours(1));
        when(profileRepository.findAllBySymbolIn(List.of("AAPL"))).thenReturn(List.of(cachedProfile));

        List<FmpStockProfile> profiles = service.fetchAndSave(List.of("AAPL"));

        assertEquals(List.of(cachedProfile), profiles);
        verify(client, never()).fetchProfile(any());
        verify(apiCallRepository, never()).countByCalledAtAfter(any());
        verify(profileRepository, never()).saveAllAndFlush(any());
    }

    @Test
    void updatesAnExpiredSavedProfileInsteadOfCreatingAnotherRow() {
        FmpStockProfile savedProfile = new FmpStockProfile();
        savedProfile.setId(42L);
        savedProfile.setSymbol("AAPL");
        savedProfile.setCompanyName("Old Name");
        savedProfile.setUpdatedAt(LocalDateTime.now().minusHours(25));

        FmpStockProfile refreshedProfile = new FmpStockProfile();
        refreshedProfile.setSymbol("AAPL");
        refreshedProfile.setCompanyName("Updated Name");
        when(profileRepository.findAllBySymbolIn(List.of("AAPL"))).thenReturn(List.of(savedProfile));
        when(apiCallRepository.countByCalledAtAfter(any())).thenReturn(0L);
        when(client.fetchProfile("AAPL")).thenReturn(refreshedProfile);
        when(profileRepository.saveAllAndFlush(any())).thenReturn(List.of(refreshedProfile));

        List<FmpStockProfile> result = service.fetchAndSave(List.of("AAPL"));

        assertEquals(42L, refreshedProfile.getId());
        assertEquals("Updated Name", result.getFirst().getCompanyName());
        verify(profileRepository).saveAllAndFlush(List.of(refreshedProfile));
    }

    @Test
    void returnsFullSavedProfilesInRequestOrderWithoutCallingFmp() {
        FmpStockProfile msft = new FmpStockProfile();
        msft.setSymbol("MSFT");
        msft.setCountry("US");
        FmpStockProfile aapl = new FmpStockProfile();
        aapl.setSymbol("AAPL");
        aapl.setCountry("US");
        when(profileRepository.findAllBySymbolIn(List.of("MSFT", "MISSING", "AAPL")))
                .thenReturn(List.of(aapl, msft));

        List<FmpStockProfile> result =
                service.findSavedProfiles(List.of(" msft ", "MISSING", "AAPL", "MSFT"));

        assertEquals(List.of(msft, aapl), result);
        verify(client, never()).fetchProfile(any());
    }
}
