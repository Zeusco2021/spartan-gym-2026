package com.spartangoldengym.common.constants;

public final class AppConstants {

    private AppConstants() {
    }

    // Roles
    public static final String ROLE_CLIENT = "client";
    public static final String ROLE_TRAINER = "trainer";
    public static final String ROLE_ADMIN = "admin";

    // Security
    public static final int BCRYPT_COST_FACTOR = 12;
    public static final int MAX_FAILED_LOGIN_ATTEMPTS = 5;
    public static final int ACCOUNT_LOCKOUT_MINUTES = 15;

    // Rate Limiting
    public static final int RATE_LIMIT_AUTHENTICATED = 1000;
    public static final int RATE_LIMIT_UNAUTHENTICATED = 100;
    public static final int RATE_LIMIT_WINDOW_SECONDS = 60;

    // Pagination
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;

    // Supported Locales
    public static final String LOCALE_EN = "en";
    public static final String LOCALE_ES = "es";
    public static final String LOCALE_FR = "fr";
    public static final String LOCALE_DE = "de";
    public static final String LOCALE_JA = "ja";
}
