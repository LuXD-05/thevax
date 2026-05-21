package com.luxd.thevax.db

import android.content.Context
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "TheVax.db", null, 1) {

    // TODO: gestire ogni operazione SQL in un thread in background invece che sul main thread (così da non far crashare l'app) con tipo async/await

    // OnCreate: called once when DB is created
    override fun onCreate(db: SQLiteDatabase) {
        // Creates DB & FKs from schema
        DbInfo.createDB(db)

        // TODO: inserire Therapies e Vaccines nelle tabelle (prendendo dati da fonti esterne)
        // Esempio: // db.execSQL("INSERT INTO vaccines (name, vaccine_type) VALUES ('Influenza', 'inactivated')")
    }

    // Called when upgrading DB version
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Drops DB & auto-drops indexes
        DbInfo.dropDB(db)

        // Re-creates everything
        onCreate(db)
    }

    // Not needed
    // override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    //     onUpgrade(db, oldVersion, newVersion)
    // }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        // Enables FK
        if (!db.isReadOnly)
            db.execSQL("PRAGMA foreign_keys=ON;")
    }
}