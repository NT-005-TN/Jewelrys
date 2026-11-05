package com.jewelry.workshop.presentation.controller;

import com.jewelry.workshop.presentation.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ApiErrorsTest {
    @RestController static class FailingController {
        @GetMapping("/failure") String failure() { throw new RuntimeException("db-password=secret"); }
        @GetMapping("/conflict") String conflict() { throw new org.springframework.dao.DataIntegrityViolationException("private SQL"); }
        @GetMapping("/missing") String missing() { throw new jakarta.persistence.EntityNotFoundException("missing"); }
    }
    @Test void internalFailuresDoNotExposeMessages() throws Exception {
        var mvc = MockMvcBuilders.standaloneSetup(new FailingController()).setControllerAdvice(new GlobalExceptionHandler()).build();
        mvc.perform(get("/failure")).andExpect(status().isInternalServerError())
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("secret"))));
        mvc.perform(get("/conflict")).andExpect(status().isConflict())
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("private SQL"))));
        mvc.perform(get("/missing")).andExpect(status().isNotFound());
    }
}
