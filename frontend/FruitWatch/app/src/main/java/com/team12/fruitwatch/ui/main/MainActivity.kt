package com.team12.fruitwatch.ui.main

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
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
import com.team12.fruitwatch.ui.login.LoginActivity
import com.team12.fruitwatch.ui.login.LoginViewModel
import com.team12.fruitwatch.ui.main.fragments.FragmentDataLink
import com.team12.fruitwatch.ui.main.fragments.results.ResultsFragment
import com.team12.fruitwatch.ui.main.fragments.search.PastSearchItemModel
import com.team12.fruitwatch.ui.settings.SettingsActivity
import kotlinx.coroutines.*
import java.io.File
import java.time.LocalDateTime

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, FragmentDataLink{

    private val TAG = "MainActivity"
    companion object {
        const val IN_DEVELOPMENT = true
        lateinit var userInfo: LoggedInUser
    }

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var userDisplayName: TextView
    private lateinit var navHostFragment: NavHostFragment


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!intent.hasExtra("USER_KEY")) {
            throw Exception()
        }
        checkCameraPermissions(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout

        val navView: NavigationView = binding.navView

        navHostFragment =  supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
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
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = navHostFragment.findNavController()
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.main_activity_menu_action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_search,
            R.id.nav_results -> {
                val navController = findNavController(R.id.nav_host_fragment_content_main)
                navController.navigate(item.itemId)
            }
            R.id.nav_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
            R.id.nav_logout -> {
                CoroutineScope(Dispatchers.Main).launch {
                    val loginRepo = LoginViewModel(AuthenticationRepository(AuthenticationDataSource()))
                    if (loginRepo.logout(userInfo.jwt)) {
                        CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(applicationContext,R.string.logout_success,Toast.LENGTH_LONG).show()
                            startActivity(Intent(applicationContext, LoginActivity::class.java))
                            finish()
                        }
                    }else{
                        CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(applicationContext,R.string.logout_failed,Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }

        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    fun checkCameraPermissions(context: Context?) {
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

    override fun startTextSearch(pastSearchItemModel: PastSearchItemModel) {
            GlobalScope.launch(Dispatchers.IO) {
                val result = NetworkRequestController().startSearchWithItemName(MainActivity.userInfo,pastSearchItemModel!!.itemName)
                    val resultsFragment : ResultsFragment = ResultsFragment()
                    val bundle : Bundle = Bundle()
                bundle.putParcelable("SEARCH_RESULTS",result)
                    bundle.putParcelable("PAST_SEARCH_REQUEST",pastSearchItemModel)
                    resultsFragment.arguments = bundle
                    val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
                    transaction.addToBackStack(null)
                    transaction.replace(R.id.nav_host_fragment_content_main, resultsFragment)
                    transaction.commit()

            }

    }

    override fun startImageSearch(imageFile: File) {
        GlobalScope.launch(Dispatchers.IO) {
            val result = NetworkRequestController().startSearchWithImage(MainActivity.userInfo,imageFile)
            if (result.name != "Unknown"){
                if(PastSearchDb(applicationContext).createPastSearchEntry(PastSearch(-1L,result.name, LocalDateTime.now(), BitmapFactory.decodeFile(imageFile.absolutePath)))){
                    GlobalScope.launch(Dispatchers.Main) {
                        Toast.makeText(applicationContext, "Search History Updated!", Toast.LENGTH_SHORT).show()
                    }
                    Log.d(TAG,"Past Search Result Added!!")
                }
            }
            val resultsFragment : ResultsFragment = ResultsFragment()
            val bundle : Bundle = Bundle()
            bundle.putParcelable("SEARCH_RESULTS",result)
            bundle.putByteArray("SEARCH_IMAGE",imageFile.readBytes())
            resultsFragment.arguments = bundle
            val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
            transaction.addToBackStack(null)
            transaction.replace(R.id.nav_host_fragment_content_main, resultsFragment)
            transaction.commit()
        }
    }
}