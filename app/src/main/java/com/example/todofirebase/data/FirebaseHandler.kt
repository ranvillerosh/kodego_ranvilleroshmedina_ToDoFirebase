package com.example.todofirebase.data

import android.app.Application
import com.google.firebase.database.FirebaseDatabase

class FirebaseHandler:Application() {
    override fun onCreate() {
        super.onCreate()
        //Enable Offline Data Persistence
        FirebaseDatabase.getInstance("https://todofirebase-a02a1-default-rtdb.asia-southeast1.firebasedatabase.app/").setPersistenceEnabled(true)

        //Keep Data Fresh
        FirebaseDatabase.getInstance("https://todofirebase-a02a1-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().keepSynced(true)
    }
}