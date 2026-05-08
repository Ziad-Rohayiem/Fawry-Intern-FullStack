# Photo Manager Project

## Overview
This project is an in-memory Photo Management System that allows users to upload, index, delete, and search for photos efficiently. The system is designed to handle metadata such as tags, dates, and locations (both specific geographical coordinates and generalized string locations).

## Design Assumptions

1. **Immutability of Photos**: Once a `Photo` object is created and uploaded to the manager, its metadata (ID, filename, date, location, tags) does not change. If an update is required, the user must delete the existing photo and upload a new one. This ensures index consistency.
2. **ID-based Indexing**: Index maps (`tagIndex`, `dateIndex`, `locationIndex`) store Photo IDs instead of whole `Photo` objects.
   - This avoids memory duplication.
   - Updates (like deletes) do not need to seek out deep object references.
   - It facilitates quick boolean intersection (e.g., when searching by multiple tags), which known as `Inverted Index`.
3. **Geo-Location and Zones**: A `Location` can be either a textual `NamedLocation` or a coordinate-based `GeoLocation`.
   - The application inherently supports checking if a `GeoLocation` falls within a specific `GeoZone` using the Haversine formula for spherical distance calculation.
   - A 1-to-1 mapping is assumed between a city's textual name and its standard `GeoZone`. For example, all searches for "Cairo" will also fetch photos with exact coordinates inside the Cairo zone (e.g., "Tahrir Square").

## Data Structures and Complexity Analysis

### 1. Primary Store: `HashMap<String, Photo> primaryStore`
- **Purpose**: Acts as the single source of truth storing the actual `Photo` objects, mapped by their unique String ID.
- **Why HashMap**: Fast lookups during ID resolution.
- **Time Complexity**:
  - Insert / Delete: **O(1)** average case.
  - Lookup (by ID): **O(1)** average case.

### 2. Tag Index: `HashMap<Tag, Set<String>> tagIndex`
- **Purpose**: Maps a specific `Tag` (normalized) to a Set of `Photo IDs` that contain this tag. 
- **Why HashMap & HashSet**: Allows O(1) retrieval of the set of photos for a given tag. Using a Set for the IDs prevents duplicates and allows fast O(1) removal when a photo is deleted.
- **Time Complexity**:
  - Insert / Delete a Tag mapping: **O(1)** average.
  - Single Tag Search: **O(1)** lookup to get the set.
  - Multiple Tag Search (Intersection): **O(N + M + ...)** where N, M are the sizes of the result sets for the given tags. We optimize this by sorting the query tags from rarest to most common, limiting the intersection workload drastically.

### 3. Date Index: `TreeMap<LocalDate, List<String>> dateIndex`
- **Purpose**: Maps a specific `LocalDate` to a List of `Photo IDs` taken on that exact date.
- **Why TreeMap**: Unlike a HashMap, a `TreeMap` keeps the keys (dates) sorted. This is required to support efficient **range queries** (e.g., find all photos between Date X and Date Y).
- **Time Complexity**:
  - Insert / Delete / Exact Date Search: **O(log D)** where D is the number of distinct dates.
  - Date Range Query: **O(log D + K)** where K is the number of dates in the matched range (using `.subMap()`).

### 4. Location Index: `HashMap<String, List<String>> locationIndex`
- **Purpose**: Maps a normalized location name (e.g., "cairo") to a list of `Photo IDs`.
- **Why HashMap**: Fast retrieval for exact string-based location searches.
- **Time Complexity**:
  - Insert / Delete / Lookup: **O(1)** average case.
  - **Note on GeoZone Search**: Searching by location also iterates over the `primaryStore` to check geographic containment via `GeoLocation`. This fallback operation entails an **O(P)** complexity where P is the total number of photos.

### 5. Geo Zones: `List<GeoZone> geoZones`
- **Purpose**: Stores the registered geographical zones (cities with lat, lng, and radius).
- **Why List**: The number of pre-registered city zones is generally small and usually searched linearly when matching a queried zone name.
- **Time Complexity**:
  - Zone Retrieval (by name): **O(Z)** where Z is the number of registered zones.

## Near-Term Future Work

Currently, geographical zone matching relies on a predefined list of statically registered `GeoZone` objects mapping a city name to its coordinates and a radius. In the near future, this functionality will be enhanced by integrating an **external Geocoding/Reverse-Geocoding API**. 
This upcoming API integration will allow:
- **Dynamic location matching**: Eliminating the need to manually hardcode `GeoZones` with estimated radiuses.
- **Reverse Geocoding**: Resolving arbitrary lat/lng GPS coordinates directly into real-world city names dynamically during the indexing or searching phases.

## Compilation and Execution

To compile the project from the root directory:
```bash
javac -d bin src/*.java
```

To run the main application:
```bash
java -cp bin Main
```
