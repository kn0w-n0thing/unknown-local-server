package org.chronusartcenter.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Console(modifier: Modifier, content: List<String>) {

    val scrollState = rememberScrollState()

    LaunchedEffect(content.size) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Column(modifier.background(Color.Gray)) {
        Text(
            modifier = Modifier
                .verticalScroll(scrollState)
                .fillMaxHeight()
                .padding(end = 5.dp),
            text = content.reduce(String::plus),
            fontFamily = FontFamily.Monospace,
            fontSize = 18.sp
        )
        // TODO: modify display
//        VerticalScrollbar(
//            modifier = Modifier
//                .align(Alignment.End)
//                .background(Color.White),
//            adapter = rememberScrollbarAdapter(scrollState)
//        )
    }

}