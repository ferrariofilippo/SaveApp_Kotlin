package com.ferrariofilippo.saveapp

import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.ferrariofilippo.saveapp.util.CurrencyUtil
import com.ferrariofilippo.saveapp.util.SettingsUtil
import com.google.android.material.bottomappbar.BottomAppBar
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private var lastFragmentId: Int = R.id.homeFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val saveApp = application as SaveAppApplication

        saveApp.setCurrentActivity(this)

        SettingsUtil.init(saveApp)
        lifecycleScope.launch { CurrencyUtil.init(saveApp) }

        setupButtons()
    }

    fun goBack() {
        lastFragmentId ?: return

        when (lastFragmentId) {
            R.id.homeFragment ->
                findNavController(R.id.containerView).navigate(R.id.homeFragment)

            R.id.historyFragment ->
                findNavController(R.id.containerView).navigate(R.id.historyFragment)

            R.id.budgetsFragment ->
                findNavController(R.id.containerView).navigate(R.id.budgetsFragment)

            R.id.statsFragment ->
                findNavController(R.id.containerView).navigate(R.id.statsFragment)
        }
    }

    private fun setupButtons() {
        val addMovementButton: Button = findViewById(R.id.addMovementFAB)
        addMovementButton.setOnClickListener {
            onAddMovementClick()
        }

        val appBar: BottomAppBar = findViewById(R.id.bottomAppBar)
        appBar.setOnMenuItemClickListener { menuItem: MenuItem ->
            onMenuItemClick(menuItem)
        }
    }

    private fun onAddMovementClick() {
        findNavController(R.id.containerView).navigate(R.id.newMovementFragment)
    }

    private fun onMenuItemClick(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.home -> {
                findNavController(R.id.containerView).navigate(R.id.homeFragment)
                lastFragmentId = R.id.homeFragment

                return true
            }

            R.id.history -> {
                findNavController(R.id.containerView).navigate(R.id.historyFragment)
                lastFragmentId = R.id.historyFragment

                return true
            }

            R.id.budget -> {
                findNavController(R.id.containerView).navigate(R.id.budgetsFragment)
                R.id.budgetsFragment

                return true
            }

            R.id.stats -> {
                findNavController(R.id.containerView).navigate(R.id.statsFragment)
                R.id.statsFragment

                return true
            }
        }

        return false
    }
}
