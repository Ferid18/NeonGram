package com.neongroup.neogram.helpers;

import android.content.SharedPreferences;
import android.text.TextUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;
import java.util.HashSet;

public final class NeoDefaultFolderHelper {
    private static final String DEFAULT_FOLDERS_CREATED = "neo_default_folders_created_v2_";
    private static final int FIRST_CUSTOM_FILTER_ID = 2;
    private static final int FILTER_COLOR_NONE = -1;
    private static final HashSet<String> creatingDefaultFolders = new HashSet<>();

    private NeoDefaultFolderHelper() {
    }

    public static void ensureDefaultFolders(int account, MessagesController messagesController) {
        if (messagesController == null || !UserConfig.getInstance(account).isClientActivated()) {
            return;
        }

        long userId = UserConfig.getInstance(account).getClientUserId();
        if (userId == 0) {
            return;
        }

        SharedPreferences settings = MessagesController.getMainSettings(account);
        String key = DEFAULT_FOLDERS_CREATED + userId;
        if (settings.getBoolean(key, false) || creatingDefaultFolders.contains(key)) {
            return;
        }

        creatingDefaultFolders.add(key);

        ArrayList<PresetFolder> missingFolders = getMissingPresetFolders(messagesController);
        if (missingFolders.isEmpty()) {
            markDefaultFoldersHandled(settings, key);
            return;
        }

        if (containsOfficialFolder(missingFolders)) {
            resolveOfficialChannelDialogs(account, messagesController, officialDialogs -> {
                addDefaultFolders(account, messagesController, missingFolders, officialDialogs);
                markDefaultFoldersHandled(settings, key);
            });
        } else {
            addDefaultFolders(account, messagesController, missingFolders, new ArrayList<>());
            markDefaultFoldersHandled(settings, key);
        }
    }

    private static void markDefaultFoldersHandled(SharedPreferences settings, String key) {
        settings.edit().putBoolean(key, true).apply();
        creatingDefaultFolders.remove(key);
    }

    private static ArrayList<PresetFolder> getMissingPresetFolders(MessagesController messagesController) {
        ArrayList<PresetFolder> missingFolders = new ArrayList<>();
        ArrayList<PresetFolder> presetFolders = getPresetFolders();
        for (int a = 0, n = presetFolders.size(); a < n; a++) {
            PresetFolder presetFolder = presetFolders.get(a);
            if (!hasPresetFolder(messagesController, presetFolder)) {
                missingFolders.add(presetFolder);
            }
        }
        return missingFolders;
    }

    private static boolean hasPresetFolder(MessagesController messagesController, PresetFolder presetFolder) {
        ArrayList<MessagesController.DialogFilter> filters = messagesController.getDialogFilters();
        for (int a = 0, n = filters.size(); a < n; a++) {
            MessagesController.DialogFilter filter = filters.get(a);
            if (filter.isDefault()) {
                continue;
            }
            if (presetFolder.matches(filter)) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsOfficialFolder(ArrayList<PresetFolder> presetFolders) {
        for (int a = 0, n = presetFolders.size(); a < n; a++) {
            if (presetFolders.get(a).officialFolder) {
                return true;
            }
        }
        return false;
    }

    private static ArrayList<PresetFolder> getPresetFolders() {
        ArrayList<PresetFolder> presetFolders = new ArrayList<>();
        presetFolders.add(new PresetFolder(LocaleController.getString(R.string.NeoFolderGeneral), MessagesController.DIALOG_FILTER_FLAG_ALL_CHATS, false));
        presetFolders.add(new PresetFolder(LocaleController.getString(R.string.NeoFolderFavorites), 0, false));
        presetFolders.add(new PresetFolder(LocaleController.getString(R.string.NeoFolderUsers), MessagesController.DIALOG_FILTER_FLAG_CONTACTS | MessagesController.DIALOG_FILTER_FLAG_NON_CONTACTS, false));
        presetFolders.add(new PresetFolder(LocaleController.getString(R.string.NeoFolderChannels), MessagesController.DIALOG_FILTER_FLAG_CHANNELS, false));
        presetFolders.add(new PresetFolder(LocaleController.getString(R.string.NeoFolderGroups), MessagesController.DIALOG_FILTER_FLAG_GROUPS, false));
        presetFolders.add(new PresetFolder(LocaleController.getString(R.string.NeoFolderBots), MessagesController.DIALOG_FILTER_FLAG_BOTS, false));
        presetFolders.add(new PresetFolder(LocaleController.getString(R.string.NeoFolderNeogram), 0, true));
        return presetFolders;
    }

    private static void addDefaultFolders(int account, MessagesController messagesController, ArrayList<PresetFolder> presetFolders, ArrayList<Long> officialDialogs) {
        int[] nextId = new int[]{nextFilterId(messagesController, FIRST_CUSTOM_FILTER_ID)};
        ArrayList<MessagesController.DialogFilter> defaultFolders = new ArrayList<>();
        for (int a = 0, n = presetFolders.size(); a < n; a++) {
            PresetFolder presetFolder = presetFolders.get(a);
            defaultFolders.add(createFilter(messagesController, nextId, presetFolder.name, presetFolder.flags, presetFolder.officialFolder ? officialDialogs : null));
        }

        for (int a = 0, n = defaultFolders.size(); a < n; a++) {
            MessagesController.DialogFilter filter = defaultFolders.get(a);
            messagesController.addFilter(filter, false);
            MessagesStorage.getInstance(account).saveDialogFilter(filter, false, true);
            saveFilterToServer(account, messagesController, filter);
        }

        MessagesStorage.getInstance(account).saveDialogFiltersOrder();
        saveFiltersOrderToServer(account, messagesController);
        NotificationCenter.getInstance(account).postNotificationName(NotificationCenter.dialogFiltersUpdated);
    }

    private static MessagesController.DialogFilter createFilter(MessagesController messagesController, int[] nextId, String name, int flags, ArrayList<Long> alwaysShow) {
        MessagesController.DialogFilter filter = new MessagesController.DialogFilter();
        filter.id = nextId[0];
        nextId[0] = nextFilterId(messagesController, filter.id + 1);
        filter.name = name;
        filter.flags = flags;
        filter.color = FILTER_COLOR_NONE;
        filter.pendingUnreadCount = filter.unreadCount = -1;
        if (alwaysShow != null) {
            filter.alwaysShow.addAll(alwaysShow);
        }
        return filter;
    }

    private static int nextFilterId(MessagesController messagesController, int startId) {
        int id = startId;
        while (messagesController.dialogFiltersById.get(id) != null) {
            id++;
        }
        return id;
    }

    private static void resolveOfficialChannelDialogs(int account, MessagesController messagesController, DialogsCallback callback) {
        String[] usernames = NeoOfficialHelper.getOfficialChannelUsernames();
        if (usernames.length == 0) {
            callback.run(new ArrayList<>());
            return;
        }

        ArrayList<Long> resolvedDialogs = new ArrayList<>();
        HashSet<Long> resolvedSet = new HashSet<>();
        long[] officialChannelIds = NeoOfficialHelper.getOfficialChannelIds();
        for (int a = 0, n = officialChannelIds.length; a < n; a++) {
            long channelId = officialChannelIds[a];
            if (channelId != 0 && resolvedSet.add(-channelId)) {
                resolvedDialogs.add(-channelId);
            }
        }
        int[] pending = new int[]{usernames.length};

        for (String username : usernames) {
            if (TextUtils.isEmpty(username)) {
                if (--pending[0] == 0) {
                    callback.run(resolvedDialogs);
                }
                continue;
            }

            messagesController.getUserNameResolver().resolve(username, peerId -> AndroidUtilities.runOnUIThread(() -> {
                if (peerId != null && peerId < 0 && resolvedSet.add(peerId)) {
                    resolvedDialogs.add(peerId);
                }
                if (--pending[0] == 0) {
                    callback.run(resolvedDialogs);
                }
            }));
        }
    }

    private static void saveFilterToServer(int account, MessagesController messagesController, MessagesController.DialogFilter filter) {
        TLRPC.TL_messages_updateDialogFilter req = new TLRPC.TL_messages_updateDialogFilter();
        req.id = filter.id;
        req.flags |= 1;
        req.filter = new TLRPC.TL_dialogFilter();
        req.filter.id = filter.id;
        req.filter.contacts = (filter.flags & MessagesController.DIALOG_FILTER_FLAG_CONTACTS) != 0;
        req.filter.non_contacts = (filter.flags & MessagesController.DIALOG_FILTER_FLAG_NON_CONTACTS) != 0;
        req.filter.groups = (filter.flags & MessagesController.DIALOG_FILTER_FLAG_GROUPS) != 0;
        req.filter.broadcasts = (filter.flags & MessagesController.DIALOG_FILTER_FLAG_CHANNELS) != 0;
        req.filter.bots = (filter.flags & MessagesController.DIALOG_FILTER_FLAG_BOTS) != 0;
        req.filter.exclude_muted = (filter.flags & MessagesController.DIALOG_FILTER_FLAG_EXCLUDE_MUTED) != 0;
        req.filter.exclude_read = (filter.flags & MessagesController.DIALOG_FILTER_FLAG_EXCLUDE_READ) != 0;
        req.filter.exclude_archived = (filter.flags & MessagesController.DIALOG_FILTER_FLAG_EXCLUDE_ARCHIVED) != 0;
        req.filter.title = new TLRPC.TL_textWithEntities();
        req.filter.title.text = filter.name;
        req.filter.title.entities = filter.entities;
        req.filter.title_noanimate = filter.title_noanimate;
        if (!TextUtils.isEmpty(filter.emoticon)) {
            req.filter.emoticon = filter.emoticon;
            req.filter.flags |= 33554432;
        }
        if (filter.color >= 0) {
            req.filter.color = filter.color;
            req.filter.flags |= 134217728;
        }

        for (int a = 0, n = filter.alwaysShow.size(); a < n; a++) {
            TLRPC.InputPeer inputPeer = getInputPeer(messagesController, filter.alwaysShow.get(a));
            if (inputPeer != null) {
                req.filter.include_peers.add(inputPeer);
            }
        }
        for (int a = 0, n = filter.neverShow.size(); a < n; a++) {
            TLRPC.InputPeer inputPeer = getInputPeer(messagesController, filter.neverShow.get(a));
            if (inputPeer != null) {
                req.filter.exclude_peers.add(inputPeer);
            }
        }

        ConnectionsManager.getInstance(account).sendRequest(req, null);
    }

    private static TLRPC.InputPeer getInputPeer(MessagesController messagesController, long dialogId) {
        if (dialogId > 0) {
            TLRPC.User user = messagesController.getUser(dialogId);
            return user != null ? MessagesController.getInputPeer(user) : null;
        }
        if (dialogId < 0) {
            TLRPC.Chat chat = messagesController.getChat(-dialogId);
            if (chat == null) {
                return null;
            }
            if (ChatObject.isChannel(chat)) {
                return MessagesController.getInputPeer(chat);
            }
            TLRPC.TL_inputPeerChat inputPeer = new TLRPC.TL_inputPeerChat();
            inputPeer.chat_id = -dialogId;
            return inputPeer;
        }
        return null;
    }

    private static void saveFiltersOrderToServer(int account, MessagesController messagesController) {
        TLRPC.TL_messages_updateDialogFiltersOrder req = new TLRPC.TL_messages_updateDialogFiltersOrder();
        ArrayList<MessagesController.DialogFilter> filters = messagesController.getDialogFilters();
        for (int a = 0, n = filters.size(); a < n; a++) {
            MessagesController.DialogFilter filter = filters.get(a);
            req.order.add(filter.isDefault() ? 0 : filter.id);
        }
        ConnectionsManager.getInstance(account).sendRequest(req, null);
    }

    private interface DialogsCallback {
        void run(ArrayList<Long> dialogs);
    }

    private static class PresetFolder {
        final String name;
        final int flags;
        final boolean officialFolder;

        PresetFolder(String name, int flags, boolean officialFolder) {
            this.name = name;
            this.flags = flags;
            this.officialFolder = officialFolder;
        }

        boolean matches(MessagesController.DialogFilter filter) {
            if (officialFolder) {
                return TextUtils.equals(filter.name, name)
                        || TextUtils.equals(filter.name, "NeoGram app");
            }
            if (flags == 0) {
                return TextUtils.equals(filter.name, name);
            }
            return filter.flags == flags && filter.alwaysShow.isEmpty() && filter.neverShow.isEmpty();
        }
    }
}
