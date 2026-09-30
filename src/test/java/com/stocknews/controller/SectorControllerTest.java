package com.stocknews.controller;

import com.stocknews.dto.SectorRequest;
import com.stocknews.dto.SectorResponse;
import com.stocknews.service.SectorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SectorController.class)
class SectorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SectorService sectorService;

    @Test
    void createReturnsCreatedResourceAndLocation() throws Exception {
        when(sectorService.create(new SectorRequest("Technology")))
                .thenReturn(new SectorResponse(7, "Technology", LocalDateTime.now()));

        mockMvc.perform(post("/api/sectors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sectorName\":\"Technology\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/sectors/7"))
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.sectorName").value("Technology"));
    }

    @Test
    void findAllReturnsSectors() throws Exception {
        when(sectorService.findAll())
                .thenReturn(List.of(new SectorResponse(7, "Technology", LocalDateTime.now())));

        mockMvc.perform(get("/api/sectors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sectorName").value("Technology"));
    }

    @Test
    void findByIdDelegatesToService() throws Exception {
        when(sectorService.findById(7))
                .thenReturn(new SectorResponse(7, "Technology", LocalDateTime.now()));

        mockMvc.perform(get("/api/sectors/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7));

        verify(sectorService).findById(7);
    }

    @Test
    void updateReturnsUpdatedSector() throws Exception {
        when(sectorService.update(7, new SectorRequest("Healthcare")))
                .thenReturn(new SectorResponse(7, "Healthcare", LocalDateTime.now()));

        mockMvc.perform(put("/api/sectors/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sectorName\":\"Healthcare\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sectorName").value("Healthcare"));
    }

    @Test
    void deleteReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/sectors/7"))
                .andExpect(status().isNoContent());

        verify(sectorService).delete(7);
    }

    @Test
    void createRejectsBlankSectorName() throws Exception {
        mockMvc.perform(post("/api/sectors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sectorName\":\" \"}"))
                .andExpect(status().isBadRequest());
    }
}
