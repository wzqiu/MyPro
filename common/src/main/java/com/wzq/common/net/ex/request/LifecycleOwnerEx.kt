package com.wzq.common.net.ex.request

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.wzq.common.net.*
import com.wzq.common.net.ex.Result
import kotlinx.coroutines.*

/**
 *
 * Author: WZQ
 * CreateDate: 2021/10/22 17:31
 * Version: 1.0
 * Description: java类作用描述
 */
internal fun <DATA : Any> LifecycleOwner.requestResult(
    block: suspend () -> BaseResponse<DATA>,
    result: (Result) -> Unit = {}
): Job {
    return lifecycleScope.launch {
        try {
            runCatching {
                block.invoke()
            }.mapCatching {
                if (it.isSuccess) it.data else throw ApiException(it.code, it.message)
            }.onSuccess {
                withContext(Dispatchers.Main) {
                    result.invoke(Result.Success(it))
                }
            }.onFailure {
                val httpError = requestError(it)
                withContext(Dispatchers.Main) {
                    result.invoke(Result.Failure(httpError.first, httpError.second ?: ""))
                }
            }
        } finally {
            result.invoke(Result.Cancel)
        }
    }

}