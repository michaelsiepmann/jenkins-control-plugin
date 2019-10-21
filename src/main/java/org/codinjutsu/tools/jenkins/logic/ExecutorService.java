package org.codinjutsu.tools.jenkins.logic;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class ExecutorService {

    private final ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;

    public ExecutorService() {
        this.scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
    }

    public ScheduledThreadPoolExecutor getExecutor() {
        return scheduledThreadPoolExecutor;
    }

    public static ExecutorService getInstance(Project project) {
        return ServiceManager.getService(project, ExecutorService.class);
    }

    public void safeTaskCancel(Future<?> futureTask) {
        if (futureTask == null) {
            return;
        }
        if (!futureTask.isDone() || !futureTask.isCancelled()) {
            futureTask.cancel(false);
        }
    }
}
