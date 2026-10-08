package app.neogram.translator;

public final class DeepLTranslator extends BaseTranslator {
    public static final int FORMALITY_DEFAULT = 0;
    public static final int FORMALITY_MORE = 1;
    public static final int FORMALITY_LESS = 2;

    private static final DeepLTranslator INSTANCE = new DeepLTranslator();

    private DeepLTranslator() {
        super(TranslatorBridge.singleton("DeepLTranslator"));
    }

    public static DeepLTranslator getInstance() {
        return INSTANCE;
    }

    public static void setFormality(int formality) {
        TranslatorBridge.invokeStaticUnchecked("DeepLTranslator", "setFormality", new Class<?>[]{int.class}, formality);
    }
}
