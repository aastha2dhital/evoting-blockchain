package com.example.evotingmobileapp.screens

import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.evotingmobileapp.BuildConfig
import com.example.evotingmobileapp.auth.AuthSessionViewModel
import com.example.evotingmobileapp.navigation.AppRoutes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.security.SecureRandom

private const val OTP_VALIDITY_MILLIS = 5 * 60 * 1000L
private const val OTP_RESEND_COOLDOWN_MILLIS = 30 * 1000L

@Composable
fun AdminLoginScreen(
    navController: NavHostController,
    authSessionViewModel: AuthSessionViewModel
) {
    val firebaseAuth = remember { FirebaseAuth.getInstance() }
    val httpClient = remember { OkHttpClient() }
    val mainHandler = remember { Handler(Looper.getMainLooper()) }
    val secureRandom = remember { SecureRandom() }

    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var otpCode by rememberSaveable { mutableStateOf("") }
    var generatedOtp by rememberSaveable { mutableStateOf<String?>(null) }
    var otpExpiresAt by rememberSaveable { mutableLongStateOf(0L) }
    var lastOtpSentAt by rememberSaveable { mutableLongStateOf(0L) }
    var otpAttempts by rememberSaveable { mutableIntStateOf(0) }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var isBusy by rememberSaveable { mutableStateOf(false) }
    var otpSent by rememberSaveable { mutableStateOf(false) }

    var statusMessage by rememberSaveable {
        mutableStateOf("Sign in with the authorised polling officer account. A 6-digit email OTP is required before dashboard access.")
    }

    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }

    fun completeAdminLogin() {
        authSessionViewModel.disconnectWallet()
        authSessionViewModel.connectWallet(BuildConfig.ADMIN_WALLET_ADDRESS)
        authSessionViewModel.selectAdminRole()

        navController.navigate(AppRoutes.ADMIN_DASHBOARD) {
            popUpTo(AppRoutes.LOGIN) { inclusive = true }
        }
    }

    fun isAuthorisedAdminEmail(value: String): Boolean {
        return value.trim().equals(BuildConfig.ADMIN_EMAIL, ignoreCase = true)
    }

    fun createOtp(): String {
        return secureRandom.nextInt(900_000).plus(100_000).toString()
    }

    fun sendOtpEmail(code: String) {
        val payload = JSONObject()
            .put("service_id", BuildConfig.EMAILJS_SERVICE_ID)
            .put("template_id", BuildConfig.EMAILJS_TEMPLATE_ID)
            .put("user_id", BuildConfig.EMAILJS_PUBLIC_KEY)
            .put(
                "template_params",
                JSONObject()
                    .put("otp_code", code)
                    .put("to_email", BuildConfig.ADMIN_EMAIL)
                    .put("admin_email", BuildConfig.ADMIN_EMAIL)
                    .put("app_name", "SecureVote Nepal")
            )

        val requestBody = payload
            .toString()
            .toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("https://api.emailjs.com/api/v1.0/email/send")
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .build()

        isBusy = true
        errorMessage = null
        statusMessage = "Sending a 6-digit OTP to the registered admin email..."

        httpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                mainHandler.post {
                    isBusy = false
                    generatedOtp = null
                    otpSent = false
                    statusMessage = "Could not send the admin OTP email."
                    errorMessage = e.localizedMessage
                        ?: "Network error while sending OTP. Check internet connection and try again."
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseText = response.body?.string().orEmpty()
                val successful = response.isSuccessful
                response.close()

                mainHandler.post {
                    isBusy = false

                    if (successful) {
                        generatedOtp = code
                        otpExpiresAt = System.currentTimeMillis() + OTP_VALIDITY_MILLIS
                        lastOtpSentAt = System.currentTimeMillis()
                        otpAttempts = 0
                        otpSent = true
                        otpCode = ""
                        statusMessage = "OTP sent. Enter the 6-digit code to open the admin dashboard."
                        errorMessage = null
                    } else {
                        generatedOtp = null
                        otpSent = false
                        statusMessage = "EmailJS could not send the OTP email."
                        errorMessage = if (responseText.isNotBlank()) {
                            "EmailJS error: $responseText"
                        } else {
                            "EmailJS request failed. Check service ID, template ID, public key, and template settings."
                        }
                    }
                }
            }
        })
    }

    fun requestOtpAfterFirebaseLogin(forceResend: Boolean = false) {
        val cleanEmail = email.trim()

        when {
            cleanEmail.isBlank() -> {
                errorMessage = "Enter the authorised admin email address."
                return
            }

            password.isBlank() -> {
                errorMessage = "Enter the admin password."
                return
            }

            !isAuthorisedAdminEmail(cleanEmail) -> {
                errorMessage = "This email is not authorised for admin access."
                statusMessage = "Only the registered polling officer account can access this portal."
                return
            }
        }

        val now = System.currentTimeMillis()
        if (forceResend && now - lastOtpSentAt < OTP_RESEND_COOLDOWN_MILLIS) {
            errorMessage = "Please wait a few seconds before requesting another OTP."
            return
        }

        isBusy = true
        errorMessage = null
        statusMessage = "Verifying admin email and password..."

        firebaseAuth.signInWithEmailAndPassword(cleanEmail, password)
            .addOnCompleteListener { task ->
                isBusy = false

                if (!task.isSuccessful) {
                    generatedOtp = null
                    otpSent = false

                    errorMessage = when (val exception = task.exception) {
                        is FirebaseAuthInvalidUserException ->
                            "No admin account was found for this email. Check the Firebase user account."

                        is FirebaseAuthInvalidCredentialsException ->
                            "Invalid admin email or password. Please check your credentials."

                        else ->
                            exception?.localizedMessage
                                ?: "Admin sign-in failed. Please check your email and password."
                    }

                    statusMessage = "Firebase admin authentication could not be completed."
                    return@addOnCompleteListener
                }

                val signedInEmail = firebaseAuth.currentUser?.email.orEmpty()

                if (!isAuthorisedAdminEmail(signedInEmail)) {
                    firebaseAuth.signOut()
                    generatedOtp = null
                    otpSent = false
                    statusMessage = "Only the registered polling officer account can access this portal."
                    errorMessage = "This Firebase account is not authorised for admin access."
                    return@addOnCompleteListener
                }

                sendOtpEmail(createOtp())
            }
    }

    fun verifyOtpAndOpenDashboard() {
        val expectedOtp = generatedOtp
        val cleanOtp = otpCode.trim()
        val now = System.currentTimeMillis()

        when {
            expectedOtp.isNullOrBlank() || !otpSent -> {
                errorMessage = "Request an OTP before verifying."
            }

            now > otpExpiresAt -> {
                generatedOtp = null
                otpSent = false
                otpCode = ""
                statusMessage = "The admin OTP has expired."
                errorMessage = "Please request a new OTP and try again."
            }

            cleanOtp.length != 6 -> {
                errorMessage = "Enter the 6-digit OTP sent to the admin email."
            }

            otpAttempts >= 4 -> {
                generatedOtp = null
                otpSent = false
                otpCode = ""
                statusMessage = "Too many incorrect OTP attempts."
                errorMessage = "Please request a new OTP."
            }

            cleanOtp == expectedOtp -> {
                statusMessage = "Admin OTP verified successfully."
                errorMessage = null
                generatedOtp = null
                otpSent = false
                completeAdminLogin()
            }

            else -> {
                otpAttempts += 1
                errorMessage = "Incorrect OTP. Please check the email and try again."
                statusMessage = "OTP verification failed."
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            isBusy = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f),
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.14f)
                    )
                )
            )
    ) {
        AdminDecorativeBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
                .padding(top = 16.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AdminTopBar()

            AdminHeroCard()

            AdminEmailOtpCard(
                authorisedEmail = BuildConfig.ADMIN_EMAIL,
                email = email,
                password = password,
                otpCode = otpCode,
                passwordVisible = passwordVisible,
                statusMessage = statusMessage,
                errorMessage = errorMessage,
                isBusy = isBusy,
                otpSent = otpSent,
                onEmailChange = { value ->
                    email = value.trim()
                    errorMessage = null
                },
                onPasswordChange = { value ->
                    password = value
                    errorMessage = null
                },
                onOtpChange = { value ->
                    otpCode = value.filter { it.isDigit() }.take(6)
                    errorMessage = null
                },
                onTogglePasswordVisibility = {
                    passwordVisible = !passwordVisible
                },
                onRequestOtp = { requestOtpAfterFirebaseLogin(forceResend = false) },
                onResendOtp = { requestOtpAfterFirebaseLogin(forceResend = true) },
                onVerifyOtp = { verifyOtpAndOpenDashboard() },
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
private fun AdminDecorativeBackground() {
    Box(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier
                .size(190.dp)
                .align(Alignment.TopEnd)
                .offset(x = 70.dp, y = 78.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        ) {}

        Surface(
            modifier = Modifier
                .size(150.dp)
                .align(Alignment.TopStart)
                .offset(x = (-62).dp, y = 300.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.07f)
        ) {}

        Surface(
            modifier = Modifier
                .size(140.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 54.dp, y = (-70).dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.10f)
        ) {}
    }
}

@Composable
private fun AdminTopBar() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(999.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
            tonalElevation = 2.dp,
            shadowElevation = 3.dp,
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.26f)
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AdminCircleIcon(text = "SV")

                Text(
                    text = "SecureVote Nepal",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(999.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.74f),
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
            )
        ) {
            Text(
                text = "ADMIN",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun AdminCircleIcon(text: String) {
    Surface(
        modifier = Modifier.size(36.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun AdminHeroCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.86f),
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.36f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            AdminHeroDecorations()

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                            border = BorderStroke(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                            )
                        ) {
                            Text(
                                text = "SECURE POLLING OFFICER ACCESS",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Text(
                            text = "Admin Control Portal",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "Create elections, manage QR check-ins, monitor turnout, and close results from a protected admin session.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Surface(
                        modifier = Modifier.size(64.dp),
                        shape = RoundedCornerShape(22.dp),
                        color = MaterialTheme.colorScheme.primary,
                        shadowElevation = 4.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "AD",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AdminHeroChip(
                        modifier = Modifier.weight(1f),
                        title = "Firebase",
                        value = "Login"
                    )

                    AdminHeroChip(
                        modifier = Modifier.weight(1f),
                        title = "Email",
                        value = "OTP"
                    )

                    AdminHeroChip(
                        modifier = Modifier.weight(1f),
                        title = "Wallet",
                        value = "Admin"
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminHeroDecorations() {
    Box(modifier = Modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier
                .size(118.dp)
                .align(Alignment.TopEnd)
                .offset(x = 46.dp, y = (-50).dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        ) {}

        Surface(
            modifier = Modifier
                .size(82.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-42).dp, y = 98.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
        ) {}
    }
}

@Composable
private fun AdminHeroChip(
    modifier: Modifier = Modifier,
    title: String,
    value: String
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        ),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AdminEmailOtpCard(
    authorisedEmail: String,
    email: String,
    password: String,
    otpCode: String,
    passwordVisible: Boolean,
    statusMessage: String,
    errorMessage: String?,
    isBusy: Boolean,
    otpSent: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onOtpChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onRequestOtp: () -> Unit,
    onResendOtp: () -> Unit,
    onVerifyOtp: () -> Unit,
    onBack: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.30f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.26f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            SectionTitle(
                label = "FIREBASE + EMAIL OTP",
                title = "Verify Admin Identity",
                subtitle = "Sign in with the authorised account, then enter the OTP sent to the registered admin email."
            )

            AdminStepRow(otpSent = otpSent)

            StatusPanel(
                text = statusMessage,
                isError = false
            )

            errorMessage?.let { message ->
                StatusPanel(
                    text = message,
                    isError = true
                )
            }

            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = "Admin email") },
                placeholder = { Text(text = authorisedEmail) },
                singleLine = true,
                enabled = !isBusy && !otpSent,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                ),
                shape = RoundedCornerShape(20.dp)
            )

            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = "Password") },
                singleLine = true,
                enabled = !isBusy && !otpSent,
                visualTransformation = if (passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    TextButton(
                        onClick = onTogglePasswordVisibility,
                        enabled = !isBusy
                    ) {
                        Text(
                            text = if (passwordVisible) "HIDE" else "SHOW",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                ),
                shape = RoundedCornerShape(20.dp)
            )

            if (otpSent) {
                OtpEntrySection(
                    otpCode = otpCode,
                    isBusy = isBusy,
                    onOtpChange = onOtpChange,
                    onVerifyOtp = onVerifyOtp,
                    onResendOtp = onResendOtp
                )
            } else {
                Button(
                    onClick = onRequestOtp,
                    enabled = !isBusy,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(20.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
                ) {
                    Text(
                        text = if (isBusy) {
                            "Verifying..."
                        } else {
                            "Sign in and send OTP"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f)
            )

            OutlinedButton(
                onClick = onBack,
                enabled = !isBusy,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.36f)
                )
            ) {
                Text(
                    text = "Back to home",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(
    label: String,
    title: String,
    subtitle: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(999.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f),
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
            )
        ) {
            Text(
                text = label,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun OtpEntrySection(
    otpCode: String,
    isBusy: Boolean,
    onOtpChange: (String) -> Unit,
    onVerifyOtp: () -> Unit,
    onResendOtp: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = otpCode,
            onValueChange = onOtpChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = "6-digit email OTP") },
            singleLine = true,
            enabled = !isBusy,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword
            ),
            shape = RoundedCornerShape(20.dp)
        )

        Button(
            onClick = onVerifyOtp,
            enabled = !isBusy,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
        ) {
            Text(
                text = "Verify OTP and continue",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        OutlinedButton(
            onClick = onResendOtp,
            enabled = !isBusy,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)
            )
        ) {
            Text(
                text = "Resend OTP",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun AdminStepRow(otpSent: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AdminStepChip(
            modifier = Modifier.weight(1f),
            label = "1",
            title = "Firebase",
            value = "Credentials",
            active = true
        )

        AdminStepChip(
            modifier = Modifier.weight(1f),
            label = "2",
            title = "Email",
            value = "OTP",
            active = otpSent
        )
    }
}

@Composable
private fun AdminStepChip(
    modifier: Modifier = Modifier,
    label: String,
    title: String,
    value: String,
    active: Boolean
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = if (active) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.66f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.66f)
        },
        border = BorderStroke(
            width = 1.dp,
            color = if (active) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Surface(
                modifier = Modifier.size(28.dp),
                shape = CircleShape,
                color = if (active) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (active) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = value,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun StatusPanel(
    text: String,
    isError: Boolean
) {
    val containerColor = if (isError) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.50f)
    }

    val contentColor = if (isError) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        border = BorderStroke(
            width = 1.dp,
            color = if (isError) {
                MaterialTheme.colorScheme.error.copy(alpha = 0.20f)
            } else {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
            }
        )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor,
            fontWeight = FontWeight.Medium
        )
    }
}