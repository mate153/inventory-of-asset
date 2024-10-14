package com.devforce.inventory.service

import android.content.Context
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devforce.inventory.R
import com.devforce.inventory.fragments.RoomConfirmationFragment

class Filters (
    private val fragment: Fragment,
    private var recyclerView: RecyclerView,
    private var textViewScanData: TextView?,
    private var textViewInventoryNumber: TextView?,
    private val requireContext: Context
){
    private lateinit var adapter: CsvAdapter
    private lateinit var selectedRow: List<String>

    fun aloneItems(bool: String, rowData: List<List<String>>, counterTotal: TextView?, counterToFind: TextView?, refreshSettings: (p1: List<String>) -> Unit) {
        val foundedItems = rowData.filter { "${it.getOrNull(7)}" == "" && it.last() == bool }

        counterTotal!!.text = rowData.count { "${it.getOrNull(7)}" == "" }.toString()
        counterToFind!!.text = rowData.count { "${it.getOrNull(7)}" == "" && it.last() == "true" }.toString()

        adapter = CsvAdapter(foundedItems)
        adapter.setOnItemClickListener(object : CsvAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                selectedRow = foundedItems[position]
                val clickedItemInventoryNumber = foundedItems[position][4]

                textViewInventoryNumber!!.text = clickedItemInventoryNumber
                refreshSettings(selectedRow)
                adapter.toggleSelection(position)
            }
        })

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext)
    }

    fun filterByRoomNumber(
        rowData: List<List<String>>,
        readRoomBarcode: String,
        counterTotal: TextView?,
        counterToFind: TextView?,
        refreshSettings: (p1: List<String>) -> Unit
    ) {
        if (!fragment.isAdded) {
            return
        }

        if (!::adapter.isInitialized) {
            adapter = CsvAdapter(emptyList())
            recyclerView.adapter = adapter
            recyclerView.layoutManager = LinearLayoutManager(requireContext)
        }

        val tmpRowData = rowData.filter { "${it.getOrNull(6)}${it.getOrNull(7)}" == readRoomBarcode && it.last() == "false" }

        counterTotal!!.text = rowData.count { "${it.getOrNull(6)}${it.getOrNull(7)}" == readRoomBarcode }.toString()
        counterToFind!!.text = rowData.count { "${it.getOrNull(6)}${it.getOrNull(7)}" == readRoomBarcode && it.last() == "true" }.toString()

        adapter = CsvAdapter(tmpRowData)
        adapter.setOnItemClickListener(object : CsvAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                selectedRow = tmpRowData[position]
                val clickedItem = tmpRowData[position][4]

                textViewInventoryNumber!!.text = clickedItem
                refreshSettings(selectedRow)
                adapter.toggleSelection(position)
            }
        })
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext)
        if (tmpRowData.isEmpty()) {
            textViewScanData!!.text = requireContext.getString(R.string.wrong_room_number_barcode)
            textViewInventoryNumber!!.text = ""
        }
    }

    fun showFilteredByCheckedItems(
        bool: String,
        rowData: List<List<String>>,
        isRoomLess: Boolean,
        readRoomBarcode: String,
        counterTotal: TextView?,
        counterToFind: TextView?,
        refreshSettings: (p1: List<String>) -> Unit
    ){
        val foundedItems = if (isRoomLess){
            rowData.filter { "${it.getOrNull(7)}" == readRoomBarcode && it.last() == bool }
        } else{
            rowData.filter { "${it.getOrNull(6)}${it.getOrNull(7)}" == readRoomBarcode && it.last() == bool }
        }

        if(isRoomLess) {
            counterTotal!!.text = rowData.count { "${it.getOrNull(7)}" == "" }.toString()
            counterToFind!!.text = rowData.count { "${it.getOrNull(7)}" == "" && it.last() == "true" }.toString()
        } else {
            counterTotal!!.text = rowData.count { "${it.getOrNull(6)}${it.getOrNull(7)}" == readRoomBarcode }.toString()
            counterToFind!!.text = rowData.count { "${it.getOrNull(6)}${it.getOrNull(7)}" == readRoomBarcode && it.last() == "true" }.toString()
        }

        adapter = CsvAdapter(foundedItems)
        adapter.setOnItemClickListener(object : CsvAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                selectedRow = foundedItems[position]
                val clickedItemInventoryNumber = foundedItems[position][4]
                val clickedItemRoomNumber = "${foundedItems[position][6]}${foundedItems[position][7]}"

                textViewInventoryNumber!!.text = clickedItemInventoryNumber
                textViewScanData!!.text = clickedItemRoomNumber
                refreshSettings(selectedRow)
                adapter.toggleSelection(position)
            }
        })

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext)
    }
}
