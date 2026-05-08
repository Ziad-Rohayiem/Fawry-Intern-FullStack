import java.time.LocalDate;
import java.util.Set;

public class Main {

    public static void main(String[] args) {

        // pre-registers Egyptian city zones in its constructor
        PhotoManager manager = new PhotoManager();

        manager.uploadPhoto(new Photo("1", "pyramids.jpg", LocalDate.of(2023, 3, 10),
                "Giza", Set.of("pyramids", "history", "egypt", "tourism")));

        manager.uploadPhoto(new Photo("2", "nile.jpg", LocalDate.of(2023, 4, 22),
                "Cairo", Set.of("nile", "river", "sunset", "egypt")));

        manager.uploadPhoto(new Photo("3", "mosque.jpg", LocalDate.of(2023, 4, 22),
                "Cairo", Set.of("mosque", "islamic", "architecture", "egypt")));

        manager.uploadPhoto(new Photo("4", "museum.jpg", LocalDate.of(2023, 5, 15),
                "Cairo", Set.of("museum", "antiquities", "egypt", "pharaohs")));

        // Upload photos with real GPS coordinates
        // These are tagged with specific landmarks, NOT "Cairo", but their GPS
        // coordinates fall inside Cairo's registered GeoZone (radius 25 km).
        // searchByLocation("Cairo") will still find them via geo-zone matching.

        manager.uploadPhoto(new Photo("5", "tahrir.jpg", LocalDate.of(2023, 6, 1),
                new GeoLocation("Tahrir Square", 30.0440, 31.2360),  // Cairo center
                Set.of("square", "landmark", "egypt")));

        manager.uploadPhoto(new Photo("6", "cairo_tower.jpg", LocalDate.of(2023, 6, 3),
                new GeoLocation("Cairo Tower", 30.0459, 31.2243),    // Zamalek island
                Set.of("tower", "landmark", "egypt", "tourism")));

        // One  example in Alexandria, should NOT appear in Cairo zone search
        manager.uploadPhoto(new Photo("7", "library.jpg", LocalDate.of(2023, 7, 10),
                new GeoLocation("Bibliotheca Alexandrina", 31.2089, 29.9089),
                Set.of("library", "architecture", "egypt", "knowledge")));

        // Required searches
        printSection("Photos with tag 'egypt'");
        manager.searchByTag("egypt").forEach(System.out::println);

        printSection("Photos taken on 2023-04-22");
        manager.searchByDate(LocalDate.of(2023, 4, 22)).forEach(System.out::println);

        printSection("Photos taken in Cairo (string match)");
        manager.searchByLocation("Cairo").forEach(System.out::println);

        printSection("Photos with tags [egypt, museum]");
        manager.searchByMultipleTags(Set.of("egypt", "museum")).forEach(System.out::println);

        printSection("Date range: 2023-04-01 to 2023-05-31 (TreeMap range query)");
        ((PhotoManager) manager)
                .searchByDateRange(LocalDate.of(2023, 4, 1), LocalDate.of(2023, 5, 31))
                .forEach(System.out::println);

        printSection("Cairo GEO-ZONE search — finds GPS photos inside Cairo zone\n"
                   + "  (Tahrir Square & Cairo Tower match; Bibliotheca does NOT)");
        manager.searchByLocation("Cairo").forEach(System.out::println);

        printSection("Alexandria GEO-ZONE search — finds Bibliotheca by coordinates");
        manager.searchByLocation("Alexandria").forEach(System.out::println);

        printSection("Multi-tag: [egypt, landmark, tourism]");
        manager.searchByMultipleTags(Set.of("egypt", "landmark", "tourism")).forEach(System.out::println);

        // Deletion
        printSection("After deleting photo '2' (nile.jpg) — tag 'nile' and 2023-04-22 results shrink");
        manager.deletePhoto("2");
        System.out.println("searchByTag('nile'):          " + manager.searchByTag("nile"));
        System.out.println("searchByDate(2023-04-22):     " + manager.searchByDate(LocalDate.of(2023, 4, 22)));
        System.out.println("searchByLocation('Cairo'):    " + manager.searchByLocation("Cairo"));
    }

    private static void printSection(String title) {
        System.out.println("\n╔══════════════════════════════════════════════════════");
        System.out.println("║ " + title);
        System.out.println("╚══════════════════════════════════════════════════════");
    }
}
