package com.tbscg.doosan.magnolia.product.catalog.extimport.buildandquote;

import lombok.val;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class DefaultBuildAndQuoteServiceTest {

    private static final String TEST_CATEGORY_NAME = "TestCategoryName";

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Mock
    private BuildAndQuoteClient buildAndQuoteClientMock;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void ctxShouldThrowOnNullClient() {
        // expect
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("buildAndQuoteClient is null");

        // when
        // noinspection ConstantConditions
        new DefaultBuildAndQuoteService(null);
    }

    @Test
    public void shouldCallBuildAndQuoteClient() {
        // given
        final DefaultBuildAndQuoteService buildAndQuoteService =
                defaultBuildAndQuoteService();

        // when
        buildAndQuoteService.getCategoryByName(TEST_CATEGORY_NAME);

        // then
        verify(buildAndQuoteClientMock).getCategoryList();
    }

    @Test
    public void shouldReturnCategoryByNameFromClient() {
        // given
        final DefaultBuildAndQuoteService buildAndQuoteService =
                defaultBuildAndQuoteService();

        final String name1 = "name1";
        final String name2 = "name2";

        final BuildAndQuoteCategory cat1 = BuildAndQuoteCategory.builder()
                .withProdCategoryId("id1")
                .withProdCategoryName(name1)
                .build();
        final BuildAndQuoteCategory cat2 = BuildAndQuoteCategory.builder()
                .withProdCategoryId("id2")
                .withProdCategoryName(name2)
                .build();

        final List<BuildAndQuoteCategory> categories = Arrays.asList(cat1, cat2);
        doReturn(categories).when(buildAndQuoteClientMock).getCategoryList();

        // when
        val category1 = buildAndQuoteService.getCategoryByName(name1);
        val category2 = buildAndQuoteService.getCategoryByName(name2);

        // then
        assertThat(assertHasValueAndGet(category1), equalTo(cat1));
        assertThat(assertHasValueAndGet(category2), equalTo(cat2));
    }

    @Test
    public void shouldCallClientOnlyOnce() {
        // given
        val buildAndQuoteService = defaultBuildAndQuoteService();

        // when
        buildAndQuoteService.getCategoryByName("foobar");
        buildAndQuoteService.getCategoryByName("bazbar");

        // then
        verify(buildAndQuoteClientMock, times(1)).getCategoryList();
    }

    private DefaultBuildAndQuoteService defaultBuildAndQuoteService() {
        return new DefaultBuildAndQuoteService(buildAndQuoteClientMock);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static <T> T assertHasValueAndGet(Optional<T> optional) {
        return optional.orElseThrow(() -> new AssertionError("Optional expected to have value here."));
    }
}
