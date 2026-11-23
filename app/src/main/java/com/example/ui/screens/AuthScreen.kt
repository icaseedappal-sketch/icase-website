package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.example.data.GoogleAuthHelper
import com.example.ui.components.ICaseLogo
import com.example.ui.theme.LocalResponsiveSpec
import com.example.ui.theme.iCaseBlue
import com.example.ui.theme.iCaseGreen
import com.example.ui.theme.iCaseNavy
import com.example.ui.theme.iCaseRed
import com.google.firebase.auth.FirebaseUser

@Composable
fun AuthScreen(
    isCheckingInitialSession: Boolean,
    onAuthSuccess: (FirebaseUser) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val spec = LocalResponsiveSpec.current

    var isSigningIn by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF071220),
                        iCaseNavy,
                        Color(0xFF162944)
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(spec.horizontalPadding)
            .testTag("auth_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Brand Pill
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 6.dp
            ) {
                Box(
                    modifier = Modifier.padding(
                        horizontal = if (spec.isCompactSmall) 18.dp else 24.dp,
                        vertical = if (spec.isCompactSmall) 10.dp else 14.dp
                    )
                ) {
                    ICaseLogo(
                        isDarkBackground = false,
                        scale = if (spec.isCompactSmall) 1.05f else 1.25f
                    )
                }
            }

            Spacer(modifier = Modifier.height(if (spec.isCompactSmall) 16.dp else 24.dp))

            Text(
                text = "Welcome to iCASE",
                fontSize = spec.sp(25),
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Genuine mobile accessories, 1-hour doorstep delivery & certified smartphone repair in Kerala.",
                fontSize = spec.sp(14),
                color = Color(0xFFCBD5E1),
                textAlign = TextAlign.Center,
                lineHeight = spec.sp(20)
            )

            Spacer(modifier = Modifier.height(if (spec.isCompactSmall) 16.dp else 24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(spec.cardPadding),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Sign in to Continue",
                        fontSize = spec.sp(19),
                        fontWeight = FontWeight.Bold,
                        color = iCaseNavy
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Sync your orders, repair pickups, addresses, and wishlist securely across your devices.",
                        fontSize = spec.sp(13),
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center,
                        lineHeight = spec.sp(18)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    AuthFeatureRow(
                        icon = Icons.Default.ElectricBolt,
                        tint = iCaseRed,
                        title = "Express 1-Hour Local Delivery",
                        subtitle = "Fast accessory delivery straight to your doorstep"
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    AuthFeatureRow(
                        icon = Icons.Default.Build,
                        tint = iCaseBlue,
                        title = "Doorstep Device Repair Pickup",
                        subtitle = "Live technician tracking from pickup to return"
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    AuthFeatureRow(
                        icon = Icons.Default.VerifiedUser,
                        tint = iCaseGreen,
                        title = "Private Account Sync",
                        subtitle = "Your personal order history stays private to your Google account"
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    if (errorMessage != null) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            color = Color(0xFFFEE2E2),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = errorMessage!!,
                                fontSize = 12.sp,
                                color = Color(0xFF991B1B),
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    if (isCheckingInitialSession) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 52.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.5.dp,
                                color = iCaseRed
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Checking your session...",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = iCaseNavy
                            )
                        }
                    } else {
                        Button(
                            onClick = {
                                if (isSigningIn) return@Button
                                errorMessage = null
                                isSigningIn = true
                                GoogleAuthHelper.onGoogleSignInClicked(
                                    context = context,
                                    credentialManager = credentialManager,
                                    scope = scope,
                                    onAuthSuccess = { user ->
                                        isSigningIn = false
                                        onAuthSuccess(user)
                                    },
                                    onAuthError = { err ->
                                        isSigningIn = false
                                        errorMessage = err
                                    },
                                    onAuthCancelled = {
                                        isSigningIn = false
                                    }
                                )
                            },
                            enabled = !isSigningIn,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = iCaseRed,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(26.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 52.dp)
                                .testTag("auth_google_signin_button")
                        ) {
                            if (isSigningIn) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.5.dp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Signing in...",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(Color.White),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "G",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Black,
                                        color = iCaseRed
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Sign in with Google",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AuthFeatureRow(
    icon: ImageVector,
    tint: Color,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF8FAFC))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = iCaseNavy
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = Color(0xFF64748B)
            )
        }
    }
}
