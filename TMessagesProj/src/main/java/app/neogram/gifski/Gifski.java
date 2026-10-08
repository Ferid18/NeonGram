package app.neogram.gifski;

import android.graphics.Bitmap;

public class Gifski {
    private final Object delegate;

    public Gifski(Settings settings) {
        delegate = GifskiBridge.newGifski(settings.delegate);
    }

    public void setFileOutput(String path) {
        GifskiBridge.invoke(delegate, "setFileOutput", new Class<?>[]{String.class}, path);
    }

    public void addFrameBitmap(int frame, Bitmap bitmap, double pts) {
        GifskiBridge.invoke(delegate, "addFrameBitmap", new Class<?>[]{int.class, Bitmap.class, double.class}, frame, bitmap, pts);
    }

    public void finish() {
        GifskiBridge.invoke(delegate, "finish", new Class<?>[]{});
    }

    public static class Settings {
        private final Object delegate = GifskiBridge.newSettings();

        public void setHeight(int height) {
            GifskiBridge.invoke(delegate, "setHeight", new Class<?>[]{int.class}, height);
        }

        public void setWidth(int width) {
            GifskiBridge.invoke(delegate, "setWidth", new Class<?>[]{int.class}, width);
        }

        public void setQuality(int quality) {
            GifskiBridge.invoke(delegate, "setQuality", new Class<?>[]{int.class}, quality);
        }

        public void setRepeat(short repeat) {
            GifskiBridge.invoke(delegate, "setRepeat", new Class<?>[]{short.class}, repeat);
        }
    }
}
