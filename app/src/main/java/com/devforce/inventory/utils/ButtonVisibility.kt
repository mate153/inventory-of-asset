package com.devforce.inventory.utils

import android.view.View
import com.devforce.inventory.databinding.FragmentBuildRoomconfirmationBinding
import com.devforce.inventory.databinding.FragmentRoomconfirmationBinding

class ButtonVisibility {

    fun isCheckedOut(binding: FragmentRoomconfirmationBinding) {
        binding.roomConfirmationDetailsButton.visibility = View.GONE
        binding.roomConfirmationBackButton.visibility = View.GONE
        binding.roomConfirmationShowFoundButton.visibility = View.GONE
        binding.roomConfirmationShowOpenButton.visibility = View.GONE
        binding.roomConfirmationNewButton.visibility = View.GONE
        binding.recyclerView.visibility = View.GONE
        binding.itemCounter.visibility = View.GONE
        binding.counterTotal.visibility = View.GONE
        binding.counterToFind.visibility = View.GONE

        binding.roomConfirmationExitButton.visibility = View.VISIBLE
        binding.roomConfirmationExportButton.visibility = View.VISIBLE
        binding.roomConfirmationAloneItemsButton.visibility = View.VISIBLE
        binding.roomConfirmationManagementContainer.visibility = View.VISIBLE
    }

    fun isNotCheckedOut(binding: FragmentRoomconfirmationBinding) {
        binding.roomConfirmationDetailsButton.visibility = View.VISIBLE
        binding.roomConfirmationBackButton.visibility = View.VISIBLE
        binding.roomConfirmationShowFoundButton.visibility = View.VISIBLE
        binding.roomConfirmationShowOpenButton.visibility = View.VISIBLE
        binding.roomConfirmationNewButton.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.VISIBLE
        binding.itemCounter.visibility = View.VISIBLE
        binding.counterTotal.visibility = View.VISIBLE
        binding.counterToFind.visibility = View.VISIBLE

        binding.roomConfirmationExitButton.visibility = View.GONE
        binding.roomConfirmationExportButton.visibility = View.GONE
        binding.roomConfirmationAloneItemsButton.visibility = View.GONE
        binding.roomConfirmationManagementContainer.visibility = View.GONE
    }

    fun isCheckedOutBuild(binding: FragmentBuildRoomconfirmationBinding) {
        binding.roomConfirmationBackButton.visibility = View.GONE
        binding.roomConfirmationBackButton.visibility = View.GONE
        binding.roomConfirmationShowFoundButton.visibility = View.GONE
        binding.roomConfirmationShowOpenButton.visibility = View.GONE
        binding.roomConfirmationNewButton.visibility = View.GONE
        binding.recyclerView.visibility = View.GONE
        binding.itemCounter.visibility = View.GONE
        binding.counterTotal.visibility = View.GONE
        binding.counterToFind.visibility = View.GONE

        binding.roomConfirmationExitButton.visibility = View.VISIBLE
        binding.roomConfirmationExportButton.visibility = View.VISIBLE
        binding.roomConfirmationAloneItemsButton.visibility = View.VISIBLE
        binding.roomConfirmationManagementContainer.visibility = View.VISIBLE
    }

    fun isNotCheckedOutBuild(binding: FragmentBuildRoomconfirmationBinding) {
        binding.roomConfirmationDetailsButton.visibility = View.VISIBLE
        binding.roomConfirmationBackButton.visibility = View.VISIBLE
        binding.roomConfirmationShowFoundButton.visibility = View.VISIBLE
        binding.roomConfirmationShowOpenButton.visibility = View.VISIBLE
        binding.roomConfirmationNewButton.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.VISIBLE
        binding.itemCounter.visibility = View.VISIBLE
        binding.counterTotal.visibility = View.VISIBLE
        binding.counterToFind.visibility = View.VISIBLE

        binding.roomConfirmationExitButton.visibility = View.GONE
        binding.roomConfirmationExportButton.visibility = View.GONE
        binding.roomConfirmationAloneItemsButton.visibility = View.GONE
        binding.roomConfirmationManagementContainer.visibility = View.GONE
    }
}