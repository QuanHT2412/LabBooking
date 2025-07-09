package io.diendanx.labbooking.repository;

import io.diendanx.labbooking.models.OrderItem;

public class OrdersItemRepository extends BaseRepository<OrderItem> {

    public OrdersItemRepository() {
        super("order_item");
    }

}
