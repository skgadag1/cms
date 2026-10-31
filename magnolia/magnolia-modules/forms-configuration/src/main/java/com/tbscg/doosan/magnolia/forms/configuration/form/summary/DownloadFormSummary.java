package com.tbscg.doosan.magnolia.forms.configuration.form.summary;

import com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil;
import com.tbscg.doosan.magnolia.forms.configuration.form.summary.downloader.CSVDownloader;
import com.tbscg.doosan.magnolia.forms.configuration.form.summary.downloader.CSVSections;
import com.tbscg.doosan.magnolia.forms.configuration.form.summary.downloader.FormDownloader;
import com.tbscg.doosan.magnolia.forms.configuration.form.summary.downloader.LoaderInput;
import com.tbscg.doosan.magnolia.forms.configuration.form.summary.loader.DataLoader;
import com.tbscg.doosan.magnolia.forms.configuration.form.summary.loader.DynamoDBItem;
import com.tbscg.doosan.magnolia.forms.configuration.form.summary.loader.DynamoDBLoader;
import com.tbscg.doosan.magnolia.forms.configuration.form.summary.loader.FormRequest;
import com.tbscg.doosan.magnolia.forms.configuration.form.summary.parsers.DynamoDBItemsToCsvSectionParser;
import com.tbscg.doosan.magnolia.forms.configuration.form.summary.parsers.Parser;
import com.tbscg.doosan.magnolia.regions.service.RegionsService;
import com.vaadin.data.Binder;
import com.vaadin.data.BinderValidationStatus;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Notification;
import info.magnolia.ui.ValueContext;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.editor.EditorView;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.jcr.Node;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static com.tbscg.doosan.magnolia.forms.configuration.form.summary.BinderUtils.getFieldFromList;

@Slf4j
public class DownloadFormSummary extends AbstractAction<DownloadFormSummaryDefinition> {
    private static final String CSVExtension = ".csv";
    private static final String FROM = "from";
    private static final String TO = "to";

    private final DataLoader<DynamoDBItem> dataLoader;
    private final FormDownloader<CSVSections> formDownloader;
    private final Parser<DynamoDBItem, CSVSections> parser;
    private final EditorView<DateField> form;
    private final ValueContext<Node> valueContext;
    private final RegionsService regionService;

    @Inject
    protected DownloadFormSummary(DownloadFormSummaryDefinition definition, EditorView<DateField> form, ValueContext<Node> valueContext, RegionsService regionService, DynamoDBLoader dataLoader, CSVDownloader formDownloader, DynamoDBItemsToCsvSectionParser parser) {
        super(definition);
        this.valueContext = valueContext;
        this.form = form;
        this.dataLoader = dataLoader;
        this.formDownloader = formDownloader;
        this.parser = parser;
        this.regionService = regionService;
    }

    @Override
    public void execute() {
        try {
            FormRequest formRequest = getFormRequest();
            LoaderInput<CSVSections> dataToDownload = getDataToDownload(formRequest);
            formDownloader.initDownload(dataToDownload);
        } catch (Exception e) {
            log.error("Failed to download caused by", e);
        }
    }

    private Map<String, DateField> getDateFieldsFromEditorView() throws Exception {
        Map<String, DateField> map = new HashMap<>();
        List<Binder<?>> fields = form.validate().stream().filter(BinderValidationStatus::isOk).map(BinderValidationStatus::getBinder).collect(Collectors.toList());
        map.put(FROM, getFieldFromList(fields, FROM));
        map.put(TO, getFieldFromList(fields, TO));

        return map;
    }

    private LoaderInput<CSVSections> getDataToDownload(FormRequest formRequest) {
        List<DynamoDBItem> items = dataLoader.getData(formRequest);
        if (items.isEmpty()) {
            Notification.show("No Data available for the selected dates.", Notification.Type.TRAY_NOTIFICATION).setDelayMsec(3600);
            return null;
        }
        String filename = getFileName(formRequest);
        CSVSections parsedData = parser.parse(items, formRequest);
        return LoaderInput
                .<CSVSections>builder()
                .withData(parsedData)
                .withFileName(filename)
                .build();
    }

    private String getFileName(FormRequest formRequest) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");
        Date from = Date.from(formRequest.getFrom().getValue().atStartOfDay()
                .atZone(ZoneId.systemDefault())
                .toInstant());

        Date to = Date.from(formRequest.getTo().getValue().atStartOfDay()
                .atZone(ZoneId.systemDefault())
                .toInstant());
        return String.format("%s-%s.%s%s", dateFormat.format(from), dateFormat.format(to), formRequest.getFormName(), CSVExtension);
    }

    private FormRequest getFormRequest() throws Exception {
        Map<String, DateField> fields = getDateFieldsFromEditorView();
        Node node = valueContext.getSingleOrThrow();
        return FormRequest
                .builder()
                .withRegion(regionService.getRegionFromPath(NodeUtil.getPath(node)).getName())
                .withFormName(NodeUtil.getName(node))
                .withFrom(fields.get(FROM))
                .withTo(fields.get(TO))
                .build();
    }
}
