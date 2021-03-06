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

package dev.pthomain.android.dejavu.demo.dejavu.clients.base

import dev.pthomain.android.dejavu.cache.metadata.response.DejaVuResult
import dev.pthomain.android.dejavu.demo.dejavu.clients.model.CatFactResponse
import io.reactivex.Single

interface SingleClients {

    interface Data {
        // GET

        fun get(): Single<CatFactResponse>
        fun compressed(): Single<CatFactResponse>
        fun encrypted(): Single<CatFactResponse>
        fun compressedEncrypted(): Single<CatFactResponse>

        // GET freshOnly

        fun freshOnly(): Single<CatFactResponse>
        fun freshOnlyCompressed(): Single<CatFactResponse>
        fun freshOnlyEncrypted(): Single<CatFactResponse>
        fun freshOnlyCompressedEncrypted(): Single<CatFactResponse>

        // REFRESH

        fun refresh(): Single<CatFactResponse>
        fun refreshCompressed(): Single<CatFactResponse>
        fun refreshEncrypted(): Single<CatFactResponse>
        fun refreshCompressedEncrypted(): Single<CatFactResponse>

        fun refreshFreshOnly(): Single<CatFactResponse>
        fun refreshCompressedFreshOnly(): Single<CatFactResponse>
        fun refreshEncryptedFreshOnly(): Single<CatFactResponse>
        fun refreshCompressedEncryptedFreshOnly(): Single<CatFactResponse>

        // OFFLINE

        fun offline(): Single<CatFactResponse>
        fun offlineFreshOnly(): Single<CatFactResponse>
    }

    interface Operations {
        // CLEAR

        fun clearCache(): Single<DejaVuResult<CatFactResponse>>

        // INVALIDATE

        fun invalidate(): Single<DejaVuResult<CatFactResponse>>
    }
}
