package com.mikirinkode.pikul.feature.owner.product

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
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
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.mikirinkode.pikul.R
import com.mikirinkode.pikul.data.model.PikulResult
import com.mikirinkode.pikul.databinding.FragmentAddProductBinding
import com.mikirinkode.pikul.utils.CameraActivity
import com.mikirinkode.pikul.utils.ImageHelper
import dagger.hilt.android.AndroidEntryPoint
import java.io.File


@AndroidEntryPoint
class AddProductFragment: Fragment() {
    private var _binding: FragmentAddProductBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProductViewModel by viewModels()
    private var getFile: File? = null
    private var isBackCamera: Boolean? = null

    private val args: AddProductFragmentArgs by navArgs()

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10

        const val EDIT_MODE = "Ubah Produk"
        const val ADD_MODE = "Tambah Produk"

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAddProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        onClickAction()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initView(){
        binding.apply {
            val categories = resources.getStringArray(R.array.product_categories)
            val arrayAdapter = ArrayAdapter<String>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                categories
            )
            actvCategory.setAdapter(arrayAdapter)

            if (args.actionMode == EDIT_MODE && args.product != null){
                etProductName.setText(args.product!!.productName)
                actvCategory.setText(args.product!!.productCategory)
                etProductPrice.setText(args.product!!.productPrice.toString())

                Glide.with(requireContext())
                    .load(args.product!!.productThumbnailUrl)
                    .placeholder(R.drawable.progress_animation)
                    .into(ivThumbnail)

                topAppBar.title = EDIT_MODE
                btnAddProduct.text = EDIT_MODE
            }
        }
    }

    private fun showGetImageDialog() {
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
                ivThumbnail.setImageURI(selectedImg)
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
                ivThumbnail.setImageBitmap(BitmapFactory.decodeFile(myFile.path))
            }
        }
    }



    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkInputValidation(name: String, category: String, price: String): Boolean {
        var isValid = true
        binding.apply {
            if (name.isBlank()){
                isValid = false
                etProductName.error = getString(R.string.empty_name)
            }

            if (category.isBlank()){
                isValid = false
                actvCategory.error = getString(R.string.txt_empty_product_category)
            }

            if (price.isBlank()){
                isValid = false
                etProductPrice.error = getString(R.string.txt_empty_product_price)
            }

            if (getFile == null && args.actionMode == ADD_MODE){
                isValid = false
                Toast.makeText(requireContext(), "Harap upload gambar", Toast.LENGTH_SHORT).show()
            }
        }
        return isValid
    }

    private fun editProduct(){
        binding.apply {
            val name = etProductName.text.toString().trim()
            val category = actvCategory.text.toString().trim()
            val price = etProductPrice.text.toString().trim()
            val isValid = checkInputValidation(name, category, price)


            if (isValid){
                viewModel.editProduct(
                    name,
                    category,
                    price.toFloat(),
                    getFile,
                    args.product!!
                ).observe(viewLifecycleOwner){ result ->
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
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.txt_edit_product_success),
                                    Toast.LENGTH_SHORT
                                ).show()
                                Navigation.findNavController(binding.root).navigateUp()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun addProduct(){
        binding.apply {
            val name = etProductName.text.toString().trim()
            val category = actvCategory.text.toString().trim()
            val price = etProductPrice.text.toString().trim()
            val isValid = checkInputValidation(name, category, price)

            if (isValid){
                viewModel.createProduct(
                    name,
                    category,
                    price.toFloat(),
                    getFile,
                ).observe(viewLifecycleOwner){ result ->
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
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.txt_add_product_success),
                                    Toast.LENGTH_SHORT
                                ).show()
                                Navigation.findNavController(binding.root).navigateUp()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun onClickAction(){
        binding.apply {
            topAppBar.setNavigationOnClickListener {
                Navigation.findNavController(binding.root).navigateUp()
            }
            cardThumbnail.setOnClickListener {
                showGetImageDialog()

            }
            btnCaptureImage.setOnClickListener {
                showGetImageDialog()
            }
            btnAddProduct.setOnClickListener {
                if (args.actionMode == EDIT_MODE && args.product != null) {
                    editProduct()
                } else {
                    addProduct()
                }
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
            } else {
                showGetImageDialog()
            }
        }
    }
}