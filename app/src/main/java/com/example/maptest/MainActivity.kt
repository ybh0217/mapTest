package com.example.maptest

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_change_member.*
import kotlinx.android.synthetic.main.fragment_detail_signup.*
import kotlinx.android.synthetic.main.fragment_detail_signup.AllText
import kotlinx.android.synthetic.main.fragment_detail_signup.BtAddressText
import kotlinx.android.synthetic.main.fragment_login.*
import kotlin.concurrent.timer


@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    lateinit var bAdapter: BluetoothAdapter
    private var mBluetoothStateReceiver: BroadcastReceiver? = null

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient:FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    val helper = DataBase()
    var loginmember = membership(0, "", "", "", "", "")
    var logout = 1

    val database = FirebaseDatabase.getInstance()
    val members = database.getReference("membership")

    val tableNumber = database.getReference("number")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkPermission()

        bAdapter = BluetoothAdapter.getDefaultAdapter()
        setFragmtent()

        timer(period = 10000) {
            pointMarking()
        }

        searchButton.setOnClickListener {
            var findAd = ""

            tableNumber.get().addOnSuccessListener {
                var num = it.value.hashCode()
                for (index in num downTo 0) {
                    members.child(index.toString()).child("pointName").get().addOnSuccessListener {
                        if (it.value == PointSearchEdit.text.toString()) {
                            members.child(index.toString()).child("pointAddress").get().addOnSuccessListener {
                                findAd = it.value.toString()

                                if (findAd != null){
                                    val geocoder = Geocoder(this)

                                    var cor = geocoder.getFromLocationName(findAd, 1)
                                    var searchLocation = LatLng(cor[0].latitude, cor[0].longitude)

                                    mMap.moveCamera(CameraUpdateFactory.newLatLng(searchLocation))
                                } else {
                                    Toast.makeText(this, "내용을 입력하세요.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun startProcess() {
        setContentView(R.layout.activity_main)
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        updateLocation()
    }

    fun updateLocation() {
        val locationRequest = LocationRequest.create()
        locationRequest.run {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult?.let {
                    for ((i, location) in it.locations.withIndex()) {
                        Log.d("Location", "$i ${location.latitude}, ${location.longitude}")
                        setLastLocation(location)
                    }
                }
            }
        }
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
    }

    fun setLastLocation(lastLocation: Location) {
        val LATLNG = LatLng(lastLocation.latitude, lastLocation.longitude)
        val cameraPosition = CameraPosition.Builder()
                .target(LATLNG)
                .zoom(15.0f)
                .build()
        mMap.clear()
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    override fun onDestroy() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        super.onDestroy()
    }

    fun setFragmtent() {
        val transaction = supportFragmentManager.beginTransaction()
        val menu = Menu()
        transaction.add(R.id.MenuLayout, menu)

        transaction.commit()
    }

    fun goLogin() {
        val login = Login()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.MenuLayout, login)
        transaction.addToBackStack("login")
        transaction.commit()
    }

    fun goDetail() {
        onBackPressed()

        val detailSignup = DetailSignup()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.MenuLayout, detailSignup)
        transaction.addToBackStack("detail")
        transaction.commit()
    }

    fun goMemberPage() {
        onBackPressed()

        val memberpage = memberPage()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.MenuLayout, memberpage)
        transaction.addToBackStack("memberPage")
        transaction.commit()
    }

    fun onChangeMember() {
        onBackPressed()

        val changeMember = changeMember()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.MenuLayout, changeMember)
        transaction.commit()
    }

    fun loginIng() {
        onBackPressed()

        val loging = loging()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.MenuLayout, loging)
        transaction.commit()
    }

    fun goBack() {
        onBackPressed()
        setFragmtent()
    }

    fun goBackLo() {
        onBackPressed()

        logout=0

        loginmember = membership(0, "", "", "", "", "")

        goLogin()
    }

    fun login() {
        var answer = membership(0, "", "", "", "", "")

        val Idlogin = IdLogin.text.toString()
        val Pwlogin = PwLogin.text.toString()

        tableNumber.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val num = snapshot.getValue().hashCode()
                for (index in num downTo 0) {
                    members.child(index.toString()).child("id").addValueEventListener(object :
                            ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val ID = snapshot.getValue().toString()
                            if (ID == Idlogin) {
                                answer.ID = ID
                                members.child(index.toString()).child("pw").addValueEventListener(object :
                                        ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        val PW = snapshot.getValue().toString()
                                        if (PW == Pwlogin) {
                                            answer.PW = PW
                                            answer.no = index
                                            members.child(index.toString()).child("pointName").addValueEventListener(object :
                                                    ValueEventListener {
                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                    val pointName = snapshot.getValue().toString()
                                                    answer.PointName = pointName
                                                }

                                                override fun onCancelled(error: DatabaseError) {
                                                }
                                            })
                                            members.child(index.toString()).child("pointAddress").addValueEventListener(object :
                                                    ValueEventListener {
                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                    val pointAddress = snapshot.getValue().toString()
                                                    answer.PointAddress = pointAddress
                                                }

                                                override fun onCancelled(error: DatabaseError) {
                                                }
                                            })
                                            members.child(index.toString()).child("btaddress").addValueEventListener(object :
                                                    ValueEventListener {
                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                    val btAddress = snapshot.getValue().toString()
                                                    answer.BTAddress = btAddress

                                                    if (answer.ID == Idlogin) {
                                                        if (answer.PW == Pwlogin) {
                                                            //Toast.makeText(this, "Login", Toast.LENGTH_SHORT).show()

                                                            logout == 1

                                                            loginmember = answer

                                                            timer(period = 30000) {
                                                                members.child(loginmember.no.toString()).child(loginmember.BTAddress).removeValue()
                                                                members.child(loginmember.no.toString()).child(loginmember.BTAddress).child("cNumber").setValue(1)
                                                                addPairing()

                                                                if (logout == 0) {
                                                                    cancel()
                                                                }
                                                            }
                                                            goMemberPage()
                                                        } else {
                                                            //Toast.makeText(this, "잘못된 PW입니다.", Toast.LENGTH_SHORT).show()
                                                        }
                                                    } else {
                                                        //Toast.makeText(this, "잘못된 ID입니다.", Toast.LENGTH_SHORT).show()
                                                    }
                                                }

                                                override fun onCancelled(error: DatabaseError) {
                                                }
                                            })
                                        } else {
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                    }
                                })
                            } else {
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }


    fun findAddress() {
        if (bAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Bluetooth is available", Toast.LENGTH_SHORT).show()

            if (bAdapter.isEnabled) {
                Toast.makeText(this, "Already on", Toast.LENGTH_SHORT).show()
            } else {
                bAdapter.enable()
                Toast.makeText(this, "Bluetooth is on", Toast.LENGTH_LONG).show()
            }
            val deviceAddress = bAdapter.address

            if (BtAddressText.text == "") {
                BtAddressText.append("$deviceAddress")
            }
        }
    }

    fun signUp() {

        val newID = newIdEdit.text.toString()
        val newPW = newPwEdit.text.toString()
        val newPointName = newPointNameEdit.text.toString()
        val newPointAddress = newPointAddressEdit.text.toString()

        if (newID.isNotEmpty()) {
            if (newPW.isNotEmpty()) {
                if (newPointName.isNotEmpty()) {
                    if (newPointAddress.isNotEmpty()) {
                        if (bAdapter != null) {
                            var BTfix = bAdapter.address.replace(":", "")

                            val member = membership(
                                    0,
                                    newID,
                                    newPW,
                                    newPointName,
                                    newPointAddress,
                                    BTfix
                            )

                            Log.d("dbsqudgus","으djdjdjdjdjdjdjdjdjdjdj")

                            helper.insertMembership(member)
                            newIdEdit.setText("")
                            newPwEdit.setText("")
                            newPointNameEdit.setText("")
                            newPointAddressEdit.setText("")

                            AllText.setText("")
                            AllText.append("ID : $newID\nPW : $newPW\n지점 이름 : $newPointName\n주소 : $newPointAddress\n블루투스 주소 : ${bAdapter.address}")
                        } else {
                            Toast.makeText(this, "회원가입이 불가합니다", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "주소를 입력하세요", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "지점 이름을 입력하세요", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "PW를 입력하세요", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Id를 입력하세요", Toast.LENGTH_SHORT).show()
        }
    }

    fun changeMemberIT() {
        var list = loginmember

        val changeID = changeIdEdit.text.toString()
        val changePW = changePwEdit.text.toString()
        val changePointName = changePointNameEdit.text.toString()
        val changePointAddress = changePointAddressEdit.text.toString()

        list.no = loginmember.no

        if(list.ID!=changeID) {
            list.ID = changeID
        }
        if(list.PW!=changePW) {
            list.PW = changePW
        }
        if(list.PointName!=changePointName){
            list.PointName = changePointName
        }
        if(list.PointAddress!=changePointAddress) {
            list.PointAddress = changePointAddress
        }

        helper.updateMembership(loginmember.no, list)

        loginmember = list
    }

    fun withDrawal() {
        helper.deleteMembership(loginmember.no)

        goBackLo()
    }

    //권한
    val permissions = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    )
    val PERM_LOCATION = 99

    fun checkPermission() {
        var permitted_all = true
        for (permission in permissions) {
            val result = ContextCompat.checkSelfPermission(this, permission)
            if (result != PackageManager.PERMISSION_GRANTED) {
                permitted_all = false
                requestPermission()
                break
            }
        }
        if (permitted_all) {
            startProcess()
        }
    }

    fun requestPermission() {
        ActivityCompat.requestPermissions(this, permissions, PERM_LOCATION)
    }

    fun confirmAgain() {
        AlertDialog.Builder(this)
                .setTitle("권한 승인 확인")
                .setMessage("위치 관련 권한을 모두 승인하셔야 앱을 사용할 수 있습니다. 권한 승인을 다시 하시겠습니까?")
                .setPositiveButton("네", { _, _ ->
                    requestPermission()
                })
                .setNegativeButton("아니요", { _, _ ->
                    finish()
                })
                .create()
                .show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode) {
            99 -> {
                var granted_all = true
                for (result in grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        granted_all = false
                        break
                    }
                }
                if (granted_all) {
                    startProcess()
                } else {
                    confirmAgain()
                }
            }
        }
    }
    fun addPairing() {
        if (bAdapter == null || !bAdapter?.isEnabled!!) {
            val bleEnableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(bleEnableIntent, 1)
        }

        bAdapter.startDiscovery()
        helper.clearTable(loginmember.no, loginmember.BTAddress)

        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(mBluetoothStateReceiver, filter)

        mBluetoothStateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action
                if (BluetoothDevice.ACTION_FOUND == action) {
                    val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    val deviceName = device!!.name
                    val deviceHardwareAddress = device!!.address // MAC address


                    val devicetype = device!!.type

                    if(devicetype == 1) {
                        helper.insertPair(loginmember.no, loginmember.BTAddress, deviceName, deviceHardwareAddress)
                    }

                    Log.d("dbsdkfka", "$deviceName")
                    Log.d("dbsdkfka", "$deviceHardwareAddress")

                }
            }
        }
    }

    fun pointMarking() {
        val geocoder = Geocoder(this)
        //var memberList = mutableListOf<membership>()

        tableNumber.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val num = snapshot.value.hashCode()
                if (num > 1) {
                    for (index in num - 1 downTo 1) {
                        members.child(index.toString()).child("pointName").addValueEventListener(object :
                                ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val pointName = snapshot.value.toString()

                                members.child(index.toString()).child("pointAddress").addValueEventListener(object :
                                        ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        val pointAddress = snapshot.value.toString()

                                        if (pointAddress != null) {
                                            var cor = geocoder.getFromLocationName(pointAddress, 1)
                                            var pointing = LatLng(cor[0].latitude, cor[0].longitude)

                                            var markerOptions = MarkerOptions()

                                            markerOptions.position(pointing)

                                            members.child(index.toString()).child("btaddress").addValueEventListener(object :
                                                    ValueEventListener {
                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                    val btaddress = snapshot.value.toString()

                                                    members.child(index.toString()).child(btaddress).child("cNumber").addValueEventListener(object :
                                                            ValueEventListener {
                                                        override fun onDataChange(snapshot: DataSnapshot) {
                                                            var mCount = snapshot.value.hashCode()

                                                            markerOptions.title(pointName+" 인원수: "+mCount)
                                                            markerOptions.snippet(pointAddress)
                                                            mMap.addMarker(markerOptions)
                                                        }
                                                        override fun onCancelled(error: DatabaseError) {
                                                        }
                                                    })
                                                }
                                                override fun onCancelled(error: DatabaseError) {
                                                }
                                            })
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                    }
                                })
                            }

                            override fun onCancelled(error: DatabaseError) {
                            }
                        })

                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}
