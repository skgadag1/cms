package com.tbscg.doosan.magnolia.dxp.core.setup.task.bootstrap

import com.tbscg.doosan.magnolia.dxp.core.setup.placeholder.PlaceholderFacade
import info.magnolia.module.InstallContext
import info.magnolia.module.ModuleRegistry
import info.magnolia.module.ModuleVersionHandler
import info.magnolia.module.delta.Task
import info.magnolia.module.model.DependencyDefinition
import info.magnolia.module.model.ModuleDefinition
import info.magnolia.module.model.Version
import spock.lang.Specification

import javax.inject.Provider

class DxpModuleBootstrapFacadeSpec extends Specification {

    Provider<String> authorProvider, environmentProvider;

    PlaceholderFacade placeholderFacade = Mock(PlaceholderFacade.class);

    ModuleRegistry moduleRegistry = Mock(ModuleRegistry.class);

    DxpModuleBootstrapFacade facade;

    InstallContext installContext

    ModuleDefinition moduleDefinition

    def setup() {
        authorProvider = new Provider<String>() {
            @Override
            String get() {
                return "author"
            }
        }
        environmentProvider = new Provider<String>() {
            @Override
            String get() {
                return "dev"
            }
        }

        facade = new DxpModuleBootstrapFacade(authorProvider, environmentProvider, placeholderFacade, moduleRegistry);

        installContext = Mock(InstallContext.class)
        moduleDefinition = Mock(ModuleDefinition.class)
        installContext.getCurrentModuleDefinition() >> moduleDefinition

        setUpModuleRegistry()
    }

    private void setUpModuleRegistry() {
        ModuleDefinition dxpCore = new ModuleDefinition("dxp-core", Mock(Version.class), "", ModuleVersionHandler.class);

        ModuleDefinition faq = new ModuleDefinition("faq", Mock(Version.class), "", ModuleVersionHandler.class);
        faq.addDependency(new DependencyDefinition("dxp-core", "", false))

        ModuleDefinition regions = new ModuleDefinition("regions", Mock(Version.class), "", ModuleVersionHandler.class);
        regions.addDependency(new DependencyDefinition("dxp-core", "", false))

        ModuleDefinition other = new ModuleDefinition("otherModule", Mock(Version.class), "", ModuleVersionHandler.class);

        moduleRegistry.getModuleDefinitions() >> [dxpCore, faq, regions, other]
    }

    def 'creating dynamic tasks for module returns 4 pre tasks and 4 tasks'() {
        given:
        moduleDefinition.getName() >> "myModuleName"

        when:
        List<Task> tasks = facade.getDynamicAllTasksForModule();

        then:
        tasks.size() == 8
        tasks.get(0) instanceof DxpModuleBootstrapPreTask
        tasks.get(1) instanceof DxpModuleBootstrapPreTask
        tasks.get(2) instanceof DxpModuleBootstrapPreTask
        tasks.get(3) instanceof DxpModuleBootstrapPreTask
        tasks.get(4) instanceof DxpModuleBootstrapTask
        tasks.get(5) instanceof DxpModuleBootstrapTask
        tasks.get(6) instanceof DxpModuleBootstrapTask
        tasks.get(7) instanceof DxpModuleBootstrapTask
    }

    def 'creating dynamic tasks for module returns tasks which bootstrap only proper resources for given environment, module and instance type'() {
        given:
        moduleDefinition.getName() >> "myModuleName"

        when:
        List<Task> tasks = facade.getDynamicAllTasksForModule();

        then:
        tasks.get(0).resourceFilter.filter(installContext, "/mgnl-bootstrap/myModuleName/common/shared/someFile.yaml")
        tasks.get(1).resourceFilter.filter(installContext, "/mgnl-bootstrap/myModuleName/common/dev/someFile.yaml")
        tasks.get(2).resourceFilter.filter(installContext, "/mgnl-bootstrap/myModuleName/author/shared/someFile.yaml")
        tasks.get(3).resourceFilter.filter(installContext, "/mgnl-bootstrap/myModuleName/author/dev/someFile.yaml")

        tasks.get(4).resourceFilter.filter(installContext, "/mgnl-bootstrap/myModuleName/common/shared/someFile.yaml")
        tasks.get(5).resourceFilter.filter(installContext, "/mgnl-bootstrap/myModuleName/common/dev/someFile.yaml")
        tasks.get(6).resourceFilter.filter(installContext, "/mgnl-bootstrap/myModuleName/author/shared/someFile.yaml")
        tasks.get(7).resourceFilter.filter(installContext, "/mgnl-bootstrap/myModuleName/author/dev/someFile.yaml")
    }

    def 'creating dynamic tasks for module returns tasks which firstly perform dry run for current module'() {
        given:
        moduleDefinition.getName() >> "myModuleName"

        when:
        List<Task> tasks = facade.getDynamicAllTasksForModule();
        tasks.get(0).execute(installContext)

        then:
        1 * placeholderFacade.dryCheck(_, "myModuleName")
        0 * placeholderFacade.apply(_, _)
    }

    def 'creating dynamic tasks for module returns tasks which secondly perform apply run for current module'() {
        given:
        moduleDefinition.getName() >> "myModuleName"

        when:
        List<Task> tasks = facade.getDynamicAllTasksForModule();
        tasks.get(4).execute(installContext)

        then:
        1 * placeholderFacade.apply(_, "myModuleName")
        0 * placeholderFacade.dryCheck(_, _)
    }

    def 'creating dynamic tasks for module returns tasks which blocks bootstrapping incorrect resources'() {
        given:
        moduleDefinition.getName() >> "myModuleName"

        when:
        List<Task> tasks = facade.getDynamicAllTasksForModule();

        then:
        // wrong environment
        !tasks.any{it.resourceFilter.filter(installContext, "/mgnl-bootstrap/myModuleName/common/prod/someFile.yaml")}
        // wrong instance type
        !tasks.any{it.resourceFilter.filter(installContext, "/mgnl-bootstrap/myModuleName/public/shared/someFile.yaml")}
        // wrong module
        !tasks.any{it.resourceFilter.filter(installContext, "/mgnl-bootstrap/otherModule/common/shared/someFile.yaml")}
    }



    def 'creating static pre tasks happens only for modules being dependant on core module, which bootstrap only proper resources for given environment and instance type for each module'() {
        given:
        moduleDefinition.getName() >> "dxp-core"

        when:
        List<Task> tasks = facade.getStaticPreTasksForAllDependantModules("dxp-core");

        then:
        tasks.size() == 8
        tasks.every{it instanceof DxpModuleBootstrapPreTask}

        tasks.get(0).resourceFilter.filter(installContext, "/mgnl-bootstrap/faq/common/shared/someFile.yaml")
        tasks.get(1).resourceFilter.filter(installContext, "/mgnl-bootstrap/faq/common/dev/someFile.yaml")
        tasks.get(2).resourceFilter.filter(installContext, "/mgnl-bootstrap/faq/author/shared/someFile.yaml")
        tasks.get(3).resourceFilter.filter(installContext, "/mgnl-bootstrap/faq/author/dev/someFile.yaml")

        tasks.get(4).resourceFilter.filter(installContext, "/mgnl-bootstrap/regions/common/shared/someFile.yaml")
        tasks.get(5).resourceFilter.filter(installContext, "/mgnl-bootstrap/regions/common/dev/someFile.yaml")
        tasks.get(6).resourceFilter.filter(installContext, "/mgnl-bootstrap/regions/author/shared/someFile.yaml")
        tasks.get(7).resourceFilter.filter(installContext, "/mgnl-bootstrap/regions/author/dev/someFile.yaml")
    }

    def 'creating static pre tasks returns tasks which blocks bootstrapping incorrect resources'() {
        given:
        moduleDefinition.getName() >> "dxp-core"

        when:
        List<Task> tasks = facade.getStaticPreTasksForAllDependantModules("dxp-core");

        then:
        // wrong environment
        !tasks.any{it.resourceFilter.filter(installContext, "/mgnl-bootstrap/faq/common/prod/someFile.yaml")}
        // wrong instance type
        !tasks.any{it.resourceFilter.filter(installContext, "/mgnl-bootstrap/faq/public/shared/someFile.yaml")}
        // module not dependant on dxp-core
        !tasks.any{it.resourceFilter.filter(installContext, "/mgnl-bootstrap/otherModule/common/shared/someFile.yaml")}
        // neither dxp-core itself
        !tasks.any{it.resourceFilter.filter(installContext, "/mgnl-bootstrap/dxp-core/common/shared/someFile.yaml")}
    }

    def 'creating static pre tasks returns tasks which perform dry run for modules being dependant on core module'() {
        given:
        moduleDefinition.getName() >> "dxp-core"

        when:
        List<Task> tasks = facade.getStaticPreTasksForAllDependantModules("dxp-core");
        tasks.get(0).execute(installContext)

        then:
        1 * placeholderFacade.dryCheck(_, "faq")
        0 * placeholderFacade.apply(_, _)
    }
}
