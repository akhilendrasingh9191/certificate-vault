package com.example.ui.certificates

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Certificate
import com.example.data.model.UserProfile
import com.example.util.PdfExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun PdfExportDialog(
    profile: UserProfile?,
    allCertificates: List<Certificate>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var dossierTitle by remember { mutableStateOf("Verified Credentials & Achievements Dossier") }
    var selectedCertIds by remember { mutableStateOf(allCertificates.map { it.id }.toSet()) }

    var isGenerating by remember { mutableStateOf(false) }
    var generatedFile by remember { mutableStateOf<File?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Export Structured PDF",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (generatedFile != null) {
                    // Success View
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "PDF Dossier Generated!",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${selectedCertIds.size} certificates compiled into structured PDF",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                generatedFile?.let { file ->
                                    PdfExporter.sharePdf(context, file)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("share_pdf_btn"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share PDF")
                        }

                        OutlinedButton(
                            onClick = {
                                generatedFile?.let { file ->
                                    PdfExporter.openPdf(context, file)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("open_pdf_btn"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("View PDF")
                        }
                    }
                } else {
                    // Configuration View
                    Text(
                        text = "Customize your portfolio dossier before exporting as a structured, professional document.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = dossierTitle,
                        onValueChange = { dossierTitle = it },
                        label = { Text("Dossier Title") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Select All Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Include Credentials (${selectedCertIds.size}/${allCertificates.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        TextButton(
                            onClick = {
                                selectedCertIds = if (selectedCertIds.size == allCertificates.size) {
                                    emptySet()
                                } else {
                                    allCertificates.map { it.id }.toSet()
                                }
                            }
                        ) {
                            Text(if (selectedCertIds.size == allCertificates.size) "Deselect All" else "Select All")
                        }
                    }

                    // Certificate Checklist
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    ) {
                        items(allCertificates, key = { it.id }) { cert ->
                            val isSelected = selectedCertIds.contains(cert.id)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { checked ->
                                        selectedCertIds = if (checked) {
                                            selectedCertIds + cert.id
                                        } else {
                                            selectedCertIds - cert.id
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = cert.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "${cert.organization} • ${cert.issueDate}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (generatedFile == null) {
                Button(
                    onClick = {
                        if (selectedCertIds.isEmpty()) {
                            Toast.makeText(context, "Please select at least one certificate", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isGenerating = true
                        coroutineScope.launch {
                            val selectedList = allCertificates.filter { selectedCertIds.contains(it.id) }
                            val file = withContext(Dispatchers.IO) {
                                PdfExporter.generatePortfolioPdf(
                                    context = context,
                                    profile = profile,
                                    certificates = selectedList,
                                    portfolioTitle = dossierTitle
                                )
                            }
                            isGenerating = false
                            if (file != null) {
                                generatedFile = file
                            } else {
                                Toast.makeText(context, "Failed to generate PDF", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    enabled = !isGenerating && selectedCertIds.isNotEmpty(),
                    modifier = Modifier.testTag("generate_pdf_button"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Compiling PDF...")
                    } else {
                        Text("Generate PDF")
                    }
                }
            } else {
                Button(onClick = onDismiss) {
                    Text("Done")
                }
            }
        },
        dismissButton = {
            if (generatedFile == null) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}
