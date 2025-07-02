package io.diendanx.labbooking.Helpers;

import com.google.firebase.auth.FirebaseAuth;

public class FireBaseUtil {
    public static String currentUserId() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        return auth.getCurrentUser().getUid();
    }

    public static boolean isUserLoggedIn() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        return auth.getCurrentUser() != null;
    }

    public static String getChatRoomId(String userId1, String userId2) {
        if (userId1.hashCode() < userId2.hashCode()) {
            return userId1 + "_" + userId2;
        } else {
            return userId2 + "_" + userId1;
        }

    }
}
