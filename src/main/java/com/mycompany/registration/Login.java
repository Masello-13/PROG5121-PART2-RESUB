package com.mycompany.registration;

/** Part 1 registration, validation, and login logic. */
public final class Login {
    private String registeredUsername;
    private String registeredPassword;
    private String firstName;
    private String lastName;
    private String registeredCellPhone;

    public Login() {
        // Registration stores the credentials when registerUser() succeeds.
    }

    /** Username must contain an underscore and have at most five characters. */
    public boolean checkUserName(String username) {
        return username != null && username.contains("_") && username.length() <= 5;
    }

    /** Password must have 8+ characters, uppercase, a digit and special character. */
    public boolean checkPasswordComplexity(String password) {
        if (password == null || password.length() < 8) return false;
        boolean uppercase = false;
        boolean digit = false;
        boolean special = false;
        for (char character : password.toCharArray()) {
            if (Character.isUpperCase(character)) uppercase = true;
            if (Character.isDigit(character)) digit = true;
            if (!Character.isLetterOrDigit(character)) special = true;
        }
        return uppercase && digit && special;
    }

    /** A South African number written with the +27 international prefix. */
    public boolean checkCellPhoneNumber(String number) {
        return number != null && number.matches("\\+27[0-9]{9}");
    }

    /** Store the registration only after all Part 1 checks pass. */
    public String registerUser(String username, String password, String firstName,
                               String lastName, String phone) {
        if (!checkUserName(username)) {
            return "Username is not correctly formatted; please ensure that your username "
                    + "contains an underscore and is no more than five characters in length.";
        }
        if (!checkPasswordComplexity(password)) {
            return "Password is not correctly formatted; please ensure that the password "
                    + "contains at least eight characters, a capital letter, a number, and a special character.";
        }
        if (!checkCellPhoneNumber(phone)) {
            return "Cell phone number incorrectly formatted or does not contain an international code.";
        }
        this.registeredUsername = username;
        this.registeredPassword = password;
        this.registeredCellPhone = phone;
        this.firstName = firstName;
        this.lastName = lastName;
        return "User registered successfully.";
    }

    public String getRegisteredCellPhone() {
        return registeredCellPhone;
    }

    public boolean loginUser(String username, String password) {
        return registeredUsername != null && registeredPassword != null
                && registeredUsername.equals(username)
                && registeredPassword.equals(password);
    }

    public String returnLoginStatus(boolean loggedIn) {
        return loggedIn
                ? "Welcome " + firstName + " " + lastName + ", it is great to see you again."
                : "Username or password incorrect, please try again.";
    }
}
