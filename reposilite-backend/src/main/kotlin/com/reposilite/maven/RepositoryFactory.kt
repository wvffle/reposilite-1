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

package com.reposilite.maven

import com.reposilite.config.Configuration.RepositoryConfiguration
import com.reposilite.config.Configuration.RepositoryConfiguration.ProxiedHostConfiguration
import com.reposilite.journalist.Journalist
import com.reposilite.maven.MavenFacade.Companion.REPOSITORIES
import com.reposilite.shared.loadCommandBasedConfiguration
import com.reposilite.shared.safeResolve
import com.reposilite.storage.StorageProviderFactory.createStorageProvider
import java.nio.file.Path

internal class RepositoryFactory(
    private val journalist: Journalist,
    private val workingDirectory: Path,
) {

    fun createRepository(repositoryName: String, repositoryConfiguration: RepositoryConfiguration): Repository =
        Repository(
            repositoryName,
            repositoryConfiguration.visibility,
            repositoryConfiguration.proxied.associate { createProxiedHostConfiguration(it) },
            createStorageProvider(
                journalist,
                workingDirectory.safeResolve(REPOSITORIES).safeResolve(repositoryName),
                repositoryConfiguration.storageProvider,
                repositoryConfiguration.diskQuota
            ),
            repositoryConfiguration.redeployment
        )

    private fun createProxiedHostConfiguration(configuration: String): Pair<String, ProxiedHostConfiguration> =
        with(loadCommandBasedConfiguration(ProxiedHostConfiguration(), configuration)) {
            if (first.endsWith("/"))
                Pair(first.substring(0, first.length - 1), second)
            else
                this
        }

}