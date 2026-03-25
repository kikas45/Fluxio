package com.example.inprideexchange.ui.registerfeature

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.example.inprideexchange.R
import com.example.inprideexchange.ui.designsystem.dimens.AppShapes


@Composable
fun SimpleSmoothHelperToolBar(
    title: String,
    onBack: () -> Unit,
    onTitleClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onBackground,
) {

    ConstraintLayout(
        modifier = modifier
            .fillMaxWidth()
            .height(49.dp)
    ) {
        val (backBtn, titleText) = createRefs()




        Icon(
            painter = painterResource(id = R.drawable.ic_back_arrow),
            contentDescription = "Back",
            tint = tint,
            modifier = Modifier
                .size(22.dp)
                .clip(AppShapes.Circle)
                .padding(2.dp)
                .offset(x = 2.dp)
                .clickable {
                    onBack()
                }
                .constrainAs(backBtn) {
                    start.linkTo(parent.start, margin = 16.dp)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                }
        )


        Text(
            text = title,
            fontSize = 16.sp,
            maxLines = 1,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .clickable {
                    onTitleClick()
                }
                .constrainAs(titleText) {
                    end.linkTo(parent.end, margin = 24.dp)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                }
        )

    }
}
