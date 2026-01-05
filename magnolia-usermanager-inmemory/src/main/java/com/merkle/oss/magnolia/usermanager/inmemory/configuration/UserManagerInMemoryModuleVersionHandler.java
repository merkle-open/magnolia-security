package com.merkle.oss.magnolia.usermanager.inmemory.configuration;

import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.Task;
import info.magnolia.module.model.Version;

import java.util.List;

import com.merkle.oss.magnolia.usermanager.inmemory.filter.InstallClientIdBasicLoginHandlerTask;

import jakarta.inject.Inject;

public class UserManagerInMemoryModuleVersionHandler extends DefaultModuleVersionHandler {
	private final List<Task> installOrUpdateTasks;

    @Inject
	public UserManagerInMemoryModuleVersionHandler(
			final InstallInMemoryUserManagerTask installInMemoryUserManagerTask,
			final InstallClientIdBasicLoginHandlerTask installClientIdBasicLoginHandlerTask
	) {
		this.installOrUpdateTasks = List.of(
				installInMemoryUserManagerTask,
				installClientIdBasicLoginHandlerTask
		);
    }

	@Override
	protected final List<Task> getExtraInstallTasks(final InstallContext installContext) { // when module node does not exist
		return installOrUpdateTasks;
	}

	@Override
	protected final List<Task> getDefaultUpdateTasks(final Version forVersion) { //on every module update
		return installOrUpdateTasks;
	}
}
