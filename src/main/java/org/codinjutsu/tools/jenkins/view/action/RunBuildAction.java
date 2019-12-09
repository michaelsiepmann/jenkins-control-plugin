/*
 * Copyright (c) 2013 David Boissier
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codinjutsu.tools.jenkins.view.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import org.apache.log4j.Logger;
import org.codinjutsu.tools.jenkins.JenkinsAppSettings;
import org.codinjutsu.tools.jenkins.logic.ExecutorService;
import org.codinjutsu.tools.jenkins.logic.RequestManager;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.model.ViewElement;
import org.codinjutsu.tools.jenkins.util.GuiUtil;
import org.codinjutsu.tools.jenkins.util.HtmlUtil;
import org.codinjutsu.tools.jenkins.view.BrowserPanel;
import org.codinjutsu.tools.jenkins.view.BuildParamDialog;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;
import java.util.concurrent.TimeUnit;

public class RunBuildAction extends AnAction implements DumbAware {

    private static final Icon EXECUTE_ICON = GuiUtil.isUnderDarcula() ? GuiUtil.loadIcon("execute_dark.png") : GuiUtil.loadIcon("execute.png");
    private static final Logger LOG = Logger.getLogger(RunBuildAction.class.getName());
    private static final int BUILD_STATUS_UPDATE_DELAY = 1;

    private final BrowserPanel browserPanel;


    public RunBuildAction(BrowserPanel browserPanel) {
        super("Build on Jenkins", "Run a build on Jenkins Server", EXECUTE_ICON);
        this.browserPanel = browserPanel;
    }


    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        final Project project = ActionUtil.getProject(event);

        final BrowserPanel browserPanel = BrowserPanel.getInstance(project);
        try {
            final Job job = browserPanel.getSelectedJob();
            new Task.Backgroundable(project, "Running build", false) {

                @Override
                public void onSuccess() {
                    notifyOnGoingMessage(job);
                    ExecutorService.getInstance(project).getExecutor().schedule(() ->
                            GuiUtil.runInSwingThread(() -> {
                                final ViewElement newJob = browserPanel.getJob(job.getJobName());
                                browserPanel.loadJob(newJob);
                            }), BUILD_STATUS_UPDATE_DELAY, TimeUnit.SECONDS); //FIXME check delay coud be in settings
                }

                @Override
                public void run(@NotNull ProgressIndicator progressIndicator) {
                    progressIndicator.setIndeterminate(true);
                    RequestManager requestManager = browserPanel.getJenkinsManager();
                    if (job.hasParameters()) {
                        BuildParamDialog.showDialog(job, JenkinsAppSettings.getSafeInstance(project), requestManager, new BuildParamDialog.RunBuildCallback() {

                            @Override
                            public void notifyOnOk(Job job) {
                                notifyOnGoingMessage(job);
                                browserPanel.loadJob(job);
                            }

                            @Override
                            public void notifyOnError(Job job, Exception ex) {
                                browserPanel.notifyErrorJenkinsToolWindow("Build '" + job.getJobName() + "' cannot be run: " + ex.getMessage());
                                browserPanel.loadJob(job);
                            }

                        });

                    } else {
                        requestManager.runBuild(job, JenkinsAppSettings.getSafeInstance(project));
                    }
                }
            }.queue();

        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
            browserPanel.notifyErrorJenkinsToolWindow("Build cannot be run: " + ex.getMessage());
        }
    }

    @Override
    public void update(AnActionEvent event) {
        Job selectedJob = browserPanel.getSelectedJob();
        event.getPresentation().setVisible(selectedJob != null && selectedJob.isBuildable());
    }


    private void notifyOnGoingMessage(Job job) {
        browserPanel.notifyInfoJenkinsToolWindow(HtmlUtil.createHtmlLinkMessage(
                job.getJobName() + " build is on going",
                job.getUrl()));
    }
}
