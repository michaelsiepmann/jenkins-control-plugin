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

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.codinjutsu.tools.jenkins.JenkinsAppSettings;
import org.codinjutsu.tools.jenkins.JenkinsSettings;
import org.codinjutsu.tools.jenkins.exception.ConfigurationException;
import org.codinjutsu.tools.jenkins.model.Build;
import org.codinjutsu.tools.jenkins.model.Jenkins;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.model.TestResult;
import org.codinjutsu.tools.jenkins.model.View;
import org.codinjutsu.tools.jenkins.model.ViewElement;
import org.codinjutsu.tools.jenkins.security.JenkinsVersion;
import org.codinjutsu.tools.jenkins.security.SecurityClient;
import org.codinjutsu.tools.jenkins.security.SecurityClientFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

import javax.swing.SwingUtilities;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;

public class RequestManager {

    private static final Logger logger = Logger.getLogger(RequestManager.class);

    private static final String BUILDHIVE_CLOUDBEES = "buildhive";

    private final UrlBuilder urlBuilder;
    private SecurityClient securityClient;
    private JenkinsPlateform jenkinsPlateform = JenkinsPlateform.CLASSIC;
    private final RssParser rssParser = new RssParser();
    private final JenkinsParser jsonParser = new JenkinsJsonParser();

    public static RequestManager getInstance(Project project) {
        return ServiceManager.getService(project, RequestManager.class);
    }

    public RequestManager(Project project) {
        this.urlBuilder = UrlBuilder.getInstance(project);
    }

    public Jenkins loadJenkinsWorkspace(JenkinsAppSettings configuration) {
        if (handleNotYetLoggedInState()) {
            return null;
        }
        URL url = urlBuilder.createJenkinsWorkspaceUrl(configuration);
        String jenkinsWorkspaceData = securityClient.execute(url);

        if (configuration.getServerUrl().contains(BUILDHIVE_CLOUDBEES)) {//TODO hack need to refactor
            jenkinsPlateform = JenkinsPlateform.CLOUDBEES;
        } else {
            jenkinsPlateform = JenkinsPlateform.CLASSIC;
        }

        Jenkins jenkins = jsonParser.createWorkspace(jenkinsWorkspaceData, configuration.getServerUrl());

        int jenkinsPort = url.getPort();
        View primaryView = jenkins.getPrimaryView();
        assert primaryView != null;
        URL viewUrl = urlBuilder.createViewUrl(configuration, jenkinsPlateform, primaryView.getUrl());
        int viewPort = viewUrl.getPort();

        if (isJenkinsPortSet(jenkinsPort) && jenkinsPort != viewPort) {
            throw new ConfigurationException(String.format("Jenkins Server Port Mismatch: expected='%s' - actual='%s'. Look at the value of 'Jenkins URL' at %s/configure", jenkinsPort, viewPort, configuration.getServerUrl()));
        }

        if (!StringUtils.equals(url.getHost(), viewUrl.getHost())) {
            throw new ConfigurationException(String.format("Jenkins Server Host Mismatch: expected='%s' - actual='%s'. Look at the value of 'Jenkins URL' at %s/configure", url.getHost(), viewUrl.getHost(), configuration.getServerUrl()));
        }

        return jenkins;
    }

    private boolean isJenkinsPortSet(int jenkinsPort) {
        return jenkinsPort != -1;
    }

    public Map<String, Build> loadJenkinsRssLatestBuilds(JenkinsAppSettings configuration) {
        if (handleNotYetLoggedInState()) {
            return Collections.emptyMap();
        }
        URL url = urlBuilder.createRssLatestUrl(configuration.getServerUrl());

        String rssData = securityClient.execute(url);

        return rssParser.loadJenkinsRssLatestBuilds(rssData);
    }

    @NotNull
    public Collection<ViewElement> loadJenkinsView(JenkinsAppSettings jenkinsAppSettings, String viewUrl) {
        if (handleNotYetLoggedInState()) {
            return emptyList();
        }
        URL url = urlBuilder.createViewUrl(jenkinsAppSettings, jenkinsPlateform, viewUrl);
        String jenkinsViewData = securityClient.execute(url);
        if (jenkinsPlateform.equals(JenkinsPlateform.CLASSIC)) {
            return jsonParser.createViewJobs(jenkinsViewData);
        }
        return jsonParser.createCloudbeesViewJobs(jenkinsViewData);
    }

    private boolean handleNotYetLoggedInState() {
        boolean threadStack = false;
        boolean result = false;
        if (SwingUtilities.isEventDispatchThread()) {
            logger.warn("RequestManager.handleNotYetLoggedInState called from EDT");
            threadStack = true;
        }
        if (securityClient == null) {
            logger.warn("Not yet logged in, all calls until login will fail");
            threadStack = true;
            result = true;
        }
        if (threadStack) {
            Thread.dumpStack();
        }
        return result;
    }

    @Nullable
    private Job loadJob(String jenkinsJobUrl) {
        if (handleNotYetLoggedInState()) {
            return null;
        }
        URL url = urlBuilder.createJobUrl(jenkinsJobUrl);
        String jenkinsJobData = securityClient.execute(url);
        return jsonParser.createJob(jenkinsJobData);
    }

    private void stopBuild(String jenkinsBuildUrl) {
        if (handleNotYetLoggedInState()) {
            return;
        }
        URL url = urlBuilder.createStopBuildUrl(jenkinsBuildUrl);
        securityClient.execute(url);
    }

    @Nullable
    private Build loadBuild(Job job, String jenkinsBuildUrl) {
        if (handleNotYetLoggedInState()) {
            return null;
        }
        URL url = urlBuilder.createBuildUrl(jenkinsBuildUrl);
        String jenkinsJobData = securityClient.execute(url);
        return jsonParser.createBuild(job, jenkinsJobData);
    }

    @Nullable
    private Collection<Build> loadBuilds(Job job, String jenkinsBuildUrl) {
        if (handleNotYetLoggedInState()) {
            return null;
        }
        URL url = urlBuilder.createBuildsUrl(jenkinsBuildUrl);
        String jenkinsJobData = securityClient.execute(url);
        return jsonParser.createBuilds(job, jenkinsJobData);
    }

    public void runBuild(ViewElement job, JenkinsAppSettings configuration, Map<String, VirtualFile> files) {
        if (handleNotYetLoggedInState()) {
            return;
        }
        if (job.hasParameters() && files.size() > 0) {
            for (String key : files.keySet()) {
                if (!job.hasParameter(key)) {
                    files.remove(files.get(key));
                }
            }
            securityClient.setFiles(files);
        }
        runBuild(job, configuration);
    }

    public void runBuild(ViewElement job, JenkinsAppSettings configuration) {
        if (handleNotYetLoggedInState()) {
            return;
        }
        URL url = urlBuilder.createRunJobUrl(job.getUrl(), configuration);
        securityClient.execute(url);
    }

    public void runParameterizedBuild(Job job, JenkinsAppSettings configuration, Map<String, String> paramValueMap) {
        if (handleNotYetLoggedInState()) {
            return;
        }
        URL url = urlBuilder.createRunParameterizedJobUrl(job.getUrl(), configuration, paramValueMap);
        securityClient.execute(url);
    }

    public void authenticate(JenkinsAppSettings jenkinsAppSettings, @NotNull JenkinsSettings jenkinsSettings) {
        if (jenkinsSettings.isSecurityMode()) {
            securityClient = SecurityClientFactory.basic(jenkinsSettings.getUsername(), jenkinsSettings.getPassword(), jenkinsSettings.getCrumbData(), jenkinsSettings.getVersion());
        } else {
            securityClient = SecurityClientFactory.none(jenkinsSettings.getCrumbData(), jenkinsSettings.getVersion());
        }
        securityClient.connect(urlBuilder.createAuthenticationUrl(jenkinsAppSettings.getServerUrl()));

    }

    public void authenticate(String serverUrl, String username, String password, String crumbData, JenkinsVersion version) {
        if (StringUtils.isNotBlank(username)) {
            securityClient = SecurityClientFactory.basic(username, password, crumbData, version);
        } else {
            securityClient = SecurityClientFactory.none(crumbData, version);
        }
        securityClient.connect(urlBuilder.createAuthenticationUrl(serverUrl));
    }

    public List<ViewElement> loadFavoriteJobs(List<JenkinsSettings.FavoriteJob> favoriteJobs) {
        if (handleNotYetLoggedInState()) {
            return emptyList();
        }
        List<ViewElement> jobs = new LinkedList<>();
        for (JenkinsSettings.FavoriteJob favoriteJob : favoriteJobs) {
            jobs.add(loadJob(favoriteJob.url));
        }
        return jobs;
    }

    public void stopBuild(Build build) {
        stopBuild(build.getUrl());
    }

    public Job loadJob(ViewElement job) {
        return loadJob(job.getUrl());
    }

    public Collection<Build> loadBuilds(Job job) {
        return loadBuilds(job, job.getUrl());
    }

    public Build loadBuild(@NotNull Build build) {
        return loadBuild(build.getJob(), build.getUrl());
    }

    public String loadConsoleTextFor(@NotNull Job job) {
        Build lastBuild = job.getLastBuild();
        if (lastBuild == null) {
            return null;
        }
        String url = lastBuild.getUrl() + "/logText/progressiveText";
        return securityClient.execute(url);
    }

    public List<TestResult> loadTestResultsFor(Build lastBuild) {
        if (lastBuild == null) {
            return emptyList();
        }
        String url = lastBuild.getUrl() + "/testReport/api/json";
        String jsonData = securityClient.execute(url);
        TestResult testResult = jsonParser.createTestResult(jsonData);
        return Collections.singletonList(testResult);
    }

    @TestOnly
    void setSecurityClient(SecurityClient securityClient) {
        this.securityClient = securityClient;
    }
}
