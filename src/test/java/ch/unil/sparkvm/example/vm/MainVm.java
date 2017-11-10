package ch.unil.sparkvm.example.vm;

import ch.unil.sparkvm.example.model.Fruit;
import ch.unil.sparkvm.example.services.SomeService;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

/**
 * @author gushakov
 */
public class MainVm {

    @Inject
    private transient SomeService someService;

    private String foo;

    private List<Fruit> fruit;

    public MainVm() {
        this.foo = "bar";
        fruit = Arrays.asList(new Fruit(1, "apple"), new Fruit(2, "orange"));
    }

    public void changeFoo() {
        foo = someService.transform(foo);
    }

}
