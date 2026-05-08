import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/*
 * Index architecture (all indexes store IDs, not Photo objects):
 *
 *   primaryStore   HashMap<String, Photo>             id → Photo
 *   tagIndex       HashMap<Tag, Set<String>>           tag → {photo IDs}
 *   dateIndex      TreeMap<LocalDate, List<String>>    date → [photo IDs]  ← sorted for range queries
 *   locationIndex  HashMap<String, List<String>>       name → [photo IDs]
 *   geoZones       List<GeoZone>                       registered city zones
 *
 * Why store IDs and not Photos in the indexes?
 *   - Avoids object duplication (one Photo object in memory, referenced by ID everywhere)
 *   - A single resolve step at the end (resolveIds) fetches the actual Photos
 *   - Deletes only need to remove an ID from each index, not hunt for object references
*/
public class PhotoManager {

    // Indexes

    private final Map<String, Photo>            primaryStore  = new HashMap<>();
    private final Map<Tag, Set<String>>         tagIndex      = new HashMap<>();
    private final TreeMap<LocalDate, List<String>> dateIndex  = new TreeMap<>();
    private final Map<String, List<String>>     locationIndex = new HashMap<>();
    private final List<GeoZone>                 geoZones      = new ArrayList<>();

    // Constructor

    public PhotoManager() {
        // Pre-register common Egyptian city zones.
        registerGeoZone(new GeoZone("Cairo",      30.0444,  31.2357, 25.0));
        registerGeoZone(new GeoZone("Giza",       29.9870,  31.1313, 20.0));
        registerGeoZone(new GeoZone("Alexandria", 31.2001,  29.9187, 30.0));
        registerGeoZone(new GeoZone("Luxor",      25.6872,  32.6396, 15.0));
        registerGeoZone(new GeoZone("Aswan",      24.0889,  32.8998, 15.0));
        registerGeoZone(new GeoZone("Sharm",      27.9158,  34.3300, 15.0));
    }

    public void registerGeoZone(GeoZone zone) {
        geoZones.add(zone);
    }

    public void uploadPhoto(Photo photo) {
        if (primaryStore.containsKey(photo.getId()))
            throw new IllegalArgumentException(
                    "Photo with ID '" + photo.getId() + "' already exists. Delete first to replace.");

        // 1. Add to primary store
        primaryStore.put(photo.getId(), photo);

        // 2. Update all indexes — must stay consistent with primary store
        indexByTags(photo);
        indexByDate(photo);
        indexByLocation(photo);
    }

    public void deletePhoto(String id) {
        Photo photo = primaryStore.remove(id);
        if (photo == null) return; // no error if already deleted

        // Remove from tag index
        for (Tag tag : photo.getTags()) {
            Set<String> ids = tagIndex.get(tag);
            if (ids != null) {
                ids.remove(id);
                if (ids.isEmpty()) tagIndex.remove(tag); // In case this is the last photo associated with this tag, remove the tag
            }
        }

        // Remove from date index
        List<String> dateIds = dateIndex.get(photo.getDate());
        if (dateIds != null) {
            dateIds.remove(id);
            if (dateIds.isEmpty()) dateIndex.remove(photo.getDate());
        }

        // Remove from location index
        String locKey = normalizeKey(photo.getLocation().getName());
        List<String> locIds = locationIndex.get(locKey);
        if (locIds != null) {
            locIds.remove(id);
            if (locIds.isEmpty()) locationIndex.remove(locKey);
        }
    }

    // Indexing helpers

    private void indexByTags(Photo photo) {
        for (Tag tag : photo.getTags()) {
            tagIndex.computeIfAbsent(tag, k -> new HashSet<>()).add(photo.getId());
        }
    }

    private void indexByDate(Photo photo) {
        dateIndex.computeIfAbsent(photo.getDate(), k -> new ArrayList<>()).add(photo.getId());
    }

    private void indexByLocation(Photo photo) {
        String key = normalizeKey(photo.getLocation().getName());
        locationIndex.computeIfAbsent(key, k -> new ArrayList<>()).add(photo.getId());
    }

    // Search operations

    public List<Photo> searchByTag(String tag) {
        Tag searchTag = new Tag(tag);
        Set<String> ids = tagIndex.getOrDefault(searchTag, Collections.emptySet());
        return resolveIds(ids);
    }

    public List<Photo> searchByDate(LocalDate date) {
        List<String> ids = dateIndex.getOrDefault(date, Collections.emptyList());
        return resolveIds(ids);
    }

    public List<Photo> searchByDateRange(LocalDate from, LocalDate to) {
        if (from.isAfter(to))
            throw new IllegalArgumentException("'from' date must not be after 'to' date");

        // TreeMap.subMap gives us the sorted part in O(log n)
        NavigableMap<LocalDate, List<String>> slice = dateIndex.subMap(from, true, to, true);

        return slice.values().stream()
                .flatMap(List::stream)
                .map(primaryStore::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<Photo> searchByLocation(String locationName) {
        // LinkedHashSet: no duplicates, and preserves insertion order
        Set<String> resultIds = new LinkedHashSet<>();

        // Step 1: Exact name match. Fast O(1) lookup in the location index
        String key = normalizeKey(locationName);
        List<String> directMatches = locationIndex.getOrDefault(key, Collections.emptyList());
        resultIds.addAll(directMatches);

        // Step 2: Geo-zone match
        // Find a registered zone for this city name. Then scan all photos that have GPS coordinates and check containment.
        // This catches photos tagged "Tahrir Square" or "Heliopolis" that physically fall within the Cairo zone.
        Optional<GeoZone> targetZone = geoZones.stream()
                .filter(z -> z.cityName().equalsIgnoreCase(locationName)).findFirst();

        if (targetZone.isPresent()) {
            GeoZone zone = targetZone.get();
            for (Photo photo : primaryStore.values()) {
                if (photo.getLocation() instanceof GeoLocation geoLoc) {
                    if (zone.contains(geoLoc) && !resultIds.contains(photo.getId())) {
                        resultIds.add(photo.getId());
                    }
                }
            }
        }

        return resolveIds(resultIds);
    }

    public List<Photo> searchByMultipleTags(Set<String> rawTags) {
        if (rawTags == null || rawTags.isEmpty()) return Collections.emptyList();

        // Normalize all tags first
        List<Tag> tags = rawTags.stream().map(Tag::new).collect(Collectors.toList());

        // Sort by result-set size ascending
        // Start intersection from the rarest tag.
        tags.sort(Comparator.comparingInt(t -> tagIndex.getOrDefault(t, Collections.emptySet()).size()));

        // Seed with the smallest result set
        Set<String> result = new HashSet<>(tagIndex.getOrDefault(tags.get(0), Collections.emptySet()));

        // Intersect with each subsequent set
        for (int i = 1; i < tags.size() && !result.isEmpty(); i++) {
            Set<String> nextSet = tagIndex.getOrDefault(tags.get(i), Collections.emptySet());
            result.retainAll(nextSet);
        }

        return resolveIds(result);
    }

    // Resolution - Converting a collection of photo IDs back to Photo objects.
    private List<Photo> resolveIds(Collection<String> ids) {
        return ids.stream().map(primaryStore::get).filter(Objects::nonNull).collect(Collectors.toList());
    }

    // Utilities

    // Consistent key normalization for the location index.
    private String normalizeKey(String name) {
        return name.toLowerCase().trim();
    }

    public int getTotalPhotos() {
        return primaryStore.size();
    }
}
