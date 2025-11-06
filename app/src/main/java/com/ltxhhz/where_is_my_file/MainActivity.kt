package com.ltxhhz.where_is_my_file

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Debug
import android.os.Environment
import android.provider.DocumentsContract
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.compose.setContent
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.isDebugInspectorInfoEnabled
import androidx.core.net.toFile
import androidx.core.view.WindowCompat
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import androidx.core.net.toUri
import com.ltxhhz.where_is_my_file.ui.MainScreen


class MainActivity : AppCompatActivity() {
    private val model by viewModels<AppStateViewModel>()
    private lateinit var safPicker: SafPicker

    private lateinit var sourceUri: Uri
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightNavigationBars =
            true

        handleIntent(intent)
        safPicker = SafPicker(this)
        safPicker.onDirPicked = { uri ->
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            contentResolver.takePersistableUriPermission(uri, flags)
            fileSelected(getRealPathFromUri(uri))
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
        val uri = intent.getParcelableExtra(Intent.EXTRA_STREAM) ?: intent.data ?: return
        val fromPkg = referrer?.authority ?: ""

        val receiveFile = ReceiveFile(
            uri, fromPkg, intent.action!!, RealPathFromUriUtils.getRealPathFromUri(this, uri)
        )
        model.addItem(receiveFile)
    }

    private fun handleMultipleIntent(intent: Intent) {
        val streamUris = intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)
        if (streamUris != null) {
            for (uri in streamUris) {
                val fromPkg = referrer?.authority ?: ""

                val receiveFile = ReceiveFile(
                    uri,
                    fromPkg,
                    intent.action!!,
                    RealPathFromUriUtils.getRealPathFromUri(this, uri)
                )
                model.addItem(receiveFile)
            }
        }
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
//        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        XXPermissions.with(this) //.constantRequest() //可设置被拒绝后继续申请，直到用户授权或者永久拒绝
            //.permission(Permission.SYSTEM_ALERT_WINDOW, Permission.REQUEST_INSTALL_PACKAGES) //支持请求6.0悬浮窗权限8.0请求安装权限
            .permission(Permission.MANAGE_EXTERNAL_STORAGE) //不指定权限则自动获取清单中的危险权限
//            .interceptor(PermissionInterceptor())
            .request(object : OnPermissionCallback {
                override fun onGranted(permissions: List<String>, allGranted: Boolean) {
                    if (!allGranted) {
                        toast(R.string.permission_some)
                        return
                    }
//                    toast("获取存储权限成功")
                    safPicker.pickDirectory()
                }

                override fun onDenied(permissions: List<String>, doNotAskAgain: Boolean) {
                    if (doNotAskAgain) {
                        toast(R.string.permission_reject_permanent)
                        // 如果是被永久拒绝就跳转到应用权限系统设置页面
                        XXPermissions.startPermissionActivity(this@MainActivity, permissions)
                    } else {
                        toast(R.string.permission_reject)
                    }
                }
            })
    }

    private fun fileSelected(path: String) {
        val file = File(path)
        if (file.exists()) {
//            copyUriToFile(file.toUri(), selectedUri)
            copyToFile(path, sourceUri)
        } else {
            toastL("${getString(R.string.tip_path_not_exist)} $path")
        }
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

    private fun copyToFile(destDir: String, sourceUri: Uri) {
        val inputStream: InputStream? = contentResolver.openInputStream(sourceUri)
        inputStream?.use { input ->
            // 获取目标文件夹的路径
            val destinationFolder = File(destDir)

            // 创建目标文件
            val fileName = getFileName(sourceUri)
            val destinationFile = File(destinationFolder, fileName)
            if (destinationFile.exists()) {
                toast(R.string.tip_same_name_exist)
            } else {
                // 复制文件内容
//                toast("移动中")
                ProgressHelper.showDialog(this, getString(R.string.msg_moving))
                FileOutputStream(destinationFile).use { output ->
                    input.copyTo(output)
                    ProgressHelper.dismissDialog()
                    toast(R.string.tip_completed)
                }
            }
        }
    }

    private fun copyToFile(dest: Uri, sourceUri: Uri) {
        return copyToFile(getRealPathFromUri(dest), sourceUri)
    }

    private fun getFileName(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            result = RealPathFromUriUtils.getRealPathFromUri(this, uri)
        }
        if (result == null) {
            result = uri.path ?: "unknown_file"
            val cut = result.lastIndexOf('/')
            if (cut != -1) {
                result = result.substring(cut + 1)
            }
        }
        return result
    }

    private fun getRealPathFromUri(uri: Uri): String {
        val realPath = uri.toString()
        val documentId = DocumentsContract.getTreeDocumentId(uri)
        val split = documentId.split(":")
        val storageType = split[0]
        val path = split[1]

        return when (storageType) {
            "primary" -> {
                // 如果是 primary 存储，直接返回路径
                "${Environment.getExternalStorageDirectory()}/$path"
            }

            else -> {
                if (DocumentsContract.isDocumentUri(this, uri)) {
                    val d = DocumentsContract.getDocumentId(uri)
                    if (d.startsWith("raw:")) {
                        d.replaceFirst("raw:", "")
                    } else {
                        realPath
                    }
                } else {
                    RealPathFromUriUtils.getRealPathFromUri(this, uri) ?: realPath
                }
            }
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
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
