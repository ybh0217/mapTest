package com.example.maptest

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import kotlinx.android.synthetic.main.fragment_member_page.view.*


class memberPage : Fragment() {
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
        val view = inflater!!.inflate(R.layout.fragment_member_page,container,false)

        view.changMember.setOnClickListener { mainActivity?.onChangeMember() }
        view.withdrawal.setOnClickListener {mainActivity?.withDrawal()}
        view.btnLogout.setOnClickListener {mainActivity?.goBackLo()}
        view.underMenu.setOnClickListener {mainActivity?.loginIng()}
        return view
    }
}