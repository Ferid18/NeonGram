package org.telegram.ui.Components;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.Utilities.clamp01;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Stories.recorder.ButtonWithCounterView;

import java.util.Date;

@SuppressWarnings("FieldCanBeLocal")
public class ProfileRelationshipView extends FrameLayout {

    public static final int PANEL_HEIGHT_DP = 182;
    public static final int TYPE_ROMANTIC = 0;
    public static final int TYPE_FRIEND = 1;

    public interface Delegate {
        void openProfile(long userId);
    }

    public static class RelationshipData {
        public int type = TYPE_ROMANTIC;
        public long partnerUserId;
        public TLRPC.User partnerUser;
        public String partnerFirstName;
        public String partnerLastName;
        public String partnerDisplayName;
        public boolean partnerVerified;
        public boolean partnerPremium;
        public long sinceDate;
        public String confirmText;
        public String sinceText;
        public String durationText;
        public String visibilityText;
        public String noteText;
        public String streakTitle;
        public String streakValue;
        public String anniversaryTitle;
        public String anniversaryValue;
        public String statusTitle;
        public String statusValue;
    }

    private final Theme.ResourcesProvider resourcesProvider;
    private final FrameLayout panelView;
    private final FrameLayout avatarHolder;
    private final BackupImageView avatarView;
    private final AvatarDrawable avatarDrawable;
    private final TextView badgeView;
    private final TextView partnerNameView;
    private final ImageView premiumView;
    private final ImageView verifiedView;
    private final ImageView confirmIconView;
    private final TextView confirmView;
    private final TextView dateView;
    private final TextView durationChipView;
    private final TextView visibilityChipView;
    private final TextView noteView;
    private final TextView titleView;
    private final LinearLayout contentRow;
    private final FrameLayout artHolder;
    private final ImageView confettiView;
    private final ImageView artIconView;
    private final LinearLayout statsContainer;
    private final StatView streakView;
    private final StatView anniversaryView;
    private final StatView statusView;
    private final LinearLayout emptyStateView;
    private final FrameLayout emptyIconHolder;
    private final ImageView emptyIconView;
    private final TextView emptyTitleView;
    private final TextView emptyTextView;

    private float currentHeight;
    private float parentExpanded;
    private int currentType = TYPE_ROMANTIC;
    private RelationshipData currentData;
    private Delegate delegate;

    public ProfileRelationshipView(Context context, Theme.ResourcesProvider resourcesProvider) {
        super(context);
        this.resourcesProvider = resourcesProvider;

        setClipChildren(false);
        setClipToPadding(false);
        setVisibility(GONE);

        panelView = new FrameLayout(context);
        panelView.setBackground(createPanelBackground());
        if (AndroidUtilities.isTablet() || android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            panelView.setElevation(dp(5));
        }
        addView(panelView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, PANEL_HEIGHT_DP, Gravity.TOP | Gravity.FILL_HORIZONTAL, 14, 0, 14, 0));

        titleView = new TextView(context);
        styleTextView(titleView);
        titleView.setText(getPanelTitle(TYPE_ROMANTIC));
        titleView.setTypeface(AndroidUtilities.bold());
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        panelView.addView(titleView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.LEFT, 16, 14, 16, 0));

        visibilityChipView = createTopChipTextView(context);
        visibilityChipView.setText(LocaleController.getString(R.string.RelationshipPanelVisibilityPublic));
        panelView.addView(visibilityChipView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, 24, Gravity.TOP | Gravity.RIGHT, 0, 13, 14, 0));

        contentRow = new LinearLayout(context);
        contentRow.setOrientation(LinearLayout.HORIZONTAL);
        panelView.addView(contentRow, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.LEFT, 14, 40, 14, 0));

        avatarHolder = new FrameLayout(context);
        avatarHolder.setBackground(createCircleStrokeDrawable());
        contentRow.addView(avatarHolder, LayoutHelper.createLinear(72, 72, Gravity.TOP));

        avatarView = new BackupImageView(context);
        avatarView.setRoundRadius(dp(31));
        avatarDrawable = new AvatarDrawable();
        avatarHolder.addView(avatarView, LayoutHelper.createFrame(62, 62, Gravity.CENTER));

        badgeView = new TextView(context);
        badgeView.setText("\u2764");
        badgeView.setGravity(Gravity.CENTER);
        badgeView.setTextColor(Color.WHITE);
        badgeView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
        badgeView.setTypeface(AndroidUtilities.bold());
        badgeView.setBackground(Theme.createSimpleSelectorCircleDrawable(dp(24), getAccentColorSecondary(), getAccentPressedColor()));
        avatarHolder.addView(badgeView, LayoutHelper.createFrame(24, 24, Gravity.RIGHT | Gravity.BOTTOM, 0, 0, 0, 0));

        LinearLayout textColumn = new LinearLayout(context);
        textColumn.setOrientation(LinearLayout.VERTICAL);
        contentRow.addView(textColumn, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1f, Gravity.TOP, 10, 2, 4, 0));

        LinearLayout nameRow = new LinearLayout(context);
        nameRow.setOrientation(LinearLayout.HORIZONTAL);
        nameRow.setGravity(Gravity.CENTER_VERTICAL);
        textColumn.addView(nameRow, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        partnerNameView = new TextView(context);
        styleTextView(partnerNameView);
        partnerNameView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText, resourcesProvider));
        partnerNameView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
        partnerNameView.setTypeface(AndroidUtilities.bold());
        partnerNameView.setMaxLines(1);
        partnerNameView.setEllipsize(TextUtils.TruncateAt.END);
        nameRow.addView(partnerNameView, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1f));

        premiumView = new ImageView(context);
        premiumView.setImageDrawable(createPremiumDrawable());
        nameRow.addView(premiumView, LayoutHelper.createLinear(16, 16, Gravity.CENTER_VERTICAL, 4, 1, 0, 0));

        verifiedView = new ImageView(context);
        verifiedView.setImageDrawable(createVerifiedDrawable());
        nameRow.addView(verifiedView, LayoutHelper.createLinear(18, 18, Gravity.CENTER_VERTICAL, 6, 1, 0, 0));

        LinearLayout confirmRow = new LinearLayout(context);
        confirmRow.setOrientation(LinearLayout.HORIZONTAL);
        confirmRow.setGravity(Gravity.CENTER_VERTICAL);
        textColumn.addView(confirmRow, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT, 0, 5, 0, 0));

        confirmIconView = new ImageView(context);
        confirmIconView.setImageDrawable(createConfirmDrawable());
        confirmRow.addView(confirmIconView, LayoutHelper.createLinear(14, 14, Gravity.CENTER_VERTICAL));

        confirmView = createMetaTextView(context, getAccentColor(), 12, AndroidUtilities.bold());
        confirmView.setText(LocaleController.getString(R.string.RelationshipPanelConfirmed));
        confirmView.setMaxLines(1);
        confirmView.setEllipsize(TextUtils.TruncateAt.END);
        confirmRow.addView(confirmView, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1f, Gravity.CENTER_VERTICAL, 6, 0, 0, 0));

        dateView = createMetaTextView(context, getPrimaryTextColor(), 12, null);

        durationChipView = createChipTextView(context);
        textColumn.addView(durationChipView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, 24, Gravity.LEFT, 0, 7, 0, 0));

        noteView = createMetaTextView(context, getSecondaryTextColor(), 11, null);
        noteView.setMaxLines(1);
        noteView.setEllipsize(TextUtils.TruncateAt.END);
        textColumn.addView(noteView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT, 0, 5, 0, 0));

        artHolder = new FrameLayout(context);
        artHolder.setBackground(createHeartArtBackground());
        contentRow.addView(artHolder, LayoutHelper.createLinear(70, 78, Gravity.TOP));

        confettiView = new ImageView(context);
        confettiView.setImageResource(R.drawable.heart_confetti);
        confettiView.setScaleType(ImageView.ScaleType.FIT_XY);
        confettiView.setAlpha(0.58f);
        artHolder.addView(confettiView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        artIconView = new ImageView(context);
        artIconView.setImageResource(R.drawable.emoji_love);
        artIconView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        artIconView.setPadding(dp(8), dp(8), dp(8), dp(8));
        artIconView.setAlpha(1f);
        artHolder.addView(artIconView, LayoutHelper.createFrame(50, 50, Gravity.CENTER, 0, 3, 0, 0));

        ImageView rightArrowView = new ImageView(context);
        rightArrowView.setImageResource(R.drawable.msg_arrowright);
        rightArrowView.setScaleType(ImageView.ScaleType.CENTER);
        rightArrowView.setColorFilter(new PorterDuffColorFilter(ColorUtils.setAlphaComponent(getSecondaryTextColor(), 170), PorterDuff.Mode.SRC_IN));
        artHolder.addView(rightArrowView, LayoutHelper.createFrame(16, 16, Gravity.RIGHT | Gravity.CENTER_VERTICAL, 0, 0, 3, 0));

        statsContainer = new LinearLayout(context);
        statsContainer.setOrientation(LinearLayout.HORIZONTAL);
        statsContainer.setPadding(dp(4), dp(3), dp(4), dp(3));
        statsContainer.setBackground(createStatsBackground());
        panelView.addView(statsContainer, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 58, Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 8, 0, 8, 8));

        streakView = new StatView(context, resourcesProvider, R.drawable.filled_fire, 0xFFFF6A54, false);
        anniversaryView = new StatView(context, resourcesProvider, R.drawable.msg_calendar2, 0xFFFF4D96, false);
        statusView = new StatView(context, resourcesProvider, R.drawable.emoji_love, getStatusIconColor(), true);

        statsContainer.addView(streakView, LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 1f));
        statsContainer.addView(createDivider(context), LayoutHelper.createLinear(1, 36, Gravity.CENTER_VERTICAL, 2, 0, 2, 0));
        statsContainer.addView(anniversaryView, LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 1f));
        statsContainer.addView(createDivider(context), LayoutHelper.createLinear(1, 36, Gravity.CENTER_VERTICAL, 2, 0, 2, 0));
        statsContainer.addView(statusView, LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 1f));

        emptyStateView = new LinearLayout(context);
        emptyStateView.setOrientation(LinearLayout.HORIZONTAL);
        emptyStateView.setGravity(Gravity.CENTER_VERTICAL);
        emptyStateView.setPadding(dp(12), 0, dp(12), 0);
        emptyStateView.setVisibility(GONE);
        panelView.addView(emptyStateView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 112, Gravity.TOP | Gravity.LEFT, 16, 50, 16, 0));

        emptyIconHolder = new FrameLayout(context);
        emptyStateView.addView(emptyIconHolder, LayoutHelper.createLinear(48, 48, Gravity.CENTER_VERTICAL));

        emptyIconView = new ImageView(context);
        emptyIconView.setImageResource(R.drawable.emoji_love);
        emptyIconView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        emptyIconHolder.addView(emptyIconView, LayoutHelper.createFrame(26, 26, Gravity.CENTER));

        LinearLayout emptyTextColumn = new LinearLayout(context);
        emptyTextColumn.setOrientation(LinearLayout.VERTICAL);
        emptyStateView.addView(emptyTextColumn, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1f, Gravity.CENTER_VERTICAL, 12, 0, 0, 0));

        emptyTitleView = new TextView(context);
        styleTextView(emptyTitleView);
        emptyTitleView.setTypeface(AndroidUtilities.bold());
        emptyTitleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        emptyTitleView.setMaxLines(1);
        emptyTitleView.setEllipsize(TextUtils.TruncateAt.END);
        emptyTextColumn.addView(emptyTitleView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        emptyTextView = new TextView(context);
        styleTextView(emptyTextView);
        emptyTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        emptyTextView.setLineSpacing(dp(2), 1.0f);
        emptyTextView.setMaxLines(3);
        emptyTextView.setEllipsize(TextUtils.TruncateAt.END);
        emptyTextColumn.addView(emptyTextView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT, 0, 7, 0, 0));

        panelView.setOnClickListener(v -> showRelationshipSheet());
        statusView.setOnClickListener(v -> showStatusSheet());

        applyCurrentPalette();
    }

    public void setDelegate(Delegate delegate) {
        this.delegate = delegate;
    }

    public void clear() {
        currentData = null;
        setPanelMode(false, false);
        setVisibility(GONE);
        setAlpha(0f);
    }

    public void showEmpty(String title, String text) {
        currentData = null;
        currentType = TYPE_ROMANTIC;
        setVisibility(VISIBLE);
        setPanelMode(false, true);
        titleView.setText(LocaleController.getString(R.string.RelationshipPanelTitle));
        emptyTitleView.setText(title);
        emptyTextView.setText(text);
        applyCurrentPalette();
    }

    public void bind(RelationshipData data) {
        if (data == null) {
            clear();
            return;
        }
        currentData = data;
        currentType = data.type;
        setVisibility(VISIBLE);
        setPanelMode(true, false);
        applyCurrentPalette();
        TLRPC.User partner = getPartnerUser(data);
        avatarDrawable.setInfo(partner);
        avatarView.setForUserOrChat(partner, avatarDrawable);

        partnerNameView.setText(getPartnerDisplayName(data));
        premiumView.setVisibility(data.partnerPremium ? VISIBLE : GONE);
        verifiedView.setVisibility(data.partnerVerified ? VISIBLE : GONE);
        titleView.setText(getPanelTitle(data.type));
        badgeView.setText(getBadgeSymbol(data.type));
        confirmView.setText(!TextUtils.isEmpty(data.confirmText) ? data.confirmText : LocaleController.getString(R.string.RelationshipPanelConfirmed));
        dateView.setText(!TextUtils.isEmpty(data.sinceText) ? data.sinceText : formatSinceDate(data.sinceDate));
        durationChipView.setText(data.durationText);
        visibilityChipView.setText(data.visibilityText);
        if (TextUtils.isEmpty(data.noteText)) {
            noteView.setVisibility(GONE);
            noteView.setText(null);
        } else {
            noteView.setVisibility(VISIBLE);
            noteView.setText(data.noteText);
        }
        streakView.set(data.streakTitle, data.streakValue);
        anniversaryView.set(data.anniversaryTitle, !TextUtils.isEmpty(data.anniversaryValue) ? data.anniversaryValue : formatAnniversaryDate(data.sinceDate));
        statusView.setIcon(getStatusIconRes(data.type), getStatusIconColor());
        statusView.set(data.statusTitle, !TextUtils.isEmpty(data.statusValue) ? data.statusValue : getStatusValue(data.type));
    }

    private void setPanelMode(boolean showContent, boolean showEmpty) {
        contentRow.setVisibility(showContent ? VISIBLE : GONE);
        statsContainer.setVisibility(showContent ? VISIBLE : GONE);
        visibilityChipView.setVisibility(showContent ? VISIBLE : GONE);
        emptyStateView.setVisibility(showEmpty ? VISIBLE : GONE);
    }

    public void setParentExpanded(float expanded) {
        if (parentExpanded != expanded) {
            parentExpanded = expanded;
            invalidate();
        }
    }

    public void updatePosition(float y, float newHeight) {
        currentHeight = newHeight;
        setTranslationY(y);
        setAlpha(clamp01(newHeight / (float) dp(PANEL_HEIGHT_DP)));
        invalidate();
    }

    @Override
    protected void dispatchDraw(@NonNull Canvas canvas) {
        if (currentHeight <= 0) {
            return;
        }
        final int save = canvas.save();
        canvas.clipRect(0, 0, getMeasuredWidth(), Math.min(getMeasuredHeight(), currentHeight));
        super.dispatchDraw(canvas);
        canvas.restoreToCount(save);
    }

    private boolean isDarkTheme() {
        return Theme.isCurrentThemeDark();
    }

    private int getPrimaryTextColor() {
        return Theme.getColor(Theme.key_windowBackgroundWhiteBlackText, resourcesProvider);
    }

    private int getSecondaryTextColor() {
        return Theme.getColor(Theme.key_windowBackgroundWhiteGrayText, resourcesProvider);
    }

    private int getAccentColor() {
        if (currentType == TYPE_FRIEND) {
            return isDarkTheme() ? 0xFF82B8FF : 0xFF4F88F7;
        }
        return isDarkTheme() ? 0xFFF18BBB : 0xFFE34C93;
    }

    private int getAccentColorSecondary() {
        if (currentType == TYPE_FRIEND) {
            return isDarkTheme() ? 0xFF6DA7F7 : 0xFF6FA8FF;
        }
        return isDarkTheme() ? 0xFFEA72AD : 0xFFFF4B97;
    }

    private int getAccentPressedColor() {
        return ColorUtils.blendARGB(getAccentColorSecondary(), Color.BLACK, isDarkTheme() ? 0.08f : 0.14f);
    }

    private int getChipBackgroundColor() {
        final int base = Theme.getColor(isDarkTheme() ? Theme.key_dialogBackground : Theme.key_windowBackgroundWhite, resourcesProvider);
        return ColorUtils.blendARGB(base, getAccentColor(), isDarkTheme() ? 0.18f : 0.12f);
    }

    private int getChipPressedColor() {
        return ColorUtils.blendARGB(getChipBackgroundColor(), getAccentColor(), isDarkTheme() ? 0.24f : 0.2f);
    }

    private int getVisibilityChipBackgroundColor() {
        final int base = Theme.getColor(isDarkTheme() ? Theme.key_dialogBackground : Theme.key_windowBackgroundWhite, resourcesProvider);
        return ColorUtils.blendARGB(base, getAccentColor(), isDarkTheme() ? 0.14f : 0.08f);
    }

    private int getVisibilityChipPressedColor() {
        return ColorUtils.blendARGB(getVisibilityChipBackgroundColor(), getAccentColor(), isDarkTheme() ? 0.22f : 0.16f);
    }

    private int getSheetRowBackgroundColor() {
        final int base = Theme.getColor(isDarkTheme() ? Theme.key_dialogBackgroundGray : Theme.key_windowBackgroundWhite, resourcesProvider);
        return ColorUtils.blendARGB(base, getAccentColor(), isDarkTheme() ? 0.09f : 0.025f);
    }

    private int getSheetRowPressedColor() {
        return ColorUtils.blendARGB(getSheetRowBackgroundColor(), Theme.getColor(Theme.key_listSelector, resourcesProvider), isDarkTheme() ? 0.55f : 0.35f);
    }

    private String formatSinceDate(long sinceDate) {
        if (sinceDate <= 0) {
            return "";
        }
        return LocaleController.formatString(R.string.RelationshipPanelSinceCompact, LocaleController.getInstance().getFormatterGiveawayMonthDayYear().format(new Date(sinceDate)));
    }

    private String formatAnniversaryDate(long sinceDate) {
        if (sinceDate <= 0) {
            return "";
        }
        return LocaleController.getInstance().getFormatterGiveawayMonthDay().format(new Date(sinceDate));
    }

    private void applyCurrentPalette() {
        panelView.setBackground(createPanelBackground());
        avatarHolder.setBackground(createCircleStrokeDrawable());
        badgeView.setBackground(Theme.createSimpleSelectorCircleDrawable(dp(24), getAccentColorSecondary(), getAccentPressedColor()));
        titleView.setTextColor(getAccentColor());
        confirmIconView.setImageDrawable(createConfirmDrawable());
        confirmView.setTextColor(getAccentColor());
        durationChipView.setTextColor(getAccentColor());
        durationChipView.setBackground(Theme.createSimpleSelectorRoundRectDrawable(dp(12), getChipBackgroundColor(), getChipPressedColor()));
        visibilityChipView.setTextColor(getAccentColor());
        visibilityChipView.setBackground(Theme.createSimpleSelectorRoundRectDrawable(dp(12), getVisibilityChipBackgroundColor(), getVisibilityChipPressedColor()));
        noteView.setTextColor(getSecondaryTextColor());
        emptyIconHolder.setBackground(createEmptyIconBackground());
        emptyIconView.setColorFilter(new PorterDuffColorFilter(getAccentColorSecondary(), PorterDuff.Mode.SRC_IN));
        emptyTitleView.setTextColor(getPrimaryTextColor());
        emptyTextView.setTextColor(getSecondaryTextColor());
        artHolder.setBackground(createHeartArtBackground());
        confettiView.setVisibility(currentType == TYPE_FRIEND ? GONE : VISIBLE);
        confettiView.setAlpha(isDarkTheme() ? 0.7f : 0.82f);
        confettiView.setColorFilter(new PorterDuffColorFilter(ColorUtils.setAlphaComponent(getAccentColorSecondary(), isDarkTheme() ? 210 : 230), PorterDuff.Mode.SRC_IN));
        artIconView.setBackground(createArtIconBackground());
        artIconView.setImageResource(getStatusIconRes(currentType));
        artIconView.setColorFilter(new PorterDuffColorFilter(currentType == TYPE_FRIEND ? getAccentColor() : getAccentColorSecondary(), PorterDuff.Mode.SRC_IN));
        statsContainer.setBackground(createStatsBackground());
        anniversaryView.setIcon(R.drawable.msg_calendar2, getAccentColorSecondary());
        statusView.setIcon(getStatusIconRes(currentType), getStatusIconColor());
    }

    private String getPanelTitle(int type) {
        return LocaleController.getString(type == TYPE_FRIEND ? R.string.RelationshipPanelTitleFriend : R.string.RelationshipPanelTitleRomantic);
    }

    private String getStatusValue(int type) {
        return LocaleController.getString(type == TYPE_FRIEND ? R.string.RelationshipPanelStatusFriend : R.string.RelationshipPanelStatusDating);
    }

    private int getStatusIconRes(int type) {
        return type == TYPE_FRIEND ? R.drawable.msg_mini_closefriends : R.drawable.emoji_love;
    }

    private int getStatusIconColor() {
        return currentType == TYPE_FRIEND ? getAccentColor() : 0xFFFF4D96;
    }

    private String getBadgeSymbol(int type) {
        return type == TYPE_FRIEND ? "\u2605" : "\u2764";
    }

    private TLRPC.User getPartnerUser(RelationshipData data) {
        if (data != null && data.partnerUser != null) {
            return data.partnerUser;
        }
        TLRPC.TL_user partner = new TLRPC.TL_user();
        if (data != null) {
            partner.id = data.partnerUserId;
            partner.first_name = data.partnerFirstName;
            partner.last_name = data.partnerLastName;
        }
        return partner;
    }

    private String getPartnerDisplayName(RelationshipData data) {
        if (data == null) {
            return "";
        }
        if (!TextUtils.isEmpty(data.partnerDisplayName)) {
            return data.partnerDisplayName;
        }
        String displayName = formatDisplayName(data.partnerFirstName, data.partnerLastName);
        return !TextUtils.isEmpty(displayName) ? displayName : "";
    }

    private String formatDisplayName(String firstName, String lastName) {
        if (!TextUtils.isEmpty(firstName) && !TextUtils.isEmpty(lastName)) {
            return firstName + " " + lastName;
        }
        if (!TextUtils.isEmpty(firstName)) {
            return firstName;
        }
        return lastName;
    }

    private void showRelationshipSheet() {
        if (currentData == null || getContext() == null) {
            return;
        }

        BottomSheet.Builder builder = new BottomSheet.Builder(getContext(), false, resourcesProvider);
        Runnable dismiss = builder.getDismissRunnable();

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setClipChildren(false);
        layout.setClipToPadding(false);

        FrameLayout topAvatarHolder = new FrameLayout(getContext());
        topAvatarHolder.setBackground(createCircleStrokeDrawable());
        BackupImageView topAvatarView = new BackupImageView(getContext());
        topAvatarView.setRoundRadius(dp(32));
        AvatarDrawable topAvatarDrawable = new AvatarDrawable();
        TLRPC.User partner = getPartnerUser(currentData);
        topAvatarDrawable.setInfo(partner);
        topAvatarView.setForUserOrChat(partner, topAvatarDrawable);
        topAvatarHolder.addView(topAvatarView, LayoutHelper.createFrame(64, 64, Gravity.CENTER));
        layout.addView(topAvatarHolder, LayoutHelper.createLinear(74, 74, Gravity.CENTER_HORIZONTAL, 0, 18, 0, 8));

        TextView title = new TextView(getContext());
        styleTextView(title);
        title.setText(getPartnerDisplayName(currentData));
        title.setTypeface(AndroidUtilities.bold());
        title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText, resourcesProvider));
        layout.addView(title, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 20, 0, 20, 6));

        TextView subtitle = new TextView(getContext());
        styleTextView(subtitle);
        subtitle.setText(LocaleController.getString(R.string.RelationshipSheetSubtitle));
        subtitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText, resourcesProvider));
        layout.addView(subtitle, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 20, 0, 20, 14));

        layout.addView(createSheetRow(R.drawable.msg_calendar2,
                LocaleController.getString(R.string.RelationshipSheetSinceTitle),
                !TextUtils.isEmpty(currentData.sinceText) ? currentData.sinceText : LocaleController.formatString(R.string.RelationshipPanelTogetherSince, LocaleController.getInstance().getFormatterGiveawayMonthDayYear().format(new Date(currentData.sinceDate))),
                LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 10, 0, 10, 0)));
        layout.addView(createSheetRow(R.drawable.filled_fire,
                LocaleController.getString(R.string.RelationshipSheetTogetherTitle),
                currentData.streakValue,
                LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 10, 0, 10, 0)));
        layout.addView(createSheetRow(R.drawable.msg_mini_stickerstar,
                LocaleController.getString(R.string.RelationshipSheetVisibilityTitle),
                currentData.visibilityText,
                LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 10, 0, 10, 0)));
        layout.addView(createSheetRow(getStatusIconRes(currentData.type),
                LocaleController.getString(R.string.RelationshipSheetStatusTitle),
                !TextUtils.isEmpty(currentData.statusValue) ? currentData.statusValue : getStatusValue(currentData.type),
                LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 10, 0, 10, 4)));

        if (currentData.partnerUserId != 0) {
            ButtonWithCounterView button = new ButtonWithCounterView(getContext(), resourcesProvider);
            button.setRound();
            button.setText(LocaleController.getString(R.string.RelationshipSheetOpenProfile), false);
            button.setOnClickListener(v -> {
                dismiss.run();
                if (delegate != null) {
                    delegate.openProfile(currentData.partnerUserId);
                }
            });
            layout.addView(button, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48, 16, 14, 16, 8));
        }

        builder.setCustomView(layout);
        builder.show();
    }

    private void showStatusSheet() {
        if (currentData == null || getContext() == null) {
            return;
        }

        BottomSheet.Builder builder = new BottomSheet.Builder(getContext(), false, resourcesProvider);
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setClipChildren(false);
        layout.setClipToPadding(false);

        TextView title = new TextView(getContext());
        styleTextView(title);
        title.setText(LocaleController.getString(R.string.RelationshipStatusSheetTitle));
        title.setTypeface(AndroidUtilities.bold());
        title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText, resourcesProvider));
        layout.addView(title, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 20, 20, 20, 6));

        TextView subtitle = new TextView(getContext());
        styleTextView(subtitle);
        subtitle.setText(LocaleController.getString(R.string.RelationshipStatusSheetSubtitle));
        subtitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText, resourcesProvider));
        layout.addView(subtitle, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 20, 0, 20, 14));

        layout.addView(createSheetRow(getStatusIconRes(currentData.type),
                LocaleController.getString(R.string.RelationshipStatusSheetCurrent),
                !TextUtils.isEmpty(currentData.statusValue) ? currentData.statusValue : getStatusValue(currentData.type),
                LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 10, 0, 10, 0)));
        layout.addView(createSheetRow(R.drawable.verified_check,
                LocaleController.getString(R.string.RelationshipStatusSheetConfirmation),
                currentData.confirmText,
                LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 10, 0, 10, 0)));
        layout.addView(createSheetRow(R.drawable.msg_mini_stickerstar,
                LocaleController.getString(R.string.RelationshipStatusSheetVisibility),
                currentData.visibilityText,
                LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 10, 0, 10, 10)));

        builder.setCustomView(layout);
        builder.show();
    }

    private View createSheetRow(int iconRes, String title, String value, LinearLayout.LayoutParams params) {
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(12), dp(10), dp(12), dp(10));
        row.setBackground(Theme.createSimpleSelectorRoundRectDrawable(dp(14), getSheetRowBackgroundColor(), getSheetRowPressedColor()));

        FrameLayout iconWrap = new FrameLayout(getContext());
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(ColorUtils.setAlphaComponent(getAccentColor(), isDarkTheme() ? 52 : 20));
        iconWrap.setBackground(bg);
        row.addView(iconWrap, LayoutHelper.createLinear(34, 34, Gravity.CENTER_VERTICAL));

        ImageView icon = new ImageView(getContext());
        icon.setImageResource(iconRes);
        icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        icon.setColorFilter(new PorterDuffColorFilter(getAccentColor(), PorterDuff.Mode.SRC_IN));
        iconWrap.addView(icon, LayoutHelper.createFrame(18, 18, Gravity.CENTER));

        LinearLayout textColumn = new LinearLayout(getContext());
        textColumn.setOrientation(LinearLayout.VERTICAL);
        row.addView(textColumn, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1f, Gravity.CENTER_VERTICAL, 10, 0, 0, 0));

        TextView titleView = new TextView(getContext());
        styleTextView(titleView);
        titleView.setText(title);
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
        titleView.setTextColor(getSecondaryTextColor());
        textColumn.addView(titleView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));

        TextView valueView = new TextView(getContext());
        styleTextView(valueView);
        valueView.setText(value);
        valueView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        valueView.setTypeface(AndroidUtilities.bold());
        valueView.setTextColor(getPrimaryTextColor());
        textColumn.addView(valueView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT, 0, 2, 0, 0));

        row.setLayoutParams(params);
        return row;
    }

    private TextView createMetaTextView(Context context, int color, int textSizeDp, android.graphics.Typeface typeface) {
        TextView textView = new TextView(context);
        styleTextView(textView);
        textView.setTextColor(color);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSizeDp);
        if (typeface != null) {
            textView.setTypeface(typeface);
        }
        return textView;
    }

    private TextView createChipTextView(Context context) {
        TextView textView = new TextView(context);
        styleTextView(textView);
        textView.setPadding(dp(10), 0, dp(10), 0);
        textView.setTextColor(getAccentColor());
        textView.setTypeface(AndroidUtilities.bold());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 11);
        textView.setGravity(Gravity.CENTER_VERTICAL);
        textView.setBackground(Theme.createSimpleSelectorRoundRectDrawable(dp(12), getChipBackgroundColor(), getChipPressedColor()));
        return textView;
    }

    private TextView createTopChipTextView(Context context) {
        TextView textView = new TextView(context);
        styleTextView(textView);
        textView.setPadding(dp(8), 0, dp(8), 0);
        textView.setTextColor(getAccentColor());
        textView.setTypeface(AndroidUtilities.bold());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
        textView.setGravity(Gravity.CENTER);
        textView.setBackground(Theme.createSimpleSelectorRoundRectDrawable(dp(12), getVisibilityChipBackgroundColor(), getVisibilityChipPressedColor()));
        return textView;
    }

    private void styleTextView(TextView textView) {
        textView.setIncludeFontPadding(false);
    }

    private View createDivider(Context context) {
        View divider = new View(context);
        divider.setBackgroundColor(ColorUtils.setAlphaComponent(getSecondaryTextColor(), isDarkTheme() ? 44 : 24));
        return divider;
    }

    private Drawable createVerifiedDrawable() {
        Drawable verifiedBackground = ContextCompat.getDrawable(getContext(), R.drawable.verified_area).mutate();
        verifiedBackground.setColorFilter(new PorterDuffColorFilter(0xFF2B87F3, PorterDuff.Mode.MULTIPLY));
        Drawable verifiedCheck = ContextCompat.getDrawable(getContext(), R.drawable.verified_check).mutate();
        verifiedCheck.setColorFilter(new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY));
        return new CombinedDrawable(verifiedBackground, verifiedCheck);
    }

    private Drawable createConfirmDrawable() {
        GradientDrawable background = new GradientDrawable();
        background.setShape(GradientDrawable.OVAL);
        background.setColor(getAccentColorSecondary());

        Drawable verifiedCheck = ContextCompat.getDrawable(getContext(), R.drawable.verified_check).mutate();
        verifiedCheck.setColorFilter(new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY));
        return new CombinedDrawable(background, verifiedCheck);
    }

    private Drawable createPremiumDrawable() {
        Drawable premiumDrawable = ContextCompat.getDrawable(getContext(), R.drawable.msg_premium_liststar).mutate();
        premiumDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_profile_verifiedBackground, resourcesProvider), PorterDuff.Mode.MULTIPLY));
        return premiumDrawable;
    }

    private Drawable createPanelBackground() {
        final int baseColor = Theme.getColor(isDarkTheme() ? Theme.key_dialogBackground : Theme.key_windowBackgroundWhite, resourcesProvider);
        final int accent = getAccentColor();
        GradientDrawable drawable = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{
                        ColorUtils.blendARGB(baseColor, accent, isDarkTheme() ? 0.08f : 0.03f),
                        ColorUtils.blendARGB(baseColor, accent, isDarkTheme() ? 0.12f : 0.06f),
                        ColorUtils.blendARGB(baseColor, accent, isDarkTheme() ? 0.1f : 0.04f)
                }
        );
        drawable.setCornerRadius(dp(26));
        drawable.setStroke(dp(1), ColorUtils.setAlphaComponent(accent, isDarkTheme() ? 48 : 20));
        return drawable;
    }

    private Drawable createStatsBackground() {
        final int baseColor = Theme.getColor(isDarkTheme() ? Theme.key_dialogBackground : Theme.key_windowBackgroundWhite, resourcesProvider);
        final int accent = getAccentColor();
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(ColorUtils.blendARGB(baseColor, accent, isDarkTheme() ? 0.06f : 0.02f));
        drawable.setCornerRadius(dp(20));
        drawable.setStroke(dp(1), ColorUtils.setAlphaComponent(getSecondaryTextColor(), isDarkTheme() ? 38 : 18));
        return drawable;
    }

    private Drawable createHeartArtBackground() {
        final int accent = getAccentColor();
        final int accentSecondary = getAccentColorSecondary();
        GradientDrawable drawable = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{
                        ColorUtils.setAlphaComponent(accentSecondary, isDarkTheme() ? 70 : 34),
                        ColorUtils.setAlphaComponent(accent, isDarkTheme() ? 30 : 14)
                }
        );
        drawable.setCornerRadius(dp(24));
        return drawable;
    }

    private Drawable createArtIconBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(ColorUtils.setAlphaComponent(getAccentColor(), isDarkTheme() ? 58 : 34));
        drawable.setStroke(dp(1), ColorUtils.setAlphaComponent(getAccentColorSecondary(), isDarkTheme() ? 96 : 78));
        return drawable;
    }

    private Drawable createEmptyIconBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(ColorUtils.setAlphaComponent(getAccentColor(), isDarkTheme() ? 42 : 22));
        drawable.setStroke(dp(1), ColorUtils.setAlphaComponent(getAccentColorSecondary(), isDarkTheme() ? 92 : 66));
        return drawable;
    }

    private Drawable createCircleStrokeDrawable() {
        final int accent = getAccentColor();
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(ColorUtils.setAlphaComponent(accent, isDarkTheme() ? 48 : 24));
        drawable.setStroke(dp(3), ColorUtils.blendARGB(accent, Color.WHITE, isDarkTheme() ? 0.12f : 0.42f));
        return drawable;
    }

    private static class StatView extends LinearLayout {

        private final GradientDrawable iconBackgroundDrawable;
        private final ImageView iconView;
        private final TextView titleView;
        private final TextView valueView;

        public StatView(Context context, Theme.ResourcesProvider resourcesProvider, int iconRes, int iconColor, boolean showChevron) {
            super(context);
            setOrientation(HORIZONTAL);
            setGravity(Gravity.CENTER_VERTICAL);
            setPadding(dp(8), 0, dp(8), 0);

            FrameLayout iconBackground = new FrameLayout(context);
            iconBackgroundDrawable = new GradientDrawable();
            iconBackgroundDrawable.setShape(GradientDrawable.OVAL);
            iconBackgroundDrawable.setColor(ColorUtils.setAlphaComponent(iconColor, 34));
            iconBackground.setBackground(iconBackgroundDrawable);
            addView(iconBackground, LayoutHelper.createLinear(24, 24, Gravity.CENTER_VERTICAL));

            iconView = new ImageView(context);
            iconView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            iconView.setImageResource(iconRes);
            iconView.setColorFilter(new PorterDuffColorFilter(iconColor, PorterDuff.Mode.SRC_IN));
            iconBackground.addView(iconView, LayoutHelper.createFrame(14, 14, Gravity.CENTER));

            LinearLayout textColumn = new LinearLayout(context);
            textColumn.setOrientation(VERTICAL);
            addView(textColumn, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1f, Gravity.CENTER_VERTICAL, 7, 0, 0, 0));

            titleView = new TextView(context);
            titleView.setIncludeFontPadding(false);
            titleView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText, resourcesProvider));
            titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10.5f);
            textColumn.addView(titleView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));

            valueView = new TextView(context);
            valueView.setIncludeFontPadding(false);
            valueView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText, resourcesProvider));
            valueView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14.5f);
            valueView.setTypeface(AndroidUtilities.bold());
            valueView.setSingleLine(true);
            textColumn.addView(valueView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT, 0, 1, 0, 0));

            if (showChevron) {
                ImageView arrowView = new ImageView(context);
                arrowView.setScaleType(ImageView.ScaleType.CENTER);
                arrowView.setImageResource(R.drawable.msg_arrowright);
                arrowView.setColorFilter(new PorterDuffColorFilter(ColorUtils.setAlphaComponent(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText, resourcesProvider), 170), PorterDuff.Mode.SRC_IN));
                addView(arrowView, LayoutHelper.createLinear(16, 16, Gravity.CENTER_VERTICAL, 2, 0, 0, 0));
            }
        }

        public void set(String title, String value) {
            titleView.setText(title);
            valueView.setText(value);
        }

        public void setIcon(int iconRes, int iconColor) {
            iconView.setImageResource(iconRes);
            iconView.setColorFilter(new PorterDuffColorFilter(iconColor, PorterDuff.Mode.SRC_IN));
            iconBackgroundDrawable.setColor(ColorUtils.setAlphaComponent(iconColor, 34));
        }
    }
}
