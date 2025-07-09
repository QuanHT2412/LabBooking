package io.diendanx.labbooking.repository;

import io.diendanx.labbooking.models.Order;

public class OrderRepository extends BaseRepository<Order> {

    public OrderRepository() {
        super("order");
    }

}
