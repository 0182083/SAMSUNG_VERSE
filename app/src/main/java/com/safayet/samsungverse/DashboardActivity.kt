package com.safayet.samsungverse

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import safayet.samsungverse.ClubFragment
import safayet.samsungverse.DiscoverFragment
import safayet.samsungverse.MeFragment

class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        // Default fragment → Discover
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, DiscoverFragment())
            .commit()

        bottomNav.setOnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.nav_discover -> DiscoverFragment()
                R.id.nav_club -> ClubFragment()
                R.id.nav_me -> MeFragment()
                else -> null
            }

            fragment?.let {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, it)
                    .commit()
                true
            } ?: false
        }
    }
}
