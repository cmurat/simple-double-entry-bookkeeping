package per.cmurat.other.revolut;

import com.google.inject.AbstractModule;
import per.cmurat.other.revolut.accounting.model.AssetAccountRepository;
import per.cmurat.other.revolut.accounting.model.TransactionRepository;
import per.cmurat.other.revolut.accounting.service.AccountingService;
import per.cmurat.other.revolut.rest.RestMapper;

public class SimpleModule extends AbstractModule {
    @Override
    protected void configure() {
    }
}
