package per.cmurat.other.revolut.core.rest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import per.cmurat.other.revolut.core.accounting.model.AssetAccount;
import per.cmurat.other.revolut.core.accounting.model.Transaction;
import per.cmurat.other.revolut.core.accounting.service.AccountingService;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountingControllerTest {

    @Mock
    private AccountingService accountingService;

    @InjectMocks
    private AccountingController tested;

    @Test
    void createAccountShouldSucceed() {
        final long accountId = 1L;
        final BigDecimal balance = new BigDecimal("123.124");
        final AssetAccount expected = new AssetAccount();
        expected.setId(accountId);
        expected.setBalance(balance);

        when(accountingService.createAccount(balance)).thenReturn(expected);

        final AssetAccount actual = tested.createAccount(balance);

        assertEquals(expected, actual);
    }

    @Test
    void createAccountShouldThrowForIllegalBalance() {
        final BigDecimal nullBalance = null;
        final BigDecimal negativeBalance = new BigDecimal("-123.124");

        assertThrows(IllegalArgumentException.class, () -> tested.createAccount(nullBalance));
        assertThrows(IllegalArgumentException.class, () -> tested.createAccount(negativeBalance));
    }

    @Test
    void getAccountShouldSucceed() {
        final long accountId = 1L;
        final AssetAccount expected = new AssetAccount();
        expected.setId(accountId);

        when(accountingService.getAccount(accountId)).thenReturn(expected);

        final AssetAccount actual = tested.getAccount(String.valueOf(accountId));

        assertEquals(expected, actual);
    }

    @Test
    void getAccountShouldThrowForIllegalId() {
        final String nullId = null;
        final String emptyId = "";
        final String nonLongId = "Not a long";

        assertThrows(IllegalArgumentException.class, () -> tested.getAccount(nullId));
        assertThrows(IllegalArgumentException.class, () -> tested.getAccount(emptyId));
        assertThrows(IllegalArgumentException.class, () -> tested.getAccount(nonLongId));
    }

    @Test
    void validateTransferShouldSucceed() throws Throwable {
        final long sendingAccountId = 1L;
        final long receivingAccountId = 2L;
        final BigDecimal amount = new BigDecimal("123.124");

        tested.validateTransfer(sendingAccountId, receivingAccountId, amount);

        verify(accountingService, times(1)).validate(sendingAccountId, receivingAccountId, amount);
    }

    @Test
    void validateTransferShouldThrowForNonPositiveAmount() throws Throwable {
        final BigDecimal nullAmount = null;
        final BigDecimal zeroAmount = new BigDecimal("0");
        final BigDecimal negativeAmount = new BigDecimal("-123.124");

        assertThrows(IllegalArgumentException.class, () ->
                tested.validateTransfer(1L, 2L, nullAmount));
        assertThrows(IllegalArgumentException.class, () ->
                tested.validateTransfer(1L, 2L, zeroAmount));
        assertThrows(IllegalArgumentException.class, () ->
                tested.validateTransfer(1L, 2L, negativeAmount));
    }

    @Test
    void processTransferShouldSucceed() throws Throwable {
        final long sendingAccountId = 1L;
        final long receivingAccountId = 2L;
        final BigDecimal amount = new BigDecimal("123.124");

        final Transaction expected = new Transaction();

        when(accountingService.transfer(sendingAccountId, receivingAccountId, amount)).thenReturn(expected);

        final Transaction actual = tested.processTransfer(sendingAccountId, receivingAccountId, amount);

        assertEquals(expected, actual);

        verify(accountingService, times(1)).transfer(sendingAccountId, receivingAccountId, amount);
    }

    @Test
    void processTransferShouldThrowForNonPositiveAmount() throws Throwable {
        final BigDecimal nullAmount = null;
        final BigDecimal zeroAmount = new BigDecimal("0");
        final BigDecimal negativeAmount = new BigDecimal("-123.124");

        assertThrows(IllegalArgumentException.class, () ->
                tested.processTransfer(1L, 2L, nullAmount));
        assertThrows(IllegalArgumentException.class, () ->
                tested.processTransfer(1L, 2L, zeroAmount));
        assertThrows(IllegalArgumentException.class, () ->
                tested.processTransfer(1L, 2L, negativeAmount));
    }
}
