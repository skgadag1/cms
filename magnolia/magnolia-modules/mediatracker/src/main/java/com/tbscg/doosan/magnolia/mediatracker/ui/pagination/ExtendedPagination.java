package com.tbscg.doosan.magnolia.mediatracker.ui.pagination;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class ExtendedPagination extends HorizontalLayout {

    private static final long serialVersionUID = 1L;

    final List<ExtendedPaginationChangeListener> listeners = new ArrayList<>();

    private ExtendedPaginationResource paginationResource;

    HorizontalLayout itemsPerPage;
    HorizontalLayout pageControls;

    final ComboBox<Integer> itemsPerPageSelect = new ComboBox();

    final Label currentPageLabel = new Label("1");
    final Label totalPageLabel = new Label();

    final Button firstButton = new Button(VaadinIcons.ARROW_CIRCLE_LEFT);
    final Button previousButton = new Button(VaadinIcons.ARROW_CIRCLE_LEFT_O);
    final Button nextButton = new Button(VaadinIcons.ARROW_CIRCLE_RIGHT_O);
    final Button lastButton = new Button(VaadinIcons.ARROW_CIRCLE_RIGHT);

    private final Label totalItemsLabel;

    private String[] currentSearchAfter = new String[]{};

    private String[] currentSearchBefore = new String[]{};


    public ExtendedPagination(ExtendedPaginationResource paginationResource) {
        setWidth("100%");
        setSpacing(true);
        totalItemsLabel = new Label();
        init(paginationResource);
    }

    protected void init(ExtendedPaginationResource resource) {
        if (getComponentCount() > 0) {
            removeAllComponents();
        }
        paginationResource = resource;
        itemsPerPage = createItemsPerPage();
        pageControls = createPageControlFields();
        addComponents(itemsPerPage, pageControls);
        setComponentAlignment(pageControls, Alignment.MIDDLE_RIGHT);
        setExpandRatio(pageControls, 1);
        buttonsEnabled();
        setItemsRange(resource);
        addComponent(totalItemsLabel);
        setComponentAlignment(totalItemsLabel, Alignment.MIDDLE_RIGHT);
    }

    /**
     * Method for updating items range label
     */
    public void setItemsRange() {
        setItemsRange(getExtendedPaginationResource());
    }

    public Integer getLimit() {
        return getExtendedPaginationResource().limit();
    }

    public void setItemsPerPage(int... perPage) {
        List<Integer> items = new ArrayList<>();
        for (int page : perPage) {
            items.add(page);
        }
        itemsPerPageSelect.setItems(items);
        itemsPerPageSelect.setSelectedItem(this.paginationResource.limit());
        if (!itemsPerPageSelect.isSelected(this.paginationResource.limit())) {
            throw new IllegalArgumentException("itemsPerPageSelect.isSelected(paginationResource.size()) not found!");
        }
    }

    public void setTotalCount(long total) {
        paginationResource.setTotal(total);
        totalPageLabel.setValue(String.valueOf(paginationResource.totalPage()));
        buttonsEnabled();
    }

    public void setItemsPerPageEnabled(boolean enabled) {
        itemsPerPage.setEnabled(enabled);
    }

    public void setItemsPerPageVisible(boolean enabled) {
        itemsPerPage.setVisible(enabled);
        setPageControlsAlignment(Alignment.MIDDLE_CENTER);
    }

    public void addPageChangeListener(ExtendedPaginationChangeListener listener) {
        listeners.add(listener);
    }

    public void removePageChangeListener(ExtendedPaginationChangeListener listener) {
        listeners.remove(listener);
    }

    public void setCurrentPage(int page) {
        currentPageLabel.setValue(String.valueOf(page));
    }

    public void firstClick() {
        firstButton.click();
    }

    public void previousClick() {
        previousButton.click();
    }

    public void nextClick() {
        nextButton.click();
    }

    public void lastClick() {
        lastButton.click();
    }

    private void setItemsPerPageAlignment(Alignment alignment) {
        setComponentAlignment(itemsPerPage, alignment);
    }

    private void setPageControlsAlignment(Alignment alignment) {
        setComponentAlignment(pageControls, alignment);
    }

    private HorizontalLayout createItemsPerPage() {
        final Label itemsPerPageLabel = new Label("&nbsp;Items per page", ContentMode.HTML);
        itemsPerPageSelect.setTextInputAllowed(false);
        itemsPerPageSelect.setEmptySelectionAllowed(false);
        itemsPerPageSelect.setWidth("80px");
        itemsPerPageSelect.setStyleName(ValoTheme.COMBOBOX_SMALL);
        itemsPerPageSelect.addValueChangeListener(event -> {
            int pageSize = event.getValue();
            if (pageSize == paginationResource.limit()) {
                return;
            }
            paginationResource.setLimit(event.getValue());
            paginationResource.setPage(1);
            firePagedChangedEvent();
        });
        final HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(true);
        layout.addComponents(itemsPerPageLabel, itemsPerPageSelect);
        return layout;
    }

    @SuppressWarnings("serial")
    private HorizontalLayout createPageControlFields() {
        firstButton.setStyleName(ValoTheme.BUTTON_LINK);
        previousButton.setStyleName(ValoTheme.BUTTON_LINK);
        nextButton.setStyleName(ValoTheme.BUTTON_LINK);
        lastButton.setStyleName(ValoTheme.BUTTON_LINK);

        firstButton.addClickListener(e -> {
            ExtendedPaginationResource first = paginationResource.first();
            buttonClickEvent(first);
        });

        previousButton.addClickListener(e -> {
            ExtendedPaginationResource previous = paginationResource.previous();
            buttonClickEvent(previous);
        });

        nextButton.addClickListener(e -> {
            ExtendedPaginationResource next = paginationResource.next();
            buttonClickEvent(next);
        });

        lastButton.addClickListener(e -> {
            ExtendedPaginationResource last = paginationResource.last();
            buttonClickEvent(last);
        });

        HorizontalLayout pageFields = createPageFields();

        final HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(true);
        layout.addComponents(firstButton, previousButton, pageFields, nextButton, lastButton);
        return layout;
    }

    @SuppressWarnings("serial")
    private HorizontalLayout createPageFields() {
        final Label pageLabel = new Label("Page&nbsp;", ContentMode.HTML);
        final Label sepLabel = new Label("&nbsp;/&nbsp;", ContentMode.HTML);
        totalPageLabel.setValue(String.valueOf(this.paginationResource.totalPage()));

        final HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(true);
        layout.addComponents(pageLabel, currentPageLabel, sepLabel, totalPageLabel);
        layout.setComponentAlignment(pageLabel, Alignment.MIDDLE_LEFT);
        layout.setComponentAlignment(currentPageLabel, Alignment.MIDDLE_LEFT);
        layout.setComponentAlignment(sepLabel, Alignment.MIDDLE_LEFT);
        layout.setComponentAlignment(totalPageLabel, Alignment.MIDDLE_LEFT);
        return layout;
    }

    private void setItemsRange(ExtendedPaginationResource resource) {
        Integer rangeStart = 1 + (resource.page() - 1) * resource.limit();
        Integer total = Math.toIntExact(resource.total());
        Integer rangeEnd = resource.limit() * resource.page();
        String range = "%d - %d / %d";
        totalItemsLabel.setValue(String.format(range, rangeStart, rangeEnd > total ? total : rangeEnd, total));
    }

    protected void buttonClickEvent(ExtendedPaginationResource change) {
        paginationResource.setTotal(change.total());
        paginationResource.setPage(change.page());
        paginationResource.setLimit(change.limit());
        paginationResource.setInitIndex(change.initIndex());
        paginationResource.setButtonType(change.getButtonType());
        firePagedChangedEvent();
    }

    protected void firePagedChangedEvent() {
        buttonsEnabled();
        currentPageLabel.setValue(String.valueOf(paginationResource.page()));
        totalPageLabel.setValue(String.valueOf(paginationResource.totalPage()));
        if (listeners != null) {
            for (int i = 0; i < listeners.size(); i++) {
                ExtendedPaginationChangeListener listener = listeners.get(i);
                listener.changed(paginationResource);
            }
        }
        setItemsRange(paginationResource);
    }

    protected void buttonsEnabled() {
        firstButton.setEnabled(!this.paginationResource.isFirst());
        previousButton.setEnabled(this.paginationResource.hasPrevious());
        nextButton.setEnabled(this.paginationResource.hasNext());
        lastButton.setEnabled(!this.paginationResource.isLast());
    }

    protected Button getFirstButton() {
        return this.firstButton;
    }

    protected Button getPreButton() {
        return this.previousButton;
    }

    protected Button getNextButton() {
        return this.nextButton;
    }

    protected Button getLastButton() {
        return this.lastButton;
    }

    protected ExtendedPaginationResource getExtendedPaginationResource() {
        return this.paginationResource;
    }

    public void setCurrentSearchAfter(String[] currentSearchAfter) {
        this.currentSearchAfter = currentSearchAfter;
    }

    public void setCurrentSearchBefore(String[] currentSearchBefore) {
        this.currentSearchBefore = currentSearchBefore;
    }

    public String[] getCurrentSearchAfter() {
        return currentSearchAfter;
    }

    public String[] getCurrentSearchBefore() {
        return currentSearchBefore;
    }

    public ExtendedPaginationResource getPaginationResource() {
        return paginationResource;
    }
}
