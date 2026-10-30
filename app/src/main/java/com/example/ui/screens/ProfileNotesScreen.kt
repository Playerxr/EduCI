package com.example.ui.screens
import androidx.compose.animation.*

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.border
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import com.example.data.work.ReminderScheduler
import com.example.data.models.BookmarkEntity
import com.example.data.models.CertificateEntity
import com.example.data.models.NoteEntity
import com.example.data.models.Subject
import com.example.data.models.UserProfileEntity
import com.example.ui.components.getIconForSubject

@Composable
fun ProfileNotesScreen(
    userProfile: UserProfileEntity?,
    selectedLevel: String,
    selectedSeries: String,
    totalXp: Int,
    streakDays: Int,
    coins: Int = 100,
    bookmarks: List<BookmarkEntity>,
    notes: List<NoteEntity>,
    certificates: List<CertificateEntity> = emptyList(),
    allSubjects: List<Subject> = emptyList(),
    allHistorique: List<com.example.data.models.HistoriqueEntity> = emptyList(),
    isOfflineMode: Boolean = false,
    isDarkTheme: Boolean = false,
    onToggleDarkTheme: () -> Unit = {},
    onToggleSubjectSelection: (String, Boolean) -> Unit = { _, _ -> },
    onSaveProfile: (name: String, matricule: String, schoolName: String, level: String, series: String, examTarget: String) -> Unit,
    onSelectLevel: (String) -> Unit,
    onSelectSeries: (String) -> Unit,
    onSelectChapter: (String) -> Unit,
    onDeleteNote: (String) -> Unit,
    onRegisterWithAuth: ((email: String, pass: String, name: String, onResult: (Boolean, String?) -> Unit) -> Unit)? = null,
    onSignInWithAuth: ((email: String, pass: String, onResult: (Boolean, String?) -> Unit) -> Unit)? = null
) {
    var selectedSection by remember { mutableStateOf(0) } // 0: Mon Profil, 1: Favoris, 2: Notes Perso, 3: Certificats

    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("edu_ci_prefs", Context.MODE_PRIVATE) }

    var remindersEnabled by remember { mutableStateOf(sharedPrefs.getBoolean("pref_reminders_enabled", false)) }
    var reminderHour by remember { mutableStateOf(sharedPrefs.getInt("pref_reminder_hour", 18)) }
    var reminderMinute by remember { mutableStateOf(sharedPrefs.getInt("pref_reminder_minute", 0)) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            remindersEnabled = true
            sharedPrefs.edit().putBoolean("pref_reminders_enabled", true).apply()
            ReminderScheduler.scheduleDailyReminder(context, reminderHour, reminderMinute)
            Toast.makeText(context, "Rappel activé pour ${String.format("%02d:%02d", reminderHour, reminderMinute)} !", Toast.LENGTH_SHORT).show()
        } else {
            remindersEnabled = false
            sharedPrefs.edit().putBoolean("pref_reminders_enabled", false).apply()
            Toast.makeText(context, "Rappel désactivé (notification non autorisée).", Toast.LENGTH_LONG).show()
        }
    }

    // Local form state initialized from userProfile
    var nameInput by remember(userProfile) { mutableStateOf(userProfile?.name ?: "") }
    var matriculeInput by remember(userProfile) { mutableStateOf(userProfile?.matricule ?: "") }
    var schoolInput by remember(userProfile) { mutableStateOf(userProfile?.schoolName ?: "") }
    var targetInput by remember(userProfile) { mutableStateOf(userProfile?.examTarget ?: "") }
    var currentLevel by remember(userProfile) { mutableStateOf(userProfile?.selectedLevel ?: selectedLevel) }
    var currentSeries by remember(userProfile) { mutableStateOf(userProfile?.selectedSeries ?: selectedSeries) }
    var isSavedSuccess by remember { mutableStateOf(false) }

    // Firebase Auth fields
    var emailAuthInput by remember { mutableStateOf("") }
    var passwordAuthInput by remember { mutableStateOf("") }
    var authStatusMsg by remember { mutableStateOf<String?>(null) }
    var isAuthLoading by remember { mutableStateOf(false) }

    val currentXp = userProfile?.totalXp ?: totalXp
    val currentStreak = userProfile?.streakDays ?: streakDays
    val currentCoins = userProfile?.coins ?: coins

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("profile_notes_screen")
    ) {
        // Profile Top Header
        Surface(
            tonalElevation = 2.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(top = 40.dp, bottom = 16.dp, start = 20.dp, end = 20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Avatar",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = if (userProfile?.name.isNullOrBlank()) "Élève Non Connecté(e)" else "${userProfile!!.name} 🇨🇮",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (!userProfile?.matricule.isNullOrBlank()) {
                            Text(
                                text = "Matricule : ${userProfile!!.matricule}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = if (userProfile?.schoolName.isNullOrBlank()) "Veuillez renseigner votre profil ci-dessous" else userProfile!!.schoolName,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stats Cards with 0 initially until earned + Coins
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = Color(0xFFFF9800), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Column {
                                Text("$currentStreak j", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Série", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Stars, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Column {
                                Text("$currentXp XP", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Points", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Column {
                                Text("$currentCoins 🪙", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("EduCoins", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        // Section Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedSection,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            edgePadding = 16.dp
        ) {
            Tab(
                selected = selectedSection == 0,
                onClick = { selectedSection = 0 },
                text = { Text("Inscription & Profil", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedSection == 1,
                onClick = { selectedSection = 1 },
                text = { Text("Favoris (${bookmarks.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedSection == 2,
                onClick = { selectedSection = 2 },
                text = { Text("Notes (${notes.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedSection == 3,
                onClick = { selectedSection = 3 },
                text = { Text("Certificats (${certificates.size}) 📜", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedSection == 4,
                onClick = { selectedSection = 4 },
                text = { Text("Performances 📊", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (selectedSection) {
                0 -> { // Registration Form & Configuration
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Mode Sombre / Nuit",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (isDarkTheme) "Activé (Optimal pour les révisions nocturnes)" else "Désactivé (Thème Clair)",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Switch(
                                    checked = isDarkTheme,
                                    onCheckedChange = { onToggleDarkTheme() },
                                    modifier = Modifier.testTag("switch_dark_theme")
                                )
                            }
                        }
                    }

                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.NotificationsActive,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "Rappels de Révision ⏰",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = if (remindersEnabled) "Activé • Rappel à ${String.format("%02d:%02d", reminderHour, reminderMinute)}" else "Désactivé (Recommande de l'activer)",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Switch(
                                        checked = remindersEnabled,
                                        onCheckedChange = { isChecked ->
                                            if (isChecked) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                                    val hasPermission = ContextCompat.checkSelfPermission(
                                                        context,
                                                        Manifest.permission.POST_NOTIFICATIONS
                                                    ) == PackageManager.PERMISSION_GRANTED
                                                    
                                                    if (hasPermission) {
                                                        remindersEnabled = true
                                                        sharedPrefs.edit().putBoolean("pref_reminders_enabled", true).apply()
                                                        ReminderScheduler.scheduleDailyReminder(context, reminderHour, reminderMinute)
                                                        Toast.makeText(context, "Rappels activés à ${String.format("%02d:%02d", reminderHour, reminderMinute)} !", Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                                    }
                                                } else {
                                                    remindersEnabled = true
                                                    sharedPrefs.edit().putBoolean("pref_reminders_enabled", true).apply()
                                                    ReminderScheduler.scheduleDailyReminder(context, reminderHour, reminderMinute)
                                                    Toast.makeText(context, "Rappels activés à ${String.format("%02d:%02d", reminderHour, reminderMinute)} !", Toast.LENGTH_SHORT).show()
                                                }
                                            } else {
                                                remindersEnabled = false
                                                sharedPrefs.edit().putBoolean("pref_reminders_enabled", false).apply()
                                                ReminderScheduler.cancelDailyReminder(context)
                                                Toast.makeText(context, "Rappels désactivés.", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.testTag("switch_daily_reminders")
                                    )
                                }

                                androidx.compose.animation.AnimatedVisibility(
                                    visible = remindersEnabled,
                                    enter = fadeIn() + expandVertically(),
                                    exit = fadeOut() + shrinkVertically()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "Définir l'heure du rappel quotidien",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        
                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            // Hour block
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                IconButton(
                                                    onClick = {
                                                        reminderHour = (reminderHour + 1) % 24
                                                        sharedPrefs.edit().putInt("pref_reminder_hour", reminderHour).apply()
                                                        ReminderScheduler.scheduleDailyReminder(context, reminderHour, reminderMinute)
                                                    },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Plus")
                                                }
                                                
                                                Surface(
                                                    color = MaterialTheme.colorScheme.primaryContainer,
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.width(56.dp).height(40.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Text(
                                                            text = String.format("%02d", reminderHour),
                                                            fontSize = 18.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                                        )
                                                    }
                                                }
                                                
                                                IconButton(
                                                    onClick = {
                                                        reminderHour = if (reminderHour == 0) 23 else reminderHour - 1
                                                        sharedPrefs.edit().putInt("pref_reminder_hour", reminderHour).apply()
                                                        ReminderScheduler.scheduleDailyReminder(context, reminderHour, reminderMinute)
                                                    },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Moins")
                                                }
                                            }
                                            
                                            Text(
                                                text = ":",
                                                fontSize = 24.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 12.dp)
                                            )
                                            
                                            // Minute block
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                IconButton(
                                                    onClick = {
                                                        reminderMinute = (reminderMinute + 5) % 60
                                                        sharedPrefs.edit().putInt("pref_reminder_minute", reminderMinute).apply()
                                                        ReminderScheduler.scheduleDailyReminder(context, reminderHour, reminderMinute)
                                                    },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Plus")
                                                }
                                                
                                                Surface(
                                                    color = MaterialTheme.colorScheme.primaryContainer,
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.width(56.dp).height(40.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Text(
                                                            text = String.format("%02d", reminderMinute),
                                                            fontSize = 18.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                                        )
                                                    }
                                                }
                                                
                                                IconButton(
                                                    onClick = {
                                                        reminderMinute = if (reminderMinute < 5) 55 else reminderMinute - 5
                                                        sharedPrefs.edit().putInt("pref_reminder_minute", reminderMinute).apply()
                                                        ReminderScheduler.scheduleDailyReminder(context, reminderHour, reminderMinute)
                                                    },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Moins")
                                                }
                                            }
                                        }

                                        val activeSubjectsStr = allSubjects.filter { it.isSelected }.joinToString(", ") { it.name }
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Surface(
                                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Text(
                                                    text = "Matières au programme de révision :",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Text(
                                                    text = if (activeSubjectsStr.isNotEmpty()) activeSubjectsStr else "Aucune matière sélectionnée (clique sur les interrupteurs ci-dessous pour en ajouter)",
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.padding(top = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("card_room_subjects_selection")
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.MenuBook,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "Matières du Second Cycle (Room)",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "Sélectionnez vos matières de révision",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            text = "${allSubjects.count { it.isSelected }}/${allSubjects.size} actives",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                allSubjects.forEachIndexed { index, subject ->
                                    if (index > 0) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .background(
                                                        color = Color(subject.colorHex).copy(alpha = 0.15f),
                                                        shape = CircleShape
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = getIconForSubject(subject.iconName),
                                                    contentDescription = subject.name,
                                                    tint = Color(subject.colorHex),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = subject.name,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 14.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = subject.description,
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                        Switch(
                                            checked = subject.isSelected,
                                            onCheckedChange = { isChecked ->
                                                onToggleSubjectSelection(subject.id, isChecked)
                                            },
                                            modifier = Modifier.testTag("switch_subject_${subject.id}")
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isOfflineMode) Icons.Default.CloudOff else Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = if (isOfflineMode) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Authentification Firebase & Compteur Éléve", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                                Spacer(modifier = Modifier.height(12.dp))

                                if (isOfflineMode) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                            .padding(14.dp)
                                    ) {
                                        Column {
                                            Text(
                                                text = "🔌 Fonctionnalité Réduite Hors-ligne",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onErrorContainer
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "La création de compte et la connexion via Firebase nécessitent une connexion internet active. Passez en mode connecté (via la barre d'état) pour y accéder.",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.85f)
                                            )
                                        }
                                    }
                                }

                                if (!isOfflineMode) {
                                    OutlinedTextField(
                                    value = emailAuthInput,
                                    onValueChange = { emailAuthInput = it; authStatusMsg = null },
                                    label = { Text("Adresse Email Élève") },
                                    placeholder = { Text("ex: eleve@education.ci") },
                                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = passwordAuthInput,
                                    onValueChange = { passwordAuthInput = it; authStatusMsg = null },
                                    label = { Text("Mot de Passe") },
                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            if (emailAuthInput.isNotBlank() && passwordAuthInput.isNotBlank()) {
                                                isAuthLoading = true
                                                onRegisterWithAuth?.invoke(emailAuthInput, passwordAuthInput, nameInput) { success, msg ->
                                                    isAuthLoading = false
                                                    authStatusMsg = if (success) "Inscription réussie avec l'Email !" else "Erreur: $msg"
                                                }
                                            } else {
                                                authStatusMsg = "Veuillez entrer un email et un mot de passe valide."
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("S'inscrire", fontSize = 12.sp)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            if (emailAuthInput.isNotBlank() && passwordAuthInput.isNotBlank()) {
                                                isAuthLoading = true
                                                onSignInWithAuth?.invoke(emailAuthInput, passwordAuthInput) { success, msg ->
                                                    isAuthLoading = false
                                                    authStatusMsg = if (success) "Connexion réussie !" else "Erreur: $msg"
                                                }
                                            } else {
                                                authStatusMsg = "Veuillez entrer un email et un mot de passe."
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Se Connecter", fontSize = 12.sp)
                                    }
                                }

                                if (authStatusMsg != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = authStatusMsg!!,
                                        fontSize = 12.sp,
                                        color = if (authStatusMsg!!.contains("réussie", ignoreCase = true)) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Badge, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Fiche Élève & Informations", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                                Spacer(modifier = Modifier.height(16.dp))

                                OutlinedTextField(
                                    value = nameInput,
                                    onValueChange = { nameInput = it; isSavedSuccess = false },
                                    label = { Text("Nom et Prénom(s) de l'Élève") },
                                    placeholder = { Text("ex: Seka Junior") },
                                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Column {
                                    OutlinedTextField(
                                        value = matriculeInput,
                                        onValueChange = { matriculeInput = it; isSavedSuccess = false },
                                        label = { Text("Matricule Élève / Établissement") },
                                        placeholder = { Text("ex: MAT-2026-CI-8492") },
                                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )
                                    TextButton(
                                        onClick = {
                                            matriculeInput = "MAT-2026-CI-" + (1000..9999).random()
                                            isSavedSuccess = false
                                        }
                                    ) {
                                        Icon(Icons.Default.Autorenew, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Générer un Matricule Automatique", fontSize = 12.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = schoolInput,
                                    onValueChange = { schoolInput = it; isSavedSuccess = false },
                                    label = { Text("Nom du Lycée / Collège") },
                                    placeholder = { Text("ex: Lycée Scientifique de Yamoussoukro") },
                                    leadingIcon = { Icon(Icons.Default.School, contentDescription = null) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = targetInput,
                                    onValueChange = { targetInput = it; isSavedSuccess = false },
                                    label = { Text("Objectif (Ex: Réussir le BAC avec Mention)") },
                                    placeholder = { Text("ex: Mention Très Bien au BAC 2026") },
                                    leadingIcon = { Icon(Icons.Default.EmojiEvents, contentDescription = null) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Niveau d'Études", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("Seconde", "Première", "Terminale").forEach { lvl ->
                                        FilterChip(
                                            selected = currentLevel == lvl,
                                            onClick = {
                                                currentLevel = lvl
                                                onSelectLevel(lvl)
                                                isSavedSuccess = false
                                            },
                                            label = { Text(lvl) }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Série du BAC / Filière", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("A", "C", "D").forEach { srs ->
                                        FilterChip(
                                            selected = currentSeries == srs,
                                            onClick = {
                                                currentSeries = srs
                                                onSelectSeries(srs)
                                                isSavedSuccess = false
                                            },
                                            label = { Text("Série $srs") }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                Button(
                                    onClick = {
                                        onSaveProfile(nameInput, matriculeInput, schoolInput, currentLevel, currentSeries, targetInput)
                                        isSavedSuccess = true
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Save, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Enregistrer & Connecter l'Élève")
                                }

                                if (isSavedSuccess) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Surface(
                                        color = Color(0xFFE8F5E9),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                "Profil sauvegardé avec succès dans Room SQLite !",
                                                color = Color(0xFF2E7D32),
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 13.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> { // Favoris Bookmarks
                    if (bookmarks.isEmpty()) {
                        item {
                            Text("Aucun chapitre en favori pour l'instant.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        items(bookmarks) { bm ->
                            Card(
                                onClick = { onSelectChapter(bm.chapterId) },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(bm.chapterTitle, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Text("Cliquer pour ouvrir la fiche", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                    }
                                    Icon(Icons.Default.Bookmark, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }

                2 -> { // Personal Notes
                    if (notes.isEmpty()) {
                        item {
                            Text("Aucune note personnelle créée. Tu peux ajouter des notes lors de la lecture des cours !", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        items(notes) { note ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(note.noteTitle, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        IconButton(onClick = { onDeleteNote(note.id) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                    Text("Chapitre : ${note.chapterTitle}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(note.noteContent, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }
                }

                3 -> { // Certificats de Réussite
                    if (certificates.isEmpty()) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {

                                    Icon(Icons.Default.MilitaryTech, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(48.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Aucun certificat obtenu pour le moment", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Complète un quiz dans l'onglet Cours pour décrocher ton premier Certificat de Réussite EduCI !", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    } else {
                        items(certificates) { cert ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.5.dp, Color(0xFFFFD54F), RoundedCornerShape(16.dp))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(24.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text("Certificat de Réussite Interne EduCI", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                                Text("Attestation d'accomplissement dans l'application", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                        Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(8.dp)) {
                                            Text(
                                                text = "${cert.scorePercentage}% Réussite",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF2E7D32),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = cert.chapterTitle,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Matière : ${cert.subjectName} | ${cert.level} - Série ${cert.series}",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Délivré à : ${cert.studentName}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                            Text("Code : ${cert.certificateCode}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Text("Date : ${cert.issueDate}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }

                4 -> { // Suivi & Courbe de Performance
                    val history = allHistorique
                    val averageScore = if (history.isNotEmpty()) history.map { it.scorePercentage }.average().toInt() else 0
                    val totalRevision = history.size

                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Score Moyen", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("$averageScore%", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Révisions", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("$totalRevision sessions", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = if (averageScore >= 80) "Félicitations ! Tu as une excellente maîtrise globale du programme BAC 🇨🇮."
                                    else if (averageScore >= 60) "Bonne progression ! Continue de t'entraîner pour viser la mention."
                                    else "N'abandonne pas ! Chaque quiz t'aide à comprendre et à progresser.",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    if (history.isNotEmpty()) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        "Évolution des Scores (%)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    ScoreEvolutionChart(
                                        history = history,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp)
                                    )
                                }
                            }
                        }

                        items(history.sortedByDescending { it.revisionDate }) { h ->
                            val dateStr = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.FRANCE).format(java.util.Date(h.revisionDate))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(h.chapterTitle, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                            Text("${h.subjectName} • $dateStr", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Surface(
                                            color = if (h.scorePercentage >= 80) Color(0xFFE8F5E9) else if (h.scorePercentage >= 50) Color(0xFFFFF3E0) else Color(0xFFFFEBEE),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = "${h.score}/${h.totalQuestions} (${h.scorePercentage}%)",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = if (h.scorePercentage >= 80) Color(0xFF2E7D32) else if (h.scorePercentage >= 50) Color(0xFFE65100) else Color(0xFFC62828),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                    if (h.feedback.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = h.feedback,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Timeline,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("Pas encore de données d'évolution", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text(
                                        "Réalise ton premier quiz d'évaluation pour visualiser l'évolution de tes scores au fil du temps !",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScoreEvolutionChart(
    history: List<com.example.data.models.HistoriqueEntity>,
    modifier: Modifier = Modifier
) {
    val sorted = history.sortedBy { it.revisionDate }
    if (sorted.isEmpty()) {
        Box(
            modifier = modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text("Aucune donnée disponible", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val gridColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant

    androidx.compose.foundation.Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val paddingLeft = 45.dp.toPx()
        val paddingRight = 20.dp.toPx()
        val paddingTop = 20.dp.toPx()
        val paddingBottom = 30.dp.toPx()

        val chartWidth = width - paddingLeft - paddingRight
        val chartHeight = height - paddingTop - paddingBottom

        // Draw Y axis guidelines (0%, 25%, 50%, 75%, 100%)
        val ySteps = listOf(0, 25, 50, 75, 100)
        ySteps.forEach { score ->
            val y = paddingTop + chartHeight * (100 - score) / 100f
            drawLine(
                color = gridColor,
                start = androidx.compose.ui.geometry.Offset(paddingLeft, y),
                end = androidx.compose.ui.geometry.Offset(width - paddingRight, y),
                strokeWidth = 1.dp.toPx()
            )
            // Y label
            drawContext.canvas.nativeCanvas.drawText(
                "$score%",
                10.dp.toPx(),
                y + 4.dp.toPx(),
                android.graphics.Paint().apply {
                    color = textColor.toArgb()
                    textSize = 10.sp.toPx()
                    textAlign = android.graphics.Paint.Align.LEFT
                }
            )
        }

        // Draw X axis and plot points
        val pointCount = sorted.size
        val points = mutableListOf<androidx.compose.ui.geometry.Offset>()
        sorted.forEachIndexed { index, item ->
            val x = if (pointCount > 1) {
                paddingLeft + chartWidth * index / (pointCount - 1)
            } else {
                paddingLeft + chartWidth / 2
            }
            val y = paddingTop + chartHeight * (100 - item.scorePercentage) / 100f
            points.add(androidx.compose.ui.geometry.Offset(x, y))

            // X label (index + 1)
            drawContext.canvas.nativeCanvas.drawText(
                "N°${index + 1}",
                x,
                height - 6.dp.toPx(),
                android.graphics.Paint().apply {
                    color = textColor.toArgb()
                    textSize = 10.sp.toPx()
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            )
        }

        // Draw path connecting points
        if (points.size > 1) {
            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(points[0].x, points[0].y)
                for (i in 1 until points.size) {
                    lineTo(points[i].x, points[i].y)
                }
            }
            drawPath(
                path = path,
                color = primaryColor,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 3.dp.toPx(),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round,
                    join = androidx.compose.ui.graphics.StrokeJoin.Round
                )
            )

            // Draw shadow/gradient below the path
            val fillPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(points[0].x, points[0].y)
                for (i in 1 until points.size) {
                    lineTo(points[i].x, points[i].y)
                }
                lineTo(points.last().x, paddingTop + chartHeight)
                lineTo(points[0].x, paddingTop + chartHeight)
                close()
            }
            drawPath(
                path = fillPath,
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(primaryColor.copy(alpha = 0.25f), Color.Transparent),
                    startY = paddingTop,
                    endY = paddingTop + chartHeight
                )
            )
        }

        // Draw data point markers
        points.forEachIndexed { index, point ->
            drawCircle(
                color = secondaryColor,
                radius = 5.dp.toPx(),
                center = point
            )
            drawCircle(
                color = Color.White,
                radius = 2.5.dp.toPx(),
                center = point
            )
        }
    }
}

