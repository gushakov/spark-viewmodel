package ch.unil.sparkvm.example.services;

import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author gushakov
 */
@Singleton
public class DefaultSomeService implements SomeService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultSomeService.class);

    public DefaultSomeService() {
        logger.info("New instance of the service: {}", this.getClass().getSimpleName());
    }

    @Override
    public String transform(String value) {
        return value.toUpperCase();
    }
}
