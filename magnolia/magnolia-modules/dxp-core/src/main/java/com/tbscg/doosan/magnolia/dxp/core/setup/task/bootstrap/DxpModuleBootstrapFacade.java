package com.tbscg.doosan.magnolia.dxp.core.setup.task.bootstrap;

import com.tbscg.doosan.magnolia.dxp.core.setup.placeholder.PlaceholderFacade;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.module.delta.Task;
import info.magnolia.module.model.DependencyDefinition;
import info.magnolia.module.model.ModuleDefinition;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

import static com.tbscg.doosan.magnolia.dxp.core.util.EnvironmentConstants.*;
import static java.util.stream.Collectors.toList;


@Singleton
public class DxpModuleBootstrapFacade {

    private final String currentInstanceType;
    private final String currentEnvironment;
    private final PlaceholderFacade placeholderFacade;
    private final ModuleRegistry moduleRegistry;

    @Inject
    public DxpModuleBootstrapFacade(@Named("instanceType") Provider<String> currentInstanceType, @Named("environment") Provider<String> currentEnvironment, PlaceholderFacade placeholderFacade, ModuleRegistry moduleRegistry) {
        this.currentInstanceType = currentInstanceType.get();
        this.currentEnvironment = currentEnvironment.get();
        this.placeholderFacade = placeholderFacade;
        this.moduleRegistry = moduleRegistry;
    }

    /**
     * Create dry-check pre tasks for bootstrapping purpose of all modules depending on given module.
     * Such approach ensures atomicity of bootstrapping on all modules level.
     */
    public List<Task> getStaticPreTasksForAllDependantModules(String dependantModuleName) {
        List<Task> tasks = new ArrayList<>();

        List<ModuleDefinition> moduleDefinitions = moduleRegistry.getModuleDefinitions();
        for (ModuleDefinition def : moduleDefinitions) {
            for (DependencyDefinition dependency : def.getDependencies()) {
                String name = dependency.getName();
                if (name.equals(dependantModuleName)) {
                    List<DxpResourceStaticFilter> staticResourceFilters = createOrderedStaticResourceFilters(def.getName());
                    tasks.addAll(staticResourceFilters.stream().map(filter -> new DxpModuleBootstrapPreTask(filter, placeholderFacade)).collect(toList()));
                    break;
                }
            }
        }
        return tasks;
    }

    /**
     * Create all tasks for bootstrapping purpose of single module in specified order:
     * first pre-tasks with dry check, and then actual tasks which apply changes.
     * Such approach ensures atomicity of bootstrapping on single module level.
     */
    public List<Task> getDynamicAllTasksForModule() {
        List<DxpResourceDynamicFilter> dynamicResourceFilters = createOrderedDynamicResourceFilters();
        List<Task> tasks = new ArrayList<>();

        tasks.addAll(dynamicResourceFilters.stream().map(filter -> new DxpModuleBootstrapPreTask(filter, placeholderFacade)).collect(toList()));
        tasks.addAll(dynamicResourceFilters.stream().map(filter -> new DxpModuleBootstrapTask(filter, placeholderFacade)).collect(toList()));

        return tasks;
    }

    private List<DxpResourceStaticFilter> createOrderedStaticResourceFilters(String moduleName) {
        List<DxpResourceStaticFilter> filters = new ArrayList<>();

        filters.add(new DxpResourceStaticFilter(COMMON_INSTANCE, SHARED_ENVIRONMENT, moduleName));

        if(currentEnvironment.equals(FEATURE_ENVIRONMENT)) {
           filters.add(new DxpResourceStaticFilter(COMMON_INSTANCE, DEV_ENVIRONMENT, moduleName));
        }
        filters.add(new DxpResourceStaticFilter(COMMON_INSTANCE, currentEnvironment, moduleName));

        filters.add(new DxpResourceStaticFilter(currentInstanceType, SHARED_ENVIRONMENT, moduleName));

        if(currentEnvironment.equals(FEATURE_ENVIRONMENT)) {
           filters.add(new DxpResourceStaticFilter(currentInstanceType, DEV_ENVIRONMENT, moduleName));
        }
        filters.add(new DxpResourceStaticFilter(currentInstanceType, currentEnvironment, moduleName));

        return filters;
    }

    private List<DxpResourceDynamicFilter> createOrderedDynamicResourceFilters() {
        List<DxpResourceDynamicFilter> filters = new ArrayList<>();

        filters.add(new DxpResourceDynamicFilter(COMMON_INSTANCE, SHARED_ENVIRONMENT));
        filters.add(new DxpResourceDynamicFilter(COMMON_INSTANCE, currentEnvironment));
        filters.add(new DxpResourceDynamicFilter(currentInstanceType, SHARED_ENVIRONMENT));
        filters.add(new DxpResourceDynamicFilter(currentInstanceType, currentEnvironment));

        return filters;
    }
}
