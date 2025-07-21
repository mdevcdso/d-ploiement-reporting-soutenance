package com.example.ghostapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.ghostapp.R
import android.content.Intent
import android.widget.ImageView
import android.widget.TextView
import com.example.ghostapp.MapActivity
import com.example.ghostapp.ProfileActivity

class HeaderDefaultFragment : Fragment() {
    private var headerTitle: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_header_default, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<ImageView>(R.id.ghost_logo).setOnClickListener {
            startActivity(Intent(requireContext(), MapActivity::class.java))
        }

        view.findViewById<TextView>(R.id.header_title).text = headerTitle

        view.findViewById<ImageView>(R.id.user_profile).setOnClickListener {
            startActivity(Intent(requireContext(), ProfileActivity::class.java))
        }
    }

    fun setHeaderTitle(title: String) {
        headerTitle = title
        view?.findViewById<TextView>(R.id.header_title)?.text = title
    }
}
