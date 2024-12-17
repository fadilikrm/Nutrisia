package com.example.nutrisia

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import com.example.nutrisia.databinding.FragmentOnboardingBinding

class OnboardingFragment : Fragment() {

    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_TITLE = "title"
        private const val ARG_DESCRIPTION = "description"
        private const val ARG_IMAGE = "image"

        fun newInstance(title: String, description: String, imageRes: Int): OnboardingFragment {
            val fragment = OnboardingFragment()
            val args = Bundle()
            args.putString(ARG_TITLE, title)
            args.putString(ARG_DESCRIPTION, description)
            args.putInt(ARG_IMAGE, imageRes)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val title = arguments?.getString(ARG_TITLE)
        val description = arguments?.getString(ARG_DESCRIPTION)
        val imageRes = arguments?.getInt(ARG_IMAGE)

        // Menggunakan binding untuk mengakses elemen-elemen UI
        binding.onboardingTitle.text = title
        binding.onboardingDescription.text = description
        imageRes?.let {
            binding.onboardingImage.setImageResource(it)
        }

        // Menambahkan animasi fade-in untuk elemen-elemen UI
        binding.onboardingTitle.startAnimation(AnimationUtils.loadAnimation(requireContext(), android.R.anim.fade_in))
        binding.onboardingDescription.startAnimation(AnimationUtils.loadAnimation(requireContext(), android.R.anim.fade_in))
        binding.onboardingImage.startAnimation(AnimationUtils.loadAnimation(requireContext(), android.R.anim.fade_in))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
