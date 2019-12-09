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

package org.codinjutsu.tools.jenkins.logic;

import org.codinjutsu.tools.jenkins.model.Build;
import org.codinjutsu.tools.jenkins.model.Jenkins;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.model.ViewElement;
import org.codinjutsu.tools.jenkins.model.TestResult;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface JenkinsParser {
    String JOBS = "jobs";
    String JOB_NAME = "name";
    String JOB_DISPLAY_NAME = "displayName";
    String JOB_FULLDISPLAY_NAME = "fullDisplayName";
    String JOB_FULL_NAME = "fullName";
    String JOB_HEALTH = "healthReport";
    String JOB_HEALTH_ICON = "iconUrl";
    String JOB_HEALTH_DESCRIPTION = "description";
    String JOB_URL = "url";
    String JOB_COLOR = "color";
    String JOB_LAST_BUILD = "lastBuild";
    String JOB_IS_BUILDABLE = "buildable";
    String JOB_IS_IN_QUEUE = "inQueue";
    String VIEWS = "views";
    String PRIMARY_VIEW = "primaryView";
    String VIEW_NAME = "name";
    String VIEW_URL = "url";
    String BUILDS = "builds";
    String BUILD_IS_BUILDING = "building";
    String BUILD_ID = "id";
    String BUILD_RESULT = "result";
    String BUILD_URL = "url";
    String BUILD_NUMBER = "number";
    String BUILD_TIMESTAMP = "timestamp";
    String BUILD_DURATION = "duration";
    String PARAMETER_PROPERTY = "property";
    String PARAMETER_DEFINITIONS = "parameterDefinitions";
    String PARAMETER_NAME = "name";
    String PARAMETER_DESCRIPTION = "description";
    String PARAMETER_TYPE = "type";
    String PARAMETER_DEFAULT_PARAM = "defaultParameterValue";
    String PARAMETER_DEFAULT_PARAM_VALUE = "value";
    String PARAMETER_CHOICE = "choices";

    Jenkins createWorkspace(String jsonData, String serverUrl);

    Job createJob(String jsonData);

    Build createBuild(Job job, String jsonData);

    Collection<Build> createBuilds(Job job, String jsonData);

    @NotNull
    Collection<ViewElement> createViewJobs(String jsonData);

    Collection<ViewElement> createCloudbeesViewJobs(String jsonData);

    TestResult createTestResult(String jsonData);
}
