public record GeoZone(String cityName, double lat, double lng, double radiusKm) {

    private static final double EARTH_RADIUS_KM = 6371.0;

    // Returns true if the given GeoLocation falls within this zone's radius.
    public boolean contains(GeoLocation location) {
        double distance = haversineDistance(
                this.lat, this.lng,
                location.latitude(), location.longitude()
        );
        return distance <= radiusKm;
    }

    /**
     * Haversine formula (knew about it from Mostafa Medhat) - computes the great-circle distance in km
     * between two points on Earth given their lat/lng in decimal degrees.
     *
     * Formula:
     *   a = sin²(Δlat/2) + cos(lat1)·cos(lat2)·sin²(Δlng/2)
     *   c = 2·atan2(√a, √(1−a))
     *   d = R·c
     */
    private double haversineDistance(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    @Override
    public String toString() {
        return String.format("GeoZone{city='%s', center=(%.4f, %.4f), radius=%.1fkm}",
                cityName, lat, lng, radiusKm);
    }
}
