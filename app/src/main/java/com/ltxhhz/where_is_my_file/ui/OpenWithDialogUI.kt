package com.ltxhhz.where_is_my_file.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun OpenWithDialog(
    onDismissRequest: () -> Unit,
    onTypeButtonClick: () -> Unit,
    onCloseClick: () -> Unit,
    title: String = "打开方式"
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // 标题
                Text(
                    text = title,
                    fontSize = 20.sp,
                    color = Color.Black
                )

                // 应用图标网格 (留空占位)
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    // 应用网格内容将在这里实现
                }

                // 底部按钮
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                ) {
                    Button(
                        onClick = onTypeButtonClick,
                        colors = ButtonDefaults.textButtonColors()
                    ) {
                        Text("类型")
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = onCloseClick,
                        colors = ButtonDefaults.textButtonColors()
                    ) {
                        Text("关闭")
                    }
                }
            }
        }
    }
}