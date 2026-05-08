public record GeoLocation(String name, double latitude, double longitude) implements Location {

    public GeoLocation {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Location name cannot be blank");
        if (latitude < -90 || latitude > 90)
            throw new IllegalArgumentException("Latitude must be between -90 and 90, got: " + latitude);
        if (longitude < -180 || longitude > 180)
            throw new IllegalArgumentException("Longitude must be between -180 and 180, got: " + longitude);
    }

    @Override
    public String getName() { return name; }

    @Override
    public String toString() {
        return String.format("%s (%.4f, %.4f)", name, latitude, longitude);
    }
}
