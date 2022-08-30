package com.team12.fruitwatch.ui.main

import android.Manifest
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.team12.fruitwatch.R
import com.team12.fruitwatch.controllers.NetworkRequestController
import com.team12.fruitwatch.data.AuthenticationDataSource
import com.team12.fruitwatch.data.model.LoggedInUser
import com.team12.fruitwatch.database.entities.PastSearch
import com.team12.fruitwatch.database.entitymanager.PastSearchDb
import com.team12.fruitwatch.databinding.ActivityMainBinding
import com.team12.fruitwatch.ui.animation.LoadingAnimation
import com.team12.fruitwatch.ui.animation.LoadingAnimationController
import com.team12.fruitwatch.ui.camera.CameraActivity
import com.team12.fruitwatch.ui.login.LoginActivity
import com.team12.fruitwatch.ui.login.LoginViewModel
import com.team12.fruitwatch.ui.main.fragments.FragmentDataLink
import com.team12.fruitwatch.ui.main.fragments.results.RecentResults
import com.team12.fruitwatch.ui.main.fragments.results.ResultsFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.aviran.cookiebar2.CookieBar
import java.io.File
import java.time.LocalDateTime


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, FragmentDataLink , LoadingAnimationController {

    private val TAG = "MainActivity"
    private var pressedTime: Long = 0

    companion object {
        const val IN_DEVELOPMENT = false
        var showLoginGreeting = true
        lateinit var userInfo: LoggedInUser
        val PAST_RES_KEY = "PAST_RESULTS"
        val SEARCH_RES_KEY = "SEARCH_RESULTS"
    }

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var userDisplayName: TextView
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navView: NavigationView
    private lateinit var loadingAnimation: LoadingAnimation
    private lateinit var mainViewModel: MainViewModel

    // Initialise all the elements of the main activity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!intent.hasExtra("USER_KEY")) {
            throw Exception()
        }

        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]
        if(checkCameraHardware(this)) {
            checkCameraPermissions(this)
        }
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.appBarMain.toolbar)
        val drawerLayout: DrawerLayout = binding.drawerLayout
        navView = binding.navView
        navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        val navController: NavController = navHostFragment.navController

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_search, R.id.nav_results, R.id.nav_settings
            ), drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        navView.setNavigationItemSelectedListener(this)
        userDisplayName = navView.getHeaderView(0).findViewById(R.id.nav_main_subtitle)
        userInfo = intent.getParcelableExtra("USER_KEY")!!
        userDisplayName.text = getString(R.string.welcome, userInfo.displayName)
        loadingAnimation = LoadingAnimation(this, "loading.json")
        insertValidJWT()


    }

    // Inflate the menu; this adds items to the action bar if it is present.
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        //menuInflater.inflate(R.menu.app_bar_menu, menu)
        val resultsItem = menu.findItem(R.id.nav_results)
        if(resultsItem != null){
            resultsItem.isVisible = RecentResults.mostRecentSearchResults != null
        }
        return true
    }

    // Build the drawer layout menu
    override fun onSupportNavigateUp(): Boolean {
        val navController = navHostFragment.findNavController()
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    // Prevent the user from accidentally exiting the app by confirming with an additional button press
    override fun onBackPressed() {
        if (pressedTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed()
            finish()
        } else {
            CookieBar.build(this@MainActivity)
                .setTitle("Press back again to exit")
                .setBackgroundColor(R.color.orange_500)
                .setIcon(R.drawable.ic_baseline_priority_high_24)
                .setAnimationIn(android.R.anim.slide_in_left, android.R.anim.slide_in_left)
                .setAnimationOut(android.R.anim.slide_out_right, android.R.anim.slide_out_right)
                .setDuration(3000)
                .show()
        }
        pressedTime = System.currentTimeMillis()
    }

    // Saves most recent search results in case activity is destroyed
    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        savedInstanceState.putParcelable(SEARCH_RES_KEY,RecentResults.mostRecentSearchResults)
        savedInstanceState.putParcelable(PAST_RES_KEY,RecentResults.mostRecentPastSearch)
    }

    // Restores most recent search results when activity is created after being destroyed
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        RecentResults.mostRecentSearchResults = savedInstanceState.getParcelable(SEARCH_RES_KEY)
        RecentResults.mostRecentPastSearch = savedInstanceState.getParcelable(PAST_RES_KEY)
        toggleResultsMenuItemVisibility()
    }

    // All actions for each of the drawer menu options
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        toggleResultsMenuItemVisibility()
        when (item.itemId) {
            R.id.nav_search -> {
                val navController = navHostFragment.findNavController()
                navController.navigate(item.itemId)
            }
            R.id.nav_results -> {
                val navController = navHostFragment.findNavController()
                if (RecentResults.mostRecentSearchResults != null) {
                    navController.navigate(R.id.nav_results)
                } else {
                    val noRecentSearch = AlertDialog.Builder(this,R.style.Theme_FruitWatch_Dialog)
                        .setTitle("Nothing to show")
                        .setMessage("You need to conduct a search before looking at the results, do you want to start a search now?")
                        .setPositiveButton("Yes, Search", DialogInterface.OnClickListener { dialog: DialogInterface, i: Int ->
                            navController.navigate(R.id.nav_search)
                        }
                        ).setNegativeButton("No", null).create()
                    noRecentSearch.show()
                }
            }
            R.id.nav_settings -> {
                val navController = navHostFragment.findNavController()
                navController.navigate(R.id.nav_settings)
            }
            R.id.nav_logout -> {
                CoroutineScope(Dispatchers.IO).launch {
                    val loginRepo = LoginViewModel(AuthenticationDataSource())
                    if (loginRepo.logout(userInfo.jwt)) {
                        clearJWT()
                        CoroutineScope(Dispatchers.Main).launch {
                            startActivity(Intent(applicationContext, LoginActivity::class.java))
                            finish()
                        }
                    } else {
                        CoroutineScope(Dispatchers.Main).launch {
                            CookieBar.build(this@MainActivity)
                                .setTitle(R.string.logout_failed)
                                .setBackgroundColor(R.color.red_500)
                                .setIcon(R.drawable.ic_baseline_clear_24)
                                .setAnimationIn(android.R.anim.slide_in_left, android.R.anim.slide_in_left)
                                .setAnimationOut(android.R.anim.slide_out_right, android.R.anim.slide_out_right)
                                .setDuration(4000)
                                .show()
                        }
                    }
                }
            }
            R.id.nav_credit -> {
                    val creditDialog = AlertDialog.Builder(this,R.style.Theme_FruitWatch_Dialog)
                        .setTitle("Fruit Watch Creators")
                        .setMessage("The creators/authors and their sections of contribution towards the Fruit Watch project are:" +
                                "\n\nCameron Akers \n\t\t\t- Frontend\n\n" +
                                "Marck Munoz\n\t\t\t- Backend\n" +
                                "\t\t\t- Server Management\n" +
                                "\t\t\t- Machine Learning\n\n" +
                                "Matthew Fuaux\n\t\t\t- Backend\n" +
                                "\t\t\t- Scraper")
                        .setNeutralButton("Close", null).create()
                creditDialog.show()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    // Toggles the visibility for the 'Results' menu options based on the existence of the search results
    private fun toggleResultsMenuItemVisibility(){
        val resultsItem = navView.menu.findItem(R.id.nav_results)
        if(resultsItem != null) {
            resultsItem.isVisible = RecentResults.mostRecentSearchResults != null
        }
    }

    // Evaluate the visibility of the 'Results' menu options each time the MainActivity is resumed
    override fun onStart() {
        super.onStart()
        toggleResultsMenuItemVisibility()
        if(showLoginGreeting) {
            val welcomeText = getString(R.string.welcome, userInfo.displayName)
            CookieBar.build(this)
                .setTitle(welcomeText)
                .setBackgroundColor(R.color.blue_500)
                .setIcon(R.drawable.ic_baseline_back_hand_24)
                .setAnimationIn(android.R.anim.slide_in_left, android.R.anim.slide_in_left)
                .setAnimationOut(android.R.anim.slide_out_right, android.R.anim.slide_out_right)
                .setDuration(4000) 
                .show()
            showLoginGreeting = false
        }
    }

    // Clear the recent results when activity is destroyed
    override fun onDestroy() {
        super.onDestroy()
        RecentResults.mostRecentSearchResults = null
        RecentResults.mostRecentPastSearch = null
    }

    // Store the Users JWT on the device to 'auto-login' next time Fruit Watch is re-opened
    private fun insertValidJWT(){
        val SHARED_PREF_NAME :String  = "FWS"
        val TOKEN_PREF_NAME :String = "ult"
        val preferences: SharedPreferences = applicationContext.getSharedPreferences(SHARED_PREF_NAME,Context.MODE_PRIVATE)
        val storedJWT = preferences.getString(TOKEN_PREF_NAME,"None")
        if(storedJWT != userInfo.jwt){
            preferences.edit().putString(TOKEN_PREF_NAME, userInfo.jwt).apply()
            Log.i(TAG,"User Token Updated")
        }
    }

    // Removes the stored JWT when the user logouts
    private fun clearJWT(){
        val SHARED_PREF_NAME :String  = "FWS"
        val TOKEN_PREF_NAME :String = "ult"
        val preferences: SharedPreferences = applicationContext.getSharedPreferences(SHARED_PREF_NAME,Context.MODE_PRIVATE)
        preferences.edit().putString(TOKEN_PREF_NAME, "None").apply()
        Log.i(TAG,"User Token Cleared")
    }

    // Check the user has granted the  required Camera Permissions
    private fun checkCameraPermissions(context: Context?) {
        if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted
            Log.d("checkCameraPermissions", "No Camera Permissions")
            ActivityCompat.requestPermissions(
                (context as Activity?)!!, arrayOf(Manifest.permission.CAMERA),
                100
            )
        }
    }

    // Grabs the most recent item picture
    private fun getSavedImageFileFromInternalStorage(): File {
        val directory: File = applicationContext.getDir("search_images", Context.MODE_PRIVATE)
        val mypath = File(directory, "image.png")
        return mypath
    }

    // Gets the image taken by the user to start a new search
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            onStartLoading()
            val imageFile = getSavedImageFileFromInternalStorage()
            startImageSearch(imageFile)
        } else if (resultCode == CameraActivity.RESULT_FAILED) {
            Log.d("HomeFragment", "No camera attached on device")
            CookieBar.build(this@MainActivity)
                .setTitle("No camera detected on device!")
                .setBackgroundColor(R.color.red_500)
                .setIcon(R.drawable.ic_baseline_clear_24)
                .setAnimationIn(android.R.anim.slide_in_left, android.R.anim.slide_in_left)
                .setAnimationOut(android.R.anim.slide_out_right, android.R.anim.slide_out_right)
                .setDuration(4000) 
                .show()
        }
    }

    // Starts a search using the name of a previously searched item
    override fun startTextSearch(pastSearch: PastSearch) {
        mainViewModel.viewModelScope.launch(Dispatchers.IO) {
            RecentResults.mostRecentSearchResults = NetworkRequestController().startSearchWithItemName(userInfo, pastSearch.itemName!!)
            RecentResults.mostRecentPastSearch = pastSearch
            mainViewModel.viewModelScope.launch(Dispatchers.Main) {
                onFinishedLoading()
                toggleResultsMenuItemVisibility()
                val navController = navHostFragment.findNavController()
                navController.navigate(R.id.nav_results)
            }
        }
    }

    // Start a new search with a recently taken image
    override fun startImageSearch(imageFile: File) {

        mainViewModel.viewModelScope.launch(Dispatchers.IO) {
            RecentResults.mostRecentSearchResults = NetworkRequestController().startSearchWithImage(userInfo, imageFile)
            if (RecentResults.mostRecentSearchResults!!.name != "Unknown") {
                val pastSearchDb = PastSearchDb(applicationContext)
                if (pastSearchDb.checkIfSearchIsNew(RecentResults.mostRecentSearchResults!!.name)){
                    RecentResults.mostRecentPastSearch = PastSearch(-1L, RecentResults.mostRecentSearchResults!!.name, LocalDateTime.now(), imageFile.readBytes())
                    RecentResults.mostRecentPastSearch!!.id = PastSearchDb(applicationContext).createPastSearchEntry(RecentResults.mostRecentPastSearch!!)
                    if (RecentResults.mostRecentPastSearch!!.id != -1L) {
                        if(pastSearchDb.determineIfPastSearchLimitIsReached()){
                            mainViewModel.viewModelScope.launch(Dispatchers.Main) {
                                CookieBar.build(this@MainActivity)
                                    .setTitle("Search History Updated!")
                                    .setBackgroundColor(R.color.green_500)
                                    .setIcon(R.drawable.ic_baseline_check_24)
                                    .setAnimationIn(android.R.anim.slide_in_left, android.R.anim.slide_in_left)
                                    .setAnimationOut(android.R.anim.slide_out_right, android.R.anim.slide_out_right)
                                    .setDuration(3000) 
                                    .show()
                            }
                            Log.d(TAG, "Past Search Result Updated!!")
                        }else{
                            mainViewModel.viewModelScope.launch(Dispatchers.Main) {
                                CookieBar.build(this@MainActivity)
                                    .setTitle("Search History Appended!")
                                    .setBackgroundColor(R.color.green_500)
                                    .setIcon(R.drawable.ic_baseline_check_24)
                                    .setAnimationIn(android.R.anim.slide_in_left, android.R.anim.slide_in_left)
                                    .setAnimationOut(android.R.anim.slide_out_right, android.R.anim.slide_out_right)
                                    .setDuration(3000) 
                                    .show()
                            }
                            Log.d(TAG, "Past Search Result Added!!")
                        }
                    }
                }else{
                    RecentResults.mostRecentPastSearch = pastSearchDb.getPastSearchByItemName(RecentResults.mostRecentSearchResults!!.name)!!
                }
            }else{
                val itemUnrecognized = AlertDialog.Builder(applicationContext,R.style.Theme_FruitWatch_Dialog)
                    .setTitle("Item Unrecognised")
                    .setMessage("The item captured was unrecognised, please re-capture the image to restart the search")
                    .setPositiveButton("Ok, Re-Capture", DialogInterface.OnClickListener { dialog: DialogInterface, i: Int ->
                        openCamera()
                    }
                    ).setNegativeButton("No", null).create()
                itemUnrecognized.show()
            }
            mainViewModel.viewModelScope.launch(Dispatchers.Main) {
                onFinishedLoading()
                toggleResultsMenuItemVisibility()
                val navController = navHostFragment.findNavController()
                navController.navigate(R.id.nav_results)
            }
        }
    }

    // Open the camera to take a picture
    override fun openCamera() {
        if(checkCameraHardware(applicationContext)) {
            checkCameraPermissions(applicationContext!!)
            val takePictureIntent = Intent(this, CameraActivity::class.java)
            try {
                startActivityForResult(takePictureIntent, ResultsFragment.REQUEST_IMAGE_CAPTURE)
            } catch (e: ActivityNotFoundException) {
                // display error state to the user
                Log.d("HomeFragment", "Activity Not Found $e")
            }
        }
    }

    // Open the Search Result Fragment
    override fun openSearchFrag() {
        toggleResultsMenuItemVisibility()
        val navController = navHostFragment.findNavController()
        navController.navigate(R.id.nav_search)
    }

    // Show the searching animation
    override fun onStartLoading() {
        loadingAnimation.playAnimation()
    }

    // Hide the searching animation
    override fun onFinishedLoading() {
        loadingAnimation.stopAnimation()
    }

    // Check if the device has a camera
    private fun checkCameraHardware(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
    }
}