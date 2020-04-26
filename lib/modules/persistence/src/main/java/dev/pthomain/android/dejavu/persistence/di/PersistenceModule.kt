/*
 *
 *  Copyright (C) 2017-2020 Pierre Thomain
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

package dev.pthomain.android.dejavu.persistence.di

import dagger.Module
import dagger.Provides
import dev.pthomain.android.dejavu.persistence.base.store.KeySerialiser
import dev.pthomain.android.dejavu.persistence.serialisation.SerialisationManager
import dev.pthomain.android.dejavu.persistence.serialisation.Serialiser
import dev.pthomain.android.dejavu.shared.di.SharedModule
import dev.pthomain.android.dejavu.shared.serialisation.SerialisationDecorator
import dev.pthomain.android.dejavu.shared.utils.Function1
import javax.inject.Singleton

@Module(includes = [SharedModule::class])
abstract class PersistenceModule(
        private val decoratorList: List<SerialisationDecorator>,
        private val serialiser: Serialiser
) {

    @Provides
    @Singleton
    internal fun provideSerialiser() = serialiser

    @Provides
    @Singleton
    internal fun provideSerialisationManager(
            serialiser: Serialiser,
            byteToStringConverter: Function1<ByteArray, String>
    ) =
            SerialisationManager(
                    serialiser,
                    byteToStringConverter::get,
                    decoratorList
            )

    @Provides
    @Singleton
    internal fun provideByteToStringConverter() =
            object : Function1<ByteArray, String> {
                override fun get(t1: ByteArray) = String(t1)
            }

    @Provides
    @Singleton
    internal fun provideFileNameSerialiser() = KeySerialiser()

}
