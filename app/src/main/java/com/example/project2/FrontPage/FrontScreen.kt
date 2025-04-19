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

@Composable
fun FrontScreen(modifier: Modifier = Modifier, onClickJumpToAssistant: () -> Unit ={}, onClickJumpToSynth: () -> Unit ={}, onClickJumpToMusicGen: () -> Unit ={}) {
    val ClassContainersSetterChatPage = ClassContainerSetter(
        text = stringResource(R.string.get_start),
        icon = Icons.Filled.Face,
        color = MaterialTheme.colorScheme.primary
    )

    val ClassContainersSetterMusicPage = ClassContainerSetter(
        text = stringResource(R.string.start_with_demo_music),
        icon = Icons.Filled.Create,
        color = MaterialTheme.colorScheme.secondaryContainer
    )
    val ClassContainersSetterGeneratePage = ClassContainerSetter(
        text = stringResource(R.string.start_with_only_text),
        icon = Icons.Filled.Edit,
        color = MaterialTheme.colorScheme.tertiaryContainer
    )

    val items = listOf(ClassContainersSetterChatPage, ClassContainersSetterMusicPage, ClassContainersSetterGeneratePage)

    Surface(
        modifier = modifier
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.tertiary
    ) {
        Column (
            modifier = Modifier,
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_background),
                contentDescription = ""
            )
            Spacer(modifier = Modifier.height(100.dp))
            CarouselSelector(
                modifier = Modifier,
                items = items,
                onItemClick = { index, item ->
                    when (index) {
                        0 -> onClickJumpToAssistant()
                        1 -> onClickJumpToSynth()
                        2 -> onClickJumpToMusicGen()
                    }
                }
            )
            Spacer(modifier = Modifier.weight(2f))
        }
    }
}

@Composable
fun CarouselSelector(modifier: Modifier,
                     items: List<ClassContainerSetter>,
                     onItemClick:  (Int, ClassContainerSetter) -> Unit) {
    val pagerState = rememberPagerState(initialPage = 1, pageCount = {3})
    val currentPage = pagerState.currentPage

    HorizontalPager(
        beyondViewportPageCount = items.size,
        state = pagerState,
        contentPadding = PaddingValues(horizontal = 64.dp),
        pageSpacing = 16.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(10.dp)
    ) { page ->
        val scale = if (page == currentPage) 1.2f else 0.8f
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
