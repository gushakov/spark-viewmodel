package ch.unil.sparkvm.app.vm;

import ch.unil.sparkvm.app.services.SomeService;

import javax.inject.Inject;

/**
 * @author gushakov
 */
public class MainVm {

    @Inject
    private transient SomeService someService;

    private String foo;


    public MainVm() {
        this.foo = "bar";
    }

    public void changeFoo() {
        foo = someService.transform(foo);
    }

}
