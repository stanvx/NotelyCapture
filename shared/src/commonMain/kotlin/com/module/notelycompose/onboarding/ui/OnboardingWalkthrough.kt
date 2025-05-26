package com.module.notelycompose.onboarding.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.module.notelycompose.notes.ui.theme.PoppingsFontFamily
import kotlinx.coroutines.launch
import notelycompose.shared.generated.resources.Res
import notelycompose.shared.generated.resources.onboarding_one
import org.jetbrains.compose.resources.painterResource

data class OnboardingPage(
    val title: String,
    val description: String,
    val backgroundColor: Color,
    val textColor: Color
)

@Composable
fun OnboardingWalkthrough(
    onFinish: () -> Unit = {}
) {
    val pages = listOf(
        OnboardingPage(
            title = "Create Notes\nand Share",
            description = "Write and share your notes\ninstantly with ease",
            backgroundColor = Color(0xFFFFF9C7),
            textColor = Color(0xFFCA7F58)
        ),
        OnboardingPage(
            title = "Record Voice\nNote and Share",
            description = "Capture and share voice notes\non the go.",
            backgroundColor = Color(0xFFFFF9C7),
            textColor = Color(0xFFCA7F58)
        ),
        OnboardingPage(
            title = "Transcribe\nand Summarise",
            description = "Convert voice notes to text and\nsummaries without internet.",
            backgroundColor = Color(0xFFFFF9C7),
            textColor = Color(0xFFCA7F58)
        ),
        OnboardingPage(
            title = "Stay Organized",
            description = "Keep all your voice notes organized and easily accessible whenever you need them.",
            backgroundColor = Color(0xFFFFF9C7),
            textColor = Color(0xFFCA7F58)
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(pages[pagerState.currentPage].backgroundColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Status Bar Spacer
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onFinish,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        text = "Skip",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Pager Content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(0.dp)
            ) { page ->
                OnboardingPageContent(
                    page = pages[page]
                )
            }

            // Bottom Navigation Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PageIndicators(
                    pageCount = pages.size,
                    currentPage = pagerState.currentPage,
                    activeColor = pages[pagerState.currentPage].textColor,
                    inactiveColor = pages[pagerState.currentPage].textColor.copy(alpha = 0.3f)
                )

                // Next/Get Started Button
                Button(
                    onClick = {
                        if (pagerState.currentPage == pages.size - 1) {
                            onFinish()
                        } else {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                    modifier = Modifier.height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = pages[pagerState.currentPage].textColor
                    ),
                    shape = RoundedCornerShape(24.dp),
                    contentPadding = PaddingValues(horizontal = 32.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = if (pagerState.currentPage == pages.size - 1) "Get Started" else "Next",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
fun OnboardingPageContent(
    page: OnboardingPage
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        VoiceNotePageContent(page)
    }
}

@Composable
fun VoiceNotePageContent(page: OnboardingPage) {

    Text(
        text = page.title,
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = PoppingsFontFamily(),
        color = page.textColor,
        textAlign = TextAlign.Start,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        lineHeight = 32.sp
    )

    // TODO: Check if tablet, modify for better spacing
    // Spacer(modifier = Modifier.height(88.dp))

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(Res.drawable.onboarding_one),
                contentDescription = "Image illustration",
                modifier = Modifier
                    .width(500.dp),
                contentScale = ContentScale.FillWidth
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = page.description,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF333333),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

@Composable
fun PageIndicators(
    pageCount: Int,
    currentPage: Int,
    activeColor: Color,
    inactiveColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val isActive = index == currentPage

            val animatedWidth by animateDpAsState(
                targetValue = if (isActive) 32.dp else 8.dp,
                animationSpec = tween(300, easing = EaseInOutQuad),
                label = "indicator_width"
            )

            val animatedColor by animateColorAsState(
                targetValue = if (isActive) activeColor else inactiveColor,
                animationSpec = tween(300, easing = EaseInOutQuad),
                label = "indicator_color"
            )

            Box(
                modifier = Modifier
                    .width(animatedWidth)
                    .height(8.dp)
                    .background(
                        animatedColor,
                        RoundedCornerShape(4.dp)
                    )
            )
        }
    }
}
