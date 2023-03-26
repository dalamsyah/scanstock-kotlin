package com.scan.stock.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.scan.stock.model.DaoModel
import com.scan.stock.model.ScanStock

@Database(entities = [ScanStock::class], version = 2)
abstract class MyDB: RoomDatabase() {
    abstract fun daoScanStock(): DaoModel
}