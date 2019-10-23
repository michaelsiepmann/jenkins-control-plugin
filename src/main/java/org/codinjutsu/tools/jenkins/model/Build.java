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
import org.codinjutsu.tools.jenkins.util.GuiUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.codinjutsu.tools.jenkins.logic.JenkinsParser.BUILD_DURATION;
import static org.codinjutsu.tools.jenkins.logic.JenkinsParser.BUILD_IS_BUILDING;
import static org.codinjutsu.tools.jenkins.logic.JenkinsParser.BUILD_NUMBER;
import static org.codinjutsu.tools.jenkins.logic.JenkinsParser.BUILD_RESULT;
import static org.codinjutsu.tools.jenkins.logic.JenkinsParser.BUILD_TIMESTAMP;
import static org.codinjutsu.tools.jenkins.logic.JenkinsParser.BUILD_URL;

public class Build {

    public static final Map<BuildStatusEnum, Icon> ICON_BY_BUILD_STATUS_MAP = new HashMap<>();

    private String url;
    private int number;
    private boolean building;
    private String message;
    private Date timestamp;
    private Long duration;

    private BuildStatusEnum status;
    private Job job;

    static {
        ICON_BY_BUILD_STATUS_MAP.put(BuildStatusEnum.SUCCESS, GuiUtil.loadIcon("blue.png"));
        ICON_BY_BUILD_STATUS_MAP.put(BuildStatusEnum.STABLE, GuiUtil.loadIcon("blue.png"));
        ICON_BY_BUILD_STATUS_MAP.put(BuildStatusEnum.FAILURE, GuiUtil.loadIcon("red.png"));
        ICON_BY_BUILD_STATUS_MAP.put(BuildStatusEnum.UNSTABLE, GuiUtil.loadIcon("yellow.png"));
        ICON_BY_BUILD_STATUS_MAP.put(BuildStatusEnum.NULL, GuiUtil.loadIcon("grey.png"));
        ICON_BY_BUILD_STATUS_MAP.put(BuildStatusEnum.ABORTED, GuiUtil.loadIcon("grey.png"));
        ICON_BY_BUILD_STATUS_MAP.put(BuildStatusEnum.FOLDER, GuiUtil.loadIcon("folder.png"));
    }


    public static Build createBuildFromWorkspace(String buildUrl, String number, String status, String isBuilding, Long timestamp, Long duration) {
        return createBuild(buildUrl, Long.parseLong(number), status, Boolean.parseBoolean(isBuilding), null, timestamp, duration);
    }

    public static Build createBuildFromRss(String buildUrl, String number, String status, String isBuilding, String message) {
        return createBuild(buildUrl, Long.parseLong(number), status, Boolean.parseBoolean(isBuilding), message, 0L, 0L);
    }

    private static Build createBuild(String buildUrl, Long number, String status, Boolean isBuilding, String message, Long timestamp, Long duration) {
        BuildStatusEnum buildStatusEnum = BuildStatusEnum.parseStatus(status);
        return new Build(buildUrl, number.intValue(), buildStatusEnum, isBuilding, message, timestamp, duration);
    }

    public Build() {
    }

    @JsonCreator
    public Build(
            @JsonProperty(BUILD_URL)
                    String url,
            @JsonProperty(BUILD_NUMBER)
                    int number,
            @JsonProperty(BUILD_RESULT)
                    String status,
            @JsonProperty(BUILD_IS_BUILDING)
                    boolean isBuilding,
            @JsonProperty(BUILD_TIMESTAMP)
                    Long timestamp,
            @JsonProperty(BUILD_DURATION)
                    Long duration) {
        this(url, number, BuildStatusEnum.parseStatus(status), isBuilding, null, timestamp, duration);
    }

    private Build(String url, int number, BuildStatusEnum status, boolean isBuilding, String message, Long timestamp, Long duration) {
        this.url = url;
        this.number = number;
        this.status = status;
        this.building = isBuilding;
        this.message = message;
        setTimestamp(timestamp);
        this.duration = duration;
    }


    public static Icon getStateIcon(String jobColor) {
        if (jobColor == null) {
            // NB: This assumes the case of rendering a folder.
            // TODO: handle the folder-case explicitly
            return ICON_BY_BUILD_STATUS_MAP.get(BuildStatusEnum.FOLDER);
        }
        BuildStatusEnum[] jobStates = BuildStatusEnum.values();
        for (BuildStatusEnum jobState : jobStates) {
            String stateName = jobState.getColor();
            if (jobColor.startsWith(stateName)) {
                return ICON_BY_BUILD_STATUS_MAP.get(jobState);
            }
        }

        return ICON_BY_BUILD_STATUS_MAP.get(BuildStatusEnum.NULL);
    }

    public Icon getStateIcon() {
        return ICON_BY_BUILD_STATUS_MAP.get(status);
    }


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public BuildStatusEnum getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = BuildStatusEnum.parseStatus(status);
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = new Date(timestamp);
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public boolean isBuilding() {
        return building;
    }

    public void setBuilding(boolean building) {
        this.building = building;
    }

    public boolean isAfter(@NotNull Build aBuild) {
        return this.getNumber() > aBuild.getNumber();
    }

    public String getMessage() {
        return message;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    @NotNull
    public String getStatusText() {
        return building ? " (running)" : "";
    }
}
