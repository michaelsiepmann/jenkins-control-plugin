package org.codinjutsu.tools.jenkins.view.action;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import org.apache.log4j.Logger;
import org.codinjutsu.tools.jenkins.logic.RequestManager;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.view.BrowserPanel;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;

public class StopBuildAction extends AnAction implements DumbAware {

    private static final Icon STOP_ICON = AllIcons.Actions.Suspend;
    private static final Logger LOG = Logger.getLogger(RunBuildAction.class.getName());

    private final BrowserPanel browserPanel;


    public StopBuildAction(BrowserPanel browserPanel) {
        super("Stop on Jenkins", "Stop a build on Jenkins Server", STOP_ICON);
        this.browserPanel = browserPanel;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        final Project project = ActionUtil.getProject(event);

        try {
            final Job job = browserPanel.getSelectedJob();
            new Task.Backgroundable(project, "Stopping build", false){

                @Override
                public void onSuccess() {
                    browserPanel.loadJob(job);
//                    browserPanel.refreshCurrentView();
                }

                @Override
                public void run(@NotNull ProgressIndicator progressIndicator) {
                    RequestManager requestManager = RequestManager.getInstance(project);
                    requestManager.stopBuild(job.getLastBuild());
                }
            }.queue();

        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
            browserPanel.notifyErrorJenkinsToolWindow("Build cannot be run: " + ex.getMessage());
        }
    }

    @Override
    public void update(AnActionEvent event) {
        Job selectedJob = null; //browserPanel.getSelectedJob(); //temporarily disabled
        event.getPresentation().setVisible(selectedJob != null && selectedJob.isBuildable());
    }
}
