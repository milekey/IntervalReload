package com.scaredeer.intervalreload.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeGesturesPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.scaredeer.intervalreload.ui.theme.AppTheme

@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
        modifier = Modifier.fillMaxSize().safeGesturesPadding()
    ) {
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { viewModel.toggleTimer() }
        ) {
            Text(
                text = uiState.buttonLabel,
                fontSize = 16.sp
            )
        }
        Text(
            text = uiState.datetime,
            modifier = Modifier
        )
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { viewModel.update() }
        ) {
            Text(
                text = "manual update",
                fontSize = 16.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    AppTheme {
        MainScreen()
    }
}