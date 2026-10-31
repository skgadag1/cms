package com.tbscg.doosan.magnolia.mediatracker.app.urlList;

import com.tbscg.doosan.magnolia.mediatracker.data.export.ExportGenerator;
import com.tbscg.doosan.magnolia.mediatracker.data.model.VanityUrl;
import com.tbscg.doosan.magnolia.mediatracker.util.DateFormatUtil;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

public class UrlExportGenerator implements ExportGenerator<VanityUrl> {

    @Override
    public void generate(Stream<VanityUrl> domains, OutputStream exportStream) {
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(exportStream, StandardCharsets.UTF_8));
        writer.println("sep=;");
        writer.println("Code;Domain;Path;Start;End;Status;CreatedBy;Placement;Tracking Parameter;Redirect Location");
        domains.forEachOrdered(url -> {
            StringBuilder row = new StringBuilder();
            row.append(url.getCode())
                    .append(";")
                    .append(url.getVanityDomain().getFullDomainName())
                    .append(";")
                    .append(url.getPath())
                    .append(";")
                    .append(DateFormatUtil.formatOffsetDateTime(url.getStartDate()))
                    .append(";")
                    .append(DateFormatUtil.formatOffsetDateTime(url.getEndDate()))
                    .append(";")
                    .append(url.getStatus())
                    .append(";")
                    .append(url.getCreatedBy())
                    .append(";")
                    .append(url.getPlacement())
                    .append(";")
                    .append(url.getTrackingParameter())
                    .append(";")
                    .append(url.getRedirectLocation());
            writer.println(row);
        });
        writer.close();
    }
}
