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

package dev.pthomain.android.dejavu.persistence.memory

import dev.pthomain.android.boilerplate.core.utils.log.Logger
import dev.pthomain.android.dejavu.persistence.base.store.KeySerialiser
import dev.pthomain.android.dejavu.persistence.base.store.KeyValuePersistenceManager
import dev.pthomain.android.dejavu.serialisation.SerialisationManager
import java.util.*

class MemoryPersistenceManagerFactory internal constructor(
        private val dateFactory: (Long?) -> Date,
        private val logger: Logger,
        private val keySerialiser: KeySerialiser,
        private val storeFactory: MemoryStore.Factory,
        private val serialisationManager: SerialisationManager
) {

    fun create(maxEntries: Int = 20) =
            KeyValuePersistenceManager(
                    dateFactory,
                    logger,
                    keySerialiser,
                    storeFactory.create(maxEntries),
                    serialisationManager
            )

}