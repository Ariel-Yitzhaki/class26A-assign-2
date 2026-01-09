package com.example.racing_assignment.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.racing_assignment.fragments.GameFragment
import com.example.racing_assignment.R
import com.example.racing_assignment.fragments.RecordsFragment
import com.example.racing_assignment.databinding.ActivityMenuBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class MenuActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMenuBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager.addOnBackStackChangedListener {
            // If stack has entries (>0), a fragment is open -> Disable buttons
            // If stack is empty (0), we are at home -> Enable buttons
            val shouldEnable = supportFragmentManager.backStackEntryCount == 0
            setButtonsEnabled(shouldEnable)
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        requestLocationPermission()

        Glide.with(this)
            .load(R.drawable.menu_background)
            .into(binding.menuBackground)

        binding.buttons.setOnClickListener {
            openGameFragment(true)
        }

        binding.sensors.setOnClickListener {
            openGameFragment(false)
        }

        binding.records.setOnClickListener {
            val fragment = RecordsFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun setButtonsEnabled(isEnabled: Boolean) {
        binding.buttons.isEnabled = isEnabled
        binding.sensors.isEnabled = isEnabled
        binding.records.isEnabled = isEnabled
    }

    private fun openGameFragment(useButtons: Boolean) {
        val fragment = GameFragment().apply {
            arguments = Bundle().apply {
                putBoolean("useButtons", useButtons)
            }
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1001
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}