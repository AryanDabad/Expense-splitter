package com.example.expensemanager.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.expensemanager.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class HomeActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    private var bottomNavigationView: BottomNavigationView? = null
    private var frameLayout: FrameLayout? = null

    //Fragment
    private var dashboardFragment: DashboardFragment? = null
    private var incomeFragment: IncomeFragment? = null
    private var expenseFragment: ExpenseFragment? = null
    private var statsFragment: StatsFragment? = null
    private var mAuth: FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        mAuth = FirebaseAuth.getInstance()
        val toolbar = findViewById<Toolbar>(R.id.my_toolbar)
        toolbar.title = "Expense Manager"
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toggle: ActionBarDrawerToggle = object : ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        ) {}
        toggle.isDrawerIndicatorEnabled = true
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        val navigationView = findViewById<NavigationView>(R.id.navView)
        navigationView.setNavigationItemSelectedListener(this)
        bottomNavigationView = findViewById(R.id.bottomNavbar)
        frameLayout = findViewById(R.id.main_frame)
        dashboardFragment = DashboardFragment()
        incomeFragment = IncomeFragment()
        expenseFragment = ExpenseFragment()
        statsFragment = StatsFragment()
        setFragment(dashboardFragment!!)
        bottomNavigationView?.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.dashboard -> {
                    setFragment(dashboardFragment!!)
                    true
                }
                R.id.income -> {
                    setFragment(incomeFragment!!)
                    true
                }
                R.id.expense -> {
                    setFragment(expenseFragment!!)
                    true
                }
                /*R.id.stats -> {
                    setFragment(statsFragment!!)
                    true
                }*/
                R.id.groups -> {
                    setFragment(GroupsFragments())
                    true
                }
                else -> false
            }
        }
    }

    private fun setFragment(fragment: Fragment) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.main_frame, fragment)
        fragmentTransaction.commit()
    }

    override fun onBackPressed() {
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END)
        } else {
            super.onBackPressed()
        }
    }

    fun displaySelectedListener(itemId: Int) {
        var fragment: Fragment? = null
        when (itemId) {
            android.R.id.home -> {
                val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
                drawerLayout.openDrawer(GravityCompat.START)
                return
            }
            R.id.dashboard -> {
                bottomNavigationView!!.selectedItemId = R.id.dashboard
                fragment = DashboardFragment()
            }
            R.id.income -> {
                bottomNavigationView!!.selectedItemId = R.id.income
                fragment = IncomeFragment()
            }
            R.id.expense -> {
                bottomNavigationView!!.selectedItemId = R.id.expense
                fragment = ExpenseFragment()
            }
            /*R.id.stats -> {
                bottomNavigationView!!.selectedItemId = R.id.stats
                bottomNavigationView!!.findViewById<View>(R.id.stats).performClick()
                bottomNavigationView!!.performClick()
                fragment = StatsFragment()
            }*/
            R.id.account -> fragment = AccountFragment()
            R.id.logout -> {
                mAuth!!.signOut()
                startActivity(Intent(applicationContext, MainActivity::class.java))
                finish()
            }
        }
        if (fragment != null) {
            val ft = supportFragmentManager.beginTransaction()
            ft.replace(R.id.main_frame, fragment)
            ft.commit()
        }
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        Log.i("ITEM ID", Integer.toString(item.itemId))
        displaySelectedListener(item.itemId)
        return true
    }
}