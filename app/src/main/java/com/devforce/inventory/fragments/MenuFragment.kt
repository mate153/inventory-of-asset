package com.devforce.inventory.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.devforce.inventory.R
import com.devforce.inventory.config.DevForceConfig
import com.devforce.inventory.config.DevForceSave
import com.devforce.inventory.databinding.FragmentMenuBinding
import com.devforce.inventory.controllers.ReadCsv
import com.devforce.inventory.controllers.jsonController
import com.devforce.inventory.utils.ShowDialog
import com.devforce.inventory.utils.checkJsonFile
import com.devforce.inventory.utils.showCustomToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MenuFragment : Fragment() {
    private var _binding: FragmentMenuBinding? = null
    private val binding get() = _binding!!
    private lateinit var readCsv: ReadCsv
    private lateinit var newDialog: ShowDialog
    private lateinit var chosenInventory: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMenuBinding.inflate(inflater, container, false)
        newDialog = ShowDialog(requireContext())
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (DevForceSave.Auth.LOGGED_IN_AS == "" && DevForceConfig.Auth.NEED_AUTH) {
            findNavController().navigate(R.id.action_FROM_MenuSelect_To_LoginFragment)
        }

        newDialog.loading(getString(R.string.dialog_loading_text2))
        readCsv = ReadCsv(requireContext())
        val regularJsonExistText = checkJsonFile(requireContext(), DevForceConfig.Save.Regular.JSON_FILE_NAME)
        val buildJsonExistText = checkJsonFile(requireContext(), DevForceConfig.Save.Build.JSON_FILE_NAME)
        newDialog.dismissLoading("loading") { _ -> }

        val regularInventoryButton = binding.buttonExportedInventory
        regularInventoryButton.setOnClickListener {
            chosenInventory = "regular"
            newDialog.exportedInventoryDialog(requireContext(), regularJsonExistText) { result ->
                if (result != null) {
                    if (result == "new") {
                        if (regularJsonExistText != null) {
                            newDialog.confirmation(
                                getString(R.string.dialog_new_session_title),
                                getString(R.string.dialog_new_session_message),
                                getString(R.string.dialog_yes_btn),
                                getString(R.string.dialog_no_btn)
                            ) { result2 ->
                                if (result2) {
                                    getContent.launch("*/*")
                                }
                            }
                        } else {
                            getContent.launch("*/*")
                        }
                    } else {
                        if (regularJsonExistText != null) {
                            findNavController().navigate(R.id.action_MenuFragment_To_RoomConfirmationFragment)
                        }
                    }
                }
            }
        }

        val buildInventoryButton = binding.buttonBuildInventory
        buildInventoryButton.setOnClickListener {
            chosenInventory = "build"
            newDialog.exportedInventoryDialog(requireContext(), buildJsonExistText) { result ->
                if (result != null) {
                    if (result == "new") {
                        if (buildJsonExistText != null) {
                            newDialog.confirmation(
                                getString(R.string.dialog_new_session_title),
                                getString(R.string.dialog_new_session_message),
                                getString(R.string.dialog_yes_btn),
                                getString(R.string.dialog_no_btn)
                            ) { result2 ->
                                if (result2) {
                                    newDialog.newBuildInventoryDialog(requireContext()) { result3 ->
                                        if (result3 == true) {
                                            createEmptyBuildInventoryJson()
                                            findNavController().navigate(R.id.action_MenuFragment_To_BuildInventoryFragment)
                                        }
                                    }
                                }
                            }
                        } else {
                            newDialog.newBuildInventoryDialog(requireContext()) { result3 ->
                                if (result3 == true) {
                                    createEmptyBuildInventoryJson()
                                    findNavController().navigate(R.id.action_MenuFragment_To_BuildInventoryFragment)
                                }
                            }
                        }
                    } else {
                        if (buildJsonExistText != null) {
                            findNavController().navigate(R.id.action_MenuFragment_To_BuildInventoryFragment)
                        }
                    }
                }
            }
        }

        val backButton = binding.buttonExit
        backButton.setOnClickListener {
            showCustomToast(
                R.string.notify_logout_success,
                requireActivity(),
                "success"
            )
            findNavController().navigate(R.id.action_FROM_MenuSelect_To_LoginFragment)
        }
    }

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { selectedFileUri ->
            newDialog.loading(getString(R.string.dialog_loading_text2))

            CoroutineScope(Dispatchers.IO).launch {
                val documentFile = DocumentFile.fromSingleUri(requireContext(), selectedFileUri)

                if (documentFile != null && documentFile.canRead()) {
                    val inputStream: InputStream? =
                        requireContext().contentResolver.openInputStream(selectedFileUri)

                    if (inputStream != null) {
                        if (readCsv.checkFileExtension(selectedFileUri)) {
                            val csvContent = readCsv.readCSV(inputStream)
                            Log.d("MenuFragment", "CSV Content: $csvContent")

                            if (chosenInventory == "regular") {
                                if (jsonController(requireContext(), csvContent, false)) {
                                    requireActivity().runOnUiThread {
                                        showCustomToast(
                                            R.string.file_open_success,
                                            requireActivity(),
                                            "success"
                                        )
                                    }

                                    newDialog.dismissLoading("loading") { _ -> }

                                    requireActivity().runOnUiThread {
                                        findNavController().navigate(R.id.action_MenuFragment_To_RoomConfirmationFragment)
                                    }
                                }
                            } else if (chosenInventory == "build") {
                                if (jsonController(requireContext(), csvContent, true)) {
                                    requireActivity().runOnUiThread {
                                        showCustomToast(
                                            R.string.file_open_success,
                                            requireActivity(),
                                            "success"
                                        )
                                    }

                                    newDialog.dismissLoading("loading") { _ -> }

                                    requireActivity().runOnUiThread {
                                        findNavController().navigate(R.id.action_MenuFragment_To_BuildInventoryFragment)
                                    }
                                }
                            }
                        } else {
                            newDialog.dismissLoading("loading") { _ -> }
                            requireActivity().runOnUiThread {
                                showCustomToast(
                                    R.string.file_open_error,
                                    requireActivity(),
                                    "error"
                                )
                            }
                        }
                    } else {
                        newDialog.dismissLoading("loading") { _ -> }
                        Log.e("Action", "Error opening the file")
                    }
                } else {
                    newDialog.dismissLoading("loading") { _ -> }
                    Log.e("Action", "Unable to open the file")
                }
            }
        }
    }

    private fun createEmptyBuildInventoryJson() {
        val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val emptyInventoryJson = """
        {
            "data": [],
            "metadata": {
                "lastSave": "$currentTime"
            }
        }
        """.trimIndent()

        val documentsDirectory = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val saveFolder = File(documentsDirectory, "save")
        if (!saveFolder.exists()) {
            saveFolder.mkdirs()
        }
        val file = File(saveFolder, DevForceConfig.Save.Build.JSON_FILE_NAME + "." + DevForceConfig.Save.Build.JSON_FILE_EXTENSION)

        try {
            FileOutputStream(file).use {
                it.write(emptyInventoryJson.toByteArray())
            }
            Log.d("CreateJSON", "File created successfully at: ${file.absolutePath}")
            showCustomToast(R.string.notify_file_created_success, requireActivity(), "success")
        } catch (e: Exception) {
            Log.e("CreateJSON", "Error creating file", e)
            showCustomToast(R.string.notify_file_creation_error, requireActivity(), "error")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}