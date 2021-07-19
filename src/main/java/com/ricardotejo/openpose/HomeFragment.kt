package com.ricardotejo.openpose

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.ricardotejo.openpose.databinding.HomefragmentBinding


class HomeFragment :Fragment() {
    private lateinit var binding: HomefragmentBinding


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = HomefragmentBinding.inflate(inflater, container, false)
        binding.btnSquatStart.setOnClickListener {
            activity?.let {
                val nextIntent = Intent(context, MocapActivity::class.java)
                startActivity(nextIntent)
            }
        }
        return binding.root
    }


    override fun onResume() {
        super.onResume()
        val activity = activity
        if (activity != null) {
            (activity as MainActivity).setActionBarTitle("Home")
        }
    }


}