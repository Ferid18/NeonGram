package app.neogram.translator;

public final class GoogleAppTranslator extends BaseTranslator {
    private static final GoogleAppTranslator INSTANCE = new GoogleAppTranslator();

    private GoogleAppTranslator() {
        super(TranslatorBridge.singleton("GoogleAppTranslator"));
    }

    public static GoogleAppTranslator getInstance() {
        return INSTANCE;
    }
}
