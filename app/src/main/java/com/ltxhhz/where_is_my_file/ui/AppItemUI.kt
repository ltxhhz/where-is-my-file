package com.ltxhhz.where_is_my_file.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times

@Composable
fun AppItem(
    appName: String,
    appIconResId: Int?, // 可以传入图标资源ID
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .wrapContentWidth()
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 应用图标
        Box(
            modifier = Modifier
                .size(48.dp),
            contentAlignment = Alignment.Center
        ) {
            if (appIconResId != null) {
                Image(
                    painter = painterResource(id = appIconResId),
                    contentDescription = null
                )
            } else {
                // 默认图标占位符
                Icon(
                    painter = painterResource(android.R.drawable.sym_def_app_icon),
                    contentDescription = null
                )
            }
        }

        // 应用名称
        Text(
            text = appName,
            fontSize = 13.sp,
            color = Color.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = 4.dp)
                .width(5 * 13.dp) // 大约相当于ems=5
        )
    }
}