package com.tbscg.doosan.magnolia.forms.configuration.form.summary;

import com.tbscg.doosan.magnolia.forms.configuration.form.summary.downloader.CSVDownloader;
import com.tbscg.doosan.magnolia.forms.configuration.form.summary.downloader.CSVSections;
import com.tbscg.doosan.magnolia.forms.configuration.form.summary.downloader.LoaderInput;
import com.tbscg.doosan.magnolia.forms.configuration.form.summary.loader.DynamoDBItem;
import com.tbscg.doosan.magnolia.forms.configuration.form.summary.loader.DynamoDBLoader;
import com.tbscg.doosan.magnolia.forms.configuration.form.summary.loader.FormRequest;
import com.tbscg.doosan.magnolia.forms.configuration.form.summary.parsers.DynamoDBItemsToCsvSectionParser;
import com.tbscg.doosan.magnolia.regions.model.Region;
import com.tbscg.doosan.magnolia.regions.service.RegionNotFoundException;
import com.tbscg.doosan.magnolia.regions.service.RegionsService;
import com.vaadin.data.Binder;
import com.vaadin.data.BinderValidationStatus;
import com.vaadin.ui.DateField;
import info.magnolia.ui.ValueContext;
import info.magnolia.ui.editor.EditorView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("DownloadFormSummary class tests")
class DownloadFormSummaryTest {
    @Mock
    private DownloadFormSummaryDefinition definition;

    @Mock
    private EditorView<DateField> form;

    @Mock
    private RegionsService regionService;

    @Mock
    private DynamoDBLoader dataLoader;

    @Mock
    private CSVDownloader formDownloader;

    @Mock
    private DynamoDBItemsToCsvSectionParser parser;

    @Mock
    private ValueContext<Node> valueContext;

    private DownloadFormSummary downloadFormSummary;
    private List<DynamoDBItem> dynamoDBItems;

    @BeforeEach
    void init() throws RepositoryException {
        MockitoAnnotations.openMocks(this);
        Node node = mock(Node.class);
        CSVSections csvSections = CSVSections.builder().withHeaders(Arrays.asList("one", "two")).build();

        dynamoDBItems = Collections.singletonList(DynamoDBItem.builder().build());
        downloadFormSummary = new DownloadFormSummary(definition, form, valueContext, regionService, dataLoader, formDownloader, parser);

        when(node.getPath()).thenReturn("/test");
        when(valueContext.getSingleOrThrow()).thenReturn(node);
        when(dataLoader.getData(any())).thenReturn(dynamoDBItems);
        when(parser.parse(eq(dynamoDBItems), any(FormRequest.class))).thenReturn(csvSections);
    }

    @BeforeEach
    void initRegion() throws RegionNotFoundException {
        Region region = mock(Region.class);

        when(region.getName()).thenReturn("na");
        when(regionService.getRegionFromPath(any())).thenReturn(region);
    }

    @BeforeEach
    void initForm() {
        DateField dateField = mock(DateField.class);
        Binder.Binding binding = mock(Binder.Binding.class);
        Binder binder = mock(Binder.class);
        BinderValidationStatus<?> binderValidationStatus = mock(BinderValidationStatus.class);

        when(dateField.getValue()).thenReturn(LocalDate.of(2022, 1, 1));

        when(binding.getField()).thenReturn(dateField);
        when(binder.getBinding("from")).thenReturn(Optional.of(binding));
        when(binder.getBinding("to")).thenReturn(Optional.of(binding));

        when(binderValidationStatus.getBinder()).thenReturn(binder);
        when(binderValidationStatus.isOk()).thenReturn(true);

        when(form.validate()).thenReturn(Collections.singletonList(binderValidationStatus));
    }

    @Test
    public void testExecute() throws RegionNotFoundException {
        downloadFormSummary.execute();

        verify(form, times(1)).validate();
        verify(regionService, times(1)).getRegionFromPath(eq("/test"));
        verify(parser, times(1)).parse(eq(dynamoDBItems), any(FormRequest.class));
        verify(dataLoader, times(1)).getData(any(FormRequest.class));
        verify(valueContext, times(1)).getSingleOrThrow();

        ArgumentCaptor<LoaderInput<CSVSections>> loaderInputArgumentCaptor = ArgumentCaptor.forClass(LoaderInput.class);
        verify(formDownloader).initDownload(loaderInputArgumentCaptor.capture());
        LoaderInput<CSVSections> loaderInput = loaderInputArgumentCaptor.getValue();

        assertEquals("2022.01.01-2022.01.01.null.csv", loaderInput.getFileName());
        assertEquals(loaderInput.getData().getHeaders().size(), 2);
        assertEquals(loaderInput.getData().getHeaders().get(0), "one");
        assertEquals(loaderInput.getData().getHeaders().get(1), "two");
    }
}