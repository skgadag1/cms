package com.tbscg.doosan.magnolia.cloudinary.command;

import com.tbscg.doosan.magnolia.cloudinary.api.CloudinaryService;
import com.tbscg.doosan.magnolia.cloudinary.api.FolderService;
import com.tbscg.doosan.magnolia.dxp.core.util.PathUtil;
import info.magnolia.commands.MgnlCommand;
import info.magnolia.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class UpdateFoldersCommand extends MgnlCommand {

	private static final Logger log = LoggerFactory.getLogger(UpdateFoldersCommand.class);

	private final FolderService folders;
	private final CloudinaryService cloudinary;

	@Inject
	public UpdateFoldersCommand(FolderService folders, CloudinaryService cloudinary) {
		this.folders = folders;
		this.cloudinary = cloudinary;
	}

	@Override
	public boolean execute(Context context) throws Exception {
		log.debug("Preloading contents of root folder...");
		this.folders.getSubfolders(PathUtil.ROOT_FOLDER_PATH);
		this.cloudinary.getAssetsInFolder(PathUtil.ROOT_FOLDER_PATH);
		log.debug("Update finished");
		return true;
	}
}
