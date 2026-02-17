package guest.controller;

import guest.service.GuestBookingService;
import common.model.Room;
import common.service.FavoritesService;
import common.model.User;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Thin controller that mediates between UI and GuestBookingService.
 * Keeps UI free from direct service/DAO calls.
 */
public class GuestController {
    private final GuestBookingService service;

    public GuestController(GuestBookingService service) {
        this.service = service;
    }

    /**
     * Fetch rooms according to supplied flags. This method delegates to the
     * service layer and FavoritesService where appropriate.
     */
    public List<Room> fetchRooms(boolean showingFavorites, boolean onlyFavoritesFilter,
                                 boolean onlyAvailable, String destinationQuery, User currentUser) {
        List<Room> base;
        if (showingFavorites && currentUser != null) {
            var favIds = FavoritesService.getFavorites(currentUser);
            base = service.getAllRooms().stream()
                    .filter(r -> favIds.contains(r.getId()))
                    .collect(Collectors.toList());
        } else {
            base = service.getAllRooms();
        }

        if (onlyFavoritesFilter && currentUser != null) {
            var favIds = FavoritesService.getFavorites(currentUser);
            base = base.stream().filter(r -> favIds.contains(r.getId())).collect(Collectors.toList());
        }

        String q = destinationQuery == null ? "" : destinationQuery.trim().toLowerCase();
        if (!q.isEmpty()) {
            base = base.stream()
                    .filter(r -> (r.getHotelName() != null && r.getHotelName().toLowerCase().contains(q))
                            || (r.getLocation() != null && r.getLocation().toLowerCase().contains(q)))
                    .collect(Collectors.toList());
        }

        if (onlyAvailable) {
            base = base.stream().filter(Room::isAvailable).collect(Collectors.toList());
        }

        return base;
    }
}
