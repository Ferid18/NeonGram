package app.neogram.translator;

public final class TranSmartTranslator extends BaseTranslator {
    private static final TranSmartTranslator INSTANCE = new TranSmartTranslator();

    private TranSmartTranslator() {
        super(TranslatorBridge.singleton("TranSmartTranslator"));
    }

    public static TranSmartTranslator getInstance() {
        return INSTANCE;
    }
}
