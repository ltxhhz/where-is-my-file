package com.ltxhhz.where_is_my_file.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ltxhhz.where_is_my_file.ReceiveFile

@Composable
fun ContentMain(
    list: List<ReceiveFile>,
    emptyText: String = "从其他应用中将文件选择由本软件打开即可查看信息",
    onItemClick: (ReceiveFile) -> Unit,
    onItemLongClick: (ReceiveFile) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (list.isEmpty()) {
            Text(
                text = emptyText,
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
//                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                list.forEach {
                    ItemLayout(
                        item = it,
                        onClick = { onItemClick(it) },
                    ) { onItemLongClick(it) }
                }
            }
        }
    }
}