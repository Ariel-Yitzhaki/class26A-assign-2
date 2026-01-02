package com.example.racing_assignment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.core.content.edit
import com.bumptech.glide.Glide
import com.example.racing_assignment.databinding.FragmentRecordsBinding

class RecordsFragment : Fragment() {

    private var _binding: FragmentRecordsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecordsBinding.inflate(inflater, container, false)

        Glide.with(this)
            .load(R.drawable.leaderboard_background)
            .centerCrop()
            .into(binding.leaderboardBackground)

        val prefs = requireContext().getSharedPreferences("records", Context.MODE_PRIVATE)

        val records = listOf(
            binding.record1, binding.record2, binding.record3, binding.record4, binding.record5,
            binding.record6, binding.record7, binding.record8, binding.record9, binding.record10
        )

        records.forEachIndexed { index, textView ->
            val score = prefs.getInt("record${index + 1}", 0)
            textView.setOnClickListener {
                // TODO: handle record click
            }
        }

        binding.returnMenu.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun saveScore(position: Int, score: Int) {
        val prefs = requireContext().getSharedPreferences("records", Context.MODE_PRIVATE)
        prefs.edit { putInt("record$position", score) }
    }
}