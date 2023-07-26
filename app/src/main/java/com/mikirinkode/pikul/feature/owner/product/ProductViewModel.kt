package com.mikirinkode.pikul.feature.owner.product

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.mikirinkode.pikul.data.local.LocalPreference
import com.mikirinkode.pikul.data.model.PikulResult
import com.mikirinkode.pikul.data.model.Product
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val fireStore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val preferences: LocalPreference,
) : ViewModel() {

    fun getProductList(): LiveData<PikulResult<List<Product>>> {
        val result = MutableLiveData<PikulResult<List<Product>>>()

        result.postValue(PikulResult.Loading)

        val userId = auth.currentUser?.uid
        if (userId != null) {
            fireStore.collection("products").whereEqualTo("ownerId", userId).get()
                .addOnSuccessListener {documents ->
                    val list = ArrayList<Product>()
                    for (doc in documents) {
                        val product = doc.toObject(Product::class.java)
                        list.add(product)
                    }
                    result.postValue(PikulResult.Success(list))
                }
                .addOnFailureListener {
                    val errorMessage: String =
                        it.message ?: "Terjadi kesalahan saat mengambil data produk"
                    result.postValue(PikulResult.Error(errorMessage))
                }
        }


        return result
    }

    fun createProduct(
        name: String,
        category: String,
        price: Float,
        getFile: File?
    ): MutableLiveData<PikulResult<Boolean>> {
        val result = MutableLiveData<PikulResult<Boolean>>()
        result.postValue(PikulResult.Loading)

        val userId = auth.currentUser?.uid

        if (userId != null) {
            val ref = fireStore.collection("products").document()

            // upload the thumbnail first
            val path = "thumbnail_${ref.id}"

            val sRef: StorageReference =
                storage.reference.child("products/${ref.id}").child(path)

            val uri = Uri.fromFile(getFile)
            sRef.putFile(uri)
                .addOnProgressListener { taskSnapshot ->
                    // Update the progress bar as the upload progresses
                    val progress =
                        (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                    result.value = PikulResult.LoadingWithProgress(progress)
                }
                .addOnSuccessListener { uploadSnapshot ->
                    result.postValue(PikulResult.Loading)

                    uploadSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri ->
                        val data = Product(
                            productId = ref.id,
                            ownerId = userId,
                            productName = name,
                            productCategory = category,
                            productPrice = price,
                            productThumbnailUrl = uri.toString()
                        )

                        fireStore.collection("products").document(ref.id)
                            .set(data)
                            .addOnSuccessListener {
                                result.postValue(PikulResult.Success(true))
                            }
                            .addOnFailureListener {
                                val errorMessage: String =
                                    it.message ?: "Terjadi Kesalahan saat mengunggah data"
                                result.postValue(PikulResult.Error(errorMessage))
                            }
                    }
                }
                .addOnFailureListener {
                    val errorMessage: String =
                        it.message ?: "Terjadi Kesalahan saat mengunggah gambar"
                    result.postValue(PikulResult.Error(errorMessage))
                }
        }

        return result
    }

    companion object {
        private const val TAG = "BaseViewModel"
    }
}