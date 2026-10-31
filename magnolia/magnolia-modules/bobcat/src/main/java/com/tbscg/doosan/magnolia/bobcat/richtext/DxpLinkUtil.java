package com.tbscg.doosan.magnolia.bobcat.richtext;

import com.tbscg.doosan.magnolia.bobcat.BobcatModuleConfiguration;
import com.tbscg.doosan.magnolia.bobcat.custompath.CustomPathUtil;
import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.SessionUtil;
import info.magnolia.link.Link;
import info.magnolia.link.LinkException;
import info.magnolia.link.LinkTransformer;
import info.magnolia.link.LinkUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.regex.Matcher;

@Slf4j
public class DxpLinkUtil extends LinkUtil {

    private final BobcatModuleConfiguration moduleConfiguration;
    private final ServerConfiguration serverConfiguration;

    @Inject
    public DxpLinkUtil(final BobcatModuleConfiguration moduleConfiguration, final ServerConfiguration serverConfiguration) {
        this.moduleConfiguration = moduleConfiguration;
        this.serverConfiguration = serverConfiguration;
    }

    public String convertCustomLinksFromUUIDPattern(String str, LinkTransformer transformer, String lang) throws LinkException {
        if (str == null) {
            return null;
        }
        StringBuffer res = new StringBuffer();
        // first replace all image links in UUID pattern format with fingerprinted link based on provided link transformer
        Matcher imageMatcher = IMAGE_PATTERN.matcher(str);
        while (imageMatcher.find()) {
            Matcher matcher = UUID_PATTERN.matcher(imageMatcher.group());
            String imageMatcherGroup = imageMatcher.group();
            while (matcher.find()) {
                Link link = createLinkInstance(matcher.group(1), matcher.group(2), matcher.group(5), matcher.group(7), matcher.group(8), matcher.group(10), matcher.group(12));
                String replacement = transformer.transform(link);
                try {
                    replacement = addFingerprintToLink(replacement, NodeTypes.LastModified.getLastModified(MgnlContext.getJCRSession(matcher.group(2)).getNodeByIdentifier(matcher.group(1))));
                } catch (RepositoryException e) {
                    try {
                        final Node node = MgnlContext.getJCRSession(matcher.group(2)).getNode(matcher.group(5) != null ? matcher.group(5) : "");
                        replacement = addFingerprintToLink(replacement, NodeTypes.LastModified.getLastModified(node));
                    } catch (RepositoryException e2) {
                        log.warn("Can't find node with uuid {} or handle {} in repository {}. Figerprint to link was't added.", matcher.group(1), matcher.group(5), matcher.group(2), e2);
                    }
                }
                // Replace "\" with "\\" and "$" with "\$" since Matcher.appendReplacement treats these characters specially
                replacement = StringUtils.replace(replacement, "\\", "\\\\");
                replacement = StringUtils.replace(replacement, "$", "\\$");
                if (replacement != null) {
                    imageMatcherGroup = imageMatcherGroup.replace(matcher.group(), replacement);
                }
            }
            imageMatcher.appendReplacement(res, imageMatcherGroup);
        }
        imageMatcher.appendTail(res);

        // second replace all other links in UUID pattern
        Matcher matcher = UUID_PATTERN.matcher(res);
        res = new StringBuffer();
        while (matcher.find()) {
            Link link = createLinkInstance(matcher.group(1), matcher.group(2), matcher.group(5), matcher.group(7), matcher.group(8), matcher.group(10), matcher.group(12));
            String replacement = "";
            if (isCustomPathEnabled(link) && !serverConfiguration.isAdmin()) {
                Node node = SessionUtil.getNode(link.getWorkspace(), link.getPath());
                replacement = CustomPathUtil.toCustomLink(moduleConfiguration.isCustomPathResolutionEnabled(), node, lang);
            } else {
                replacement = transformer.transform(link);
            }
            // Replace "\" with "\\" and "$" with "\$" since Matcher.appendReplacement treats these characters specially
            replacement = StringUtils.replace(replacement, "\\", "\\\\");
            replacement = StringUtils.replace(replacement, "$", "\\$");
            matcher.appendReplacement(res, replacement);
        }
        matcher.appendTail(res);
        return res.toString();
    }

    private boolean isCustomPathEnabled(Link link) {
        return "website".equals(link.getWorkspace()) && link.getPath().startsWith("/bobcat");
    }

}
