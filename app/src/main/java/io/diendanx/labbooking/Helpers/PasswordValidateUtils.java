package io.diendanx.labbooking.Helpers;

public class PasswordValidateUtils {
    public static boolean isValidPassword(String password) {
        return password.length() >= 6 && password.length() <= 20
                && password.matches(".*[a-z].*")
                && password.matches(".*[A-Z].*")
                && password.matches(".*[0-9].*");
    }
}
