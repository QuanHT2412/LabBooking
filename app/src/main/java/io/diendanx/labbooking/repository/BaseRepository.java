package io.diendanx.labbooking.repository;

import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseRepository<T> {
    protected final DatabaseReference ref;
    private final Class<T> type;

    public interface ListCallback<T> {
        void onSuccess(List<T> items);
        void onFailure(Exception e);
    }

    public interface ItemCallback<T> {
        void onSuccess(T item);
        void onFailure(Exception e);
    }

    @SuppressWarnings("unchecked")
    public BaseRepository(String node) {
        this.ref = FirebaseDatabase.getInstance().getReference(node);
        this.type = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    public void getAll(final ListCallback<T> callback) {
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<T> items = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    T item = data.getValue(type);
                    if (item != null) items.add(item);
                }
                callback.onSuccess(items);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure(error.toException());
            }
        });
    }

    public void getById(String id, final ItemCallback<T> callback) {
        ref.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                T item = snapshot.getValue(type);
                if (item != null) {
                    callback.onSuccess(item);
                } else {
                    callback.onFailure(new Exception(type.getSimpleName() + " not found"));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure(error.toException());
            }
        });
    }

    public void add(String id, T item, final ItemCallback<T> callback) {
        ref.child(id).setValue(item)
            .addOnSuccessListener(aVoid -> callback.onSuccess(item))
            .addOnFailureListener(callback::onFailure);
    }

    public void update(String id, T item, final ItemCallback<T> callback) {
        ref.child(id).setValue(item)
            .addOnSuccessListener(aVoid -> callback.onSuccess(item))
            .addOnFailureListener(callback::onFailure);
    }

    public void delete(String id, final ItemCallback<T> callback) {
        ref.child(id).removeValue()
            .addOnSuccessListener(aVoid -> callback.onSuccess(null))
            .addOnFailureListener(callback::onFailure);
    }
}

