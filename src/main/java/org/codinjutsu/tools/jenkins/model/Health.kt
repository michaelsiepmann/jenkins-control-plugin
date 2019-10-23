package org.codinjutsu.tools.jenkins.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.codinjutsu.tools.jenkins.logic.JenkinsParser.JOB_HEALTH_DESCRIPTION
import org.codinjutsu.tools.jenkins.logic.JenkinsParser.JOB_HEALTH_ICON

class Health @JsonCreator
constructor(
        @JsonProperty(JOB_HEALTH_ICON)
        healthLevel: String,
        @JsonProperty(JOB_HEALTH_DESCRIPTION)
        var description: String?
) {

    var level = getHealthLevel(healthLevel)

    private fun getHealthLevel(icon: String?) =
            if (icon?.isNotEmpty() == true) {
                SUFFIXES.stream()
                        .filter { icon.endsWith(it) }
                        .findFirst()
                        .map { suffix -> icon.substring(0, icon.lastIndexOf(suffix)) }
                        .orElse(icon)
            } else {
                icon
            }

    companion object {

        private val SUFFIXES = listOf(".png", ".gif")

        fun createHealth(healthLevel: String, healthDescription: String): Health {
            return Health(healthLevel, healthDescription)
        }
    }
}
