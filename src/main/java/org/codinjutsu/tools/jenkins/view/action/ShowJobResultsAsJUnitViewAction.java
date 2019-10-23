package org.codinjutsu.tools.jenkins.view.action;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import org.codinjutsu.tools.jenkins.model.Build;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.view.BrowserPanel;
import org.codinjutsu.tools.jenkins.view.action.results.JobTestResultsToolWindow;

import javax.swing.Icon;

public class ShowJobResultsAsJUnitViewAction extends AnAction {
    private static final Icon ICON = AllIcons.Actions.GroupByTestProduction;

    private final BrowserPanel browserPanel;

    public ShowJobResultsAsJUnitViewAction(BrowserPanel browserPanel) {
        super("Show test results", "Show test results as JUnit view", ICON);
        this.browserPanel = browserPanel;
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);
        Job job = browserPanel.getSelectedJob();
        if (job == null) {
            Build selectedBuild = browserPanel.getSelectedBuild();
            new JobTestResultsToolWindow(project, selectedBuild, buildLabel(selectedBuild)).showMavenToolWindow();
        } else {
            new JobTestResultsToolWindow(project, job.getLastBuild(), job.getName()).showMavenToolWindow();
        }
    }

    private String buildLabel(Build build) {
        return String.format("%s #%d", build.getJob().getName(), build.getNumber());
    }

    @Override
    public void update(AnActionEvent event) {
        Build selectedBuild = browserPanel.getSelectedBuild();
        if (selectedBuild != null) {
            event.getPresentation().setVisible(true);
        } else {
            Job selectedJob = browserPanel.getSelectedJob();
            event.getPresentation().setVisible(selectedJob != null && selectedJob.isBuildable());
        }
    }
}
