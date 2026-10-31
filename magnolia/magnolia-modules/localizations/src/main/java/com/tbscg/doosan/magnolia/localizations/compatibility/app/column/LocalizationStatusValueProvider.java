package com.tbscg.doosan.magnolia.localizations.compatibility.app.column;

import com.tbscg.doosan.magnolia.localizations.model.status.LocalizationPackageStatus;
import com.tbscg.doosan.magnolia.localizations.model.status.LocalizationPublicationInfo;
import com.tbscg.doosan.magnolia.localizations.service.LocalizationPackageService;
import com.vaadin.data.ValueProvider;
import info.magnolia.cms.util.DateUtil;
import info.magnolia.ui.datasource.jcr.JcrNodeWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

import javax.inject.Inject;
import javax.jcr.Node;

/**
 * Value provider for the Localization Status column with HTML support.
 * Implements ValueProvider to safely render HTML content.
 * External content is escaped using StringEscapeUtils.escapeHtml4().
 */
@Slf4j
public class LocalizationStatusValueProvider implements ValueProvider<JcrNodeWrapper, String> {

    private final LocalizationPackageService localizationPackageService;

    @Inject
    public LocalizationStatusValueProvider(LocalizationPackageService localizationPackageService) {
        this.localizationPackageService = localizationPackageService;
        log.info("LocalizationStatusValueProvider constructed with dependencies injected");
    }

    @Override
    public String apply(JcrNodeWrapper jcrNodeWrapper) {
        if (jcrNodeWrapper == null) {
            log.debug("LocalizationStatusValueProvider.apply() called with null JcrNodeWrapper");
            return null;
        }

        try {
            Node node = jcrNodeWrapper.getWrappedNode();
            String nodePath = node.getPath();
            
            log.debug("Checking localization status for node: {}", nodePath);
            
            // Check if this is a localized package using the service
            if (!localizationPackageService.isLocalizedPackage(node)) {
                log.debug("Node [{}] is NOT a localized package", nodePath);
                return null;
            }

            log.info("Node [{}] IS a localized package, generating status HTML", nodePath);
            
            LocalizationPublicationInfo publicationInfo = new LocalizationPublicationInfo();
            node.accept(publicationInfo);
            
            String htmlResult = generatePublicationStatusHtml(publicationInfo) + generateErrorMessageHtml(publicationInfo);
            log.debug("Generated HTML for node [{}]", nodePath);
            
            return htmlResult;
        } catch (Exception e) {
            log.error("Error while reading localization package publication status", e);
            return null;
        }
    }

    /**
     * Generates the HTML displayed in the Localization Status column.
     * External content (tooltip) is properly escaped for security.
     *
     * @param publicationInfo All the information that needs to be displayed within the cell.
     * @return The formatted HTML to be displayed within the cell.
     */
    private String generatePublicationStatusHtml(LocalizationPublicationInfo publicationInfo) {
        String htmlResult = "<span class=\"" + publicationInfo.getStatus().getColor() + "\"";
        String tooltip = publicationInfo.getStatus().getDescription();
        if (publicationInfo.getLastPublication() != null) {
            tooltip += ". Last attempt: " + DateUtil.formatDateTime(publicationInfo.getLastPublication().getTime());
        }
        // Escape external content for security using StringEscapeUtils
        htmlResult += " title=\"" + StringEscapeUtils.escapeHtml4(tooltip) + "\"></span>";
        return htmlResult;
    }

    /**
     * Generates HTML to inform in case an error happened.
     * External content (error message) is properly escaped for security.
     *
     * @param publicationInfo All the information that needs to be displayed within the cell.
     * @return The formatted HTML for errors to be displayed within the cell.
     */
    private String generateErrorMessageHtml(LocalizationPublicationInfo publicationInfo) {
        if (publicationInfo.getStatus() == LocalizationPackageStatus.ERROR) {
            // Escape external content for security using StringEscapeUtils
            return "<span class=\"icon-error-l audit-error-localizationPackageStatus\" title=\""
                    + StringEscapeUtils.escapeHtml4(publicationInfo.getMessage())
                    + "\"></span>";
        } else {
            return StringUtils.EMPTY;
        }
    }
}
