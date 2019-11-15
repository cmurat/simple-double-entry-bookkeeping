package per.cmurat.other.revolut.core;

import com.google.inject.Guice;
import com.google.inject.Injector;
import per.cmurat.other.revolut.core.rest.RestMapper;

public class App {
    public static void main(String[] args) {
        new App().start();
    }

    private Injector injector;

    public App() {
        this.injector = Guice.createInjector(new SimpleModule());
    }

    public void start() {
        this.injector.getInstance(RestMapper.class).createMappings();
    }

    public void stop() {
        this.injector.getInstance(RestMapper.class).stopServer();
    }
}
