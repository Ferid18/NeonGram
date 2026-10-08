package app.neogram.translator;

public final class SogouTranslator extends BaseTranslator {
    private static final SogouTranslator INSTANCE = new SogouTranslator();

    private SogouTranslator() {
        super(TranslatorBridge.singleton("SogouTranslator"));
    }

    public static SogouTranslator getInstance() {
        return INSTANCE;
    }
}
