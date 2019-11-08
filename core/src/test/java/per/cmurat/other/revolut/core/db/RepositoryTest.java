package per.cmurat.other.revolut.core.db;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import per.cmurat.other.revolut.core.accounting.model.AssetAccount;
import per.cmurat.other.revolut.core.accounting.model.AssetAccountRepository;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RepositoryTest {
    private final AssetAccountRepository tested = new AssetAccountRepository();

    @Test
    void storeAndFindShouldSucceed() {
        AssetAccount assetAccount = new AssetAccount();

        assertNull(assetAccount.getId());
        tested.store(assetAccount);
        assertNotNull(assetAccount.getId());

        assertEquals(assetAccount, tested.findById(assetAccount.getId()));
    }

    @Test
    void storeShouldFailForNullValue() {
        assertThrows(NullPointerException.class, () -> tested.store(null));
    }

    @Test
    void storeShouldAssignIncrementalIds() {
        AssetAccount assetAccount1 = new AssetAccount();
        AssetAccount assetAccount2 = new AssetAccount();
        AssetAccount assetAccount3 = new AssetAccount();

        tested.store(assetAccount1);
        tested.store(assetAccount2);
        tested.store(assetAccount3);

        assertTrue(0 < assetAccount1.getId());
        assertTrue(assetAccount1.getId() < assetAccount2.getId());
        assertTrue(assetAccount2.getId() < assetAccount3.getId());
    }
}
