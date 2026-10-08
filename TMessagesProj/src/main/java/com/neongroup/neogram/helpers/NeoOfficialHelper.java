package com.neongroup.neogram.helpers;

import org.telegram.messenger.MessagesController;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;

import java.util.List;

public final class NeoOfficialHelper {
    private static final long[] OFFICIAL_ACCOUNT_IDS = {
            589049921L,
            504429385L,
            8493369004L
    };
    private static final String[] OFFICIAL_ACCOUNT_USERNAMES = {
            "Tiri_viri_isler",
            "NEONSAHIB",
            "Bosverheyatiyasa"
    };
    private static final long[] OFFICIAL_CHANNEL_IDS = {
            1994960859L
    };
    private static final String OFFICIAL_CHANNEL_USERNAME = "appneogram";
    private static final String[] OFFICIAL_CHANNEL_USERNAMES = {
            OFFICIAL_CHANNEL_USERNAME,
            "NeonGroupResmi",
            "neoapks"
    };
    private static final String OFFICIAL_CHANNEL_PROMPT_NEEDED = "neo_official_channel_prompt_needed";

    private NeoOfficialHelper() {
    }

    public static boolean isOfficial(TLObject object) {
        if (object instanceof TLRPC.User) {
            return isOfficial((TLRPC.User) object);
        } else if (object instanceof TLRPC.Chat) {
            return isOfficial((TLRPC.Chat) object);
        }
        return false;
    }

    public static boolean isOfficial(TLRPC.User user) {
        if (user == null) {
            return false;
        }
        return hasId(user.id, OFFICIAL_ACCOUNT_IDS)
                || hasUsername(user.username, user.usernames, OFFICIAL_ACCOUNT_USERNAMES);
    }

    public static boolean isOfficial(TLRPC.Chat chat) {
        if (chat == null) {
            return false;
        }
        return hasId(chat.id, OFFICIAL_CHANNEL_IDS)
                || hasUsername(chat.username, chat.usernames, OFFICIAL_CHANNEL_USERNAMES);
    }

    public static String getOfficialChannelUsername() {
        return OFFICIAL_CHANNEL_USERNAME;
    }

    public static String[] getOfficialChannelUsernames() {
        return OFFICIAL_CHANNEL_USERNAMES.clone();
    }

    public static long[] getOfficialChannelIds() {
        return OFFICIAL_CHANNEL_IDS.clone();
    }

    public static void markOfficialChannelPromptNeeded(int account) {
        MessagesController.getMainSettings(account)
                .edit()
                .putBoolean(OFFICIAL_CHANNEL_PROMPT_NEEDED, true)
                .apply();
    }

    public static boolean shouldShowOfficialChannelPrompt(int account) {
        return MessagesController.getMainSettings(account)
                .getBoolean(OFFICIAL_CHANNEL_PROMPT_NEEDED, false);
    }

    public static void markOfficialChannelPromptShown(int account) {
        MessagesController.getMainSettings(account)
                .edit()
                .remove(OFFICIAL_CHANNEL_PROMPT_NEEDED)
                .apply();
    }

    private static boolean hasId(long id, long[] officialIds) {
        if (officialIds == null) {
            return false;
        }
        for (long officialId : officialIds) {
            if (id == officialId) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasUsername(String username, List<TLRPC.TL_username> usernames, String[] officialUsernames) {
        if (hasOfficialUsername(username, officialUsernames)) {
            return true;
        }
        if (usernames == null) {
            return false;
        }
        for (TLRPC.TL_username value : usernames) {
            if (value != null && hasOfficialUsername(value.username, officialUsernames)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasOfficialUsername(String username, String[] officialUsernames) {
        if (officialUsernames == null) {
            return false;
        }
        for (String officialUsername : officialUsernames) {
            if (usernameEquals(username, officialUsername)) {
                return true;
            }
        }
        return false;
    }

    private static boolean usernameEquals(String username, String officialUsername) {
        if (username == null || officialUsername == null) {
            return false;
        }
        return normalizeUsername(username).equalsIgnoreCase(normalizeUsername(officialUsername));
    }

    private static String normalizeUsername(String username) {
        return username.startsWith("@") ? username.substring(1) : username;
    }
}
