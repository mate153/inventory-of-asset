package com.devforce.inventory.utils

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.devforce.inventory.R
import com.devforce.inventory.fragments.RoomConfirmationFragment
import com.honeywell.aidc.BarcodeFailureEvent
import com.honeywell.aidc.BarcodeReadEvent
import com.honeywell.aidc.BarcodeReader
import com.honeywell.aidc.ScannerNotClaimedException
import com.honeywell.aidc.ScannerUnavailableException
import com.honeywell.aidc.TriggerStateChangeEvent

class SetupBarcodeReader(
    private var barcodeReader: BarcodeReader,
    private var requireActivity: FragmentActivity,
    private var textViewScanData: EditText?,
    private var textViewInventoryNumber: TextView?,
    private var requireContext: Context,
    private val roomReader: (p1: String) -> Unit,
    private val itemReader: (p1: String) -> Unit,
    private val fragment: Fragment
) : BarcodeReader.BarcodeListener, BarcodeReader.TriggerListener {

    fun setupReader() {
        try {
            barcodeReader.let {
                it.addBarcodeListener(this)
                it.addTriggerListener(this)
                it.claim()
                it.setProperty(
                    BarcodeReader.PROPERTY_TRIGGER_CONTROL_MODE,
                    BarcodeReader.TRIGGER_CONTROL_MODE_AUTO_CONTROL)
            }
        } catch(e: Exception) {
            showCustomToast(R.string.test_string, requireActivity, "error")
        }

        val defaultProfile = "DEFAULT"
        val profiles: List<String> = this.barcodeReader.profileNames
        var barcodeReaderProperties: Map<String, Any>?

        for (profile: String in profiles) {
            if(profile.contains(defaultProfile) && barcodeReader.loadProfile(profile)) {
                barcodeReaderProperties = barcodeReader.allProperties

                if(!barcodeReaderProperties.isNullOrEmpty()) {
                    barcodeReaderProperties.put(BarcodeReader.PROPERTY_CENTER_DECODE, true)
                    barcodeReaderProperties.put(BarcodeReader.PROPERTY_CODE_128_ENABLED, true)
                    barcodeReaderProperties.put(BarcodeReader.PROPERTY_GS1_128_ENABLED, true)
                    barcodeReaderProperties.put(BarcodeReader.PROPERTY_QR_CODE_ENABLED, true)
                    barcodeReaderProperties.put(BarcodeReader.PROPERTY_CODE_39_ENABLED, true)
                    barcodeReader.setProperties(barcodeReaderProperties)
                    break
                } else {
                    showCustomToast(R.string.test_string2, requireActivity, "info")
                }
            }
        }
    }

    override fun onBarcodeEvent(p0: BarcodeReadEvent?) {
        p0?.let {
            Handler(Looper.getMainLooper()).post {
                if (fragment.isAdded) {
                    if (it.barcodeData.toString().startsWith("6300")){
                        roomReader(it.barcodeData.trim())
                        textViewScanData?.setText(it.barcodeData)
                        textViewInventoryNumber!!.text = ""
                    } else {
                        itemReader(it.barcodeData)
                        textViewInventoryNumber!!.text = it.barcodeData
                    }
                }
            }
        }
    }

    override fun onFailureEvent(p0: BarcodeFailureEvent?) {
        p0?.let {
            Handler(Looper.getMainLooper()).post {
                textViewScanData?.setText(requireContext.getString(R.string.room_number_error_text))
            }
        }
    }

    override fun onTriggerEvent(p0: TriggerStateChangeEvent?) {
        p0?.let {
            try {
                barcodeReader.aim(it.state)
                barcodeReader.light(it.state)
                barcodeReader.decode(it.state)
            } catch (e: ScannerNotClaimedException) {
                e.printStackTrace()
            } catch (e: ScannerUnavailableException) {
                e.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}