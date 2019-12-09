package org.codinjutsu.tools.jenkins.jobtracker

import com.google.common.base.Objects
import org.codinjutsu.tools.jenkins.model.Build
import org.codinjutsu.tools.jenkins.model.BuildStatusEnum
import org.codinjutsu.tools.jenkins.model.Job

class TraceableBuildJob internal constructor(private val job: Job, numTries: Int, private val runBuild: () -> Unit) {
    private var numBuildTriesLeft: Int = numTries - 1
    private var passedAnyBuild: Boolean = false

    val isDone: Boolean
        get() = !shouldStillTryBuilding()

    fun someBuildFinished(build: Build) {
        if (buildBelongsToThisJob(build)) {
            updatePassedAnyBuildStatus(build)
            if (shouldStillTryBuilding()) {
                runBuild()
                numBuildTriesLeft--
            }
        }
    }

    private fun updatePassedAnyBuildStatus(build: Build) {
        passedAnyBuild = passedAnyBuild || build.status == BuildStatusEnum.SUCCESS
    }

    private fun shouldStillTryBuilding(): Boolean {
        return numBuildTriesLeft > 0 && !passedAnyBuild
    }

    private fun buildBelongsToThisJob(build: Build): Boolean {
        return build.url
                .contains(job.url ?: "")
    }

    override fun equals(other: Any?): Boolean {
        return other != null && javaClass == other.javaClass && (other as TraceableBuildJob).job.url == job.url
    }

    override fun hashCode(): Int {
        return Objects.hashCode(job.name, job.url)
    }
}