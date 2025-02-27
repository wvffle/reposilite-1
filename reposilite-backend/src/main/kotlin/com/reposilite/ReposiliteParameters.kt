package com.reposilite

import com.reposilite.config.Configuration
import com.reposilite.config.DEFAULT_CONFIGURATION_FILE
import com.reposilite.shared.safeResolve
import com.reposilite.token.api.AccessTokenPermission.MANAGER
import com.reposilite.token.api.CreateAccessTokenRequest
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import java.nio.file.Path
import java.nio.file.Paths

@Command(name = "reposilite", version = ["Reposilite $VERSION"])
class ReposiliteParameters : Runnable {

    @Option(names = ["--help"], usageHelp = true, description = ["display help message"])
    var usageHelpRequested = false

    @Option(names = ["--version", "-v"], versionHelp = true, description = ["display current version of reposilite"])
    var versionInfoRequested = false

    @Option(names = ["--working-directory", "-wd"], description = ["set custom working directory of application instance"], defaultValue = "")
    private lateinit var workingDirectoryName: String
    lateinit var workingDirectory: Path

    @Option(names = ["--configuration", "--config", "-cfg"], description = ["set custom location of configuration file"], defaultValue = DEFAULT_CONFIGURATION_FILE)
    private lateinit var configurationFileName: String
    lateinit var configurationFile: Path

    @Option(names = ["--hostname", "-h"], description = ["override hostname from configuration"])
    var hostname = ""

    @Option(names = ["--port", "-p"], description = ["override port from configuration"])
    var port = -1

    @Option(names = ["--token", "-t"], description = ["create temporary token with the given credentials in name:secret format", "Created token has all permissions"])
    private lateinit var tokenEntries: Array<String>
    lateinit var tokens: Collection<CreateAccessTokenRequest>

    @Option(names = ["--test-env", "--debug", "-d"], description = ["enable test mode"])
    var testEnv = false

    override fun run() {
        this.workingDirectory = Paths.get(workingDirectoryName)
        this.configurationFile = workingDirectory.safeResolve(configurationFileName.ifEmpty { DEFAULT_CONFIGURATION_FILE })
        this.tokens = tokenEntries
            .map { it.split(":", limit = 2) }
            .map { (name, secret) -> CreateAccessTokenRequest(name, secret, setOf(MANAGER)) }
    }

    fun applyLoadedConfiguration(configuration: Configuration) {
        if (hostname.isEmpty()) {
            this.hostname = configuration.hostname
        }

        if (port == -1) {
            this.port = configuration.port
        }
    }

}