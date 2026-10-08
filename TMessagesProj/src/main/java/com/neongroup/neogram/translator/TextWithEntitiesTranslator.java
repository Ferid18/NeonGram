package com.neongroup.neogram.translator;

import org.telegram.tgnet.TLRPC;
import org.telegram.ui.Components.TranslateAlert2;

import java.util.HashMap;
import java.util.List;

import app.neogram.translator.BaiduTranslator;
import app.neogram.translator.BaseTranslator;
import app.neogram.translator.DeepLTranslator;
import app.neogram.translator.GoogleAppTranslator;
import app.neogram.translator.LingoTranslator;
import app.neogram.translator.MicrosoftTranslator;
import app.neogram.translator.SogouTranslator;
import app.neogram.translator.TranSmartTranslator;
import app.neogram.translator.YandexTranslator;
import app.neogram.translator.YouDaoTranslator;
import com.neongroup.neogram.NeoConfig;
import com.neongroup.neogram.translator.html.HTMLKeeper;

public class TextWithEntitiesTranslator implements Translator.ITranslator {

    private static final HashMap<String, TextWithEntitiesTranslator> wrappedTranslators = new HashMap<>();

    public static TextWithEntitiesTranslator of(String type) {
        return wrappedTranslators.computeIfAbsent(type, type1 -> {
            var translator = switch (type1) {
                case Translator.PROVIDER_YANDEX -> YandexTranslator.getInstance();
                case Translator.PROVIDER_LINGO -> LingoTranslator.getInstance();
                case Translator.PROVIDER_DEEPL -> {
                    DeepLTranslator.setFormality(NeoConfig.deepLFormality);
                    yield DeepLTranslator.getInstance();
                }
                case Translator.PROVIDER_MICROSOFT -> MicrosoftTranslator.getInstance();
                case Translator.PROVIDER_YOUDAO -> YouDaoTranslator.getInstance();
                case Translator.PROVIDER_BAIDU -> BaiduTranslator.getInstance();
                case Translator.PROVIDER_SOGOU -> SogouTranslator.getInstance();
                case Translator.PROVIDER_TENCENT -> TranSmartTranslator.getInstance();
                default -> GoogleAppTranslator.getInstance();
            };
            return new TextWithEntitiesTranslator(translator);
        });
    }

    private final BaseTranslator translator;

    private TextWithEntitiesTranslator(BaseTranslator translator) {
        this.translator = translator;
    }

    @Override
    public Translator.TranslationResult translate(TLRPC.TL_textWithEntities query, String fl, String tl) throws Exception {
        if (NeoConfig.keepFormatting) {
            var html = HTMLKeeper.entitiesToHtml(query.text, query.entities, false);
            var result = translator.translate(html, null, tl);
            var textAndEntitiesTranslated = HTMLKeeper.htmlToEntities(result.translation, query.entities, false);
            return Translator.TranslationResult.of(
                    TranslateAlert2.preprocess(query, textAndEntitiesTranslated),
                    result.sourceLanguage
            );
        } else {
            var result = translator.translate(query.text, null, tl);
            return Translator.TranslationResult.of(Translator.textWithEntities(result.translation, null), result.sourceLanguage);
        }
    }

    @Override
    public boolean supportLanguage(String language) {
        return translator.supportLanguage(language);
    }

    @Override
    public List<String> getTargetLanguages() {
        return translator.getTargetLanguages();
    }
}
