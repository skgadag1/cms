package com.tbscg.doosan.magnolia.mediatracker.app.domainList;

import com.tbscg.doosan.magnolia.mediatracker.data.export.ExportGenerator;
import com.tbscg.doosan.magnolia.mediatracker.data.model.VanityDomain;
import com.tbscg.doosan.magnolia.mediatracker.util.DateFormatUtil;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

public class DomainExportGenerator implements ExportGenerator<VanityDomain> {

    @Override
    public void generate(Stream<VanityDomain> domains, OutputStream exportStream) {
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(exportStream, StandardCharsets.UTF_8));
        writer.println("sep=;");
        writer.println("Code;Domain;Start;End;Status");
        domains.forEachOrdered(domain -> {
            StringBuilder row = new StringBuilder();
            row.append(domain.getCode())
                    .append(";")
                    .append(domain.getFullDomainName())
                    .append(";")
                    .append(DateFormatUtil.formatOffsetDateTime(domain.getStartDate()))
                    .append(";")
                    .append(DateFormatUtil.formatOffsetDateTime(domain.getEndDate()))
                    .append(";")
                    .append(domain.getStatus());
            writer.println(row);
        });
        writer.close();
    }
}
