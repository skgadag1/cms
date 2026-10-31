package com.tbscg.doosan.magnolia.dxp.core.setup

import com.tbscg.doosan.magnolia.dxp.core.setup.task.bootstrap.DxpModuleBootstrapFacade
import info.magnolia.module.InstallContext
import info.magnolia.module.delta.DeltaBuilder
import info.magnolia.module.delta.Task
import info.magnolia.module.model.ModuleDefinition
import info.magnolia.module.model.Version
import spock.lang.Specification

class DefaultDxpVersionHandlerSpec extends Specification {

    DefaultDxpVersionHandler handler = new DefaultDxpVersionHandler();

    InstallContext installContext

    DxpModuleBootstrapFacade dxpModuleBootstrapFacade = Mock(DxpModuleBootstrapFacade.class)

    Task existingDeltaTask = Mock(Task.class)

    Task autoBootstrappingTask = Mock(Task.class)

    def setup() {
        installContext = Mock(InstallContext.class)
        def moduleDefinition = Mock(ModuleDefinition.class)
        installContext.getCurrentModuleDefinition() >> moduleDefinition
        moduleDefinition.getVersion() >> Version.parseVersion("1.9.0")
        handler.dxpModuleBootstrapFacade = dxpModuleBootstrapFacade;

        handler.register(DeltaBuilder.update(Version.parseVersion("1.8.0"), "some existing deltas").addTask(existingDeltaTask))
    }

    def "Installing fresh module with DefaultDxpVersionHandler uses auto bootstrapping"() {
        when:
        def deltas = handler.getDeltas(installContext, null)

        then:
        1 * dxpModuleBootstrapFacade.getDynamicAllTasksForModule() >> [autoBootstrappingTask]
        0 * dxpModuleBootstrapFacade.getStaticPreTasksForAllDependantModules(_)
        deltas.any { it.getTasks().any { it == autoBootstrappingTask } }
    }

    def "Updating existing module with DefaultDxpVersionHandler uses custom deltas but does NOT use auto bootstrapping"() {
        when:
        def deltas = handler.getDeltas(installContext, Version.parseVersion("1.7.0"))

        then:
        0 * dxpModuleBootstrapFacade.getDynamicAllTasksForModule()
        0 * dxpModuleBootstrapFacade.getStaticPreTasksForAllDependantModules(_)
        deltas.get(0).getTasks().get(0) == existingDeltaTask
    }
}
