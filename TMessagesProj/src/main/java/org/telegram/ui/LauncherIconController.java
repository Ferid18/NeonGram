package org.telegram.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.R;

public class LauncherIconController {
    private static final String[] REMOVED_ICON_KEYS = {
            "AquaIcon",
            "PremiumIcon",
            "TurboIcon",
            "NoxIcon",
            "MaterialIcon"
    };

    public static void tryFixLauncherIconIfNeeded() {
        Context ctx = ApplicationLoader.applicationContext;
        PackageManager pm = ctx.getPackageManager();
        disableRemovedIcons(ctx, pm);
        for (LauncherIcon icon : LauncherIcon.values()) {
            if (isEnabled(icon)) {
                return;
            }
        }

        setIcon(LauncherIcon.DEFAULT);
    }

    public static boolean isEnabled(LauncherIcon icon) {
        Context ctx = ApplicationLoader.applicationContext;
        int i = ctx.getPackageManager().getComponentEnabledSetting(icon.getComponentName(ctx));
        return i == PackageManager.COMPONENT_ENABLED_STATE_ENABLED || i == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT && icon == LauncherIcon.DEFAULT;
    }

    public static void setIcon(LauncherIcon icon) {
        Context ctx = ApplicationLoader.applicationContext;
        PackageManager pm = ctx.getPackageManager();
        disableRemovedIcons(ctx, pm);
        for (LauncherIcon i : LauncherIcon.values()) {
            pm.setComponentEnabledSetting(i.getComponentName(ctx), i == icon ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED :
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        }
    }

    private static void disableRemovedIcons(Context ctx, PackageManager pm) {
        for (String key : REMOVED_ICON_KEYS) {
            pm.setComponentEnabledSetting(new ComponentName(ctx.getPackageName(), "org.telegram.messenger." + key),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        }
    }

    public enum LauncherIcon {
        DEFAULT("DefaultIcon", R.mipmap.icon_14_launcher_background, R.mipmap.icon_14_launcher_foreground, R.string.AppIconDefault),
        //VINTAGE("VintageIcon", R.drawable.icon_6_background_sa, R.drawable.ic_launcher_foreground, R.string.AppIconVintage),
        MERIO("MerioIcon", R.mipmap.icon_7_launcher_background, R.mipmap.icon_7_launcher_foreground, R.string.AppIconMerio),
        RAINBOW("RainbowIcon", R.mipmap.icon_8_launcher_background, R.mipmap.icon_8_launcher_foreground, R.string.AppIconRainbow),
        SCHOOL("OldSchoolIcon", R.mipmap.icon_9_launcher_background, R.mipmap.icon_9_launcher_foreground, R.string.AppIconOldSchool),
        MUSHEEN("MusheenIcon", R.mipmap.icon_10_launcher_background, R.mipmap.icon_10_launcher_foreground, R.string.AppIconMusheen),
        SPACE("SpaceIcon", R.mipmap.icon_11_launcher_background, R.mipmap.icon_11_launcher_foreground, R.string.AppIconSpace),
        CLOUD("CloudIcon", R.mipmap.icon_12_launcher_background, R.mipmap.icon_12_launcher_foreground, R.string.AppIconCloud),
        NEON("NeonIcon", R.mipmap.icon_13_launcher_background, R.mipmap.icon_13_launcher_foreground, R.string.AppIconNeon);

        public final String key;
        public final int background;
        public final int foreground;
        public final int title;
        public final boolean premium;

        private ComponentName componentName;

        public ComponentName getComponentName(Context ctx) {
            if (componentName == null) {
                componentName = new ComponentName(ctx.getPackageName(), "org.telegram.messenger." + key);
            }
            return componentName;
        }

        LauncherIcon(String key, int background, int foreground, int title) {
            this(key, background, foreground, title, false);
        }

        LauncherIcon(String key, int background, int foreground, int title, boolean premium) {
            this.key = key;
            this.background = background;
            this.foreground = foreground;
            this.title = title;
            this.premium = premium;
        }
    }
}
