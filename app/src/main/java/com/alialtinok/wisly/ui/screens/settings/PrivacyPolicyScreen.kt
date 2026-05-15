package com.alialtinok.wisly.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alialtinok.wisly.i18n.LocalAppStrings
import com.alialtinok.wisly.ui.screens.my.components.SubScreenScaffold
import com.alialtinok.wisly.ui.theme.WislyColors

@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    val s = LocalAppStrings.current

    SubScreenScaffold(title = s.privacyPolicy, onBack = onBack) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(WislyColors.Background),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = s.privacyPolicy,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                    )
                    Text(
                        text = "Last updated: May 2026",
                        fontSize = 12.sp,
                        color = WislyColors.OnSurfaceMuted,
                    )
                }
            }
            item {
                PolicySection(
                    icon = Icons.Filled.Security,
                    color = WislyColors.AccentGreen,
                    title = "No account required",
                    content = "Wisly does not ask for your name, email address, phone number, or account credentials.",
                )
            }
            item {
                PolicySection(
                    icon = Icons.Filled.Storage,
                    color = WislyColors.Primary,
                    title = "Local learning data",
                    content = "Favorites, review lists, custom words, streaks, settings, and cached translations are stored on your device.",
                )
            }
            item {
                PolicySection(
                    icon = Icons.Filled.Translate,
                    color = WislyColors.AccentPurple,
                    title = "Translation requests",
                    content = "When your native language is not Turkish, Wisly may request word translations from the MyMemory translation API. The requested word and language pair are sent only for that translation.",
                )
            }
            item {
                PolicySection(
                    icon = Icons.Filled.WifiOff,
                    color = WislyColors.B1,
                    title = "Offline content",
                    content = "Bundled vocabulary, phrasal verbs, idioms, saved words, and Turkish translations work offline. Non-Turkish translations require a network request the first time they are fetched.",
                )
            }
            item {
                PolicySection(
                    icon = Icons.AutoMirrored.Filled.VolumeUp,
                    color = WislyColors.AccentAmber,
                    title = "Text-to-speech",
                    content = "Wisly uses Android text-to-speech to pronounce words and translations. Speech behavior depends on the voices installed on your device.",
                )
            }
            item {
                PolicySection(
                    icon = Icons.Filled.BarChart,
                    color = WislyColors.AccentRed,
                    title = "No analytics or ads",
                    content = "Wisly does not include advertising SDKs, usage analytics, or behavior tracking.",
                )
            }
            item {
                PolicySection(
                    icon = Icons.Filled.Lock,
                    color = WislyColors.AccentGreen,
                    title = "Children's privacy",
                    content = "Wisly is designed to be safe for users of all ages and does not knowingly collect personal information from children.",
                )
            }
            item {
                PolicySection(
                    icon = Icons.Filled.Email,
                    color = WislyColors.Primary,
                    title = "Contact",
                    content = "Questions about this policy can be sent to wislyapp@gmail.com.",
                )
            }
            item {
                Text(
                    text = "This privacy policy may be updated from time to time. Continued use of the app after changes constitutes acceptance of the updated policy.",
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = WislyColors.OnSurfaceMuted,
                    modifier = Modifier.padding(top = 6.dp, bottom = 20.dp),
                )
            }
        }
    }
}

@Composable
private fun PolicySection(
    icon: ImageVector,
    color: Color,
    title: String,
    content: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(WislyColors.Surface, RoundedCornerShape(14.dp))
            .border(1.dp, WislyColors.SurfaceBorder, RoundedCornerShape(14.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier
                    .size(28.dp)
                    .background(color.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                    .padding(6.dp),
            )
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
        }
        Text(
            text = content,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = WislyColors.OnSurfaceMuted,
        )
    }
}
