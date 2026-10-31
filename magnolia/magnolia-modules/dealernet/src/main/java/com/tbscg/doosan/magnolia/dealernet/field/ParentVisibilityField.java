package com.tbscg.doosan.magnolia.dealernet.field;

import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import info.magnolia.ui.editor.FormDefinition;
import info.magnolia.ui.editor.FormView;
import info.magnolia.ui.editor.LocaleContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;

import javax.jcr.Node;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.tbscg.doosan.magnolia.dxp.core.util.VisibilityUtil.getParentVisibilityNodes;

@Slf4j
public class ParentVisibilityField<T> extends FormView<T> {
    protected final ParentVisibilityFieldDefinition<T> formDefinition;

    public ParentVisibilityField(FormDefinition<T> formDefinition, LocaleContext localeContext) {
        super(formDefinition, localeContext);
        this.formDefinition = (ParentVisibilityFieldDefinition<T>) formDefinition;
    }

    @Override
    public void populate(T item) {
        super.populate(item);
        final Layout layout = (Layout) super.asVaadinComponent();
        List<GridItem> gridItems = fetchParentVisibilityItems(item);
        if (!gridItems.isEmpty()) {
            layout.addComponents(getGrid(gridItems));
        } else {
            layout.addComponents(getErrorMsg("No parent visibility found"));
        }
        layout.setSizeFull();
    }

    private Component getErrorMsg(String msg) {
        return new Label(msg, ContentMode.HTML);
    }

    private Component getGrid(List<GridItem> gridItems) {
        Grid grid = createGrid();
        grid.setItems(gridItems);
        grid.setWidthFull();
        grid.setHeightUndefined();
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        return grid;
    }

    protected Grid createGrid() {
        Grid grid = new Grid(GridItem.class);
        grid.setColumns("title", "path");
        return grid;
    }

    protected List<GridItem> fetchParentVisibilityItems(T item) {
        Node node = (Node) item;
        String rootPath = Optional.ofNullable(formDefinition.getRoot()).orElse(Strings.EMPTY);
        return getParentVisibilityNodes(node, rootPath).stream()
                .map(GridItem::new)
                .distinct()
                .collect(Collectors.toList());
    }
}
