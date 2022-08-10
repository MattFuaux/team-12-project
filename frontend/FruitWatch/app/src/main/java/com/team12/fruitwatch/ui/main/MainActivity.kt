package com.team12.fruitwatch.ui.main

import android.Manifest

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
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
import com.team12.fruitwatch.data.AuthenticationRepository
import com.team12.fruitwatch.data.model.LoggedInUser
import com.team12.fruitwatch.database.entities.PastSearch
import com.team12.fruitwatch.database.entitymanager.PastSearchDb
import com.team12.fruitwatch.databinding.ActivityMainBinding
import com.team12.fruitwatch.ui.camera.CameraActivity
import com.team12.fruitwatch.ui.login.LoginActivity
import com.team12.fruitwatch.ui.login.LoginViewModel
import com.team12.fruitwatch.ui.main.fragments.FragmentDataLink
import com.team12.fruitwatch.ui.main.fragments.results.ResultsFragment
import com.team12.fruitwatch.ui.main.fragments.settings.SettingsFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDateTime


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, FragmentDataLink {

    private val TAG = "MainActivity"


    companion object {
        const val IN_DEVELOPMENT = false
        lateinit var userInfo: LoggedInUser
        private var lastSearchResults: NetworkRequestController.SearchResults? = null
    }

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var userDisplayName: TextView
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navView: NavigationView
    private lateinit var latestBundle: Bundle


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!intent.hasExtra("USER_KEY")) {
            throw Exception()
        }
        checkCameraPermissions(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        latestBundle = Bundle()
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
        userDisplayName.text = userInfo.displayName
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        val resultsItem = menu.findItem(R.id.nav_results)
        if(resultsItem != null){
            resultsItem.isVisible = lastSearchResults != null
        }

        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = navHostFragment.findNavController()
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.main_activity_menu_action_settings -> {
                startActivity(Intent(this, SettingsFragment::class.java))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        toggleResultsMenuItemVisibility()
        when (item.itemId) {
            R.id.nav_search -> {
                val navController = findNavController(R.id.nav_host_fragment_content_main)
                navController.navigate(item.itemId)
            }
            R.id.nav_results -> {
                val navController = findNavController(R.id.nav_host_fragment_content_main)
                if (!latestBundle.isEmpty) {
                    navController.navigate(R.id.nav_results, latestBundle)
                } else {
                    val noRecentSearch = AlertDialog.Builder(this, androidx.appcompat.R.style.Theme_AppCompat_Dialog)
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
                //startActivity(Intent(this, SettingsActivity::class.java))
                val navController = findNavController(R.id.nav_host_fragment_content_main)
                navController.navigate(R.id.nav_settings)
            }
            R.id.nav_logout -> {
                CoroutineScope(Dispatchers.Main).launch {
                    val loginRepo = LoginViewModel(AuthenticationRepository(AuthenticationDataSource()))
                    if (loginRepo.logout(userInfo.jwt)) {
                        CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(applicationContext, R.string.logout_success, Toast.LENGTH_LONG).show()
                            startActivity(Intent(applicationContext, LoginActivity::class.java))
                            finish()
                        }
                    } else {
                        CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(applicationContext, R.string.logout_failed, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }

        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun toggleResultsMenuItemVisibility(){
        val resultsItem = navView.menu.findItem(R.id.nav_results)
        if(resultsItem != null) {
            resultsItem.isVisible = lastSearchResults != null
        }
    }

    override fun onStart() {
        super.onStart()
        toggleResultsMenuItemVisibility()
    }

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

    private fun getSavedImageFileFromInternalStorage(): File {
        val directory: File = applicationContext.getDir("search_images", Context.MODE_PRIVATE)
        // Create imageDir
        val mypath = File(directory, "image.png")
        return mypath
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            val imageFile = getSavedImageFileFromInternalStorage()
            startImageSearch(imageFile)
        } else if (resultCode == CameraActivity.RESULT_FAILED) {
            Log.d("HomeFragment", "No camera attached on device")
            Toast.makeText(this, "No camera detected on device!", Toast.LENGTH_LONG).show()
        }
    }

    override fun startTextSearch(pastSearch: PastSearch) {
        GlobalScope.launch(Dispatchers.IO) {
            lastSearchResults = NetworkRequestController().startSearchWithItemName(userInfo, pastSearch.itemName!!)
            latestBundle = Bundle()
            latestBundle.putParcelable("SEARCH_RESULTS", lastSearchResults)
            latestBundle.putParcelable("PAST_SEARCH_REQUEST", pastSearch)
            GlobalScope.launch(Dispatchers.Main) {
                toggleResultsMenuItemVisibility()
                val navController = findNavController(R.id.nav_host_fragment_content_main)
                navController.navigate(R.id.nav_results, latestBundle)
            }
//            resultsFragment.arguments = bundle
//            val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
//            transaction.addToBackStack(null)
//            transaction.replace(R.id.nav_host_fragment_content_main, resultsFragment)
//            transaction.commit()
        }

    }

    override fun startImageSearch(imageFile: File) {
        // TODO: Add loading wheel while waiting for network activity
        GlobalScope.launch(Dispatchers.IO) {
            lastSearchResults = NetworkRequestController().startSearchWithImage(userInfo, imageFile)

            latestBundle = Bundle()
            if (lastSearchResults!!.name != "Unknown") {
                val pastSearch = PastSearch(-1L, lastSearchResults!!.name, LocalDateTime.now(), imageFile.readBytes())
                pastSearch.id = PastSearchDb(applicationContext).createPastSearchEntry(pastSearch)
                if (pastSearch.id != -1L) {
                    GlobalScope.launch(Dispatchers.Main) {
                        Toast.makeText(applicationContext, "Search History Updated!", Toast.LENGTH_SHORT).show()
                    }
                    Log.d(TAG, "Past Search Result Added!!")
                }
                latestBundle.putParcelable("PAST_SEARCH_REQUEST", pastSearch)
            }else{
                val itemUnrecognized = AlertDialog.Builder(applicationContext, androidx.appcompat.R.style.Theme_AppCompat_Dialog)
                    .setTitle("Item Unrecognised")
                    .setMessage("The item captured was unrecognised, please re-capture the image to restart the search")
                    .setPositiveButton("Ok, Re-Capture", DialogInterface.OnClickListener { dialog: DialogInterface, i: Int ->
                        openCamera()
                    }
                    ).setNegativeButton("No", null).create()
                itemUnrecognized.show()
            }
            latestBundle.putParcelable("SEARCH_RESULTS", lastSearchResults)

//            resultsFragment.arguments = bundle
            GlobalScope.launch(Dispatchers.Main) {
                toggleResultsMenuItemVisibility()
                val navController = findNavController(R.id.nav_host_fragment_content_main)
                navController.navigate(R.id.nav_results, latestBundle)
            }

//            val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
//            transaction.addToBackStack(null)
//            transaction.replace(R.id.nav_host_fragment_content_main, resultsFragment)
//            transaction.commit()

        }
    }

    override fun openCamera() {
        //val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val takePictureIntent = Intent(this, CameraActivity::class.java)
        try {
            startActivityForResult(takePictureIntent, ResultsFragment.REQUEST_IMAGE_CAPTURE)
        } catch (e: ActivityNotFoundException) {
            // display error state to the user
            Log.d("HomeFragment", "Activity Not Found ${e.toString()}")
        }
    }

    override fun openSearchFrag() {
        toggleResultsMenuItemVisibility()
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        navController.navigate(R.id.nav_search)
    }

}