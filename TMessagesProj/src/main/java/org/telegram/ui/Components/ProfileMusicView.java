package org.telegram.ui.Components;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.AndroidUtilities.dpf2;
import static org.telegram.messenger.AndroidUtilities.lerp;
import static org.telegram.messenger.LocaleController.formatString;
import static org.telegram.messenger.LocaleController.getString;
import static org.telegram.ui.ActionBar.Theme.multAlpha;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.graphics.RenderNode;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ProfileActivity;

public class ProfileMusicView extends View {

    private final Theme.ResourcesProvider resourcesProvider;
    private final PorterDuffColorFilter filterColorWhite = new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);

    private Text author, title;
    private final Drawable icon;
    private final ImageReceiver coverReceiver = new ImageReceiver(this);
    private final Paint coverFallbackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final RectF rect = new RectF();
    private final RectF coverRect = new RectF();
    private final Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint accentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint playButtonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint playIconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path clipPath = new Path();
    private final Path playPath = new Path();

    private final ButtonBounce bounce = new ButtonBounce(this);

    public ProfileMusicView(Context context, Theme.ResourcesProvider resourcesProvider) {
        super(context);
        this.resourcesProvider = resourcesProvider;

        icon = context.getResources().getDrawable(R.drawable.files_music).mutate();
        coverReceiver.setRoundRadius(dp(10));

        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(dpf2(0.66f));

        setColor(null);
        setText("Author", "Title");
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(
            MeasureSpec.makeMeasureSpec(
                MeasureSpec.getSize(widthMeasureSpec),
                MeasureSpec.EXACTLY
            ),
            MeasureSpec.makeMeasureSpec(
                dp(76),
                MeasureSpec.EXACTLY
            )
        );
    }

    private int textColor = Color.WHITE;
    private int accentColor = Color.WHITE;
    private float parentExpanded;
    private int backgroundColor;
    private boolean withShadows;

    public void setColor(MessagesController.PeerColor peerColor) {
        int color1, color2;
        if (peerColor == null) {
            color1 = color2 = Theme.getColor(Theme.key_actionBarDefault, resourcesProvider);
        } else {
            color1 = peerColor.getBgColor1(Theme.isCurrentThemeDark());
            color2 = peerColor.getBgColor2(Theme.isCurrentThemeDark());
        }
        accentColor = peerColor == null ? Theme.getColor(Theme.key_featuredStickers_addButton, resourcesProvider) : ColorUtils.blendARGB(color1, color2, .55f);

        if (peerColor == null) {
            backgroundColor = Theme.getColor(Theme.key_windowBackgroundWhite, resourcesProvider);
            withShadows = true;
        } else {
            backgroundColor = Theme.adaptHSV(ColorUtils.blendARGB(color1, color2, .18f), +.04f, -.09f);
            withShadows = false;
        }
        backgroundPaint.setColor(backgroundColor);
        coverFallbackPaint.setColor(textColor);
        checkTextColor();
    }

    private void checkTextColor() {
        final boolean useBlackText = parentExpanded < 0.8f && AndroidUtilities.computePerceivedBrightness(backgroundColor) > 0.85f;
        textColor = useBlackText ? Color.BLACK : Color.WHITE;
        icon.setColorFilter(useBlackText ? new PorterDuffColorFilter(accentColor, PorterDuff.Mode.SRC_IN) : filterColorWhite);
        coverFallbackPaint.setColor(textColor);
        playIconPaint.setColor(useBlackText ? accentColor : Color.WHITE);
        invalidate();
    }

    public void setParentExpanded(float expanded) {
        if (parentExpanded != expanded) {
            parentExpanded = expanded;
            checkTextColor();
            invalidate();
        }
    }



    public void setMusicDocument(TLRPC.Document document) {
        CharSequence author = getAuthor(document);
        CharSequence title = getTitle(document);
        if (TextUtils.isEmpty(title)) {
            title = getString(R.string.AudioUnknownTitle);
        }
        if (TextUtils.isEmpty(author)) {
            author = getString(R.string.AudioUnknownArtist);
        }
        setCoverDocument(document);
        setText(author, title);
    }

    private void setCoverDocument(TLRPC.Document document) {
        if (document == null) {
            coverReceiver.clearImage();
            return;
        }
        if (document.thumbs == null || document.thumbs.isEmpty()) {
            coverReceiver.clearImage();
            return;
        }
        TLRPC.PhotoSize thumb = FileLoader.getClosestPhotoSizeWithSize(document.thumbs, 160);
        if (!(thumb instanceof TLRPC.TL_photoSize) && !(thumb instanceof TLRPC.TL_photoSizeProgressive)) {
            thumb = null;
        }
        if (thumb != null) {
            coverReceiver.setImage(ImageLocation.getForDocument(thumb, document), "56_56", null, document.size, null, document, 1);
        } else {
            coverReceiver.clearImage();
        }
    }

    public static CharSequence getTitle(TLRPC.Document document) {
        if (document == null) {
            return null;
        }
        if (document.attributes == null) {
            String fileName = FileLoader.getDocumentFileName(document);
            return TextUtils.isEmpty(fileName) ? null : fileName;
        }
        for (int a = 0; a < document.attributes.size(); a++) {
            TLRPC.DocumentAttribute attribute = document.attributes.get(a);
            if (attribute instanceof TLRPC.TL_documentAttributeAudio) {
                String title = attribute.title;
                if (title == null || title.length() == 0) {
                    title = FileLoader.getDocumentFileName(document);
                }
                return title;
            }
        }
        String fileName = FileLoader.getDocumentFileName(document);
        if (!TextUtils.isEmpty(fileName)) {
            return fileName;
        }
//        return getString(R.string.AudioUnknownTitle);
        return null;
    }

    public static CharSequence getAuthor(TLRPC.Document document) {
        if (document == null || document.attributes == null) {
            return null;
        }
        for (int a = 0; a < document.attributes.size(); a++) {
            TLRPC.DocumentAttribute attribute = document.attributes.get(a);
            if (attribute instanceof TLRPC.TL_documentAttributeAudio) {
                if (!attribute.voice) {
                    return attribute.performer;
                }
            }
        }
        return null;
    }

    public void setText(CharSequence author, CharSequence title) {
        if (author == null) {
            author = "";
        }
        if (title == null) {
            title = "";
        }
        this.author = new Text(author, 12);
        this.title = new Text(title, 15, AndroidUtilities.bold());

        setContentDescription(getString(R.string.AccDescrProfileMusic) + " " + title + " - " + author);
        invalidate();
    }

    private ProfileActivity.AvatarImageView avatarView;
    private boolean ignoreRect = false;
    private RenderNode renderNode;
    private float renderNodeScale;
    private float renderNodeTranslateY;

    public void drawingBlur(boolean drawing) {
        if (ignoreRect != drawing || renderNode != null) {
            ignoreRect = drawing;
            renderNode = null;
            avatarView = null;
            invalidate();
        }
    }

    public void drawingBlur(RenderNode renderNode, ProfileActivity.AvatarImageView avatarView, float scale, float dy) {
        this.ignoreRect = false;
        this.renderNode = renderNode;
        this.avatarView = avatarView;
        this.renderNodeScale = scale;
        this.renderNodeTranslateY = dy;
        invalidate();
    }

    private float currentHeight;

    public void updatePosition(float y, float newHeight) {
        currentHeight = newHeight;
        setTranslationY(y);
        invalidate();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        coverReceiver.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        coverReceiver.onDetachedFromWindow();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        final float alpha = Utilities.clamp01((currentHeight) / dp(52));
        if (alpha <= 0) return false;
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            bounce.setPressed(rect.contains(event.getX(), event.getY()));
        } else if (event.getAction() == MotionEvent.ACTION_MOVE && bounce.isPressed()) {
            if (!rect.contains(event.getX(), event.getY())) {
                bounce.setPressed(false);
            }
        } else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
            bounce.setPressed(false);
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (bounce.isPressed()) {
                performClick();
            }
            bounce.setPressed(false);
        }
        return bounce.isPressed();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        if (this.author == null || this.title == null) return;

        final float alpha = Utilities.clamp01((currentHeight) / dp(52));
        final float scale = bounce.getScale(0.02f);
        if (alpha <= 0) return;

        final int clipSave = canvas.save();
        canvas.clipRect(0, 0, getMeasuredWidth(), Math.min(getMeasuredHeight(), currentHeight));

        final int side = dp(26);
        final int cardHeight = dp(60);
        final int coverSize = dp(44);
        final int playButtonSize = dp(30);
        final int contentLeft = side + dp(12) + coverSize + dp(12);
        final int contentRight = getWidth() - side - playButtonSize - dp(18);
        final int textWidth = Math.max(0, contentRight - contentLeft);

        this.title.ellipsize(textWidth);
        this.author.ellipsize(textWidth);

        canvas.save();
        canvas.scale(scale, scale, getWidth() / 2f, getHeight() / 2f);

        rect.set(
            side,
            dp(8),
            getWidth() - side,
            dp(8) + cardHeight
        );
        if (withShadows && SharedConfig.shadowsInSections) {
            backgroundPaint.setShadowLayer(dpf2(2.5f), 0, dpf2(1), multAlpha(0x16000000, alpha));
            strokePaint.setShadowLayer(dpf2(0.33f), 0, 0, multAlpha(0x12000000, alpha));
            strokePaint.setColor(Theme.multAlpha(textColor, 0.10f * alpha));
        } else {
            backgroundPaint.setShadowLayer(0, 0, 0, 0);
            strokePaint.setShadowLayer(0, 0, 0, 0);
            strokePaint.setColor(Theme.multAlpha(textColor, 0.08f * alpha));
        }
        int wasAlpha = backgroundPaint.getAlpha();
        backgroundPaint.setAlpha((int) (wasAlpha * alpha));
        canvas.drawRoundRect(rect, dp(13), dp(13), backgroundPaint);
        backgroundPaint.setAlpha(wasAlpha);
        canvas.drawRoundRect(rect, dp(13), dp(13), strokePaint);

        clipPath.rewind();
        clipPath.addRoundRect(rect, dp(13), dp(13), Path.Direction.CW);

        canvas.save();
        canvas.clipPath(clipPath);
        if (!ignoreRect && renderNode != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && canvas.isHardwareAccelerated()) {
            canvas.save();
            canvas.translate(0f, renderNodeTranslateY);
            canvas.scale(renderNodeScale, renderNodeScale);
            canvas.drawRenderNode(renderNode);
            canvas.restore();
        }

        final float coverLeft = rect.left + dp(12);
        final float coverTop = rect.centerY() - coverSize / 2f;
        coverRect.set(coverLeft, coverTop, coverLeft + coverSize, coverTop + coverSize);
        coverFallbackPaint.setColor(ColorUtils.blendARGB(backgroundColor, accentColor, AndroidUtilities.computePerceivedBrightness(backgroundColor) > 0.85f ? .13f : .26f));
        coverFallbackPaint.setAlpha((int) (255 * alpha));
        canvas.drawRoundRect(coverRect, dp(8), dp(8), coverFallbackPaint);
        accentPaint.setColor(Theme.multAlpha(accentColor, 0.18f * alpha));
        canvas.drawRoundRect(coverRect.left, coverRect.top, coverRect.left + dp(5), coverRect.bottom, dp(2.5f), dp(2.5f), accentPaint);
        coverReceiver.setImageCoords(coverRect.left, coverRect.top, coverRect.width(), coverRect.height());
        coverReceiver.setAlpha(alpha);
        coverReceiver.draw(canvas);

        if (!coverReceiver.hasBitmapImage()) {
            final int iconSz = dp(19);
            icon.setBounds(
                (int) coverRect.centerX() - iconSz / 2,
                (int) coverRect.centerY() - iconSz / 2,
                (int) coverRect.centerX() + iconSz / 2,
                (int) coverRect.centerY() + iconSz / 2
            );
            icon.setAlpha((int) (255 * alpha));
            icon.draw(canvas);
            icon.setAlpha(255);
        }

        final float titleY = rect.top + dp(22);
        final float authorY = rect.top + dp(42);
        this.title.draw(canvas, contentLeft, titleY, textColor, alpha);
        this.author.draw(canvas, contentLeft, authorY, textColor, 0.62f * alpha);

        final float playCx = getWidth() - side - playButtonSize / 2f;
        final float playCy = rect.centerY();
        playButtonPaint.setColor(Theme.multAlpha(accentColor, (AndroidUtilities.computePerceivedBrightness(backgroundColor) > 0.85f ? 0.13f : 0.24f) * alpha));
        canvas.drawCircle(playCx, playCy, playButtonSize / 2f, playButtonPaint);

        playPath.rewind();
        playPath.moveTo(playCx - dp(3.5f), playCy - dp(6));
        playPath.lineTo(playCx - dp(3.5f), playCy + dp(6));
        playPath.lineTo(playCx + dp(6), playCy);
        playPath.close();
        final int wasPlayAlpha = playIconPaint.getAlpha();
        playIconPaint.setAlpha((int) (wasPlayAlpha * alpha));
        canvas.drawPath(playPath, playIconPaint);
        playIconPaint.setAlpha(wasPlayAlpha);

        canvas.restore();

        canvas.restore();
        canvas.restoreToCount(clipSave);
    }
}
