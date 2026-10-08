package app.neogram.translator;

import java.util.List;

public class BaseTranslator {
    private final Object delegate;

    protected BaseTranslator(Object delegate) {
        this.delegate = delegate;
    }

    public Result translate(String query, String fromLanguage, String toLanguage) throws Exception {
        Object result = TranslatorBridge.invoke(
                delegate,
                "translate",
                new Class<?>[]{String.class, String.class, String.class},
                query,
                fromLanguage,
                toLanguage);
        return new Result(
                TranslatorBridge.readStringField(result, "translation"),
                TranslatorBridge.readStringField(result, "sourceLanguage"));
    }

    public boolean supportLanguage(String language) {
        try {
            return (Boolean) TranslatorBridge.invoke(delegate, "supportLanguage", new Class<?>[]{String.class}, language);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public List<String> getTargetLanguages() {
        try {
            return (List<String>) TranslatorBridge.invoke(delegate, "getTargetLanguages", new Class<?>[]{});
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static final class Result {
        public final String translation;
        public final String sourceLanguage;

        private Result(String translation, String sourceLanguage) {
            this.translation = translation;
            this.sourceLanguage = sourceLanguage;
        }
    }
}
