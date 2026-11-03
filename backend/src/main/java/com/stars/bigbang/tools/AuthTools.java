package com.stars.bigbang.tools;

import java.util.Locale;

public final class AuthTools {

    public static String normalizeEmail(String email){
        return email.toLowerCase(Locale.ROOT).trim();
    }
}
