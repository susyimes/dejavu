/*
 *
 *  Copyright (C) 2017 Pierre Thomain
 *
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package dev.pthomain.android.dejavu.interceptors.internal.cache.serialisation

import dev.pthomain.android.boilerplate.core.utils.lambda.Action
import dev.pthomain.android.boilerplate.core.utils.log.Logger
import dev.pthomain.android.dejavu.configuration.CacheConfiguration
import dev.pthomain.android.dejavu.configuration.NetworkErrorProvider
import dev.pthomain.android.dejavu.configuration.Serialiser
import dev.pthomain.android.dejavu.interceptors.internal.cache.token.CacheToken
import dev.pthomain.android.dejavu.response.CacheMetadata
import dev.pthomain.android.dejavu.response.ResponseWrapper
import dev.pthomain.android.mumbo.base.EncryptionManager

//TODO JavaDoc
class SerialisationManager<E>(private val logger: Logger,
                              private val configuration: CacheConfiguration<E>,
                              private val byteToStringConverter: (ByteArray) -> String,
                              private val encryptionManager: EncryptionManager,
                              private val compresser: (ByteArray) -> ByteArray,
                              private val uncompresser: (ByteArray, Int, Int) -> ByteArray,
                              private val serialiser: Serialiser)
        where E : Exception,
              E : NetworkErrorProvider {

    fun serialise(responseWrapper: ResponseWrapper<E>,
                  encryptData: Boolean,
                  compressData: Boolean): ByteArray? {
        val response = responseWrapper.response
        val responseClass = responseWrapper.responseClass

        val serialised = when {
            responseClass == String::class.java -> response as String
            response == null || !serialiser.canHandleType(response.javaClass) -> return null
            else -> serialiser.serialise(response)
        }

        return serialised
                .let {
                    //TODO This is to store the metadata, make class abstract and extract this logic
                    if (useFileCache()) responseClass.name + "\n" + it
                    else it
                }
                .toByteArray()
                .let { data ->
                    if (encryptData && encryptionManager.isEncryptionAvailable)
                        encryptionManager.encryptBytes(data, DATA_TAG)
                    else data
                }
                ?.let { data ->
                    if (compressData)
                        compresser(data).also { compressed ->
                            logCompression(
                                    compressed,
                                    responseClass.simpleName,
                                    data
                            )
                        }
                    else data
                }
    }

    fun deserialise(instructionToken: CacheToken,
                    data: ByteArray,
                    isEncrypted: Boolean,
                    isCompressed: Boolean,
                    onError: Action): ResponseWrapper<E>? {
        val responseClass = instructionToken.instruction.responseClass
        val simpleName = responseClass.simpleName

        try {
            return (if (isCompressed) {
                uncompresser(data, 0, data.size).also {
                    logCompression(data, simpleName, it)
                }
            } else {
                data
            }).let {
                if (isEncrypted && encryptionManager.isEncryptionAvailable)
                    encryptionManager.decryptBytes(it, DATA_TAG)
                            ?: throw IllegalStateException("Could not decrypt data")
                else it
            }.let {
                val serialised = byteToStringConverter(it)

                val payload = if (useFileCache())
                    serialised.substring(serialised.indexOf("\n"))
                else serialised

                serialiser.deserialise(payload, responseClass)
            }.let {
                ResponseWrapper(
                        responseClass,
                        it,
                        CacheMetadata<E>(instructionToken, null)
                )
            }
        } catch (e: Exception) {
            logger.e(
                    this,
                    e,
                    "Could not deserialise $simpleName: clearing the cache"
            )
            onError()
            return null
        }
    }

    private fun useFileCache() = configuration.cacheDirectory != null

    private fun logCompression(compressedData: ByteArray,
                               simpleName: String,
                               uncompressed: ByteArray) {
        logger.d(
                this,
                "Compressed/uncompressed $simpleName: ${compressedData.size}B/${uncompressed.size}B "
                        + "(${100 * compressedData.size / uncompressed.size}%)"
        )
    }

    companion object {
        private const val DATA_TAG = "DATA_TAG"
    }

}
