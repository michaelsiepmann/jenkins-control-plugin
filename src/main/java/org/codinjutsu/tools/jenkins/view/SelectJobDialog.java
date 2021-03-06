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

package org.codinjutsu.tools.jenkins.view;

import com.intellij.application.options.CodeStyle;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.diff.impl.patch.FilePatch;
import com.intellij.openapi.diff.impl.patch.IdeaTextPatchBuilder;
import com.intellij.openapi.diff.impl.patch.UnifiedDiffWriter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeList;
import com.intellij.openapi.vcs.changes.patch.PatchWriter;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.IdeBorderFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.codinjutsu.tools.jenkins.JenkinsAppSettings;
import org.codinjutsu.tools.jenkins.exception.ConfigurationException;
import org.codinjutsu.tools.jenkins.logic.RequestManager;
import org.codinjutsu.tools.jenkins.model.ViewElement;
import org.codinjutsu.tools.jenkins.view.action.UploadPatchToJob;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.MutableComboBoxModel;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.codinjutsu.tools.jenkins.util.HtmlUtil.createHtmlLinkMessage;
import static org.codinjutsu.tools.jenkins.view.action.UploadPatchToJob.PARAMETER_NAME;

public class SelectJobDialog extends JDialog {

    private static final String FILENAME = "jenkins.diff";

    private static final Logger LOG = Logger.getInstance(UploadPatchToJob.class.getName());

    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JComboBox<String> jobsList;
    private JList<String> changedFilesList;
    private JScrollPane changedFilesPane;

    private MutableComboBoxModel<String> listModel = new DefaultComboBoxModel<>();

    private Project project;

    private ChangeList[] changeLists;

    public SelectJobDialog(ChangeList[] changeLists, Collection<ViewElement> jobs, Project project) {

        this.project = project;

        this.changeLists = changeLists;

        fillJobList(jobs);

        fillChangedFilesList();

        setContentPane(contentPane);
        setModal(true);

        setTitle("Create Patch and build on Jenkins");
        setResizable(false);

        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(event -> onOK());

        buttonCancel.addActionListener(e -> onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void fillJobList(Collection<ViewElement> jobs) {
        if (null != jobs) {
            jobs.stream()
                .filter(job -> job.hasParameters() && job.hasParameter(PARAMETER_NAME))
                .forEach(job -> listModel.addElement(job.getJobName()));
        }

        jobsList.setModel(listModel);
    }

    private void fillChangedFilesList() {

        DefaultListModel<String> model = new DefaultListModel<>();

        if (changeLists != null && (changeLists.length > 0)) {
            StringBuilder builder = new StringBuilder();

            int count = 1;
            for (ChangeList changeList : changeLists) {
                builder.append(changeList.getName());
                if (count < changeLists.length) {
                    builder.append(", ");
                }
                if (changeList.getChanges().size() > 0) {
                    for (Change change : changeList.getChanges()) {
                        VirtualFile virtualFile = change.getVirtualFile();
                        if (null != virtualFile) {
                            model.addElement(virtualFile.getPath());
                        }
                    }
                }
                count++;
            }

            changedFilesPane.setBorder(IdeBorderFactory.createTitledBorder(String.format("Changelists: %s", builder.toString()), true));

        }

        changedFilesList.setModel(model);
    }

    private void createPatch() throws IOException, VcsException {
        FileWriter writer = new FileWriter(FILENAME);
        Collection<Change> changes = new ArrayList<>();
        if (changeLists.length > 0) {
            for (ChangeList changeList : changeLists) {
                changes.addAll(changeList.getChanges());
            }
        }
        String base = PatchWriter.calculateBaseForWritingPatch(project, changes).getPath();
        List<FilePatch> patches = IdeaTextPatchBuilder.buildPatch(project, changes, base, false);
        UnifiedDiffWriter.write(project, patches, writer, CodeStyle.getProjectOrDefaultSettings(project).getLineSeparator(), null);
        writer.close();
    }

    private void watchJob(BrowserPanel browserPanel, ViewElement job) {
        if (changeLists.length > 0) {
            for (ChangeList list : changeLists) {
                browserPanel.addToWatch(list.getName(), job);
            }
        }
    }

    private void onOK() {
        BrowserPanel browserPanel = BrowserPanel.getInstance(project);
        try {
            createPatch();
            RequestManager requestManager = browserPanel.getJenkinsManager();
            String selectedJobName = (String) jobsList.getSelectedItem();
            if (StringUtils.isEmpty(selectedJobName)) {
                return;
            }
            ViewElement selectedJob = browserPanel.getJob(selectedJobName);
            if (selectedJob == null) {
                return;
            }
            if (!selectedJob.hasParameters()) {
                throw new ConfigurationException(String.format("Job \"%s\" has no parameters", selectedJob.getJobName()));
            }
            if (!selectedJob.hasParameter(PARAMETER_NAME)) {
                throw new ConfigurationException(String.format("Job \"%s\" should have parameter with name \"%s\"", selectedJob.getJobName(), PARAMETER_NAME));
            }
            JenkinsAppSettings settings = JenkinsAppSettings.getSafeInstance(project);
            Map<String, VirtualFile> files = new HashMap<>();
            VirtualFile virtualFile = UploadPatchToJob.prepareFile(browserPanel, LocalFileSystem.getInstance().refreshAndFindFileByIoFile(new File(FILENAME)), settings, selectedJob);
            if (virtualFile == null || !virtualFile.exists()) {
                throw new ConfigurationException(String.format("File \"%s\" not found", virtualFile != null ? virtualFile.getPath() : FILENAME));
            }
            files.put(PARAMETER_NAME, virtualFile);
            requestManager.runBuild(selectedJob, settings, files);
            //browserPanel.loadSelectedJob();
            browserPanel.notifyInfoJenkinsToolWindow(createHtmlLinkMessage(
                    selectedJob.getJobName() + " build is on going",
                    selectedJob.getUrl())
            );
            watchJob(browserPanel, selectedJob);
        } catch (Exception e) {
            String message = String.format("Build cannot be run: %s", e.getMessage());
            LOG.info(message);
            browserPanel.notifyErrorJenkinsToolWindow(message);
        }

        deletePatchFile();

        dispose();

    }

    private void deletePatchFile() {
        File file = new File(FILENAME);
        FileUtils.deleteQuietly(file);
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public static void main(String[] args) {
        SelectJobDialog dialog = new SelectJobDialog(new ChangeList[]{}, null, null);
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
