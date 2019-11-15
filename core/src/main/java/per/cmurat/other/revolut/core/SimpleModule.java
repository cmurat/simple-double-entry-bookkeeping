package per.cmurat.other.revolut.core;

import com.google.inject.AbstractModule;
import per.cmurat.other.revolut.core.accounting.model.AssetAccountRepository;
import per.cmurat.other.revolut.core.accounting.model.TransactionRepository;
import per.cmurat.other.revolut.core.accounting.service.AccountingService;
import per.cmurat.other.revolut.core.rest.RestMapper;

public class SimpleModule extends AbstractModule {
    @Override
    protected void configure() {
    }
}
