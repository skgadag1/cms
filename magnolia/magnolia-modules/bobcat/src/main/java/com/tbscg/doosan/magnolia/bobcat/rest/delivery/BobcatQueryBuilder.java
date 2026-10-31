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

import info.magnolia.jcr.RuntimeRepositoryException;
import info.magnolia.rest.delivery.jcr.FilteringCondition;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.util.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Workspace;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

/**
 * Bobcat implementation for {@link info.magnolia.rest.delivery.jcr.QueryBuilder}
 */
public class BobcatQueryBuilder {

    private static final Logger log = LoggerFactory.getLogger(BobcatQueryBuilder.class);

    private static final String SELECTOR_NAME = "t";

    private static final String SELECT_TEMPLATE = "SELECT * FROM [nt:base] AS " + SELECTOR_NAME;

    private static final String WHERE_TEMPLATE_FOR_PATH = " ISDESCENDANTNODE('%s')";

    private static final String WHERE_TEMPLATE_FOR_NAME = " NAME() = '%s'";

    private static final String ORDER_BY = " ORDER BY ";

    private static final String ASCENDING_KEYWORD = " ASC";

    private static final String JCR_NAME_FUNCTION = "LOWER(NAME(" + SELECTOR_NAME + "))";

    private static final String JCR_NAME = "@name";

    private static final String WHERE_TEMPLATE_FOR_SEARCH = "LOWER(LOCALNAME()) LIKE '%1$s%%'";

    private static final String CONTAINS_TEMPLATE_FOR_SEARCH = "CONTAINS(" + SELECTOR_NAME + ".*, '%1$s')";

    private static final String JCR_IS_SAME_NODE_FUNCTION = "ISSAMENODE(" + SELECTOR_NAME + ", '%1$s')";

    private final Pattern simpleTermsRegexPattern = Pattern.compile("[^\\s\"']+|\"[^\"]*\"|'[^']*'");

    private final Workspace workspace;
    private String rootPath;
    private String name;
    private boolean strict;
    private List<String> nodeTypes = new ArrayList<>();
    private String keyword;
    private List<FilteringCondition> filteringConditions = new ArrayList<>();
    private List<String> propertiesToOrder;
    private long offset;
    private long limit;

    private BobcatQueryBuilder(Workspace workspace) {
        this.workspace = workspace;
    }

    public static BobcatQueryBuilder inWorkspace(Workspace workspace) {
        return new BobcatQueryBuilder(workspace);
    }

    public BobcatQueryBuilder rootPath(String rootPath) {
        this.rootPath = rootPath;
        return this;
    }

    public BobcatQueryBuilder name(String name) {
        this.name = name;
        return this;
    }

    public BobcatQueryBuilder strict(boolean strict) {
        this.strict = strict;
        return this;
    }

    public BobcatQueryBuilder nodeTypes(List<String> nodeTypes) {
        this.nodeTypes.addAll(nodeTypes);
        return this;
    }

    public BobcatQueryBuilder keyword(String keyword) {
        this.keyword = keyword;
        return this;
    }

    public BobcatQueryBuilder conditions(Map<String, List<String>> conditions) {
        List<FilteringCondition> conditionList = conditions.entrySet().stream()
                .map(this::toFilteringConditions)
                .flatMap(Function.identity())
                .collect(toList());
        this.filteringConditions.addAll(conditionList);
        return this;
    }

    public BobcatQueryBuilder orderBy(List<String> propertiesToOrder) {
        this.propertiesToOrder = propertiesToOrder;
        return this;
    }

    public BobcatQueryBuilder offset(long offset) {
        this.offset = offset;
        return this;
    }

    public BobcatQueryBuilder limit(long limit) {
        this.limit = limit;
        return this;
    }

    public Query build() {
        StringBuilder statement = new StringBuilder(SELECT_TEMPLATE);
        try {
            // Collect condition clauses.
            List<String> conditionClauses = new ArrayList<>();
            conditionClauses.add(getWhereClauseForNodeTypes());
            conditionClauses.add(getWhereClauseWorkspacePath());
            conditionClauses.add(getWhereClauseForName());
            conditionClauses.add(getWhereClauseForSearch());
            conditionClauses.addAll(getWhereClausesForFiltering());

            // Remove empty clauses.
            List<String> clauses = conditionClauses.stream()
                    .filter(clause -> !clause.isEmpty())
                    .collect(toList());

            // Append condition clauses.
            statement.append(clauses.isEmpty() ? "" : " WHERE ");
            statement.append(clauses.stream()
                    .map(clause -> "(" + clause + ")")
                    .collect(joining(" AND ")));

            if (!CollectionUtils.isEmpty(propertiesToOrder)) {
                statement.append(getOrderByClause());
            }

            // Get query and set offset and limit.
            QueryManager jcrQueryManager = workspace.getQueryManager();
            Query query = jcrQueryManager.createQuery(statement.toString(), Query.JCR_SQL2);

            if (offset > 0) {
                query.setOffset(offset);
            }
            if (limit > 0) {
                query.setLimit(limit);
            }

            log.debug("SQL statement is {}", query.getStatement());
            return query;

        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    private Stream<FilteringCondition> toFilteringConditions(Map.Entry<String, List<String>> entry) {
        String key = entry.getKey();
        return entry.getValue().stream()
                .map(value -> new FilteringCondition(sanitize(key), sanitize(value)));
    }

    private String getOrderByClause() {
        StringBuilder orderByBuilder = new StringBuilder(ORDER_BY);
        for (String propertyToOrder : propertiesToOrder) {
            String[] tokens = propertyToOrder.split(" ");
            String property = tokens[0];
            String direction = "";

            if (tokens.length > 1) {
                direction = tokens[1];
            }

            if (JCR_NAME.equalsIgnoreCase(property)) {
                orderByBuilder.append(String.format("%s %s, ", JCR_NAME_FUNCTION, direction));
            } else {
                orderByBuilder.append(String.format("LOWER(%s.[%s]) %s, ", SELECTOR_NAME, property, direction));
            }
        }

        return StringUtils.removeEnd(orderByBuilder.toString(), ", ");
    }

    private String getWhereClauseForNodeTypes() {
        Set<String> nodeTypes = new HashSet<>(this.nodeTypes);
        if (!strict) {
            for (String nodeType : this.nodeTypes) {
                NodeTypeIterator nodeTypeIterator = getNodeType(nodeType).getSubtypes();
                while (nodeTypeIterator.hasNext()) {
                    NodeType subNodeType = nodeTypeIterator.nextNodeType();
                    nodeTypes.add(subNodeType.getName());
                }
            }
        }

        return nodeTypes.stream()
                .map(this::getNodeType)
                .map(this::getConditionBasedOnNodeType)
                .collect(joining(" OR "));
    }

    private NodeType getNodeType(String nodeTypeStr) {
        try {
            NodeTypeManager nodeTypeManager = workspace.getNodeTypeManager();
            return nodeTypeManager.getNodeType(nodeTypeStr);
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    private String getConditionBasedOnNodeType(NodeType nodeType) {
        if (nodeType.isMixin()) {
            return String.format("[jcr:mixinTypes] = '%s'", nodeType.getName());
        }
        return String.format("[jcr:primaryType] = '%s'", nodeType.getName());
    }

    private String getWhereClauseWorkspacePath() {
        return StringUtils.isNotBlank(rootPath) && !"/".equals(rootPath) ? String.format(WHERE_TEMPLATE_FOR_PATH, rootPath) : "";
    }

    private String getWhereClauseForName() {
        return StringUtils.isNotBlank(name) ? String.format(WHERE_TEMPLATE_FOR_NAME, name) : "";
    }

    private String getWhereClauseForSearch() {
        if (StringUtils.isBlank(keyword)) {
            return "";
        }

        String lowercaseText = keyword.toLowerCase();
        String jcrCharsEscapedText = Text.escapeIllegalJcrChars(lowercaseText);
        String singleQuoteEscapedText = sanitize(jcrCharsEscapedText);
        String escapedFullTextExpression = escapeFullTextExpression(lowercaseText);

        // The given search query string starts with "/" is considered as abs path.
        if (Paths.get(escapedFullTextExpression).isAbsolute()) {
            String rootPath = this.rootPath;

            if (StringUtils.isEmpty(rootPath) || "/".equals(rootPath) || escapedFullTextExpression.startsWith(rootPath)) {
                rootPath = "";
            }

            return String.format(JCR_IS_SAME_NODE_FUNCTION, rootPath + escapedFullTextExpression);
        }
        return String.format(WHERE_TEMPLATE_FOR_SEARCH, singleQuoteEscapedText) + String.format(" OR " + CONTAINS_TEMPLATE_FOR_SEARCH, escapedFullTextExpression);
    }

    /**
     * See http://wiki.apache.org/jackrabbit/EncodingAndEscaping.
     * Copied over from {@link info.magnolia.ui.workbench.search.SearchJcrContainer}.
     */
    private String escapeFullTextExpression(String fulltextExpression) {
        List<String> matchList = findSimpleTerms(fulltextExpression);

        List<String> simpleTerms = new ArrayList<>();
        for (String token : matchList) {
            simpleTerms.add(escapeIllegalFullTextSearchChars(token));
        }
        // Workaround as our regex does not match one single double quote ["].
        if ("\"".equals(fulltextExpression)) {
            simpleTerms.add("\\\"");
        }

        return sanitize(simpleTerms.stream().collect(joining(" ")));
    }

    /**
     * @return a list of simple terms according to JCR 2.0 definition, i.e. SimpleTerm ::= Word | '"' Word {Space Word} '"'
     * (See http://www.day.com/specs/jcr/2.0/6_Query.html#6.7.19%20FullTextSearch)
     * Copied over from {@link info.magnolia.ui.workbench.search.SearchJcrContainer}.
     */
    private List<String> findSimpleTerms(String unescapedFullTextExpression) {
        List<String> matchList = new LinkedList<>();
        Matcher regexMatcher = simpleTermsRegexPattern.matcher(unescapedFullTextExpression);
        while (regexMatcher.find()) {
            matchList.add(regexMatcher.group());
        }
        return matchList;
    }

    /**
     * Within a term, each sensitive char must be escaped by a preceding “\”.<br>
     * - “-” (minus sign), “+” (plus sign) and “\” (backslash) are escaped if they are the single element of the term <br>
     * - "()[]{}" (all brackets) are always escaped<br>
     * - “"” (double quote) is always escape unless it delimits a simple term, i.e <code>"foo -bar"</code><br>
     * <strong>This method has package visibility for testing purposes.</strong>
     * Copied over from {@link info.magnolia.ui.workbench.search.SearchJcrContainer}.
     */
    private String escapeIllegalFullTextSearchChars(String simpleTerm) {
        StringBuilder sb = new StringBuilder(simpleTerm.length());

        for (int i = 0; i < simpleTerm.length(); i++) {
            char ch = simpleTerm.charAt(i);
            if (("\\+-".contains(String.valueOf(ch)) && simpleTerm.length() == 1)
                    || ("()[]{}".contains(String.valueOf(ch)))
                    || ("\"".contains(String.valueOf(ch)) && (i != 0 && i != simpleTerm.length() - 1))) {
                sb.append('\\');
            }
            sb.append(ch);
        }
        return sb.toString();
    }

    private List<String> getWhereClausesForFiltering() {
        return filteringConditions.stream()
                .map(FilteringCondition::asSqlString)
                .collect(toList());
    }

    private static String sanitize(String text) {
        String trimmedText = text.trim();
        return trimmedText.replaceAll("'", "''");
    }
}
