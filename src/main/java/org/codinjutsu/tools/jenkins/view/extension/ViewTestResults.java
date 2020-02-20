package org.codinjutsu.tools.jenkins.view.extension;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.project.Project;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.view.BrowserPanel;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ViewTestResults {

    ExtensionPointName<ViewTestResults> EP_NAME = ExtensionPointName.create("Jenkins Control Plugin.viewTestResults");

    @Nullable
    @Nls(capitalization = Nls.Capitalization.Sentence)
    String getDescription();

    boolean canHandle(@NotNull Job job);

    void handle(@NotNull BrowserPanel browserPanel, @NotNull Project project);
}
