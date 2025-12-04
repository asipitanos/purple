package com.example.tides.screens.landingScreen.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun HalfWidthComponent(
    text: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    val halfWidthModifier = modifier.fillMaxWidth(fraction = 0.5f)

    Box(
        modifier =
            halfWidthModifier
                .padding(bottom = 12.dp)
                .height(30.dp),
        // Give it some height to be visible
        contentAlignment = Alignment.Center,
    ) {
        Text(text = "$text: $value")
    }
}

@Preview
@Composable
fun HalfWidthComponentPreview() {
    // The Row takes the full available width.
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)) {
        // This component is assigned 1 "part" of the total weight.
        HalfWidthComponent(
            text = "Text 1",
            value = "Value 1",
            modifier = Modifier.weight(1f),
        )

        // This component is also assigned 1 "part" of the total weight.
        // Since the total weight is 2 (1 + 1), each gets 1/2 of the space.
        HalfWidthComponent(
            text = "Text 2",
            value = "Value 2",
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
        )
    }
}
