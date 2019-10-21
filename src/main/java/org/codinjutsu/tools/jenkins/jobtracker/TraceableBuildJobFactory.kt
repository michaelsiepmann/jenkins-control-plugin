package org.codinjutsu.tools.jenkins.jobtracker

import org.codinjutsu.tools.jenkins.JenkinsAppSettings
import org.codinjutsu.tools.jenkins.logic.RequestManager
import org.codinjutsu.tools.jenkins.model.Job
import java.text.MessageFormat

object TraceableBuildJobFactory {
    private const val RETRY_LIMIT = 10

    fun newBuildJob(job: Job, configuration: JenkinsAppSettings, paramValueMap: Map<String, String>,
                    requestManager: RequestManager): TraceableBuildJob {
        val numBuildRetries = configuration.numBuildRetries
        ensureRetryLimit(numBuildRetries)
        return TraceableBuildJob(job, numBuildRetries) {
            requestManager.runParameterizedBuild(job, configuration, paramValueMap)
        }
    }

    private fun ensureRetryLimit(numBuildRetries: Int) {
        require(numBuildRetries <= RETRY_LIMIT) { MessageFormat.format("can't retry more than {0} times", RETRY_LIMIT) }
    }
}