package com.cryptoArb.aspect;

import com.cryptoArb.domain_spring.CurrencyPair;
import com.cryptoArb.service.PriceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test to verify AOP Logging.
 *
 * We use @SpringBootTest to load the full application context, ensuring that
 * Spring AOP proxies are created for our services.
 *
 * We use OutputCaptureExtension to capture console output (System.out/err)
 * so we can assert that the logs are actually written.
 */
@SpringBootTest
@ExtendWith(OutputCaptureExtension.class)
class LoggingAspectTest {

    @Autowired
    private PriceService priceService;

    // We mock the repository to isolate the test from the database.
    // We only care that the service method *runs*, not what data it returns.
    @MockitoBean
    private com.cryptoArb.repository.PriceTickRepository priceTickRepository;

    @Test
    @DisplayName("Should log execution time when a service method is called")
    void shouldLogServiceExecutionTime(CapturedOutput output) {
        // Given
        CurrencyPair pair = new CurrencyPair("BTC", "USD");

        // When
        // We call the service method.
        // If AOP is working, the LoggingAspect (which doesn't exist yet) should intercept this call.
        priceService.getConsolidatedPriceForPair(pair);

        // Then
        // We expect to see a log message indicating the execution time.
        assertThat(output.getOut()).contains("Execution time for");
    }
}