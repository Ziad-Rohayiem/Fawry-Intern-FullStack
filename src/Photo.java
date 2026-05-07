import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * An immutable photo entity.
 * Immutability is intentional, changing a photo's tags or location
 * after upload would silently break all indexes in PhotoManager.
 * If metadata needs updating, delete and re-upload.
 */
public class Photo {

    private final String id;
    private final String filename;
    private final LocalDate date;
    private final Location location;
    private final Set<Tag> tags;      // unmodifiable, enforced by constructor

    public Photo(String id, String filename, LocalDate date, Location location, Set<Tag> tags) {
        // Validation
        validateCommonFields(id, filename, date);
        if (location == null) throw new IllegalArgumentException("Location cannot be null");
        if (tags == null || tags.isEmpty()) throw new IllegalArgumentException("Tags cannot be null or empty");

        this.id = id.trim();
        this.filename = filename.trim();
        this.date = date;
        this.location = location;
        this.tags = Collections.unmodifiableSet(new HashSet<>(tags)); // Defensive copy → caller cannot mutate our internal set
    }

    public Photo(String id, String filename, LocalDate date, String locationName, Set<String> rawTags) {
        validateCommonFields(id, filename, date);
        if (locationName == null || locationName.isBlank())
            throw new IllegalArgumentException("Location name cannot be blank");
        if (rawTags == null || rawTags.isEmpty())
            throw new IllegalArgumentException("Tags cannot be null or empty");

        this.id = id.trim();
        this.filename = filename.trim();
        this.date = date;
        this.location = new NamedLocation(locationName);
        this.tags = Collections.unmodifiableSet(
                rawTags.stream().map(Tag::new).collect(Collectors.toSet())
        );
    }

    public Photo(String id, String filename, LocalDate date, GeoLocation geoLocation, Set<String> rawTags) {
        validateCommonFields(id, filename, date);
        if (geoLocation == null) throw new IllegalArgumentException("Location cannot be null");
        if (rawTags == null || rawTags.isEmpty()) throw new IllegalArgumentException("Tags cannot be null or empty");

        this.id = id.trim();
        this.filename = filename.trim();
        this.date = date;
        this.location = geoLocation;
        this.tags = Collections.unmodifiableSet(
                rawTags.stream().map(Tag::new).collect(Collectors.toSet())
        );
    }

    private static void validateCommonFields(String id, String filename, LocalDate date) {
        if (id == null || id.isBlank())       throw new IllegalArgumentException("Photo ID cannot be blank");
        if (filename == null || filename.isBlank()) throw new IllegalArgumentException("Filename cannot be blank");
        if (date == null)                     throw new IllegalArgumentException("Date cannot be null");
    }

    public String    getId()       { return id; }
    public String    getFilename() { return filename; }
    public LocalDate getDate()     { return date; }
    public Location  getLocation() { return location; }
    public Set<Tag>  getTags()     { return tags; }   // already unmodifiable

    @Override
    public String toString() {
        return String.format("Photo{id='%s', file='%s', date=%s, location='%s', tags=%s}",
                id, filename, date, location.getName(), tags);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Photo other)) return false;
        return id.equals(other.id);     // identity is purely the ID
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
