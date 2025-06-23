package io.diendanx.labbooking.Helpers;

public class ValidatorsUtils {
    public static boolean isValidPassword(String password) {
        return password.length() >= 6 && password.length() <= 20
                && password.matches(".*[a-z].*")
                && password.matches(".*[A-Z].*")
                && password.matches(".*[0-9].*");
    }

    public static boolean isValidEmail(String email) {
        return email != null && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
