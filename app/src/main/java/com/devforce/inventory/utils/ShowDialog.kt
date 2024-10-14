package com.devforce.inventory.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.text.Editable
import android.text.InputType
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.getString
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.devforce.inventory.R

class ShowDialog(private val requireContext: Context) {
    private lateinit var confirmationDialog: Dialog
    private lateinit var updateConfirmationDialog: Dialog
    private lateinit var loadingDialog: Dialog
    private var isLoading = false

    fun info(message: String) {
        val infoDialog = Dialog(requireContext)

        infoDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        infoDialog.setCancelable(false)
        infoDialog.setContentView(R.layout.layout_info)
        infoDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val dialogMessage: TextView = infoDialog.findViewById(R.id.infoText)
        val btnConfirm: Button = infoDialog.findViewById(R.id.buttonConfirm)

        dialogMessage.text = message

        btnConfirm.setOnClickListener {
            infoDialog.dismiss()
        }

        infoDialog.show()
    }

    fun updateConfirmation(title: String, message: String, yesText: String, noText: String, callback: (Boolean) -> Unit) {
        updateConfirmationDialog = Dialog(requireContext)

        updateConfirmationDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        updateConfirmationDialog.setCancelable(false)
        updateConfirmationDialog.setContentView(R.layout.layout_update)
        updateConfirmationDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val dialogMessage: TextView = updateConfirmationDialog.findViewById(R.id.dialogMessage)
        val dialogTitle: TextView = updateConfirmationDialog.findViewById(R.id.dialogTitle)
        val btnYes: Button = updateConfirmationDialog.findViewById(R.id.dialogBtnYes)
        val btnNo: Button = updateConfirmationDialog.findViewById(R.id.dialogBtnNo)

        dialogMessage.text = message
        dialogTitle.text = title
        btnYes.text = yesText
        btnNo.text = noText

        btnYes.setOnClickListener {
            updateConfirmationDialog.dismiss()
            callback(true)
        }

        btnNo.setOnClickListener {
            updateConfirmationDialog.dismiss()
            callback(false)
        }

        updateConfirmationDialog.show()
    }

    fun confirmation(title: String, message: String, yesText: String, noText: String, callback: (Boolean) -> Unit) {
        confirmationDialog = Dialog(requireContext)

        confirmationDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        confirmationDialog.setCancelable(false)
        confirmationDialog.setContentView(R.layout.layout_dialog)
        confirmationDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val dialogMessage: TextView = confirmationDialog.findViewById(R.id.dialogMessage)
        val dialogTitle: TextView = confirmationDialog.findViewById(R.id.dialogTitle)
        val btnYes: Button = confirmationDialog.findViewById(R.id.dialogBtnYes)
        val btnNo: Button = confirmationDialog.findViewById(R.id.dialogBtnNo)

        dialogMessage.text = message
        dialogTitle.text = title
        btnYes.text = yesText
        btnNo.text = noText

        btnYes.setOnClickListener {
            confirmationDialog.dismiss()
            callback(true)
        }

        btnNo.setOnClickListener {
            confirmationDialog.dismiss()
            callback(false)
        }

        confirmationDialog.show()
    }

    fun loading(text: String) {
        if (!isLoading) {
            isLoading = true
            loadingDialog = Dialog(requireContext)

            loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            loadingDialog.setCancelable(false)
            loadingDialog.setContentView(R.layout.layout_loading)
            loadingDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val loadingText: TextView = loadingDialog.findViewById(R.id.loadingText)
            loadingText.text = text

            loadingDialog.show()
        }
    }

    fun dismissLoading(t: String, callback: (Boolean) -> Unit) {
        if (isLoading) {
            loadingDialog.dismiss()
            isLoading = false

            if(t == "export") {
                openDoneDialog { result ->
                    if(result) {
                        callback(true)
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun assetCreateDialog(requireContext: Context, activity: Activity, callback: (Triple<String, String, String>?) -> Unit) {
        val createAssetDialog = Dialog(requireContext)

        createAssetDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        createAssetDialog.setCancelable(false)
        createAssetDialog.setContentView(R.layout.layout_asset_create)
        createAssetDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnCancel: Button = createAssetDialog.findViewById(R.id.changeBack)
        val btnModify: Button = createAssetDialog.findViewById(R.id.changeModify)

        val editableFields = collectCreateEditTexts(createAssetDialog)

        btnCancel.setOnClickListener {
            callback(null)
            createAssetDialog.dismiss()
        }

        btnModify.setOnClickListener {
            if(editableFields.third.text.toString() == "") {
                showCustomToast(R.string.notify_details_asset_name, activity, "info")
            } else {
                val cbStrings = Triple(editableFields.first.text.toString(), editableFields.second.text.toString(), editableFields.third.text.toString())
                callback(cbStrings)
                createAssetDialog.dismiss()
            }
        }

        createAssetDialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun assetDetailsDialog(requireContext: Context, p1: List<String>?, callback: (Pair<String, String>?) -> Unit) {
        val detailsDialog = Dialog(requireContext)

        detailsDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        detailsDialog.setCancelable(false)
        detailsDialog.setContentView(R.layout.layout_asset_change)
        detailsDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnCancel: Button = detailsDialog.findViewById(R.id.changeBack)
        val btnModify: Button = detailsDialog.findViewById(R.id.changeModify)

        val editableFields = collectDetailsEditTexts(detailsDialog, p1)

        btnCancel.setOnClickListener {
            callback(null)
            detailsDialog.dismiss()
        }

        btnModify.setOnClickListener {
            val cbStrings = Pair(editableFields.first.text.toString(), editableFields.second.text.toString())
            callback(cbStrings)
            detailsDialog.dismiss()
        }

        detailsDialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun collectCreateEditTexts(createAssetDialog: Dialog) : Triple<EditText, EditText, EditText> {
        val assetNumber: EditText = createAssetDialog.findViewById(R.id.editText1)
        val asset: EditText = createAssetDialog.findViewById(R.id.editText)
        val assetName: EditText = createAssetDialog.findViewById(R.id.editText2)
        val inventoryNumber: EditText = createAssetDialog.findViewById(R.id.editText3)
        val companyCode: EditText = createAssetDialog.findViewById(R.id.editText4)
        val costCenter: EditText = createAssetDialog.findViewById(R.id.editText5)
        val newCostCenter: EditText = createAssetDialog.findViewById(R.id.editText7)
        val user: EditText = createAssetDialog.findViewById(R.id.editText6)
        val date: EditText = createAssetDialog.findViewById(R.id.editText8)
        val room: EditText = createAssetDialog.findViewById(R.id.editText9)
        val newRoom: EditText = createAssetDialog.findViewById(R.id.editText10)

        assetNumber.setInputType(InputType.TYPE_NULL)
        asset.setInputType(InputType.TYPE_NULL)
        inventoryNumber.setInputType(InputType.TYPE_NULL)
        companyCode.setInputType(InputType.TYPE_NULL)
        costCenter.setInputType(InputType.TYPE_NULL)
        user.setInputType(InputType.TYPE_NULL)
        date.setInputType(InputType.TYPE_NULL)
        room.setInputType(InputType.TYPE_NULL)

        return Triple(newCostCenter, newRoom, assetName)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun collectDetailsEditTexts(detailsDialog: Dialog, p1: List<String>?) : Pair<EditText, EditText> {
        val row = p1!!

        val assetNumber: EditText = detailsDialog.findViewById(R.id.editText1)
        val asset: EditText = detailsDialog.findViewById(R.id.editText)
        val assetName: EditText = detailsDialog.findViewById(R.id.editText2)
        val inventoryNumber: EditText = detailsDialog.findViewById(R.id.editText3)
        val companyCode: EditText = detailsDialog.findViewById(R.id.editText4)
        val costCenter: EditText = detailsDialog.findViewById(R.id.editText5)
        val newCostCenter: EditText = detailsDialog.findViewById(R.id.editText7)
        val user: EditText = detailsDialog.findViewById(R.id.editText6)
        val date: EditText = detailsDialog.findViewById(R.id.editText8)
        val room: EditText = detailsDialog.findViewById(R.id.editText9)
        val newRoom: EditText = detailsDialog.findViewById(R.id.editText10)

        assetNumber.text = Editable.Factory.getInstance().newEditable(row[1])
        asset.text = Editable.Factory.getInstance().newEditable(row[0])
        assetName.text = Editable.Factory.getInstance().newEditable(row[3])
        inventoryNumber.text = Editable.Factory.getInstance().newEditable(row[4])
        companyCode.text = Editable.Factory.getInstance().newEditable(row[6])
        costCenter.text = Editable.Factory.getInstance().newEditable(row[5])
        newCostCenter.text = Editable.Factory.getInstance().newEditable(row[5])
        user.text = Editable.Factory.getInstance().newEditable(row[10])
        date.text = Editable.Factory.getInstance().newEditable(row[11])
        room.text = Editable.Factory.getInstance().newEditable(row[7])
        newRoom.text = Editable.Factory.getInstance().newEditable(row[7])

        assetNumber.setInputType(InputType.TYPE_NULL)
        asset.setInputType(InputType.TYPE_NULL)
        assetName.setInputType(InputType.TYPE_NULL)
        inventoryNumber.setInputType(InputType.TYPE_NULL)
        companyCode.setInputType(InputType.TYPE_NULL)
        costCenter.setInputType(InputType.TYPE_NULL)
        user.setInputType(InputType.TYPE_NULL)
        date.setInputType(InputType.TYPE_NULL)
        room.setInputType(InputType.TYPE_NULL)

        return Pair(newCostCenter, newRoom)
    }

    private fun openDoneDialog(callback: (Boolean) -> Unit) {
        val doneDialog = Dialog(requireContext)

        doneDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        doneDialog.setCancelable(false)
        doneDialog.setContentView(R.layout.layout_export_done)
        doneDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val successGif: ImageView = doneDialog.findViewById(R.id.imageView3)
        val btnLogout: Button = doneDialog.findViewById(R.id.buttonLogout)

        Glide.with(requireContext)
            .asGif()
            .load(R.drawable.done)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .into(successGif)

        btnLogout.setOnClickListener {
            doneDialog.dismiss()

            callback(true)
        }

        doneDialog.show()
    }

    @Suppress("DEPRECATION")
    @SuppressLint("SetTextI18n")
    fun exportedInventoryDialog(
        requireContext: Context,
        jsonExistText: String?,
        callback: (String?) -> Unit
    ) {
        val exportedDialog = Dialog(requireContext)

        exportedDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        exportedDialog.setCancelable(false)
        exportedDialog.setContentView(R.layout.layout_regular_inventory_dialog)
        exportedDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnCancel: Button = exportedDialog.findViewById(R.id.buttonBack)
        val btnNew: Button = exportedDialog.findViewById(R.id.buttonContinueBlind)
        val btnResume: Button = exportedDialog.findViewById(R.id.buttonContinueRegular)
        val designResId: Int
        val continueText = exportedDialog.findViewById<TextView>(R.id.continueExportedText)
        val attentionTitleText = exportedDialog.findViewById<TextView>(R.id.textView14)
        val attentionDescText = exportedDialog.findViewById<TextView>(R.id.textView15)

        if(jsonExistText != null) {
            designResId = R.drawable.button_design
            val firstText = getString(requireContext, R.string.session_continue)
            continueText.text = "$firstText$jsonExistText"
            attentionTitleText.visibility = View.VISIBLE
            attentionDescText.visibility = View.VISIBLE
        } else {
            designResId = R.drawable.button_design_inactive
            continueText.text = getString(requireContext, R.string.session_not_found)
            attentionTitleText.visibility = View.GONE
            attentionDescText.visibility = View.GONE
        }

        val design = ResourcesCompat.getDrawable(requireContext.resources, designResId, null)
        btnResume.setBackgroundDrawable(design)

        btnCancel.setOnClickListener {
            callback(null)
            exportedDialog.dismiss()
        }

        btnNew.setOnClickListener {
            callback("new")
            exportedDialog.dismiss()
        }

        if(jsonExistText != null) {
            btnResume.setOnClickListener {
                callback("resume")
                exportedDialog.dismiss()
            }
        }

        exportedDialog.show()
    }

    fun newBuildInventoryDialog(
        requireContext: Context,
        callback: (Boolean?) -> Unit)
    {
        val buildDialog = Dialog(requireContext)

        buildDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        buildDialog.setCancelable(false)
        buildDialog.setContentView(R.layout.layout_build_new_template)
        buildDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnCancel: Button = buildDialog.findViewById(R.id.backButton)
        val btnStart: Button = buildDialog.findViewById(R.id.startButton)

        btnCancel.setOnClickListener {
            callback(null)
            buildDialog.dismiss()
        }

        btnStart.setOnClickListener {
            callback(true)
            buildDialog.dismiss()
        }

        buildDialog.show()
    }
}