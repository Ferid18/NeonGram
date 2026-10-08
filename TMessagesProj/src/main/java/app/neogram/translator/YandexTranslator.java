package app.neogram.translator;

public final class YandexTranslator extends BaseTranslator {
    private static final YandexTranslator INSTANCE = new YandexTranslator();

    private YandexTranslator() {
        super(TranslatorBridge.singleton("YandexTranslator"));
    }

    public static YandexTranslator getInstance() {
        return INSTANCE;
    }
}
