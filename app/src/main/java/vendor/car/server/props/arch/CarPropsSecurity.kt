package vendor.car.server.props.arch

import android.content.ContentProvider
import android.content.Context
import android.net.Uri
import android.util.Log
import vendor.car.server.R

object CarPropsSecurity {
    private const val TAG = "CarProps.Security"
    private val identifies: MutableMap<String, Set<String>> = mutableMapOf()

    @Throws(Exception::class)
    fun check(provider: ContentProvider, uri: Uri, readonly: Boolean) {
        // 校验调用 apk 包名
        loadIdentifies(context = provider.context)
        val caller = provider.callingPackage
        // 宿主应用跳过检查
        if (caller == provider.context?.packageName) return
        // 校验调用包
        val rules = identifies[caller] ?: throw Exception("not support for caller `$caller`")
        // 校验 URI 读写权限
        val path = uri.path?.replace("/", "")?.trim()
        if (path.isNullOrEmpty()) throw Exception("request path is null or empty")
        val item = rules.find { it.startsWith(path) }
            ?: throw Exception("no grant on `$path` for `$caller`")
        if (!readonly) {
            val parts = item.split(";")
            val writeable = parts.getOrNull(1)?.contains("w", ignoreCase = true) ?: false
            if (!writeable) throw Exception("no grant write permission on `$path` for `$caller`")
        }
    }

    private fun loadIdentifies(context: Context?) {
        if (identifies.isNotEmpty()) return
        val resources = context?.resources
        resources ?: throw Exception("resources is null while security check.")
        val packageName = context.packageName.orEmpty()
        val defType = resources.getResourceTypeName(R.array.grant_packages)
        val values = resources.getStringArray(R.array.grant_packages).mapNotNull { el ->
            return@mapNotNull kotlin.runCatching {
                val id = resources.getIdentifier(el, defType, packageName)
                val rows = resources.getStringArray(id).filter { it.isNotEmpty() }
                val key = rows.firstOrNull() ?: throw Exception("no package config for `$el`")
                val n = rows.size - 1
                val value = if (n > 0) rows.takeLast(n) else emptyList()
                return@runCatching Pair(key, value.toSet())
            }.onFailure {
                Log.e(TAG, "parse rule exception: ${it.message}")
            }.getOrNull()
        }
        identifies.putAll(values)
    }
}