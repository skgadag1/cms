package com.tbscg.doosan.magnolia.dxp.workflow.action.ui.validators;

import com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil;
import info.magnolia.cms.security.User;
import info.magnolia.context.MgnlContext;
import info.magnolia.ui.datasource.jcr.JcrNodeWrapper;

import javax.jcr.Node;
import java.util.Collection;
import java.util.stream.Stream;

public class RegionalActionNodeValidator<T extends JcrNodeWrapper> implements NodeValidator<T> {
    private final Collection<String> allowedRoles;

    public RegionalActionNodeValidator(Collection<String> allowedRoles) {
        this.allowedRoles = allowedRoles;
    }

    public boolean validate(Stream<T> sources, Stream<T> destinations) {
        return checkIsInExceptionRoles() || checkIsInSingleRegion(sources, destinations);
    }

    private boolean checkIsInSingleRegion(Stream<T> sources, Stream<T> destinations) {
         return Stream.concat(sources, destinations).map(this::getRegionFromNode).distinct().count() == 1;
    }

    private String getRegionFromNode(Node node) {
        return NodeUtil.getPath(node).split("/")[2];
    }

    private boolean checkIsInExceptionRoles() {
        if (allowedRoles.isEmpty()) {
            return true;
        }

        User user = MgnlContext.getUser();
        Stream<String> roles = user.getAllRoles().stream();
        return roles.anyMatch(allowedRoles::contains);
    }
}
