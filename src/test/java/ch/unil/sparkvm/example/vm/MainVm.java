package ch.unil.sparkvm.example.vm;

import ch.unil.sparkvm.example.services.SomeService;

import javax.inject.Inject;

/**
 * @author gushakov
 */
public class MainVm {

    @Inject
    private transient SomeService someService;

    private boolean homeActive;

    private boolean aboutActive;

    private boolean contactActive;

    private String foo;

    private String message;

    public MainVm() {
        this.foo = "bar";
        resetNavigation();
        this.homeActive = true;
        this.message = "Hello World";
    }

    public void changeFoo() {
        foo = someService.transform(foo);
    }

    public void onNavigate(String link) {
        homeActive = link.equals("home");
        aboutActive = link.equals("about");
        contactActive = link.equals("contact");
    }

    private void resetNavigation() {
        homeActive = false;
        aboutActive = false;
        contactActive = false;
    }

}
