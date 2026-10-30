package com.example.ui.components

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Subject

@Composable
fun EduBottomBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        modifier = Modifier.testTag("edu_bottom_bar")
    ) {
        val items = listOf(
            Triple(0, "Accueil", Icons.Default.Home to Icons.Outlined.Home),
            Triple(1, "Cours", Icons.Default.MenuBook to Icons.Outlined.MenuBook),
            Triple(2, "Quiz BAC", Icons.Default.Quiz to Icons.Outlined.Quiz),
            Triple(3, "EduBot IA", Icons.Default.SmartToy to Icons.Outlined.SmartToy),
            Triple(4, "Profil", Icons.Default.Person to Icons.Outlined.Person)
        )

        items.forEach { (index, label, icons) ->
            val isSelected = selectedTab == index
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(index) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) icons.first else icons.second,
                        contentDescription = label,
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                modifier = Modifier.testTag("tab_item_$index")
            )
        }
    }
}

@Composable
fun SubjectChipCard(
    subject: Subject,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(subject.colorHex) else MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .padding(end = 8.dp)
            .testTag("subject_chip_${subject.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(
                        color = if (isSelected) Color.White.copy(alpha = 0.25f) else Color(subject.colorHex).copy(alpha = 0.15f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getIconForSubject(subject.iconName),
                    contentDescription = subject.name,
                    tint = if (isSelected) Color.White else Color(subject.colorHex),
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = subject.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

fun getIconForSubject(iconName: String): ImageVector {
    return when (iconName) {
        "Calculate" -> Icons.Default.Calculate
        "Science" -> Icons.Default.Science
        "Biotech" -> Icons.Default.Biotech
        "Psychology" -> Icons.Default.Psychology
        "MenuBook" -> Icons.Default.MenuBook
        "Public" -> Icons.Default.Public
        "Translate" -> Icons.Default.Translate
        "Language" -> Icons.Default.Language
        "SportsSoccer" -> Icons.Default.SportsSoccer
        else -> Icons.Default.Book
    }
}
