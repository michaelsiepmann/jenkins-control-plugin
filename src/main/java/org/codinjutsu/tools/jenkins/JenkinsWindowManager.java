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

package org.codinjutsu.tools.jenkins;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.DumbAwareRunnable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import org.codinjutsu.tools.jenkins.logic.BrowserPanelAuthenticationHandler;
import org.codinjutsu.tools.jenkins.logic.ExecutorService;
import org.codinjutsu.tools.jenkins.logic.LoginService;
import org.codinjutsu.tools.jenkins.logic.RssAuthenticationActionHandler;
import org.codinjutsu.tools.jenkins.logic.RssLogic;
import org.codinjutsu.tools.jenkins.util.GuiUtil;
import org.codinjutsu.tools.jenkins.view.BrowserPanel;
import org.codinjutsu.tools.jenkins.view.JenkinsWidget;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;
import java.util.Optional;

public class JenkinsWindowManager implements Disposable {

    public static final String JENKINS_BROWSER = "Jenkins";

    private final Project project;

    @NotNull
    public static Optional<JenkinsWindowManager> getInstance(Project project) {
        return Optional.ofNullable(ServiceManager.getService(project, JenkinsWindowManager.class));
    }

    public JenkinsWindowManager(Project project) {
        this.project = project;
    }

    public void reloadConfiguration() {
        LoginService.getInstance(project).performAuthentication();
    }

    @Override
    public void dispose() {
        RssAuthenticationActionHandler.getInstance(project).dispose();
        BrowserPanelAuthenticationHandler.getInstance(project).dispose();

        JenkinsWidget.getInstance(project).dispose();

        ExecutorService.getInstance(project).getExecutor().shutdown();
    }
}