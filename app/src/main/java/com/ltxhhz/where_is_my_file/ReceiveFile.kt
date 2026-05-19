package com.ltxhhz.where_is_my_file

import android.content.Intent
import android.net.Uri

class ReceiveFile(
    val uri: Uri,
    val fromPkg: String,
    val action: String,
    realPath: String?,
    displayName: String? = null
) {
    val path: String
    val filename: String
    val isDir: Boolean
    val possible: Boolean

    val isView: Boolean
        get() = action == Intent.ACTION_VIEW

    val isSend: Boolean
        get() = action == Intent.ACTION_SEND || action == Intent.ACTION_SEND_MULTIPLE

    init {
        filename = displayName ?: extractFileName(uri.toString())
        possible = realPath == null || uri.scheme != "file"
        path = realPath ?: Uri.decode(uri.toString().replaceFirst(Regex("content://[^/]+"), ""))
        isDir = path.endsWith("/")
    }

    private fun extractFileName(url: String): String {
        val regExp = Regex("[^/?]+\$")
        val matchResult = regExp.find(Uri.decode(url))
        return matchResult?.value ?: ""
    }
}
