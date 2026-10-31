package com.tbscg.doosan.magnolia.regions.field.datasource;

import info.magnolia.cms.security.Group;
import info.magnolia.cms.security.GroupManager;
import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.objectfactory.Components;
import info.magnolia.ui.datasource.optionlist.Option;
import info.magnolia.ui.datasource.optionlist.OptionListDefinition;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Datasource for publisher selection that dynamically populates options with available Magnolia user groups.
 * User groups are used as publishers in the regions module.
 */
@Slf4j
public class PublisherOptionListDefinition extends OptionListDefinition {

    public PublisherOptionListDefinition() {
        super();
    }

    @Override
    public List<Option> getOptions() {
        List<Option> options = new ArrayList<>();
        try {
            SecuritySupport securitySupport = Components.getComponent(SecuritySupport.class);
            GroupManager groupManager = securitySupport.getGroupManager();
            Collection<Group> allGroups = groupManager.getAllGroups();
            
            for (Group group : allGroups) {
                Option option = new Option();
                option.setValue(group.getId());
                option.setLabel(group.getName() != null && !group.getName().isEmpty() 
                    ? group.getName() 
                    : group.getId());
                option.setName(group.getName());
                options.add(option);
            }
            
            log.debug("Loaded {} user groups for publisher selection", options.size());
            
        } catch (Exception e) {
            log.error("Error loading user groups from group manager", e);
        }
        return options;
    }
}

