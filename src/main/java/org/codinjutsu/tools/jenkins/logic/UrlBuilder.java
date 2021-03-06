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
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import org.codinjutsu.tools.jenkins.JenkinsAppSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.appendIfMissing;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.join;
import static org.codinjutsu.tools.jenkins.logic.JenkinsParser.BUILD_DURATION;
import static org.codinjutsu.tools.jenkins.logic.JenkinsParser.BUILD_ID;
import static org.codinjutsu.tools.jenkins.logic.JenkinsParser.BUILD_IS_BUILDING;
import static org.codinjutsu.tools.jenkins.logic.JenkinsParser.BUILD_NUMBER;
import static org.codinjutsu.tools.jenkins.logic.JenkinsParser.BUILD_RESULT;
import static org.codinjutsu.tools.jenkins.logic.JenkinsParser.BUILD_TIMESTAMP;
import static org.codinjutsu.tools.jenkins.logic.JenkinsParser.BUILD_URL;

public class UrlBuilder {

    private static final String API_JSON = "/api/json";
    private static final String BUILD = "/build";
    private static final String PARAMETERIZED_BUILD = "/buildWithParameters";
    private static final String RSS_LATEST = "/rssLatest";
    private static final String TREE_PARAM = "?tree=";
    private static final String BASIC_BUILD_INFO = join(
            new String[]{BUILD_ID, BUILD_URL, BUILD_IS_BUILDING, BUILD_RESULT, BUILD_NUMBER, BUILD_TIMESTAMP, BUILD_DURATION},
            ",");
    private static final String BASIC_JENKINS_INFO = "nodeName,nodeDescription,primaryView[name,url],views[name,url,views[name,url]]";
    private static final String BASIC_JOB_INFO = "name,displayName,fullDisplayName,fullName,url,color,buildable,inQueue,healthReport[description,iconUrl],lastBuild[" + BASIC_BUILD_INFO + "],property[parameterDefinitions[name,type,defaultParameterValue[value],description,choices]]";
    private static final String BASIC_VIEW_INFO = "name,url,jobs[" + BASIC_JOB_INFO + "]";
    private static final String CLOUDBEES_VIEW_INFO = "name,url,views[jobs[" + BASIC_JOB_INFO + "]]";
    private static final String TEST_CONNECTION_REQUEST = "?tree=nodeName";
    private static final String BASIC_BUILDS_INFO = "builds[" + BASIC_BUILD_INFO + "]";

    public static UrlBuilder getInstance(Project project) {
        return ServiceManager.getService(project, UrlBuilder.class);
    }


    public URL createRunJobUrl(String jobBuildUrl, JenkinsAppSettings configuration) {
        try {
            String s = jobBuildUrl + URIUtil.encodePathQuery(String.format("%s?delay=%dsec", BUILD, configuration.getBuildDelay()));
            return new URL(s);
        } catch (Exception ex) {
            handleException(ex);
        }
        return null;
    }

    public URL createStopBuildUrl(String buildUrl) {
        try {//http://jenkins.internal/job/it4em-it4em-DPD-GEOR-UAT-RO/27/stop
            return new URL(buildUrl + URIUtil.encodePath("stop"));
        } catch (Exception ex) {
            handleException(ex);
        }
        return null;
    }

    public URL createRunParameterizedJobUrl(String jobUrl, JenkinsAppSettings configuration, Map<String, String> paramValueMap) {
        StringBuilder strBuilder = new StringBuilder(String.format("%s?delay=%dsec", PARAMETERIZED_BUILD, configuration.getBuildDelay()));
        for (Map.Entry<String, String> valueByName : paramValueMap.entrySet()) {
            strBuilder.append("&").append(valueByName.getKey()).append("=").append(valueByName.getValue());
        }
        try {
            return new URL(jobUrl + URIUtil.encodePathQuery(strBuilder.toString()));
        } catch (Exception ex) {
            handleException(ex);
        }
        return null;
    }

    public URL createJenkinsWorkspaceUrl(JenkinsAppSettings configuration) {
        try {
            return new URL(URIUtil.encodePathQuery(configuration.getServerUrl() + API_JSON + TREE_PARAM + BASIC_JENKINS_INFO));
        } catch (Exception ex) {
            handleException(ex);
        }
        return null;
    }

    public URL createViewUrl(JenkinsAppSettings jenkinsAppSettings, JenkinsPlateform jenkinsPlateform, String viewUrl) {
        String basicViewInfo = BASIC_VIEW_INFO;
        if (JenkinsPlateform.CLOUDBEES.equals(jenkinsPlateform)) {
            basicViewInfo = CLOUDBEES_VIEW_INFO;
        }
        try {
            return new URL(updateURL(jenkinsAppSettings, viewUrl) + URIUtil.encodePathQuery(API_JSON + TREE_PARAM + basicViewInfo));
        } catch (Exception ex) {
            handleException(ex);
        }

        return null;
    }

    @NotNull
    private String updateURL(@NotNull JenkinsAppSettings jenkinsAppSettings, @NotNull String url) {
        if (isNotEmpty(jenkinsAppSettings.getFixedURL()) &&
                url.equals(appendIfMissing(jenkinsAppSettings.getServerUrl(), "/"))) {
            return appendIfMissing(jenkinsAppSettings.getFixedURL(), "/");
        }
        return url;
    }

    public URL createJobUrl(String jobUrl) {
        try {
            return new URL(jobUrl + URIUtil.encodePathQuery(API_JSON + TREE_PARAM + BASIC_JOB_INFO));
        } catch (Exception ex) {
            handleException(ex);
        }
        return null;
    }

    public URL createBuildUrl(String buildUrl) {
        try {
            return new URL(buildUrl + URIUtil.encodePathQuery(API_JSON + TREE_PARAM + BASIC_BUILD_INFO));
        } catch (Exception ex) {
            handleException(ex);
        }
        return null;
    }

    public URL createBuildsUrl(String buildUrl) {
        try {
            return new URL(buildUrl + URIUtil.encodePathQuery(API_JSON + TREE_PARAM + BASIC_BUILDS_INFO));
        } catch (Exception ex) {
            handleException(ex);
        }
        return null;
    }

    public URL createRssLatestUrl(String serverUrl) {
        try {
            return new URL(serverUrl + RSS_LATEST);
        } catch (Exception ex) {
            handleException(ex);
        }
        return null;
    }

    public URL createAuthenticationUrl(String serverUrl) {
        try {
            return new URL(serverUrl + API_JSON + TEST_CONNECTION_REQUEST);
        } catch (Exception ex) {
            handleException(ex);
        }
        return null;
    }

    @Nullable
    public URL createCrumbDataUrl(String serverUrl) {
        try {
            return new URL(serverUrl + "/crumbIssuer/api/xml?tree=crumb");
        } catch (MalformedURLException e) {
            handleException(e);
        }
        return null;
    }

    private void handleException(Exception ex) {
        if (ex instanceof MalformedURLException) {
            throw new IllegalArgumentException("URL is malformed", ex);
        } else if (ex instanceof URIException) {
            throw new IllegalArgumentException("Error during URL creation", ex);
        }
    }
}
