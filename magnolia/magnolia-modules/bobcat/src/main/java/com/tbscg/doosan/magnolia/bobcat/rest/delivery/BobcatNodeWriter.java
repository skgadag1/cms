/**
 * This file Copyright (c) 2017-2018 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 * <p>
 * <p>
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 * <p>
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 * <p>
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * <p>
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 * <p>
 * Any modifications to this file must keep this entire header
 * intact.
 */
package com.tbscg.doosan.magnolia.bobcat.rest.delivery;

import com.google.common.collect.Sets;
import com.tbscg.doosan.magnolia.bobcat.rest.model.CompatibleMachines;
import com.tbscg.doosan.magnolia.bobcat.rest.model.PageVisibility;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.rest.delivery.jcr.OutputStreamWrapper;
import info.magnolia.rest.delivery.jcr.v2.DeliveryContext;
import info.magnolia.rest.delivery.jcr.v2.JcrDeliveryEndpointDefinition;
import info.magnolia.rest.reference.ReferenceResolver;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.JcrConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.jcr.*;
import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.ws.rs.Priorities;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Predicate;

import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * Item content provider.
 * <p>
 * This provider due to increased priority should be picked up before original Magnolia
 * info.magnolia.rest.delivery.jcr.NodeWriter.
 * <p>
 * This custom implementation has one additional step defined and implemented in addUrlToPageNode method
 * which checks if resolver with propertyName equals to jcr:uuid has been defined for endpoint and if yes
 * then it resolves that property to page URL but only if that node type is mgnl:page.
 * <p>
 * Resolving jcr:uuid property by resolvers is not supported in original Node Writer.
 */
@Provider
@Priority(Priorities.HEADER_DECORATOR)
@Produces({MediaType.APPLICATION_JSON})
public class BobcatNodeWriter implements MessageBodyWriter<Node> {

    private static final Logger log = LoggerFactory.getLogger(BobcatNodeWriter.class);

    private static final String JCR_PAGE_TYPE = "mgnl:page";
    private static final String JCR_PRODUCT_TYPE = "dxp:product";
    private static final String JCR_PRODUCT_GROUP_TYPE = "dxp:productGroup";
    private static final String JCR_CATEGORY_TYPE = "dxp:category";
    private static final String JCR_SUBCATEGORY_TYPE = "dxp:subcategory";
    private static final String JCR_ATTACHMENT_TYPE = "dxp:attachment";
    private static final String JCR_ATTACHMENT_GROUP_TYPE = "dxp:attachmentGroup";
    private static final String JCR_ATTACHMENT_CATEGORY_TYPE = "dxp:attachmentCategory";
    private static final String JCR_NAVIGATION_LINK_TYPE = "dxp:navigationLink";
    private static final String JCR_NAVIGATION_ITEM_TYPE = "dxp:navigationItem";
    private static final String SAME_NAME_SIBLING_PREFIX = "_";
    private static final String META_PROPERTY_PREFIX = "@";
    private static final String PATH_PROPERTY = META_PROPERTY_PREFIX + "path";
    private static final String NAME_PROPERTY = META_PROPERTY_PREFIX + "name";
    private static final String CHILD_PROPERTY = META_PROPERTY_PREFIX + "nodes";
    private static final String UUID_PROPERTY = META_PROPERTY_PREFIX + "id";
    private static final String NODE_TYPE_PROPERTY = META_PROPERTY_PREFIX + "nodeType";
    private static final Integer DEFAULT_RESOLUTION_DEPTH = 1;
    private static final Set<String> additionalSpecialProps = Sets.newHashSet(JcrConstants.JCR_PRIMARYTYPE, JcrConstants.JCR_UUID);
    private static final String PAGE_VISIBILITY_CONTRACT_TYPE = "contractType";
    private static final String PAGE_VISIBILITY_CONTRACT_CODE = "contractCode";
    private static final String PAGE_VISIBILITY_DEALER_ROLE = "dealerRole";
    private static final String COMPATIBLE_MACHINES_FIELD = "machines";
    private static final String COMPATIBLE_MACHINE_NAME_FIELD = "name";

    @Context
    private Providers providers;

    @Override
    public boolean isWriteable(final Class<?> type, final Type genericType, final Annotation[] annotations, final MediaType mediaType) {
        return Node.class.isAssignableFrom(type);
    }

    @Override
    public long getSize(final Node node, final Class<?> type, final Type genericType, final Annotation[] annotations, final MediaType mediaType) {
        // As of JAX-RS 2.0, the method has been deprecated and the value returned by the method is ignored by a JAX-RS runtime.
        return -1;
    }

    @Override
    public void writeTo(final Node node, final Class<?> type, final Type genericType, final Annotation[] annotations, final MediaType mediaType, final MultivaluedMap<String, Object> httpHeaders, final OutputStream entityStream) throws IOException, WebApplicationException {
        final JsonGenerator jsonGenerator = entityStream instanceof OutputStreamWrapper ?
                ((OutputStreamWrapper) entityStream).getJsonGenerator() :
                Json.createGenerator(entityStream);

        final Stack<String> stack = new Stack<>();
        writeNode(jsonGenerator, node, null, true, stack, 0, annotations, entityStream, mediaType, httpHeaders);

        if (!OutputStreamWrapper.isWrapped(entityStream)) {
            jsonGenerator.flush();
            jsonGenerator.close();
        }
    }

    // package-private only for instrumentation in tests
    void writeNode(final JsonGenerator jsonGenerator, final Node node, final String objectKey, final boolean isResolveReference, final Stack<String> stack, final int depth, final Annotation[] annotations, final OutputStream entityStream, final MediaType mediaType, final MultivaluedMap<String, Object> httpHeaders) throws IOException {
        try {
            stack.push(node.getIdentifier());
        } catch (final RepositoryException e) {
            log.error(e.getMessage());
            throw new IOException(e);
        }

        try {
            if (objectKey != null) {
                jsonGenerator.writeStartObject(objectKey);
            } else {
                jsonGenerator.writeStartObject();
            }

            writeSpecialProperties(jsonGenerator, node);

            //Additional step. Rest of code hasn't changed.
            addCustomFieldToNode(CustomUrlJcrReferenceResolver.class::isInstance, "pageUrl", JCR_PAGE_TYPE, jsonGenerator, node, stack, depth, annotations, entityStream, mediaType, httpHeaders);
            addCustomFieldToNode(PageVisibilityResolver.class::isInstance, "pageVisibility", JCR_NAVIGATION_LINK_TYPE, jsonGenerator, node, stack, depth, annotations, entityStream, mediaType, httpHeaders);
            addCustomFieldToNode(PageVisibilityResolver.class::isInstance, "pageVisibility", JCR_NAVIGATION_ITEM_TYPE, jsonGenerator, node, stack, depth, annotations, entityStream, mediaType, httpHeaders);
            addCustomFieldToNode(PageTitleResolver.class::isInstance, "titleWithAffix", JCR_PAGE_TYPE, jsonGenerator, node, stack, depth, annotations, entityStream, mediaType, httpHeaders);
            addCustomFieldToNode(CompatibleAttachmentsCountResolver.class::isInstance, "compatibleAttachmentsCount", JCR_PRODUCT_TYPE, jsonGenerator, node, stack, depth, annotations, entityStream, mediaType, httpHeaders);
            addCustomFieldToNode(ProductParentResolver.class::isInstance, "parent", JCR_PRODUCT_TYPE, jsonGenerator, node, stack, depth, annotations, entityStream, mediaType, httpHeaders);
            addCustomFieldToNode(ProductParentResolver.class::isInstance, "parent", JCR_PRODUCT_GROUP_TYPE, jsonGenerator, node, stack, depth, annotations, entityStream, mediaType, httpHeaders);
            addCustomFieldToNode(ProductParentResolver.class::isInstance, "parent", JCR_CATEGORY_TYPE, jsonGenerator, node, stack, depth, annotations, entityStream, mediaType, httpHeaders);
            addCustomFieldToNode(ProductParentResolver.class::isInstance, "parent", JCR_SUBCATEGORY_TYPE, jsonGenerator, node, stack, depth, annotations, entityStream, mediaType, httpHeaders);
            addCustomFieldToNode(AttachmentParentResolver.class::isInstance, "parent", JCR_ATTACHMENT_TYPE, jsonGenerator, node, stack, depth, annotations, entityStream, mediaType, httpHeaders);
            addCustomFieldToNode(CompatibleMachinesResolver.class::isInstance, "compatibleMachines", JCR_ATTACHMENT_TYPE, jsonGenerator, node, stack, depth, annotations, entityStream, mediaType, httpHeaders);
            addCustomFieldToNode(AttachmentParentResolver.class::isInstance, "parent", JCR_ATTACHMENT_GROUP_TYPE, jsonGenerator, node, stack, depth, annotations, entityStream, mediaType, httpHeaders);
            addCustomFieldToNode(AttachmentParentResolver.class::isInstance, "parent", JCR_ATTACHMENT_CATEGORY_TYPE, jsonGenerator, node, stack, depth, annotations, entityStream, mediaType, httpHeaders);
            
            final PropertyIterator properties = node.getProperties();

            while (properties.hasNext()) {
                final Property property = properties.nextProperty();
                if (isAllowedJcrProperty(property.getName())) {
                    writeProperty(jsonGenerator, property, node.hasNode(property.getName()), isResolveReference, stack, depth, annotations, entityStream, mediaType, httpHeaders);
                }
            }

            /* Please note that the I18nDeliveryNodeWrapper can return duplicated values in some specific cases
                (when the default node is before a language-specific one)
             */
            final NodeIterator children = node.getNodes();

            // Get excluded node names from endpoint definition if available
            final Set<String> excludedNodeNames = getDeliveryContext()
                    .map(DeliveryContext::getEndpointDefinition)
                    .filter(BobcatConfiguredJcrDeliveryEndpointDefinition.class::isInstance)
                    .map(BobcatConfiguredJcrDeliveryEndpointDefinition.class::cast)
                    .map(BobcatConfiguredJcrDeliveryEndpointDefinition::getExcludeNodeNames)
                    .map(HashSet::new)
                    .orElseGet(HashSet::new);

            final Set<String> childNodeNames = new LinkedHashSet<>();
            while (children.hasNext()) {
                final Node child = children.nextNode();
                final String childNodeName = child.getName();
                
                // Skip excluded nodes
                if (excludedNodeNames.contains(childNodeName)) {
                    continue;
                }
                
                if (childNodeNames.contains(childNodeName)) {
                    continue;
                }
                writeNode(jsonGenerator, child, childNodeName, true, stack, depth, annotations, entityStream, mediaType, httpHeaders);
                childNodeNames.add(childNodeName);
            }

            jsonGenerator.write(CHILD_PROPERTY, Json.createArrayBuilder(childNodeNames).build());

            jsonGenerator.writeEnd();
        } catch (final RepositoryException re) {
            log.error(re.getMessage());
        }
        stack.pop();
    }

    private void addCustomFieldToNode(Predicate<Object> predicate, String customProperty, String nodePrimaryType, final JsonGenerator jsonGenerator, final Node node, final Stack<String> stack, final int depth, final Annotation[] annotations, final OutputStream entityStream, final MediaType mediaType, final MultivaluedMap<String, Object> httpHeaders) throws RepositoryException, IOException {
        String nodeType = node.getPrimaryNodeType().getName();

        if (nodeType.equalsIgnoreCase(nodePrimaryType)) {
            final Property property = node.getProperty(JcrConstants.JCR_UUID);
            final Optional<ReferenceResolver<?, ?>> resolverByType = getDeliveryContext()
                    .flatMap(deliveryContext -> deliveryContext
                            .getResolvers().values().stream()
                            .filter(predicate)
                            .findFirst());

            final Optional<? extends ReferenceResolver> referenceResolver = resolverByType.isPresent() ? resolverByType : getReferenceResolver(property);
            if (referenceResolver.isPresent()) {
                final String valueAsString = getValueString(property.getValue());
                final Optional<Object> reference = (Optional<Object>) referenceResolver.get().resolve(valueAsString);
                if (reference.isPresent()) {
                    writeResolvedReference(jsonGenerator, reference.get(), customProperty, stack, depth, annotations, entityStream, mediaType, httpHeaders);
                }
            }
        }
    }

    protected boolean isAllowedJcrProperty(final String propertyName) {
        return !additionalSpecialProps.contains(propertyName);
    }

    private void writeProperty(final JsonGenerator jsonGenerator, final Property property, final boolean shouldAddPrefix, final boolean isResolveReference, final Stack<String> stack, final int depth, final Annotation[] annotations, final OutputStream entityStream, final MediaType mediaType, final MultivaluedMap<String, Object> httpHeaders) throws RepositoryException, IOException {
        final String propertyName = (shouldAddPrefix ? SAME_NAME_SIBLING_PREFIX : "") + property.getName();

        final Optional<? extends ReferenceResolver> referenceResolver = getReferenceResolver(property);

        if (!property.isMultiple()) {
            final String valueAsString = getValueString(property.getValue());
            // Resolve references
            if (isResolveReference && referenceResolver.isPresent()) {
                final Optional<Object> reference = (Optional<Object>) referenceResolver.get().resolve(valueAsString);
                if (reference.isPresent() && canWriteReference(stack, valueAsString, depth)) {
                    writeResolvedReference(jsonGenerator, reference.get(), propertyName, stack, depth, annotations, entityStream, mediaType, httpHeaders);
                } else {
                    jsonGenerator.write(propertyName, valueAsString);
                }
            } else {
                jsonGenerator.write(propertyName, valueAsString);
            }
        } else {
            jsonGenerator.writeStartArray(propertyName);
            final Value[] values = property.getValues();

            final Set<String> resolvedValues = new HashSet<>();
            for (final Value value : values) {
                final String valueAsString = getValueString(value);
                // Resolve references
                if (isResolveReference && referenceResolver.isPresent()) {
                    final Optional<Object> reference = (Optional<Object>) referenceResolver.get().resolve(valueAsString);
                    if (reference.isPresent() && canWriteReference(stack, valueAsString, depth)) {
                        writeResolvedReference(jsonGenerator, reference.get(), null, stack, depth, annotations, entityStream, mediaType, httpHeaders);
                        resolvedValues.add(valueAsString);
                    } else if (reference.isPresent() && resolvedValues.contains(valueAsString)) {
                        // There may be a case that node has duplicate property somehow
                        writeResolvedReference(jsonGenerator, reference.get(), null, stack, depth, annotations, entityStream, mediaType, httpHeaders);
                    } else {
                        jsonGenerator.write(valueAsString);
                    }
                } else {
                    jsonGenerator.write(valueAsString);
                }
            }
            jsonGenerator.writeEnd();
        }
    }

    private boolean canWriteReference(final Stack<String> stack, final String value, final int depth) {
        final int resolutionDepth = getDeliveryContext()
                .map(DeliveryContext::getEndpointDefinition)
                .map(JcrDeliveryEndpointDefinition::getReferenceDepth)
                .orElse(DEFAULT_RESOLUTION_DEPTH);
        final boolean referenceRepeat = getDeliveryContext()
                .map(DeliveryContext::getEndpointDefinition)
                .map(JcrDeliveryEndpointDefinition::isReferenceRepeat)
                .orElse(false);
        return (!stack.contains(value) || referenceRepeat) && depth < resolutionDepth;
    }

    private Optional<? extends ReferenceResolver<?, ?>> getReferenceResolver(final Property property) {
        final Optional<DeliveryContext> context = getDeliveryContext();

        if (context.isPresent()) {
            // Reference resolver only supports resolving a property, doesn't support resolving a node yet
            return context.get().getResolvers().entrySet().stream()
                    .filter(entry -> entry.getKey().test(property))
                    .map(Map.Entry::getValue)
                    .findFirst();
        }

        return Optional.empty();
    }

    private Optional<DeliveryContext> getDeliveryContext() {
        if (providers == null) {
            return Optional.empty();
        }
        // Init reference context
        final ContextResolver<DeliveryContext> contextResolver = providers.getContextResolver(DeliveryContext.class, MediaType.WILDCARD_TYPE);
        final DeliveryContext context = contextResolver.getContext(DeliveryContext.class);
        return Optional.ofNullable(context);
    }

    private void writeResolvedReference(final JsonGenerator jsonGenerator, final Object resolvedObject, final String propertyName, final Stack<String> stack, int depth, final Annotation[] annotations, OutputStream entityStream, final MediaType mediaType, final MultivaluedMap<String, Object> httpHeaders) throws IOException, RepositoryException {
        if (resolvedObject instanceof Node) {
            writeNode(jsonGenerator, (Node) resolvedObject, propertyName, true, stack, ++depth, annotations, entityStream, mediaType, httpHeaders);
            return;
        }

        if (resolvedObject instanceof Property) {
            final Property resolvedProperty = (Property) resolvedObject;
            // Don't write object key when writing reference in a JSON array
            if (propertyName == null) {
                jsonGenerator.write(resolvedProperty.getString());
            } else {
                jsonGenerator.write(propertyName, resolvedProperty.getString());
            }
            return;
        }

        if (resolvedObject instanceof PageVisibility[] pageVisibilities) {
            if (propertyName != null) {
                jsonGenerator.writeStartArray(propertyName);
                Arrays.stream(pageVisibilities).forEach(pageVisibility -> {
                    jsonGenerator.writeStartObject();
                    jsonGenerator.write(PAGE_VISIBILITY_CONTRACT_TYPE, pageVisibility.getContractType());
                    jsonGenerator.write(PAGE_VISIBILITY_CONTRACT_CODE, pageVisibility.getContractCode());
                    jsonGenerator.write(PAGE_VISIBILITY_DEALER_ROLE, pageVisibility.getDealerRole());
                    jsonGenerator.writeEnd();
                });
                jsonGenerator.writeEnd();
            }
            return;
        }

        if (resolvedObject instanceof String) {
            // Don't write object key when writing reference in a JSON array
            if (propertyName == null) {
                jsonGenerator.write(resolvedObject.toString());
            } else {
                jsonGenerator.write(propertyName, resolvedObject.toString());
            }
            return;
        }

        if (resolvedObject instanceof Integer) {
            // Handle Integer values (e.g., from count resolvers)
            if (propertyName == null) {
                jsonGenerator.write((Integer) resolvedObject);
            } else {
                jsonGenerator.write(propertyName, (Integer) resolvedObject);
            }
            return;
        }

        if (resolvedObject instanceof Long) {
            // Handle Long values
            if (propertyName == null) {
                jsonGenerator.write((Long) resolvedObject);
            } else {
                jsonGenerator.write(propertyName, (Long) resolvedObject);
            }
            return;
        }

        if (resolvedObject instanceof CompatibleMachines resolvedCompatibleMachines) {
            writeCompatibleMachines(jsonGenerator, resolvedCompatibleMachines, propertyName);
            return;
        }

        if(resolvedObject instanceof Map) {
            final Map<String, Object> resolvedMap = (Map<String, Object>) resolvedObject;
            if (propertyName != null) {
                writeMap(jsonGenerator, resolvedMap, propertyName, stack, depth, annotations, entityStream, mediaType, httpHeaders);
            }
            return;
        }

        // Get the corresponding MessageBodyWriter for Object which is returned from Reference Resolver
        final MessageBodyWriter writer = providers.getMessageBodyWriter(resolvedObject.getClass(), resolvedObject.getClass(), annotations, mediaType);
        jsonGenerator.flush();

        if (propertyName != null) {
            jsonGenerator.writeKey(propertyName);
        }

        // Using OutputStreamWrapper to share the same JsonGenerator to MessageBodyWriter of resolved reference
        if (!(entityStream instanceof OutputStreamWrapper)) {
            entityStream = new OutputStreamWrapper(entityStream, jsonGenerator);
        }
        writer.writeTo(resolvedObject, resolvedObject.getClass(), resolvedObject.getClass(), annotations, mediaType, httpHeaders, entityStream);
    }

    private void writeCompatibleMachines(final JsonGenerator jsonGenerator,
                                         final CompatibleMachines compatibleMachines,
                                         final String propertyName) {
        if (propertyName == null) {
            return;
        }

        final List<CompatibleMachines.Machine> machines = compatibleMachines.getMachines() == null ?
                Collections.emptyList() : compatibleMachines.getMachines();

        jsonGenerator.writeStartObject(propertyName);

        jsonGenerator.writeStartArray(COMPATIBLE_MACHINES_FIELD);
        for (CompatibleMachines.Machine machine : machines) {
            jsonGenerator.writeStartObject();
            final String machineName = machine != null && machine.getName() != null ? machine.getName() : EMPTY;
            jsonGenerator.write(COMPATIBLE_MACHINE_NAME_FIELD, machineName);
            jsonGenerator.writeEnd();
        }
        jsonGenerator.writeEnd();
        jsonGenerator.writeEnd();
    }

    private void writeMap(JsonGenerator jsonGenerator, Map<String, Object> resolvedMap, String propertyName, Stack<String> stack, int depth, Annotation[] annotations, OutputStream entityStream, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders) {
        jsonGenerator.writeStartObject(propertyName);
        for (Map.Entry<String, Object> entry : resolvedMap.entrySet()) {
            try {
                writeResolvedReference(jsonGenerator, entry.getValue(), entry.getKey(), stack, depth, annotations, entityStream, mediaType, httpHeaders);
            } catch (IOException | RepositoryException e) {
                log.error("Error on writing resolved reference", e);
            }
        }
        jsonGenerator.writeEnd();
    }

    private void writeSpecialProperties(final JsonGenerator jsonGenerator, final Node node) throws RepositoryException {
        jsonGenerator.write(NAME_PROPERTY, node.getName());
        jsonGenerator.write(PATH_PROPERTY, node.getPath());
        jsonGenerator.write(UUID_PROPERTY, node.getIdentifier());
        jsonGenerator.write(NODE_TYPE_PROPERTY, node.getPrimaryNodeType().getName());
    }

    private String getValueString(final Value value) throws RepositoryException {
        switch (value.getType()) {
            case PropertyType.BINARY:
                return getBinaryString(value);
            case PropertyType.DECIMAL:
                return String.valueOf(value.getDecimal());
            case PropertyType.NAME:
            case PropertyType.URI:
            case PropertyType.DATE:
                return value.getString();
            default:
                return PropertyUtil.getValueString(value);
        }
    }

    /**
     * Gets a string representation of a {@link Value}.
     * {@link PropertyType#BINARY} types will be base64 encoded.
     *
     * @see Base64#encode(byte[])
     */
    private String getBinaryString(final Value propertyValue) throws RepositoryException {
        // Binary values have to be base64 encoded
        final byte[] decodedPropertyValue;
        try {
            decodedPropertyValue = Base64.encodeBase64(IOUtils.toByteArray(propertyValue.getBinary().getStream()));
        } catch (final IOException e) {
            log.error("Failed to get binary value from property.", e);
            throw new RepositoryException(e);
        }
        return new String(decodedPropertyValue);
    }
}
