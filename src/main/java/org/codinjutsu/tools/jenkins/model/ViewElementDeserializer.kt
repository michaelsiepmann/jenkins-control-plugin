package org.codinjutsu.tools.jenkins.model

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import org.codinjutsu.tools.jenkins.logic.JenkinsJsonParser
import org.codinjutsu.tools.jenkins.logic.JenkinsParser.JOB_COLOR
import org.codinjutsu.tools.jenkins.logic.JenkinsParser.JOB_DISPLAY_NAME
import org.codinjutsu.tools.jenkins.logic.JenkinsParser.JOB_FULLDISPLAY_NAME
import org.codinjutsu.tools.jenkins.logic.JenkinsParser.JOB_FULL_NAME
import org.codinjutsu.tools.jenkins.logic.JenkinsParser.JOB_IS_BUILDABLE
import org.codinjutsu.tools.jenkins.logic.JenkinsParser.JOB_IS_IN_QUEUE
import org.codinjutsu.tools.jenkins.logic.JenkinsParser.JOB_LAST_BUILD
import org.codinjutsu.tools.jenkins.logic.JenkinsParser.JOB_NAME
import org.codinjutsu.tools.jenkins.logic.JenkinsParser.JOB_URL

internal class ViewElementDeserializer : StdDeserializer<ViewElement>(ViewElement::class.java) {

    override fun deserialize(parser: JsonParser, context: DeserializationContext): ViewElement {
        val objectMapper = JenkinsJsonParser.createObjectMapper()
        val node: JsonNode = objectMapper.readTree(parser)
        val clazz = node.getText("_class")
        if (clazz.contains("Folder")) {
            return createFolder(node)
        }
        return createJob(node, objectMapper)
    }

    private fun createFolder(node: JsonNode): Folder {
        val name = node.getText(JOB_NAME)
        val displayName = node.getText(JOB_DISPLAY_NAME)
        val fullDisplayName = node.getText(JOB_FULLDISPLAY_NAME)
        val fullName = node.getText(JOB_FULL_NAME)
        val url = node.getText(JOB_URL)
        return Folder(name, displayName, fullDisplayName, fullName, url, node.createHealth())
    }

    private fun createJob(node: JsonNode, objectMapper: ObjectMapper): Job {
        val name = node.getText(JOB_NAME)
        val displayName = node.getText(JOB_DISPLAY_NAME)
        val fullDisplayName = node.getText(JOB_FULLDISPLAY_NAME)
        val fullName = node.getText(JOB_FULL_NAME)
        val url = node.getText(JOB_URL)
        val job = Job(name, displayName, fullDisplayName, fullName, url)
        job.setHealth(node.createHealth())
        job.setColor(node.getText(JOB_COLOR))
        job.setBuildable(node.getText(JOB_IS_BUILDABLE).toBoolean())
        job.setInQueue(node.getText(JOB_IS_IN_QUEUE).toBoolean())
        val lastBuilds = node.get(JOB_LAST_BUILD)
        if (lastBuilds?.isArray == true) {
            val result = mutableListOf<Build>()
            lastBuilds.elements().forEachRemaining {
                result.add(it.createBuild(objectMapper, job))
            }
            job.setLastBuilds(result)
        } else if (lastBuilds?.isObject == true) {
            job.setLastBuilds(listOf(lastBuilds.createBuild(objectMapper, job)))
        }
        return job
    }

    private fun JsonNode.getText(key: String) = get(key).asText() ?: ""

    private fun JsonNode.createBuild(objectMapper: ObjectMapper, job: Job) =
            objectMapper.readValue(toString(), Build::class.java)
                    .apply {
                        this.job = job
                    }

    private fun JsonNode.createHealth(): Health {
        val healthReport = get("healthReport")
        if (healthReport?.isArray == true) {
            val health = healthReport.get(0)
            return Health.createHealth(health.getText("iconUrl"), health.getText("description"))
        }
        return Health.createHealth(healthReport.getText("iconUrl"), healthReport.getText("description"))
    }
}