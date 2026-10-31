/**
 * This file Copyright (c) 2014-2018 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 * <p>
 * <p>
 * This program and the accompanying materials are made
 * available under the terms of the Magnolia Network Agreement
 * which accompanies this distribution, and is available at
 * http://www.magnolia-cms.com/mna.html
 * <p>
 * Any modifications to this file must keep this entire header
 * intact.
 */
package com.tbscg.doosan.magnolia.regions.workflow;

import com.tbscg.doosan.magnolia.regions.model.Region;
import com.tbscg.doosan.magnolia.regions.service.RegionsService;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.workflow.api.WorkflowConstants;
import info.magnolia.module.workflow.jbpm.humantask.HumanTask;
import info.magnolia.module.workflow.jbpm.humantask.definition.PublicationTaskDefinition;
import info.magnolia.module.workflow.jbpm.humantask.parameter.AbstractHumanTaskParameterResolver;
import info.magnolia.repository.RepositoryConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.WorkItem;

import javax.inject.Inject;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Creates a publication related task with needed parameters.
 * Possible assignees are defined by configuration in {@link info.magnolia.module.workflow.jbpm.humantask.definition.PublicationTaskDefinition} and content is read from the
 * {@link WorkflowConstants#VARIABLE_DATA} passed by {@link WorkItem}.
 */
@Slf4j
public class PublicationTaskParameterResolver extends AbstractHumanTaskParameterResolver<PublicationTaskDefinition> {

    protected final DateFormat DATE_PARSER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    private final RegionsService regionService;

    @Inject
    public PublicationTaskParameterResolver(PublicationTaskDefinition definition, RegionsService regionService) {
        super(definition);
        this.regionService = regionService;
    }

    @Override
    public HumanTask createTask(WorkItem workItem, KieSession kieSession) {
        HumanTask task = new HumanTask();
        setTaskParameters(task, workItem);
        setProcessData(task, workItem);
        return task;
    }

    @Override
    public void setTaskParameters(HumanTask task, WorkItem workItem) {
        String taskName = (String) workItem.getParameter(TASK_NAME);
        task.setName(taskName);
        task.setActorIds(getDefinition().getActors());

        Map<String, Object> content = (Map<String, Object>) workItem.getParameter(WorkflowConstants.VARIABLE_DATA);
        task.setGroupIds(publishersGroups((String) content.get(Context.ATTRIBUTE_PATH)));

        if (content != null) {
            task.setComment((String) content.get(Context.ATTRIBUTE_COMMENT));
            task.setRequestor((String) content.get(Context.ATTRIBUTE_REQUESTOR));

            setTaskContent(task, content);
        }
    }

    private List<String> publishersGroups(String path) {
        List<String> publishers = new ArrayList<>(getDefinition().getGroups());
        try {
            Region region = this.regionService.getRegionFromPath(path);

            List<String> regionPublishers =  region.getPublishers().stream()
                    .map(PublicationTaskParameterResolver::mapPublisherIdToPublisherName)
                    .filter(StringUtils::isNotEmpty)
                    .collect(Collectors.toList());

            publishers.addAll(regionPublishers);
        } catch (Exception e) {
            log.warn("Could not extract region from path: {}", path);
        }
        return publishers;
    }

    private static String mapPublisherIdToPublisherName(String publisherId) {
        try {
            return MgnlContext.getJCRSession(RepositoryConstants.USER_GROUPS).getNodeByIdentifier(publisherId).getName();
        } catch (Exception ex) {
            log.error("Could not map user group id to name", ex);
            return StringUtils.EMPTY;
        }
    }

    /*
     * Clean the content of complex and unused types. The task content map can only hold simple types.
     */
    private void setTaskContent(HumanTask task, final Map<String, Object> content) {
        Map<String, Object> taskContent = new HashMap<String, Object>() {{
            put(Context.ATTRIBUTE_REPOSITORY, content.get(Context.ATTRIBUTE_REPOSITORY));
            put(Context.ATTRIBUTE_PATH, content.get(Context.ATTRIBUTE_PATH));
            put(Context.ATTRIBUTE_UUID, content.get(Context.ATTRIBUTE_UUID));
            put(Context.ATTRIBUTE_RECURSIVE, content.get(Context.ATTRIBUTE_RECURSIVE));
            put(Context.ATTRIBUTE_COMMENT, content.get(Context.ATTRIBUTE_COMMENT));
            put(Context.ATTRIBUTE_REQUESTOR, content.get(Context.ATTRIBUTE_REQUESTOR));
            put(Context.ATTRIBUTE_VERSION, content.get(Context.ATTRIBUTE_VERSION));
            put(WorkflowConstants.PUBLICATION_DATE, getDate(content.get(WorkflowConstants.PUBLICATION_DATE)));
            put(WorkflowConstants.IS_DELETION, content.get(WorkflowConstants.IS_DELETION));
        }};

        task.setContent(taskContent);

    }

    private Date getDate(Object dateString) {
        Date date = null;
        if (dateString != null) {
            String string = String.valueOf(dateString);
            try {
                date = DATE_PARSER.parse(string);
            } catch (ParseException e) {
                log.error("Could not parse publication date string.", e);
            }
        }
        return date;
    }

}
