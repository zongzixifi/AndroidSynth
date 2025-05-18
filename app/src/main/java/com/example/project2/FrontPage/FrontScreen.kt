package com.example.project2.FrontPage

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.VerticalAlignmentLine
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.project2.R
import kotlinx.coroutines.launch
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter



@Composable
fun FrontScreen(modifier: Modifier = Modifier, onClickJumpToAssistant: () -> Unit = {}, onClickJumpToSynth: () -> Unit = {}, onClickJumpToMusicGen: () -> Unit = {}) {
    val ClassContainersSetterChatPage = ClassContainerSetter(
        text = stringResource(R.string.get_start),
        imageResId = R.drawable.deepseek
    )

    val ClassContainersSetterMusicPage = ClassContainerSetter(
        text = stringResource(R.string.start_with_demo_music),
        imageResId = R.drawable.deepseek
    )
    val ClassContainersSetterGeneratePage = ClassContainerSetter(
        text = stringResource(R.string.start_with_only_text),
        imageResId = R.drawable.deepseek
    )

    val items = listOf(ClassContainersSetterChatPage, ClassContainersSetterMusicPage, ClassContainersSetterGeneratePage)

    val pagerState = rememberPagerState(initialPage = 1, pageCount = { 3 })

    Box(modifier = modifier.fillMaxSize()) {
        Image(painter = painterResource(id = R.drawable.test), contentDescription = null, modifier = Modifier.fillMaxSize(),contentScale = ContentScale.FillHeight,
            colorFilter = ColorFilter.tint(
                color = Color.Black.copy(alpha = 0.3f),
                blendMode = BlendMode.Multiply
            ))
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "选择一种方式\n开始创作",
                style = TextStyle(
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White
            )
            Spacer(modifier = Modifier.height(100.dp))
            CarouselSelector(
                modifier = Modifier,
                items = items,
                pagerState = pagerState,
                onItemClick = { _, _ -> }
            )
            Spacer(modifier = Modifier.weight(2f))
            Button(
                onClick = {
                    val currentPage = pagerState.currentPage
                    when (currentPage) {
                        0 -> onClickJumpToAssistant()
                        1 -> onClickJumpToSynth()
                        2 -> onClickJumpToMusicGen()
                    }
                },
                modifier = Modifier
                    .width(600.dp)
                    .height(70.dp)
                    .padding(horizontal = 32.dp, vertical = 8.dp),
                shape = RoundedCornerShape(30.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = colorResource(id=R.color.d4) )
            ) {
                Text(
                    text = "继续",
                    color = Color.White ,
                    style = TextStyle(
                            fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                )
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun CarouselSelector(modifier: Modifier,
                     items: List<ClassContainerSetter>,
                     pagerState: androidx.compose.foundation.pager.PagerState,
                     onItemClick:  (Int, ClassContainerSetter) -> Unit) {
    val currentPage = pagerState.currentPage

    HorizontalPager(
        beyondViewportPageCount = items.size,
        state = pagerState,
        contentPadding = PaddingValues(horizontal = 100.dp),
        pageSpacing = 16.dp,
        modifier = modifier
            .fillMaxWidth()
    ) { page ->
        val scale = if (page == currentPage) 1.1f else 0.9f
        val item = items[page]

        ModelContainer(
            modifier = Modifier
                .fillMaxWidth()
                .scale(scale),
            setter = item,
            onClick = { onItemClick(page, item) }
        )
    }
}


@Preview
@Composable
private fun FrontScreenPrev() {
    FrontScreen()
}

