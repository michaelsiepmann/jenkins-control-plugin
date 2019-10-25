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
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsDataKeys;
import com.intellij.openapi.vcs.changes.ChangeList;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.ChangeListManagerEx;
import org.codinjutsu.tools.jenkins.view.BrowserPanel;
import org.codinjutsu.tools.jenkins.view.SelectJobDialog;
import org.jetbrains.annotations.NotNull;

import javax.swing.SwingUtilities;
import java.awt.Dimension;
import java.util.Arrays;

/**
 * CreatePatchAndBuildAction class
 *
 * @author Yuri Novitsky
 */
public class CreatePatchAndBuildAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        ChangeList[] selectedChangeLists = getChangeLists(event);
        if (selectedChangeLists != null) {
            showDialog(selectedChangeLists, ActionUtil.getProject(event));
        }
    }

    private void showDialog(ChangeList[] selectedChangeLists, Project project) {
        SwingUtilities.invokeLater(() -> {
            final BrowserPanel browserPanel = BrowserPanel.getInstance(project);
            SelectJobDialog dialog = new SelectJobDialog(selectedChangeLists, browserPanel.getJobs(), project);
            dialog.setLocationRelativeTo(null);
            dialog.setMaximumSize(new Dimension(300, 200));
            dialog.pack();
            dialog.setVisible(true);
        });
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        event.getPresentation().setEnabled(isEnabled(event));
    }

    private ChangeList[] getChangeLists(@NotNull AnActionEvent event) {
        DataContext dataContext = event.getDataContext();
        return VcsDataKeys.CHANGE_LISTS.getData(dataContext);
    }

    private boolean isEnabled(@NotNull AnActionEvent event) {
        Project project = ActionUtil.getProject(event);
        ChangeList[] selectedChangeLists = getChangeLists(event);
        if (selectedChangeLists != null && selectedChangeLists.length > 0) {
            ChangeListManagerEx changeListManager = (ChangeListManagerEx) ChangeListManager.getInstance(project);
            if (!changeListManager.isInUpdate()) {
                return Arrays.stream(selectedChangeLists).anyMatch(list -> list.getChanges().size() > 0);
            }
        }
        return false;
    }
}
