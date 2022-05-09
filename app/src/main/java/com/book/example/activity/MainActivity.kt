package com.book.example.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import com.book.example.R
import com.book.example.databinding.ActivityMainBinding
import com.book.example.fragment.AllBooksFragment
import com.book.example.fragment.RentedBooksFragment

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    var previousMenuItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater).also { setContentView(it.root) }

        setUpToolbar()

        openDashboard()

        val actionBarDrawerToggle = ActionBarDrawerToggle(
            this@MainActivity, binding.drawerLayout,
            R.string.open_drawer,
            R.string.close_drawer
        )
        binding.drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        binding.navigationView.setNavigationItemSelectedListener {

            if (previousMenuItem != null) {
                previousMenuItem?.isChecked = false
            }
            it.isCheckable = true
            it.isChecked = true
            previousMenuItem = it

            with(binding) {
                when (it.itemId) {
                    R.id.all_books -> {
                        openDashboard()
                        drawerLayout.closeDrawers()
                    }
                    R.id.rented_books -> {
                        supportFragmentManager.beginTransaction()
                            .replace(
                                R.id.frame,
                                RentedBooksFragment()
                            )
                            .commit()
                        supportActionBar?.title = "Rented books"
                        drawerLayout.closeDrawers()
                    }
                }
            }
            return@setNavigationItemSelectedListener true
        }
    }

    fun setUpToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "All books"
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        return super.onOptionsItemSelected(item)
    }

    fun openDashboard() {
        val fragment = AllBooksFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame, fragment)
        transaction.commit()
        supportActionBar?.title = "All books"
        binding.navigationView.setCheckedItem(R.id.all_books)
    }

    override fun onBackPressed() {
        val frag = supportFragmentManager.findFragmentById(R.id.frame)
        when (frag) {
            !is AllBooksFragment -> openDashboard()

            else -> super.onBackPressed()
        }
    }
}
