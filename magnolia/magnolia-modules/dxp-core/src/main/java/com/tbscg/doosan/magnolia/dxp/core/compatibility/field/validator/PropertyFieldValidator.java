package com.tbscg.doosan.magnolia.dxp.core.compatibility.field.validator;

import com.google.common.base.Preconditions;
import com.vaadin.v7.data.validator.AbstractStringValidator;
import info.magnolia.context.MgnlContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

/**
 * {@link com.vaadin.v7.data.Validator} JCR Property validator
 *
 * @author apuigescobar
 */
@Deprecated
public class PropertyFieldValidator extends AbstractStringValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyFieldValidator.class);

    private final String propertyName;
    private final String propertyValue;
    private final String workspace;

    public PropertyFieldValidator(String propertyName, String propertyValue, String workspace, String errorMessage) {
        super(errorMessage);
        this.propertyName = propertyName;
        this.propertyValue = propertyValue;
        this.workspace = workspace;
    }

    @Override
    protected boolean isValidValue(String nodeUuid) {
        try {
            return this.isMatchingProperty(nodeUuid);
        } catch (RepositoryException e) {
            LOGGER.warn("Error when accessing repository during node [{}] property validation", nodeUuid,e);
        }
        return false;
    }

    private boolean isMatchingProperty(String nodeUuid) throws RepositoryException {
        Preconditions.checkArgument(StringUtils.isNotBlank(this.workspace),
                String.format("%s: workspace cannot be blank", this.getClass().getSimpleName()));

        Session session = MgnlContext.getJCRSession(this.workspace);

        Node node = session.getNodeByIdentifier(nodeUuid);
        Value selPropertyValue = node.getProperty(this.propertyName).getValue();

        return selPropertyValue.getString().equals(this.propertyValue);
    }

    @Override
    public Class<String> getType() {
        return String.class;
    }
}
