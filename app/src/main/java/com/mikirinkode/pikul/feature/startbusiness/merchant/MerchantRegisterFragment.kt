package com.mikirinkode.pikul.feature.startbusiness.merchant

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.mikirinkode.pikul.R
import com.mikirinkode.pikul.data.local.LocalPreference
import com.mikirinkode.pikul.data.local.LocalPreferenceConstants
import com.mikirinkode.pikul.data.local.MAIN_VIEW
import com.mikirinkode.pikul.data.model.PikulResult
import com.mikirinkode.pikul.data.model.UserAccount
import com.mikirinkode.pikul.databinding.FragmentMerchantRegisterBinding
import com.mikirinkode.pikul.feature.merchant.MerchantMainActivity
import com.mikirinkode.pikul.utils.CameraActivity
import com.mikirinkode.pikul.utils.ImageHelper
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class MerchantRegisterFragment : Fragment() {

    private var _binding: FragmentMerchantRegisterBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var preferences: LocalPreference

    private val user: UserAccount? by lazy {
        preferences?.getObject(LocalPreferenceConstants.USER, UserAccount::class.java)
    }

    private val viewModel: MerchantRegisterViewModel by viewModels()

    private var getFile: File? = null
    private var isBackCamera: Boolean? = null

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentMerchantRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initTextWatcher()
        onClickAction()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initView() {
        binding.apply {
            if (user != null) {
                val provinces = resources.getStringArray(R.array.provinces)
                val arrayAdapter = ArrayAdapter<String>(
                    requireContext(),
                    android.R.layout.simple_list_item_1,
                    provinces
                )
                actvProvince.setAdapter(arrayAdapter)


                etName.setText(user?.name)
                etEmail.setText(user?.email)

                if (user?.avatarUrl.isNullOrBlank()) {
                    Glide.with(requireContext())
                        .load(R.drawable.ic_default_user_avatar)
                        .into(ivUserAvatar)
                    btnCaptureImage.visibility = View.VISIBLE
                } else {
                    btnCaptureImage.visibility = View.GONE
                    Glide.with(requireContext())
                        .load(user?.avatarUrl)
                        .into(ivUserAvatar)
                }
            }
        }
    }

    private fun initTextWatcher() {
        binding.actvProvince.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {
                val provinces = resources.getStringArray(R.array.provinces)
                val selectedProvince = binding.actvProvince.text.toString()
                binding.btnNext.isEnabled = provinces.contains(selectedProvince)
            }
        })
    }

    private fun showDialog() {
        // check if permission not granted, then request for permission else show picture dialog
        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        } else {
            val pictureDialog = AlertDialog.Builder(requireContext())
            pictureDialog.setTitle(getString(R.string.txt_dialog_choosing_action))
            val pictureDialogItem = arrayOf(
                getString(R.string.txt_dialog_item_gallery),
                getString(R.string.txt_dialog_item_camera)
            )
            pictureDialog.setItems(pictureDialogItem) { _, which ->
                when (which) {
                    0 -> openGallery()
                    1 -> openCamera()
                }
            }
            pictureDialog.show()
        }
    }

    private fun openGallery() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser =
            Intent.createChooser(intent, getString(R.string.txt_intent_title_choose_picture))
        launcherIntentGallery.launch(chooser)
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            val selectedImg: Uri = result.data?.data as Uri
            val myFile = ImageHelper.uriToFile(selectedImg, requireContext())
            getFile = myFile

            binding.apply {
                if (getFile == null) btnCaptureImage.visibility =
                    View.VISIBLE else btnCaptureImage.visibility = View.GONE
                ivUserAvatar.setImageURI(selectedImg)
            }
        }
    }

    // Open CameraX
    private fun openCamera() {
        val intent = Intent(requireContext(), CameraActivity::class.java)
        launcherIntentCameraX.launch(intent)
    }

    // resultForActivity to get returned file from CameraActivity
    private val launcherIntentCameraX = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == CameraActivity.CAMERA_X_RESULT) {
            val myFile = it.data?.getSerializableExtra("picture") as File
            isBackCamera = it.data?.getBooleanExtra("isBackCamera", true) as Boolean
            myFile?.let { file ->
                ImageHelper.rotateFile(file, isBackCamera == true)
            }
            getFile = myFile
//            val result = rotateBitmap(BitmapFactory.decodeFile(myFile.path), isBackCamera!!)

            binding.apply {
                if (getFile == null) btnCaptureImage.visibility =
                    View.VISIBLE else btnCaptureImage.visibility = View.GONE
                ivUserAvatar.setImageBitmap(BitmapFactory.decodeFile(myFile.path))
            }
        }
    }

    // check permission for Camera
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.txt_error_permission_not_granted),
                    Toast.LENGTH_SHORT
                ).show()
                requireActivity().finish()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    private fun registerMerchantProfile() {
        binding.apply {
            val provinces = resources.getStringArray(R.array.provinces)
            val userProvince = actvProvince.text.toString()
            val userName = etName.text.toString()
            var isValid = true

            if (userName.isEmpty()) {
                isValid = false
                etName.error = getString(R.string.empty_name)
            }
            if (!provinces.contains(userProvince)) {
                isValid = false
                Toast.makeText(
                    requireContext(),
                    "Mohon untuk pilih provinsi yang valid",
                    Toast.LENGTH_SHORT
                ).show()
            }

            if (isValid) {
                viewModel.registerAsMerchant(userProvince, userName, getFile)
                    .observe(viewLifecycleOwner) { result ->
                        when (result) {
                            is PikulResult.Loading -> {
                                layoutLoading.visibility = View.VISIBLE
                                layoutLoadingWithProgress.visibility = View.GONE
                            }
                            is PikulResult.LoadingWithProgress -> {
                                val progress = result.progress
                                layoutLoading.visibility = View.GONE
                                layoutLoadingWithProgress.visibility = View.VISIBLE
                                tvLoadingTitle.text = "Upload Gambar"
                                uploadingProgressBar.progress = progress
                            }
                            is PikulResult.Error -> {
                                layoutLoading.visibility = View.GONE
                                layoutLoadingWithProgress.visibility = View.GONE
                                val errorMessage = result.errorMessage
                                Toast.makeText(
                                    requireContext(),
                                    "Terjadi Kesalahan: ${errorMessage}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            is PikulResult.Success -> {
                                layoutLoading.visibility = View.GONE
                                layoutLoadingWithProgress.visibility = View.GONE
                                val success = result.data
                                if (success) {
                                    preferences.saveString(LocalPreferenceConstants.SELECTED_MAIN_VIEW, MAIN_VIEW.BUSINESS_VIEW.toString())
                                    Toast.makeText(
                                        requireContext(),
                                        getString(R.string.txt_registration_success),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    startActivity(Intent(requireContext(), MerchantMainActivity::class.java))
                                    requireActivity().finishAffinity()
                                }
                            }
                        }
                    }
            }
        }
    }


    private fun onClickAction() {
        binding.apply {
            topAppBar.setNavigationOnClickListener {
                Navigation.findNavController(binding.root).navigateUp()
            }

            ivUserAvatar.setOnClickListener {
                showDialog()
            }

            btnCaptureImage.setOnClickListener {
                showDialog()
            }

            btnNext.setOnClickListener {
                registerMerchantProfile()
            }

        }
    }
}