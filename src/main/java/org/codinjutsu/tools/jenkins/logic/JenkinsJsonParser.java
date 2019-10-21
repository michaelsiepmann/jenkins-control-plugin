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
import org.codinjutsu.tools.jenkins.logic.json.ParsedBuild;
import org.codinjutsu.tools.jenkins.logic.json.ParsedBuilds;
import org.codinjutsu.tools.jenkins.logic.json.ParsedJob;
import org.codinjutsu.tools.jenkins.logic.json.ParsedJobs;
import org.codinjutsu.tools.jenkins.logic.json.ParsedView;
import org.codinjutsu.tools.jenkins.logic.json.ParsedViews;
import org.codinjutsu.tools.jenkins.logic.json.ParsedWorkspace;
import org.codinjutsu.tools.jenkins.model.Build;
import org.codinjutsu.tools.jenkins.model.Health;
import org.codinjutsu.tools.jenkins.model.Jenkins;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.model.View;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

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

    private ObjectMapper createObjectMapper() {
        return new ObjectMapper().configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private List<View> getViews(Iterable<ParsedView> viewsObjects) {
        List<View> views = new LinkedList<>();
        for (ParsedView obj : viewsObjects) {
            views.add(getView(obj));
        }
        return views;
    }

    private View getView(ParsedView viewObject) {
        View view = new View();
        view.setNested(false);
        view.setName(viewObject.getName());
        view.setUrl(viewObject.getUrl());
        Collection<ParsedView> subViewObjs = viewObject.getSubViews();
        if (subViewObjs != null && !subViewObjs.isEmpty()) {
            for (ParsedView obj : subViewObjs) {
                View nestedView = new View();
                nestedView.setNested(true);
                nestedView.setName(obj.getName());
                nestedView.setUrl(obj.getUrl());
                view.addSubView(nestedView);
            }
        }
        return view;
    }

    @Override
    public Job createJob(String jsonData) {
        checkJsonDataAndThrowExceptionIfNecessary(jsonData);

        try {
            ParsedJob job = createObjectMapper().readValue(jsonData, ParsedJob.class);
            return getJob(job);
        } catch (IOException e) {
            String message = String.format("Error during parsing JSON data : %s", jsonData);
            LOG.error(message, e);
            throw new RuntimeException(e);
        }

    }

    public Build createBuild(String jsonData) {
        checkJsonDataAndThrowExceptionIfNecessary(jsonData);
        try {
            ParsedBuild build = createObjectMapper().readValue(jsonData, ParsedBuild.class);
            return getBuild(build);

        } catch (IOException e) {
            String message = String.format("Error during parsing JSON data : %s", jsonData);
            LOG.error(message, e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Build> createBuilds(String jsonData) {
        checkJsonDataAndThrowExceptionIfNecessary(jsonData);
        try {
            ParsedBuilds builds = createObjectMapper().readValue(jsonData, ParsedBuilds.class);
            return getBuilds(builds.getBuilds());
        } catch (IOException e) {
            String message = String.format("Error during parsing JSON data : %s", jsonData);
            LOG.error(message, e);
            throw new RuntimeException(e);
        }
    }

    @Nullable
    private Build getBuild(ParsedBuild lastBuildObject) {
        if (lastBuildObject == null) {
            return null;
        }

        Build build = new Build();
        build.setBuildDate(lastBuildObject.getId());
        build.setBuilding(lastBuildObject.getBuilding());
        build.setNumber(lastBuildObject.getNumber());
        build.setStatus(lastBuildObject.getStatus());
        build.setUrl(lastBuildObject.getUrl());
        Long timestamp = lastBuildObject.getTimestamp();
        if (null != timestamp) {
            build.setTimestamp(timestamp);
        }
        Long duration = lastBuildObject.getDuration();
        if (null != duration) {
            build.setDuration(duration);
        }

        return build;
    }

    private List<Build> getBuilds(Collection<ParsedBuild> buildsObjects) {
        return buildsObjects.stream().map(this::getBuild).collect(Collectors.toCollection(LinkedList::new));
    }

    private Job getJob(ParsedJob parsedJob) {
        Job job = new Job();
        job.setName(parsedJob.getName());
        job.setDisplayName(parsedJob.getDisplayName());
        job.setFullDisplayName(parsedJob.getFullDisplayName());
        job.setUrl(parsedJob.getUrl());
        job.setColor(parsedJob.getColor());
        job.setHealth(getHealth(parsedJob.getHealths()));
        job.setBuildable(parsedJob.getBuildable());
        job.setInQueue(parsedJob.getInQueue());
        job.setLastBuild(getBuild(parsedJob.getLastBuild()));
        job.addParameters(parsedJob.getParameters());
        return job;
    }

    @Contract("null -> null")
    @Nullable
    private Health getHealth(Collection<Health> healths) {
        if (healths == null || healths.isEmpty()) {
            return null;
        }
        Health health = (Health) CollectionUtils.get(healths, 0);
        if (isNotEmpty(health.getLevel())) {
            return health;
        }
        return null;
    }

    @Override
    public List<Job> createViewJobs(String jsonData) {
        checkJsonDataAndThrowExceptionIfNecessary(jsonData);

        try {
            ParsedJobs parsedJobs = createObjectMapper().readValue(jsonData, ParsedJobs.class);
            return parsedJobs.getJobs().stream().map(this::getJob).collect(Collectors.toCollection(LinkedList::new));
        } catch (IOException e) {
            String message = String.format("Error during parsing JSON data : %s", jsonData);
            LOG.error(message, e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Job> createCloudbeesViewJobs(String jsonData) {
        checkJsonDataAndThrowExceptionIfNecessary(jsonData);

        try {
            ParsedViews parsedViews = createObjectMapper().readValue(jsonData, ParsedViews.class);
            Collection<ParsedView> viewObjs = parsedViews.getViews();
            if (viewObjs == null || viewObjs.isEmpty()) {
                return emptyList();
            }

            ParsedView view = (ParsedView) CollectionUtils.get(viewObjs, 0);
            Collection<ParsedJob> jobs = view.getJobs();
            if (jobs == null) {
                return emptyList();
            }
            return jobs.stream().map(this::getJob).collect(Collectors.toCollection(LinkedList::new));
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
