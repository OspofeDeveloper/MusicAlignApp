package com.example.musicalignapp.ui.screens.addfile.trim_image

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.musicalignapp.R
import com.example.musicalignapp.databinding.FragmentImageBinding
import com.example.musicalignapp.databinding.FragmentTrimImageBinding

class TrimImageFragment : Fragment() {

    private var _binding: FragmentTrimImageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTrimImageBinding.inflate(inflater, container, false)
        return binding.root
    }
}