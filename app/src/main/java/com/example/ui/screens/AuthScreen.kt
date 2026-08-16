package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.LikRosePrimary
import com.example.ui.viewmodel.AuthUiState
import com.example.ui.viewmodel.AuthViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun AuthScreen(
    authViewModel: AuthViewModel,
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    val uiState by authViewModel.uiState.collectAsState()
    val usernameAvailability by authViewModel.usernameAvailability.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) } // 0 = LOGIN, 1 = REGISTER

    // Login state
    var loginUsername by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var loginPasswordVisible by remember { mutableStateOf(false) }

    // Register state
    var regUsername by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regPasswordVisible by remember { mutableStateOf(false) }
    var regDob by remember { mutableStateOf("") }

    // DatePicker setup for Date of Birth
    val calendar = Calendar.getInstance()
    // Default to ~20 years ago for good UX
    calendar.add(Calendar.YEAR, -20)
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedCal = Calendar.getInstance()
                selectedCal.set(year, month, dayOfMonth)
                val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                regDob = format.format(selectedCal.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    // React to username changes to check availability on register tab
    LaunchedEffect(regUsername) {
        if (selectedTabIndex == 1 && regUsername.trim().length >= 2) {
            authViewModel.checkUsernameAvailability(regUsername)
        } else {
            authViewModel.clearUsernameAvailability()
        }
    }

    // Notify parent when authenticated
    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Authenticated) {
            onAuthSuccess()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            LikRosePrimary.copy(alpha = 0.12f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Official Lik App Logo
                Image(
                    painter = painterResource(id = R.drawable.ic_lik_logo),
                    contentDescription = "Lik App Logo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(88.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .testTag("app_logo_image")
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Lik",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )

                // Tagline
                Text(
                    text = "Be Happy 😊✨",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                )

                Spacer(modifier = Modifier.height(28.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Tab Selector: LOGIN | REGISTER
                        TabRow(
                            selectedTabIndex = selectedTabIndex,
                            containerColor = Color.Transparent,
                            contentColor = LikRosePrimary,
                            indicator = { tabPositions ->
                                TabRowDefaults.SecondaryIndicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                    color = LikRosePrimary
                                )
                            },
                            divider = {}
                        ) {
                            Tab(
                                selected = selectedTabIndex == 0,
                                onClick = {
                                    selectedTabIndex = 0
                                    authViewModel.clearError()
                                },
                                text = {
                                    Text(
                                        "LOGIN",
                                        fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 15.sp
                                    )
                                },
                                modifier = Modifier.testTag("login_tab")
                            )
                            Tab(
                                selected = selectedTabIndex == 1,
                                onClick = {
                                    selectedTabIndex = 1
                                    authViewModel.clearError()
                                },
                                text = {
                                    Text(
                                        "REGISTER",
                                        fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 15.sp
                                    )
                                },
                                modifier = Modifier.testTag("register_tab")
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // LOGIN TAB CONTENT
                        if (selectedTabIndex == 0) {
                            OutlinedTextField(
                                value = loginUsername,
                                onValueChange = { loginUsername = it },
                                label = { Text("Username") },
                                leadingIcon = {
                                    Icon(Icons.Default.Person, contentDescription = null)
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("login_username_field"),
                                shape = RoundedCornerShape(16.dp)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            OutlinedTextField(
                                value = loginPassword,
                                onValueChange = { loginPassword = it },
                                label = { Text("Password") },
                                leadingIcon = {
                                    Icon(Icons.Default.Lock, contentDescription = null)
                                },
                                trailingIcon = {
                                    IconButton(onClick = { loginPasswordVisible = !loginPasswordVisible }) {
                                        Icon(
                                            imageVector = if (loginPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = if (loginPasswordVisible) "Hide password" else "Show password"
                                        )
                                    }
                                },
                                visualTransformation = if (loginPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("login_password_field"),
                                shape = RoundedCornerShape(16.dp)
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = {
                                    authViewModel.login(loginUsername, loginPassword)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("login_submit_button"),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = LikRosePrimary)
                            ) {
                                if (uiState is AuthUiState.Loading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Login", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(Icons.Default.ArrowForward, contentDescription = null)
                                    }
                                }
                            }
                        }

                        // REGISTER TAB CONTENT
                        if (selectedTabIndex == 1) {
                            OutlinedTextField(
                                value = regUsername,
                                onValueChange = { regUsername = it },
                                label = { Text("Username") },
                                leadingIcon = {
                                    Icon(Icons.Default.Person, contentDescription = null)
                                },
                                trailingIcon = {
                                    if (regUsername.trim().length >= 2 && usernameAvailability != null) {
                                        if (usernameAvailability == true) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = "Username available",
                                                tint = Color(0xFF4CAF50)
                                            )
                                        } else {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Username taken",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                },
                                supportingText = {
                                    if (regUsername.trim().length >= 2 && usernameAvailability != null) {
                                        if (usernameAvailability == true) {
                                            Text("Username is available", color = Color(0xFF4CAF50))
                                        } else {
                                            Text("Username is already taken.", color = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                },
                                isError = usernameAvailability == false,
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("register_username_field"),
                                shape = RoundedCornerShape(16.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = regPassword,
                                onValueChange = { regPassword = it },
                                label = { Text("Create Password") },
                                leadingIcon = {
                                    Icon(Icons.Default.Lock, contentDescription = null)
                                },
                                trailingIcon = {
                                    IconButton(onClick = { regPasswordVisible = !regPasswordVisible }) {
                                        Icon(
                                            imageVector = if (regPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = if (regPasswordVisible) "Hide password" else "Show password"
                                        )
                                    }
                                },
                                visualTransformation = if (regPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("register_password_field"),
                                shape = RoundedCornerShape(16.dp)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Date of Birth Picker Field
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { datePickerDialog.show() }
                            ) {
                                OutlinedTextField(
                                    value = regDob,
                                    onValueChange = {},
                                    readOnly = true,
                                    enabled = false,
                                    label = { Text("Date of Birth") },
                                    placeholder = { Text("YYYY-MM-DD") },
                                    leadingIcon = {
                                        Icon(Icons.Default.CalendarToday, contentDescription = null)
                                    },
                                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                                        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("register_dob_field"),
                                    shape = RoundedCornerShape(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = {
                                    authViewModel.register(regUsername, regPassword, regDob)
                                },
                                enabled = usernameAvailability != false,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("register_submit_button"),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = LikRosePrimary)
                            ) {
                                if (uiState is AuthUiState.Loading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Create Account", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(Icons.Default.ArrowForward, contentDescription = null)
                                    }
                                }
                            }
                        }

                        // Error notification message
                        if (uiState is AuthUiState.Error) {
                            val err = (uiState as AuthUiState.Error).message
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = err,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
