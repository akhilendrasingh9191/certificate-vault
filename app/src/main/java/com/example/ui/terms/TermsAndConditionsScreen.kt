package com.example.ui.terms

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun TermsAndConditionsScreen(
    onAcceptTerms: () -> Unit
) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    var hasReachedBottom by remember { mutableStateOf(false) }

    // Dynamic scroll listener: precisely checks when user has scrolled all the way to the bottom
    val isScrolledToBottom by remember {
        derivedStateOf {
            if (scrollState.maxValue == 0) {
                false
            } else {
                !scrollState.canScrollForward || scrollState.value >= (scrollState.maxValue - 24)
            }
        }
    }

    LaunchedEffect(isScrolledToBottom) {
        if (isScrolledToBottom) {
            hasReachedBottom = true
        }
    }

    val scrollPercent by remember {
        derivedStateOf {
            if (scrollState.maxValue > 0) {
                ((scrollState.value.toFloat() / scrollState.maxValue.toFloat()) * 100).toInt().coerceIn(0, 100)
            } else 0
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Gavel,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "Terms & Conditions",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "CertVault User Service Agreement",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Scroll helper badge with dynamic scroll progress
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                shape = RoundedCornerShape(10.dp),
                color = if (hasReachedBottom) {
                    Color(0xFFECFDF5)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (hasReachedBottom) Icons.Default.CheckCircle else Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = if (hasReachedBottom) Color(0xFF059669) else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (hasReachedBottom) {
                                "Completed reading (100%). Agreement unlocked!"
                            } else {
                                "Scroll down to bottom to accept terms ($scrollPercent% read)"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (hasReachedBottom) Color(0xFF065F46) else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (hasReachedBottom) FontWeight.SemiBold else FontWeight.Normal,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (!hasReachedBottom && scrollState.maxValue > 0) {
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { scrollPercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Scrollable Terms Document
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp)
                        .verticalScroll(scrollState)
                        .testTag("terms_scroll_container")
                ) {
                    Text(
                        text = "TERMS OF SERVICE & PRIVACY AGREEMENT",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Effective Date: October 2026 • Version 1.0.0",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    TermsSection(
                        number = "1",
                        title = "Acceptance of Terms",
                        content = "By accessing, downloading, or using CertVault ('the Application'), you signify your explicit consent to be bound by these Terms and Conditions and our Privacy Policy. If you do not agree with any portion of these terms, you must refrain from using the Application. CertVault is developed and maintained by Akhilendra Singh."
                    )

                    TermsSection(
                        number = "2",
                        title = "Purpose and User Responsibilities",
                        content = "CertVault is designed as a personal credential repository, allowing individuals to record, organize, store, and export their certificates, academic degrees, professional licenses, and achievements into structured PDF dossiers. You warrant that all credentials and documents uploaded by you represent authentic, valid qualifications belonging to you or entities you are legally authorized to represent."
                    )

                    TermsSection(
                        number = "3",
                        title = "Security & Cloud Storage",
                        content = "User accounts and data records are securely synchronized with Google Cloud Firestore and Firebase Authentication. While we apply robust industry-standard access control rules, you remain responsible for maintaining the confidentiality of your credentials and devices. You agree not to upload fraudulent, malicious, copyright-infringing, or defamatory content."
                    )

                    TermsSection(
                        number = "4",
                        title = "File Attachments & Privacy",
                        content = "CertVault enables you to attach digital documents and scans (such as PDF files and images). These attachments are stored directly within your application sandbox and isolated storage. We do not sell, rent, or monetize your personal documents, certificates, or contact details to any third-party advertisers."
                    )

                    TermsSection(
                        number = "5",
                        title = "PDF Dossier Export & Sharing",
                        content = "The PDF export functionality compiles your provided achievements into an executive portfolio document. You are entirely responsible for the distribution, sharing, and transmission of generated PDF files to external parties, recruiters, or employers."
                    )

                    TermsSection(
                        number = "6",
                        title = "Intellectual Property Rights",
                        content = "All user-uploaded credentials remain your sole property. The interface design, brand identity, layout engines, and code of CertVault are the intellectual property of the developer, Akhilendra Singh. Unauthorized reverse engineering, scraping, or distribution is prohibited."
                    )

                    TermsSection(
                        number = "7",
                        title = "Limitation of Liability",
                        content = "The Application is provided on an 'as-is' and 'as-available' basis without warranties of any kind. Under no circumstances shall the developer be liable for incidental, special, or consequential damages resulting from the use or inability to use the Application, including loss of data or verification discrepancies with third-party issuing authorities."
                    )

                    TermsSection(
                        number = "8",
                        title = "Contact & Dispute Resolution",
                        content = "For support, feedback, reviews, suggestions, or complaints, users may directly contact Akhilendra Singh via email at akhilendrasingh9191@gmail.com."
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "End of Agreement • Thank you for choosing CertVault.",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // Bottom Agree and Continue Action Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Button(
                    onClick = onAcceptTerms,
                    enabled = hasReachedBottom,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("agree_continue_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                        disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                ) {
                    Text(
                        text = if (hasReachedBottom) "Agree and Continue" else "Scroll to Read All Terms",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "By continuing, you confirm your acceptance of the CertVault service terms.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun TermsSection(
    number: String,
    title: String,
    content: String
) {
    Column(modifier = Modifier.padding(bottom = 14.dp)) {
        Text(
            text = "$number. $title",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = content,
            style = MaterialTheme.typography.bodySmall,
            lineHeight = 18.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
