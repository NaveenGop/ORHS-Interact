package com.google.samples.quickstart.signin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.support.v4.app.ActivityCompat.startActivityForResult
import android.support.v4.content.ContextCompat
import android.support.v4.content.ContextCompat.startActivity
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
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
                                                           private val spreadsheetId: String) : AsyncTask<Void, Map<String, String>, Void>() {

    private val weakActivity: WeakReference<Activity> = WeakReference(activity)
    private var mService: com.google.api.services.sheets.v4.Sheets? = null
    private var mLastError: Exception? = null

    /**
     * Fetch a list of names and majors of students in a sample spreadsheet:
     * https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
     * @return List of names and majors
     * @throws IOException
     */
    private val dataFromApi: List<Map<String, String>>
        @Throws(IOException::class)
        get() {
            val results: List<Map<String, String>>
            val response = this.mService!!.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .setMajorDimension("COLUMNS")
                    .execute()
            val values = response.getValues()
            values?.removeAt(0)
            Log.i("Hi", "async executed")
            results = values?.map { mapOf("name" to it[1].toString(),
                    "date" to it[2].toString(),
                    "link" to it[3].toString(),
                    "location" to it[4].toString(),
                    "desc" to it[5].toString()) } ?: ArrayList()
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
    override fun doInBackground(vararg params: Void): Void? {
        try {
            dataFromApi
        } catch (e: Exception) {
            mLastError = e
            cancel(true)
            return null
        }
        for (event in dataFromApi)
            publishProgress(event)

        return null
    }

    override fun onProgressUpdate(vararg values: Map<String, String>) {
        val activity = weakActivity.get()
        // activity is no longer valid, don't do anything!
        if (activity == null || activity.isFinishing || activity.isDestroyed)
            return

        var info = values[0]["desc"]!!
        //Creating Relative Layout Programmatically
        val relativeLayout = RelativeLayout(activity)
        //CHANGE LATER
        relativeLayout.id = View.generateViewId()
        relativeLayout.setBackgroundResource(R.drawable.borders)
        val rlp = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT)
        //rlp.topMargin = 15
        relativeLayout.layoutParams = rlp


        ////////////// TEXT VIEWS //////////////
        val titleView = TextView(activity)
        titleView.setTextColor(ContextCompat.getColor(activity, R.color.text))
        titleView.setBackgroundResource(R.color.background2)
        if (Build.VERSION.SDK_INT < 23)
            titleView.setTextAppearance(activity, android.R.style.TextAppearance_Large)
        else
            titleView.setTextAppearance(android.R.style.TextAppearance_Large)
        //CHANGE LATER
        titleView.id = View.generateViewId()
        titleView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
        titleView.text = values[0]["name"]
        titleView.setTextColor(ContextCompat.getColor(activity, R.color.text))

        val infoView = TextView(activity)
        if (Build.VERSION.SDK_INT < 23)
            infoView.setTextAppearance(activity, android.R.style.TextAppearance_Small)
        else
            infoView.setTextAppearance(android.R.style.TextAppearance_Small)
        infoView.id = View.generateViewId()
        val infoLayout = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT)
        infoLayout.addRule(RelativeLayout.BELOW, titleView.id)
        infoView.layoutParams = infoLayout

        var placeToDisplay = values[0]["location"]!!
        if (placeToDisplay.length > 15)
            placeToDisplay = placeToDisplay.substring(0, 16) + "..."
        if (info.length > 30)
            info = info.substring(0, 31) + "..."
        infoView.text = activity.getString(R.string.info, placeToDisplay, values[0]["date"], info)
        infoView.setTextColor(ContextCompat.getColor(activity, R.color.text))


        //////////////// BUTTON ///////////////
        val signUpButton = Button(activity)
        signUpButton.setBackgroundResource(R.drawable.alternativebuttons)
        //signUpButton.getBackground().setColorFilter(
        //getResources().getColor(R.color.blue_grey_500), PorterDuff.Mode.MULTIPLY);
        val buttonLayout = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT)
        buttonLayout.addRule(RelativeLayout.ALIGN_PARENT_TOP)
        buttonLayout.addRule(RelativeLayout.ALIGN_PARENT_END)
        signUpButton.gravity = Gravity.CENTER
        signUpButton.layoutParams = buttonLayout
        signUpButton.text = activity.getString(R.string.signin)
        signUpButton.setTextColor(ContextCompat.getColor(activity, R.color.text))
        signUpButton.id = View.generateViewId()
        signUpButton.setOnClickListener {
            startActivity(activity, Intent(Intent.ACTION_VIEW, Uri.parse(values[0]["link"])), null)
        }


        //////////////// BUTTON ///////////////
        val locationButton = ImageButton(activity)
        locationButton.setImageResource(R.drawable.marker)
        val locationButtonLayout = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT)
        locationButtonLayout.addRule(RelativeLayout.ALIGN_PARENT_END)
        locationButtonLayout.addRule(RelativeLayout.ALIGN_BOTTOM)
        locationButtonLayout.addRule(RelativeLayout.BELOW, signUpButton.id)
        locationButtonLayout.setMargins(50, 10, 50, 10)
        locationButton.layoutParams = locationButtonLayout
        locationButton.id = View.generateViewId()
        locationButton.setOnClickListener {
            startActivity(activity, Intent(Intent.ACTION_VIEW,  Uri.parse("geo:0,0?q=%s".format(values[0]["location"]))), null)
        }


        relativeLayout.setPadding(20, 10, 30, 10)
        //////////////Combine Everything///////////

        relativeLayout.addView(titleView)
        relativeLayout.addView(infoView)
        relativeLayout.addView(signUpButton)
        relativeLayout.addView(locationButton)
        val layout = activity.findViewById(R.id.scrollLayout) as LinearLayout
        layout.addView(relativeLayout)
    }

    //TODO
    override fun onPostExecute(result: Void?) {
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