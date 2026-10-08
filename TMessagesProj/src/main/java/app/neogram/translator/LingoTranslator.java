package app.neogram.translator;

public final class LingoTranslator extends BaseTranslator {
    private static final LingoTranslator INSTANCE = new LingoTranslator();

    private LingoTranslator() {
        super(TranslatorBridge.singleton("LingoTranslator"));
    }

    public static LingoTranslator getInstance() {
        return INSTANCE;
    }
}
