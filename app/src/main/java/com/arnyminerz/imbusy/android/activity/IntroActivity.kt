package com.arnyminerz.imbusy.android.activity

import android.content.IntentSender.SendIntentException
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.res.stringResource
import androidx.core.os.LocaleListCompat
import com.arnyminerz.imbusy.android.R
import com.arnyminerz.imbusy.android.ui.components.SignInButton
import com.arnyminerz.imbusy.android.ui.intro.IntroPager
import com.arnyminerz.imbusy.android.ui.intro.data.IntroPageData
import com.arnyminerz.imbusy.android.ui.intro.data.OptionsData
import com.arnyminerz.imbusy.android.ui.theme.ImBusyTheme
import com.arnyminerz.imbusy.android.utils.toast
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import timber.log.Timber

class IntroActivity : AppCompatActivity() {
    companion object {
        const val RESULT_FINISH = 0
    }

    private lateinit var auth: FirebaseAuth

    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest

    private var currentUser = mutableStateOf<FirebaseUser?>(null)
    private var isLoggedIn = mutableStateOf<Boolean>(false)

    private fun updateIsLoggedIn() {
        isLoggedIn.value = currentUser.value != null
    }

    private val googleSignInRegistration = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        try {
            val data = result.data
            val googleCredential = oneTapClient.getSignInCredentialFromIntent(data)
            val idToken = googleCredential.googleIdToken
            when {
                idToken != null -> {
                    // Got an ID token from Google. Use it to authenticate
                    // with Firebase.
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    auth.signInWithCredential(firebaseCredential)
                        .addOnSuccessListener { signInResult ->
                            // Sign in success, update UI with the signed-in user's information
                            Timber.i("signInWithCredential:success")
                            val user = signInResult.user
                            currentUser.value = user
                            updateIsLoggedIn()
                        }
                        .addOnFailureListener { e ->
                            // If sign in fails, display a message to the user.
                            // TODO: Show error to user
                            Timber.w(e, "signInWithCredential:failure")
                            toast(this, R.string.toast_error_login)
                            currentUser.value = null
                            updateIsLoggedIn()
                        }
                }
                else ->
                    // Shouldn't happen.
                    Timber.d("No ID token!")
            }
        } catch (e: ApiException) {
            // TODO: Show error to the user
            Timber.e(e, "Could not login.")
            toast(this, R.string.toast_error_login)
        }
    }

    @ExperimentalPagerApi
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth

        oneTapClient = Identity.getSignInClient(this)
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    // Your server's client ID, not your Android client ID.
                    .setServerClientId(getString(R.string.default_web_client_id))
                    // Only show accounts previously used to sign in.
                    .setFilterByAuthorizedAccounts(true)
                    .build()
            )
            // Automatically sign in when exactly one credential is retrieved.
            .setAutoSelectEnabled(true)
            .build()

        setContent {
            ImBusyTheme {
                val intro2DropdownDescription =
                    stringResource(R.string.intro_2_dropdown_description)

                IntroPager(
                    listOf(
                        IntroPageData(
                            stringResource(R.string.intro_1_title),
                            stringResource(R.string.intro_1_subtitle),
                            "👋🏼",
                        ),
                        IntroPageData(
                            stringResource(R.string.intro_2_title),
                            stringResource(R.string.intro_2_subtitle),
                            "🌐",
                            options = object : OptionsData(
                                intro2DropdownDescription,
                            ) {
                                override val options: Map<String, String> =
                                    mapOf("en-US" to "English")

                                override fun onChosenOption(option: String) {
                                    val appLocale: LocaleListCompat =
                                        LocaleListCompat.forLanguageTags(option)
                                    // Call this on the main thread as it may require Activity.restart()
                                    AppCompatDelegate.setApplicationLocales(appLocale)
                                }
                            }
                        ),
                        IntroPageData(
                            stringResource(R.string.intro_3_title),
                            stringResource(R.string.intro_3_subtitle),
                            "\uD83D\uDD12", // 🔒
                            requires = isLoggedIn,
                            buttons = listOf {
                                SignInButton(
                                    text = stringResource(R.string.auth_google),
                                ) {
                                    oneTapClient
                                        .beginSignIn(signInRequest)
                                        .addOnSuccessListener { result ->
                                            try {
                                                googleSignInRegistration.launch(
                                                    IntentSenderRequest.Builder(result.pendingIntent.intentSender)
                                                        .build()
                                                )
                                            } catch (e: SendIntentException) {
                                                // TODO: Show error to the user
                                                Timber.e(e, "Could not launch sign in intent.")
                                                toast(this, R.string.toast_error_login)
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            // TODO: Show error to the user
                                            Timber.e(e, "Could not launch sign in intent.")
                                            toast(this, R.string.toast_error_login)
                                        }
                                }
                            }
                        ),
                    )
                ) {
                    setResult(RESULT_FINISH)
                    finish()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        currentUser.value = auth.currentUser
        updateIsLoggedIn()
    }
}
