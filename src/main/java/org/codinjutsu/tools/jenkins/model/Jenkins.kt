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

package org.codinjutsu.tools.jenkins.model

import org.apache.commons.lang.StringUtils
import org.codinjutsu.tools.jenkins.JenkinsAppSettings

import java.util.LinkedList

class Jenkins @JvmOverloads constructor(description: String? = null, serverUrl: String? = null) {

    var name: String? = null
        private set
    var serverUrl: String? = null
        private set

    private var jobs: MutableList<Job> = mutableListOf()

    private var views: MutableList<View> = mutableListOf()
    var primaryView: View? = null

    init {
        this.name = description
        this.serverUrl = serverUrl
        this.jobs = LinkedList()
        this.views = LinkedList()
    }


    fun setJobs(jobs: MutableList<Job>) {
        this.jobs = jobs
    }


    fun getJobs(): List<Job> {
        return jobs
    }


    fun getViews(): List<View> {
        return views
    }


    fun setViews(views: MutableList<View>) {
        this.views = views
    }

    fun getViewByName(lastSelectedViewName: String): View? {
        for (view in views) {
            if (StringUtils.equals(lastSelectedViewName, view.name)) {
                return view
            }
        }

        return null
    }

    fun update(jenkins: Jenkins) {
        this.name = jenkins.name
        this.serverUrl = jenkins.serverUrl
        this.jobs.clear()
        this.jobs.addAll(jenkins.getJobs())
        this.views.clear()
        this.views.addAll(jenkins.getViews())
        this.primaryView = jenkins.primaryView
    }

    companion object {

        fun byDefault(): Jenkins {
            return Jenkins("", JenkinsAppSettings.DUMMY_JENKINS_SERVER_URL)
        }
    }
}