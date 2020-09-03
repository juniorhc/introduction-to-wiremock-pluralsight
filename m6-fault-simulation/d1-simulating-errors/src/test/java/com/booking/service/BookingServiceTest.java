package com.booking.service;

import com.booking.gateway.PayBuddyGateway;
import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.math.BigDecimal;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

public class BookingServiceTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule();

    private BookingService bookingService;

    @Before
    public void setUp() {
        bookingService = new BookingService(
                new PayBuddyGateway(
                        "localhost",
                        8080));
    }

    @Test
    public void shouldAddTaxOntoTheInvoice() {
        // Given
        stubFor(get(
                urlPathEqualTo("/vat"))
                .withQueryParam("amount", equalTo("100"))
                .willReturn(okJson("{\"amount\" : 20}")));

        // When
        final Invoice invoice = bookingService.generateInvoice(
                "1234");

        // Then
        assertThat(invoice.getCostOfFlight())
                .isEqualByComparingTo(new BigDecimal(100));

        assertThat(invoice.getTax())
                .isEqualByComparingTo(new BigDecimal(20));

        assertThat(invoice.getTotal())
                .isEqualByComparingTo(new BigDecimal(120));
    }

    @Test
    public void shouldAddZeroTaxOntoTheInvoiceWhenThereIsAServerError() {
        // Given
        stubFor(get(
                urlPathEqualTo("/vat"))
                .withQueryParam("amount", equalTo("100"))
                .willReturn(serverError())); //type of wiremock error, in order do prevent future errors

        // When
        final Invoice invoice = bookingService.generateInvoice(
                "1234");

        // Then
        assertThat(invoice.getCostOfFlight())
                .isEqualByComparingTo(new BigDecimal(100));

        assertThat(invoice.getTax())
                .isEqualByComparingTo(BigDecimal.ZERO);

        assertThat(invoice.getTotal())
                .isEqualByComparingTo(new BigDecimal(100));
    }

    @Test
    public void shouldAddZeroTaxOntoTheInvoiceWhenResponseIsEmpty() {
        // Given
        stubFor(get(
                urlPathEqualTo("/vat"))
                .withQueryParam("amount", equalTo("100"))
                .willReturn(aResponse().withFault(Fault.EMPTY_RESPONSE))); //type of wiremock error, in order do prevent future errors

        // When
        final Invoice invoice = bookingService.generateInvoice(
                "1234");

        // Then
        assertThat(invoice.getCostOfFlight())
                .isEqualByComparingTo(new BigDecimal(100));

        assertThat(invoice.getTax())
                .isEqualByComparingTo(BigDecimal.ZERO);

        assertThat(invoice.getTotal())
                .isEqualByComparingTo(new BigDecimal(100));
    }

    @Test
    public void shouldAddZeroTaxOntoTheInvoiceWhenResponseIsRandomData() {
        // Given
        stubFor(get(
                urlPathEqualTo("/vat"))
                .withQueryParam("amount", equalTo("100"))
                .willReturn(aResponse().withFault(Fault.RANDOM_DATA_THEN_CLOSE))); //type of wiremock error, in order do prevent future errors

        // When
        final Invoice invoice = bookingService.generateInvoice(
                "1234");

        // Then
        assertThat(invoice.getCostOfFlight())
                .isEqualByComparingTo(new BigDecimal(100));

        assertThat(invoice.getTax())
                .isEqualByComparingTo(BigDecimal.ZERO);

        assertThat(invoice.getTotal())
                .isEqualByComparingTo(new BigDecimal(100));
    }

    @Test
    public void shouldAddZeroTaxOntoTheInvoiceWhenConnectionIsReset() {
        // Given
        stubFor(get(
                urlPathEqualTo("/vat"))
                .withQueryParam("amount", equalTo("100"))
                .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER))); //type of wiremock error, in order do prevent future errors

        // When
        final Invoice invoice = bookingService.generateInvoice(
                "1234");

        // Then
        assertThat(invoice.getCostOfFlight())
                .isEqualByComparingTo(new BigDecimal(100));

        assertThat(invoice.getTax())
                .isEqualByComparingTo(BigDecimal.ZERO);

        assertThat(invoice.getTotal())
                .isEqualByComparingTo(new BigDecimal(100));
    }
}
