package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.User
import com.example.ui.viewmodel.PairingUiState
import com.example.ui.viewmodel.PairingViewModel

@Composable
fun PairingScreen(
    currentUser: User?,
    pairingViewModel: PairingViewModel,
    onPairedSuccess: () -> Unit
) {
    var inputCode by remember { mutableStateOf("") }
    val pairingState by pairingViewModel.pairingState.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    var copyNotice by remember { mutableStateOf(false) }

    LaunchedEffect(pairingState) {
        if (pairingState is PairingUiState.Success) {
            onPairedSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Icon(
            imageVector = Icons.Default.Link,
            contentDescription = "Pairing",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Connect with Partner",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Share your unique Lik code with your partner or enter their code below.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Your code card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "YOUR LIK CODE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = currentUser?.pairCode ?: "LIK-XXXX",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = {
                        currentUser?.pairCode?.let {
                            clipboardManager.setText(AnnotatedString(it))
                            copyNotice = true
                        }
                    },
                    modifier = Modifier.testTag("copy_pair_code_btn")
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (copyNotice) "Copied to Clipboard!" else "Copy Code")
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Enter Partner's Code",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = inputCode,
            onValueChange = { inputCode = it.uppercase() },
            placeholder = { Text("e.g. LIK-1234") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("partner_code_input")
        )

        if (pairingState is PairingUiState.Error) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = (pairingState as PairingUiState.Error).error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                pairingViewModel.pairWithPartner(inputCode)
            },
            enabled = inputCode.isNotBlank() && pairingState !is PairingUiState.Loading,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("submit_pair_btn")
        ) {
            if (pairingState is PairingUiState.Loading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
            } else {
                Text("Connect Partner", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
