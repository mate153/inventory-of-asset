package com.devforce.inventory.service

class Statistics {

    /* Count all room */
    fun allRooms(rowData: List<List<String>>): String {
        val countedRoom: MutableList<String> = mutableListOf()

        for ( row in rowData ){
            val tmpRoomNumber = row[7]
            if (tmpRoomNumber != "" ){
                if(!countedRoom.contains(tmpRoomNumber)){
                    countedRoom.add(tmpRoomNumber)
                }
            }
        }

        return countedRoom.size.toString()
    }

    /* Count all  item */
    fun allItems(rowData: List<List<String>>): String {
        return rowData.size.toString()
    }

    /* Count checked item */
    fun allCheckedItems(rowData: List<List<String>>): String {
        var counter = 0
        for (row in rowData){
            if (row.last() == "true"){
                counter++
            }
        }

        return counter.toString()
    }

    /* Count all visited room */
    fun allVisitedRooms(rowData: List<List<String>>): String {
        var counter = 0
        val visitedRoomsList: MutableSet<String> = mutableSetOf()

        for (row in rowData) {
            val roomID = "${row[6]}${row[7]}"

            if (row[7] != "" && row.last() != "false" && !visitedRoomsList.contains(roomID)) {
                visitedRoomsList.add(roomID)
                counter++
            }
        }

        return counter.toString()
    }

    /* Count all finished room */
    fun allFinishedRooms(rowData: List<List<String>>): String {
        var counter = 0
        val allRooms: MutableMap<String, MutableList<String>> = mutableMapOf()

        for (row in rowData) {
            val tmpRoomNumber = row[7]

            if (tmpRoomNumber.isNotBlank()) {
                if (!allRooms.containsKey(tmpRoomNumber)) {
                    allRooms[tmpRoomNumber] = mutableListOf()
                    allRooms[tmpRoomNumber]?.add(row.last())
                }else {
                    allRooms[tmpRoomNumber]?.add(row.last())
                }
            }
        }

        for ((_, elements) in allRooms) {
            if (elements.all { it == "true" }) {
                counter++
            }
        }

        return counter.toString()
    }
}