package org.codinjutsu.tools.jenkins.model

import org.codinjutsu.tools.jenkins.model.Job.ICON_BY_JOB_HEALTH_MAP
import javax.swing.Icon

internal class Folder(name: String, displayName : String, fullDisplayName: String, fullname: String, url: String, private val health: Health) : ViewElement(name, displayName, fullDisplayName, fullname, url) {

    override var lastBuild: Build? = null

    override fun isInQueue() = false

    override fun getColor() = ""

    override fun getStateIcon(): Icon = Build.getStateIcon(null)

    override fun getHealthIcon(): Icon {
        return ICON_BY_JOB_HEALTH_MAP[health.level]!!
    }

    override fun findHealthDescription(): String? {
        return health.description
    }

    override fun isBuildable() = false

    override fun getLastBuilds(): Collection<Build> = emptyList()

    override fun isFetchBuild() = false

    override fun hasParameters() = false

    override fun hasParameter(name: String) = false

    override fun updateContentWith(updatedJob: Job) {
    }
}
