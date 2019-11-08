package per.cmurat.other.revolut.functest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import per.cmurat.other.revolut.core.App;
import per.cmurat.other.revolut.core.rest.dto.AssetAccountDto;
import per.cmurat.other.revolut.core.rest.dto.TransactionDto;
import per.cmurat.other.revolut.functest.AccountingClient.Response;

import java.io.IOException;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static per.cmurat.other.revolut.core.rest.RestMapper.STATUS_BAD_REQUEST;
import static per.cmurat.other.revolut.core.rest.RestMapper.STATUS_OK;

class TestAccounting {

    private AccountingClient client = new AccountingClient();

    private App app;

    @BeforeEach
    void startApp() {
        app = new App();
        app.start();
    }

    @AfterEach
    void stopApp() {
        app.stop();
    }

    @Test
    void createAndGetAccountShouldSucceed() throws IOException {
        final AssetAccountDto createAccountResponseDto = client.createAccount(new BigDecimal("123.124")).getParsedObject();
        final AssetAccountDto getAccountResponseDto = client.getAccount(createAccountResponseDto.getId()).getParsedObject();

        assertEquals(createAccountResponseDto.getId(), getAccountResponseDto.getId());
        assertEquals(createAccountResponseDto.getBalance(), getAccountResponseDto.getBalance());
    }

    @Test
    void createAccountShouldFailWithNegativeBalance() throws IOException {
        final Response response = client.createAccount(new BigDecimal("-123.124"));
        assertEquals(STATUS_BAD_REQUEST, response.getResponse().getStatusLine().getStatusCode());
    }

    @Test
    void getAccountShouldFailWithNonExistentId() throws IOException {
        final Response response = client.getAccount(1L);
        assertEquals(STATUS_BAD_REQUEST, response.getResponse().getStatusLine().getStatusCode());
    }

    @Test
    void validateAndProcessTransferShouldSucceed() throws IOException {
        final AssetAccountDto sendingAccountDto = client.createAccount(new BigDecimal("123.124")).getParsedObject();
        final AssetAccountDto receivingAccountDto = client.createAccount(new BigDecimal("123.124")).getParsedObject();
        final BigDecimal amount = new BigDecimal("10.1");

        final Response validateResponse = client.validateTransfer(
                sendingAccountDto.getId(),
                receivingAccountDto.getId(),
                amount);

        assertEquals(STATUS_OK, validateResponse.getResponse().getStatusLine().getStatusCode());

        final TransactionDto transactionDto = client.processTransfer(
                sendingAccountDto.getId(),
                receivingAccountDto.getId(),
                amount)
                .getParsedObject();

        assertEquals(sendingAccountDto.getId(), transactionDto.getSendingAccountId());
        assertEquals(receivingAccountDto.getId(), transactionDto.getReceivingAccountId());
        assertEquals(amount, transactionDto.getAmount());
        assertNotNull(transactionDto.getDateTime());

        final AssetAccountDto sendingAccountAfterTransfer =
                client.getAccount(transactionDto.getSendingAccountId()).getParsedObject();
        final AssetAccountDto receivingAccountAfterTransfer =
                client.getAccount(transactionDto.getReceivingAccountId()).getParsedObject();

        assertEquals(sendingAccountAfterTransfer.getBalance(), sendingAccountDto.getBalance().subtract(amount));
        assertEquals(receivingAccountAfterTransfer.getBalance(), sendingAccountDto.getBalance().add(amount));
    }

    @Test
    void validateAndProcessTransferShouldFailWithIllegalArguments() throws IOException {
        final AssetAccountDto sendingAccountDto = client.createAccount(new BigDecimal("123.124")).getParsedObject();
        final AssetAccountDto receivingAccountDto = client.createAccount(new BigDecimal("123.124")).getParsedObject();
        final BigDecimal amount = new BigDecimal("10.1");


        final long illegalAccountId = 3L;
        final Response validateTransferWithWrongAccountIdResponse = client.validateTransfer(
                illegalAccountId,
                receivingAccountDto.getId(),
                amount);

        assertEquals(STATUS_BAD_REQUEST, validateTransferWithWrongAccountIdResponse.getResponse().getStatusLine().getStatusCode());

        final Response processTransferWithWrongAccountIdResponse = client.processTransfer(
                illegalAccountId,
                receivingAccountDto.getId(),
                amount);

        assertEquals(STATUS_BAD_REQUEST, processTransferWithWrongAccountIdResponse.getResponse().getStatusLine().getStatusCode());


        final BigDecimal illegalAmount = new BigDecimal("-10.1");
        final Response validateTransferResponse = client.validateTransfer(
                sendingAccountDto.getId(),
                receivingAccountDto.getId(),
                illegalAmount);

        assertEquals(STATUS_BAD_REQUEST, validateTransferResponse.getResponse().getStatusLine().getStatusCode());

        final Response processTransferResponse = client.processTransfer(
                sendingAccountDto.getId(),
                receivingAccountDto.getId(),
                illegalAmount);

        assertEquals(STATUS_BAD_REQUEST, processTransferResponse.getResponse().getStatusLine().getStatusCode());
    }
}
