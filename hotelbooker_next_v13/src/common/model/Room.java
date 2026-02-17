package common.model;

public class Room {
    private String id;
    private String hotelName;
    private String roomType; // e.g. Deluxe Queen, Standard Twin
    private String location;
    private double pricePerNight;
    private double rating;
    private int reviewCount;
    private String[] amenities;
    private int capacity;
    /**
     * How many identical rooms/units exist for this listing.
     * Example: a hotel has 5 "Deluxe Queen" rooms.
     */
    private int units;
    /**
     * Admin-controlled flag (listed/enabled). This is NOT date-availability.
     */
    private boolean available;
    private String imagePath; // relative path under project root (e.g. assets/images/grand_plaza.jpg)

    public Room(String id, String hotelName, String roomType, String location, double pricePerNight,
                double rating, int reviewCount, String[] amenities, int capacity, int units,
                boolean available, String imagePath) {
        this.id = id;
        this.hotelName = hotelName;
        this.roomType = (roomType == null || roomType.isBlank()) ? "Standard Room" : roomType;
        this.location = location;
        this.pricePerNight = pricePerNight;
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.amenities = amenities;
        this.capacity = capacity;
        this.units = Math.max(1, units);
        this.available = available;
        this.imagePath = imagePath;
    }

    // Backward-compatible constructor (v11 schema with no units column)
    public Room(String id, String hotelName, String roomType, String location, double pricePerNight,
                double rating, int reviewCount, String[] amenities, int capacity, boolean available,
                String imagePath) {
        this(id, hotelName, "Standard Room", location, pricePerNight, rating, reviewCount, amenities, capacity,
             1, available, imagePath);
    }

    // Backward-compatible constructor (older data files without image column)
    public Room(String id, String hotelName, String roomType, String location, double pricePerNight,
                double rating, int reviewCount, String[] amenities, int capacity, boolean available) {
        this(id, hotelName, "Standard Room", location, pricePerNight, rating, reviewCount, amenities, capacity, 1, available,
             "assets/images/city_center.jpg");
    }

    // Getters
    public String getId() { return id; }
    public String getHotelName() { return hotelName; }
    public String getRoomType() { return roomType; }
    public String getLocation() { return location; }
    public double getPricePerNight() { return pricePerNight; }
    public double getRating() { return rating; }
    public int getReviewCount() { return reviewCount; }
    public String[] getAmenities() { return amenities; }
    public int getCapacity() { return capacity; }
    public int getUnits() { return units; }
    public boolean isAvailable() { return available; }
    public String getImagePath() { return imagePath; }

    // Setters
    public void setAvailable(boolean available) { this.available = available; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public void setUnits(int units) { this.units = Math.max(1, units); }

    // Format price as string
    public String getPriceTag() {
        return common.util.CurrencyUtil.formatNightly(pricePerNight);
    }

    // Format rating as string
    public String getRatingTag() {
        return String.format("%.1f", rating);
    }

    @Override
    public String toString() {
        // Persist in a stable schema including units.
        // Schema (v13): id|hotel|roomType|location|price|rating|reviews|amenities|capacity|units|available|imagePath
        return id + "|" + hotelName + "|" + roomType + "|" + location + "|" + pricePerNight + "|" + rating + "|" +
               reviewCount + "|" + String.join(",", amenities) + "|" + capacity + "|" + units + "|" +
               available + "|" + (imagePath == null ? "" : imagePath);
    }
}
