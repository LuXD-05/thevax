package com.luxd.thevax.db

import android.content.Context
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context,"TheVax.db",null,1) {

    val CREATE_TABLE_USERS = """
        CREATE TABLE IF NOT EXISTS users (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            email TEXT NOT NULL UNIQUE,
            password_hash TEXT NOT NULL,
            first_name TEXT,
            last_name TEXT,
            age INTEGER,
            sex TEXT
        );
    """

    val CREATE_TABLE_THERAPIES = """
        CREATE TABLE IF NOT EXISTS therapies (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            user_id INTEGER NOT NULL,
            drug_name TEXT NOT NULL,
            drug_category TEXT NOT NULL,
            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
        );
    """

    val CREATE_TABLE_CLINICAL_CONDITIONS = """
        CREATE TABLE IF NOT EXISTS clinical_conditions (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            user_id INTEGER NOT NULL,
            condition_name TEXT NOT NULL,
            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
        );
    """

    val CREATE_TABLE_VACCINES = """
        CREATE TABLE IF NOT EXISTS vaccines (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL,
            vaccine_type TEXT NOT NULL,
            target_therapy_id INTEGER,
            FOREIGN KEY (target_therapy_id) REFERENCES therapies(id) ON DELETE SET NULL
        );
    """

    val CREATE_TABLE_RECOMMENDATIONS = """
        CREATE TABLE IF NOT EXISTS vax_recommendations (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            vaccine_id INTEGER NOT NULL,
            min_age INTEGER,
            max_age INTEGER,
            target_condition_id INTEGER, 
            recommendation_status TEXT NOT NULL,    -- "recommended"/"contraindicated"
            notes TEXT,
            FOREIGN KEY (vaccine_id) REFERENCES vaccines(id) ON DELETE CASCADE,
            FOREIGN KEY (target_condition_id) REFERENCES clinical_conditions(id) ON DELETE CASCADE
        );
    """

    val CREATE_TABLE_EVALUATIONS = """
        CREATE TABLE IF NOT EXISTS vax_evaluations (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            user_id INTEGER NOT NULL,
            vaccine_id INTEGER NOT NULL,
            status TEXT NOT NULL,
            notes TEXT,
            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
            FOREIGN KEY (vaccine_id) REFERENCES vaccines(id)
        );
    """

    val CREATE_TABLE_APPOINTMENTS = """
        CREATE TABLE IF NOT EXISTS vax_appointments (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            vax_evaluation_id INTEGER NOT NULL,
            scheduled_date INTEGER NOT NULL,
            status TEXT NOT NULL DEFAULT 'scheduled',
            notes TEXT,
            FOREIGN KEY (vax_evaluation_id) REFERENCES vax_evaluations(id) ON DELETE CASCADE
        );
    """

    val CREATE_TABLE_HISTORY = """
        CREATE TABLE IF NOT EXISTS vax_history (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            user_id INTEGER NOT NULL,
            vaccine_id INTEGER NOT NULL,
            start_date INTEGER NOT NULL,  
            last_administration_date INTEGER NOT NULL,
            notes TEXT,
            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
            FOREIGN KEY (vaccine_id) REFERENCES vaccines(id)
        );
    """

    val CREATE_FK_VACCINES_THERAPY = "CREATE INDEX IF NOT EXISTS idx_vaccines_target_therapy_id ON vaccines(target_therapy_id);"
    val CREATE_FK_THERAPIES_USER = "CREATE INDEX IF NOT EXISTS idx_therapies_user_id ON therapies(user_id);"
    val CREATE_FK_CLINICAL_CONDITIONS_USER = "CREATE INDEX IF NOT EXISTS idx_clinical_conditions_user_id ON clinical_conditions(user_id);"
    val CREATE_FK_RECOMMENDATIONS_VACCINE = "CREATE INDEX IF NOT EXISTS idx_vax_recommendations_vaccine_id ON vax_recommendations(vaccine_id);"
    val CREATE_FK_RECOMMENDATIONS_CLINICAL_CONDITION = "CREATE INDEX IF NOT EXISTS idx_vax_recommendations_target_condition_id ON vax_recommendations(target_condition_id);"
    val CREATE_FK_EVALUATIONS_USER = "CREATE INDEX IF NOT EXISTS idx_vax_evaluations_user_id ON vax_evaluations(user_id);"
    val CREATE_FK_EVALUATIONS_VACCINE = "CREATE INDEX IF NOT EXISTS idx_vax_evaluations_vaccine_id ON vax_evaluations(vaccine_id);"
    val CREATE_FK_APPOINTMENTS_EVALUATION = "CREATE INDEX IF NOT EXISTS idx_vax_appointments_evaluation_id ON vax_appointments(vax_evaluation_id);"
    val CREATE_FK_HISTORY_USER = "CREATE INDEX IF NOT EXISTS idx_vax_history_user_id ON vax_history(user_id);"
    val CREATE_FK_HISTORY_VACCINE = "CREATE INDEX IF NOT EXISTS idx_vax_history_vaccine_id ON vax_history(vaccine_id);"


    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE_USERS)
        db.execSQL(CREATE_TABLE_THERAPIES)
        db.execSQL(CREATE_FK_THERAPIES_USER)
        db.execSQL(CREATE_TABLE_CLINICAL_CONDITIONS)
        db.execSQL(CREATE_FK_CLINICAL_CONDITIONS_USER)
        db.execSQL(CREATE_TABLE_VACCINES)
        db.execSQL(CREATE_FK_VACCINES_THERAPY)
        db.execSQL(CREATE_TABLE_RECOMMENDATIONS)
        db.execSQL(CREATE_FK_RECOMMENDATIONS_VACCINE)
        db.execSQL(CREATE_FK_RECOMMENDATIONS_CLINICAL_CONDITION)
        db.execSQL(CREATE_TABLE_EVALUATIONS)
        db.execSQL(CREATE_FK_EVALUATIONS_USER)
        db.execSQL(CREATE_FK_EVALUATIONS_VACCINE)
        db.execSQL(CREATE_TABLE_APPOINTMENTS)
        db.execSQL(CREATE_FK_APPOINTMENTS_EVALUATION)
        db.execSQL(CREATE_TABLE_HISTORY)
        db.execSQL(CREATE_FK_HISTORY_USER)
        db.execSQL(CREATE_FK_HISTORY_VACCINE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS vax_history")
        db.execSQL("DROP TABLE IF EXISTS vax_appointments")
        db.execSQL("DROP TABLE IF EXISTS vax_evaluations")
        db.execSQL("DROP TABLE IF EXISTS vax_recommendations")
        db.execSQL("DROP TABLE IF EXISTS vaccines")
        db.execSQL("DROP TABLE IF EXISTS clinical_conditions")
        db.execSQL("DROP TABLE IF EXISTS therapies")
        db.execSQL("DROP TABLE IF EXISTS users")

        onCreate(db)
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)

        if (!db.isReadOnly)
            db.execSQL("PRAGMA foreign_keys=ON;")
    }
}