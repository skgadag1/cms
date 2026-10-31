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

import com.tbscg.doosan.magnolia.bobcat.BobcatModuleConfiguration;
import com.tbscg.doosan.magnolia.bobcat.custompath.CustomPathUtil;
import com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.context.WebContext;
import info.magnolia.link.LinkUtil;
import info.magnolia.rest.delivery.jcr.decorator.AdditionNodeWrapper;
import info.magnolia.rest.delivery.jcr.decorator.ReadOnlyProperty;
import info.magnolia.rest.reference.ReferenceResolver;
import info.magnolia.rest.reference.jcr.JcrReferenceResolverDefinition;
import lombok.extern.slf4j.Slf4j;
import org.apache.jackrabbit.value.StringValue;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.Collections;
import java.util.Optional;

/**
 * <p>Custom implementation of {@link ReferenceResolver} on JCR.</p>
 * <p>Links generated with this resolver will take customNodeName into account, creating a friendlyURL.</p>
 */
@Slf4j
public class CustomUrlJcrReferenceResolver implements ReferenceResolver<String, Object> {

    private static final String websiteWorkspace = "website";
    private static final String bobcatSitePrefix = "/bobcat";
    private static final String langParam = "lang";
    private static final String fallbackLang = "en";


    private final Provider<Context> contextProvider;
    private final JcrReferenceResolverDefinition definition;
    private final BobcatModuleConfiguration moduleConfiguration;

    @Inject
    public CustomUrlJcrReferenceResolver(
            final Provider<Context> contextProvider,
            final JcrReferenceResolverDefinition definition,
            final BobcatModuleConfiguration moduleConfiguration) {
        this.contextProvider = contextProvider;
        this.definition = definition;
        this.moduleConfiguration = moduleConfiguration;
    }

    @Override
    public Optional<Object> resolve(final String item) {
        log.debug("resolve({})", item);

        if (definition.isGenerateLink() || definition.isExpand()) {
            // Try to use WebContext in order to be able to generate link.
            final Context currentContext = MgnlContext.getInstance();
            try {
                log.debug("Resolving item {} in workspace {}", item, definition.getTargetWorkspace());
                Node node = contextProvider.get().getJCRSession(definition.getTargetWorkspace()).getNodeByIdentifier(item);
                log.debug("Resolved node {} in workspace {}", node, definition.getTargetWorkspace());
                if (definition.isGenerateLink()) {
                    log.debug("Generating link for node {} in workspace {}", node, definition.getTargetWorkspace());
                    if (currentContext instanceof SystemContext) {
                        final Context originalContext = ((SystemContext) currentContext).getOriginalContext();
                        log.info("Switching to original context {}", originalContext);
                        if (originalContext instanceof WebContext) {
                            MgnlContext.setInstance(originalContext);
                        }
                    }

                    // BEGIN custom code
                    Optional<String> workspace = NodeUtil.tryGetNodeWorkspace(node);
                    log.debug("Workspace: {}", workspace);
                    String path = NodeUtil.getPath(node);
                    log.debug("Path: {}", path);
                    String link;
                    if (isCustomPathEnabled(workspace, path)) {
                        Context context = this.contextProvider.get();
                        String langFromContext = context.getAttribute(langParam);
                        String lang = langFromContext != null ? langFromContext : fallbackLang;
                        link = CustomPathUtil.toCustomLink(moduleConfiguration.isCustomPathResolutionEnabled(), node, lang);
                        log.debug("Translated resolved customPath: '{}'.", link);
                    } else {
                        link = LinkUtil.createExternalLink(node);
                    }
                    // END custom code
                    log.debug("Generated link {} for node {} in workspace {}", link, node, definition.getTargetWorkspace());
                    node = new AdditionNodeWrapper(node, Collections.singletonList(new ReadOnlyProperty("@link", new StringValue(link), node)));
                    if (!definition.isExpand()) {
                        log.debug("Returning link {} for node {} in workspace {}", link, node, definition.getTargetWorkspace());
                        return Optional.ofNullable(link);
                    }
                }
                return Optional.of(node);
            } catch (final ItemNotFoundException e) {
                log.warn("Could not resolve JCR reference for identifier {} within workspace {}; reference is either orphaned, or the session did not have read access.", item, definition.getTargetWorkspace());
                log.warn("Error on resolving", e);
            } catch (final RepositoryException e) {
                log.warn("Error on resolving item {}", item);
            } finally {
                MgnlContext.setInstance(currentContext);
            }
        }
        return Optional.empty();
    }

    private boolean isCustomPathEnabled(Optional<String> workspace, String path) {
        return workspace.isPresent() && workspace.get().equals(websiteWorkspace) && path.startsWith(bobcatSitePrefix);
    }
}
