package com.example.uberclone

import android.app.Application
import android.util.Log
import com.parse.Parse
import com.parse.ParseACL
import com.parse.ParseObject
import com.parse.ParseUser

class StarterApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        //Enable Local Datastore
        Parse.enableLocalDatastore(this)

        //Add your initialization code here
        Parse.initialize(Parse.Configuration.Builder(applicationContext)
            .applicationId("O4zuS9oWLDQq7SJqDXGXh7stVss9xfx55tDAghLg")
            .clientKey("9qcy0zQSYSCnm5TQNWaSSmcs85sAYyR58lm1G732")
            .server("https://parseapi.back4app.com/")
            .build()
        )

//        ParseUser.enableAutomaticUser()
////        ParseUser.getCurrentUser().increment("RunCount")
////        ParseUser.getCurrentUser().saveInBackground()


        val defaultACL = ParseACL()
        defaultACL.publicReadAccess = true
        defaultACL.publicWriteAccess = true
        ParseACL.setDefaultACL(defaultACL, true)

    }
}