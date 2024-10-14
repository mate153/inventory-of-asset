package com.devforce.inventory.fragments

import com.devforce.inventory.utils.LocaleSwitcher
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.devforce.inventory.R
import com.devforce.inventory.config.DevForceConfig
import com.devforce.inventory.config.DevForceSave
import com.devforce.inventory.databinding.FragmentLoginBinding
import com.devforce.inventory.service.downloadFileAndInstall
import com.devforce.inventory.service.fetchUpdate
import com.devforce.inventory.utils.ShowDialog
import com.devforce.inventory.utils.showCustomToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginFragment : Fragment(){
    private var _binding: FragmentLoginBinding? = null

    private val binding get() = _binding!!
    private lateinit var buttonLanguageSwitchToEnglish: ImageButton
    private lateinit var buttonLanguageSwitchToGerman: ImageButton
    private lateinit var buttonLanguageSwitchToHungarian: ImageButton
    private lateinit var newDialog: ShowDialog


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        newDialog = ShowDialog(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val versionNumber = view.findViewById<TextView>(R.id.versionNumber)
        versionNumber.text = DevForceConfig.Basics.VERSION

        setupLanguages(view)
        checkLoginSettings()
        if(DevForceConfig.Update.ACTIVE) checkUpdates()

        binding.buttonLogin.setOnClickListener {
            checkLoginData(view)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun checkLoginSettings() {
        if(DevForceConfig.Auth.NEED_AUTH) {
            binding.editTextPassword.visibility = View.VISIBLE
            binding.editTextUsername.visibility = View.VISIBLE
        } else {
            binding.editTextPassword.visibility = View.GONE
            binding.editTextUsername.visibility = View.GONE
        }
    }

    private fun checkUpdates() {
        newDialog.loading(getString(R.string.dialog_check_update))

        CoroutineScope(Dispatchers.Main).launch {
            val updateInfo = fetchUpdate(requireContext())
            newDialog.dismissLoading("update") {}

            if (updateInfo != null) {
                val newVersion = updateInfo.newVersion
                val updateLog = updateInfo.updateLog

                newDialog.updateConfirmation(getString(R.string.dialog_update_available) + newVersion, updateLog, getString(R.string.dialog_update_yesBtn), getString(R.string.dialog_update_noBtn)) { result ->
                    if(result) {
                        newDialog.loading(getString(R.string.dialog_updating))

                        CoroutineScope(Dispatchers.Main).launch {
                            val updatingResult = downloadFileAndInstall(requireContext())
                            newDialog.dismissLoading("update") {}
                        }
                    }
                }
            }
        }
    }

    private fun checkLoginData(view: View) {
        if(!DevForceConfig.Auth.NEED_AUTH) {
            showCustomToast(R.string.notify_login_success, requireActivity(), "success")
            findNavController().navigate(R.id.action_Login_To_MenuSelect)
        } else {
            val username = view.findViewById<EditText>(R.id.editTextUsername).text.toString()
            val password = view.findViewById<EditText>(R.id.editTextPassword).text.toString()

            val isValidUser = DevForceConfig.Auth.USERS.any { it["name"] == username && it["password"] == password }

            if (isValidUser) {
                showCustomToast(R.string.notify_login_success, requireActivity(), "success")
                DevForceSave.Auth.LOGGED_IN_AS = username
                findNavController().navigate(R.id.action_Login_To_MenuSelect)
            } else {
                showCustomToast(R.string.notify_login_failed, requireActivity(), "error")
            }
        }
    }

    // Languages onClickListeners
    private fun setupLanguages(view: View) {
        buttonLanguageSwitchToEnglish = view.findViewById(R.id.imageButton)
        buttonLanguageSwitchToGerman = view.findViewById(R.id.imageButton1)
        buttonLanguageSwitchToHungarian = view.findViewById(R.id.imageButton2)

        buttonLanguageSwitchToEnglish.setOnClickListener {
            LocaleSwitcher.updateBaseContextLocale(requireContext(), "en")
        }

        buttonLanguageSwitchToGerman.setOnClickListener {
            LocaleSwitcher.updateBaseContextLocale(requireContext(), "de")
        }

        buttonLanguageSwitchToHungarian.setOnClickListener {
            LocaleSwitcher.updateBaseContextLocale(requireContext(), "hu")
        }
    }
}