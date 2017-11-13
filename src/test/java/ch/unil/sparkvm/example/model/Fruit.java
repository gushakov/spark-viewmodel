package ch.unil.sparkvm.example.model;

/**
 * @author gushakov
 */
public class Fruit {
    private int id;
    private String name;

    public Fruit(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }
}
