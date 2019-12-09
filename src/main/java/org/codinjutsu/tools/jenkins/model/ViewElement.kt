package org.codinjutsu.tools.jenkins.model

import javax.swing.Icon

abstract class ViewElement(
        var name: String? = null,
        var displayName: String? = null,
        val fullDisplayName: String,
        val fullName: String,
        var url: String? = null
) {
    abstract var lastBuild: Build?
    abstract fun isInQueue(): Boolean

    fun getJobName(): String {
        if (fullDisplayName.isNotEmpty()) {
            return fullDisplayName
        }
        return if (displayName?.isNotEmpty() == true) {
            displayName!!
        } else {
            name ?: ""
        }
    }

    abstract fun getColor(): String
    abstract fun getStateIcon(): Icon
    abstract fun getHealthIcon(): Icon
    abstract fun findHealthDescription(): String?
    abstract fun isBuildable(): Boolean
    abstract fun getLastBuilds(): Collection<Build>
    abstract fun isFetchBuild(): Boolean
    abstract fun hasParameters(): Boolean
    abstract fun hasParameter(name: String): Boolean
    abstract fun updateContentWith(updatedJob: Job)
}