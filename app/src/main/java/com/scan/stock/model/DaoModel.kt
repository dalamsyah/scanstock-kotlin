package com.scan.stock.model

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DaoModel {
    @Query("SELECT * FROM ScanStock ORDER BY sn ASC")
    fun getAll(): Flow<List<ScanStock>>

    @Query("SELECT * FROM ScanStock ORDER BY sn ASC")
    fun getAll2(): List<ScanStock>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg scanStock: ScanStock)

    @Delete
    fun delete(scanStock: ScanStock)

    @Query("DELETE FROM ScanStock")
    fun delete()

    @Update
    fun update(vararg scanStock: ScanStock)

    @Query("UPDATE ScanStock SET scan = :scan, scan_datetime = :scan_datetime WHERE sn = :barcode OR sn2 = :barcode")
    fun update(scan: Int, scan_datetime: String, barcode: String)

    @Query("UPDATE ScanStock SET upload = :upload WHERE sn = :barcode")
    fun updateAfterUpload(upload: Int, barcode: String)

    @Query("SELECT COUNT(sn) FROM ScanStock")
    fun totalData() : Flow<Int>

    @Query("SELECT COUNT(sn) FROM ScanStock WHERE scan > 0")
    fun totalScanData() : Flow<Int>

    @Query("SELECT * FROM ScanStock WHERE scan > 0 AND upload = 0")
    fun getListForUpload(): List<ScanStock>

    @Query("SELECT COUNT(sn) FROM ScanStock WHERE sn = :barcode")
    fun checkBeforeScan(barcode: String) : Int

    @Query("SELECT sn FROM ScanStock WHERE sn = :sn LIMIT 1")
    fun getItemSN(sn: String): String?

}