package com.tbscg.doosan.magnolia.product.catalog.action;

import com.google.common.collect.ImmutableMap;
import com.tbscg.doosan.magnolia.product.catalog.extimport.DefaultProductImportService;
import com.tbscg.doosan.magnolia.product.catalog.extimport.PimCatalog;
import com.tbscg.doosan.magnolia.product.catalog.extimport.PimService;
import com.tbscg.doosan.magnolia.regions.model.Region;
import com.tbscg.doosan.magnolia.regions.service.RegionNotFoundException;
import com.tbscg.doosan.magnolia.regions.service.RegionsService;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.predicate.NodeTypePredicate;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.app.AppContext;
import info.magnolia.ui.api.message.Message;
import info.magnolia.ui.api.message.MessageType;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.function.FailableCallable;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.HashMap;
import java.util.Map;

/**
 * Action handler for importing categories & basic product information from external PIM systems.
 */
@Slf4j
public class ImportExternalModelAction extends AbstractAction<ImportExternalModelActionDefinition> {

    private final AppContext appContext;
    private final PimService pimService;
    private final DefaultProductImportService importService;
    private final RegionsService regionsService;
    private final JcrItemAdapter item;
    private Map<String, FailableCallable<PimCatalog, Exception>> sourceToGetCatalogMethodMap;

    @Inject
    public ImportExternalModelAction(
            final ImportExternalModelActionDefinition definition,
            final AppContext appContext,
            final PimService pimService,
            final JcrItemAdapter item,
            final DefaultProductImportService importService,
            final RegionsService regionsService) {
        super(definition);
        this.appContext = appContext;
        this.pimService = pimService;
        this.importService = importService;
        this.regionsService = regionsService;
        this.item = item;
    }

    @PostConstruct
    public void setUpSourceCatalog() {
        sourceToGetCatalogMethodMap = buildSourceToGetCatalogMethodMap();
    }

    @Override
    public void execute() throws ActionExecutionException {
        try {
            showInfoNotificationMessage("Import process started.");
            doImportExternalData();
            showInfoNotificationMessage("Import process finished.");
        } catch (Exception e) {
            throw new ActionExecutionException("Failed to import products.", e);
        }
    }

    private void doImportExternalData() throws Exception {
        log.debug("Importing external data: source = {}, workspace = {}, target = {}",
                getDefinition().getSource(), getDefinition().getWorkspace(), getDefinition().getTargetPath());
        if (getDefinition().getSource() == ImportSource.GPIM_PRODUCTS || getDefinition().getSource() == ImportSource.GPIM_ATTACHMENTS) {
            GPIMImportExternalData();
        } else {
            basicImportExternalData();
        }
    }

    private void GPIMImportExternalData() {
        Map<String, String> regionLocalesMap = getMapOfLocales();
        for (Map.Entry<String, String> entry : regionLocalesMap.entrySet()) {
            String key = entry.getKey();
            try {
                importService.importExternalCatalog(getCatalogSource("_" + key),
                        this.pimService::getRelationDetails,
                        getDefinition().getTargetPath() + "/" + key,
                        getDefinition().getWorkspace()
                );
            } catch (Exception err) {
                log.error(err.getMessage());
                showInfoNotificationMessage("Failed to import for region " + key);
            }
        }
    }

    private Map<String, String> getMapOfLocales() {
        try {
            final String currentPath = com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil.getPath(item.getJcrItem());
            if (currentPath.equals("/bobcat")) {
                Map<String, String> regionLocalesMap = getMapOfAlaoRegionLocales();
                regionLocalesMap.put("na", "en_US");
                return regionLocalesMap;
            }

            final Region region = regionsService.getRegionFromPath(currentPath);
            return ImmutableMap.of(region.getName(), "en_" + region.getCountryCode());
        } catch (RegionNotFoundException e) {
            log.error("Failed to get region from path", e);
            return new HashMap<>();
        }
    }

    private void basicImportExternalData() throws Exception {
        importService.importExternalCatalog(getCatalogSource(), this.pimService::getRelationDetails, getDefinition().getTargetPath(), getDefinition().getWorkspace());
    }

    private ImmutableMap<String, FailableCallable<PimCatalog, Exception>> buildSourceToGetCatalogMethodMap() {
        final ImmutableMap.Builder<String, FailableCallable<PimCatalog, Exception>> immutableMap = ImmutableMap.<String, FailableCallable<PimCatalog, Exception>>builder()
                .put(ImportSource.GPIM_PRODUCTS.label + "_na", () -> this.pimService.getGPIMProductCatalog("en_US"))
                .put(ImportSource.PIMPP_PRODUCTS.label, this.pimService::getEMEAProductCatalog)
                .put(ImportSource.GPIM_ATTACHMENTS.label + "_na", () -> this.pimService.getGPIMAttachmentCatalog("en_US"))
                .put(ImportSource.PIMPP_ATTACHMENTS.label, this.pimService::getEMEAAttachmentCatalog);

        final Map<String, String> regionLocalesMap = getMapOfAlaoRegionLocales();

        regionLocalesMap.forEach((key, value) -> immutableMap.put(
                ImportSource.GPIM_PRODUCTS.label + "_" + key,
                () -> this.pimService.getGPIMProductCatalog(value)
        ).put(
                ImportSource.GPIM_ATTACHMENTS.label + "_" + key,
                () -> this.pimService.getGPIMAttachmentCatalog(value)
        ));

        return immutableMap.build();
    }

    private Map<String, String> getMapOfAlaoRegionLocales() {
        final Map<String, String> regionLocalesMap = new HashMap<>();

        try {
            final Session session = MgnlContext.getJCRSession("regions");
            final Node regionRootNode = session.getNode("/bobcat");
            final Iterable<Node> regions = NodeUtil.collectAllChildren(regionRootNode, new NodeTypePredicate("dxp:region"));

            for (Node node : regions) {
                if (!(node.hasProperty("businessUnit") && node.getProperty("businessUnit").getString().equals("alao"))) {
                    continue;
                }
                final String code = getCountryCode(node);
                regionLocalesMap.put(node.getName(), "en_" + code.toUpperCase());
            }
        } catch (RepositoryException err) {
            log.error(err.getMessage());
        }

        return regionLocalesMap;
    }

    private String getCountryCode(Node node) throws RepositoryException {
        if (node.hasProperty("countryCode")) {
            return node.getProperty("countryCode").getString();
        }
        return node.getName();
    }

    private FailableCallable<PimCatalog, Exception> getCatalogSource() throws Exception {
        return getCatalogSource("");
    }

    private FailableCallable<PimCatalog, Exception> getCatalogSource(String key) throws Exception {
        final FailableCallable<PimCatalog, Exception> callable = sourceToGetCatalogMethodMap.get(getDefinition().getSource().label + key);
        if (callable == null) {
            throw new Exception("Could not import from unknown source: '" + getDefinition().getSource().label + key + "'");
        }
        return callable;
    }


    private void showInfoNotificationMessage(final String message) {
        appContext.sendLocalMessage(new Message(MessageType.INFO, "Import", message));
    }
}
