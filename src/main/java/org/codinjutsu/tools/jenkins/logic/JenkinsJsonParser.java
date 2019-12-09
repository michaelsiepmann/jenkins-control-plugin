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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.codinjutsu.tools.jenkins.logic.json.ParsedView;
import org.codinjutsu.tools.jenkins.logic.json.ParsedViews;
import org.codinjutsu.tools.jenkins.logic.json.ParsedWorkspace;
import org.codinjutsu.tools.jenkins.model.Build;
import org.codinjutsu.tools.jenkins.model.Builds;
import org.codinjutsu.tools.jenkins.model.Jenkins;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.model.ViewElement;
import org.codinjutsu.tools.jenkins.model.Jobs;
import org.codinjutsu.tools.jenkins.model.TestResult;
import org.codinjutsu.tools.jenkins.model.View;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static java.util.Collections.emptyList;

public class JenkinsJsonParser implements JenkinsParser {

    private static final Logger LOG = Logger.getLogger(JenkinsJsonParser.class);

    @Override
    public Jenkins createWorkspace(String jsonData, String serverUrl) {
        checkJsonDataAndThrowExceptionIfNecessary(jsonData);
        Jenkins jenkins = new Jenkins("", serverUrl);
        try {
            ParsedWorkspace workspace = createObjectMapper().readValue(jsonData, ParsedWorkspace.class);
            ParsedView primaryViewObject = workspace.getPrimaryView();
            if (primaryViewObject != null) {
                jenkins.setPrimaryView(getView(primaryViewObject));
            }

            Collection<ParsedView> views = workspace.getViews();
            if (views != null && !views.isEmpty()) {
                jenkins.setViews(getViews(views));
            }
        } catch (IOException e) {
            String message = String.format("Error during parsing JSON data : %s", jsonData);
            LOG.error(message, e);
            throw new RuntimeException(e);
        }
        return jenkins;
    }

    public static ObjectMapper createObjectMapper() {
        return new ObjectMapper().configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private Collection<View> getViews(Collection<ParsedView> viewsObjects) {
        return viewsObjects.stream()
                           .map(this::getView)
                           .collect(Collectors.toList());
    }

    private View getView(ParsedView parsedView) {
        View view = new View();
        view.setNested(false);
        view.setName(parsedView.getName());
        view.setUrl(parsedView.getUrl());
        Collection<ParsedView> subViews = parsedView.getSubViews();
        if (subViews != null && !subViews.isEmpty()) {
            for (ParsedView subView : subViews) {
                View nestedView = new View();
                nestedView.setNested(true);
                nestedView.setName(subView.getName());
                nestedView.setUrl(subView.getUrl());
                view.addSubView(nestedView);
            }
        }
        return view;
    }

    @Override
    public Job createJob(String jsonData) {
        checkJsonDataAndThrowExceptionIfNecessary(jsonData);

        try {
            Job job = createObjectMapper().readValue(jsonData, Job.class);
            if (job.getLastBuild() != null) {
                job.getLastBuild().setJob(job);
            }
            return job;
        } catch (IOException e) {
            String message = String.format("Error during parsing JSON data : %s", jsonData);
            LOG.error(message, e);
            throw new RuntimeException(e);
        }

    }

    @Override
    public Build createBuild(Job job, String jsonData) {
        checkJsonDataAndThrowExceptionIfNecessary(jsonData);
        try {
            Build build = createObjectMapper().readValue(jsonData, Build.class);
            build.setJob(job);
            return build;
        } catch (IOException e) {
            String message = String.format("Error during parsing JSON data : %s", jsonData);
            LOG.error(message, e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Collection<Build> createBuilds(Job job, String jsonData) {
        checkJsonDataAndThrowExceptionIfNecessary(jsonData);
        try {
            Collection<Build> builds = createObjectMapper().readValue(jsonData, Builds.class).getBuilds();
            builds.forEach(build -> build.setJob(job));
            return builds;
        } catch (IOException e) {
            String message = String.format("Error during parsing JSON data : %s", jsonData);
            LOG.error(message, e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public TestResult createTestResult(String jsonData) {
        checkJsonDataAndThrowExceptionIfNecessary(jsonData);
        try {
            return createObjectMapper().readValue(jsonData, TestResult.class);
        } catch (IOException e) {
            String message = String.format("Error during parsing JSON data : %s", jsonData);
            LOG.error(message, e);
            throw new RuntimeException(e);
        }
    }

    @Override
    @NotNull
    public Collection<ViewElement> createViewJobs(String jsonData) {
        checkJsonDataAndThrowExceptionIfNecessary(jsonData);

        try {
            Jobs jobs = createObjectMapper().readValue(jsonData, Jobs.class);
            return jobs.getViews();
        } catch (IOException e) {
            String message = String.format("Error during parsing JSON data : %s", jsonData);
            LOG.error(message, e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Collection<ViewElement> createCloudbeesViewJobs(String jsonData) {
        checkJsonDataAndThrowExceptionIfNecessary(jsonData);

        try {
            ParsedViews parsedViews = createObjectMapper().readValue(jsonData, ParsedViews.class);
            Collection<ParsedView> views = parsedViews.getViews();
            if (views == null || views.isEmpty()) {
                return emptyList();
            }

            ParsedView view = (ParsedView) CollectionUtils.get(views, 0);
            Collection<ViewElement> jobs = view.getViews();
            if (jobs == null) {
                return emptyList();
            }
            return jobs;
        } catch (IOException e) {
            String message = String.format("Error during parsing JSON data : %s", jsonData);
            LOG.error(message, e);
            throw new RuntimeException(e);
        }
    }

    private void checkJsonDataAndThrowExceptionIfNecessary(String jsonData) {
        if (StringUtils.isEmpty(jsonData) || "{}".equals(jsonData)) {
            String message = "Empty JSON data!";
            LOG.error(message);
            throw new IllegalStateException(message);
        }
    }
}
