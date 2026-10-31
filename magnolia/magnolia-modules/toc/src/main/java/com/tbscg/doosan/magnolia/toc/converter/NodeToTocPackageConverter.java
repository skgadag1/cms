package com.tbscg.doosan.magnolia.toc.converter;

import com.google.common.base.Preconditions;
import com.tbscg.doosan.magnolia.dxp.core.converter.BeanConversionException;
import com.tbscg.doosan.magnolia.dxp.core.converter.BeanConverter;
import com.tbscg.doosan.magnolia.dxp.core.i18n.LocalesWrapper;
import com.tbscg.doosan.magnolia.dxp.core.util.PropertyUtil;
import com.tbscg.doosan.magnolia.regions.service.LocaleService;
import com.tbscg.doosan.magnolia.toc.model.TocDocument;
import com.tbscg.doosan.magnolia.toc.model.TocPackage;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.SessionUtil;
import org.apache.jackrabbit.JcrConstants;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import java.util.*;

import static com.tbscg.doosan.magnolia.toc.util.TocConstants.TOC_WORKSPACE;


/**
 * {@link BeanConverter} for converting {@link Node} into {@link TocPackage}.
 *
 * @author apuigescobar
 */
@Singleton
public class NodeToTocPackageConverter implements BeanConverter<Node, TocPackage> {

    private static final String PROPERTY_TITLE = "title";
    private static final String PROPERTY_BRAND = "brand";
    private static final String PROPERTY_REGION = "region";
    private static final String PROPERTY_CHANNEL = "channel";
    private static final String PROPERTY_APPLICATION = "application";
    private static final String NODE_DOCUMENTS = "documents";

    private final NodeToTocDocumentConverter nodeToTocDocumentConverter;
    private final LocaleService localeService;

    @Inject
    public NodeToTocPackageConverter(NodeToTocDocumentConverter nodeToTocDocumentConverter, LocaleService localeService) {
        this.nodeToTocDocumentConverter = nodeToTocDocumentConverter;
        this.localeService = localeService;
    }

    /**
     * Converts JCR node to <code>TocPackage</code> model instance.
     *
     * @param fromNode node to convert into <code>TocPackage</code>.
     * @return <code>TocPackage</code> model instance constructed from node parameter, <code>null</code> if there is
     * any error during the conversion.
     * @throws BeanConversionException in case that something goes wrong when converting node to <code>TocPackage</code>.
     */
    @Override
    public TocPackage toBean(Node fromNode) throws BeanConversionException {

        try {
            return this.handleConversion(fromNode);

        } catch (Exception e) {
            throw new BeanConversionException("Unable to convert node to " + TocPackage.class.getSimpleName(), e);
        }
    }

    private TocPackage handleConversion(Node fromNode) throws RepositoryException {
        LocalesWrapper resolvedLocalesWrapper = localeService.resolveLocales(fromNode);
        Locale defaultLocale = resolvedLocalesWrapper.getDefaultLocale();
        List<Locale> locales = resolvedLocalesWrapper.getLocales();

        TocPackage tocPackage = new TocPackage();
        String id = PropertyUtil.getCheckedString(fromNode, JcrConstants.JCR_UUID, "ToC package ID cannot be blank");
        tocPackage.setId(id);

        Map<String, String> title = PropertyUtil.extractLocalizedProperties(fromNode, PROPERTY_TITLE, locales, defaultLocale, "ToC document title cannot be blank");

        tocPackage.setTitle(title);

        String brandName = PropertyUtil.getCheckedString(fromNode, PROPERTY_BRAND, "ToC package brandName cannot be blank");
        tocPackage.setBrand(brandName);

        String regionName = PropertyUtil.getCheckedString(fromNode, PROPERTY_REGION, "ToC package regionName cannot be blank");
        tocPackage.setRegion(regionName);

        String channel = PropertyUtil.getCheckedString(fromNode, PROPERTY_CHANNEL, "ToC package channel cannot be blank");
        tocPackage.setChannel(channel);

        String application = info.magnolia.jcr.util.PropertyUtil.getString(fromNode, PROPERTY_APPLICATION, null);
        tocPackage.setApplication(application);

        List<TocDocument> documents = this.getTocDocuments(fromNode);
        Preconditions.checkArgument(!documents.isEmpty(), "ToC package has to contain at least one document");
        tocPackage.setDocuments(documents);

        String version = PropertyUtil.getCheckedString(fromNode, NodeTypes.Activatable.LAST_ACTIVATED_VERSION, "ToC package content needs to be versioned");
        tocPackage.setVersion(version);

        Date lastModifiedDate = PropertyUtil.getCheckedDate(fromNode, NodeTypes.LastModified.LAST_MODIFIED, "Last modified date must exist");
        tocPackage.setLastModifiedDate(lastModifiedDate);

        String lastModifiedBy = PropertyUtil.getCheckedString(fromNode, NodeTypes.LastModified.LAST_MODIFIED_BY, "Last modified by cannot be blank");
        tocPackage.setLastModifiedBy(lastModifiedBy);
        return tocPackage;
    }

    private List<TocDocument> getTocDocuments(Node packageNode) throws RepositoryException {

        Value[] documentNodeIdValues = packageNode.getProperty(NODE_DOCUMENTS).getValues();

        List<TocDocument> documentsList = new ArrayList<>();

        for (Value documentNodeIdValue : documentNodeIdValues) {
            String documentNodeId = documentNodeIdValue.getString();
            Node documentNode = SessionUtil.getNodeByIdentifier(TOC_WORKSPACE, documentNodeId);
            TocDocument document = this.nodeToTocDocumentConverter.toBean(documentNode);
            documentsList.add(document);
        }

        return documentsList;
    }
}
