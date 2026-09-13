package com.noxtope.jam.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noxtope.jam.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsScreen(onVolver: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.terminos_title)) },
                navigationIcon = {
                    TextButton(onClick = onVolver) {
                        Text(stringResource(R.string.volver), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(stringResource(R.string.terminos_uso),
                fontSize = 14.sp, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(4.dp))
            Text(stringResource(R.string.terminos_actualizacion),
                fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(20.dp))

            val sections = listOf(
                stringResource(R.string.terms_1_title) to stringResource(R.string.terms_1_body),
                stringResource(R.string.terms_2_title) to stringResource(R.string.terms_2_body),
                stringResource(R.string.terms_3_title) to stringResource(R.string.terms_3_body),
                stringResource(R.string.terms_4_title) to stringResource(R.string.terms_4_body),
                stringResource(R.string.terms_5_title) to stringResource(R.string.terms_5_body),
                stringResource(R.string.terms_6_title) to stringResource(R.string.terms_6_body),
                stringResource(R.string.terms_7_title) to stringResource(R.string.terms_7_body),
                stringResource(R.string.terms_8_title) to stringResource(R.string.terms_8_body),
                stringResource(R.string.terms_9_title) to stringResource(R.string.terms_9_body),
                stringResource(R.string.terms_10_title) to stringResource(R.string.terms_10_body),
                stringResource(R.string.terms_11_title) to stringResource(R.string.terms_11_body)
            )

            sections.forEach { (title, body) ->
                Text(title,
                    fontSize = 13.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(4.dp))
                Text(body,
                    fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
