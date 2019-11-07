package per.cmurat.other.revolut;

import com.google.inject.Guice;
import com.google.inject.Injector;
import per.cmurat.other.revolut.rest.RestMapper;

public class Main {
    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new SimpleModule());
        injector.getInstance(RestMapper.class).createMappings();
    }
}
