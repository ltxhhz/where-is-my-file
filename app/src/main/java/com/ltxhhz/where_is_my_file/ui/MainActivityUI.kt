package com.ltxhhz.where_is_my_file.ui

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import com.ltxhhz.where_is_my_file.AppStateViewModel
import com.ltxhhz.where_is_my_file.R
import com.ltxhhz.where_is_my_file.ReceiveFile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: AppStateViewModel,
    onItemClick: (ReceiveFile) -> Unit,
    onItemLongClick: (ReceiveFile) -> Unit,
    onFabClick: () -> Unit
) {
    val context = LocalContext.current
    val list by viewModel.list.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.app_name))
                },
                actions = {
                    IconButton(onClick = {
                        val intent = Intent(
                            Intent.ACTION_VIEW, "https://github.com/ltxhhz/where-is-my-file".toUri()
                        )
                        context.startActivity(intent)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Github"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onFabClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "清空"
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            ContentMain(list, onItemClick = onItemClick, onItemLongClick = onItemLongClick)
        }
    }
}