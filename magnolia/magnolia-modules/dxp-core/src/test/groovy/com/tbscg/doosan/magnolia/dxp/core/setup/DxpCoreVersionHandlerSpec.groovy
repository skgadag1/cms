package com.tbscg.doosan.magnolia.dxp.core.setup


import com.tbscg.doosan.magnolia.dxp.core.setup.task.bootstrap.DxpModuleBootstrapFacade
import info.magnolia.module.InstallContext
import info.magnolia.module.delta.DeltaBuilder
import info.magnolia.module.delta.Task
import info.magnolia.module.model.ModuleDefinition
import info.magnolia.module.model.Version
import spock.lang.Specification

class DxpCoreVersionHandlerSpec extends Specification {
    DxpCoreVersionHandler handler = new DxpCoreVersionHandler();

    InstallContext installContext

    DxpModuleBootstrapFacade dxpModuleBootstrapFacade = Mock(DxpModuleBootstrapFacade.class)

    Task existingDeltaTask = Mock(Task.class)

    Task autoBootstrappingTask = Mock(Task.class)

    Task staticPreTask = Mock(Task.class)

    def setup() {
        installContext = Mock(InstallContext.class)
        def moduleDefinition = Mock(ModuleDefinition.class)
        installContext.getCurrentModuleDefinition() >> moduleDefinition
        moduleDefinition.getVersion() >> Version.parseVersion("1.9.0")
        moduleDefinition.getName() >> "dxp-core"
        handler.dxpModuleBootstrapFacade = dxpModuleBootstrapFacade;

        handler.register(DeltaBuilder.update(Version.parseVersion("1.8.0"), "some existing deltas").addTask(existingDeltaTask))
    }

    def "Installing fresh module in DxpCoreVersionHandler uses first static pre tasks for dependant modules, then auto bootstrapping"() {
        when:
        def deltas = handler.getDeltas(installContext, null)

        then:
        1 * dxpModuleBootstrapFacade.getDynamicAllTasksForModule() >> [autoBootstrappingTask]
        1 * dxpModuleBootstrapFacade.getStaticPreTasksForAllDependantModules("dxp-core") >> [staticPreTask]
        deltas.get(0).getTasks().get(0) == staticPreTask
        deltas.get(1).getTasks().any { it == autoBootstrappingTask }
    }

    def "Updating existing module in DxpCoreVersionHandler uses first static pre tasks for dependant modules, then auto bootstrapping, then custom deltas"() {
        when:
        def deltas = handler.getDeltas(installContext, Version.parseVersion("1.7.0"))

        then:
        1 * dxpModuleBootstrapFacade.getDynamicAllTasksForModule() >> [autoBootstrappingTask]
        1 * dxpModuleBootstrapFacade.getStaticPreTasksForAllDependantModules("dxp-core") >> [staticPreTask]
        deltas.get(0).getTasks().get(0) == staticPreTask
        deltas.get(1).getTasks().get(0) == autoBootstrappingTask
        deltas.get(2).getTasks().get(0) == existingDeltaTask
    }
}
