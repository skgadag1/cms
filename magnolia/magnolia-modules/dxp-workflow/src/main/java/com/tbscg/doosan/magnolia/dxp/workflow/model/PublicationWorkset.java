package com.tbscg.doosan.magnolia.dxp.workflow.model;

import com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil;
import lombok.extern.slf4j.Slf4j;

import javax.jcr.Node;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@Slf4j
public record PublicationWorkset(String workspace, String rootPath, boolean recursive, List<PublicationItem> items) {

    public PublicationWorkset {
        items = items == null ? List.of() : List.copyOf(items);
    }

    public int size() {
        return items.size();
    }

    public List<Node> resolveNodes() {
        return resolveNodes(item -> true);
    }

    public List<Node> resolveNodes(final Predicate<PublicationItem> filter) {
        return items.stream()
                .filter(filter)
                .map(this::resolveNode)
                .flatMap(Optional::stream)
                .toList();
    }

    public List<String> getIds() {
        return items.stream()
                .map(PublicationItem::uuid)
                .toList();
    }

    private Optional<Node> resolveNode(final PublicationItem item) {
        final Optional<Node> node = NodeUtil.collectDxpNode(item.path(), item.workspace());
        if (node.isEmpty()) {
            log.warn("Publication workset item no longer exists. workspace={}, path={}", item.workspace(), item.path());
        }
        return node;
    }
}
