package com.scan.stock.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ScanStock (
    @ColumnInfo(name = "item_code") var item_code: String? = "",
    @ColumnInfo(name = "sn") var sn: String? = "",
    @ColumnInfo(name = "sn2") var sn2: String? = "",
    @ColumnInfo(name = "loc") var loc: String? = "",
    @ColumnInfo(name = "zone") var zone: String? = "",
    @ColumnInfo(name = "area") var area: String? = "",
    @ColumnInfo(name = "rack") var rack: String? = "",
    @ColumnInfo(name = "bin") var bin: String? = "",
    @ColumnInfo(name = "scan") var scan: Int = 0,
    @ColumnInfo(name = "upload") var upload: Int = 0,
    @ColumnInfo(name = "scan_datetime") var scan_datetime: String? = "",
){
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
}

data class ResultScanStock (
    var message: String = "",
    var success: Boolean = false,
    var data: List<ScanStock> = emptyList()
)