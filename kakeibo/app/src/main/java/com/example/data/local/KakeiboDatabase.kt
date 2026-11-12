package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.model.MonthlyBudget
import com.example.data.model.ReflectionJournal
import com.example.data.model.TransactionEntity
import com.example.data.model.Wallet

@Database(
    entities = [
        Wallet::class,
        MonthlyBudget::class,
        TransactionEntity::class,
        ReflectionJournal::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class KakeiboDatabase : RoomDatabase() {
    abstract fun kakeiboDao(): KakeiboDao

    companion object {
        @Volatile
        private var INSTANCE: KakeiboDatabase? = null

        fun getDatabase(context: Context): KakeiboDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KakeiboDatabase::class.java,
                    "kakeibo_pocket.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
