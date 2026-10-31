package com.tbscg.doosan.magnolia.localizations.converter;

import com.google.common.base.Preconditions;
import com.tbscg.doosan.magnolia.concepts.service.ConceptsService;
import com.tbscg.doosan.magnolia.dxp.core.i18n.LocalesWrapper;
import com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil;
import com.tbscg.doosan.magnolia.localizations.model.LanguageGroupsPair;
import com.tbscg.doosan.magnolia.localizations.model.Localization;
import com.tbscg.doosan.magnolia.localizations.model.LocalizationsGroupPair;
import com.tbscg.doosan.magnolia.localizations.model.LocalizationsPackage;
import com.tbscg.doosan.magnolia.regions.service.LocaleService;
import org.apache.commons.lang3.StringUtils;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.*;
import java.util.stream.Collectors;

import static com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil.getDirectNodes;

/**
 * Abstract node to localizations package converter strategy.
 *
 * @author apuigescobar
 */
public abstract class AbstractNodeToLocalizationsPackageConverterStrategy {

    private final LocaleService localeService;
    private final ConceptsService conceptsService;

    public AbstractNodeToLocalizationsPackageConverterStrategy(LocaleService localeService, ConceptsService conceptsService) {
        this.localeService = localeService;
        this.conceptsService = conceptsService;
    }

    protected abstract LocalizationsPackage handleConversion(Node fromNode) throws RepositoryException;

    protected Set<LanguageGroupsPair> getLanguageGroupsPairs(Node regionNode) throws RepositoryException {
        final LocalesWrapper localesWrapper = localeService.resolveLocales(regionNode);
        final Collection<Locale> locales = localesWrapper.getLocales();
        Preconditions.checkNotNull(locales, "Locales can't be null");
        final Collection<String> languagesSet = locales.stream().map(Locale::getLanguage).collect(Collectors.toSet());
        final Locale defaultLocale = localesWrapper.getDefaultLocale();
        //this will never be null anyway
        Preconditions.checkNotNull(defaultLocale, String.format("Default locale for region [%s] can't be null", regionNode.getName()));
        final String defaultLanguage = defaultLocale.getLanguage();
        languagesSet.add(defaultLanguage);

        final Collection<String> groupNames = this.getGroupNames(regionNode);
        Preconditions.checkArgument(!groupNames.isEmpty(), "There must be at least one group name");
        final Set<LanguageGroupsPair> localizationsGroupsPairSet = new HashSet<>();

        for (final String language : languagesSet) {
            localizationsGroupsPairSet.add(new LanguageGroupsPair(language, this.getLocalizationsGroups(regionNode,
                    groupNames, language)));
        }

        Preconditions.checkArgument(!localizationsGroupsPairSet.isEmpty(), "Localizations groups pair set can't be empty");
        return localizationsGroupsPairSet;
    }

    protected Set<LocalizationsGroupPair> getLocalizationsGroups(Node siteNode, Collection<String> groupNames, String language)
            throws RepositoryException {
        final Set<LocalizationsGroupPair> localizationsGroupPairs = new HashSet<>();

        for (final String groupName : groupNames) {
            localizationsGroupPairs.add(new LocalizationsGroupPair(groupName, this.getLocalizations(siteNode, groupName, language)));
        }

        Preconditions.checkArgument(!localizationsGroupPairs.isEmpty(), "Localizations groups set can't be empty");
        return localizationsGroupPairs;
    }

    protected Set<Localization> getLocalizations(Node siteNode, String groupName, String language)
            throws RepositoryException {
        final Node groupNode = siteNode.getNode(groupName);
        final Collection<Node> conceptNodes = getDirectNodes(groupNode).stream().filter(NodeUtil::isActivated).toList();
        if (conceptNodes.isEmpty()) {
            return Collections.emptySet();
        }
        final Set<Localization> localizationsSet = new HashSet<>();

        for (final Node conceptNode : conceptNodes) {
            localizationsSet.add(this.getLocalization(conceptNode, language));
        }

        return localizationsSet;
    }

    private Localization getLocalization(Node conceptNode, String language) throws RepositoryException {
        return new Localization(conceptNode.getName(), conceptsService.getLocalizedConcept(conceptNode, language));
    }

    protected abstract Collection<String> getGroupNames(Node fromNode) throws RepositoryException;

    protected Collection<String> getDirectGroupNames(Node fromNode) throws RepositoryException {
        return NodeUtil.getDirectNodes(fromNode).stream().filter(NodeUtil::isActivated).map(NodeUtil::getName).collect(Collectors.toSet());
    }

    protected String getCheckedAppName(Node fromNode) throws RepositoryException {
        final String appName = this.getAppName(fromNode);
        Preconditions.checkArgument(StringUtils.isNotBlank(appName), "Name can't be blank");
        return appName;
    }

    protected abstract String getAppName(Node fromNode) throws RepositoryException;
}
