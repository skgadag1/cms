package com.tbscg.doosan.magnolia.dxp.core.compatibility.app.availability;

import info.magnolia.context.MgnlContext;
import info.magnolia.ui.api.availability.AbstractAvailabilityRule;
import info.magnolia.ui.contentapp.browser.JcrContentClipboard;
import info.magnolia.ui.framework.ContentClipboard;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemId;

import javax.inject.Inject;
import javax.jcr.Session;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * {@link AbstractAvailabilityRule} that checks if content clipboard contains items of a specific node type.
 *
 * @author apuigescobar
 */
@Deprecated
public abstract class ClipboardContentIsNodeType extends AbstractAvailabilityRule {

    private final ContentClipboard<?> contentClipboard;

    @Inject
    public ClipboardContentIsNodeType(ContentClipboard<?> contentClipboard) {
        this.contentClipboard = contentClipboard;
    }

    /**
     * Checks if clipboard content is from any of the provided node types.
     *
     * @param nodeTypes node types to check.
     * @param workspace workspace where nodes are located.
     * @return whether clipboard content is from at least one of the provided node types or not.
     */
    public boolean isClipboardContentOfType(List<String> nodeTypes, String workspace) {
        Collection<JcrItemId> clipboardContent = this.getClipboardContent();

        return nodeTypes.stream().anyMatch(nodeType -> this.isClipboardContentOfType(clipboardContent, nodeType, workspace));
    }

    /**
     * Checks if given clipboard content is from a specific node type.
     *
     * @param clipboardContent clipboard content to check.
     * @param nodeType node type to assert.
     * @param workspace workspace where nodes are located.
     * @return whether clipboard content is from the provided node type or not.
     */
    private boolean isClipboardContentOfType(Collection<JcrItemId> clipboardContent, String nodeType, String workspace) {
        return !clipboardContent.isEmpty() &&
                clipboardContent.stream().allMatch(contentItem -> this.isValidContentItem(contentItem, nodeType, workspace));
    }

    /**
     * Checks if clipboard content is from a specific node type.
     *
     * @param nodeType node type to assert.
     * @param workspace workspace where nodes are located.
     * @return whether clipboard content is from the provided node type or not.
     */
    public boolean isClipboardContentOfType(String nodeType, String workspace) {

        Collection<JcrItemId> clipboardContent = this.getClipboardContent();

        return this.isClipboardContentOfType(clipboardContent, nodeType, workspace);
    }

    /**
     * Checks if content item (within clipboard content) is from a specific node type.
     *
     * @param contentItem content item to check.
     * @param nodeType node type to assert.
     * @param workspace workspace containing content item.
     * @return whether content item is from the provided node type or not.
     */
    private boolean isValidContentItem(Object contentItem, String nodeType, String workspace) {
        try {
            JcrItemId jcrItem = (JcrItemId) contentItem;
            Session session = MgnlContext.getJCRSession(workspace);
            return session.getNodeByIdentifier(jcrItem.getUuid()).getPrimaryNodeType().isNodeType(nodeType);

        } catch (Exception e) {
            //unable to access workspace or to get node
            return false;
        }
    }

    /**
     * Retrieves clipboard content. Returns an empty list in case that there is no clipboard content.
     *
     * @return clipboard content.
     */
    private Collection<JcrItemId> getClipboardContent() {

        Field jcrItemsField = this.getClipboardItemsField();

        if (jcrItemsField != null) {

            jcrItemsField.setAccessible(true);

            try {
                return (Collection<JcrItemId>) jcrItemsField.get(this.contentClipboard);

            } catch (Exception e) {/*silent exception*/}
        }

        return Collections.emptyList();
    }

    /**
     * Get the most accurate field that might contain content items.
     *
     * @return content items collection field. <code>null</code> if field doesn't exist.
     */
    private Field getClipboardItemsField() {
        return Stream.of(JcrContentClipboard.class.getDeclaredFields())
                .filter(this::isParameterizedAsJcrItem)
                .findFirst()
                .orElse(null);
    }

    /**
     * Checks if field is parameterized as JcrItem.
     *
     * @param field field to inspect.
     * @return whether a field contains content items.
     */
    private boolean isParameterizedAsJcrItem(Field field) {
        if (Stream.of(field.getGenericType().getClass().getGenericInterfaces())
                .anyMatch(type -> ParameterizedType.class.getName().equals(type.getTypeName()))) {

            return Stream.of(((ParameterizedType) field.getGenericType()).getActualTypeArguments())
                    .anyMatch(typeArg -> JcrItemId.class.getName().equals(typeArg.getTypeName()));
        }

        return false;
    }
}
