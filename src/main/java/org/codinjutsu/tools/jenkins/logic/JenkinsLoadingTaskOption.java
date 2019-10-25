package org.codinjutsu.tools.jenkins.logic;

import com.intellij.openapi.progress.PerformInBackgroundOption;

public class JenkinsLoadingTaskOption implements PerformInBackgroundOption {

    public static JenkinsLoadingTaskOption INSTANCE = new JenkinsLoadingTaskOption();

    @Override
    public boolean shouldStartInBackground() {
        return true;
    }

    @Override
    public void processSentToBackground() {

    }

}
