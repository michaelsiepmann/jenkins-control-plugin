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

package org.codinjutsu.tools.jenkins.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.collections.CollectionUtils;
import org.codinjutsu.tools.jenkins.util.GuiUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.codinjutsu.tools.jenkins.logic.JenkinsParser.JOB_COLOR;
import static org.codinjutsu.tools.jenkins.logic.JenkinsParser.JOB_DISPLAY_NAME;
import static org.codinjutsu.tools.jenkins.logic.JenkinsParser.JOB_FULLDISPLAY_NAME;
import static org.codinjutsu.tools.jenkins.logic.JenkinsParser.JOB_HEALTH;
import static org.codinjutsu.tools.jenkins.logic.JenkinsParser.JOB_IS_BUILDABLE;
import static org.codinjutsu.tools.jenkins.logic.JenkinsParser.JOB_IS_IN_QUEUE;
import static org.codinjutsu.tools.jenkins.logic.JenkinsParser.JOB_LAST_BUILD;
import static org.codinjutsu.tools.jenkins.logic.JenkinsParser.JOB_NAME;
import static org.codinjutsu.tools.jenkins.logic.JenkinsParser.JOB_URL;
import static org.codinjutsu.tools.jenkins.logic.JenkinsParser.PARAMETER_PROPERTY;

public class Job {

    private static final Map<String, Icon> ICON_BY_JOB_HEALTH_MAP = new HashMap<>();
    private String name;

    private String displayName;
    private String fullDisplayName;
    private String url;

    private String color;
    private boolean inQueue;
    private boolean buildable;
    private boolean fetchBuild = false;

    private Health health;

    private Build lastBuild;

    private Collection<Build> lastBuilds = new LinkedList<>();

    private final List<JobParameterDefinition> parameters = new LinkedList<>();

    static {
        ICON_BY_JOB_HEALTH_MAP.put("health-00to19", GuiUtil.loadIcon("health-00to19.png"));
        ICON_BY_JOB_HEALTH_MAP.put("health-20to39", GuiUtil.loadIcon("health-20to39.png"));
        ICON_BY_JOB_HEALTH_MAP.put("health-40to59", GuiUtil.loadIcon("health-40to59.png"));
        ICON_BY_JOB_HEALTH_MAP.put("health-60to79", GuiUtil.loadIcon("health-60to79.png"));
        ICON_BY_JOB_HEALTH_MAP.put("health-80plus", GuiUtil.loadIcon("health-80plus.png"));
        ICON_BY_JOB_HEALTH_MAP.put("null", GuiUtil.loadIcon("null.png"));
    }

    @Contract("null -> null")
    @Nullable
    private static Health getHealth(Collection<Health> healths) {
        if (healths == null || healths.isEmpty()) {
            return null;
        }
        Health health = (Health) CollectionUtils.get(healths, 0);
        if (isNotEmpty(health.getLevel())) {
            return health;
        }
        return null;
    }

    private static Collection<JobParameterDefinition> getParameterDefinitions(Collection<JobParameter> parameters) {
        if (parameters == null) {
            return Collections.emptyList();
        }
        Collection<JobParameterDefinition> result = new LinkedList<>();
        for (JobParameter parameter : parameters) {
            Collection<JobParameterDefinition> definitions = parameter.getDefinitions();
            if (definitions != null) {
                result.addAll(definitions);
            }
        }
        return result;
    }

    public Job() {
    }

    @JsonCreator
    public Job(
            @JsonProperty(JOB_NAME)
                    String name,
            @JsonProperty(JOB_DISPLAY_NAME)
                    String displayName,
            @JsonProperty(JOB_FULLDISPLAY_NAME)
                    String fullDisplayName,
            @JsonProperty(JOB_URL)
                    String url,
            @JsonProperty(JOB_COLOR)
                    String color,
            @JsonProperty(JOB_IS_BUILDABLE)
                    Boolean buildable,
            @JsonProperty(JOB_IS_IN_QUEUE)
                    Boolean inQueue,
            @JsonProperty(JOB_LAST_BUILD)
                    Build lastBuild,
            @JsonProperty(JOB_HEALTH)
                    Collection<Health> healths,
            @JsonProperty(PARAMETER_PROPERTY)
                    Collection<JobParameter> parameters
    ) {
        this(name, displayName, color, url, inQueue, buildable);
        this.fullDisplayName = fullDisplayName;
        this.lastBuild = lastBuild;
        this.health = getHealth(healths);
        this.parameters.addAll(getParameterDefinitions(parameters));
    }

    private Job(String name, String displayName, String color, String url, Boolean inQueue, Boolean buildable) {
        this.name = name;
        this.displayName = displayName;
        this.color = color;
        this.url = url;
        this.inQueue = inQueue;
        this.buildable = buildable;
    }


    public static Job createJob(String jobName, String displayName, String jobColor, String jobUrl, String inQueue, String buildable) {
        return new Job(jobName, displayName, jobColor, jobUrl, Boolean.valueOf(inQueue), Boolean.valueOf(buildable));
    }


    public Icon getStateIcon() {
        return Build.getStateIcon(color);
    }

    public Icon getHealthIcon() {
        if (health == null) {
            return ICON_BY_JOB_HEALTH_MAP.get("null");
        }
        return ICON_BY_JOB_HEALTH_MAP.get(health.getLevel());
    }

    public String findHealthDescription() {
        if (health == null) {
            return "";
        }
        return health.getDescription();
    }


    public void updateContentWith(Job updatedJob) {
        this.color = updatedJob.getColor();
        this.health = updatedJob.getHealth();
        this.inQueue = updatedJob.isInQueue();
        this.lastBuild = updatedJob.getLastBuild();
        this.lastBuilds = updatedJob.getLastBuilds();
    }


    public void addParameter(String paramName, String paramType, String defaultValue, String... choices) {
        parameters.add(JobParameterDefinition.create(paramName, paramType, defaultValue, choices));
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        if (isNotEmpty(fullDisplayName)) {
            return fullDisplayName;
        }
        if (isNotEmpty(displayName)) {
            return displayName;
        }
        return name;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isInQueue() {
        return inQueue;
    }

    public void setInQueue(boolean inQueue) {
        this.inQueue = inQueue;
    }

    public boolean isBuildable() {
        return buildable;
    }

    public void setBuildable(boolean buildable) {
        this.buildable = buildable;
    }

    public Build getLastBuild() {
        return lastBuild;
    }

    public void setLastBuild(Build lastBuild) {
        this.lastBuild = lastBuild;
    }

    public Collection<Build> getLastBuilds() {
        return lastBuilds;
    }

    public void setLastBuilds(Collection<Build> builds) {
        lastBuilds = builds;
    }

    Health getHealth() {
        return health;
    }

    public void setHealth(Health health) {
        this.health = health;
    }

    public boolean hasParameters() {
        return !parameters.isEmpty();
    }

    public void setFetchBuild(boolean fetchBuild) {
        this.fetchBuild = fetchBuild;
    }

    public boolean isFetchBuild() {
        return fetchBuild;
    }


    public List<JobParameterDefinition> getParameters() {
        return parameters;
    }

    public boolean hasParameter(String name) {
        if (hasParameters()) {
            for (JobParameterDefinition parameter : parameters) {
                if (parameter.getName().equals(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "Job{" +
                "name='" + name + '\'' +
                '}';
    }
}
