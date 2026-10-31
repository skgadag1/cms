package com.tbscg.doosan.magnolia.dxp.core.util;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.predicate.AbstractPredicate;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.PropertyUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang.StringUtils;

import javax.jcr.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static info.magnolia.jcr.util.NodeUtil.getPathIfPossible;
import static org.apache.jackrabbit.ocm.repository.NodeUtil.PATH_SEPARATOR;

/**
 * Utility class for facilitating operations with JCR nodes.
 *
 * @author Alejandro Puig Escobar
 */
@Slf4j
public class NodeUtil {

    private static final String NODE_NAME_FIELD_TYPE = "jcrName";

    /**
     * Get a list of all children nodes from the node provided as parameter.
     *
     * @param node provided {@link Node}.
     * @return Collection of children nodes {@link Collection} {@link Node}.
     * @throws RepositoryException in case of exception accessing the JCR repository.
     */
    public static Collection<Node> getNodes(Node node) throws RepositoryException {

        Collection<Node> nodeList = new ArrayList<>();

        NodeIterator nodeIterator = node.getNodes();

        if (!nodeIterator.hasNext()) {
            return nodeList;
        }

        while (nodeIterator.hasNext()) {
            Node childNode = nodeIterator.nextNode();
            nodeList.addAll(getNodes(childNode));
            nodeList.add(childNode);
        }

        return nodeList;
    }

    /**
     * Get a list of nodes of all children nodes from a node that have a specific nodeType.
     *
     * @param node     provided {@link Node}.
     * @param nodeType node type to match.
     * @return Collection of children nodes {@link Collection} {@link Node}.
     * @throws RepositoryException in case of exception accessing the JCR repository.
     */
    public static Collection<Node> getNodes(Node node, String nodeType) throws RepositoryException {

        Collection<Node> nodeList = new ArrayList<>();

        NodeIterator nodeIterator = node.getNodes();

        if (!nodeIterator.hasNext()) {
            return nodeList;
        }

        while (nodeIterator.hasNext()) {
            Node childNode = nodeIterator.nextNode();

            nodeList.addAll(getNodes(childNode, nodeType));

            if (childNode.getPrimaryNodeType().getName().equals(nodeType)) {
                nodeList.add(childNode);
            }
        }

        return nodeList;
    }

    /**
     * Get a list of all children nodes from the node provided as parameter
     * and the node provided as parameter with specific nodeType.
     *
     * @param node     provided {@link Node}.
     * @param nodeType node type to match.
     * @return Collection of children nodes with parent {@link Collection} {@link Node}.
     * @throws RepositoryException in case of exception accessing the JCR repository.
     */
    public static Collection<Node> getNodesWithParent(Node node, String nodeType) throws RepositoryException {
        Collection<Node> nodeList = new ArrayList<>();
        nodeList.add(node);
        nodeList.addAll(NodeUtil.getNodes(node, nodeType));
        return nodeList;
    }

    /**
     * Checks if two nodes have ancestor/descendant relationship.
     *
     * @param supposedAncestor   node which is supposed to be an ancestor of the provided child.
     * @param supposedDescendant node which is supposed to be a descendant of the provided ancestor.
     * @return whether the supposed ancestor is a real ancestor of the supposed child or not.
     * @throws RepositoryException in case of exception accessing the JCR repository.
     */
    public static boolean isAncestor(Node supposedAncestor, Node supposedDescendant) throws RepositoryException {

        Collection<Node> supposedAncestorChildren = NodeUtil.getNodes(supposedAncestor);

        return supposedAncestorChildren.stream().anyMatch(child -> NodeUtil.isSameNode(child, supposedDescendant));
    }

    private static boolean isSameNode(Node node, Node toCompareNode) {
        try {
            return node.getIdentifier().equals(toCompareNode.getIdentifier());

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get a list of nodes of direct children nodes (1st level depth) from the node provided as parameters.
     *
     * @param node {@link Node}
     * @return List of direct children nodes
     * @throws RepositoryException in case of exception accessing the JCR repository.
     */
    public static Collection<Node> getDirectNodes(Node node) throws RepositoryException {
        Collection<Node> nodeList = new ArrayList<>();

        NodeIterator nodeIterator = node.getNodes();

        while (nodeIterator.hasNext()) {
            Node childNode = nodeIterator.nextNode();
            nodeList.add(childNode);
        }

        return nodeList;
    }

    public static Collection<Node> tryGetDirectNodes(Node node) {
        Collection<Node> nodeList = new ArrayList<>();
        try {
            NodeIterator nodeIterator = node.getNodes();
            while (nodeIterator.hasNext()) {
                Node childNode = nodeIterator.nextNode();
                nodeList.add(childNode);
            }
        } catch (RepositoryException e) {
        }

        return nodeList;
    }

    public static Optional<Node> tryGetDirectNode(Node node, String path) {
        try {
            return Optional.ofNullable(node.getNode(path));
        } catch (RepositoryException e) {
            return Optional.empty();
        }
    }

    /**
     * Get a list of nodes of direct children nodes (1st level depth) from the node provided as parameters.
     *
     * @param node {@link Node}
     * @return List of direct children nodes
     * @throws RepositoryException in case of exception accessing the JCR repository.
     */
    public static Collection<Node> getDirectNodes(Node node, String nodeType) throws RepositoryException {
        Collection<Node> nodeList = new ArrayList<>();

        NodeIterator nodeIterator = node.getNodes();

        while (nodeIterator.hasNext()) {
            Node childNode = nodeIterator.nextNode();
            if (childNode.getPrimaryNodeType().getName().equals(nodeType)) {
                nodeList.add(childNode);
            }
        }

        return nodeList;
    }

    /**
     * Checks if a target node has a child with the same name as the provided source node.
     *
     * @param targetNode target node to get children from.
     * @param sourceNode source node to compare name with target node's children.
     * @return whether target node has a child with the same name as source node or not.
     * @throws RepositoryException in case of exception accessing the JCR repository.
     */
    public static boolean hasDirectChildWithSameName(Node targetNode, Node sourceNode) throws RepositoryException {

        Collection<Node> directNodes = NodeUtil.getDirectNodes(targetNode);

        return directNodes.stream().anyMatch(directChild -> NodeUtil.hasSameName(directChild, sourceNode));
    }

    private static boolean hasSameName(Node node, Node toCompareNode) {
        try {
            return node.getName().equals(toCompareNode.getName());

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if a target node is parent of a source node.
     *
     * @param targetNode target node to check direct children from.
     * @param sourceNode source node to check parent from.
     * @return whether the target node is the parent of the source node.
     * @throws RepositoryException in case of exception accessing the JCR repository.
     */
    public static boolean isParent(Node targetNode, Node sourceNode) throws RepositoryException {

        Collection<Node> directChildNodes = NodeUtil.getDirectNodes(targetNode);

        return directChildNodes.stream().anyMatch(child -> NodeUtil.hasSameName(child, sourceNode));
    }

    /**
     * Exceptionless way of retrieving node name.
     *
     * @param node node to get name from
     * @return node name string
     */
    public static String getName(Node node) {
        try {
            return node.getName();
        } catch (Exception e) {
            return StringUtils.EMPTY;
        }
    }

    /**
     * Exceptionless way of retrieving node path.
     *
     * @param node node to get path from
     * @return node path string
     */
    public static String getPath(Item node) {
        try {
            return node.getPath();
        } catch (Exception e) {
            return PATH_SEPARATOR;
        }
    }

    /**
     * Checks if node is deleted.
     *
     * @param node node used for property check
     * @return whether node is deleted or not
     */
    public static boolean isDeleted(Node node) {
        try {
            return node.hasProperty(NodeTypes.Deleted.DELETED);
        } catch (Exception e) {
            //defaulting to deleted node
            return true;
        }
    }

    /**
     * Removes a node from the current session.
     *
     * @param node node to remove.
     */
    public static void remove(Node node) {
        try {
            node.remove();
        } catch (RepositoryException e) {/*Silent Exception*/}
    }

    /**
     * Removes a child node from given node.
     *
     * @param parent        parent node.
     * @param childNodeName node name to remove from parent.
     */
    public static void removeChildNode(Node parent, String childNodeName) throws RepositoryException {
        if (parent.hasNode(childNodeName)) {
            parent.getNode(childNodeName).remove();
        }
    }

    /**
     * Exceptionless way of retrieving primary node type.
     *
     * @param node node to get primary node type from.
     * @return primary node type string.
     */
    public static String getPrimaryNodeType(Node node) {
        try {
            return node.getPrimaryNodeType().getName();
        } catch (RepositoryException e) {
            log.warn("Cannot find NodeType name for node {} because of {}", info.magnolia.jcr.util.NodeUtil.getNodePathIfPossible(node), e);
        }
        return StringUtils.EMPTY;
    }

    /**
     * Exceptionless way of retrieving primary node type.
     *
     * @param node node to get primary node type from.
     * @return primary node type string.
     */
    public static boolean isNodeType(Node node, String nodeType) {
        try {
            return node.getPrimaryNodeType().isNodeType(nodeType);
        } catch (RepositoryException e) {
            return false;
        }
    }

    public static Optional<String> tryGetNodeWorkspace(Node node) {
        try {
            return Optional.of(node.getSession().getWorkspace().getName());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Exceptionless way of checking whether a node is activated or not.
     *
     * @param node node to check activation status.
     * @return if node has been activated or not.
     */
    public static boolean isActivated(Node node) {
        try {
            return NodeTypes.Activatable.isActivated(node);
        } catch (RepositoryException e) {
            log.warn("Error evaluating availability for node [{}], returning false: {}",
                    getPathIfPossible(node), e.getMessage());
            return false;
        }
    }

    public static String getParentNodeType(Item node){
        return NodeUtil.tryGetParentNode(node)
                .map(NodeUtil::getPrimaryNodeType)
                .orElse("");
    }

    public static Optional<Node> tryGetParentNode(Item node) {
        try {
            return Optional.of(node.getParent());
        } catch (RepositoryException e) {
            return Optional.empty();
        }
    }

    public static Optional<Node> tryGetParentNodeWithType(Item node, String nodeType) {
        Optional<Node> parent = tryGetParentNode(node);
        while (parent.isPresent() && !getPrimaryNodeType(parent.get()).equals(nodeType)) {
            parent = tryGetParentNode(parent.get());
        }
        return parent;
    }

    public static Optional<Node> tryGetParentNodeWithTypes(Item node, List<String> nodeTypes) {
        Optional<Node> parent = tryGetParentNode(node);
        while (parent.isPresent() && !nodeTypes.contains(getPrimaryNodeType(parent.get()))) {
            parent = tryGetParentNode(parent.get());
        }
        return parent;
    }

    public static String getParentNodeId(Item node) {
        return NodeUtil.tryGetParentNode(node)
                .map(info.magnolia.jcr.util.NodeUtil::getNodeIdentifierIfPossible)
                .orElse("");
    }


    /**
     * Return, from JCR, list of all nodes and subnodes with "dxp:" prefix for given path
     *
     * @param path JCR path to retrieve all nodes
     * @return list of nodes
     */
    public static List<Node> collectAllDxpNodes(final String path, final String workspace) {
        List<Node> nodes = new ArrayList<>();
        try {
            Session session = MgnlContext.getJCRSession(workspace);
            Node node = session.getNode(path);
            nodes.add(node);
            List<Node> children = IterableUtils.toList(info.magnolia.jcr.util.NodeUtil.collectAllChildren(node, DXP_FILTER));
            nodes.addAll(children);
        } catch (RepositoryException e) {
            log.warn("Cannot collect all children for node {}", path);
        }
        return nodes;
    }

    /**
     * Return, from JCR, node with "dxp:" prefix for given path
     *
     * @param path JCR path to retrieve node
     * @return Optional of node
     */
    public static Optional<Node> collectDxpNode(final String path, final String workspace) {
        try {
            Session session = MgnlContext.getJCRSession(workspace);
            return Optional.of(session.getNode(path));
        } catch (RepositoryException e) {
            log.warn("Cannot collect node for {}", path);
            return Optional.empty();
        }
    }

    /**
     * Return, from JCR, node with "dxp:" prefix for given uuid
     *
     * @param uuid JCR uuid to retrieve node
     * @return Optional of node
     */
    public static Optional<Node> collectDxpNodeByIdentifier(final String uuid, final String workspace) {
        try {
            Session session = MgnlContext.getJCRSession(workspace);
            return Optional.of(session.getNodeByIdentifier(uuid));
        } catch (RepositoryException e) {
            log.warn("Cannot collect node for {}", uuid);
            return Optional.empty();
        }
    }

    public static Node getOrAddNode(Node node, String relPath, String primaryNodeTypeName) throws RepositoryException {
        return node.hasNode(relPath) ? node.getNode(relPath) : node.addNode(relPath, primaryNodeTypeName);
    }

    public static void setOrUpdateProperty(Node node, String name, String value) throws RepositoryException {
        if (node.hasProperty(name)) {
            node.getProperty(name).setValue(value);
        } else {
            node.setProperty(name, value);
        }
    }

    public static void setOrUpdateProperty(Node node, String name, Boolean value) throws RepositoryException {
        if (node.hasProperty(name)) {
            node.getProperty(name).setValue(value);
        } else {
            node.setProperty(name, value);
        }
    }

    private static final AbstractPredicate<Node> DXP_FILTER =
            new AbstractPredicate<Node>() {
                @Override
                public boolean evaluateTyped(Node node) {
                    try {
                        String nodeTypeName = node.getPrimaryNodeType().getName();
                        return nodeTypeName.startsWith("dxp:");
                    } catch (RepositoryException e) {
                        log.error("Unable to read nodeType for node {}", info.magnolia.jcr.util.NodeUtil.getNodePathIfPossible(node));
                    }
                    return false;
                }
            };

    /**
     * Extract last modified date form node
     *
     * @param node
     * @return
     */
    public static Calendar getLastModifiedDate(Node node) {
        return info.magnolia.jcr.util.PropertyUtil.getDate(node, "mgnl:lastModified") != null ?
                info.magnolia.jcr.util.PropertyUtil.getDate(node, "mgnl:lastModified") :
                PropertyUtil.getDate(node, "jcr:created");
    }

    /**
     * Create new node under the parent
     *
     * @param parent      parent of new node.
     * @param newNodeName name of new node.
     * @param type        JCR type of new node.
     * @return returns new node if we can create it.
     */
    public static Node addNode(Node parent, String newNodeName, String type) {
        try {
            return parent.addNode(newNodeName, type);
        } catch (RepositoryException e) {
            log.error("Failed to add {} to root node", newNodeName);
            return null;
        }
    }

    /**
     * Collects nodes from node iterator into list. If the iterator is null the result is empty.
     *
     * @param nodeIterator input iterator
     * @return list of properties
     */
    public static List<Node> asList(final NodeIterator nodeIterator) {
        if (nodeIterator == null) {
            return Collections.emptyList();
        }

        final List<Node> nodes = new ArrayList<>();
        while (nodeIterator.hasNext()) {
            nodes.add(nodeIterator.nextNode());
        }
        return nodes;
    }

    /**
     * Collects nodes path from node iterator into list. If the iterator is null the result is empty.
     *
     * @param nodeIterator input iterator
     * @return list of paths
     */
    public static List<String> asPathsList(final NodeIterator nodeIterator) throws RepositoryException {
        if (nodeIterator == null) {
            return Collections.emptyList();
        }

        final List<String> nodes = new ArrayList<>();
        while (nodeIterator.hasNext()) {
            nodes.add(nodeIterator.nextNode().getPath());
        }
        return nodes;
    }

    /**
     * Checks if a node has a unique property value in all direct children of the parent node.
     * @param node node to check
     * @param propertyName property name to check
     * @param propertyValue property value to check
     * @param nodeType  node type to check
     * @param isNodeCreateBeforeSave  flag to check if node is created before save
     * @return true if property is unique in parent, false otherwise
     */
    public static boolean isNodePropertyUniqueInParent(Node node, String propertyName, String propertyValue, String nodeType, boolean isNodeCreateBeforeSave) {
        try {
            if (isNodeCreateBeforeSave) {
                Node parent = node.getParent();
                return !getAllPropertiesValuesWithoutCurrentNode(node, parent, propertyName, nodeType).contains(propertyValue);
            }
            return !getAllPropertiesValues(node, propertyName, nodeType).contains(propertyValue);
        } catch (RepositoryException e) {
            log.error("Error while checking if property is unique in parent", e);
            return false;
        }
    }

    /**
     * Get all properties values from children nodes of a parent node, except the current node.
     * @param nodeToCompare node to compare
     * @param parentNode parent node
     * @param propertyName property name
     * @param nodeType node type
     * @return list of property values
     * @throws RepositoryException
     */
    public static Collection<String> getAllPropertiesValuesWithoutCurrentNode(Node nodeToCompare, Node parentNode, String propertyName, String nodeType) throws RepositoryException {
        Collection<Node> children = NodeUtil.getDirectNodes(parentNode, nodeType);
        return children.stream()
                .filter(node -> !isSameNode(node, nodeToCompare))
                .map(node -> getPropertyValue(node, propertyName))
                .collect(Collectors.toList());
    }

    /**
     * Get all properties values from children nodes of a parent node.
     * @param parentNode parent node
     * @param propertyName property name
     * @param nodeType node type
     * @return list of property values
     * @throws RepositoryException
     */
    public static Collection<String> getAllPropertiesValues(Node parentNode, String propertyName, String nodeType) throws RepositoryException {
        Collection<Node> children = NodeUtil.getDirectNodes(parentNode, nodeType);
        return children.stream()
                .map(node -> getPropertyValue(node, propertyName))
                .collect(Collectors.toList());
    }

    /**
     * Get property value from node.
     * @param node node
     * @param propertyName property name
     * @return field value
     */
    public static String getPropertyValue(Node node, String propertyName)  {
        try {
            return isPropertyNodeName(propertyName) ? node.getName() : getNodeProperty(node, propertyName);
        } catch (RepositoryException e) {
            return "";
        }
    }

    /**
     * Check if property is node name.
     * @param propertyName property name
     * @return true if property is node name, false otherwise
     */
    public static boolean isPropertyNodeName(String propertyName){
        return NODE_NAME_FIELD_TYPE.equals(propertyName);
    }

    /**
     * Get node property.
     * @param node node
     * @param propertyName property name
     * @return property value
     * @throws RepositoryException
     */
    public static String getNodeProperty(Node node, String propertyName) throws RepositoryException {
        return node.hasProperty(propertyName) ? node.getProperty(propertyName).getString() : null;
    }

    /**
     * Filter function to check distinct based on arbitrary key
     * <a href="https://stackoverflow.com/a/27872852">from stackoverflow</a>
     * @param keyExtractor function that returns key to check uniqueness
     * @return predicate that returns true if no duplicates exist
     * @param <T> type of target object
     */
    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }


    /**
     * Checks whether the given node or its parent is locked.
     *
     * @param node the JCR node to check
     * @return true if the node or its parent is locked, false otherwise.
     */
    public static boolean isNodeOrParentLocked(Node node) {
        try {
            if (node.isLocked()) {
                return true;
            }
            Node parent = node.getParent();
            return parent != null && parent.isLocked();
        } catch (RepositoryException e) {
            return false;
        }
    }

    public static List<Node> collectAllNodesWithType(final String path, final String workspace, final String type) {
        try {
            Session session = MgnlContext.getJCRSession(workspace);
            Node node = session.getNode(path);
            return collectAllNodesWithType(node, type);
        } catch (RepositoryException e) {
            log.warn("Cannot collect all children of type {} for path {}", type, path);
        }

        return Collections.emptyList();
    }

    public static List<Node> collectAllNodesWithType(final Node node, final String type) {
        final List<Node> nodes = new ArrayList<>();
        try {
            if (node.isNodeType(type)) {
                nodes.add(node);
            }

            NodeIterator nodeIterator = node.getNodes();
            while (nodeIterator.hasNext()) {
                Node childNode = nodeIterator.nextNode();
                nodes.addAll(collectAllNodesWithType(childNode, type));
            }
        } catch (RepositoryException e) {
            log.warn("Cannot collect all children of type {}", type);
        }

        return nodes;
    }
}
