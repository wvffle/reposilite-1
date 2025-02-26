/*
 * Copyright (c) 2021 dzikoysk
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
package com.reposilite.config

import com.reposilite.journalist.Journalist
import com.reposilite.journalist.Logger
import net.dzikoysk.cdn.CdnFactory
import panda.utilities.StringUtils
import java.nio.file.Path

const val DEFAULT_CONFIGURATION_FILE = "reposilite.cdn"

class ConfigurationLoader(private val journalist: Journalist) : Journalist {

    fun tryLoad(customConfigurationFile: Path): Configuration {
        return try {
            load(customConfigurationFile)
        } catch (exception: Exception) {
            throw RuntimeException("Cannot load configuration", exception)
        }
    }

    private fun load(configurationFile: Path): Configuration =
        CdnFactory.createStandard().let { cdn ->
            cdn.load(configurationFile.toFile(), Configuration::class.java).also {
                verifyBasePath(it)
                // verifyProxied(configuration)
                cdn.render(it, configurationFile.toFile())
                // loadProperties(it)
            }
        }

    private fun verifyBasePath(configuration: Configuration) {
        var basePath = configuration.basePath

        if (!StringUtils.isEmpty(basePath)) {
            if (!basePath.startsWith("/")) {
                basePath = "/$basePath"
            }

            if (!basePath.endsWith("/")) {
                basePath += "/"
            }

            configuration.basePath = basePath
        }
    }

    override fun getLogger(): Logger =
        journalist.logger

}