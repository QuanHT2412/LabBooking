package io.diendanx.labbooking.repository;

import io.diendanx.labbooking.models.User;

public class UserRepository extends BaseRepository<User> {
    public UserRepository() {
        super("users");
    }

    // Additional methods specific to UserRepository can be added here
}
