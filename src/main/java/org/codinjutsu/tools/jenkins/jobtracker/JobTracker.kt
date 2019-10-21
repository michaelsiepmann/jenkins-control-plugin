package org.codinjutsu.tools.jenkins.jobtracker

import org.codinjutsu.tools.jenkins.model.Build
import java.util.ArrayList

object JobTracker {

    private val buildJobs = ArrayList<TraceableBuildJob>()

    fun registerJob(buildJob: TraceableBuildJob) {
        if (buildJobs.contains(buildJob)) {
            buildJobs.removeIf { buildJob == it }
        }
        buildJobs.add(buildJob)
    }

    fun onNewFinishedBuilds(finishedBuilds: Map<String, Build>) {
        notifyJobsAboutNewFinishedBuilds(finishedBuilds.values)
        removeDoneJobs()
    }

    private fun removeDoneJobs() {
        buildJobs.filter { it.isDone }
                .forEach { buildJobs.remove(it) }
    }

    private fun notifyJobsAboutNewFinishedBuilds(finishedBuilds: Collection<Build>) {
        buildJobs.forEach { traceableBuildJob ->
            finishedBuilds.forEach {
                traceableBuildJob.someBuildFinished(it)
            }
        }
    }
}