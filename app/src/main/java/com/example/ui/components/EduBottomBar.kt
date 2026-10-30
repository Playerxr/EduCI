package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.OrangePrimary

data class NavItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String
)

@Composable
fun EduBottomBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val navItems = listOf(
        NavItem("Accueil", Icons.Filled.Home, Icons.Outlined.Home, "nav_accueil"),
        NavItem("Cours", Icons.Filled.MenuBook, Icons.Outlined.MenuBook, "nav_cours"),
        NavItem("Quiz", Icons.Filled.Quiz, Icons.Outlined.Quiz, "nav_quiz"),
        NavItem("Guide IA", Icons.Filled.Psychology, Icons.Outlined.Psychology, "nav_edubot"),
        NavItem("Profil", Icons.Filled.Person, Icons.Outlined.Person, "nav_profil")
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        shadowElevation = 12.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEachIndexed { index, item ->
                val isSelected = selectedTab == index
                val animatedBgColor by animateColorAsState(
                    targetValue = if (isSelected) OrangePrimary.copy(alpha = 0.15f) else Color.Transparent,
                    label = "bg_color"
                )
                val animatedIconColor by animateColorAsState(
                    targetValue = if (isSelected) OrangePrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    label = "icon_color"
                )

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(animatedBgColor)
                        .clickable { onTabSelected(index) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag(item.tag),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.title,
                            tint = animatedIconColor,
                            modifier = Modifier.size(24.dp)
                        )
                        if (isSelected) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                ),
                                color = OrangePrimary
                            )
                        }
                    }
                }
            }
        }
    }
}
