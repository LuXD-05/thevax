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
            recommendation_status TEXT,
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

    /**
     * Seeds all tables with initial data
     */
    fun seedDB(db: SQLiteDatabase) {
        db.execSQL(SEED_THERAPIES)
        db.execSQL(SEED_CONDITIONS)
        db.execSQL(SEED_VACCINES)
        db.execSQL(SEED_THERAPY_VACCINES)
        db.execSQL(SEED_VACCINE_CONDITIONS)
    }

    // SEEDS

    private const val SEED_THERAPIES = """
    INSERT INTO therapies (name, description) VALUES
        ('Anti-TNF', 'Inibitori del TNF-alfa (es. Adalimumab, Etanercept, Infliximab). Aumentano il rischio di infezioni batteriche e tubercolosi. I vaccini vivi attenuati sono controindicati.'),
        ('Anti-IL17', 'Inibitori dell''interleuchina-17 (es. Secukinumab, Ixekizumab). Usati in psoriasi e artrite psoriasica. Vaccini inattivati sicuri, vivi attenuati da evitare.'),
        ('Anti-IL23', 'Inibitori dell''interleuchina-23 (es. Guselkumab, Risankizumab). Target piu'' selettivo, immunosoppressione generalmente meno marcata.'),
        ('Anti-IL12/23', 'Inibitori combinati IL-12/23 (es. Ustekinumab). Modulazione della via Th1/Th17.'),
        ('Anti-CD20', 'Depletenti i linfociti B (es. Rituximab). Riduzione della risposta anticorpale ai vaccini; vaccinare prima della somministrazione quando possibile.'),
        ('Altri immunosoppressori', 'Farmaci immunomodulatori sistemici (es. Metotrexato, Ciclosporina, corticosteroidi ad alte dosi a lungo termine).');
    """

    private const val SEED_CONDITIONS = """
    INSERT INTO conditions (name) VALUES
        ('Diabete mellito'),
        ('Malattia polmonare cronica'),
        ('Cardiopatia cronica'),
        ('Epatopatia cronica'),
        ('Insufficienza renale cronica'),
        ('Asplenia anatomica o funzionale'),
        ('Immunodeficienza congenita o acquisita'),
        ('Infezione da HIV'),
        ('Neoplasia attiva'),
        ('Trapianto d''organo solido'),
        ('Trapianto di midollo osseo'),
        ('Terapia immunosoppressiva a lungo termine'),
        ('Deficit del complemento'),
        ('Emoglobinopatia (talassemia, anemia falciforme)');
    """

    private const val SEED_VACCINES = """
    INSERT INTO vaccines (name, vaccine_type, min_age, max_age) VALUES
        ('Influenza stagionale (quadrivalente inattivata)', 'inattivato', 6, NULL),
        ('Pneumococco (PCV20 / PCV15 + PPSV23)', 'coniugato', 18, NULL),
        ('Epatite B (Engerix-B / Recombivax HB)', 'ricombinante', 0, NULL),
        ('Epatite A (Harvix / Vaqta)', 'inattivato', 12, NULL),
        ('MPR (Morbillo-Parotite-Rosolia)', 'vivo attenuato', 12, NULL),
        ('Varicella (Varivax)', 'vivo attenuato', 12, NULL),
        ('Herpes Zoster ricombinante adiuvato (Shingrix/RZV)', 'ricombinante', 18, NULL),
        ('Herpes Zoster vivo attenuato (Zostavax/ZVL)', 'vivo attenuato', 50, NULL),
        ('HPV (Gardasil 9)', 'ricombinante', 9, 45),
        ('Meningococco ACWY (Menveo)', 'coniugato', 2, NULL),
        ('Meningococco B (Bexsero / Trumenba)', 'ricombinante', 10, NULL),
        ('Haemophilus influenzae tipo b (Hib)', 'coniugato', 2, NULL),
        ('Difterite-Tetano-Pertosse (dTpa)', 'toxoide/inattivato', 7, NULL),
        ('COVID-19 (mRNA / proteina Spike)', 'mRNA/ricombinante', 6, NULL),
        ('Rotavirus', 'vivo attenuato', 6, 32),
        ('Febbre tifoide', 'inattivato', 2, NULL),
        ('TBE (encefalite da zecca)', 'inattivato', 1, NULL);
    """

    // Check results in view
    private const val SEED_THERAPY_VACCINES = """
    INSERT INTO therapy_vaccines (therapy_id, vaccine_id, recommendation_status) VALUES
        (1, 1, 'recommended'), (1, 2, 'recommended'), (1, 7, 'recommended'), (1, 14, 'recommended'),
        (2, 1, 'recommended'), (2, 2, 'recommended'), (2, 7, 'recommended'), (2, 14, 'recommended'),
        (3, 1, 'recommended'), (3, 2, 'recommended'), (3, 7, 'recommended'), (3, 14, 'recommended'),
        (4, 1, 'recommended'), (4, 2, 'recommended'), (4, 7, 'recommended'), (4, 14, 'recommended'),
        (5, 1, 'recommended'), (5, 2, 'recommended'), (5, 3, 'recommended'), (5, 7, 'recommended'), (5, 14, 'recommended'),
        (6, 1, 'recommended'), (6, 2, 'recommended'), (6, 7, 'recommended'), (6, 14, 'recommended'),
        (1, 5, 'contraindicated'), (1, 6, 'contraindicated'), (1, 8, 'contraindicated'), (1, 15, 'contraindicated'),
        (2, 5, 'contraindicated'), (2, 6, 'contraindicated'), (2, 8, 'contraindicated'), (2, 15, 'contraindicated'),
        (3, 5, 'contraindicated'), (3, 6, 'contraindicated'), (3, 8, 'contraindicated'), (3, 15, 'contraindicated'),
        (4, 5, 'contraindicated'), (4, 6, 'contraindicated'), (4, 8, 'contraindicated'), (4, 15, 'contraindicated'),
        (5, 5, 'contraindicated'), (5, 6, 'contraindicated'), (5, 8, 'contraindicated'), (5, 15, 'contraindicated'),
        (6, 5, 'contraindicated'), (6, 6, 'contraindicated'), (6, 8, 'contraindicated'), (6, 15, 'contraindicated');
"""

    private const val SEED_VACCINE_CONDITIONS = """
    INSERT INTO vaccine_conditions (vaccine_id, condition_id, recommendation_status) VALUES
        (1, 1, 'recommended'), (1, 2, 'recommended'), (1, 3, 'recommended'), (1, 4, 'recommended'), (1, 5, 'recommended'), (1, 7, 'recommended'), (1, 8, 'recommended'), (1, 9, 'recommended'), (1, 10, 'recommended'), (1, 11, 'recommended'), (1, 12, 'recommended'), (1, 14, 'recommended'),
        (2, 1, 'recommended'), (2, 2, 'recommended'), (2, 3, 'recommended'), (2, 4, 'recommended'), (2, 5, 'recommended'), (2, 6, 'recommended'), (2, 7, 'recommended'), (2, 8, 'recommended'), (2, 9, 'recommended'), (2, 10, 'recommended'), (2, 11, 'recommended'), (2, 12, 'recommended'), (2, 13, 'recommended'), (2, 14, 'recommended'),
        (3, 1, 'recommended'), (3, 4, 'recommended'), (3, 5, 'recommended'), (3, 7, 'recommended'), (3, 8, 'recommended'), (3, 9, 'recommended'), (3, 10, 'recommended'), (3, 11, 'recommended'), (3, 14, 'recommended'),
        (4, 4, 'recommended'), (4, 8, 'recommended'),
        (5, 7, 'contraindicated'), (5, 9, 'contraindicated'), (5, 10, 'contraindicated'), (5, 11, 'contraindicated'), (5, 12, 'contraindicated'), (5, 8, 'recommended'),
        (6, 7, 'contraindicated'), (6, 9, 'contraindicated'), (6, 10, 'contraindicated'), (6, 11, 'contraindicated'), (6, 12, 'contraindicated'), (6, 8, 'recommended'),
        (7, 7, 'recommended'), (7, 8, 'recommended'), (7, 9, 'recommended'), (7, 10, 'recommended'), (7, 11, 'recommended'), (7, 12, 'recommended'),
        (8, 7, 'contraindicated'), (8, 8, 'contraindicated'), (8, 9, 'contraindicated'), (8, 10, 'contraindicated'), (8, 11, 'contraindicated'), (8, 12, 'contraindicated'),
        (9, 7, 'recommended'), (9, 8, 'recommended'), (9, 12, 'recommended'),
        (10, 6, 'recommended'), (10, 7, 'recommended'), (10, 8, 'recommended'), (10, 13, 'recommended'), (10, 14, 'recommended'),
        (11, 6, 'recommended'), (11, 7, 'recommended'), (11, 13, 'recommended'), (11, 14, 'recommended'),
        (12, 6, 'recommended'), (12, 7, 'recommended'), (12, 8, 'recommended'), (12, 11, 'recommended'), (12, 13, 'recommended'), (12, 14, 'recommended'),
        (13, 2, 'recommended'), (13, 3, 'recommended'), (13, 5, 'recommended'),
        (14, 1, 'recommended'), (14, 2, 'recommended'), (14, 3, 'recommended'), (14, 4, 'recommended'), (14, 5, 'recommended'), (14, 7, 'recommended'), (14, 8, 'recommended'), (14, 9, 'recommended'), (14, 10, 'recommended'), (14, 11, 'recommended'), (14, 12, 'recommended'), (14, 14, 'recommended'),
        (15, 7, 'contraindicated'), (15, 9, 'contraindicated'), (15, 10, 'contraindicated'), (15, 11, 'contraindicated'), (15, 12, 'contraindicated');
"""

}