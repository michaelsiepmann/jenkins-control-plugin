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

import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.RowIcon;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.text.DateFormatUtil;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.codinjutsu.tools.jenkins.JenkinsSettings;
import org.codinjutsu.tools.jenkins.model.Build;
import org.codinjutsu.tools.jenkins.model.Jenkins;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.util.GuiUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.List;

import static com.intellij.ui.SimpleTextAttributes.REGULAR_ATTRIBUTES;
import static com.intellij.ui.SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES;
import static com.intellij.ui.SimpleTextAttributes.REGULAR_ITALIC_ATTRIBUTES;

public class JenkinsTreeRenderer extends ColoredTreeCellRenderer {

    private static final Icon FAVORITE_ICON = GuiUtil.loadIcon("star_tn.png");
    private static final Icon SERVER_ICON = GuiUtil.loadIcon("server_wrench.png");

    private final List<JenkinsSettings.FavoriteJob> favoriteJobs;

    JenkinsTreeRenderer(List<JenkinsSettings.FavoriteJob> favoriteJobs) {
        this.favoriteJobs = favoriteJobs;
    }

    @Override
    public void customizeCellRenderer(@NotNull JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;

        Object userObject = node.getUserObject();
        if (userObject instanceof Jenkins) {
            Jenkins jenkins = (Jenkins) userObject;
            append(buildLabel(jenkins), REGULAR_ITALIC_ATTRIBUTES);
            setToolTipText(jenkins.getServerUrl());
            setIcon(SERVER_ICON);

        } else if (userObject instanceof Job) {
            Job job = (Job) userObject;

            append(buildLabel(job), getAttribute(job));

            setToolTipText(job.findHealthDescription());
            if (isFavoriteJob(job)) {
                setIcon(new CompositeIcon(job.getStateIcon(), job.getHealthIcon(), FAVORITE_ICON));
            } else {
                setIcon(new CompositeIcon(job.getStateIcon(), job.getHealthIcon()));
            }
        } else if (userObject instanceof Build) {
            Build build = (Build) userObject;
            append(buildLabel(build), REGULAR_ITALIC_ATTRIBUTES);
            setIcon(new CompositeIcon(build.getStateIcon()));
        }
    }

    private boolean isFavoriteJob(Job job) {
        String jobName = job.getName();
        return favoriteJobs.stream().anyMatch(favoriteJob -> favoriteJob.name.equals(jobName));
    }

    private static SimpleTextAttributes getAttribute(@NotNull Job job) {
        Build build = job.getLastBuild();
        if (build != null && (job.isInQueue() || build.isBuilding())) {
            return REGULAR_BOLD_ATTRIBUTES;
        }

        return REGULAR_ATTRIBUTES;
    }

    private static String buildLabel(Build build) {
        String status = getStatus(build);
        return String.format("#%d (%s) duration: %s %s", build.getNumber(), DateFormatUtil.formatDateTime(build.getTimestamp()), DurationFormatUtils.formatDurationHMS(build.getDuration()), status);
    }


    private static String buildLabel(@NotNull Job job) {
        Build build = job.getLastBuild();
        if (build == null) {
            return job.getName();
        }
        return String.format("%s #%s%s", job.getName(), build.getNumber(), getStatus(job, build));
    }

    @NotNull
    private static String getStatus(@NotNull Job job, Build build) {
        if (job.isInQueue()) {
            return " (in queue)";
        }
        return getStatus(build);
    }

    @NotNull
    private static String getStatus(@NotNull Build build) {
        if (build.isBuilding()) {
            return " (running)";
        }
        return "";
    }

    @NotNull
    private static String buildLabel(@NotNull Jenkins jenkins) {
        return "Jenkins " + jenkins.getName();
    }

    private static class CompositeIcon extends RowIcon {

        CompositeIcon(@NotNull Icon... icons) {
            super(icons.length);
            for (int i = 0; i < icons.length; i++) {
                setIcon(icons[i], i);
            }
        }
    }
}
