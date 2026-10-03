package com.yomy.counting;

/**
 * Single source of truth for runtime configuration.
 *
 * Public client values are safe only when they are intentionally public
 * (for example a Supabase publishable key). Never put service-role/private
 * keys, Vonage secrets, signing keys, or webhook secrets here.
 */
public final class YomyConfig {
    private YomyConfig() {}

    public static final String APP_NAME = "YOMY";
    public static final String ENVIRONMENT = BuildConfig.YOMY_ENVIRONMENT;
    public static final String SUPABASE_URL = BuildConfig.SUPABASE_URL;
    public static final String SUPABASE_PUBLISHABLE_KEY = BuildConfig.SUPABASE_PUBLISHABLE_KEY;

    /** Optional provider configuration. Empty means provider is not configured. */
    public static final String PUSH_PROVIDER = BuildConfig.PUSH_PROVIDER;
    public static final String PUSH_APP_ID = BuildConfig.PUSH_APP_ID;

    public static boolean hasSupabase() {
        return !SUPABASE_URL.isEmpty() && !SUPABASE_PUBLISHABLE_KEY.isEmpty();
    }

    public static boolean hasPushProvider() {
        return !PUSH_PROVIDER.isEmpty() && !PUSH_APP_ID.isEmpty();
    }
}
