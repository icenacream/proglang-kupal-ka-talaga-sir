package guest.service;

import common.model.CartItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** In-memory cart for multi-room booking (per app session). */
public class CartService {
    private static final CartService INSTANCE = new CartService();
    private final List<CartItem> items = new ArrayList<>();

    private CartService() {}

    public static CartService get() { return INSTANCE; }

    public synchronized void add(CartItem item) {
        items.add(item);
    }

    public synchronized void remove(int index) {
        if (index >= 0 && index < items.size()) items.remove(index);
    }

    public synchronized void clear() {
        items.clear();
    }

    public synchronized List<CartItem> list() {
        return Collections.unmodifiableList(new ArrayList<>(items));
    }

    public synchronized int size() { return items.size(); }
}
