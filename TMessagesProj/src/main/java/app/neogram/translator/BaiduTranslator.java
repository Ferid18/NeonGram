package app.neogram.translator;

public final class BaiduTranslator extends BaseTranslator {
    private static final BaiduTranslator INSTANCE = new BaiduTranslator();

    private BaiduTranslator() {
        super(TranslatorBridge.singleton("BaiduTranslator"));
    }

    public static BaiduTranslator getInstance() {
        return INSTANCE;
    }
}
