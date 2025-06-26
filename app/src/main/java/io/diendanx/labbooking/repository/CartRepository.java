package io.diendanx.labbooking.repository;

import io.diendanx.labbooking.models.Cart;

public class CartRepository extends BaseRepository<Cart> {
    public CartRepository() {
        super("cart");
    }
}

