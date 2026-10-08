package app.neogram.translator;

public final class MicrosoftTranslator extends BaseTranslator {
    private static final MicrosoftTranslator INSTANCE = new MicrosoftTranslator();

    private MicrosoftTranslator() {
        super(TranslatorBridge.singleton("MicrosoftTranslator"));
    }

    public static MicrosoftTranslator getInstance() {
        return INSTANCE;
    }
}
