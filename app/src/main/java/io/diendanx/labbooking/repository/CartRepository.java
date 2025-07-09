package io.diendanx.labbooking.repository;

import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import io.diendanx.labbooking.models.Cart;

public class CartRepository extends BaseRepository<Cart> {
    public CartRepository() {
        super("cart");
    }

    public void countCartItems(String userId, ListCallback<Cart> callback) {
        ref.whereEqualTo("userId", userId)
           .get()
           .addOnCompleteListener(task -> {
               if (task.isSuccessful() && task.getResult() != null) {
                   List<Cart> items = new ArrayList<>();
                   for (QueryDocumentSnapshot document : task.getResult()) {
                       Cart item = document.toObject(Cart.class);
                       items.add(item);
                   }
                   callback.onSuccess(items);
               } else {
                     callback.onFailure(task.getException() != null ? task.getException() : new Exception("Unknown error"));
               }
           });

    }
}

