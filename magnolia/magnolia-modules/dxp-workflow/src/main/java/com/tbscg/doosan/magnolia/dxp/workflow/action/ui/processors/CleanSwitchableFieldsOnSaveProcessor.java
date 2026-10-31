package com.tbscg.doosan.magnolia.dxp.workflow.action.ui.processors;

import com.tbscg.doosan.magnolia.dxp.core.util.SneakyWrappers;
import com.tbscg.doosan.magnolia.dxp.workflow.action.ui.processors.cleanswitchables.FormSwitchablesReport;
import com.tbscg.doosan.magnolia.dxp.workflow.action.ui.processors.cleanswitchables.FormSwitchablesReportGenerator;
import com.tbscg.doosan.magnolia.dxp.workflow.action.ui.processors.cleanswitchables.SwitchableFieldDesc;
import info.magnolia.ui.editor.FormDefinition;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * DxpActionProcessor that cleans switchable fields on save. This means it will purge all properties that are not used
 * in sub-form related to selected switchable field value.
 */
@Slf4j
public class CleanSwitchableFieldsOnSaveProcessor implements DxpActionProcessor {

    /**
     * Processes node before save.
     *
     * @param item           item to process
     * @param formDefinition form definition
     * @throws DxpActionProcessorException when processing fails
     */
    @Override
    public void process(final Node item, final FormDefinition<?> formDefinition) {
        try {
            log.debug("{} started processing form.", CleanSwitchableFieldsOnSaveProcessor.class);
            this.tryProcess(item, formDefinition);
            log.debug("{} finished processing form.", CleanSwitchableFieldsOnSaveProcessor.class);
        } catch (final RepositoryException e) {
            throw new DxpActionProcessorException("Failed to process item on save.", e);
        }
    }

    private void tryProcess(final Node item, final FormDefinition<?> formDefinition) throws RepositoryException {
        final FormSwitchablesReport formSwitchablesReport = getFormSwitchablesReport(formDefinition);
        log.debug("formSwitchablesReport: {}", formSwitchablesReport);

        if (this.processItem(item, formSwitchablesReport)) {
            log.debug("Saving item: {}", SneakyWrappers.getNodePath(item));
            item.getSession().save();
        }
    }

    private static FormSwitchablesReport getFormSwitchablesReport(final FormDefinition<?> formDefinition) {
        return new FormSwitchablesReportGenerator(formDefinition).build();
    }

    @SneakyThrows
    private boolean processItem(final Node item, final FormSwitchablesReport formSwitchablesReport) {
        log.debug("Processing item: {}", SneakyWrappers.getNodePath(item));

        final List<SwitchableFieldDesc> fieldDescriptors = formSwitchablesReport.getFieldDescList().stream()
                .filter(switchableFieldDesc -> switchableFieldDesc.pathMatches(SneakyWrappers.getNodePath(item)))
                .collect(Collectors.toList());
        log.debug("fieldDescriptors: {}", fieldDescriptors);

        final boolean isSaveRequired;
        if (fieldDescriptors.isEmpty()) {
            log.debug("No matching switchable field descriptor found for item: {}", SneakyWrappers.getNodePath(item));
            isSaveRequired = false;
        } else {
            isSaveRequired = fieldDescriptors.stream().findFirst().map(sfd -> {
                log.debug("Matched field definition: {}", sfd);
                return processSwitchableField(item, sfd);
            }).orElse(false);
        }
        log.debug("isSaveRequired: {}", isSaveRequired);

        final boolean isSubNodeSaveRequired = this.processItems(SneakyWrappers.getDirectNodes(item), formSwitchablesReport);
        log.debug("isSubNodeSaveRequired: {}", isSubNodeSaveRequired);

        return isSaveRequired || isSubNodeSaveRequired;
    }

    private boolean processItems(final Iterable<? extends Node> nodeCollection,
                                 final FormSwitchablesReport formSwitchablesReport) {
        boolean isSubNodeSaveRequired = false;
        for (final Node node : nodeCollection) {
            final boolean processResult = this.processItem(node, formSwitchablesReport);
            isSubNodeSaveRequired = processResult || isSubNodeSaveRequired;
        }
        return isSubNodeSaveRequired;
    }

    @SneakyThrows
    private static boolean processSwitchableField(
            final Node item,
            final SwitchableFieldDesc switchableFieldDesc) {
        log.debug("Processing switchable field: {}", SneakyWrappers.getNodePath(item));
        if (!item.hasProperty("field")) {
            log.debug("Skipping switchable field: {}. No field property.", SneakyWrappers.getNodePath(item));
            return false;
        }

        final Set<String> propertyNamesToRemove = getPropertyNamesToRemove(
                item.getProperty("field").getString(),
                switchableFieldDesc.getValueToPropertyList());
        log.debug("propertyNamesToRemove: {}", propertyNamesToRemove);

        return removeProperties(item, propertyNamesToRemove);
    }

    private static Set<String> getPropertyNamesToRemove(final String fieldValue, final Map<String, ? extends List<String>> valueToPropertyList) {
        final Set<String> propertyNamesToRemove = valueToPropertyList
                .values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        final List<String> currentOptionFields = valueToPropertyList.get(fieldValue);
        if(currentOptionFields == null){
            return propertyNamesToRemove;
        }
        for (final String currentOptionField : currentOptionFields) {
            propertyNamesToRemove.remove(currentOptionField);
        }
        return propertyNamesToRemove;
    }

    private static boolean removeProperties(final Node item, final Iterable<String> propertyNamesToRemove) throws RepositoryException {
        boolean isSaveRequired = false;

        log.debug("Removing properties: {}", propertyNamesToRemove);
        for (final String propertyName : propertyNamesToRemove) {
            if (!item.hasProperty(propertyName)) {
                log.debug("Skipping property: {}. No such property.", propertyName);
                continue;
            }

            log.debug("Removing property: {}", propertyName);
            item.getProperty(propertyName).remove();
            isSaveRequired = true;
        }
        return isSaveRequired;
    }

}

