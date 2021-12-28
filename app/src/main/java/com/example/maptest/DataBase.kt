package com.example.maptest

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DataBase {

    val database = FirebaseDatabase.getInstance()
    val members = database.getReference("membership")

    val tableNumber = database.getReference("number")

    var answer = membership(0, "", "", "", "", "")


    fun insertMembership(member: membership) {
        Log.d("dbsqudgus","으아아아아아아아아아아아")
        tableNumber.get().addOnSuccessListener {
            val num = it.value.hashCode()

            member.no = num
            members.child(member.no.toString()).setValue(member)

            tableNumber.setValue(num + 1)
        }
    }

    fun deleteMembership(no: Int) {
        members.child(no.toString()).removeValue()
    }

    fun checkMembership(checkID: String, checkPW: String): membership{

        tableNumber.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val num = snapshot.getValue().hashCode()
                Log.d("dbsqudgus","$num")
                for (index in num downTo 0) {
                    members.child(index.toString()).child("id").addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val ID = snapshot.getValue().toString()
                            Log.d("dbsqudgus","$ID")
                            if (ID == checkID) {
                                answer.ID = ID
                                members.child(index.toString()).child("pw").addValueEventListener(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        val PW = snapshot.getValue().toString()
                                        if (PW == checkPW) {
                                            answer.PW = PW
                                            answer.no = index
                                            members.child(index.toString()).child("pointName").addValueEventListener(object : ValueEventListener {
                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                    val pointName = snapshot.getValue().toString()
                                                    answer.PointName = pointName
                                                }
                                                override fun onCancelled(error: DatabaseError) {
                                                }
                                            })
                                            members.child(index.toString()).child("pointAddress").addValueEventListener(object : ValueEventListener {
                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                    val pointAddress = snapshot.getValue().toString()
                                                    answer.PointAddress = pointAddress
                                                }
                                                override fun onCancelled(error: DatabaseError) {
                                                }
                                            })
                                            members.child(index.toString()).child("btaddress").addValueEventListener(object : ValueEventListener {
                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                    val btAddress = snapshot.getValue().toString()
                                                    answer.BTAddress = btAddress
                                                }
                                                override fun onCancelled(error: DatabaseError) {
                                                }
                                            })
                                        } else {
                                            Log.d("dbsqudgus", "비밀번호가 잘못되었습니다.")
                                        }
                                    }
                                    override fun onCancelled(error: DatabaseError) {
                                    }
                                })
                            } else {
                                Log.d("dbsqudgus", "아이디가 잘못되었습니다.")
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
        return answer
    }


    fun clearTable(no: Int, BTAddress: String) {
        members.child(no.toString()).child(BTAddress).removeValue()
    }

    fun updateMembership(no: Int, changelist: membership){
        members.child(no.toString()).setValue(changelist)
    }

    fun selectMembership(): MutableList<membership> {
        var list = mutableListOf<membership>()

        tableNumber.get().addOnSuccessListener {
            var num = it.value.hashCode()
            for (index in num downTo 0) {
                var pointName = ""
                var pointAddress = ""
                members.child(index.toString()).child("pointName").get().addOnSuccessListener {
                    pointName = it.value.toString()
                }
                members.child(index.toString()).child("pointAddress").get().addOnSuccessListener {
                    pointAddress = it.value.toString()
                }

                list.add(membership(index, "", "", pointName, pointAddress, ""))
            }
        }

        return list
    }

    fun insertPair(no :Int, BTAddress: String,deviceName: String, deviceHardwareAddress:String){
        members.child(no.toString()).child(BTAddress).child("cNumber").get().addOnSuccessListener {
            val num = it.value.hashCode()
            val takeSoloTable = soloTable(num,deviceName,deviceHardwareAddress)
            members.child(no.toString()).child(BTAddress).child(num.toString()).setValue(takeSoloTable)

            members.child(no.toString()).child(BTAddress).child("cNumber").setValue(num + 1)
        }
    }

    fun searchPoint() : membership {

        var pointIT = membership(0,"","","","","")

        tableNumber.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val num = snapshot.value.hashCode()
                if (num > 1) {
                    for (index in num - 1 downTo 1) {
                        members.child(index.toString()).child("pointName").addValueEventListener(object :
                                ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                pointIT.PointName = snapshot.value.toString()

                                members.child(index.toString()).child("pointAddress").addValueEventListener(object :
                                        ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        pointIT.PointAddress = snapshot.value.toString()
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
        return pointIT
    }

}
data class membership(var no: Int, var ID: String, var PW: String, var PointName: String, var PointAddress: String, var BTAddress: String)
data class soloTable(var no: Int, var Phonename: String, var BTAddress: String)