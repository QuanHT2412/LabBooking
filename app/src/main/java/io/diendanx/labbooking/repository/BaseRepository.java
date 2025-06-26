package io.diendanx.labbooking.repository;

import androidx.annotation.NonNull;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseRepository<T> {
    protected final CollectionReference ref;
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
        this.ref = FirebaseFirestore.getInstance().collection(node);
        this.type = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    public void getAll(final ListCallback<T> callback) {
        ref.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    List<T> items = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        T item = document.toObject(type);
                        items.add(item);
                    }
                    callback.onSuccess(items);
                } else {
                    callback.onFailure(task.getException() != null ? task.getException() : new Exception("Unknown error"));
                }
            }
        });
    }

    public void getById(String id, final ItemCallback<T> callback) {
        ref.document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                    T item = task.getResult().toObject(type);
                    callback.onSuccess(item);
                } else {
                    callback.onFailure(new Exception(type.getSimpleName() + " not found"));
                }
            }
        });
    }

    public void add(String id, T item, final ItemCallback<T> callback) {
        ref.document(id).set(item)
            .addOnSuccessListener(aVoid -> callback.onSuccess(item))
            .addOnFailureListener(callback::onFailure);
    }

    public void update(String id, T item, final ItemCallback<T> callback) {
        ref.document(id).set(item)
            .addOnSuccessListener(aVoid -> callback.onSuccess(item))
            .addOnFailureListener(callback::onFailure);
    }

    public void delete(String id, final ItemCallback<T> callback) {
        ref.document(id).delete()
            .addOnSuccessListener(aVoid -> callback.onSuccess(null))
            .addOnFailureListener(callback::onFailure);
    }
}
