package com.tbscg.doosan.magnolia.forms.configuration.form.summary.downloader;

import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("CSVDownload class tests")
class CSVDownloaderTest {

    @Mock
    static Page page;

    final static CSVSections data = CSVSections.builder()
            .withHeaders(Arrays.asList("test", "test1"))
            .withRows(Collections.singletonList(Arrays.asList("t1", "t2")))
            .build();

    final static LoaderInput<CSVSections> loader = LoaderInput.<CSVSections>builder()
            .withData(data)
            .withFileName("testfile.txt")
            .build();

    final static SimpleDownloader expectedSimpleDownloader = new SimpleDownloader("test,test1\nt1,t2", "testfile.txt");
    private CSVDownloader downloader;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockStatic(Page.class);
        when(Page.getCurrent()).thenReturn(page);
        doNothing().when(page).open(any(Resource.class), anyString(), anyBoolean());
        downloader = new CSVDownloader();
    }

    @Test
    void initDownload() {
        // When
        downloader.initDownload(loader);

        // Then
        ArgumentCaptor<Resource> resourceCaptor = ArgumentCaptor.forClass(Resource.class);
        verify(page).open(resourceCaptor.capture(), eq("_parent"), eq(true));
        Resource capturedResource = resourceCaptor.getValue();
        assertEquals(expectedSimpleDownloader, capturedResource);
    }
}