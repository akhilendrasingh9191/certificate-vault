package com.example.ui.certificates

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Certificate
import com.example.data.model.CertificateCategories
import com.example.util.AttachmentManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CertificateEditDialog(
    initialCertificate: Certificate? = null,
    onDismiss: () -> Unit,
    onSave: (Certificate) -> Unit
) {
    val context = LocalContext.current
    val isEditMode = initialCertificate != null
    val todayFormatted = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    var title by remember { mutableStateOf(initialCertificate?.title ?: "") }
    var organization by remember { mutableStateOf(initialCertificate?.organization ?: "") }
    var category by remember { mutableStateOf(initialCertificate?.category ?: CertificateCategories.CERTIFICATION) }
    var issueDate by remember { mutableStateOf(initialCertificate?.issueDate.takeIf { !it.isNullOrBlank() } ?: todayFormatted) }
    var noExpiry by remember { mutableStateOf(initialCertificate?.expiryDate.isNullOrBlank() || initialCertificate?.expiryDate?.lowercase() in listOf("lifetime", "no expiry", "does not expire")) }
    var expiryDate by remember { mutableStateOf(if (noExpiry) "" else (initialCertificate?.expiryDate ?: "")) }
    var credentialId by remember { mutableStateOf(initialCertificate?.credentialId ?: "") }
    var credentialUrl by remember { mutableStateOf(initialCertificate?.credentialUrl ?: "") }
    var gradeOrScore by remember { mutableStateOf(initialCertificate?.gradeOrScore ?: "") }
    var description by remember { mutableStateOf(initialCertificate?.description ?: "") }
    var skillsInput by remember { mutableStateOf(initialCertificate?.skills?.joinToString(", ") ?: "") }

    // Attachment state
    var attachmentName by remember { mutableStateOf(initialCertificate?.attachmentName ?: "") }
    var attachmentType by remember { mutableStateOf(initialCertificate?.attachmentType ?: "") }
    var attachmentPath by remember { mutableStateOf(initialCertificate?.attachmentPath ?: "") }
    var attachmentSize by remember { mutableStateOf(initialCertificate?.attachmentSize ?: 0L) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val info = AttachmentManager.saveAttachment(context, uri)
            if (info != null) {
                attachmentName = info.fileName
                attachmentType = info.mimeType
                attachmentPath = info.localPath
                attachmentSize = info.sizeBytes
                Toast.makeText(context, "Attached: ${info.fileName}", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed to attach file", Toast.LENGTH_SHORT).show()
            }
        }
    }

    var titleError by remember { mutableStateOf(false) }
    var orgError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEditMode) "Edit Credential" else "Add Certificate",
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
                // Category Selector Chips
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CertificateCategories.list.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, style = MaterialTheme.typography.labelSmall) },
                            leadingIcon = if (category == cat) {
                                { Icon(Icons.Default.Check, contentDescription = null) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Certificate Title
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (it.isNotBlank()) titleError = false
                    },
                    label = { Text("Certificate / Achievement Title *") },
                    placeholder = { Text("e.g. AWS Certified Solutions Architect") },
                    isError = titleError,
                    supportingText = if (titleError) {
                        { Text("Title is required") }
                    } else null,
                    leadingIcon = { Icon(Icons.Default.Title, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("cert_title_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Issuing Organization
                OutlinedTextField(
                    value = organization,
                    onValueChange = {
                        organization = it
                        if (it.isNotBlank()) orgError = false
                    },
                    label = { Text("Issuing Organization / Institution *") },
                    placeholder = { Text("e.g. Amazon Web Services, Stanford, Google") },
                    isError = orgError,
                    supportingText = if (orgError) {
                        { Text("Issuing organization is required") }
                    } else null,
                    leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("cert_org_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Dates Row
                OutlinedTextField(
                    value = issueDate,
                    onValueChange = { issueDate = it },
                    label = { Text("Issue Date *") },
                    placeholder = { Text("YYYY-MM-DD") },
                    leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("cert_date_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // No Expiration toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = noExpiry,
                        onCheckedChange = { checked ->
                            noExpiry = checked
                            if (checked) expiryDate = ""
                        }
                    )
                    Text(
                        text = "Lifetime validity / No expiration date",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                if (!noExpiry) {
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = expiryDate,
                        onValueChange = { expiryDate = it },
                        label = { Text("Expiration Date") },
                        placeholder = { Text("YYYY-MM-DD") },
                        leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Certificate / Document Attachment Section
                Text(
                    text = "Document Attachment",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                if (attachmentName.isNotBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                val fileIcon = when {
                                    attachmentType.contains("pdf", ignoreCase = true) -> Icons.Default.PictureAsPdf
                                    attachmentType.contains("image", ignoreCase = true) -> Icons.Default.Image
                                    else -> Icons.Default.InsertDriveFile
                                }
                                Icon(
                                    imageVector = fileIcon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = attachmentName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1
                                    )
                                    val sizeStr = AttachmentManager.formatFileSize(attachmentSize)
                                    if (sizeStr.isNotBlank()) {
                                        Text(
                                            text = sizeStr,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            IconButton(
                                onClick = {
                                    attachmentName = ""
                                    attachmentType = ""
                                    attachmentPath = ""
                                    attachmentSize = 0L
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove attachment",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = {
                            filePickerLauncher.launch(
                                arrayOf("application/pdf", "image/*", "*/*")
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("attach_file_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Attach Certificate File (PDF / Image)")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Credential ID & Score
                OutlinedTextField(
                    value = credentialId,
                    onValueChange = { credentialId = it },
                    label = { Text("Credential ID / License Number (Optional)") },
                    placeholder = { Text("e.g. AWS-SAA-103982") },
                    leadingIcon = { Icon(Icons.Default.WorkspacePremium, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = gradeOrScore,
                    onValueChange = { gradeOrScore = it },
                    label = { Text("Grade / Score / Honors (Optional)") },
                    placeholder = { Text("e.g. GPA 3.9, 94%, Top 5%") },
                    leadingIcon = { Icon(Icons.Default.Star, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Verification Link
                OutlinedTextField(
                    value = credentialUrl,
                    onValueChange = { credentialUrl = it },
                    label = { Text("Verification Link / URL (Optional)") },
                    placeholder = { Text("https://...") },
                    leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Skills & Keywords
                OutlinedTextField(
                    value = skillsInput,
                    onValueChange = { skillsInput = it },
                    label = { Text("Associated Skills (Comma separated)") },
                    placeholder = { Text("e.g. Cloud, Kubernetes, Python, Machine Learning") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description & Highlights (Optional)") },
                    placeholder = { Text("Summary of coursework, honors, project work...") },
                    leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmedTitle = title.trim()
                    val trimmedOrg = organization.trim()
                    if (trimmedTitle.isBlank()) {
                        titleError = true
                        return@Button
                    }
                    if (trimmedOrg.isBlank()) {
                        orgError = true
                        return@Button
                    }

                    val parsedSkills = skillsInput
                        .split(",")
                        .map { it.trim() }
                        .filter { it.isNotBlank() }

                    val updatedCert = (initialCertificate ?: Certificate()).copy(
                        title = trimmedTitle,
                        organization = trimmedOrg,
                        category = category,
                        issueDate = issueDate.trim().ifBlank { todayFormatted },
                        expiryDate = if (noExpiry) "" else expiryDate.trim(),
                        credentialId = credentialId.trim(),
                        credentialUrl = credentialUrl.trim(),
                        gradeOrScore = gradeOrScore.trim(),
                        description = description.trim(),
                        skills = parsedSkills,
                        attachmentName = attachmentName,
                        attachmentType = attachmentType,
                        attachmentPath = attachmentPath,
                        attachmentSize = attachmentSize
                    )
                    onSave(updatedCert)
                },
                modifier = Modifier.testTag("save_cert_button"),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (isEditMode) "Save Changes" else "Add Credential")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
