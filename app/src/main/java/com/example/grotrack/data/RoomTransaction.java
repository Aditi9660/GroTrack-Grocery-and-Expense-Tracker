package com.example.grotrack.data;

import androidx.sqlite.db.SupportSQLiteDatabase;

/** Runs DAO operations in one SQLite transaction, rolling back if the operation throws. */
public final class RoomTransaction {
    private RoomTransaction() { }

    public static void run(GroTrackDatabase database, Runnable operation) {
        SupportSQLiteDatabase sqlite = database.getOpenHelper().getWritableDatabase();
        sqlite.beginTransaction();
        try {
            operation.run();
            sqlite.setTransactionSuccessful();
        } finally {
            sqlite.endTransaction();
        }
    }
}
