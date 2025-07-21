package com.example.ghostapp.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.example.ghostapp.BestiaryActivity
import com.example.ghostapp.MapActivity
import com.example.ghostapp.R
import com.example.ghostapp.ReportActivity

class NavigationFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_navigation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<ImageView>(R.id.map_activity_map_button).setOnClickListener {
            val intent = Intent(requireContext(), MapActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            }
            startActivity(intent)
            requireActivity().overridePendingTransition(0, 0)
        }

        view.findViewById<ImageView>(R.id.map_activity_report_button).setOnClickListener {
            val intent = Intent(requireContext(), ReportActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            }
            startActivity(intent)
            requireActivity().overridePendingTransition(0, 0)
        }

        view.findViewById<ImageView>(R.id.map_activity_bestiary_button).setOnClickListener {
            val intent = Intent(requireContext(), BestiaryActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            }
            startActivity(intent)
            requireActivity().overridePendingTransition(0, 0)
        }
    }
}