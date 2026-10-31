package com.tbscg.doosan.magnolia.dxp.seo.audit;

import com.tbscg.doosan.magnolia.cloudinary.api.ApiObject;
import com.tbscg.doosan.magnolia.cloudinary.api.CloudinaryService;
import com.tbscg.doosan.magnolia.cloudinary.templating.CloudinaryTemplatingFunctions;
import info.magnolia.dam.api.Asset;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.services.seo.audit.factories.UrlValidatorFactory;
import info.magnolia.services.seo.audit.impl.LinkAuditor;
import info.magnolia.services.seo.audit.impl.LinkValidation;
import info.magnolia.services.seo.audit.impl.PageLink;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class CloudinaryLinkAuditor extends LinkAuditor {

    @Inject
    private CloudinaryService cloudinaryService;
    @Inject
    private CloudinaryTemplatingFunctions cloudinaryTemplatingFunctions;

    @Inject
    public CloudinaryLinkAuditor(UrlValidatorFactory urlValidatorFactory) {
        super(urlValidatorFactory);
    }

    @Override
    public void doAuditNode(Node page, Node results, String content) throws RepositoryException {
        Map<String,PageLink> linkMap = new HashMap<>();
        Document document = Jsoup.parse(content);

        getHrefLinks(page, linkMap, document);
        getSrcLinks(page, linkMap, document);
        getImportLinks(page, linkMap, document);

        checkLinks(linkMap);

        checkCloudinaryLinks(page, linkMap);

        saveResults(page, results, linkMap);
    }

    private void checkCloudinaryLinks(Node page, Map<String, PageLink> linkMap) throws RepositoryException {
        log.debug("Checking path for assets: {}", NodeUtil.getPathIfPossible(page));
        PropertyIterator propertyIterator = page.getProperties();
        while(propertyIterator.hasNext()) {
            Property property = propertyIterator.nextProperty();
            if(property.getString().startsWith("cloudinary:")) {
                log.info("Found cloudinary reference: {}", property.getValue().getString());
                PageLink link = getAssetLink(page, property.getValue().getString(), linkMap);
                checkAssetLink(link);
            }
        }

        Iterable<Node> nodeList = getComponents(page);
        for(Node node: nodeList)
            checkCloudinaryLinks(node, linkMap);

    }

    private void checkAssetLink(PageLink link) {
        Asset asset = cloudinaryTemplatingFunctions.getAsset(link.getUrl());
        if(asset == null) {
            log.debug("Invalid URL");
            link.setResult(LinkValidation.INVALID_URL);
        } else {
            link.setUrl(asset.getPath());
            log.debug("Checking Cloudinary URL: {}", link.getUrl());
            ApiObject apiObject = cloudinaryService.getAsset(link.getUrl());
            if(apiObject == null) {
                log.debug("Error while retrieving the asset from Cloudinary");
                link.setResult(LinkValidation.CONNECTION_ERROR);
            } else {
                log.debug("Asset OK");
                link.setResult(LinkValidation.VALID_RESPONSE);
            }
        }
    }

    private PageLink getAssetLink(Node page, String cloudinaryKey, Map<String, PageLink> linkMap) {
        PageLink pageLink = new PageLink(page, cloudinaryKey);
        linkMap.put(pageLink.getUrl(), pageLink);
        return pageLink;
    }

    private Iterable<Node> getComponents(Node page) throws RepositoryException {
        return NodeUtil.getNodes(page, o -> {
            if(o instanceof Node) {
                Node node = (Node) o;
                try {
                    return !node.getPrimaryNodeType().getName().equals(NodeTypes.Page.NAME);
                } catch (RepositoryException e) {
                    log.error(e.getLocalizedMessage(), e);
                    return false;
                }
            }
            return true;
        });
    }
}
