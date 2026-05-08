public record NamedLocation(String name) implements Location {

    public NamedLocation {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Location name cannot be blank");
    }

    // Records auto-generate name(), must explicitly implement getName() for Location interface
    @Override
    public String getName() { return name; }

    @Override
    public String toString() {
        return name;
    }
}
