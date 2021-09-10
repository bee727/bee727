package com.example.homeTproject

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.homeTproject.databinding.SelectcountfragmentBinding

class SelectCountFragment : Fragment() {
    private lateinit var binding : SelectcountfragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = SelectcountfragmentBinding.inflate(inflater, container, false)
        binding.btnStart.setOnClickListener {
            activity?.let {
                val nextIntent = Intent(context, MocapActivity::class.java)
                startActivity(nextIntent)
            }
        }

        return binding.root
    }
}