package com.ltxhhz.where_is_my_file

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.ltxhhz.where_is_my_file.databinding.ActivityMainBinding
import com.z.fileselectorlib.FileSelectorSettings
import com.z.fileselectorlib.Objects.FileInfo
import com.z.fileselectorlib.Utils.FileUtil
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var list: MutableList<ReceiveFile> = mutableListOf()

    private lateinit var selectedUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

//        val navController = findNavController(R.id.nav_host_fragment_content_main)
//        appBarConfiguration = AppBarConfiguration(navController.graph)
//        setupActionBarWithNavController(navController, appBarConfiguration)
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightNavigationBars =
            true

        binding.fab.setOnClickListener { view ->
            list.clear()
            updateView()
            Snackbar.make(view, R.string.tip_clear_list, Snackbar.LENGTH_SHORT)
                .setAnchorView(R.id.fab).show()

        }
        handleIntent(intent)
        updateView()
        val mCrashHandler = CrashHandler.instance
        mCrashHandler.init(applicationContext, Activity::class.java)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FileSelectorSettings.FILE_LIST_REQUEST_CODE && resultCode == FileSelectorSettings.BACK_WITH_SELECTIONS) {
            assert(data != null)
            val bundle = data!!.extras!!
            val filePathSelected =
                bundle.getStringArrayList(FileSelectorSettings.FILE_PATH_LIST_REQUEST)
            val filePath = filePathSelected!![0]
            val accessType = FileInfo.judgeAccess(filePath)
            when (accessType) {
//                FileInfo.AccessType.Open -> {
//                    var file: File = File(file_path)
//                }
                FileInfo.AccessType.Protected -> {
                    fileSelected(
                        getRealPathFromUri(
                            FileUtil.getDocumentFilePath(
                                this,
                                filePath
                            ).uri
                        )
                    )
                }

                else -> {
                    fileSelected(filePath)
                }
            }
            Log.v("file_sel", filePath!!)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let {
            handleIntent(it)
        }
    }

    private fun handleIntent(it: Intent) {
        val action = it.action
        if (action == Intent.ACTION_VIEW || action == Intent.ACTION_SEND || action == Intent.ACTION_SEND_MULTIPLE) {
            when (action) {
                Intent.ACTION_VIEW -> handleSingleIntent(it)
                Intent.ACTION_SEND -> handleSingleIntent(it)
                Intent.ACTION_SEND_MULTIPLE -> handleMultipleIntent(it)
            }
            updateView()
        }
    }

    private fun handleSingleIntent(intent: Intent) {
        val uri = intent.getParcelableExtra(Intent.EXTRA_STREAM) ?: intent.data ?: return
        val fromPkg = referrer?.authority ?: ""

        val receiveFile = ReceiveFile(
            uri,
            fromPkg,
            intent.action!!,
            RealPathFromUriUtils.getRealPathFromUri(this, uri)
        )
        list.add(receiveFile)
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
                list.add(receiveFile)
            }
        }
    }

    private fun updateView() {
        val emptyText = findViewById<TextView>(R.id.emptyText)
        val listview = findViewById<RecyclerView>(R.id.listview)

        if (list.isEmpty()) {
            emptyText.visibility = View.VISIBLE
            listview.visibility = View.GONE
        } else {
            emptyText.visibility = View.GONE
            listview.visibility = View.VISIBLE
            listview.layoutManager = LinearLayoutManager(this)
            listview.adapter = ItemAdapter(this, list) { item -> showMenuDialog(item) }
        }
    }

    private fun showMenuDialog(item: ReceiveFile) {
        val options = arrayOf(
            getString(R.string.menu_item0),
            getString(R.string.menu_item1),
            getString(R.string.menu_item2),
            getString(R.string.menu_item3),
            getString(R.string.menu_item4)
        )

        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.menu_title)).setItems(options) { dialog, which ->
            // 处理菜单项点击事件
            when (which) {
                0 -> copyToClipboard(item.filename)
                1 -> copyToClipboard(item.path)
                2 -> copyToClipboard(item.uri.toString())
//                    3 -> openFolderOfFile(item.path)
                3 -> share(item)
                4 -> selectFolderAndCopyFile(item.uri)
            }
            dialog.dismiss() // 点击后关闭对话框
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun selectFolderAndCopyFile(sourceUri: Uri) {
        selectedUri = sourceUri
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
                    select()
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

    private fun select() {
        val settings = FileSelectorSettings()
        toast(R.string.tip_longpress_to_select)
        settings.setRootPath(FileSelectorSettings.getSystemRootPath()) //起始路径
            .setMaxFileSelect(1) //最大文件选择数
            .setTitle(getString(R.string.tip_select_folder)) //标题
            .setFileTypesToSelect(FileInfo.FileType.Folder) //可选择文件类型
            .show(this) //显示
    }

    private fun fileSelected(path: String) {
        val file = File(path)
        if (file.exists()) {
//            copyUriToFile(file.toUri(), selectedUri)
            copyToFile(path, selectedUri)
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

    private fun copyToFile(dest: String, sourceUri: Uri) {
        val inputStream: InputStream? = contentResolver.openInputStream(sourceUri)
        inputStream?.use { input ->
            // 获取目标文件夹的路径
            val destinationFolder = File(dest)

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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_github -> {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://github.com/ltxhhz/where-is-my-file")
                )
                startActivity(intent)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

}

class ReceiveFile(
    val uri: Uri,
    val fromPkg: String,
    val action: String,
    s: String?
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
        filename = extractFileName(uri.toString())
        possible = s == null
        path = s ?: Uri.decode(uri.toString().replaceFirst(Regex("content://[^/]+"), ""))
        isDir = path.endsWith("/")
    }

    private fun extractFileName(url: String): String {
        val regExp = Regex("[^/?]+\$")
        val matchResult = regExp.find(Uri.decode(url))
        return matchResult?.value ?: ""
    }
}