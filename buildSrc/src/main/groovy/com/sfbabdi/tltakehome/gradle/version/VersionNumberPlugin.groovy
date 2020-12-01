package com.sfbabdi.tltakehome.gradle.version

import org.apache.commons.lang3.StringUtils
import org.gradle.api.Plugin
import org.gradle.api.Project

class VersionNumberPlugin implements Plugin<Project> {
    /**
     * Rewrite project version to: <branch>-<githash>-<pipeline_number>
     * @param project
     */
    @Override
    void apply(Project project) {
        final String revisedVersion = String.format(
                "%s-%s-%s",
                ifNullThenDefault(getGitBranch(), "unknownBranch"),
                ifNullThenDefault(getGitHash(), "unknownGitHash"),
                ifNullThenDefault(getPipeline(), "local")
        )

        project.setVersion(revisedVersion)
        printf("Version: %s\n", project.version)
    }

    private String getGitBranch() {
        def proc = """git symbolic-ref HEAD --short""".execute()
        def output = new StringBuffer()
        proc.waitForProcessOutput(output, new StringBuffer())

        if (proc.exitValue() == 0) {
            return StringUtils.strip(output.toString().replace("/", "-"))
        } else {
            return System.getenv("CI_COMMIT_REF_NAME")
        }
    }

    private String getGitHash() {
        def proc = """git rev-parse --short HEAD""".execute()
        def output = new StringBuffer()
        proc.waitForProcessOutput(output, new StringBuffer())
        if (proc.exitValue() == 0) {
            return StringUtils.strip(output.toString())
        }
        return null
    }

    private String getPipeline() {
        return System.getenv("CI_PIPELINE_ID")
    }

    /**
     * @return If inString is null or empty, replace with defaultString.
     */
    private String ifNullThenDefault(String inString, String defaultString) {
        if (inString == null || "" == inString) {
            return defaultString
        }
        return inString
    }
}
