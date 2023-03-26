package com.scan.stock.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.scan.stock.database.MyDB
import com.scan.stock.model.ResultScanStock
import com.scan.stock.model.ScanStock
import com.scan.stock.network.NetworkConfig
import com.scan.stock.utils.getJsonDataFromAsset
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyViewModel(
    private val coroutineScope: CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
) : ViewModel() {

//    private var _totalData = 0
//    val totalData: Int
//        get() = _totalData

    private val _totalData = MutableLiveData<Int>()
    val totalData: LiveData<Int>
        get() = _totalData

    private val _count = MutableLiveData<Int>()
    val count: LiveData<Int>
        get() = _count

    fun getList(db: MyDB): LiveData<List<ScanStock>> {
        val data: Flow<List<ScanStock>> = db.daoScanStock().getAll()

        return data.asLiveData()
    }

    fun getCountScanned(db: MyDB) : LiveData<Int> {
        return db.daoScanStock().totalScanData().asLiveData()
    }

    fun getTotalData(db: MyDB) : LiveData<Int> {
        return db.daoScanStock().totalData().asLiveData()
    }

    fun calc(context: Context, db: MyDB) {
        GlobalScope.launch {
            val jsonFileString = getJsonDataFromAsset(context, "db2.json")
            val gson = Gson()
            val resultObject = object : TypeToken<ResultScanStock>() {}.type

            var results: ResultScanStock = gson.fromJson(jsonFileString, resultObject)

            var index = 1
            results.data.forEach {
                db.daoScanStock().insert(it)
                index++
            }
        }
    }

    fun fetchData(context: Context) {

        coroutineScope.launch {
            coroutineScope { // limits the scope of concurrency

                val jsonFileString = getJsonDataFromAsset(context, "db.json")
                val gson = Gson()
                val resultObject = object : TypeToken<ResultScanStock>() {}.type

                var results: ResultScanStock = gson.fromJson(jsonFileString, resultObject)

                var index = 1
                results.data.map { // is a shorter way to write IntRange(0, 10)
                    async(Dispatchers.IO) { // async means "concurrently", context goes here
//                    list.add(repository.getLastGame(x) ?: MutableLiveData<Task>(Task(cabinId = x)))
//                        Log.d("TGAS", "fetchData: $it")
                        _count.value = index
                        index++
                    }
                }.awaitAll() // waits all of them
            }
        }

//        val total = GlobalScope.async {
//
//            val jsonFileString = getJsonDataFromAsset(context, "db.json")
//
//            val gson = Gson()
//            val resultObject = object : TypeToken<ResultScanStock>() {}.type
//
//            var results: ResultScanStock = gson.fromJson(jsonFileString, resultObject)
////            _totalData.value = results.data.size
//            return@async results.data.size
////        results.data.forEachIndexed { idx, person -> Log.i("TAGS", "> Item $idx:\n$person") }
//
////            var index = 1
////            results.data.forEachIndexed{ idx, item ->
////                _count.value = index++
//////                Log.i("TAGS", "> Item $idx:\n$item")
////            }
//
////            doLongRunningTask(context, results)
//        }
//
//        GlobalScope.launch {
//            _totalData.value = total.await()
//        }

    }

    suspend fun doLongRunningTask(context: Context, results: ResultScanStock) {
        withContext(Dispatchers.Default) {



            var index = 1
            results.data.forEachIndexed{ idx, item ->
//                _count.value = index++
//                Log.i("TAGS", "> Item $idx:\n$item")
            }

        }
    }

    override fun onCleared() {
        coroutineScope.cancel()
    }
}