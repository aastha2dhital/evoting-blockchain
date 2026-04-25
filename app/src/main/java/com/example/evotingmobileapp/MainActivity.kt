package com.example.evotingmobileapp

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.example.evotingmobileapp.navigation.AppNavGraph
import com.example.evotingmobileapp.ui.theme.EVotingMobileAppTheme
import java.util.Locale

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_EVotingMobileApp)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val baseContext = LocalContext.current

            var selectedLanguageCode by rememberSaveable {
                mutableStateOf(loadSavedLanguageCode(baseContext))
            }

            val localizedContext = remember(selectedLanguageCode, baseContext) {
                baseContext.withAppLocale(selectedLanguageCode)
            }

            CompositionLocalProvider(LocalContext provides localizedContext) {
                EVotingMobileAppTheme {
                    val navController = rememberNavController()

                    Box(modifier = Modifier.fillMaxSize()) {
                        AppNavGraph(
                            navController = navController,
                            appContext = applicationContext,
                            adminViewModelStoreOwner = this@MainActivity
                        )

                        LanguageSwitcher(
                            selectedLanguageCode = selectedLanguageCode,
                            onLanguageSelected = { languageCode ->
                                selectedLanguageCode = languageCode
                                saveLanguageCode(baseContext, languageCode)
                            },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .statusBarsPadding()
                                .padding(top = 12.dp, end = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LanguageSwitcher(
    selectedLanguageCode: String,
    onLanguageSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        tonalElevation = 6.dp,
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LanguageOptionButton(
                label = stringResource(R.string.language_english_short),
                selected = selectedLanguageCode == LANGUAGE_ENGLISH,
                onClick = { onLanguageSelected(LANGUAGE_ENGLISH) }
            )

            LanguageOptionButton(
                label = stringResource(R.string.language_nepali_short),
                selected = selectedLanguageCode == LANGUAGE_NEPALI,
                onClick = { onLanguageSelected(LANGUAGE_NEPALI) }
            )
        }
    }
}

@Composable
private fun LanguageOptionButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(999.dp)

    if (selected) {
        Button(
            onClick = onClick,
            modifier = Modifier.height(38.dp),
            shape = shape,
            contentPadding = PaddingValues(horizontal = 14.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Text(text = label)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = Modifier.height(38.dp),
            shape = shape,
            contentPadding = PaddingValues(horizontal = 14.dp)
        ) {
            Text(text = label)
        }
    }
}

private fun Context.withAppLocale(languageCode: String): Context {
    val safeLanguageCode = when (languageCode) {
        LANGUAGE_NEPALI -> LANGUAGE_NEPALI
        else -> LANGUAGE_ENGLISH
    }

    val locale = Locale(safeLanguageCode)
    Locale.setDefault(locale)

    val configuration = Configuration(resources.configuration)
    configuration.setLocale(locale)
    configuration.setLayoutDirection(locale)

    return createConfigurationContext(configuration)
}

private fun loadSavedLanguageCode(context: Context): String {
    val savedLanguageCode = context
        .getSharedPreferences(LANGUAGE_PREFS_NAME, Context.MODE_PRIVATE)
        .getString(LANGUAGE_PREFS_KEY, LANGUAGE_ENGLISH)

    return when (savedLanguageCode) {
        LANGUAGE_NEPALI -> LANGUAGE_NEPALI
        else -> LANGUAGE_ENGLISH
    }
}

private fun saveLanguageCode(
    context: Context,
    languageCode: String
) {
    val safeLanguageCode = when (languageCode) {
        LANGUAGE_NEPALI -> LANGUAGE_NEPALI
        else -> LANGUAGE_ENGLISH
    }

    context
        .getSharedPreferences(LANGUAGE_PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putString(LANGUAGE_PREFS_KEY, safeLanguageCode)
        .apply()
}

private const val LANGUAGE_ENGLISH = "en"
private const val LANGUAGE_NEPALI = "ne"
private const val LANGUAGE_PREFS_NAME = "language_preferences"
private const val LANGUAGE_PREFS_KEY = "selected_language_code"