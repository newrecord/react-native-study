package com.example.androidapp.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.androidapp.bridge.AppBridgeModule

private const val STORAGE_KEY = "shared_text"

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }
    var loadedText by remember { mutableStateOf<String?>(null) }

    val prefs = remember {
        context.getSharedPreferences(AppBridgeModule.PREFS_NAME, Context.MODE_PRIVATE)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "홈",
            style = MaterialTheme.typography.headlineLarge,
        )
        Text(
            text = "Jetpack Compose 네이티브 화면",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 저장 섹션
        Text(
            text = "SharedPreferences 저장",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = inputText,
            onValueChange = { inputText = it },
            label = { Text("텍스트 입력") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                prefs.edit().putString(STORAGE_KEY, inputText).apply()
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("저장")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 불러오기 섹션
        Text(
            text = "SharedPreferences 불러오기",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                loadedText = prefs.getString(STORAGE_KEY, null)
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("불러오기")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = loadedText ?: "(저장된 값 없음)",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = if (loadedText != null)
                    MaterialTheme.colorScheme.onSurface
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
