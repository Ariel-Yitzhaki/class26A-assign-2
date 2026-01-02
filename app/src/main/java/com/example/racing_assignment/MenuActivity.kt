package com.example.racing_assignment

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.racing_assignment.databinding.ActivityMenuBinding

class MenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Glide.with(this)
            .load(R.drawable.menu_background)
            .into(binding.menuBackground)

        binding.buttons.setOnClickListener {
            val fragment = GameFragment().apply {
                arguments = Bundle().apply {
                    putBoolean("useButtons", true)
                }
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        binding.sensors.setOnClickListener {
            val fragment = GameFragment().apply {
                arguments = Bundle().apply {
                    putBoolean("useButtons", false)
                }
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        binding.records.setOnClickListener {
            val fragment = RecordsFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
    }
}