package com.screentime.blocker

import android.app.Application
import androidx.room.Room
import com.screentime.blocker.data.local.AppBlockerDatabase

class BlockerApp : Application() {
    lateinit var database: AppBlockerDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = Room.databaseBuilder(
            applicationContext,
            AppBlockerDatabase::class.java,
            "screentime_blocker.db"
        ).fallbackToDestructiveMigration().build()
    }

    companion object {
        lateinit var instance: BlockerApp
            private set
    }
}
