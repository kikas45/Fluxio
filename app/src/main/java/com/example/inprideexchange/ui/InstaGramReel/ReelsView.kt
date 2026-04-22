package com.example.inprideexchange.ui.InstaGramReel

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.inprideexchange.R

val verticalPadding = 12.dp
val horizontalPadding = 10.dp

// ===================== ROOT =====================
@Composable
fun ReelsView() {
    Box(Modifier.background(Color.Black)) {
        ReelsList()
        ReelsHeader()
    }
}

// ===================== HEADER =====================
@Composable
fun ReelsHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            "Reels",
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 21.sp
        )

        Icon(
            painter = painterResource(id = R.drawable.ic_outlined_camera),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}

// ===================== LIST =====================
@Composable
fun ReelsList() {
    val reels = DummyData.reels

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        itemsIndexed(
            items = reels,
            key = { _, reel -> reel.id }
        ) { _, reel ->

            Box(
                modifier = Modifier
                    .fillParentMaxSize()
            ) {
                VideoPlayer2(uri = reel.getVideoUrl())

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                ) {
                    ReelFooter(reel)
                }
            }
        }
    }
}

// ===================== FOOTER =====================
@Composable
fun ReelFooter(reel: Reel) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = 18.dp, bottom = 18.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        FooterUserData(
            reel = reel,
            modifier = Modifier.weight(8f)
        )

        FooterUserAction(
            reel = reel,
            modifier = Modifier.weight(2f)
        )
    }
}

// ===================== ACTION COLUMN =====================
@Composable
fun FooterUserAction(reel: Reel, modifier: Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        UserActionWithText(R.drawable.ic_outlined_favorite, reel.likesCount.toString())

        Spacer(Modifier.height(28.dp))

        UserActionWithText(R.drawable.ic_outlined_comment, reel.commentsCount.toString())

        Spacer(Modifier.height(28.dp))

        UserAction(R.drawable.ic_dm)

        Spacer(Modifier.height(28.dp))

        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = null,
            tint = Color.White
        )

        Spacer(Modifier.height(28.dp))

        // ✅ COIL (replaces Glide)
        AsyncImage(
            model = reel.userImage,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(6.dp))
        )
    }
}

// ===================== ICON =====================
@Composable
fun UserAction(@DrawableRes drawableRes: Int) {
    Icon(
        painter = painterResource(id = drawableRes),
        contentDescription = null,
        tint = Color.White,
        modifier = Modifier.size(16.dp)
    )
}

// ===================== ICON + TEXT =====================
@Composable
fun UserActionWithText(
    @DrawableRes drawableRes: Int,
    text: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            painter = painterResource(id = drawableRes),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(28.dp)
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = text,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ===================== USER DATA =====================
@Composable
fun FooterUserData(reel: Reel, modifier: Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center
    ) {

        Row(verticalAlignment = Alignment.CenterVertically) {

            AsyncImage(
                model = reel.userImage,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
            )

            Spacer(Modifier.width(horizontalPadding))

            Text(
                text = reel.userName,
                color = Color.White,
                style = MaterialTheme.typography.titleSmall
            )

            Spacer(Modifier.width(horizontalPadding))

            Canvas(modifier = Modifier.size(5.dp)) {
                drawCircle(Color.White)
            }

            Spacer(Modifier.width(horizontalPadding))

            Text(
                text = "Follow",
                color = Color.White,
                style = MaterialTheme.typography.titleSmall
            )
        }

        Spacer(Modifier.height(horizontalPadding))

        Text(text = reel.comment, color = Color.White)

        Spacer(Modifier.height(horizontalPadding))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(reel.userName, color = Color.White)

            Spacer(Modifier.width(horizontalPadding))

            Canvas(modifier = Modifier.size(5.dp)) {
                drawCircle(Color.White)
            }

            Spacer(Modifier.width(horizontalPadding))

            Text("Audio asli", color = Color.White)
        }
    }
}