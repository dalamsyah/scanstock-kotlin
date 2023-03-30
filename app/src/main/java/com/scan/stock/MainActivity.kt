package com.scan.stock

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.scan.stock.database.MyDB
import com.scan.stock.databinding.ActivityMainBinding
import com.scan.stock.model.ResultScanStock
import com.scan.stock.model.SampleAdapter2
import com.scan.stock.model.ScanStock
import com.scan.stock.network.NetworkConfig
import com.scan.stock.utils.getJsonDataFromAsset
import com.scan.stock.viewmodel.MyViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    lateinit var db: MyDB
    private val sampleAdapter by lazy{ SampleAdapter2() }
    lateinit var dialog: AlertDialog
    private var rack = "-"

    private val requestCodeCameraPermission = 1001
    private lateinit var cameraSource: CameraSource
    private lateinit var barcodeDetector: BarcodeDetector
    private lateinit var scanStockAdapter: FirebaseRecyclerAdapter<ScanStock, ScanStockHolder?>
    private var scannedValue = ""
    private lateinit var database: DatabaseReference

    private val intentRack =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == Activity.RESULT_OK) {
                val s = result.data?.getStringExtra("barcode").toString()
                rack = s
                binding.txtRack.text = "Rack: ${rack}"
            }
        }

    private val intentItem =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == Activity.RESULT_OK) {
                val item = result.data?.getStringExtra("barcode").toString()
                scanValidate(item)
            }
        }

//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.menu_item, menu)
//        return super.onCreateOptionsMenu(menu)
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        when (item.itemId){
//            R.id.setting -> {
//
//            }
//        }
//        return super.onOptionsItemSelected(item)
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root

        db = Room.databaseBuilder(applicationContext, MyDB::class.java, "my-db").fallbackToDestructiveMigration().build()
        val viewModel: MyViewModel = ViewModelProvider(this)[MyViewModel::class.java]
        database = Firebase.database.reference

        setProgressDialog()

        binding.txtRack.text = "Rack: $rack"

        if (ContextCompat.checkSelfPermission(
                this@MainActivity, android.Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            askForCameraPermission()
        }

//        viewModel.getCountScanned(db).observe(this) {
//            binding.txtCount.text = it.toString()
//        }
//
//        viewModel.getTotalData(db).observe(this) {
//            binding.txtTotalData.text = it.toString()
//        }

        GlobalScope.launch {
            sampleAdapter.submitList(db.daoScanStock().getAll2())
        }

//        viewModel.getList(db).observe(this) {
//            sampleAdapter.submitList(it)
//        }

        binding.btnScanManual.setOnClickListener {

            database.child("data").get().addOnSuccessListener {
                if (it.child("sn").value == "000000207917") {
                    binding.txtCount.text = it.childrenCount.toString()
                }

            }

//            scanValidate(binding.etBarcodeManual.text.toString())
        }

        binding.btnScanItem.setOnClickListener {
            intentItem.launch(Intent(this, ScannerItem::class.java))
        }

        binding.btnScanRack.setOnClickListener {
            intentRack.launch(Intent(this, ScannerItem::class.java))
        }

        binding.btnUpload.setOnClickListener {

            GlobalScope.launch {
                var index = 1
                db.daoScanStock().getListForUpload().forEach {

                    val body = mapOf(
                        "barcode" to it.sn,
                        "loc" to it.loc,
                        "zone" to it.zone,
                        "area" to it.area,
                        "rack" to it.rack,
                        "bin" to it.bin,
                        "scan_datetime" to it.scan_datetime,
                    )

                    val api = NetworkConfig().getService("https://portal.tpi-mexico.com/scanbarcode/").post(body).execute()

                    println(it)

                    if (api.isSuccessful) {

                        api.body()?.data?.forEach { it ->
                            it.sn?.let { it1 -> db.daoScanStock().updateAfterUpload(1, it1) }
                        }

                        return@forEach
                    } else {
                        return@launch
                    }


                }
            }



        }

        binding.btnCalculate.setOnClickListener {
//            viewModel.calc(context = applicationContext, db = db)


            GlobalScope.launch {

                if (db.daoScanStock().getListForUpload().isNotEmpty()) {
//                    val builder = AlertDialog.Builder(this@MainActivity)
//                    builder.setMessage("You have pending upload data")
//                        .setPositiveButton("OK",
//                            DialogInterface.OnClickListener { dialog, id ->
//                                // START THE GAME!
//                            })
//                        .setNegativeButton("Cancel",
//                            DialogInterface.OnClickListener { dialog, id ->
//                                // User cancelled the dialog
//                            })
//                    // Create the AlertDialog object and return it
//                    builder.create()
//                    builder.show()
                    GlobalScope.launch(Dispatchers.Main) {
                        Toast.makeText(applicationContext, "You have pending upload data, please upload data first.", Toast.LENGTH_SHORT).show()
                    }
                } else {

                    runOnUiThread {
                        dialog.show()
                    }

                    try {
                        val jsonFileString = getJsonDataFromAsset(applicationContext, "db2.json")
                        val gson = Gson()
                        val resultObject = object : TypeToken<ResultScanStock>() {}.type

                        var results: ResultScanStock = gson.fromJson(jsonFileString, resultObject)

                        var index = 1
                        db.daoScanStock().delete()
                        results.data.forEach {
                            db.daoScanStock().insert(it)
                            index++
                        }

                        sampleAdapter.submitList(db.daoScanStock().getAll2())

                    } finally {
                        runOnUiThread {
                            dialog.dismiss()
                        }
                    }
                }


            }

        }

        binding.circularProgressBar.apply {
            // or with animation
            setProgressWithAnimation(65f, 1000) // =1s

        }






        val query: Query = database
            .child("data")
            .limitToLast(1)

//        val query2: Query = database
//            .child("data")
//
//        query2.get().addOnSuccessListener {
//            binding.txtTotalData.text = it.childrenCount.toString()
//        }

//        query.get().addOnSuccessListener {
//            Log.i("firebase", "Got value ${it.value}")
//        }.addOnFailureListener{
//            Log.e("firebase", "Error getting data", it)
//        }

//        database.child("data").addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                for (postSnapshot in dataSnapshot.children) {
//                    // TODO: handle the post
//                }
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {
//                // Getting Post failed, log a message
//                Log.w("firebase", "loadPost:onCancelled", databaseError.toException())
//                // ...
//            }
//        })

        val options: FirebaseRecyclerOptions<ScanStock> = FirebaseRecyclerOptions.Builder<ScanStock>()
            .setQuery(query, ScanStock::class.java)
            .setLifecycleOwner(this)
            .build()

        scanStockAdapter = object : FirebaseRecyclerAdapter<ScanStock, ScanStockHolder?>(options) {
                override fun onCreateViewHolder(
                    parent: ViewGroup,
                    viewType: Int,
                ): ScanStockHolder {
                    return ScanStockHolder(LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_row, parent, false))
                }

                override fun onBindViewHolder(
                    holder: ScanStockHolder,
                    position: Int,
                    model: ScanStock,
                ) {
                    val current = getItem(position)
                    holder.bind(current)
                }
            }

        binding.apply {
            recyclerView.apply {
                layoutManager = LinearLayoutManager(this@MainActivity)
                adapter = scanStockAdapter
            }
        }

        setContentView(view)
    }

    fun setProgressDialog() {

        // Creating a Linear Layout
        val llPadding = 30
        val ll = LinearLayout(this)
        ll.orientation = LinearLayout.HORIZONTAL
        ll.setPadding(llPadding, llPadding, llPadding, llPadding)
        ll.gravity = Gravity.CENTER
        var llParam = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        llParam.gravity = Gravity.CENTER
        ll.layoutParams = llParam

        // Creating a ProgressBar inside the layout
        val progressBar = ProgressBar(this)
        progressBar.isIndeterminate = true
        progressBar.setPadding(0, 0, llPadding, 0)
        progressBar.layoutParams = llParam
        llParam = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        llParam.gravity = Gravity.CENTER

        // Creating a TextView inside the layout
        val tvText = TextView(this)
        tvText.text = "Loading ..."
        tvText.setTextColor(Color.parseColor("#000000"))
        tvText.textSize = 14f
        tvText.layoutParams = llParam
        ll.addView(progressBar)
        ll.addView(tvText)

        // Setting the AlertDialog Builder view
        // as the Linear layout created above
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setCancelable(true)
        builder.setView(ll)

        // Displaying the dialog
        dialog = builder.create()

        val window: Window? = dialog.window
        if (window != null) {
            val layoutParams = WindowManager.LayoutParams()
            layoutParams.copyFrom(dialog.window?.attributes)
            layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
            dialog.window?.attributes = layoutParams

            // Disabling screen touch to avoid exiting the Dialog
            window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }
    }

    fun scanValidate(barcode: String) {
        if (rack == "" || rack == "-") {
            Toast.makeText(applicationContext, "Scan rack first.", Toast.LENGTH_SHORT).show()
        } else {
            GlobalScope.launch {

                if (db.daoScanStock().checkBeforeScan(barcode) > 0) {
                    val sdf = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
                    val currentDate = sdf.format(Date())

                    db.daoScanStock().update(1, currentDate, barcode)

                    sampleAdapter.submitList(db.daoScanStock().getAll2())
                } else {
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Item not found!", Toast.LENGTH_SHORT).show()
                    }
                }

            }
        }
    }

    override fun onResume() {
        super.onResume()
//        codeScanner.startPreview()
    }

    override fun onPause() {
//        codeScanner.releaseResources()
        super.onPause()
    }

    override fun onStart() {
        super.onStart()
        scanStockAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        scanStockAdapter.stopListening()
    }

    private fun setupPermissions() {
        val permission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            makeRequest()
        }
    }

    private fun makeRequest() {
        ActivityCompat.requestPermissions(
            this, arrayOf(android.Manifest.permission.CAMERA),
            CAMERA_REQ
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == requestCodeCameraPermission && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupControls()
            } else {
                Toast.makeText(applicationContext, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraSource.stop()
    }

    private fun setupControls() {
        barcodeDetector =
            BarcodeDetector.Builder(this).setBarcodeFormats(Barcode.ALL_FORMATS).build()

        cameraSource = CameraSource.Builder(this, barcodeDetector)
            .setRequestedPreviewSize(1920, 1080)
            .setAutoFocusEnabled(true) //you should add this feature
            .build()

        binding.cameraSurfaceView.getHolder().addCallback(object : SurfaceHolder.Callback {
            @SuppressLint("MissingPermission")
            override fun surfaceCreated(holder: SurfaceHolder) {
                try {
                    //Start preview after 1s delay
                    cameraSource.start(holder)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            @SuppressLint("MissingPermission")
            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int,
            ) {
                try {
                    cameraSource.start(holder)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                cameraSource.stop()
            }
        })


        barcodeDetector.setProcessor(object : Detector.Processor<Barcode> {
            override fun release() {
                Toast.makeText(applicationContext, "Scanner has been closed", Toast.LENGTH_SHORT)
                    .show()
            }

            override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                val barcodes = detections.detectedItems
                if (barcodes.size() == 1) {
                    scannedValue = barcodes.valueAt(0).rawValue


                    //Don't forget to add this line printing value or finishing activity must run on main thread
                    runOnUiThread {
                        cameraSource.stop()
                        Toast.makeText(
                            this@MainActivity,
                            "value- $scannedValue",
                            Toast.LENGTH_SHORT
                        ).show()
//                        finish()
                    }
                }
            }
        })
    }

    private fun askForCameraPermission() {
        ActivityCompat.requestPermissions(
            this@MainActivity,
            arrayOf(android.Manifest.permission.CAMERA),
            requestCodeCameraPermission
        )
    }

    companion object {
        private const val CAMERA_REQ = 101
    }

}

class ScanStockHolder(val customView: View) : RecyclerView.ViewHolder(customView) {

    private val txtSn: TextView = customView.findViewById(R.id.txtSn)
    private val txtSn2: TextView = customView.findViewById(R.id.txtSn2)
    private val txtRack: TextView = customView.findViewById(R.id.txtRack)
    private val txtScan: TextView = customView.findViewById(R.id.txtScan)
    private val txtUpload: TextView = customView.findViewById(R.id.txtUpload)
    private val txtScanTime: TextView = customView.findViewById(R.id.txtScanTime)

    @SuppressLint("SetTextI18n")
    fun bind(scanStock: ScanStock) {
        txtSn.text = "SN: "+ scanStock.sn
        txtSn2.text = "SN2: "+ scanStock.sn2
        txtRack.text = "Rack: "+ scanStock.rack
        txtScan.text = "Scan: "+ scanStock.scan.toString()
        txtUpload.text = "Upload: "+ scanStock.upload.toString()
        txtScanTime.text = "Scan time: "+ scanStock.scan_datetime
    }
}