package com.neongroup.neogram;

import org.lsposed.lsparanoid.Obfuscate;
import org.telegram.messenger.BuildConfig;

import com.neongroup.neogram.helpers.UserHelper;

@Obfuscate
public class Extra {

    public static int APP_ID = BuildConfig.API_ID;
    public static String APP_HASH = BuildConfig.API_HASH;
    public static String TWPIC_BOT_USERNAME = BuildConfig.TWPIC_BOT_USERNAME;
    public static String SENTRY_DSN = BuildConfig.SENTRY_DSN;
    public static String HELPER_API_BASE_URL = BuildConfig.HELPER_API_BASE_URL;

    public static boolean FORCE_ANALYTICS = "play".equals(BuildConfig.BUILD_TYPE);

    private static final UserHelper.BotInfo HELPER_BOT = new UserHelper.BotInfo() {
        @Override
        public long getId() {
            return BuildConfig.HELPER_BOT_ID;
        }

        @Override
        public String getUsername() {
            return BuildConfig.HELPER_BOT_USERNAME;
        }
    };

    public static UserHelper.BotInfo getHelperBot() {
        if (BuildConfig.HELPER_BOT_ID == 0 || BuildConfig.HELPER_BOT_USERNAME.isEmpty()) {
            return null;
        }
        return HELPER_BOT;
    }

    public static boolean isDirectApp() {
        return "release".equals(BuildConfig.BUILD_TYPE) || "debug".equals(BuildConfig.BUILD_TYPE);
    }

    public static boolean isTrustedBot(long id) {
        return BuildConfig.HELPER_BOT_ID != 0 && id == BuildConfig.HELPER_BOT_ID;
    }

    public static String getHelperApiBaseUrl() {
        return HELPER_API_BASE_URL == null ? "" : HELPER_API_BASE_URL.trim();
    }
}
