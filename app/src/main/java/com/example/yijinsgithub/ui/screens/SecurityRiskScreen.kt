package com.example.yijinsgithub.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.yijinsgithub.R

@Composable
fun SecurityRiskScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.errorContainer
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = stringResource(R.string.root_detection_error),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center
            )
        }
    }
}
