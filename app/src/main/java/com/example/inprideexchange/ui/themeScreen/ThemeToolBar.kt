package com.example.inprideexchange.ui.themeScreen
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.example.inprideexchange.R
import com.example.inprideexchange.ui.designSystem.dimens.AppShapes



@Composable
fun ThemeToolBar(
    title: String,
    onBack: () -> Unit,
    onTitleClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onBackground,
) {
    Column(modifier = modifier.fillMaxWidth()) {

        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            val (backBtn, titleText) = createRefs()

            // 🔙 Back Button
            Icon(
                painter = painterResource(id = R.drawable.arrow_back_24px),
                contentDescription = "Back",
                tint = tint,
                modifier = Modifier
                    .size(24.dp)
                    .clip(AppShapes.Circle)
                    .clickable { onBack() }
                    .constrainAs(backBtn) {
                        start.linkTo(parent.start, margin = 16.dp)
                        centerVerticallyTo(parent)
                    }
            )

            // 📝 Title
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .padding(start = 12.dp)
                    .clickable { onTitleClick() }
                    .constrainAs(titleText) {
                        start.linkTo(backBtn.end)
                        centerVerticallyTo(parent)
                    }
            )
        }

        // ✅ Bottom Divider
        Divider(
            thickness = 2.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        )
    }
}



