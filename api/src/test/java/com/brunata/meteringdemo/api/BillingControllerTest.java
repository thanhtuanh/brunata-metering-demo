package com.brunata.meteringdemo.api;

import com.brunata.meteringdemo.common.RestExceptionHandler;
import com.brunata.meteringdemo.common.ValidationException;
import com.brunata.meteringdemo.domain.Invoice;
import com.brunata.meteringdemo.services.BillingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class BillingControllerTest {

    private MockMvc mvc;

    @Mock
    private BillingService billingService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        var controller = new BillingController(billingService);
        this.mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new RestExceptionHandler())
                .build();
    }

    @Test
    void returns_invoice_on_success() throws Exception {
        var inv = new Invoice();
        inv.setAmount(new BigDecimal("12.34"));
        inv.setConsumption(new BigDecimal("56.000000"));
        inv.setPeriodFrom(LocalDate.parse("2025-09-01"));
        inv.setPeriodTo(LocalDate.parse("2025-09-30"));

        when(billingService.run(
                org.mockito.ArgumentMatchers.any(UUID.class),
                org.mockito.ArgumentMatchers.any(LocalDate.class),
                org.mockito.ArgumentMatchers.any(LocalDate.class)
        )).thenReturn(inv);

        mvc.perform(post("/api/billing/run")
                        .param("contractId", UUID.randomUUID().toString())
                        .param("from", "2025-09-01")
                        .param("to", "2025-09-30")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.amount", is(12.34)))
                .andExpect(jsonPath("$.consumption", is(56.0)));
    }

    @Test
    void maps_validation_exception_to_api_error() throws Exception {
        when(billingService.run(
                org.mockito.ArgumentMatchers.any(UUID.class),
                org.mockito.ArgumentMatchers.any(LocalDate.class),
                org.mockito.ArgumentMatchers.any(LocalDate.class)
        ))
                .thenThrow(new ValidationException("No readings in period"));

        mvc.perform(post("/api/billing/run")
                        .param("contractId", UUID.randomUUID().toString())
                        .param("from", "2025-09-01")
                        .param("to", "2025-09-30")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.details[0].message", containsString("No readings")));
    }
}
