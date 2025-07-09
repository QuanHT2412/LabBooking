package io.diendanx.labbooking.repository;

import io.diendanx.labbooking.models.OrderItem;

public class OrderItemRepository extends BaseRepository<OrderItem> {

    public OrderItemRepository() {
        super("order_item");
    }

}
