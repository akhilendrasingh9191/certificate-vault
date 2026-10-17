package com.example.ui.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.data.model.UserProfile
import com.example.data.repository.CertificateRepository
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    onOpenSettings: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Sign In, 1: Register
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top action bar with optional settings icon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                if (onOpenSettings != null) {
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.testTag("auth_settings_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings & Developer Info",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // App Emblem
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CardMembership,
                    contentDescription = "CertVault",
                    modifier = Modifier.size(44.dp),
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "CertVault",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Organize, Validate & Export Your Credentials",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Auth Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Sign In / Register Tabs
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.primary
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = {
                                selectedTab = 0
                                errorMessage = null
                                successMessage = null
                            },
                            text = {
                                Text(
                                    "Sign In",
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = {
                                selectedTab = 1
                                errorMessage = null
                                successMessage = null
                            },
                            text = {
                                Text(
                                    "Create Account",
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    if (errorMessage != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = errorMessage ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    if (successMessage != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = successMessage ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF065F46),
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Register: Full Name field
                    if (selectedTab == 1) {
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("Full Name") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("auth_name_input"),
                            shape = RoundedCornerShape(10.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            errorMessage = null
                        },
                        label = { Text("Email Address") },
                        placeholder = { Text("name@example.com") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("auth_email_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = null
                        },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("auth_password_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Register: Confirm Password
                    if (selectedTab == 1) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = {
                                confirmPassword = it
                                errorMessage = null
                            },
                            label = { Text("Confirm Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("auth_confirm_password_input"),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    // Forgot Password (in Sign In mode)
                    if (selectedTab == 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = {
                                    val trimmedEmail = email.trim()
                                    if (trimmedEmail.isBlank()) {
                                        errorMessage = "Please enter your email address to reset password"
                                    } else {
                                        isLoading = true
                                        coroutineScope.launch {
                                            try {
                                                Firebase.auth.sendPasswordResetEmail(trimmedEmail).await()
                                                successMessage = "Password reset instructions sent to $trimmedEmail"
                                                errorMessage = null
                                            } catch (e: Exception) {
                                                errorMessage = e.localizedMessage ?: "Failed to send reset email"
                                            } finally {
                                                isLoading = false
                                            }
                                        }
                                    }
                                }
                            ) {
                                Text("Forgot Password?", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Primary Action Button (Sign In / Register)
                    Button(
                        onClick = {
                            val trimmedEmail = email.trim()
                            val trimmedPassword = password.trim()

                            if (trimmedEmail.isBlank() || !trimmedEmail.contains("@")) {
                                errorMessage = "Please enter a valid email address"
                                return@Button
                            }
                            if (trimmedPassword.length < 6) {
                                errorMessage = "Password must be at least 6 characters"
                                return@Button
                            }

                            if (selectedTab == 1 && trimmedPassword != confirmPassword.trim()) {
                                errorMessage = "Passwords do not match"
                                return@Button
                            }

                            isLoading = true
                            errorMessage = null
                            successMessage = null

                            coroutineScope.launch {
                                try {
                                    if (selectedTab == 0) {
                                        // Email/Password Sign In
                                        Firebase.auth.signInWithEmailAndPassword(trimmedEmail, trimmedPassword).await()
                                        onAuthSuccess()
                                    } else {
                                        // Email/Password Registration
                                        val authResult = Firebase.auth.createUserWithEmailAndPassword(trimmedEmail, trimmedPassword).await()
                                        val user = authResult.user
                                        if (user != null) {
                                            val name = if (fullName.isNotBlank()) fullName.trim() else trimmedEmail.substringBefore("@")
                                            try {
                                                val profileUpdates = userProfileChangeRequest {
                                                    displayName = name
                                                }
                                                user.updateProfile(profileUpdates).await()
                                            } catch (e: Exception) {
                                                Log.w("Auth", "Failed to update auth display name: ${e.message}")
                                            }

                                            try {
                                                val repo = CertificateRepository(context.applicationContext)
                                                val initialProfile = UserProfile(
                                                    userId = user.uid,
                                                    fullName = name,
                                                    email = trimmedEmail
                                                )
                                                repo.saveUserProfile(initialProfile)
                                            } catch (e: Exception) {
                                                Log.w("Auth", "Failed to initialize Firestore user profile: ${e.message}")
                                            }
                                        }
                                        onAuthSuccess()
                                    }
                                } catch (e: Exception) {
                                    errorMessage = formatAuthError(e)
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("email_auth_submit_btn"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (selectedTab == 0) "Signing in..." else "Creating account...")
                        } else {
                            Text(
                                text = if (selectedTab == 0) "Sign In" else "Create Account",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Optional Google Sign-In divider & button
                    val hasWebClientId = remember {
                        val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
                        resId != 0
                    }

                    if (hasWebClientId) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Divider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            Text(
                                text = "  or  ",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Divider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        }
                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                isLoading = true
                                errorMessage = null
                                onGoogleSignInClicked(
                                    context = context,
                                    credentialManager = credentialManager,
                                    onAuthSuccess = {
                                        isLoading = false
                                        onAuthSuccess()
                                    },
                                    onAuthError = { msg ->
                                        isLoading = false
                                        errorMessage = msg
                                    },
                                    scope = coroutineScope,
                                    onAuthCancelled = { isLoading = false }
                                )
                            },
                            enabled = !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("google_sign_in_alt_btn"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text("Continue with Google", fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Connected to Firebase backend • Encrypted with Firestore Security Rules",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private fun formatAuthError(e: Exception): String {
    val message = e.message ?: e.toString()
    return when {
        message.contains("user-not-found", ignoreCase = true) -> "No user found with this email. Please create an account."
        message.contains("wrong-password", ignoreCase = true) || message.contains("invalid-credential", ignoreCase = true) -> "Incorrect password or credentials. Please try again."
        message.contains("email-already-in-use", ignoreCase = true) -> "This email is already registered. Please sign in instead."
        message.contains("weak-password", ignoreCase = true) -> "The password provided is too weak. Please use at least 6 characters."
        message.contains("network", ignoreCase = true) -> "Network error. Please check your internet connection."
        else -> e.localizedMessage ?: "Authentication failed. Please check your credentials."
    }
}

fun onGoogleSignInClicked(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onAuthError: (String) -> Unit,
    scope: CoroutineScope,
    onAuthCancelled: () -> Unit = {}
) {
    val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
    val clientId = if (resId != 0) context.getString(resId) else null
    if (clientId.isNullOrBlank()) {
        onAuthError("Google Sign-In is not configured in this project. Please sign in with Email & Password.")
        return
    }

    val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
    val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

    scope.launch {
        try {
            val result = credentialManager.getCredential(context as Activity, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                Firebase.auth.signInWithCredential(authCredential).await()
                onAuthSuccess()
            } else {
                onAuthError("Unexpected credential type")
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w("Auth", "Google Sign-In flow cancelled or dismissed: ${e.message}", e)
            onAuthCancelled()
        } catch (e: Exception) {
            Log.e("Auth", "Google Sign-In failed", e)
            onAuthError(e.localizedMessage ?: "Sign in failed")
        }
    }
}

fun performSignOut(
    context: Context,
    credentialManager: CredentialManager,
    onSignOutComplete: () -> Unit,
    scope: CoroutineScope
) {
    Firebase.auth.signOut()
    scope.launch {
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.e("Auth", "Failed to clear credential state", e)
        } finally {
            onSignOutComplete()
        }
    }
}
