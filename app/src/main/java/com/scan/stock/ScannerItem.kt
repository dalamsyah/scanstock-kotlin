package com.scan.stock

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.scan.stock.database.MyDB
import com.scan.stock.databinding.ActivityMain2Binding
import com.scan.stock.databinding.ActivityMainBinding
import com.scan.stock.model.SampleAdapter2
import java.io.IOException

class ScannerItem : AppCompatActivity() {

    private lateinit var binding: ActivityMain2Binding
    lateinit var db: MyDB
    private val sampleAdapter by lazy{ SampleAdapter2() }
    lateinit var dialog: AlertDialog
    private var rack = "-"

    private val requestCodeCameraPermission = 1001
    private lateinit var cameraSource: CameraSource
    private lateinit var barcodeDetector: BarcodeDetector
    private var scannedValue = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMain2Binding.inflate(layoutInflater)
        val view = binding.root

        val aniSlide: Animation =
            AnimationUtils.loadAnimation(this@ScannerItem, R.anim.scanner_animation)
        binding.barcodeLine.startAnimation(aniSlide)

        setupControls()

        setContentView(view)
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
                height: Int
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
//                        try {
//                            if (ActivityCompat.checkSelfPermission(
//                                    this@ScannerItem,
//                                    Manifest.permission.CAMERA
//                                ) != PackageManager.PERMISSION_GRANTED
//                            ) {
//                                cameraSource.start()
//                            }
//
//                        } catch (e: IOException) {
//                            e.printStackTrace()
//                        }

//                        Toast.makeText(
//                            this@ScannerItem,
//                            "$scannedValue",
//                            Toast.LENGTH_SHORT
//                        ).show()

                        val data = Intent()
                        data.putExtra("barcode", "$scannedValue")

                        setResult(Activity.RESULT_OK, data)
                        finish()
                    }
                }
            }
        })
    }

}