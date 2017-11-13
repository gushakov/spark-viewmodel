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

    public void addFruit(String name){
        final int id = fruit.stream().map(Fruit::getId).max(Integer::compareTo).get();
        fruit.add(new Fruit(id+1, name));
    }

}
