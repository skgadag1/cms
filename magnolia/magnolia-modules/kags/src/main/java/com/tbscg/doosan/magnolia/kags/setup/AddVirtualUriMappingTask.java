package com.tbscg.doosan.magnolia.kags.setup;

import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.module.delta.ArrayDelegateTask;
import info.magnolia.module.delta.CreateNodePathTask;
import info.magnolia.module.delta.SetPropertyTask;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.virtualuri.mapping.DefaultVirtualUriMapping;

public class AddVirtualUriMappingTask extends ArrayDelegateTask {

	public AddVirtualUriMappingTask(
			String module, 
			String mappingName, 
			Class<? extends DefaultVirtualUriMapping> mappingClass,
			String fromUri,
			String toUri) {

		super(String.format("Create a virtualUriMapping '%s' in module '%s'", mappingName, module));

		String nodePath = "/modules/" + module + "/virtualUriMappings/" + mappingName;

		addTask(new CreateNodePathTask("Create a mapping node", nodePath, NodeTypes.ContentNode.NAME));

		addTask(new SetPropertyTask(RepositoryConstants.CONFIG, nodePath, "class", mappingClass.getName()));
		addTask(new SetPropertyTask(RepositoryConstants.CONFIG, nodePath, "fromUri", fromUri));
		addTask(new SetPropertyTask(RepositoryConstants.CONFIG, nodePath, "toUri", toUri));
	}
}
