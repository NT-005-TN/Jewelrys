package com.jewelry.workshop.presentation.controller;

import com.jewelry.workshop.domain.model.dto.auth.AuthResponseDTO;
import com.jewelry.workshop.service.interfaces.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerTest {
    @Test void loginIsMappedAndDelegatesValidRequests() throws Exception {
        AuthService service = mock(AuthService.class);
        when(service.login(any())).thenReturn(AuthResponseDTO.builder().accessToken("access").build());
        var mvc = MockMvcBuilders.standaloneSetup(new AuthController(service)).build();
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test@example.com\",\"password\":\"secret\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.accessToken").value("access"));
        verify(service).login(any());
    }

    @Test void invalidLoginAndWeakResetPasswordAreRejected() throws Exception {
        AuthService service = mock(AuthService.class);
        var mvc = MockMvcBuilders.standaloneSetup(new AuthController(service)).build();
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/auth/reset-password").contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\":\"token\",\"newPassword\":\"x\"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }
}
