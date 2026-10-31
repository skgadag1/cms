package com.tbscg.doosan.magnolia.dxp.core.translation.io;

import info.magnolia.translation.io.TranslationItem;
import info.magnolia.translation.io.reader.UnzippedXliffTranslationItemReader;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

public class CustomXliffTranslationItemReader extends UnzippedXliffTranslationItemReader {

    public CustomXliffTranslationItemReader(InputStream inputStream, String encoding) {
        super(inputStream, encoding);
    }

    @Override
    protected String[] readNextRow() throws IOException {
        String[] row = super.readNextRow();
        if (row != null && StringUtils.isBlank(row[0])) {
            row[0] = TranslationItem.DATE_FORMAT.format(new Date());
        }
        return row;
    }
}
