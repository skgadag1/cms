package com.tbscg.doosan.magnolia.concepts.service.impl;

import com.google.common.base.Preconditions;
import com.tbscg.doosan.magnolia.concepts.service.ConceptNotFoundException;
import com.tbscg.doosan.magnolia.concepts.service.ConceptsService;
import com.tbscg.doosan.magnolia.concepts.util.ConceptsConstants;
import com.tbscg.doosan.magnolia.dxp.core.util.JcrUtil;
import com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil;
import com.tbscg.doosan.magnolia.dxp.core.util.PropertyUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.wrapper.I18nNodeWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * Default {@link ConceptsService} implementation.
 *
 * @author apuigescobar
 */
@Slf4j
public class ConceptsServiceImpl implements ConceptsService {

    private static final String ERROR_CONCEPT_INVALID = "Invalid concept value";
    private static final String DEFAULT_LANGUAGE_SUFFIX = StringUtils.EMPTY;

    @Override
    public String getConcept(String pathOrId) throws ConceptNotFoundException {
        Preconditions.checkArgument(!StringUtils.isBlank(pathOrId), "Path or UUID is empty");

        if (JcrUtil.isUUID(pathOrId)) {
            return this.getConceptByUUID(pathOrId);
        }

        return this.getConceptByPath(pathOrId);
    }

    @Override
    public String getConceptByPath(String path) throws ConceptNotFoundException {
        Preconditions.checkArgument(StringUtils.isNotBlank(path), "Node path can't be blank");

        try {
            Node conceptNode = MgnlContext.getJCRSession(ConceptsConstants.CONCEPTS_WORKSPACE).getNode(path);
            I18nNodeWrapper i18nConceptNode = new I18nNodeWrapper(conceptNode);

            return getRichOrPlainConcept(i18nConceptNode, DEFAULT_LANGUAGE_SUFFIX);

        } catch (Exception e) {
            throw new ConceptNotFoundException("Unable to find concept for path " + path, e);
        }
    }

    @Override
    public String getConceptByUUID(String uuid) throws ConceptNotFoundException {
        Preconditions.checkArgument(StringUtils.isNotBlank(uuid), "UUID can't be blank");
        try {
            Node conceptNode = MgnlContext.getJCRSession(ConceptsConstants.CONCEPTS_WORKSPACE).getNodeByIdentifier(uuid);
            I18nNodeWrapper i18nConceptNode = new I18nNodeWrapper(conceptNode);

            return getRichOrPlainConcept(i18nConceptNode, DEFAULT_LANGUAGE_SUFFIX);

        } catch (Exception e) {
            throw new ConceptNotFoundException("Unable to find concept for UUID " + uuid, e);
        }
    }

    @Override
    public String getConcept(Node conceptNode) throws ConceptNotFoundException {
        Preconditions.checkNotNull(conceptNode, "Concept node can't be null");
        try {
            return getRichOrPlainConcept(conceptNode, DEFAULT_LANGUAGE_SUFFIX);

        } catch (Exception e) {
            throw new ConceptNotFoundException("Unable to retrieve concept value for concept node " + NodeUtil.getPath(conceptNode));
        }
    }

    @Override
    public String getLocalizedConcept(Node conceptNode, String language) {
        Preconditions.checkNotNull(conceptNode, "Concept node can't be null");
        Preconditions.checkArgument(StringUtils.isNotBlank(language), "Supplied language can't be blank");
        try {
            final String translatedConcept = getRichOrPlainConcept(conceptNode, "_" + language);
            if (!translatedConcept.isEmpty()) {
                return translatedConcept;
            }
        } catch (Exception e) {
            log.debug("Unable to find localized content for node {} and language {}", NodeUtil.getPath(conceptNode),
                    language);
        }

        //fallback to default language
        try {
            return this.getConcept(conceptNode);
        } catch (ConceptNotFoundException e) {
            log.debug("Unable to find localized content for node {} and default language", NodeUtil.getPath(conceptNode));
        }
        return StringUtils.EMPTY;
    }

    private static String getRichOrPlainConcept(Node concept, String languageSuffix) {
        final String plainValue = PropertyUtil.tryGetStringValue(concept, ConceptsConstants.NodeTypes.Concept.Properties.PLAIN_TEXT_VALUE + languageSuffix);
        if (!plainValue.isEmpty()) {
            return plainValue;
        }
        return PropertyUtil.tryGetStringValue(concept, ConceptsConstants.NodeTypes.Concept.Properties.RICH_TEXT_VALUE + languageSuffix);
    }

    @Override
    public boolean isConceptFolderEmpty(String path) {
        try {
            Node conceptFolder = MgnlContext.getJCRSession(ConceptsConstants.CONCEPTS_WORKSPACE).getNode(path);
            return conceptFolder.getNodes().getSize() == 0;
        } catch (Exception e) {
            log.error("Error on checking if concept folder is empty", e);
        }
        return true;
    }
}

