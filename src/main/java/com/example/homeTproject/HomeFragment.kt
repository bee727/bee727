package com.example.homeTproject

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import com.example.homeTproject.databinding.HomefragmentBinding
import kotlinx.coroutines.flow.combine


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
                nextIntent.putExtra("pose", "Squat")
                startActivity(nextIntent)
            }
        }
        binding.btnPlankStart.setOnClickListener {
            activity?.let {
                val nextIntent = Intent(context, MocapActivity::class.java)
                nextIntent.putExtra("pose", "Plank")
                startActivity(nextIntent)
            }
        }
        binding.btnLegRaiseStart.setOnClickListener {
            activity?.let {
                val nextIntent = Intent(context, MocapActivity::class.java)
                nextIntent.putExtra("pose", "LegRaise")
                startActivity(nextIntent)
            }
        }
        return binding.root
    }






//    override fun onResume() {
//        super.onResume()
//        //val activity = activity
//        if (activity != null) {
//            //getActivity().setActionBarTitle("Home")
////            activity.actionBar?.title = "home"
//            (activity as MainActivity).setActionBarTitle("Home")
//        }
//    }

    private fun favoriteStatuChange(imageButton: ImageButton, key : String, value : Int) {
        imageButton.isSelected = !imageButton.isSelected
        val bundle = bundleOf("키" to key)
        // 요청키로 수신측의 리스너에 값을 전달
        setFragmentResult("요청키이름", bundle)

    }


    override fun onResume() {
        super.onResume()
        binding.btnSquatFavorite.setOnClickListener { favoriteStatuChange(binding.btnSquatFavorite, "squat", 0) }
        binding.btnPlankFavorite.setOnClickListener { favoriteStatuChange(binding.btnPlankFavorite, "plank", 1) }
        binding.btnLegRaiseFavorite.setOnClickListener{ favoriteStatuChange(binding.btnLegRaiseFavorite, "leg", 2) }
    }


}