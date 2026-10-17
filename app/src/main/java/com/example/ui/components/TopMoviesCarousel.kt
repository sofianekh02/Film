package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.MediaItem
import com.example.ui.theme.BebasNeueFontFamily
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@OptIn(ExperimentalTextApi::class)
@Composable
fun TopMoviesCarousel(
    movies: List<MediaItem>,
    onMovieClick: (MediaItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (movies.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        SectionHeader(title = "Top Movies")

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            itemsIndexed(movies) { index, item ->
                TopMovieCard(
                    rank = index + 1,
                    item = item,
                    onClick = { onMovieClick(item) }
                )
            }
        }
    }
}

@Composable
fun TopMovieCard(
    rank: Int,
    item: MediaItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(155.dp)
            .clickable(onClick = onClick)
            .testTag("top_movie_card_$rank"),
        horizontalAlignment = Alignment.Start
    ) {
        // Combined Rank Number + Poster Container
        Box(
            modifier = Modifier
                .width(155.dp)
                .height(210.dp)
        ) {
            // Massive Outline Rank Number behind the card to the left
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = (-10).dp, y = 14.dp)
            ) {
                // Background outline text effect for rank number
                Text(
                    text = "$rank",
                    fontFamily = BebasNeueFontFamily,
                    fontSize = 110.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF1E2638),
                    modifier = Modifier.offset(x = 2.dp, y = 2.dp)
                )
                Text(
                    text = "$rank",
                    fontFamily = BebasNeueFontFamily,
                    fontSize = 110.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF38445E)
                )
            }

            // Rounded Poster Image
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(125.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF161B26))
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(item.posterUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = item.displayTitle,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Rating badge on bottom right
                RatingBadge(
                    percent = item.ratingPercent,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Title
        Text(
            text = item.displayTitle,
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = TextPrimary
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp)
        )

        // Year
        Text(
            text = item.releaseYear,
            style = TextStyle(
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                color = TextMuted
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp)
        )
    }
}
