/*
 * Copyright 2014 Dawid Pytel
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

package org.codinjutsu.tools.jenkins.view.action.results;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configuration.ConfigurationFactoryEx;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.UnknownConfigurationType;
import com.intellij.execution.configurations.UnknownRunConfiguration;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.testframework.TestConsoleProperties;
import com.intellij.execution.testframework.sm.SMTestRunnerConnectionUtil;
import com.intellij.execution.testframework.ui.BaseTestsOutputConsoleView;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComponentContainer;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import org.codinjutsu.tools.jenkins.model.Build;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.OutputStream;
import java.util.Optional;

import static org.codinjutsu.tools.jenkins.view.action.results.JobTestResultsToolWindowFactory.TOOL_WINDOW_ID;

public class JobTestResultsToolWindow {

    private final Project project;
    private final Build build;
    private String tabName;

    public JobTestResultsToolWindow(Project project, Build build, String tabName) {
        this.project = project;
        this.tabName = tabName;
        this.build = build;
    }

    public void showMavenToolWindow() {
        ConfigurationType configurationType = UnknownConfigurationType.getInstance();
        ConfigurationFactory configurationFactory = new ConfigurationFactory(configurationType) {
            @NotNull
            @Override
            public RunConfiguration createTemplateConfiguration(@NotNull Project project) {
                return new UnknownRunConfiguration(this, project);
            }
        };
        RunConfiguration configuration = new UnknownRunConfiguration(configurationFactory, project);
        Executor executor = new DefaultRunExecutor();
        ProcessHandler processHandler = new MyProcessHandler();
        TestConsoleProperties consoleProperties = new JobTestConsoleProperties(build, project, executor, configuration, processHandler);
        BaseTestsOutputConsoleView consoleView;
        try {
            consoleView = SMTestRunnerConnectionUtil.createAndAttachConsole(TOOL_WINDOW_ID, processHandler, consoleProperties);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        showInToolWindow(consoleView, tabName);
        processHandler.startNotify();
    }

    private void showInToolWindow(ComponentContainer consoleView, String tabName) {
        getToolWindow().ifPresent(toolWindow -> showInToolWindow(toolWindow, consoleView, tabName));
    }

    private void showInToolWindow(ToolWindow toolWindow, ComponentContainer consoleView, String tabName) {
        toolWindow.setAvailable(true, null);
        toolWindow.activate(null);
        toolWindow.show(null);
        ContentManager contentManager = toolWindow.getContentManager();
        Content content = contentManager.getFactory()
                .createContent(consoleView.getComponent(), tabName, false);
        Disposer.register(content, consoleView);
        contentManager.addContent(content);
        contentManager.setSelectedContent(content);
    }

    @NotNull
    private Optional<ToolWindow> getToolWindow() {
        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        return Optional.ofNullable(toolWindowManager.getToolWindow(TOOL_WINDOW_ID));
    }

    private static class MyProcessHandler extends ProcessHandler {
        @Override
        protected void destroyProcessImpl() {

        }

        @Override
        protected void detachProcessImpl() {

        }

        @Override
        public boolean detachIsDefault() {
            return true;
        }

        @Nullable
        @Override
        public OutputStream getProcessInput() {
            return null;
        }
    }
}
