package ch.unil.sparkvm.example.config;

import ch.unil.sparkvm.example.services.DefaultSomeService;
import ch.unil.sparkvm.example.services.SomeService;
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
