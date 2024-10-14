package com.devforce.inventory

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import com.devforce.inventory.databinding.ActivityMainBinding
import com.honeywell.aidc.AidcManager
import com.honeywell.aidc.BarcodeReader
import com.honeywell.aidc.InvalidScannerNameException

class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private var barcodeReader: BarcodeReader? = null
    private lateinit var manager: AidcManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AidcManager.create(this) { adcManager ->
            manager = adcManager
            try {
                barcodeReader = manager.createBarcodeReader()
            } catch (e: InvalidScannerNameException) {
                e.printStackTrace()
            }
        }

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
    }

    fun getBarcodeObject(): BarcodeReader? {
        return barcodeReader
    }

    override fun onDestroy() {
        super.onDestroy()

        if (barcodeReader != null){
            barcodeReader!!.close()
            barcodeReader = null
        }
        manager.close()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}