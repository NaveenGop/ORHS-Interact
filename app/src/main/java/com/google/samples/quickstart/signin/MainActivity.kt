package com.google.samples.quickstart.signin

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.*
import android.widget.ImageView
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    /**
     * The [android.support.v4.view.PagerAdapter] that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * [android.support.v4.app.FragmentStatePagerAdapter].
     */
    private lateinit var mSectionsPagerAdapter: SectionsPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

        // Set up the ViewPager with the sections adapter.
        container.adapter = mSectionsPagerAdapter

        container.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        tabs.run {
            addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(container))
            setSelectedTabIndicatorColor(ContextCompat.getColor(this.context, R.color.background))
            getTabAt(0)!!.setIcon(R.drawable.home)
            getTabAt(1)!!.setIcon(R.drawable.event)
            getTabAt(2)!!.setIcon(R.drawable.info)
        }
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

    }

    override fun onBackPressed() {
        val intent = Intent(this, SignInActivity::class.java)
        intent.putExtra("backClicked", true)
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        if (id == R.id.action_settings) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }


    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            // getItem is called to instantiate the fragment for the given page.
            // Return a Home (defined as a static inner class below).
            Log.v("hi", ""+position)
            when (position) {
                0 -> return Home.newInstance(0)
                2 -> return Info.newInstance(2)
                // 3 -> Home.newInstance(1)
            }
            return Info.newInstance(position)
        }

        //Show 3 total pages
        override fun getCount(): Int = 3
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    class Home : Fragment() {

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {
            val rootView = inflater.inflate(R.layout.fragment_main, container, false)
            //rootView.section_label.text = getString(R.string.section_format, arguments.getInt(ARG_SECTION_NUMBER))
            return rootView
        }

        companion object {
            /**
             * The fragment argument representing the section number for this
             * fragment.
             */
            private val ARG_SECTION_NUMBER = "section_number"

            /**
             * Returns a new instance of this fragment for the given section
             * number.
             */
            fun newInstance(sectionNumber: Int): Home {
                val fragment = Home()
                val args = Bundle()
                args.putInt(ARG_SECTION_NUMBER, sectionNumber)
                fragment.arguments = args
                return fragment
            }
        }
    }
    /*
    class Events: Fragment() {

        internal var browserIntent = Intent()

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {
            return inflater.inflate(R.layout.events, container, false)
        }


        override fun onActivityCreated(savedInstanceState: Bundle?) {
            super.onActivityCreated(savedInstanceState)
            setAllViewInfo()
        }


        //Later on, fix the method to have info and location
        private fun setAllViewInfo() {
            for (event in MySpreadsheetIntegration.eventList) {
                generateView(event.getName(), event.getDescription(), event.getLocation(), event.getDate(), event.getLink())
            }
        }


        private fun generateView(Title: String, Info: String, Place: String, Time: String, link: String): Int {
            var Info = Info
            //Creating Relative Layout Programmatically
            val relativeLayout = RelativeLayout(activity)
            //CHANGE LATER
            relativeLayout.id = View.generateViewId()
            relativeLayout.setBackgroundResource(R.drawable.borders)
            val rlp = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT)
            rlp.topMargin = 15
            relativeLayout.layoutParams = rlp
            ////////////// TEXT VIEWS //////////////

            val titleView = TextView(activity)
            titleView.setTextColor(ContextCompat.getColor(activity, R.color.text))
            titleView.setBackgroundResource(R.color.background2)
            if (Build.VERSION.SDK_INT < 23) {
                titleView.setTextAppearance(activity, android.R.style.TextAppearance_Large)
            } else {
                titleView.setTextAppearance(android.R.style.TextAppearance_Large)
            }
            //CHANGE LATER
            titleView.id = View.generateViewId()
            titleView.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT)
            titleView.text = Title
            titleView.setTextColor(ContextCompat.getColor(activity, R.color.text))
            val infoView = TextView(activity)
            if (Build.VERSION.SDK_INT < 23) {
                infoView.setTextAppearance(activity, android.R.style.TextAppearance_Small)
            } else {
                infoView.setTextAppearance(android.R.style.TextAppearance_Small)
            }
            infoView.id = View.generateViewId()
            val infoLayout = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT)
            infoLayout.addRule(RelativeLayout.BELOW, titleView.id)
            infoView.layoutParams = infoLayout
            var placeToDisplay = Place
            if (Place.length > 15)
                placeToDisplay = placeToDisplay.substring(0, 16) + "..."
            if (Info.length > 30)
                Info = Info.substring(0, 31) + "..."

            infoView.text = placeToDisplay + "\n" + Time + "\n" + Info
            infoView.setTextColor(ContextCompat.getColor(activity, R.color.text))

            //////////////// BUTTON ///////////////

            val signUpButton = Button(activity)
            signUpButton.setBackgroundResource(R.drawable.alternativebuttons)
            //signUpButton.getBackground().setColorFilter(
            //getResources().getColor(R.color.blue_grey_500), PorterDuff.Mode.MULTIPLY);
            val buttonLayout = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT)
            buttonLayout.addRule(RelativeLayout.CENTER_VERTICAL)
            buttonLayout.addRule(RelativeLayout.ALIGN_PARENT_END)
            signUpButton.gravity = Gravity.CENTER
            signUpButton.layoutParams = buttonLayout
            signUpButton.text = "Sign Up"
            signUpButton.setTextColor(ContextCompat.getColor(activity, R.color.text))
            signUpButton.id = View.generateViewId()
            signUpButton.setOnClickListener {
                Events.browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                startActivity(Events.browserIntent)
            }


            //////////////// BUTTON ///////////////

            val locationButton = ImageButton(activity)
            locationButton.setImageResource(R.drawable.marker)
            val locationButtonLayout = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT)
            locationButtonLayout.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
            locationButtonLayout.addRule(RelativeLayout.BELOW, infoView.id)
            locationButtonLayout.setMargins(0, 10, 0, 0)
            locationButton.layoutParams = locationButtonLayout
            locationButton.id = View.generateViewId()
            locationButton.setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW,
                        Uri.parse(String.format("geo:0,0?q=%s",
                                URLEncoder.encode(Place)))))
            }


            relativeLayout.setPadding(20, 10, 30, 10)
            //////////////Combine Everything///////////

            relativeLayout.addView(titleView)
            relativeLayout.addView(infoView)
            relativeLayout.addView(signUpButton)
            relativeLayout.addView(locationButton)
            val layout = activity.findViewById(R.id.scrollLayout) as LinearLayout ?: return 1
            layout.addView(relativeLayout)
            return signUpButton.id

        }

        companion object {
            /**
             * The fragment argument representing the section number for this
             * fragment.
             */
            private val ARG_SECTION_NUMBER = "section_number"

            /**
             * Returns a new instance of this fragment for the given section
             * number.
             */
            fun newInstance(sectionNumber: Int): Home {
                val fragment = Home()
                val args = Bundle()
                args.putInt(ARG_SECTION_NUMBER, sectionNumber)
                fragment.arguments = args
                return fragment
            }
        }
    }
    */

    class Info : Fragment(), View.OnClickListener {

        private var counter = 0
        private var img: ImageView? = null
        private var uriStrings: Map<Int, String> = mapOf(R.id.joinbutton to "https://goo.gl/forms/k9uHclWR4jazJjYj2",
                R.id.websitebutton to "https://orhsinteract.com",
                R.id.rescourcesbutton to "https://drive.google.com/folderview?id=0BwJLvdTM6Ac4UWlqSHlKTGhxRFk&usp=sharing",
                R.id.githubbutton to "https://github.com/ORHS-Web-App-Dev")

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {
            val rootView = inflater.inflate(R.layout.fragment_info, container, false)

            img = rootView.findViewById<View>(R.id.aboutWAAD) as ImageView

            // Button listeners
            rootView.findViewById<View>(R.id.aboutWAAD).setOnClickListener(this)
            rootView.findViewById<View>(R.id.joinbutton).setOnClickListener(this)
            rootView.findViewById<View>(R.id.websitebutton).setOnClickListener(this)
            rootView.findViewById<View>(R.id.rescourcesbutton).setOnClickListener(this)
            rootView.findViewById<View>(R.id.githubbutton).setOnClickListener(this)


            return rootView
        }

        override fun onClick(v: View) {
            if (v.id == R.id.aboutWAAD){
                counter++
                if (counter > 9) {
                    // mPlayer.start()
                    img?.setImageResource(R.mipmap.aboutwaad)
                }
            }
            else {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriStrings[v.id]))
                startActivity(intent)
            }
        }


        companion object {
            private val ARG_SECTION_NUMBER = "section_number"

            fun newInstance(sectionNumber: Int): Info {
                val fragment = Info()
                val args = Bundle()
                args.putInt(ARG_SECTION_NUMBER, sectionNumber)
                fragment.arguments = args
                return fragment
            }
        }
    }
}
