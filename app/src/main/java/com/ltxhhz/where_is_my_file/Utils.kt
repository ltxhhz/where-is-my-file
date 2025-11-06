package com.ltxhhz.where_is_my_file

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import java.io.File

fun getSupportedAppsForMimeType(mimeType: String, packageManager: PackageManager): List<ResolveInfo> {
    // 创建一个Intent，指定Action为ACTION_VIEW，并设置MimeType
    val intent = Intent(Intent.ACTION_VIEW).apply {
        type = mimeType
    }

    // 使用PackageManager查询能够处理此Intent的应用程序
    val resolveInfoList: List<ResolveInfo> = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)

    // 返回能够处理该MimeType的应用程序列表
    return resolveInfoList
}

fun getMimeType(file: File): String {
    val extension = MimeTypeMap.getFileExtensionFromUrl(file.path)
    return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase())
        ?: "application/octet-stream" // 默认类型
}

fun getAppsForFile(context: Context, file: File): List<ResolveInfo> {
    val mimeType = getMimeType(file)
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, mimeType)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    return context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
}