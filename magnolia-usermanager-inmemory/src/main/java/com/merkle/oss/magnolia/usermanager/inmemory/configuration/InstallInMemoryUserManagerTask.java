package com.merkle.oss.magnolia.usermanager.inmemory.configuration;

import info.magnolia.jcr.nodebuilder.AbstractNodeOperation;
import info.magnolia.jcr.nodebuilder.ErrorHandler;
import info.magnolia.jcr.nodebuilder.NodeOperation;
import info.magnolia.jcr.nodebuilder.Ops;
import info.magnolia.jcr.nodebuilder.task.ErrorHandling;
import info.magnolia.jcr.nodebuilder.task.NodeBuilderTask;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractRepositoryTask;
import info.magnolia.module.delta.TaskExecutionException;
import info.magnolia.repository.RepositoryConstants;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import com.merkle.oss.magnolia.usermanager.inmemory.InMemoryUserManager;

import jakarta.inject.Inject;

public class InstallInMemoryUserManagerTask extends AbstractRepositoryTask {
    private static final String TASK_NAME = "Install InMemoryUserManager";
    private static final String TASK_DESCRIPTION = "This task installs the InMemoryUserManager";
    private static final String PATH = "/server/security/userManagers";

    @Inject
    public InstallInMemoryUserManagerTask() {
        super(TASK_NAME, TASK_DESCRIPTION);
    }

    @Override
    protected void doExecute(final InstallContext ctx) throws TaskExecutionException {
        new NodeBuilderTask(getName(), getDescription(), ErrorHandling.strict, RepositoryConstants.CONFIG, PATH,
                Ops.getOrAddNode(InMemoryUserManager.REALM.getName(), NodeTypes.ContentNode.NAME).then(orderFirst()).then(
                    setOrAddProperty("class", InMemoryUserManager.class.getName()),
                    setOrAddProperty("lockTimePeriod", 5), // in minutes
                    setOrAddProperty("maxFailedLoginAttempts", 3)
                )
        ).execute(ctx);
    }

    public static NodeOperation setOrAddProperty(final String name, final Object newValue) {
        return new AbstractNodeOperation() {
            @Override
            protected Node doExec(final Node context, final ErrorHandler errorHandler) throws RepositoryException {
                final Value value = PropertyUtil.createValue(newValue, context.getSession().getValueFactory());
                context.setProperty(name, value);
                return context;
            }
        };
    }

    public static NodeOperation orderFirst() {
        return new AbstractNodeOperation() {
            @Override
            protected Node doExec(final Node context, final ErrorHandler errorHandler) throws RepositoryException {
                NodeUtil.orderFirst(context);
                return context;
            }
        };
    }
}

