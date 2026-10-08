package com.neongroup.neogram.helpers;

import android.net.Uri;
import android.text.TextUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.browser.Browser;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.LaunchActivity;

import java.util.Locale;
import java.util.function.Consumer;

import com.neongroup.neogram.settings.BaseNeoSettingsActivity;
import com.neongroup.neogram.settings.NeoAppearanceSettingsActivity;
import com.neongroup.neogram.settings.NeoChatSettingsActivity;
import com.neongroup.neogram.settings.NeoEmojiSettingsActivity;
import com.neongroup.neogram.settings.NeoExperimentalSettingsActivity;
import com.neongroup.neogram.settings.NeoGeneralSettingsActivity;
import com.neongroup.neogram.settings.NeoPasscodeSettingsActivity;

public class SettingsHelper {

    public static void processDeepLink(Uri uri, Consumer<BaseFragment> callback, Runnable unknown, Browser.Progress progress) {
        if (uri == null) {
            unknown.run();
            return;
        }
        var segments = uri.getPathSegments();
        if (segments.isEmpty() || segments.size() > 2) {
            unknown.run();
            return;
        }
        BaseNeoSettingsActivity fragment;
        var segment = segments.get(1);
        if (PasscodeHelper.getSettingsKey().equals(segment)) {
            fragment = new NeoPasscodeSettingsActivity();
        } else {
            switch (segment.toLowerCase(Locale.US)) {
                case "appearance":
                case "a":
                    fragment = new NeoAppearanceSettingsActivity();
                    break;
                case "chat":
                case "chats":
                case "c":
                    fragment = new NeoChatSettingsActivity();
                    break;
                case "experimental":
                case "e":
                    fragment = new NeoExperimentalSettingsActivity();
                    break;
                case "emoji":
                    fragment = new NeoEmojiSettingsActivity();
                    break;
                case "general":
                case "g":
                    fragment = new NeoGeneralSettingsActivity();
                    break;
                case "reportid":
                    SettingsHelper.copyReportId();
                    return;
                case "update":
                    LaunchActivity.instance.checkAppUpdate(true, progress);
                    return;
                default:
                    unknown.run();
                    return;
            }
        }
        callback.accept(fragment);
        var row = uri.getQueryParameter("r");
        if (TextUtils.isEmpty(row)) {
            row = uri.getQueryParameter("row");
        }
        if (!TextUtils.isEmpty(row)) {
            var rowFinal = row;
            AndroidUtilities.runOnUIThread(() -> fragment.scrollToRow(rowFinal, unknown));
        }
    }

    public static void copyReportId() {
        AndroidUtilities.addToClipboard(AnalyticsHelper.userId);
        BulletinFactory.global().createSimpleBulletin(R.raw.copy, LocaleController.getString(R.string.TextCopied), LocaleController.getString(R.string.CopyReportIdDescription)).show();
    }
}
