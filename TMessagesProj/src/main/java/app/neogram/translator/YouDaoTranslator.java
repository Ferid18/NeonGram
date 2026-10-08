package app.neogram.translator;

public final class YouDaoTranslator extends BaseTranslator {
    private static final YouDaoTranslator INSTANCE = new YouDaoTranslator();

    private YouDaoTranslator() {
        super(TranslatorBridge.singleton("YouDaoTranslator"));
    }

    public static YouDaoTranslator getInstance() {
        return INSTANCE;
    }
}
