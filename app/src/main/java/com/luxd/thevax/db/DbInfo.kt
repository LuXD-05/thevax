package com.luxd.thevax.db

import android.database.sqlite.SQLiteDatabase

object DbInfo {

    const val CREATE_TABLE_USERS = """
        CREATE TABLE IF NOT EXISTS users (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            email TEXT UNIQUE NOT NULL,
            password_hash TEXT NOT NULL,
            first_name TEXT NOT NULL,
            last_name TEXT NOT NULL,
            age INTEGER NOT NULL,
            sex TEXT NOT NULL,
            therapy_id INTEGER,
            FOREIGN KEY (therapy_id) REFERENCES therapies(id) ON UPDATE CASCADE ON DELETE SET NULL
        );
    """

    const val CREATE_TABLE_THERAPIES = """
        CREATE TABLE IF NOT EXISTS therapies (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL,
            description TEXT
        );
    """

    // no vaccine entry (in this table) --> general vaccine
    const val CREATE_TABLE_THERAPY_VACCINES = """
        CREATE TABLE IF NOT EXISTS therapy_vaccines (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            therapy_id INTEGER NOT NULL,
            vaccine_id INTEGER NOT NULL,
            FOREIGN KEY (therapy_id) REFERENCES therapies(id) ON UPDATE CASCADE ON DELETE CASCADE,
            FOREIGN KEY (vaccine_id) REFERENCES vaccines(id) ON UPDATE CASCADE ON DELETE CASCADE
        );
    """

    const val CREATE_TABLE_VACCINES = """
        CREATE TABLE IF NOT EXISTS vaccines (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL,
            vaccine_type TEXT NOT NULL,
            min_age INTEGER,
            max_age INTEGER
        );
    """

    // If vaccine has no correlation with condition --> no row in this table (optional vaccine)
    // recommendation_status: "recommended", "contraindicated"
    const val CREATE_TABLE_VACCINE_CONDITIONS = """
        CREATE TABLE IF NOT EXISTS vaccine_conditions (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            vaccine_id INTEGER NOT NULL,
            condition_id INTEGER NOT NULL,
            recommendation_status TEXT,
            FOREIGN KEY (vaccine_id) REFERENCES vaccines(id) ON UPDATE CASCADE ON DELETE CASCADE,
            FOREIGN KEY (condition_id) REFERENCES conditions(id) ON UPDATE CASCADE ON DELETE CASCADE
        );
    """

    const val CREATE_TABLE_CONDITIONS = """
        CREATE TABLE IF NOT EXISTS conditions (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL
        );
    """

    const val CREATE_TABLE_USER_CONDITIONS = """
        CREATE TABLE IF NOT EXISTS user_conditions (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            user_id INTEGER NOT NULL,
            condition_id INTEGER NOT NULL,
            FOREIGN KEY (user_id) REFERENCES users(id) ON UPDATE CASCADE ON DELETE CASCADE,
            FOREIGN KEY (condition_id) REFERENCES conditions(id) ON UPDATE CASCADE ON DELETE CASCADE
        );
    """

    // history + appointments
    // status: "scheduled", "completed", "missed", "cancelled"
    const val CREATE_TABLE_RECORDS = """
        CREATE TABLE IF NOT EXISTS records (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            user_id INTEGER NOT NULL,
            vaccine_id INTEGER NOT NULL,
            status TEXT NOT NULL, -- "scheduled", "completed", "missed", "cancelled"
            date INTEGER NOT NULL,
            notes TEXT,
            FOREIGN KEY (user_id) REFERENCES users(id) ON UPDATE CASCADE ON DELETE CASCADE,
            FOREIGN KEY (vaccine_id) REFERENCES vaccines(id) ON UPDATE CASCADE ON DELETE CASCADE
        );
    """

    const val CREATE_FK_USER_THERAPIES = "CREATE INDEX IF NOT EXISTS idx_users_therapies ON users(therapy_id);"
    const val CREATE_FK_THERAPY_VACCINES = "CREATE INDEX IF NOT EXISTS idx_therapy_vaccines ON therapy_vaccines(therapy_id);"
    const val CREATE_FK_VACCINE_THERAPIES = "CREATE INDEX IF NOT EXISTS idx_vaccine_therapies ON therapy_vaccines(vaccine_id);"
    const val CREATE_FK_VACCINE_CONDITIONS = "CREATE INDEX IF NOT EXISTS idx_vaccine_conditions ON vaccine_conditions(vaccine_id);"
    const val CREATE_FK_CONDITION_VACCINES = "CREATE INDEX IF NOT EXISTS idx_condition_vaccines ON vaccine_conditions(condition_id);"
    const val CREATE_FK_USER_CONDITIONS = "CREATE INDEX IF NOT EXISTS idx_user_conditions ON user_conditions(user_id);"
    const val CREATE_FK_CONDITION_USERS = "CREATE INDEX IF NOT EXISTS idx_condition_users ON user_conditions(condition_id);"
    const val CREATE_FK_USER_RECORDS = "CREATE INDEX IF NOT EXISTS idx_user_records ON records(user_id);"
    const val CREATE_FK_VACCINE_RECORDS = "CREATE INDEX IF NOT EXISTS idx_vaccine_records ON records(vaccine_id);"

    /**
     * Creates all tables and foreign keys
     */
    fun createDB(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE_THERAPIES)
        db.execSQL(CREATE_TABLE_USERS)
        db.execSQL(CREATE_FK_USER_THERAPIES)
        db.execSQL(CREATE_TABLE_VACCINES)
        db.execSQL(CREATE_TABLE_THERAPY_VACCINES)
        db.execSQL(CREATE_FK_THERAPY_VACCINES)
        db.execSQL(CREATE_FK_VACCINE_THERAPIES)
        db.execSQL(CREATE_TABLE_CONDITIONS)
        db.execSQL(CREATE_TABLE_VACCINE_CONDITIONS)
        db.execSQL(CREATE_FK_VACCINE_CONDITIONS)
        db.execSQL(CREATE_FK_CONDITION_VACCINES)
        db.execSQL(CREATE_TABLE_USER_CONDITIONS)
        db.execSQL(CREATE_FK_USER_CONDITIONS)
        db.execSQL(CREATE_FK_CONDITION_USERS)
        db.execSQL(CREATE_TABLE_RECORDS)
        db.execSQL(CREATE_FK_USER_RECORDS)
        db.execSQL(CREATE_FK_VACCINE_RECORDS)
    }

    /**
     * Drops all tables and foreign keys
     */
    fun dropDB(db: SQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS records")
        db.execSQL("DROP TABLE IF EXISTS user_conditions")
        db.execSQL("DROP TABLE IF EXISTS vaccine_conditions")
        db.execSQL("DROP TABLE IF EXISTS conditions")
        db.execSQL("DROP TABLE IF EXISTS therapy_vaccines")
        db.execSQL("DROP TABLE IF EXISTS vaccines")
        db.execSQL("DROP TABLE IF EXISTS users")
        db.execSQL("DROP TABLE IF EXISTS therapies")
    }

}