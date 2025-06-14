package com.example.project2.FrontPage
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.project2.R

@Composable
fun ModelContainer(modifier: Modifier = Modifier, setter: ClassContainerSetter, onClick: () -> Unit = {}) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 5.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(150.dp)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .shadow(elevation = 10.dp)
                    .clip(RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(30.dp),
                color = Color.White
            ) {
                // 添加一个空的 Box 作为 Surface 的内容
                Box(modifier = Modifier.fillMaxSize())
            }
            Image(
                painter = painterResource(id = setter.imageResId),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
        }
        Text(
            modifier = Modifier
                .padding(top = 16.dp),
            text = setter.text,
            style = TextStyle(
                fontSize = 16.sp,
            ),
            color = Color.White
        )
    }
}

data class ClassContainerSetter(
    val text: String,
    val imageResId: Int
)

@Preview
@Composable
private fun ModelContainerPrev() {
    val setter = ClassContainerSetter(
        text = stringResource(R.string.get_start),
        imageResId = R.drawable.deepseek
    )
    ModelContainer(setter = setter, onClick = {})
}