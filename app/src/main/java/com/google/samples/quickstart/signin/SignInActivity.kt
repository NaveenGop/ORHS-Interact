package com.google.samples.quickstart.signin

import android.app.Activity
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Scope
import com.google.android.gms.common.api.Status
import com.google.android.gms.tasks.Task
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.sheets.v4.SheetsScopes
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import java.io.IOException
import java.util.*


/**
 * Created by Naveen Gopalan on 11/18/2017.
 * Activity to demonstrate basic retrieval of the Google user's ID, email address, and basic
 * profile.
 */
class SignInActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var mCredential: GoogleAccountCredential

    private lateinit var mStatusTextView: TextView
    private var backClicked: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Views
        mStatusTextView = findViewById(R.id.status)

        // Get the Intent that started this activity and extract the string
        backClicked = intent!!.getBooleanExtra("backClicked", false)

        // Button listeners
        findViewById<View>(R.id.sign_in_button).setOnClickListener(this)
        findViewById<View>(R.id.sign_out_button).setOnClickListener(this)
        findViewById<View>(R.id.disconnect_button).setOnClickListener(this)

        // [START configure_signin]
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(Scope(SheetsScopes.SPREADSHEETS))
                .build()
        // [END configure_signin]

        // [START build_client]
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        // [END build_client]

        // [START customize_button]
        // Set the dimensions of the sign-in button.
        val signInButton = findViewById<SignInButton>(R.id.sign_in_button)
        signInButton.setSize(SignInButton.SIZE_STANDARD)
        signInButton.setColorScheme(SignInButton.COLOR_LIGHT)
        // [END customize_button]

        mCredential = GoogleAccountCredential.usingOAuth2(
                applicationContext, Arrays.asList(*SCOPES))
                .setBackOff(ExponentialBackOff())
    }

    override fun onStart() {
        super.onStart()

        // [START on_start_sign_in]
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null)
            checkPermissions()
        updateUI(account)
        // [END on_start_sign_in]
    }

    // [START onActivityResult]
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        when (requestCode) {
            RC_SIGN_IN -> {
                // The Task returned from this call is always completed, no need to attach
                // a listener.
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                handleSignInResult(task)
            }
            REQUEST_PERMISSION_GET_ACCOUNTS -> {
                if (resultCode == Activity.RESULT_OK)
                    signIn()
            }
            REQUEST_PERMISSION_SCOPE, REQUEST_GOOGLE_PLAY_SERVICES -> {
                if (resultCode != Activity.RESULT_OK)
                    Toast.makeText(applicationContext, "Dawg we need this", Toast.LENGTH_LONG ).show()
                else
                    signIn()
            }
        }
    }
    // [END onActivityResult]

    // [START handleSignInResult]
    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult<ApiException>(ApiException::class.java)
            // Signed in successfully, show authenticated UI.
            if (!account.email!!.contains("eduhsd.k12.ca.us")) {
                mGoogleSignInClient.signOut()
                throw ApiException(Status(69, "Not ORHS"))
            }
            mCredential.selectedAccount = account.account
            updateUI(account)
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            when (e.statusCode){
                CommonStatusCodes.NETWORK_ERROR -> Toast.makeText(applicationContext, "No Network Access. Sign-In Failed", Toast.LENGTH_LONG ).show()
                CommonStatusCodes.TIMEOUT -> Toast.makeText(applicationContext, "Network Timeout. Sign-In Failed", Toast.LENGTH_LONG ).show()
                CommonStatusCodes.INVALID_ACCOUNT -> Toast.makeText(applicationContext, "Not a Google Account. Sign-In Failed", Toast.LENGTH_LONG ).show()
                69 -> Toast.makeText(applicationContext, "Not an ORHS account. Sign-In Failed", Toast.LENGTH_LONG ).show()
                12501 -> Toast.makeText(applicationContext, "Spreadsheet Access Necessary. Sign-In Failed.", Toast.LENGTH_LONG ).show()
                else -> { Toast.makeText(applicationContext, "Something went wrong. Sign-In Failed", Toast.LENGTH_LONG ).show() }
            }

            Log.w(TAG, "signInResult:failed code=" + e.statusCode)
            updateUI(null)
        }

    }
    // [END handleSignInResult]

    // [START signIn]
    private fun signIn() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }
    // [END signIn]

    // [START signOut]
    private fun signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this) {
                    // [START_EXCLUDE]
                    updateUI(null)
                    // [END_EXCLUDE]
                }
        backClicked = false
    }
    // [END signOut]

    // [START revokeAccess]
    private fun revokeAccess() {
        mGoogleSignInClient.revokeAccess()
                .addOnCompleteListener(this) {
                    // [START_EXCLUDE]
                    updateUI(null)
                    // [END_EXCLUDE]
                }
        backClicked = false
    }
    // [END revokeAccess]

    private fun updateUI(account: GoogleSignInAccount?) {
        if (account != null) {
            mStatusTextView.text = getString(R.string.signed_in_fmt, account.displayName)

            findViewById<View>(R.id.sign_in_button).visibility = View.GONE
            findViewById<View>(R.id.sign_out_and_disconnect).visibility = View.VISIBLE
            if (!backClicked) {
                if (mCredential.selectedAccount == null)
                    mCredential.selectedAccount = account.account
                MakeRequestTask(mCredential).execute()
            }
        } else {
            mStatusTextView.setText(R.string.signed_out)

            findViewById<View>(R.id.sign_in_button).visibility = View.VISIBLE
            findViewById<View>(R.id.sign_out_and_disconnect).visibility = View.GONE
        }
    }

    //TODO Fix This Nig
    /* override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }*/

    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private fun chooseAccount() {
            if (EasyPermissions.hasPermissions(this, android.Manifest.permission.GET_ACCOUNTS)) {
                    signIn()
            } else {
                // Request the GET_ACCOUNTS permission via a user dialog
                EasyPermissions.requestPermissions(
                        this,
                        "This app needs to access your Google account (via Contacts).",
                        REQUEST_PERMISSION_GET_ACCOUNTS,
                        android.Manifest.permission.GET_ACCOUNTS)
            }
        }

    /**
     * With the onStart() call, ensure that all services are up-to-date and
     * enabled. Redirects to proper methods to get permissions.
     */
    private fun checkPermissions(){
        if (!GoogleSignIn.hasPermissions(
                GoogleSignIn.getLastSignedInAccount(this), Scope(SheetsScopes.SPREADSHEETS))) {
            GoogleSignIn.requestPermissions(
                    this@SignInActivity,
                    REQUEST_PERMISSION_SCOPE,
                    GoogleSignIn.getLastSignedInAccount(this),
                    Scope(SheetsScopes.SPREADSHEETS))
        }
        if (!EasyPermissions.hasPermissions(this, android.Manifest.permission.GET_ACCOUNTS)) {
            // Request the GET_ACCOUNTS permission via a user dialog
            chooseAccount()
        }
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices()
        }
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     * @return true if Google Play Services is available and up to
     * date on this device; false otherwise.
     */
    private fun isGooglePlayServicesAvailable(): Boolean {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this)
        return connectionStatusCode == ConnectionResult.SUCCESS
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private fun acquireGooglePlayServices() {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this)
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode)
        }
    }


    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     * @param connectionStatusCode code describing the presence (or lack of)
     * Google Play Services on this device.
     */
    private fun showGooglePlayServicesAvailabilityErrorDialog(
            connectionStatusCode: Int) {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val dialog = apiAvailability.getErrorDialog(
                this@SignInActivity,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES)
        dialog.show()
    }


    override fun onClick(v: View) {
        when (v.id) {
            R.id.sign_in_button -> chooseAccount()
            R.id.sign_out_button -> signOut()
            R.id.disconnect_button -> revokeAccess()
        }
    }

    private inner class MakeRequestTask internal constructor(credential: GoogleAccountCredential) : AsyncTask<Void, Void, List<String>>() {
        private var mService: com.google.api.services.sheets.v4.Sheets? = null
        private var mLastError: Exception? = null

        /**
         * Fetch a list of names and majors of students in a sample spreadsheet:
         * https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
         * @return List of names and majors
         * @throws IOException
         */
        private val dataFromApi: List<String>
            @Throws(IOException::class)
            get() {
                val spreadsheetId = "1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms"
                val range = "Class Data!A2:E"
                val results = ArrayList<String>()
                val response = this.mService!!.spreadsheets().values()
                        .get(spreadsheetId, range)
                        .execute()
                val values = response.getValues()
                Log.i("Hi", "async executed")
                if (values != null) {
                    results.add("Name, Major")
                    values.mapTo(results) { it[0].toString() + ", " + it[4] }
                }
                Log.i("Hi", "async complete")
                return results
            }

        init {
            val transport = AndroidHttp.newCompatibleTransport()
            val jsonFactory = JacksonFactory.getDefaultInstance()
            mService = com.google.api.services.sheets.v4.Sheets.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Sign In Quickstart")
                    .build()
        }

        /**
         * Background task to call Google Sheets API.
         * @param params no parameters needed for this task.
         */
        override fun doInBackground(vararg params: Void): List<String>? {
            return try {
                dataFromApi
            } catch (e: Exception) {
                mLastError = e
                cancel(true)
                null
            }

        }

        //TODO
        override fun onPostExecute(result: List<String>?) {
            startActivity(Intent(this@SignInActivity, MainActivity::class.java))
        }


        override fun onCancelled() {
            if (mLastError != null) {
                when (mLastError) {
                    is GooglePlayServicesAvailabilityIOException -> showGooglePlayServicesAvailabilityErrorDialog(
                            (mLastError as GooglePlayServicesAvailabilityIOException)
                                    .connectionStatusCode)
                    is UserRecoverableAuthIOException -> startActivityForResult(
                            (mLastError as UserRecoverableAuthIOException).intent,
                            REQUEST_AUTHORIZATION)
                    else -> { Toast.makeText(applicationContext,"The following error occurred:\n" + mLastError!!.message
                            , Toast.LENGTH_LONG ).show()}
                }
            } else {
                Toast.makeText(applicationContext,"Request cancelled.", Toast.LENGTH_LONG ).show()
            }
        }
    }

    companion object {

        private const val TAG = "SignInActivity"
        private const val RC_SIGN_IN = 9001

        private const val REQUEST_AUTHORIZATION = 1001
        private const val REQUEST_GOOGLE_PLAY_SERVICES = 1002
        private const val REQUEST_PERMISSION_GET_ACCOUNTS = 1003
        private const val REQUEST_PERMISSION_SCOPE = 1004

        private val SCOPES = arrayOf(SheetsScopes.SPREADSHEETS)
    }
}
