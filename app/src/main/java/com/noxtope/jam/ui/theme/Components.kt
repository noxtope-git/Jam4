package com.noxtope.jam.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noxtope.jam.R

@Composable
fun JamLogo(
    modifier: Modifier = Modifier,
    size: Int = 120,
    showTagline: Boolean = true
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.jam_foreground),
            contentDescription = "Jam!",
            modifier = Modifier.size(size.dp),
            contentScale = ContentScale.Fit
        )
        if (showTagline) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Conecta · Comparte · Crea",
                fontSize = 13.sp,
                color = Color.Gray,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Light
            )
        }
    }
}

@Composable
fun JamIconSmall(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp
) {
    Image(
        painter = painterResource(R.drawable.jam_foreground),
        contentDescription = "Jam!",
        modifier = modifier.size(size),
        contentScale = ContentScale.Fit
    )
}
