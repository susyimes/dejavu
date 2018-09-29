package uk.co.glass_software.android.cache_interceptor.response

import uk.co.glass_software.android.cache_interceptor.interceptors.cache.CacheToken

data class CacheMetadata<E>(@Transient val cacheToken: CacheToken,
                            @Transient val exception: E? = null,
                            @Transient val callDuration: Long = 0L)
        where E : Exception,
              E : (E) -> Boolean {

    interface Holder<E>
            where E : Exception,
                  E : (E) -> Boolean {
        var metadata: CacheMetadata<E>?
    }

}