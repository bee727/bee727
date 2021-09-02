package com.example.homeTproject

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import com.example.homeTproject.databinding.LikefragmentBinding

class LikeFragment : Fragment() {

    private lateinit var binding: LikefragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 요청키이름은 마치 onActivityResult 에서 사용하는 requestKey 같은 개념입니다.
        // 해당 요청키로 전달된 값을 처리하겠다는 의미 입니다.
        setFragmentResultListener("요청키이름") { key, bundle ->
            bundle.getString("키")?.let {
                if(it == "squat"){
                    if(binding.containerSquat.visibility == View.VISIBLE){
                        binding.containerSquat.visibility = View.GONE
                    }
                    else{ binding.containerSquat.visibility = View.VISIBLE }
                }
                if(it == "plank"){
                    if(binding.containerPlank.visibility == View.VISIBLE){
                        binding.containerPlank.visibility = View.GONE
                    }
                    else{ binding.containerPlank.visibility = View.VISIBLE }
                }
                if(it == "leg"){
                    if(binding.containerLeg.visibility == View.VISIBLE){
                        binding.containerLeg.visibility = View.GONE
                    }
                    else{ binding.containerLeg.visibility = View.VISIBLE }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = LikefragmentBinding.inflate(inflater, container, false)
//        setFragmentResultListener("requestKey") { requestKey, bundle ->
//            val result = bundle.getString("bundleKey")
//            // Do something with the result
//            binding.likeTxt.text = result
//        }
        return binding.root
    }



    override fun onResume() {
        super.onResume()
        val activity = activity
        if (activity != null) {
            (activity as MainActivity).setActionBarTitle("Like List")
        }
    }


}