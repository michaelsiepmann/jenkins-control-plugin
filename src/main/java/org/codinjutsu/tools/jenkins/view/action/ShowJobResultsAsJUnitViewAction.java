package org.codinjutsu.tools.jenkins.view.action;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import org.codinjutsu.tools.jenkins.model.Build;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.view.BrowserPanel;
import org.codinjutsu.tools.jenkins.view.extension.ViewTestResultsAsJUnit;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;

public class ShowJobResultsAsJUnitViewAction extends AnAction {
    private static final Icon ICON = AllIcons.Actions.GroupByTestProduction;
    @NotNull
    private final BrowserPanel browserPanel;

    public ShowJobResultsAsJUnitViewAction(@NotNull BrowserPanel browserPanel) {
        super("Show test results", "Show test results as JUnit view", ICON);
        this.browserPanel = browserPanel;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);
        if (project != null) {
            new ViewTestResultsAsJUnit().handle(browserPanel, project);
        }
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        Build selectedBuild = browserPanel.getSelectedBuild();
        if (selectedBuild != null) {
            event.getPresentation().setVisible(true);
        } else {
            Job selectedJob = browserPanel.getSelectedJob();
            event.getPresentation().setVisible(selectedJob != null && selectedJob.isBuildable());
        }
    }
}
