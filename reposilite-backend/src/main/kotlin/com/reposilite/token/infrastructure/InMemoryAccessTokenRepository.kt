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

package com.reposilite.token.infrastructure

import com.reposilite.token.AccessTokenRepository
import com.reposilite.token.api.AccessToken

internal class InMemoryAccessTokenRepository : AccessTokenRepository {

    private val tokens: MutableMap<Int, AccessToken> = HashMap(1)

    override fun saveAccessToken(accessToken: AccessToken) {
        tokens[accessToken.id] = accessToken
    }

    override fun deleteAccessToken(accessToken: AccessToken) {
        tokens.remove(accessToken.id)
    }

    override fun findAccessTokenByName(name: String): AccessToken? =
        tokens.values.firstOrNull { it.name == name }

    override fun findAll(): Collection<AccessToken> =
        tokens.values

    override fun countAccessTokens(): Long =
        tokens.size.toLong()

}