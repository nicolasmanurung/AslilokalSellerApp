package com.kodelapo.mitra

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.kodelapo.mitra.databinding.ActivityMainBinding
import com.kodelapo.mitra.ui.account.login.LoginActivity
import com.kodelapo.mitra.ui.kelola.tambah.TambahProductActivity
import com.kodelapo.mitra.utils.KodelapoDataStore
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var dataStore = KodelapoDataStore(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.mainToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.fabMain.hide()
        binding.fabPesanan.hide()

        lifecycleScope.launch {
            val username = dataStore.read("USERNAME").toString()
            val isLogin = dataStore.read("ISLOGIN").toString()

            if (isLogin == "null") {
                startActivity(Intent(binding.root.context, LoginActivity::class.java))
                finish()
            }
        }
        binding.searchToolbar.searchToolbar.visibility = View.INVISIBLE

        binding.fabMain.setOnClickListener {
            startActivity(Intent(binding.root.context, TambahProductActivity::class.java))
        }


        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        NavigationUI.setupWithNavController(navView, navController)

        navView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_beranda -> {
                    setSupportActionBar(binding.mainToolbar)
                    navController.navigate(R.id.navigation_beranda)
                    binding.mainToolbar.title = ""
                    binding.searchToolbar.searchToolbar.title = ""
                    binding.mainToolbar.visibility = View.VISIBLE
                    binding.searchToolbar.searchToolbar.visibility = View.GONE
                    binding.fabMain.hide()
                    binding.fabPesanan.hide()
                    true
                }
                R.id.navigation_kelola_produk -> {
                    binding.mainToolbar.visibility = View.GONE
                    binding.mainToolbar.title = ""
                    binding.searchToolbar.searchToolbar.title = ""
                    binding.searchToolbar.searchToolbar.visibility = View.VISIBLE
                    setSupportActionBar(binding.searchToolbar.searchToolbar)
                    navController.navigate(R.id.navigation_kelola_produk)
                    binding.fabMain.show()
                    binding.fabPesanan.hide()
                    true
                }
                R.id.navigation_pesanan -> {
                    setSupportActionBar(binding.mainToolbar)
                    binding.mainToolbar.title = ""
                    binding.searchToolbar.searchToolbar.title = ""
                    binding.mainToolbar.visibility = View.VISIBLE
                    binding.searchToolbar.searchToolbar.visibility = View.GONE
                    navController.navigate(R.id.navigation_pesanan)
                    binding.fabMain.hide()
                    binding.fabPesanan.show()
                    true
                }
                R.id.navigation_analitik -> {
                    binding.mainToolbar.visibility = View.GONE
                    binding.mainToolbar.title = ""
                    binding.searchToolbar.searchToolbar.title = ""
                    binding.searchToolbar.searchToolbar.visibility = View.GONE
                    navController.navigate(R.id.navigation_analitik)
                    binding.fabMain.hide()
                    binding.fabPesanan.hide()
                    true
                }
                R.id.navigation_profil -> {
                    binding.mainToolbar.visibility = View.GONE
                    binding.mainToolbar.title = ""
                    binding.searchToolbar.searchToolbar.title = ""
                    binding.searchToolbar.searchToolbar.visibility = View.GONE
                    navController.navigate(R.id.navigation_profil)
                    binding.fabMain.hide()
                    binding.fabPesanan.hide()
                    true
                }
                else -> true
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.home_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.notification -> {
                Toast.makeText(
                    binding.root.context,
                    "Sedang dalam tahap pengembangan",
                    Toast.LENGTH_SHORT
                ).show()
                return true
            }
            R.id.message -> {
                Toast.makeText(
                    binding.root.context,
                    "Sedang dalam tahap pengembangan",
                    Toast.LENGTH_SHORT
                ).show()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
}