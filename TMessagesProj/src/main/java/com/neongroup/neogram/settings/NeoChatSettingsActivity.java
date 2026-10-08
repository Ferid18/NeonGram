package com.neongroup.neogram.settings;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.graphics.ColorUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.RadioColorCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextCheckbox2Cell;
import org.telegram.ui.Cells.ThemePreviewMessagesCell;
import org.telegram.ui.Components.AnimatedTextView;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.SeekBarView;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalRecyclerView;

import java.util.ArrayList;

import com.neongroup.neogram.NeoConfig;
import com.neongroup.neogram.helpers.EntitiesHelper;
import com.neongroup.neogram.helpers.PopupHelper;
import com.neongroup.neogram.helpers.VoiceEnhancementsHelper;
import com.neongroup.neogram.helpers.WhisperHelper;

public class NeoChatSettingsActivity extends BaseNeoSettingsActivity implements NotificationCenter.NotificationCenterDelegate {

    private ActionBarMenuItem resetItem;

    private final int stickerSizeRow = rowId++;
    private final int hideTimeOnStickerRow = rowId++;
    private final int showTimeHintRow = rowId++;
    private final int reducedColorsRow = rowId++;

    private final int ignoreBlockedRow = rowId++;
    private final int quickForwardRow = rowId++;
    private final int hideKeyboardOnChatScrollRow = rowId++;
    private final int tryToOpenAllLinksInIVRow = rowId++;
    private final int disableJumpToNextRow = rowId++;
    private final int disableGreetingStickerRow = rowId++;
    private final int hideChannelBottomButtonsRow = rowId++;
    private final int doubleTapActionRow = rowId++;
    private final int maxRecentStickersRow = rowId++;

    private final int transcribeProviderRow = rowId++;
    private final int cfCredentialsRow = rowId++;

    private final int markdownEnableRow = rowId++;
    private final int markdownParserRow = rowId++;
    private final int markdownParseLinksRow = rowId++;
    private final int markdown2Row = rowId++;

    private final int voiceEnhancementsRow = rowId++;
    private final int rearVideoMessagesRow = rowId++;
    private final int confirmAVRow = rowId++;
    private final int disableProximityEventsRow = rowId++;
    private final int disableVoiceMessageAutoPlayRow = rowId++;
    private final int unmuteVideosWithVolumeButtonsRow = rowId++;
    private final int autoPauseVideoRow = rowId++;
    private final int preferOriginalQualityRow = rowId++;

    private final int messageMenuRow = 100;

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();

        NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.emojiLoaded);

        return true;
    }

    @Override
    public View createView(Context context) {
        var fragmentView = super.createView(context);

        var menu = actionBar.createMenu();
        resetItem = menu.addItem(0, R.drawable.msg_reset);
        resetItem.setContentDescription(LocaleController.getString(R.string.ResetStickerSize));
        resetItem.setTag(null);
        resetItem.setOnClickListener(v -> {
            AndroidUtilities.updateViewVisibilityAnimated(resetItem, false, 0.5f, true);
            var item = listView.findItemByItemId(stickerSizeRow);
            var stickerCell = (StickerSizeCell) listView.findViewByItemId(stickerSizeRow);
            if (stickerCell != null) {
                ValueAnimator animator = ValueAnimator.ofFloat(NeoConfig.stickerSize, 14.0f);
                animator.setDuration(150);
                animator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
                animator.addUpdateListener(valueAnimator -> {
                    var floatValue = (float) valueAnimator.getAnimatedValue();
                    NeoConfig.setStickerSize(floatValue);
                    stickerCell.setValue(floatValue);
                });
                animator.start();
            } else {
                NeoConfig.setStickerSize(14.0f);
            }
            item.floatValue = 14.0f;
        });
        AndroidUtilities.updateViewVisibilityAnimated(resetItem, Float.compare(NeoConfig.stickerSize, 14.0f) != 0, 1f, false);

        return fragmentView;
    }

    public String getDoubleTapActionText(int action) {
        return switch (action) {
            case NeoConfig.DOUBLE_TAP_ACTION_REACTION ->
                    LocaleController.getString(R.string.Reactions);
            case NeoConfig.DOUBLE_TAP_ACTION_TRANSLATE ->
                    LocaleController.getString(R.string.TranslateMessage);
            case NeoConfig.DOUBLE_TAP_ACTION_REPLY -> LocaleController.getString(R.string.Reply);
            case NeoConfig.DOUBLE_TAP_ACTION_SAVE ->
                    LocaleController.getString(R.string.AddToSavedMessages);
            case NeoConfig.DOUBLE_TAP_ACTION_REPEAT -> LocaleController.getString(R.string.Repeat);
            case NeoConfig.DOUBLE_TAP_ACTION_EDIT -> LocaleController.getString(R.string.Edit);
            default -> LocaleController.getString(R.string.Disable);
        };
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        items.add(StickerSizeCellFactory.of(stickerSizeRow, LocaleController.getString(R.string.StickerSize), NeoConfig.stickerSize, progress -> {
            NeoConfig.setStickerSize(progress);
            if (progress != 14.0f && resetItem.getVisibility() != View.VISIBLE) {
                AndroidUtilities.updateViewVisibilityAnimated(resetItem, true, 0.5f, true);
            }
        }).slug("stickerSize"));
        items.add(UItem.asCheck(hideTimeOnStickerRow, LocaleController.getString(R.string.HideTimeOnSticker)).slug("hideTimeOnSticker").setChecked(NeoConfig.hideTimeOnSticker));
        items.add(UItem.asCheck(showTimeHintRow, LocaleController.getString(R.string.ShowTimeHint), LocaleController.getString(R.string.ShowTimeHintDesc)).slug("showTimeHint").setChecked(NeoConfig.showTimeHint));
        items.add(UItem.asCheck(reducedColorsRow, LocaleController.getString(R.string.ReducedColors)).slug("reducedColors").setChecked(NeoConfig.reducedColors));
        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader(LocaleController.getString(R.string.Chat)));
        items.add(UItem.asCheck(ignoreBlockedRow, LocaleController.getString(R.string.IgnoreBlocked), LocaleController.getString(R.string.IgnoreBlockedAbout)).slug("ignoreBlocked").setChecked(NeoConfig.ignoreBlocked));
        items.add(UItem.asCheck(quickForwardRow, LocaleController.getString(R.string.QuickForward)).slug("quickForward").setChecked(NeoConfig.quickForward));
        items.add(UItem.asCheck(hideKeyboardOnChatScrollRow, LocaleController.getString(R.string.HideKeyboardOnChatScroll)).slug("hideKeyboardOnChatScroll").setChecked(NeoConfig.hideKeyboardOnChatScroll));
        items.add(UItem.asCheck(tryToOpenAllLinksInIVRow, LocaleController.getString(R.string.OpenAllLinksInInstantView)).slug("tryToOpenAllLinksInIV").setChecked(NeoConfig.tryToOpenAllLinksInIV));
        items.add(UItem.asCheck(disableJumpToNextRow, LocaleController.getString(R.string.DisableJumpToNextChannel)).slug("disableJumpToNext").setChecked(NeoConfig.disableJumpToNextChannel));
        items.add(UItem.asCheck(disableGreetingStickerRow, LocaleController.getString(R.string.DisableGreetingSticker)).slug("disableGreetingSticker").setChecked(NeoConfig.disableGreetingSticker));
        items.add(UItem.asCheck(hideChannelBottomButtonsRow, LocaleController.getString(R.string.HideChannelBottomButtons)).slug("hideChannelBottomButtons").setChecked(NeoConfig.hideChannelBottomButtons));
        items.add(TextSettingsCellFactory.of(doubleTapActionRow, LocaleController.getString(R.string.DoubleTapAction), NeoConfig.doubleTapInAction == NeoConfig.doubleTapOutAction ?
                getDoubleTapActionText(NeoConfig.doubleTapInAction) :
                getDoubleTapActionText(NeoConfig.doubleTapInAction) + ", " + getDoubleTapActionText(NeoConfig.doubleTapOutAction)).slug("doubleTapAction"));
        items.add(TextSettingsCellFactory.of(maxRecentStickersRow, LocaleController.getString(R.string.MaxRecentStickers), String.valueOf(NeoConfig.maxRecentStickers)).slug("maxRecentStickers"));
        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader(LocaleController.getString(R.string.PremiumPreviewVoiceToText)));
        items.add(TextSettingsCellFactory.of(transcribeProviderRow, LocaleController.getString(R.string.TranscribeProviderShort), switch (NeoConfig.transcribeProvider) {
            case NeoConfig.TRANSCRIBE_AUTO ->
                    LocaleController.getString(R.string.TranscribeProviderAuto);
            case NeoConfig.TRANSCRIBE_WORKERSAI ->
                    LocaleController.getString(R.string.TranscribeProviderWorkersAI);
            default -> LocaleController.getString(R.string.TelegramPremium);
        }).slug("transcribeProvider"));
        items.add(TextSettingsCellFactory.of(cfCredentialsRow, LocaleController.getString(R.string.CloudflareCredentials), "").slug("cfCredentials"));
        items.add(UItem.asShadow(LocaleController.formatString(R.string.TranscribeProviderDesc, LocaleController.getString(R.string.TranscribeProviderWorkersAI))));

        items.add(UItem.asHeader(LocaleController.getString(R.string.Markdown)));
        items.add(UItem.asCheck(markdownEnableRow, LocaleController.getString(R.string.MarkdownEnableByDefault)).slug("markdownEnable").setChecked(!NeoConfig.disableMarkdownByDefault));
        items.add(TextSettingsCellFactory.of(markdownParserRow, LocaleController.getString(R.string.MarkdownParser), NeoConfig.newMarkdownParser ? "Neogram" : "Telegram").slug("markdownParser"));
        if (NeoConfig.newMarkdownParser) {
            items.add(UItem.asCheck(markdownParseLinksRow, LocaleController.getString(R.string.MarkdownParseLinks)).slug("markdownParseLinks").setChecked(NeoConfig.markdownParseLinks));
        }
        items.add(UItem.asShadow(markdown2Row, TextUtils.expandTemplate(EntitiesHelper.parseMarkdown(NeoConfig.newMarkdownParser && NeoConfig.markdownParseLinks ? LocaleController.getString(R.string.MarkdownAbout) : LocaleController.getString(R.string.MarkdownAbout2)), "**", "__", "~~", "`", "||", "[", "](", ")")));

        items.add(UItem.asHeader(LocaleController.getString(R.string.SharedMediaTab2)));
        if (VoiceEnhancementsHelper.isAvailable()) {
            items.add(UItem.asCheck(voiceEnhancementsRow, LocaleController.getString(R.string.VoiceEnhancements), LocaleController.getString(R.string.VoiceEnhancementsAbout)).slug("voiceEnhancements").setChecked(NeoConfig.voiceEnhancements));
        }
        items.add(UItem.asCheck(rearVideoMessagesRow, LocaleController.getString(R.string.RearVideoMessages)).slug("rearVideoMessages").setChecked(NeoConfig.rearVideoMessages));
        items.add(UItem.asCheck(confirmAVRow, LocaleController.getString(R.string.ConfirmAVMessage)).slug("confirmAV").setChecked(NeoConfig.confirmAVMessage));
        items.add(UItem.asCheck(disableProximityEventsRow, LocaleController.getString(R.string.DisableProximityEvents)).slug("disableProximityEvents").setChecked(NeoConfig.disableProximityEvents));
        items.add(UItem.asCheck(disableVoiceMessageAutoPlayRow, LocaleController.getString(R.string.DisableVoiceMessagesAutoPlay)).slug("disableVoiceMessageAutoPlay").setChecked(NeoConfig.disableVoiceMessageAutoPlay));
        items.add(UItem.asCheck(unmuteVideosWithVolumeButtonsRow, LocaleController.getString(R.string.UnmuteVideosWithVolumeButtons)).slug("unmuteVideosWithVolumeButtons").setChecked(NeoConfig.unmuteVideosWithVolumeButtons));
        items.add(UItem.asCheck(autoPauseVideoRow, LocaleController.getString(R.string.AutoPauseVideo), LocaleController.getString(R.string.AutoPauseVideoAbout)).slug("autoPauseVideo").setChecked(NeoConfig.autoPauseVideo));
        items.add(UItem.asCheck(preferOriginalQualityRow, LocaleController.getString(R.string.PreferOriginalQuality), LocaleController.getString(R.string.PreferOriginalQualityDesc)).slug("preferOriginalQuality").setChecked(NeoConfig.preferOriginalQuality));
        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader(LocaleController.getString(R.string.MessageMenu)));
        items.add(TextCheckbox2CellFactory.of(messageMenuRow + 1, LocaleController.getString(R.string.DeleteDownloadedFile)).slug("showDeleteDownloadedFile").setChecked(NeoConfig.showDeleteDownloadedFile));
        items.add(TextCheckbox2CellFactory.of(messageMenuRow + 2, LocaleController.getString(R.string.NoQuoteForward)).slug("showNoQuoteForward").setChecked(NeoConfig.showNoQuoteForward));
        items.add(TextCheckbox2CellFactory.of(messageMenuRow + 3, LocaleController.getString(R.string.AddToSavedMessages)).slug("showAddToSavedMessages").setChecked(NeoConfig.showAddToSavedMessages));
        items.add(TextCheckbox2CellFactory.of(messageMenuRow + 4, LocaleController.getString(R.string.Repeat)).slug("showRepeat").setChecked(NeoConfig.showRepeat));
        items.add(TextCheckbox2CellFactory.of(messageMenuRow + 5, LocaleController.getString(R.string.Prpr)).slug("showPrPr").setChecked(NeoConfig.showPrPr));
        items.add(TextCheckbox2CellFactory.of(messageMenuRow + 6, LocaleController.getString(R.string.TranslateMessage)).slug("showTranslate").setChecked(NeoConfig.showTranslate));
        items.add(TextCheckbox2CellFactory.of(messageMenuRow + 7, LocaleController.getString(R.string.ReportChat)).slug("showReport").setChecked(NeoConfig.showReport));
        items.add(TextCheckbox2CellFactory.of(messageMenuRow + 8, LocaleController.getString(R.string.MessageDetails)).slug("showMessageDetails").setChecked(NeoConfig.showMessageDetails));
        items.add(TextCheckbox2CellFactory.of(messageMenuRow + 9, LocaleController.getString(R.string.CopyPhoto)).slug("showCopyPhoto").setChecked(NeoConfig.showCopyPhoto));
        items.add(TextCheckbox2CellFactory.of(messageMenuRow + 10, LocaleController.getString(R.string.SetReminder)).slug("showSetReminder").setChecked(NeoConfig.showSetReminder));
        items.add(TextCheckbox2CellFactory.of(messageMenuRow + 11, LocaleController.getString(R.string.QrCode)).slug("showQrCode").setChecked(NeoConfig.showQrCode));
        items.add(TextCheckbox2CellFactory.of(messageMenuRow + 12, LocaleController.getString(R.string.OpenInExternalApp)).slug("showOpenIn").setChecked(NeoConfig.showOpenIn));
        items.add(UItem.asShadow(null));
    }

    @Override
    protected void onItemClick(UItem item, View view, int position, float x, float y) {
        var id = item.id;
        if (id == ignoreBlockedRow) {
            NeoConfig.toggleIgnoreBlocked();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NeoConfig.ignoreBlocked);
            }
        } else if (id == hideKeyboardOnChatScrollRow) {
            NeoConfig.toggleHideKeyboardOnChatScroll();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NeoConfig.hideKeyboardOnChatScroll);
            }
        } else if (id == rearVideoMessagesRow) {
            NeoConfig.toggleRearVideoMessages();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NeoConfig.rearVideoMessages);
            }
        } else if (id == confirmAVRow) {
            NeoConfig.toggleConfirmAVMessage();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NeoConfig.confirmAVMessage);
            }
        } else if (id == disableProximityEventsRow) {
            NeoConfig.toggleDisableProximityEvents();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NeoConfig.disableProximityEvents);
            }
            showRestartBulletin();
        } else if (id == tryToOpenAllLinksInIVRow) {
            NeoConfig.toggleTryToOpenAllLinksInIV();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NeoConfig.tryToOpenAllLinksInIV);
            }
        } else if (id == autoPauseVideoRow) {
            NeoConfig.toggleAutoPauseVideo();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NeoConfig.autoPauseVideo);
            }
        } else if (id == disableJumpToNextRow) {
            NeoConfig.toggleDisableJumpToNextChannel();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NeoConfig.disableJumpToNextChannel);
            }
        } else if (id == disableGreetingStickerRow) {
            NeoConfig.toggleDisableGreetingSticker();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NeoConfig.disableGreetingSticker);
            }
        } else if (id == disableVoiceMessageAutoPlayRow) {
            NeoConfig.toggleDisableVoiceMessageAutoPlay();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NeoConfig.disableVoiceMessageAutoPlay);
            }
        } else if (id == unmuteVideosWithVolumeButtonsRow) {
            NeoConfig.toggleUnmuteVideosWithVolumeButtons();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NeoConfig.unmuteVideosWithVolumeButtons);
            }
        } else if (id == doubleTapActionRow) {
            ArrayList<String> arrayList = new ArrayList<>();
            ArrayList<Integer> types = new ArrayList<>();
            arrayList.add(LocaleController.getString(R.string.Disable));
            types.add(NeoConfig.DOUBLE_TAP_ACTION_NONE);
            arrayList.add(LocaleController.getString(R.string.Reactions));
            types.add(NeoConfig.DOUBLE_TAP_ACTION_REACTION);
            arrayList.add(LocaleController.getString(R.string.TranslateMessage));
            types.add(NeoConfig.DOUBLE_TAP_ACTION_TRANSLATE);
            arrayList.add(LocaleController.getString(R.string.Reply));
            types.add(NeoConfig.DOUBLE_TAP_ACTION_REPLY);
            arrayList.add(LocaleController.getString(R.string.AddToSavedMessages));
            types.add(NeoConfig.DOUBLE_TAP_ACTION_SAVE);
            arrayList.add(LocaleController.getString(R.string.Repeat));
            types.add(NeoConfig.DOUBLE_TAP_ACTION_REPEAT);
            arrayList.add(LocaleController.getString(R.string.Edit));
            types.add(NeoConfig.DOUBLE_TAP_ACTION_EDIT);

            var context = getParentActivity();
            var builder = new AlertDialog.Builder(context, resourcesProvider);
            builder.setTitle(LocaleController.getString(R.string.DoubleTapAction));

            var linearLayout = new LinearLayout(context);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            builder.setView(linearLayout);

            var messagesCell = new ThemePreviewMessagesCell(context, parentLayout, 0);
            messagesCell.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
            linearLayout.addView(messagesCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

            var hLayout = new LinearLayout(context);
            hLayout.setOrientation(LinearLayout.HORIZONTAL);
            hLayout.setPadding(0, AndroidUtilities.dp(8), 0, 0);
            linearLayout.addView(hLayout);

            for (int i = 0; i < 2; i++) {
                var out = i == 1;
                var layout = new LinearLayout(context);
                layout.setOrientation(LinearLayout.VERTICAL);
                hLayout.addView(layout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, .5f));

                for (int a = 0; a < arrayList.size(); a++) {

                    var cell = new RadioColorCell(context, resourcesProvider);
                    cell.setPadding(AndroidUtilities.dp(4), 0, AndroidUtilities.dp(4), 0);
                    cell.setTag(a);
                    cell.setTextAndValue(arrayList.get(a), a == types.indexOf(out ? NeoConfig.doubleTapOutAction : NeoConfig.doubleTapInAction));
                    cell.setBackground(Theme.createRadSelectorDrawable(Theme.getColor(Theme.key_listSelector, resourcesProvider), out ? AndroidUtilities.dp(6) : 0, out ? 0 : AndroidUtilities.dp(6), out ? 0 : AndroidUtilities.dp(6), out ? AndroidUtilities.dp(6) : 0));
                    layout.addView(cell);
                    cell.setOnClickListener(v -> {
                        var which = (Integer) v.getTag();
                        var old = out ? NeoConfig.doubleTapOutAction : NeoConfig.doubleTapInAction;
                        if (types.get(which) == old) {
                            return;
                        }
                        if (out) {
                            NeoConfig.setDoubleTapOutAction(types.get(which));
                        } else {
                            NeoConfig.setDoubleTapInAction(types.get(which));
                        }
                        ((RadioColorCell) layout.getChildAt(types.indexOf(old))).setChecked(false, true);
                        cell.setChecked(true, true);
                        item.textValue = NeoConfig.doubleTapInAction == NeoConfig.doubleTapOutAction ?
                                getDoubleTapActionText(NeoConfig.doubleTapInAction) :
                                getDoubleTapActionText(NeoConfig.doubleTapInAction) + ", " + getDoubleTapActionText(NeoConfig.doubleTapOutAction);
                        listView.adapter.notifyItemChanged(position, PARTIAL);
                    });
                }
            }

            builder.setOnPreDismissListener(dialog -> listView.adapter.notifyItemChanged(position, PARTIAL));
            builder.setNegativeButton(LocaleController.getString(R.string.OK), null);
            builder.show();
        } else if (id == markdownEnableRow) {
            NeoConfig.toggleDisableMarkdownByDefault();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(!NeoConfig.disableMarkdownByDefault);
            }
        } else if (id > messageMenuRow) {
            TextCheckbox2Cell cell = ((TextCheckbox2Cell) view);
            int menuPosition = id - messageMenuRow - 1;
            if (menuPosition == 0) {
                NeoConfig.toggleShowDeleteDownloadedFile();
                cell.setChecked(NeoConfig.showDeleteDownloadedFile);
            } else if (menuPosition == 1) {
                NeoConfig.toggleShowNoQuoteForward();
                cell.setChecked(NeoConfig.showNoQuoteForward);
            } else if (menuPosition == 2) {
                NeoConfig.toggleShowAddToSavedMessages();
                cell.setChecked(NeoConfig.showAddToSavedMessages);
            } else if (menuPosition == 3) {
                NeoConfig.toggleShowRepeat();
                cell.setChecked(NeoConfig.showRepeat);
            } else if (menuPosition == 4) {
                NeoConfig.toggleShowPrPr();
                cell.setChecked(NeoConfig.showPrPr);
            } else if (menuPosition == 5) {
                NeoConfig.toggleShowTranslate();
                cell.setChecked(NeoConfig.showTranslate);
            } else if (menuPosition == 6) {
                NeoConfig.toggleShowReport();
                cell.setChecked(NeoConfig.showReport);
            } else if (menuPosition == 7) {
                NeoConfig.toggleShowMessageDetails();
                cell.setChecked(NeoConfig.showMessageDetails);
            } else if (menuPosition == 8) {
                NeoConfig.toggleShowCopyPhoto();
                cell.setChecked(NeoConfig.showCopyPhoto);
            } else if (menuPosition == 9) {
                NeoConfig.toggleShowSetReminder();
                cell.setChecked(NeoConfig.showSetReminder);
            } else if (menuPosition == 10) {
                NeoConfig.toggleShowQrCode();
                cell.setChecked(NeoConfig.showQrCode);
            } else if (menuPosition == 11) {
                NeoConfig.toggleShowOpenIn();
                cell.setChecked(NeoConfig.showOpenIn);
            }
        } else if (id == voiceEnhancementsRow) {
            NeoConfig.toggleVoiceEnhancements();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NeoConfig.voiceEnhancements);
            }
        } else if (id == maxRecentStickersRow) {
            int[] counts = {20, 30, 40, 50, 80, 100, 120, 150, 180, 200};
            ArrayList<String> types = new ArrayList<>();
            for (int count : counts) {
                if (count <= getMessagesController().maxRecentStickersCount) {
                    types.add(String.valueOf(count));
                }
            }
            PopupHelper.show(types, LocaleController.getString(R.string.MaxRecentStickers), types.indexOf(String.valueOf(NeoConfig.maxRecentStickers)), getParentActivity(), view, i -> {
                NeoConfig.setMaxRecentStickers(Integer.parseInt(types.get(i)));
                item.textValue = String.valueOf(NeoConfig.maxRecentStickers);
                listView.adapter.notifyItemChanged(position, PARTIAL);
            }, resourcesProvider);
        } else if (id == hideTimeOnStickerRow) {
            NeoConfig.toggleHideTimeOnSticker();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NeoConfig.hideTimeOnSticker);
            }
            var stickerCell = listView.findViewByItemId(stickerSizeRow);
            if (stickerCell != null) stickerCell.invalidate();
        } else if (id == markdownParserRow) {
            ArrayList<String> arrayList = new ArrayList<>();
            arrayList.add("Neogram");
            arrayList.add("Telegram");
            boolean oldParser = NeoConfig.newMarkdownParser;
            PopupHelper.show(arrayList, LocaleController.getString(R.string.MarkdownParser), NeoConfig.newMarkdownParser ? 0 : 1, getParentActivity(), view, i -> {
                NeoConfig.setNewMarkdownParser(i == 0);
                item.textValue = arrayList.get(i);
                listView.adapter.notifyItemChanged(position, PARTIAL);
                if (oldParser != NeoConfig.newMarkdownParser) {
                    if (oldParser) {
                        notifyItemRemoved(markdownParseLinksRow);
                        updateRows();
                    } else {
                        updateRows();
                        notifyItemInserted(markdownParseLinksRow);
                    }
                    notifyItemChanged(markdown2Row);
                }
            }, resourcesProvider);
        } else if (id == markdownParseLinksRow) {
            NeoConfig.toggleMarkdownParseLinks();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NeoConfig.markdownParseLinks);
            }
            notifyItemChanged(markdown2Row);
        } else if (id == quickForwardRow) {
            NeoConfig.toggleQuickForward();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NeoConfig.quickForward);
            }
        } else if (id == reducedColorsRow) {
            NeoConfig.toggleReducedColors();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NeoConfig.reducedColors);
            }
            var stickerCell = listView.findViewByItemId(stickerSizeRow);
            stickerCell.invalidate();
        } else if (id == showTimeHintRow) {
            NeoConfig.toggleShowTimeHint();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NeoConfig.showTimeHint);
            }
        } else if (id == transcribeProviderRow) {
            ArrayList<String> arrayList = new ArrayList<>();
            ArrayList<Integer> types = new ArrayList<>();
            arrayList.add(LocaleController.getString(R.string.TranscribeProviderAuto));
            types.add(NeoConfig.TRANSCRIBE_AUTO);
            arrayList.add(LocaleController.getString(R.string.TelegramPremium));
            types.add(NeoConfig.TRANSCRIBE_PREMIUM);
            arrayList.add(LocaleController.getString(R.string.TranscribeProviderWorkersAI));
            types.add(NeoConfig.TRANSCRIBE_WORKERSAI);
            PopupHelper.show(arrayList, LocaleController.getString(R.string.TranscribeProviderShort), types.indexOf(NeoConfig.transcribeProvider), getParentActivity(), view, i -> {
                NeoConfig.setTranscribeProvider(types.get(i));
                item.textValue = arrayList.get(i);
                listView.adapter.notifyItemChanged(position, PARTIAL);
            }, resourcesProvider);
        } else if (id == cfCredentialsRow) {
            WhisperHelper.showCfCredentialsDialog(this);
        } else if (id == preferOriginalQualityRow) {
            NeoConfig.togglePreferOriginalQuality();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NeoConfig.preferOriginalQuality);
            }
        } else if (id == hideChannelBottomButtonsRow) {
            NeoConfig.toggleHideChannelBottomButtons();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NeoConfig.hideChannelBottomButtons);
            }
        }
    }

    @Override
    protected String getActionBarTitle() {
        return LocaleController.getString(R.string.Chat);
    }

    @Override
    protected String getKey() {
        return "c";
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.emojiLoaded) {
            if (listView != null) {
                listView.invalidateViews();
            }
        }
    }

    private static class StickerSizeCellFactory extends UItem.UItemFactory<StickerSizeCell> {
        static {
            setup(new StickerSizeCellFactory());
        }

        @Override
        public StickerSizeCell createView(Context context, RecyclerListView listView, int currentAccount, int classGuid, Theme.ResourcesProvider resourcesProvider) {
            return new StickerSizeCell(context, resourcesProvider);
        }

        @Override
        public void bindView(View view, UItem item, boolean divider, UniversalAdapter adapter, UniversalRecyclerView listView) {
            var cell = (StickerSizeCell) view;
            var frameLayout = (FrameLayout) listView.getParent();
            cell.setFragmentView(frameLayout);
            cell.setValue(item.floatValue);
            cell.setOnDragListener((AltSeekbar.OnDrag) item.object);
        }

        public static UItem of(int id, String title, float value, AltSeekbar.OnDrag onDrag) {
            var item = UItem.ofFactory(StickerSizeCellFactory.class);
            item.id = id;
            item.text = title;
            item.object = onDrag;
            item.floatValue = value;
            return item;
        }

        @Override
        public boolean isClickable() {
            return false;
        }
    }

    private static class StickerSizeCell extends FrameLayout {

        private final StickerSizePreviewMessagesCell messagesCell;
        private final AltSeekbar sizeBar;

        private AltSeekbar.OnDrag onDrag;

        public StickerSizeCell(Context context, Theme.ResourcesProvider resourcesProvider) {
            super(context);

            setWillNotDraw(false);

            sizeBar = new AltSeekbar(context, progress -> {
                setValue(progress);
                if (onDrag != null) onDrag.run(progress);
            }, 2, 20, LocaleController.getString(R.string.StickerSize), LocaleController.getString(R.string.StickerSizeLeft), LocaleController.getString(R.string.StickerSizeRight), resourcesProvider);
            sizeBar.setValue(NeoConfig.stickerSize);
            addView(sizeBar, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

            messagesCell = new StickerSizePreviewMessagesCell(context, resourcesProvider);
            messagesCell.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
            addView(messagesCell, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 0, 112, 0, 0));
        }

        public void setOnDragListener(AltSeekbar.OnDrag onDrag) {
            this.onDrag = onDrag;
        }

        public void setFragmentView(FrameLayout fragmentView) {
            messagesCell.setFragmentView(fragmentView);
        }

        public void setValue(float value) {
            sizeBar.setValue(value);
            messagesCell.invalidate();
        }

        @Override
        public void invalidate() {
            super.invalidate();
            messagesCell.invalidate();
        }
    }

    @SuppressLint("ViewConstructor")
    private static class AltSeekbar extends FrameLayout {

        private final AnimatedTextView headerValue;
        private final TextView leftTextView;
        private final TextView rightTextView;
        private final SeekBarView seekBarView;
        private final Theme.ResourcesProvider resourcesProvider;

        private final int min, max;
        private float currentValue;
        private int roundedValue;

        public interface OnDrag {
            void run(float progress);
        }

        public AltSeekbar(Context context, AltSeekbar.OnDrag onDrag, int min, int max, String title, String left, String right, Theme.ResourcesProvider resourcesProvider) {
            super(context);
            this.resourcesProvider = resourcesProvider;

            this.max = max;
            this.min = min;

            LinearLayout headerLayout = new LinearLayout(context);
            headerLayout.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);

            TextView headerTextView = new TextView(context);
            headerTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
            headerTextView.setTypeface(AndroidUtilities.getTypeface(AndroidUtilities.TYPEFACE_ROBOTO_MEDIUM));
            headerTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueHeader, resourcesProvider));
            headerTextView.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
            headerTextView.setText(title);
            headerLayout.addView(headerTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL));

            headerValue = new AnimatedTextView(context, false, true, true) {
                final Drawable backgroundDrawable = Theme.createRoundRectDrawable(AndroidUtilities.dp(4), Theme.multAlpha(Theme.getColor(Theme.key_windowBackgroundWhiteBlueHeader, resourcesProvider), 0.15f));

                @Override
                protected void onDraw(Canvas canvas) {
                    backgroundDrawable.setBounds(0, 0, (int) (getPaddingLeft() + getDrawable().getCurrentWidth() + getPaddingRight()), getMeasuredHeight());
                    backgroundDrawable.draw(canvas);

                    super.onDraw(canvas);
                }
            };
            headerValue.setAnimationProperties(.45f, 0, 240, CubicBezierInterpolator.EASE_OUT_QUINT);
            headerValue.setTypeface(AndroidUtilities.getTypeface(AndroidUtilities.TYPEFACE_ROBOTO_MEDIUM));
            headerValue.setPadding(AndroidUtilities.dp(5.33f), AndroidUtilities.dp(2), AndroidUtilities.dp(5.33f), AndroidUtilities.dp(2));
            headerValue.setTextSize(AndroidUtilities.dp(12));
            headerValue.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueHeader, resourcesProvider));
            headerLayout.addView(headerValue, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, 17, Gravity.CENTER_VERTICAL, 6, 1, 0, 0));

            addView(headerLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.FILL_HORIZONTAL, 21, 17, 21, 0));

            seekBarView = new SeekBarView(context, true, resourcesProvider);
            seekBarView.setReportChanges(true);
            seekBarView.setDelegate((stop, progress) -> {
                currentValue = min + (max - min) * progress;
                onDrag.run(currentValue);
                if (Math.round(currentValue) != roundedValue) {
                    roundedValue = Math.round(currentValue);
                    updateText();
                }
            });
            addView(seekBarView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 38 + 6, Gravity.TOP, 6, 68, 6, 0));

            FrameLayout valuesView = new FrameLayout(context);

            leftTextView = new TextView(context);
            leftTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
            leftTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText, resourcesProvider));
            leftTextView.setGravity(Gravity.LEFT);
            leftTextView.setText(left);
            valuesView.addView(leftTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.CENTER_VERTICAL));

            rightTextView = new TextView(context);
            rightTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
            rightTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText, resourcesProvider));
            rightTextView.setGravity(Gravity.RIGHT);
            rightTextView.setText(right);
            valuesView.addView(rightTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.RIGHT | Gravity.CENTER_VERTICAL));

            addView(valuesView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.FILL_HORIZONTAL, 21, 52, 21, 0));
        }

        private void updateValues() {
            int middle = (max - min) / 2 + min;
            if (currentValue >= middle * 1.5f - min * 0.5f) {
                rightTextView.setTextColor(ColorUtils.blendARGB(
                        Theme.getColor(Theme.key_windowBackgroundWhiteGrayText, resourcesProvider),
                        Theme.getColor(Theme.key_windowBackgroundWhiteBlueText, resourcesProvider),
                        (currentValue - (middle * 1.5f - min * 0.5f)) / (max - (middle * 1.5f - min * 0.5f))
                ));
                leftTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText, resourcesProvider));
            } else if (currentValue <= (middle + min) * 0.5f) {
                leftTextView.setTextColor(ColorUtils.blendARGB(
                        Theme.getColor(Theme.key_windowBackgroundWhiteGrayText, resourcesProvider),
                        Theme.getColor(Theme.key_windowBackgroundWhiteBlueText, resourcesProvider),
                        (currentValue - (middle + min) * 0.5f) / (min - (middle + min) * 0.5f)
                ));
                rightTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText, resourcesProvider));
            } else {
                leftTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText, resourcesProvider));
                rightTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText, resourcesProvider));
            }
        }

        public void setValue(float value) {
            currentValue = value;
            seekBarView.setProgress((value - min) / (float) (max - min));
            if (Math.round(currentValue) != roundedValue) {
                roundedValue = Math.round(currentValue);
                updateText();
            }
        }

        private void updateText() {
            headerValue.cancelAnimation();
            headerValue.setText(getTextForHeader(), true);
            updateValues();
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(
                    MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(112), MeasureSpec.EXACTLY)
            );
        }

        public CharSequence getTextForHeader() {
            CharSequence text;
            if (roundedValue == min) {
                text = leftTextView.getText();
            } else if (roundedValue == max) {
                text = rightTextView.getText();
            } else {
                text = String.valueOf(roundedValue);
            }
            return text.toString().toUpperCase();
        }
    }
}
