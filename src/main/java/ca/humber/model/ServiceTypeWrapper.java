package ca.humber.model;

public class ServiceTypeWrapper {
    private final int id;
    private final String name;

    public ServiceTypeWrapper(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    // Display name in ComboBox
    @Override
    public String toString() {
        return name;
    }
}
