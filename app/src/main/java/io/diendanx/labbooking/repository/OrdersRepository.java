package io.diendanx.labbooking.repository;

import io.diendanx.labbooking.models.Order;

public class OrdersRepository extends BaseRepository<Order> {

    public OrdersRepository() {
        super("order");
    }

}
