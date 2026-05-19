package com.ltxhhz.where_is_my_file

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import com.ltxhhz.where_is_my_file.ui.MainScreen


class MainActivity : AppCompatActivity() {
    private val model by viewModels<AppStateViewModel>()
    private lateinit var safPicker: SafPicker

    private var sourceUri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightNavigationBars =
            true

        handleIntent(intent)
        safPicker = SafPicker(this)
        safPicker.onDirPicked = { uri ->
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            contentResolver.takePersistableUriPermission(uri, flags)
            copyToTree(uri, sourceUri)
        }
//        safPicker.onFilePicked = { uri ->
//        }
        val mCrashHandler = CrashHandler.instance
        mCrashHandler.init(applicationContext, Activity::class.java)
        setContent {
            MainScreen(model,{
                openFile(it)
            },{
                showMenuDialog(it)
            }) {
                model.clearList()
            }
        }
        if (BuildConfig.DEBUG){
            model.addItem(
                ReceiveFile(
                    "file:///storage/emulated/0/Download/test.txt".toUri(),
                    "com.ltxhhz.where_is_my_file",
                    "ACTION_VIEW",
                    "file:///storage/emulated/0/Download/test.txt"
                )
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    /**
     * 处理传入的Intent，根据不同的Action类型分发给相应的处理函数
     * @param it 传入的Intent对象
     */
    private fun handleIntent(it: Intent) {
        val action = it.action
        // 检查Intent的Action是否为支持的类型
        if (action == Intent.ACTION_VIEW || action == Intent.ACTION_SEND || action == Intent.ACTION_SEND_MULTIPLE) {
            // 根据具体的Action类型调用相应的处理函数
            when (action) {
                Intent.ACTION_VIEW -> handleSingleIntent(it)
                Intent.ACTION_SEND -> handleSingleIntent(it)
                Intent.ACTION_SEND_MULTIPLE -> handleMultipleIntent(it)
            }
        }
    }

    private fun handleSingleIntent(intent: Intent) {
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(Intent.EXTRA_STREAM)
        } ?: intent.data ?: return
        
        val fromPkg = referrer?.authority ?: ""

        model.addItem(createReceiveFile(uri, fromPkg, intent.action!!))
    }

    private fun handleMultipleIntent(intent: Intent) {
        val streamUris = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM, Uri::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM)
        }
        
        if (streamUris != null) {
            for (uri in streamUris) {
                val fromPkg = referrer?.authority ?: ""

                model.addItem(createReceiveFile(uri, fromPkg, intent.action!!))
            }
        }
    }

    private fun createReceiveFile(uri: Uri, fromPkg: String, action: String): ReceiveFile {
        return ReceiveFile(
            uri = uri,
            fromPkg = fromPkg,
            action = action,
            realPath = RealPathFromUriUtils.getRealPathFromUri(this, uri),
            displayName = queryDisplayName(uri)
        )
    }

    private fun showMenuDialog(item: ReceiveFile) {
        val options = arrayListOf(
            getString(R.string.menu_item0),
            getString(R.string.menu_item1),
            getString(R.string.menu_item2),
            getString(R.string.menu_item3),
            getString(R.string.menu_item4),
            getString(R.string.menu_item5)
        )
        if (BuildConfig.DEBUG) {
            options.add("test")
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.menu_title))
            .setItems(options.toTypedArray()) { dialog, which ->
                // 处理菜单项点击事件
                when (which) {
                    0 -> copyToClipboard(item.filename)
                    1 -> copyToClipboard(item.path)
                    2 -> copyToClipboard(item.uri.toString())
//                    3 -> openFolderOfFile(item.path)
                    3 -> share(item)
                    4 -> selectFolderAndCopyFile(item.uri)
                    5 -> openFile(item)
                    6 -> {
                        val apps = getAppsForFile(this, item.uri.toFile())
                        if (apps.isEmpty()) {
                            toast("没有可用的应用")
                        } else {
                            apps.forEach { resolveInfo ->
                                val appName = resolveInfo.loadLabel(packageManager).toString()
                                Log.v("apps", appName)
                                val appIcon = resolveInfo.loadIcon(packageManager)
                                // 显示到列表或对话框
                            }
                        }
                    }
                }
                dialog.dismiss() // 点击后关闭对话框
            }

        val dialog = builder.create()
        dialog.show()
    }

    private fun selectFolderAndCopyFile(sourceUri: Uri) {
        this@MainActivity.sourceUri = sourceUri
        safPicker.pickDirectory()
    }

    private fun toast(s: String) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
    }

    private fun toast(s: Int) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
    }

    private fun toastL(s: String) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show()
    }

    private fun toastL(s: Int) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show()
    }

    private fun copyToTree(destTreeUri: Uri, sourceUri: Uri?) {
        if (sourceUri == null) {
            return
        }

        val destinationFolder = DocumentFile.fromTreeUri(this, destTreeUri)
        if (destinationFolder == null || !destinationFolder.canWrite()) {
            toastL(R.string.tip_path_not_exist)
            return
        }

        val fileName = getFileName(sourceUri)
        if (destinationFolder.findFile(fileName) != null) {
            toast(R.string.tip_same_name_exist)
            return
        }

        val destinationFile = destinationFolder.createFile(getMimeType(sourceUri), fileName)
        if (destinationFile == null) {
            toastL(R.string.tip_path_not_exist)
            return
        }

        ProgressHelper.showDialog(this, getString(R.string.msg_moving))
        try {
            val copied = contentResolver.openInputStream(sourceUri)?.use { input ->
                contentResolver.openOutputStream(destinationFile.uri)?.use { output ->
                    input.copyTo(output)
                    true
                } ?: false
            } ?: false
            if (copied) {
                toast(R.string.tip_completed)
            } else {
                toastL(R.string.tip_path_not_exist)
            }
        } catch (e: Exception) {
            toastL(e.message ?: getString(R.string.tip_path_not_exist))
            e.printStackTrace()
        } finally {
            ProgressHelper.dismissDialog()
        }
    }

    private fun getFileName(uri: Uri): String {
        if (uri.scheme == "content") {
            queryDisplayName(uri)?.let {
                return it
            }
        }

        val result = uri.path ?: "unknown_file"
        val cut = result.lastIndexOf('/')
        return if (cut != -1) {
            result.substring(cut + 1)
        } else {
            result
        }
    }

    private fun queryDisplayName(uri: Uri): String? {
        var cursor: Cursor? = null
        return try {
            cursor = contentResolver.query(
                uri,
                arrayOf(OpenableColumns.DISPLAY_NAME),
                null,
                null,
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) cursor.getString(index) else null
            } else {
                null
            }
        } catch (e: Exception) {
            null
        } finally {
            cursor?.close()
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("Text", text)
        clipboardManager.setPrimaryClip(clipData)
        toast(R.string.tip_copied)
    }

    private fun share(item: ReceiveFile) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = getMimeType(item.uri)
        intent.putExtra(Intent.EXTRA_STREAM, item.uri)
        startActivity(Intent.createChooser(intent, getString(R.string.label_share_to)))
    }

    private fun getMimeType(uri: Uri): String {
        return contentResolver.getType(uri) ?: "*/*"
    }

    private fun openFile(item: ReceiveFile) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(item.uri, getMimeType(item.uri))
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        try {
            startActivity(intent)
        } catch (e: Exception) {
            toast(R.string.tip_no_app_to_open)
            e.printStackTrace()
        }
    }
}
