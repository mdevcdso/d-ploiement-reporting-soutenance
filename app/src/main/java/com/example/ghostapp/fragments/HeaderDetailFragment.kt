package com.example.ghostapp.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.ghostapp.MapActivity
import com.example.ghostapp.R

class HeaderDetailFragment : Fragment() {
    private var headerTitle: String = ""
    private var lastActivity: Class<*>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_header_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<ImageView>(R.id.ghost_logo).setOnClickListener {
            startActivity(Intent(requireContext(), MapActivity::class.java))
        }

        view.findViewById<TextView>(R.id.header_title).text = headerTitle

        view.findViewById<ImageView>(R.id.back_arrow).setOnClickListener {
            requireActivity().finish()
        }
    }

    fun setHeaderTitle(title: String) {
        headerTitle = title
        view?.findViewById<TextView>(R.id.header_title)?.text = title
    }

    fun setLastActivity(activity: Class<*>) {
        lastActivity = activity
        view?.findViewById<ImageView>(R.id.back_arrow)?.setOnClickListener {
            val intent = Intent(requireContext(), activity)
            startActivity(intent)
            requireActivity().finish()
        }
    }
}