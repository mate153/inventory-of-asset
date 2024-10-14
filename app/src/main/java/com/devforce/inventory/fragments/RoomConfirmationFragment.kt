package com.devforce.inventory.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devforce.inventory.MainActivity
import com.devforce.inventory.R
import com.devforce.inventory.config.DevForceConfig
import com.devforce.inventory.config.DevForceSave
import com.devforce.inventory.controllers.ExportCsv
import com.devforce.inventory.controllers.createJsonRow
import com.devforce.inventory.controllers.jsonReader
import com.devforce.inventory.controllers.saveToJson
import com.devforce.inventory.controllers.updateJsonRow
import com.devforce.inventory.databinding.FragmentRoomconfirmationBinding
import com.devforce.inventory.service.CsvAdapter
import com.devforce.inventory.service.Filters
import com.devforce.inventory.service.Statistics
import com.devforce.inventory.utils.ButtonVisibility
import com.devforce.inventory.utils.SetupBarcodeReader
import com.devforce.inventory.utils.ShowDialog
import com.devforce.inventory.utils.showCustomToast
import com.honeywell.aidc.BarcodeReader
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException
import kotlin.properties.Delegates


@RequiresApi(Build.VERSION_CODES.O)
class RoomConfirmationFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CsvAdapter
    private lateinit var barcodeReader: BarcodeReader
    private lateinit var rootView: View
    private lateinit var rowData: List<List<String>>
    private var _binding: FragmentRoomconfirmationBinding? = null
    private var textViewScanData: EditText? = null
    private var textViewInventoryNumber: TextView? = null
    private var textViewCounterTotal: TextView? = null
    private var textViewCounterToFind: TextView? = null
    private var readRoomBarcode: String = ""
    private var readItemBarcode: String = ""
    private var selectedBtn: String = "show_open"
    private var isViewCreated: Boolean = false
    private var availableRooms: TextView? = null
    private var visitedRooms: TextView? = null
    private var finishedRooms: TextView? = null
    private var allItems: TextView? = null
    private var checkedItems: TextView? = null
    private var statistics: Statistics = Statistics()
    private lateinit var setupBarcodeReader: SetupBarcodeReader
    private val buttonVisibility: ButtonVisibility = ButtonVisibility()
    private lateinit var newDialog: ShowDialog
    private lateinit var filters: Filters
    private var isRoomLess: Boolean = false

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRoomconfirmationBinding.inflate(inflater, container, false)

        if(DevForceSave.Auth.LOGGED_IN_AS == "" && DevForceConfig.Auth.NEED_AUTH) findNavController().navigate(R.id.action_FROM_RoomConfirmation_to_MenuFragment)

        rootView = binding.root
        rowData = jsonReader(requireContext())
        recyclerView = rootView.findViewById(R.id.recyclerView)
        textViewScanData = rootView.findViewById(R.id.textViewScanData)
        textViewInventoryNumber = rootView.findViewById(R.id.textViewInventoryNumber)
        textViewCounterTotal = rootView.findViewById(R.id.counterTotal)
        textViewCounterToFind = rootView.findViewById(R.id.counterToFind)
        availableRooms = rootView.findViewById(R.id.roomConfirmationAvailableRoomsValue)
        visitedRooms = rootView.findViewById(R.id.roomConfirmationVisitedRoomsValue)
        finishedRooms = rootView.findViewById(R.id.roomConfirmationFinishedRoomsValue)
        allItems = rootView.findViewById(R.id.roomConfirmationAllItemsValue)
        checkedItems = rootView.findViewById(R.id.roomConfirmationCheckedItemsValue)
        barcodeReader = (requireActivity() as MainActivity).getBarcodeObject()!!
        availableRooms?.text = "0"
        visitedRooms?.text = "0"
        finishedRooms?.text = "0"
        allItems?.text = "0"
        checkedItems?.text = "0"
        newDialog = ShowDialog(requireContext())
        filters = Filters(this, recyclerView, textViewScanData, textViewInventoryNumber, requireContext())

        // Setup Barcode Reader
        setupBarcodeReader = SetupBarcodeReader(
            barcodeReader, requireActivity(), textViewScanData, textViewInventoryNumber, requireContext(),
            ::roomReader, ::itemReader, this
        )
        setupBarcodeReader.setupReader()

        // Room number field OnEdit listener
        textViewScanData!!.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER)) {
                val isRoomFound = foundRoomByWritten(textViewScanData!!.text.toString())
                if (isRoomFound)  {
                    roomReader(textViewScanData!!.text.toString())
                } else {
                    newDialog.info(getString(R.string.dialog_room_not_found))
                }

                textViewScanData!!.clearFocus()
                val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(textViewScanData!!.windowToken, 0)

                true
            } else {
                false
            }
        }

        // Check if Cost Center exist
        checkCostCenter()

        return rootView
    }

    private fun roomReader(p1: String) {
        newDialog.loading(getString(R.string.dialog_loading_text))

        readRoomBarcode = p1

        val roomExists = rowData.any { row ->
            if (row.size > 7) {
                val roomNumber = row[6] + row[7]
                roomNumber == readRoomBarcode
            } else {
                false
            }
        }

        if (roomExists) {
            selectedBtn = "show_open"
            filters.filterByRoomNumber(rowData, readRoomBarcode, textViewCounterTotal, textViewCounterToFind) { detailsButtonSetting(it) }
            isCheckedOut = false

            newDialog.dismissLoading("loading") {_ ->}
        } else {
            showCustomToast(R.string.dialog_room_not_found, requireActivity(), "error")
            newDialog.dismissLoading("loading") {_ ->}
        }
    }

    private fun itemReader(p1: String) {
        if ( !isCheckedOut ) {
            newDialog.loading(getString(R.string.dialog_loading_text))
            readItemBarcode = p1.trim()
            checkCurrentRoom(readItemBarcode)
            updateRowByBarcode(p1)
            newDialog.dismissLoading("loading") {_ ->}
        } else {
            showCustomToast(R.string.dialog_not_in_room, requireActivity(), "error")
        }
    }

    private var isCheckedOut: Boolean by Delegates.observable(true) { _, _, _ ->
        if (isViewCreated) {
            updateButtonsVisibility()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isViewCreated = true
        updateButtonsVisibility()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateButtonsVisibility() {
        newDialog.loading(getString(R.string.dialog_loading_text))

        if (isCheckedOut) checkedOutSettings()
        else notCheckedOutSettings()

        newDialog.dismissLoading("loading") {_ ->}
    }

    @SuppressLint("NotifyDataSetChanged", "SimpleDateFormat")
    @OptIn(DelicateCoroutinesApi::class)
    private fun updateRowByBarcode(barcodeData: String) {
        if (!isAdded || isDetached) {
            return
        }

        val matchingRows = rowData.filter { it.getOrNull(4) == barcodeData.trim() }

        if (matchingRows.isNotEmpty()) {
            val updatedRows = ArrayList(rowData)
            var itemChecked = false
            var itemAlreadyChecked = false

            for (matchingRow in matchingRows) {
                if (matchingRow.last() == "false") {
                    var updatedRow = matchingRow.dropLast(1) + listOf("true")
                    updatedRow = updatedRow.toMutableList().apply {
                        this[size - 4] = "I"
                    }.toList()
                    val index = rowData.indexOf(matchingRow)
                    updatedRows[index] = updatedRow

                    saveToJson(requireContext(), updatedRow, index)
                    itemChecked = true
                } else {
                    itemAlreadyChecked = true
                }
            }

            rowData = updatedRows

            GlobalScope.launch(Dispatchers.Main) {
                if (isAdded && !isDetached) {
                    if (itemChecked) {
                        val clickedItemInventoryNumber = matchingRows[0][4]
                        textViewInventoryNumber?.text = clickedItemInventoryNumber
                        showCustomToast(R.string.item_checked_success, requireActivity(), "success")
                    } else if (itemAlreadyChecked) {
                        showCustomToast(R.string.item_already_checked, requireActivity(), "info")
                    }

                    if (!::adapter.isInitialized) {
                        adapter = CsvAdapter(emptyList())
                        recyclerView.adapter = adapter
                        recyclerView.layoutManager = LinearLayoutManager(requireContext())
                    }

                    adapter.notifyDataSetChanged()
                    when (selectedBtn) {
                        "show_alone" -> {
                            filters.aloneItems("false", rowData, textViewCounterTotal, textViewCounterToFind) { detailsButtonSetting(it) }
                        }
                        "show_found" -> {
                            filters.showFilteredByCheckedItems("true", rowData, isRoomLess, readRoomBarcode, textViewCounterTotal, textViewCounterToFind) { detailsButtonSetting(it) }
                        }
                        "show_open" -> {
                            filters.showFilteredByCheckedItems("false", rowData, isRoomLess, readRoomBarcode, textViewCounterTotal, textViewCounterToFind) { detailsButtonSetting(it) }
                        }
                    }
                }
            }
        } else {
            GlobalScope.launch(Dispatchers.Main) {
                if (isAdded && !isDetached) {
                    showCustomToast(R.string.item_not_found, requireActivity(), "error")
                }
            }
        }
    }

    private fun checkedOutSettings() {
        buttonVisibility.isCheckedOut(binding)
        textViewScanData?.setText("")
        textViewScanData?.isFocusable = true
        textViewScanData?.isFocusableInTouchMode = true

        // Refresh Statistics Data
        fillManagement()

        // Exit
        binding.roomConfirmationExitButton.setOnClickListener {
            newDialog.confirmation(
                getString(R.string.dialog_exit_title),
                getString(R.string.dialog_exit_message),
                getString(R.string.dialog_yes_btn),
                getString(R.string.dialog_no_btn)) { result ->
                if (result) {
                    activity?.supportFragmentManager?.popBackStack()
                }
            }
        }

        // Room-less items
        binding.roomConfirmationAloneItemsButton.setOnClickListener {
            newDialog.loading(getString(R.string.dialog_loading_text))

            isCheckedOut = false
            textViewScanData?.setText("")
            selectedBtn = "show_alone"
            isRoomLess = true
            readRoomBarcode = ""
            filters.aloneItems("false", rowData, textViewCounterTotal, textViewCounterToFind) { detailsButtonSetting(it) }

            newDialog.dismissLoading("loading") {_ ->}
        }

        // Export
        binding.roomConfirmationExportButton.setOnClickListener {
            if(DevForceConfig.Export.NEED_CONFIRMATION)
                newDialog.confirmation(
                    getString(R.string.dialog_export_title),
                    getString(R.string.dialog_export_message),
                    getString(R.string.dialog_yes_btn),
                    getString(R.string.dialog_no_btn)) { result ->
                    if (result) {
                        val export = ExportCsv(rowData, requireContext())
                        export.startExportProcess { success ->
                            if(success) {
                                showCustomToast(R.string.notify_logout_success, requireActivity(), "success")
                                findNavController().navigate(R.id.LoginFragment)
                            }
                        }
                    }
                }
            else {
                val export = ExportCsv(rowData, requireContext())
                export.startExportProcess { success ->
                    if(success) {

                        findNavController().navigate(R.id.LoginFragment)
                    }
                }
            }
        }
    }

    private fun notCheckedOutSettings() {
        buttonVisibility.isNotCheckedOut(binding)
        textViewScanData?.isFocusable = false
        textViewScanData?.isFocusableInTouchMode = false

        detailsButtonSetting(null)

        binding.roomConfirmationNewButton.setOnClickListener {
            openCreationDialog()
        }

        binding.roomConfirmationShowFoundButton.setOnClickListener {
            newDialog.loading(getString(R.string.dialog_loading_text))

            textViewInventoryNumber!!.text = ""
            detailsButtonSetting(null)
            filters.showFilteredByCheckedItems("true", rowData, isRoomLess, readRoomBarcode, textViewCounterTotal, textViewCounterToFind) { detailsButtonSetting(it) }

            newDialog.dismissLoading("loading") {_ ->}
        }
        binding.roomConfirmationShowOpenButton.setOnClickListener {
            newDialog.loading(getString(R.string.dialog_loading_text))

            textViewInventoryNumber!!.text = ""
            detailsButtonSetting(null)
            filters.showFilteredByCheckedItems("false", rowData, isRoomLess, readRoomBarcode, textViewCounterTotal, textViewCounterToFind) { detailsButtonSetting(it) }

            newDialog.dismissLoading("loading") {_ ->}
        }
        binding.roomConfirmationBackButton.setOnClickListener {
            detailsButtonSetting(null)
            isCheckedOut = true
            textViewScanData?.setText("")
            textViewInventoryNumber!!.text = ""
            isRoomLess = false
            adapter = CsvAdapter(emptyList())
            recyclerView.adapter = adapter
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun openCreationDialog() {
        newDialog.assetCreateDialog(requireContext(), requireActivity()) { response ->
            if (response != null) {
                if (createNewRow(response)) {
                    showCustomToast(
                        R.string.notify_asset_update_success,
                        requireActivity(),
                        "success"
                    )
                } else {
                    showCustomToast(
                        R.string.notify_asset_update_fail,
                        requireActivity(),
                        "error"
                    )
                }
            }

            when (selectedBtn) {
                "show_alone" -> {
                    newDialog.loading(getString(R.string.dialog_loading_text))

                    filters.aloneItems("false", rowData, textViewCounterTotal, textViewCounterToFind) { detailsButtonSetting(it) }

                    newDialog.dismissLoading("loading") {_ ->}
                }

                "show_found" -> {
                    newDialog.loading(getString(R.string.dialog_loading_text))

                    filters.showFilteredByCheckedItems(
                        "true",
                        rowData,
                        isRoomLess,
                        readRoomBarcode,
                        textViewCounterTotal,
                        textViewCounterToFind
                    ) { detailsButtonSetting(it) }

                    newDialog.dismissLoading("loading") {_ ->}
                }

                "show_open" -> {
                    newDialog.loading(getString(R.string.dialog_loading_text))

                    filters.showFilteredByCheckedItems(
                        "false",
                        rowData,
                        isRoomLess,
                        readRoomBarcode,
                        textViewCounterTotal,
                        textViewCounterToFind
                    ) { detailsButtonSetting(it) }

                    newDialog.dismissLoading("loading") {_ ->}
                }
            }

            textViewInventoryNumber!!.text = ""
            updateButtonsVisibility()
        }
    }

    private fun detailsButtonSetting(p1: List<String>?) {
        val detailsButton = binding.roomConfirmationDetailsButton

        val colorResId = if(p1 == null || textViewInventoryNumber!!.text == "") {
            R.drawable.button_design_inactive
        } else {
            R.drawable.button_light_design
        }
        val design = ResourcesCompat.getDrawable(requireContext().resources, colorResId, null)
        @Suppress("DEPRECATION")
        detailsButton.setBackgroundDrawable(design)

        detailsButton.setOnClickListener {
            if(p1 != null || textViewInventoryNumber!!.text != "") {
                newDialog.assetDetailsDialog(requireContext(), p1) { response ->
                    if (response != null) {
                        if (updateSelectedRow(p1, response)) {
                            showCustomToast(
                                R.string.notify_asset_update_success,
                                requireActivity(),
                                "success"
                            )
                        } else {
                            showCustomToast(
                                R.string.notify_asset_update_fail,
                                requireActivity(),
                                "error"
                            )
                        }
                    }

                    when (selectedBtn) {
                        "show_alone" -> {
                            newDialog.loading(getString(R.string.dialog_loading_text))

                            filters.aloneItems("false", rowData, textViewCounterTotal, textViewCounterToFind) { detailsButtonSetting(it) }

                            newDialog.dismissLoading("loading") {_ ->}
                        }

                        "show_found" -> {
                            newDialog.loading(getString(R.string.dialog_loading_text))

                            filters.showFilteredByCheckedItems(
                                "true",
                                rowData,
                                isRoomLess,
                                readRoomBarcode,
                                textViewCounterTotal,
                                textViewCounterToFind
                            ) { detailsButtonSetting(it) }

                            newDialog.dismissLoading("loading") {_ ->}
                        }

                        "show_open" -> {
                            newDialog.loading(getString(R.string.dialog_loading_text))

                            filters.showFilteredByCheckedItems(
                                "false",
                                rowData,
                                isRoomLess,
                                readRoomBarcode,
                                textViewCounterTotal,
                                textViewCounterToFind
                            ) { detailsButtonSetting(it) }

                            newDialog.dismissLoading("loading") {_ ->}
                        }
                    }

                    textViewInventoryNumber!!.text = ""
                    updateButtonsVisibility()
                }
            }
        }
    }

    private fun createNewRow(response: Triple<String, String, String>): Boolean {
        return try {
            val newRow: List<String> = listOf("", "", "", response.third, "", response.first, "", response.second, "", "B", DevForceSave.Auth.LOGGED_IN_AS, "", "true" )
            val updatedRow = ArrayList(rowData)

            updatedRow.add(newRow)
            rowData = updatedRow

            createJsonRow(requireContext(), newRow)

            true
        } catch(e: IOException) {
            Log.e("Error", e.toString())

            false
        }
    }

    private fun updateSelectedRow(p1: List<String>?, response: Pair<String, String>): Boolean {
        try {
            val updateRow = ArrayList(rowData)

            val rowIndex = rowData.indexOf(p1)
            val newRow = p1!!.toMutableList()

            newRow[5] = response.first
            newRow[7] = response.second

            updateRow[rowIndex] = newRow

            rowData = updateRow

            updateJsonRow(requireContext(), newRow, rowIndex)

            return true
        } catch(e: IOException) {
            Log.e("Error", e.toString())
            return false
        }
    }

    private fun checkCurrentRoom(itemNumber: String) {
        if (rowData.isEmpty()) {
            return
        }

        val foundAsset = rowData.filter { it.getOrNull(4) == itemNumber }

        if (foundAsset.isEmpty() || foundAsset[0].size < 8) {
            return
        }

        val foundRoomNum = "${foundAsset[0].getOrNull(6) ?: ""}${foundAsset[0].getOrNull(7) ?: ""}"

        val isInCurrentRoom = if (isRoomLess) {
            foundRoomNum.isBlank()
        } else {
            foundRoomNum.trim() == readRoomBarcode.trim()
        }

        if (!isInCurrentRoom) {
            newDialog.confirmation(getString(R.string.dialog_asset_move_title),
                getString(R.string.dialog_asset_move_message),
                getString(R.string.dialog_yes_btn),
                getString(R.string.dialog_no_btn)) { result ->
                if (result) {
                    moveItemToCurrentRoom(itemNumber)

                    filters.showFilteredByCheckedItems("false", rowData, isRoomLess, readRoomBarcode, textViewCounterTotal, textViewCounterToFind) { detailsButtonSetting(it) }
                    textViewInventoryNumber!!.text = ""
                }
            }
        } else {
            updateRowByBarcode(itemNumber)
        }
    }

    private fun moveItemToCurrentRoom(itemNumber: String) {
        val foundAsset = rowData.filter { it.getOrNull(4) == itemNumber }
        val updatedRows = ArrayList(rowData)

        if (readRoomBarcode.isNotBlank()) {
            val currentRoomNum = readRoomBarcode.split("6300")
            val numPart1 = readRoomBarcode.substring(0, 4)
            val numPart2 = currentRoomNum.getOrNull(1) ?: ""

            val updatedRow = foundAsset[0].toMutableList().apply {
                this[size - 6] = numPart2
                this[size - 7] = numPart1
            }.toList()

            val index = rowData.indexOf(foundAsset[0])
            updatedRows[index] = updatedRow
        } else {
            val updatedRow = foundAsset[0].toMutableList().apply {
                this[size - 6] = ""
                this[size - 7] = ""
            }.toList()

            val index = rowData.indexOf(foundAsset[0])
            updatedRows[index] = updatedRow
        }

        rowData = updatedRows
    }

    private fun foundRoomByWritten(p1: String): Boolean {
        val tmpRowData = rowData.filter { "${it.getOrNull(6)}${it.getOrNull(7)}" == p1 }
        Log.d("McKay", tmpRowData.size.toString())
        return tmpRowData.isNotEmpty()
    }

    private fun checkCostCenter() {
        newDialog.loading(getString(R.string.dialog_loading_text))

        val updatedRows = ArrayList(rowData)

        for (row in updatedRows) {
            if(row[5] == "" || row[0] == "") {
                var updatedRow = row
                updatedRow = updatedRow.toMutableList().apply {
                    this[size - 4] = "G"
                }.toList()
                val index = rowData.indexOf(row)
                updatedRows[index] = updatedRow
            }
        }
        rowData = updatedRows

        newDialog.dismissLoading("loading") {_ ->}
    }

    private fun fillManagement() {
        availableRooms!!.text = statistics.allRooms(rowData)
        allItems!!.text = statistics.allItems(rowData)
        checkedItems!!.text = statistics.allCheckedItems(rowData)
        visitedRooms!!.text = statistics.allVisitedRooms(rowData)
        finishedRooms!!.text = statistics.allFinishedRooms(rowData)
    }
}