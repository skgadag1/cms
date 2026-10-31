package com.tbscg.doosan.magnolia.toc.converter;

import com.tbscg.doosan.magnolia.dxp.core.converter.BeanConversionException;
import com.tbscg.doosan.magnolia.dxp.core.converter.BeanConverter;
import com.tbscg.doosan.magnolia.dxp.core.i18n.LocalesWrapper;
import com.tbscg.doosan.magnolia.dxp.core.util.PropertyUtil;
import com.tbscg.doosan.magnolia.regions.service.LocaleService;
import com.tbscg.doosan.magnolia.toc.model.TocDocument;
import info.magnolia.jcr.util.NodeTypes;
import org.apache.jackrabbit.JcrConstants;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.jcr.Node;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * {@link BeanConverter} for converting {@link Node} into {@link TocDocument}.
 *
 * @author apuigescobar
 */
@Singleton
public class NodeToTocDocumentConverter implements BeanConverter<Node, TocDocument> {

    private static final String PROPERTY_TITLE = "title";
    private static final String PROPERTY_CONTENT = "content";
    private static final String PROPERTY_VANITY_VERSION = "version";

    private final LocaleService localeService;

    @Inject
    public NodeToTocDocumentConverter(LocaleService localeService) {
        this.localeService = localeService;
    }

    /**
     * Converts JCR node to <code>TocDocument</code> model instance.
     *
     * @param fromNode node to convert into <code>TocDocument</code>.
     * @return <code>TocDocument</code> model instance constructed from node parameter, <code>null</code> if there is
     * any error during the conversion.
     * @throws BeanConversionException in case that something goes wrong when converting node to <code>TocDocument</code>.
     */
    @Override
    public TocDocument toBean(Node fromNode) throws BeanConversionException {
        try {
            return this.handleConversion(fromNode);

        } catch (Exception e) {
            throw new BeanConversionException("Unable to convert node to " + TocDocument.class.getSimpleName(), e);
        }
    }

    private TocDocument handleConversion(Node fromNode) {
        LocalesWrapper resolvedLocalesWrapper = localeService.resolveLocales(fromNode);
        Locale defaultLocale = resolvedLocalesWrapper.getDefaultLocale();
        List<Locale> locales = resolvedLocalesWrapper.getLocales();

        String id = PropertyUtil.getCheckedString(fromNode, JcrConstants.JCR_UUID, "ToC document ID cannot be blank");
        Map<String, String> title = PropertyUtil.extractLocalizedProperties(fromNode, PROPERTY_TITLE, locales, defaultLocale, "ToC document title cannot be blank");
        Map<String, String> content = PropertyUtil.extractLocalizedProperties(fromNode, PROPERTY_CONTENT, locales, defaultLocale, "ToC document content cannot be blank");
        String version = PropertyUtil.getCheckedString(fromNode, PROPERTY_VANITY_VERSION, "ToC document version cannot be blank");
        String lastModifiedBy = PropertyUtil.getCheckedString(fromNode, NodeTypes.LastModified.LAST_MODIFIED_BY, "ToC document has to be edited by someone");
        Date lastModifiedDate = PropertyUtil.getCheckedDate(fromNode, NodeTypes.LastModified.NAME, "ToC document modification date must exist");
        Date publicationDate = PropertyUtil.getCheckedDate(fromNode, NodeTypes.Activatable.LAST_ACTIVATED, "ToC document publication date must exist");

        return new TocDocument(id, title, content, version, lastModifiedBy, lastModifiedDate, publicationDate);
    }
}
