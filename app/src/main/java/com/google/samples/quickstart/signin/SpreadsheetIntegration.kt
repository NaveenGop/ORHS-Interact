package com.google.samples.quickstart.signin

import android.app.Activity
import android.os.AsyncTask
import android.support.v4.app.ActivityCompat.startActivityForResult
import android.util.Log
import android.widget.Toast
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.json.jackson2.JacksonFactory
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.*


/**
 * Created by Naveen Gopalan on 12/26/2017.
 * Allows various onPostExecute() methods to be implemented with the same source code.
 */
abstract class SpreadsheetIntegration internal constructor(credential: GoogleAccountCredential,
                                                           activity: Activity,
                                                           private val range: String,
                                                           private val spreadsheetId: String) : AsyncTask<Void, Void, List<String>>() {

    private val weakActivity: WeakReference<Activity> = WeakReference(activity)
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
           // val spreadsheetId = "1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms"
            // val range = "Class Data!A2:E"
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
        //startActivity(Intent(this@SignInActivity, MainActivity::class.java))
    }

    override fun onCancelled() {
        val activity = weakActivity.get()
        if (activity == null || activity.isFinishing || activity.isDestroyed) {
            // activity is no longer valid, don't do anything!
            return
        }

        if (mLastError != null) {
            when (mLastError) {
                //is GooglePlayServicesAvailabilityIOException -> activity.showGooglePlayServicesAvailabilityErrorDialog(
                //        (mLastError as GooglePlayServicesAvailabilityIOException).connectionStatusCode)
                is UserRecoverableAuthIOException -> startActivityForResult(
                        activity,
                        (mLastError as UserRecoverableAuthIOException).intent,
                        SignInActivity.REQUEST_AUTHORIZATION,
                        null)
                else -> { Toast.makeText(activity,"The following error occurred:\n" + mLastError!!.message
                        , Toast.LENGTH_LONG ).show()}
            }
        } else {
            Toast.makeText(activity,"Request cancelled.", Toast.LENGTH_LONG ).show()
        }
    }
}