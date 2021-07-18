package com.aslilokal.mitra

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.aslilokal.mitra.databinding.ActivityMainBinding
import com.aslilokal.mitra.ui.account.login.LoginActivity
import com.aslilokal.mitra.ui.account.verify.review.ReviewPageActivity
import com.aslilokal.mitra.ui.kelola.tambah.TambahProductActivity
import com.aslilokal.mitra.ui.notifications.NotificationActivity
import com.aslilokal.mitra.utils.AslilokalDataStore
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

// Notifikasi Channel setup
// "/topics/[name topics]"
const val NOTIFICATION_TOPIC = "/topics/notification-"

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var datastore = AslilokalDataStore(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.mainToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.fabMain.hide()
        binding.fabPesanan.hide()

        runBlocking {
            val username = datastore.read("USERNAME").toString()
            val isLogin = datastore.read("ISLOGIN").toString()

            when (isLogin) {
                "null" -> {
                    startActivity(Intent(binding.root.context, LoginActivity::class.java))
                    finish()
                }
                "review" -> {
                    startActivity(Intent(binding.root.context, ReviewPageActivity::class.java))
                    finish()
                }
                else -> {
                    FirebaseMessaging.getInstance().token.addOnSuccessListener {
                        Log.d("FCMTOKEN", it)
                        CoroutineScope(Dispatchers.IO).launch {
                            datastore.save("devicetoken", it)
                        }
                    }
                    val finalTopic = "$NOTIFICATION_TOPIC$username"
                    Log.d("FINALTOPIC", finalTopic)
                    FirebaseMessaging.getInstance().subscribeToTopic(finalTopic)
                }
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
                    binding.mainToolbar.visibility = View.VISIBLE
                    binding.searchToolbar.searchToolbar.title = ""
                    binding.searchToolbar.searchToolbar.visibility = View.GONE
                    binding.fabMain.hide()
                    binding.fabPesanan.hide()
                    true
                }
                R.id.navigation_kelola_produk -> {
                    //temp hide search feature
                    setSupportActionBar(binding.mainToolbar)
                    // hide search feature
                    binding.mainToolbar.visibility = View.VISIBLE
                    binding.mainToolbar.title = ""
                    binding.searchToolbar.searchToolbar.title = ""
                    //hide search feature
                    binding.searchToolbar.searchToolbar.visibility = View.GONE
                    //setSupportActionBar(binding.searchToolbar.searchToolbar)
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

    override fun onResume() {
        super.onResume()
        runBlocking {
            val username = datastore.read("USERNAME").toString()
            val isLogin = datastore.read("ISLOGIN").toString()

            if (isLogin == "null") {
                startActivity(Intent(binding.root.context, LoginActivity::class.java))
                finish()
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
                startActivity(Intent(this, NotificationActivity::class.java))
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