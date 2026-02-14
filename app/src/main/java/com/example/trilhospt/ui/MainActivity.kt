package com.example.trilhospt.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.trilhospt.R
import android.view.Menu
import android.view.MenuItem
import com.example.trilhospt.data.remote.api.AuthInterceptor
import com.example.trilhospt.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var authInterceptor: AuthInterceptor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        authInterceptor = AuthInterceptor(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        
        setSupportActionBar(binding.toolbar)
        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.homeFragment, R.id.trailListFragment, R.id.uploadPhotoFragment, R.id.profileFragment)
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        
        setupBottomNavigation()
    }
    
    private fun setupBottomNavigation() {
        binding.bottomNavigation.setupWithNavController(navController)
        
        // Listener to handle menu visibility based on destination
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val isAuthScreen = destination.id == R.id.loginFragment || destination.id == R.id.registerFragment
            if (isAuthScreen) {
                // Force hide toolbar menu items on auth screens
                invalidateOptionsMenu()
            }
            
            // UI Visibility logic
            when (destination.id) {
                R.id.loginFragment, R.id.registerFragment -> {
                    binding.bottomNavigation.visibility = View.GONE
                    binding.appBarLayout.visibility = View.VISIBLE
                }
                R.id.trailDetailFragment, R.id.aboutFragment -> {
                    binding.bottomNavigation.visibility = View.GONE
                    binding.appBarLayout.visibility = View.VISIBLE
                }
                else -> {
                    binding.bottomNavigation.visibility = View.VISIBLE
                    binding.appBarLayout.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        
        // Só mostra o menu "Sobre" se o utilizador estiver autenticado
        val token = authInterceptor.getToken()
        menu?.findItem(R.id.aboutFragment)?.isVisible = !token.isNullOrEmpty()
        
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val currentDestination = navController.currentDestination?.id
        val isAuthScreen = currentDestination == R.id.loginFragment || currentDestination == R.id.registerFragment
        
        val token = authInterceptor.getToken()
        
        // Só mostra se tiver token E NÃO estiver num ecrã de autenticação
        menu?.findItem(R.id.aboutFragment)?.isVisible = !token.isNullOrEmpty() && !isAuthScreen
        
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.aboutFragment -> {
                navController.navigate(R.id.aboutFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
