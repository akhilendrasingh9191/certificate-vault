package com.example.ui.certificates

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.UserProfile

@Composable
fun ProfileDialog(
    initialProfile: UserProfile?,
    defaultEmail: String = "",
    onDismiss: () -> Unit,
    onSave: (UserProfile) -> Unit
) {
    var fullName by remember { mutableStateOf(initialProfile?.fullName ?: "") }
    var titleOrProfession by remember { mutableStateOf(initialProfile?.titleOrProfession ?: "") }
    var email by remember { mutableStateOf(initialProfile?.email.takeIf { !it.isNullOrBlank() } ?: defaultEmail) }
    var phone by remember { mutableStateOf(initialProfile?.phone ?: "") }
    var summary by remember { mutableStateOf(initialProfile?.summary ?: "") }

    var nameError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Portfolio Profile",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "This information appears in the executive header of your structured PDF export dossier.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = fullName,
                    onValueChange = {
                        fullName = it
                        if (it.isNotBlank()) nameError = false
                    },
                    label = { Text("Full Name *") },
                    placeholder = { Text("e.g. Ritika Singh") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    isError = nameError,
                    supportingText = if (nameError) {
                        { Text("Full name is required") }
                    } else null,
                    modifier = Modifier.fillMaxWidth().testTag("profile_name_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = titleOrProfession,
                    onValueChange = { titleOrProfession = it },
                    label = { Text("Headline / Professional Title") },
                    placeholder = { Text("e.g. Lead Software Engineer & Architect") },
                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Contact Email") },
                    placeholder = { Text("e.g. name@example.com") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Contact Phone") },
                    placeholder = { Text("e.g. +1 (555) 019-2834") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = summary,
                    onValueChange = { summary = it },
                    label = { Text("Executive Summary / Bio") },
                    placeholder = { Text("Brief statement summarizing your expertise and milestones...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmedName = fullName.trim()
                    if (trimmedName.isBlank()) {
                        nameError = true
                        return@Button
                    }
                    val updated = (initialProfile ?: UserProfile()).copy(
                        fullName = trimmedName,
                        titleOrProfession = titleOrProfession.trim(),
                        email = email.trim(),
                        phone = phone.trim(),
                        summary = summary.trim()
                    )
                    onSave(updated)
                },
                modifier = Modifier.testTag("save_profile_button"),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Save Profile")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
