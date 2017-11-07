package ch.unil.sparkvm.app.config;

import ch.unil.sparkvm.app.services.DefaultSomeService;
import ch.unil.sparkvm.app.services.SomeService;
import com.google.inject.AbstractModule;

/**
 * @author gushakov
 */
public class ModuleConfig extends AbstractModule {
    @Override
    protected void configure() {
        bind(SomeService.class).to(DefaultSomeService.class);
    }
}
