package com.ltxhhz.where_is_my_file

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts

class SafPicker(private val activity: ComponentActivity) {

    // 选择文件
    private val filePicker =
        activity.registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let { onFilePicked?.invoke(it) }
        }

    // 选择目录
    private val dirPicker =
        activity.registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
            uri?.let { onDirPicked?.invoke(it) }
        }

    var onFilePicked: ((Uri) -> Unit)? = null
    var onDirPicked: ((Uri) -> Unit)? = null

    fun pickFile(mimeTypes: Array<String> = arrayOf("*/*")) {
        filePicker.launch(mimeTypes)
    }

    fun pickDirectory() {
        dirPicker.launch(null)
    }
}
