package com.example

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import com.example.data.repository.CertificateRepository
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.performSignOut
import com.example.ui.certificates.CertificateListScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.terms.TermsAndConditionsScreen
import com.example.ui.theme.CertVaultTheme
import com.example.ui.viewmodel.CertificateViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CertVaultTheme {
                AppContent()
            }
        }
    }
}

@Composable
fun AppContent() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    val prefs = remember { context.getSharedPreferences("certvault_prefs", Context.MODE_PRIVATE) }

    var termsAccepted by remember {
        mutableStateOf(prefs.getBoolean("terms_accepted", false))
    }
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }
    var showSettings by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    if (!termsAccepted) {
        // Step 1: Mandatory Terms & Conditions Screen with dynamic scroll completion
        TermsAndConditionsScreen(
            onAcceptTerms = {
                prefs.edit().putBoolean("terms_accepted", true).apply()
                termsAccepted = true
            }
        )
    } else if (showSettings) {
        // Top Bar Settings & Developer Info Screen
        BackHandler {
            showSettings = false
        }
        SettingsScreen(
            onNavigateBack = {
                showSettings = false
            }
        )
    } else if (currentUser == null) {
        // Step 2: Authentication Screen (Email/Password & Account Creation via Firebase)
        AuthScreen(
            onAuthSuccess = {
                currentUser = Firebase.auth.currentUser
            },
            onOpenSettings = {
                showSettings = true
            }
        )
    } else {
        val user = currentUser!!
        // Step 3: Main Certificate Vault Dashboard
        val repository = remember(user.uid) {
            CertificateRepository(context.applicationContext)
        }
        val viewModel = remember(user.uid) {
            CertificateViewModel(repository, user.uid)
        }

        CertificateListScreen(
            viewModel = viewModel,
            userEmail = user.email ?: "user@certvault.app",
            onOpenSettings = {
                showSettings = true
            },
            onSignOut = {
                performSignOut(
                    context = context,
                    credentialManager = credentialManager,
                    onSignOutComplete = {
                        currentUser = null
                        showSettings = false
                    },
                    scope = coroutineScope
                )
            }
        )
    }
}
