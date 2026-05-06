// Normalized tag value (lowercase, trimmed)
public record Tag(String value) {

    // Compact canonical constructor — runs before the auto-generated one
    public Tag {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Tag value cannot be blank");
        value = value.toLowerCase().trim();
    }

    // override the auto-generated toString()
    @Override
    public String toString() {
        return value;
    }
}