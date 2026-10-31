package com.tbscg.doosan.magnolia.dxp.core.translation.io;

import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.translation.io.TranslationItem;
import info.magnolia.translation.io.writer.XliffTranslationItemWriter;

import javax.inject.Inject;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Locale;

public class CustomXliffTranslationItemWriter extends XliffTranslationItemWriter {

    @Inject
    public CustomXliffTranslationItemWriter(
            I18nContentSupport i18nContentSupport,
            OutputStream outputStream,
            Locale[] supportedLocales,
            Locale sourceLocale) {
        super(i18nContentSupport, outputStream, supportedLocales, sourceLocale);
    }

    @Override
    public void writeRow(final TranslationItem input) throws IOException {
        TranslationItem item = input;
        if (input.getLastModificationTime() == null) {
            item = new TranslationItem.Builder(input.getKey(), input.getMasterValue(), input.getMasterLocale(), input.getContext())
                    .withLocalizedValues(input.getLocalizedValues())
                    .withLink(input.getLink())
                    .withLastModificationTime(Calendar.getInstance())
                    .build();
        }
        super.writeRow(item);
    }
}
