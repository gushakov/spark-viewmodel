package ch.unil.sparkvm.example.vm;

/**
 * @author gushakov
 */
public class NavigationVm {

    private boolean homeActive = true;

    private boolean aboutActive = false;

    private boolean contactActive = false;

    public void onNavigate(String link) {
        homeActive = link.equals("home");
        aboutActive = link.equals("about");
        contactActive = link.equals("contact");
    }

}
