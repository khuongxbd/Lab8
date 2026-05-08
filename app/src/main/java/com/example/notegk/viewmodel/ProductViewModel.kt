package com.example.notegk.viewmodel

import androidx.lifecycle.ViewModel
import com.example.notegk.model.Product
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ProductUiState(
    val products: List<Product> = emptyList(),
    val selectedProduct: Product? = null,
    val isLoading: Boolean = false,
    val message: String? = null
)

class ProductViewModel(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val collection = firestore.collection("products")
    private var listenerRegistration: ListenerRegistration? = null

    private val _uiState = MutableStateFlow(ProductUiState(isLoading = true))
    val uiState: StateFlow<ProductUiState> = _uiState.asStateFlow()

    init {
        observeProducts()
    }

    private fun observeProducts() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        listenerRegistration = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = error.message ?: "Khong the tai du lieu"
                )
                return@addSnapshotListener
            }

            val docs = snapshot?.documents.orEmpty().map { doc ->
                Product(
                    id = doc.getString("id") ?: doc.id,
                    tenSanPham = doc.getString("tenSanPham") ?: "",
                    loaiSanPham = doc.getString("loaiSanPham") ?: "",
                    gia = doc.getLong("gia") ?: 0L,
                    file = doc.getString("file") ?: ""
                )
            }

            _uiState.value = _uiState.value.copy(
                products = docs,
                isLoading = false
            )
        }
    }

    fun selectForEdit(product: Product) {
        _uiState.value = _uiState.value.copy(selectedProduct = product)
    }

    fun clearSelectedProduct() {
        _uiState.value = _uiState.value.copy(selectedProduct = null)
    }

    fun addProduct(tenSanPham: String, loaiSanPham: String, giaText: String, fileBase64: String) {
        val gia = giaText.toLongOrNull()
        if (tenSanPham.isBlank() || loaiSanPham.isBlank() || gia == null || fileBase64.isBlank()) {
            _uiState.value = _uiState.value.copy(message = "Vui long nhap day du thong tin")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true)
        val docRef = collection.document()
        val product = Product(
            id = docRef.id,
            tenSanPham = tenSanPham.trim(),
            loaiSanPham = loaiSanPham.trim(),
            gia = gia,
            file = fileBase64
        )

        docRef.set(product)
            .addOnSuccessListener {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = "Them san pham thanh cong"
                )
            }
            .addOnFailureListener { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = e.message ?: "Them san pham that bai"
                )
            }
    }

    fun updateProduct(id: String, tenSanPham: String, loaiSanPham: String, giaText: String, fileBase64: String) {
        val gia = giaText.toLongOrNull()
        if (id.isBlank() || tenSanPham.isBlank() || loaiSanPham.isBlank() || gia == null || fileBase64.isBlank()) {
            _uiState.value = _uiState.value.copy(message = "Du lieu cap nhat khong hop le")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true)
        val product = Product(
            id = id,
            tenSanPham = tenSanPham.trim(),
            loaiSanPham = loaiSanPham.trim(),
            gia = gia,
            file = fileBase64
        )

        collection.document(id)
            .set(product)
            .addOnSuccessListener {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    selectedProduct = null,
                    message = "Cap nhat thanh cong"
                )
            }
            .addOnFailureListener { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = e.message ?: "Cap nhat that bai"
                )
            }
    }

    fun deleteProduct(id: String) {
        if (id.isBlank()) return

        _uiState.value = _uiState.value.copy(isLoading = true)
        collection.document(id)
            .delete()
            .addOnSuccessListener {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = "Xoa san pham thanh cong"
                )
            }
            .addOnFailureListener { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = e.message ?: "Xoa san pham that bai"
                )
            }
    }

    fun consumeMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }

    override fun onCleared() {
        listenerRegistration?.remove()
        super.onCleared()
    }
}
