package com.example.ui.components

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay

data class SponsorAd(
    val title: String,
    val subtitle: String,
    val description: String,
    val badge: String,
    val accentColor: Color,
    val icon: ImageVector,
    val actionUrl: String
)

val sponsorAds = listOf(
    SponsorAd(
        title = "Orange Côte d'Ivoire",
        subtitle = "Pass e-School Gratuit",
        description = "Révisez vos fiches de cours EduCI avec 0 Mo consommé ! Activez le pass éducation sur Max it.",
        badge = "Sponsor Officiel",
        accentColor = Color(0xFFFF6600),
        icon = Icons.Default.NetworkWifi,
        actionUrl = "https://www.orange.ci"
    ),
    SponsorAd(
        title = "MTN Côte d'Ivoire",
        subtitle = "Y'ello Éducation à 0 F",
        description = "Bénéficiez du portail d'apprentissage sans consommer votre forfait internet. Composez le *105#.",
        badge = "Partenaire MENA",
        accentColor = Color(0xFFFFCC00),
        icon = Icons.Default.SignalCellularAlt,
        actionUrl = "https://www.mtn.ci"
    ),
    SponsorAd(
        title = "Librairie de France Abidjan",
        subtitle = "Annales BAC 2026 Disponibles",
        description = "Préparez la mention d'excellence avec nos annales officielles conformes aux exigences du MENA CI.",
        badge = "Annonce Civile",
        accentColor = Color(0xFFD32F2F),
        icon = Icons.Default.Book,
        actionUrl = "https://www.ldfci.net"
    ),
    SponsorAd(
        title = "PassBAC CI",
        subtitle = "Abonnement Premium",
        description = "Seulement 1 000 FCFA par mois pour accéder à toutes les corrections détaillées et aux tuteurs en direct.",
        badge = "Abonnement",
        accentColor = Color(0xFF1976D2),
        icon = Icons.Default.WorkspacePremium,
        actionUrl = "https://ai.studio/build"
    ),
    SponsorAd(
        title = "Univers Nova Store",
        subtitle = "Solutions Éducatives",
        description = "Propulsé par Junior Seka, nous créons des technologies d'apprentissage pour révolutionner le BAC en Côte d'Ivoire.",
        badge = "Éditeur",
        accentColor = Color(0xFF4CAF50),
        icon = Icons.Default.TipsAndUpdates,
        actionUrl = "https://ai.studio/build"
    )
)

@Composable
fun NetworkStatusBanner(
    isOfflineMode: Boolean,
    countdownSeconds: Int,
    onToggleOffline: (Boolean) -> Unit,
    onTriggerAdNow: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("network_status_banner"),
        color = if (isOfflineMode) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = if (isOfflineMode) MaterialTheme.colorScheme.error.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isOfflineMode) Icons.Default.WifiOff else Icons.Default.Wifi,
                        contentDescription = null,
                        tint = if (isOfflineMode) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (isOfflineMode) "🔌 Mode Hors-ligne Actif" else "⚡ Connecté (Soutien Actif)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isOfflineMode) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = if (isOfflineMode) {
                            "Base locale (0% Data internet consommée)"
                        } else {
                            val minutes = countdownSeconds / 60
                            val seconds = countdownSeconds % 60
                            val formattedTime = String.format("%d:%02d", minutes, seconds)
                            "Prochaine pub de soutien dans : $formattedTime"
                        },
                        fontSize = 11.sp,
                        color = if (isOfflineMode) MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (!isOfflineMode) {
                    // Quick simulation button for testing ads
                    Button(
                        onClick = onTriggerAdNow,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier
                            .height(28.dp)
                            .testTag("test_ad_button"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayCircle,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("Simuler Pub", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Inline switch to toggle offline mode
                TextButton(
                    onClick = { onToggleOffline(!isOfflineMode) },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = if (isOfflineMode) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text(
                        text = if (isOfflineMode) "Se Connecter" else "Passer Offline",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Affiche une superbe bannière de sponsor local d'Afrique de l'Ouest.
 */
@Composable
fun BannerAdWidget(
    onTriggerAdNow: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .testTag("banner_ad_widget"),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 1.dp
    ) {
        LocalSponsorAdWidget(onTriggerAdNow = onTriggerAdNow)
    }
}

@Composable
fun LocalSponsorAdWidget(
    onTriggerAdNow: () -> Unit
) {
    val context = LocalContext.current
    var currentAdIndex by remember { mutableStateOf(0) }

    // Alternance toutes les 8 secondes
    LaunchedEffect(Unit) {
        while (true) {
            delay(8000)
            currentAdIndex = (currentAdIndex + 1) % sponsorAds.size
        }
    }

    val ad = sponsorAds[currentAdIndex]

    Row(
        modifier = Modifier
            .fillMaxSize()
            .clickable {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(ad.actionUrl))
                    context.startActivity(intent)
                } catch (e: Exception) {
                    // Fallback
                }
            }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icône du sponsor
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(ad.accentColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = ad.icon,
                contentDescription = null,
                tint = ad.accentColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = ad.title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    color = ad.accentColor,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = ad.badge,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (ad.accentColor == Color(0xFFFFCC00)) Color.Black else Color.White,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
            Text(
                text = ad.description,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }

        IconButton(
            onClick = onTriggerAdNow,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Launch,
                contentDescription = "Ouvrir",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * Gère l'affichage d'un Interstitiel de sponsor local.
 */
@Composable
fun InterstitialAdDialog(
    onDismiss: () -> Unit
) {
    LocalInterstitialAdDialog(onDismiss = onDismiss)
}

@Composable
fun LocalInterstitialAdDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val randomAd = remember { sponsorAds.random() }
    
    // Verrouille la fermeture pendant 4 secondes
    var dismissSecondsLeft by remember { mutableStateOf(4) }
    var isCloseButtonEnabled by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (dismissSecondsLeft > 0) {
            delay(1000)
            dismissSecondsLeft -= 1
        }
        isCloseButtonEnabled = true
    }

    Dialog(
        onDismissRequest = {
            if (isCloseButtonEnabled) {
                onDismiss()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = isCloseButtonEnabled,
            dismissOnClickOutside = isCloseButtonEnabled,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .testTag("interstitial_ad_dialog"),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // En-tête de l'annonce
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = randomAd.accentColor,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "SPONSOR DU BAC 🇨🇮",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (randomAd.accentColor == Color(0xFFFFCC00)) Color.Black else Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        // Bouton de fermeture ou minuteur
                        Surface(
                            color = if (isCloseButtonEnabled) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.errorContainer,
                            shape = CircleShape,
                            modifier = Modifier
                                .size(32.dp)
                                .clickable {
                                    if (isCloseButtonEnabled) {
                                        onDismiss()
                                    }
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (isCloseButtonEnabled) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Fermer la publicité",
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    Text(
                                        text = dismissSecondsLeft.toString(),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Cadre icône de la marque
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(
                                color = randomAd.accentColor.copy(alpha = 0.12f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = randomAd.icon,
                            contentDescription = null,
                            tint = randomAd.accentColor,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = randomAd.title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = randomAd.subtitle,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = randomAd.accentColor,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = randomAd.description,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // Bouton d'action
                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(randomAd.actionUrl))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Fallback
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = randomAd.accentColor),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "En Savoir Plus",
                            fontWeight = FontWeight.Bold,
                            color = if (randomAd.accentColor == Color(0xFFFFCC00)) Color.Black else Color.White,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = {
                            if (isCloseButtonEnabled) {
                                onDismiss()
                            }
                        },
                        enabled = isCloseButtonEnabled,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    ) {
                        Text(
                            text = if (isCloseButtonEnabled) "Fermer la publicité" else "Fermeture autorisée dans $dismissSecondsLeft s",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Les revenus publicitaires soutiennent Univers Nova Store (Junior Seka) pour garder l'application 100% gratuite.",
                        fontSize = 9.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        lineHeight = 12.sp
                    )
                }
            }
        }
    }
}

/**
 * Simule une publicité vidéo récompensée (Rewarded Ad) d'Uptodown Ads / Sponsor local.
 * L'élève regarde une vidéo de 4 secondes pour débloquer un indice de quiz ou doubler ses récompenses.
 */
@Composable
fun RewardedAdDialog(
    onRewardGranted: () -> Unit,
    onDismiss: () -> Unit
) {
    val randomAd = remember { sponsorAds.random() }
    var secondsLeft by remember { mutableStateOf(4) }
    var isFinished by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (secondsLeft > 0) {
            delay(1000)
            secondsLeft -= 1
        }
        isFinished = true
        onRewardGranted()
    }

    Dialog(
        onDismissRequest = {
            if (isFinished) {
                onDismiss()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = isFinished,
            dismissOnClickOutside = isFinished,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .testTag("rewarded_ad_dialog"),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header with reward indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = randomAd.accentColor,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayCircle,
                                    contentDescription = null,
                                    tint = if (randomAd.accentColor == Color(0xFFFFCC00)) Color.Black else Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "SPOT SPONSORISÉ 📺",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (randomAd.accentColor == Color(0xFFFFCC00)) Color.Black else Color.White
                                )
                            }
                        }

                        // Countdown badge
                        Surface(
                            color = if (isFinished) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                            shape = CircleShape,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (isFinished) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Récompense débloquée !",
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                } else {
                                    Text(
                                        text = secondsLeft.toString(),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Simulated Video Player container
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .background(Color.Black, RoundedCornerShape(16.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            if (!isFinished) {
                                CircularProgressIndicator(
                                    color = randomAd.accentColor,
                                    strokeWidth = 3.dp,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Lecture du spot sponsorisé...",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Stars,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD54F),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "RÉCOMPENSE PRÊTE !",
                                    color = Color(0xFF81C784),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Soutenu par ${randomAd.title}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = randomAd.subtitle,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = randomAd.accentColor
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = randomAd.description,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onDismiss,
                        enabled = isFinished,
                        colors = ButtonDefaults.buttonColors(containerColor = if (isFinished) randomAd.accentColor else Color.Gray.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isFinished) "Obtenir ma Récompense 🎁" else "Regarde la vidéo ($secondsLeft s)",
                            fontWeight = FontWeight.Bold,
                            color = if (isFinished && randomAd.accentColor == Color(0xFFFFCC00)) Color.Black else Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
