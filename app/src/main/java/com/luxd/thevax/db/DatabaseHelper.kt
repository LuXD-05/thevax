package com.luxd.thevax.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "TheVax.db", null, 2) {

    // Called once when DB is first created
    override fun onCreate(db: SQLiteDatabase) {
        // Creates DB & FKs from schema
        DbInfo.createDB(db)

        // Seeds DB with initial static data (therapies, vaccines & conditions)
        DbInfo.seedDB(db)
    }

    // Called when upgrading DB version
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Drops DB & auto-drops indexes
        DbInfo.dropDB(db)

        // Re-creates everything
        onCreate(db)
    }

    // Called when downgrading DB version
    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    // Called when opening DB
    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        // Enables FKs
        if (!db.isReadOnly)
            db.execSQL("PRAGMA foreign_keys=ON;")
    }
}