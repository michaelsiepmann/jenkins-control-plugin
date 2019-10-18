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

package org.codinjutsu.tools.jenkins;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Tag;
import org.apache.commons.lang3.StringUtils;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.security.JenkinsVersion;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static org.codinjutsu.tools.jenkins.JenkinsComponent.JENKINS_CONTROL_PLUGIN_NAME;

@State(
        name = "Jenkins.Settings",
        storages = {
                @Storage("$PROJECT_FILE$"),
                @Storage("$PROJECT_CONFIG_DIR$/jenkinsSettings.xml")
        }
)
public class JenkinsSettings implements PersistentStateComponent<JenkinsSettings.State> {

    private State myState = new State();

    public static JenkinsSettings getSafeInstance(Project project) {
        JenkinsSettings settings = ServiceManager.getService(project, JenkinsSettings.class);
        return settings != null ? settings : new JenkinsSettings();
    }

    @Override
    public State getState() {
//        ensurePasswordIsStored();
        return myState;
    }

    @Override
    public void loadState(@NotNull State state) {
        XmlSerializerUtil.copyBean(state, myState);
    }

    public String getUsername() {
        return myState.username;
    }

    public void setUsername(String username) {
        myState.username = username;
    }

    public String getCrumbData() {
        return myState.crumbData;
    }

    public void setCrumbData(String crumbData) {
        myState.crumbData = crumbData;
    }

    public String getPassword() {
        CredentialAttributes attributes = createCredentialAttributes();
        Credentials credentials = PasswordSafe.getInstance().get(attributes);
        if (credentials == null) {
            return "";
        }
        String password = credentials.getPasswordAsString();
        return StringUtils.defaultIfEmpty(password, "");
    }

    @Contract(" -> new")
    @NotNull
    private CredentialAttributes createCredentialAttributes() {
        return new CredentialAttributes(JENKINS_CONTROL_PLUGIN_NAME, myState.username);
    }

    public void setPassword(String password) {
        PasswordSafe.getInstance().setPassword(createCredentialAttributes(), password);
    }

    public void addFavorite(@NotNull Collection<Job> jobs) {
        jobs.forEach(job -> myState.favoriteJobs.add(createFavoriteJob(job)));
    }

    @NotNull
    private FavoriteJob createFavoriteJob(Job job) {
        FavoriteJob favoriteJob = new FavoriteJob();
        favoriteJob.name = job.getName();
        favoriteJob.url = job.getUrl();
        return favoriteJob;
    }

    public boolean isAFavoriteJob(String jobName) {
        return myState.favoriteJobs.stream().anyMatch(favoriteJob -> StringUtils.equals(jobName, favoriteJob.name));
    }

    public void removeFavorite(Iterable<Job> selectedJobs) {//TODO need to refactor
        for (Job selectedJob : selectedJobs) {
            myState.favoriteJobs.removeIf(favoriteJob -> StringUtils.equals(selectedJob.getName(), favoriteJob.name));
        }
    }

    public List<FavoriteJob> getFavoriteJobs() {
        return myState.favoriteJobs;
    }

    public boolean isFavoriteViewEmpty() {
        return myState.favoriteJobs.isEmpty();
    }

    public void setLastSelectedView(String viewName) {
        myState.lastSelectedView = viewName;
    }

    public String getLastSelectedView() {
        return myState.lastSelectedView;
    }

    public boolean isSecurityMode() {
        return StringUtils.isNotBlank(getUsername());
    }

    public JenkinsVersion getVersion() {
        return this.myState.jenkinsVersion;
    }

    public void setVersion(JenkinsVersion jenkinsVersion) {
        this.myState.jenkinsVersion = jenkinsVersion;
    }

    @SuppressWarnings("WeakerAccess")
    public static class State {

        static final String RESET_STR_VALUE = "";

        public String username = RESET_STR_VALUE;
        public String crumbData = RESET_STR_VALUE;
        public String lastSelectedView;
        public List<FavoriteJob> favoriteJobs = new LinkedList<>();
        public JenkinsVersion jenkinsVersion = JenkinsVersion.VERSION_1;
    }

    @Tag("favorite")
    public static class FavoriteJob {

        @Attribute("name")
        public String name;

        @Attribute("url")
        public String url;
    }
}
