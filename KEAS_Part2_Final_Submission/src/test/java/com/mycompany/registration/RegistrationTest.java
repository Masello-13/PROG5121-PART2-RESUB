package com.mycompany.registration;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class RegistrationTest {
    @Test void partOneValidationRules() {
        Login login = new Login();
        assertTrue(login.checkUserName("k_1"));
        assertFalse(login.checkUserName("long_name"));
        assertTrue(login.checkPasswordComplexity("Abcdef1!"));
        assertFalse(login.checkPasswordComplexity("abcdefg1"));
        assertTrue(login.checkCellPhoneNumber("+27718693002"));
        assertFalse(login.checkCellPhoneNumber("0718693002"));
    }

    @Test void registeredCredentialsAreRequiredToLogIn() {
        Login login = new Login();
        assertEquals("User registered successfully.",
            login.registerUser("k_1", "Abcdef1!", "A", "B", "+27718693002"));
        assertTrue(login.loginUser("k_1", "Abcdef1!"));
        assertFalse(login.loginUser("k_1", "incorrect"));
    }

    @Test void loginBeforeRegistrationFails() {
        assertFalse(new Login().loginUser("k_1", "Abcdef1!"));
    }
}
