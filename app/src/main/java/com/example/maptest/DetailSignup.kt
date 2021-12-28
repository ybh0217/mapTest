package com.example.maptest

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import kotlinx.android.synthetic.main.fragment_detail_signup.view.*


class DetailSignup : Fragment() {
    var mainActivity: MainActivity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainActivity = context as MainActivity
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_detail_signup,container,false)
        view.underMenu.setOnClickListener { mainActivity?.goBackLo() }
        view.btnBtAddress.setOnClickListener {mainActivity?.findAddress()}
        view.btnChange.setOnClickListener { mainActivity?.signUp() }
        return view
    }
}