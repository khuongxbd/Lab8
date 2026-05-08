package com.example.notegk.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notegk.model.Product
import com.example.notegk.ui.components.ProductItem
import com.example.notegk.utils.ImageUtils
import com.example.notegk.viewmodel.AuthViewModel
import com.example.notegk.viewmodel.ProductViewModel
import com.example.notegk.viewmodel.ProductUiState
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import androidx.compose.material3.ButtonDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAdminScreen(
    authViewModel: AuthViewModel,
    productViewModel: ProductViewModel = viewModel(),
    onLogoutSuccess: () -> Unit
) {
    val context = LocalContext.current
    val productState by productViewModel.uiState.collectAsStateWithLifecycle()
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()

    val currentUserEmail = Firebase.auth.currentUser?.email ?: ""
    val adminEmails = listOf("admin1@gmail.com", "huydeptry02@gmail.com")
    val isAdmin = adminEmails.contains(currentUserEmail)

    var pendingDelete by remember { mutableStateOf<Product?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(productState.message) {
        val message = productState.message ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        productViewModel.consumeMessage()
    }

    LaunchedEffect(authState.isLoggedIn) {
        if (!authState.isLoggedIn) {
            onLogoutSuccess()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (isAdmin) "Quản lý sản phẩm" else "Sản phẩm") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                actions = {
                    TextButton(onClick = authViewModel::logout) {
                        Text("Đăng xuất", color = Color(0xFF1565C0))
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                item {
                    HeaderSection(isAdmin = isAdmin)
                    Spacer(modifier = Modifier.height(16.dp))

                    if (isAdmin) {
                        AdminInputForm(
                            context = context,
                            productState = productState,
                            productViewModel = productViewModel,
                            snackbarHostState = snackbarHostState
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    Text(
                        "Danh sách sản phẩm",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF1565C0)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                items(productState.products, key = { it.id }) { item ->
                    ProductItem(
                        product = item,
                        isAdmin = isAdmin,
                        onEdit = { productViewModel.selectForEdit(it) },
                        onDelete = { pendingDelete = it }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            if (productState.isLoading || authState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }

    if (pendingDelete != null) {
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Xác nhận xóa") },
            text = { Text("Bạn có chắc chắn muốn xóa sản phẩm này không?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        productViewModel.deleteProduct(pendingDelete?.id.orEmpty())
                        pendingDelete = null
                    }
                ) {
                    Text("Xóa", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Composable
fun HeaderSection(isAdmin: Boolean) {
    if (!isAdmin) {
        Column {
            Text(
                text = "Bạn đang truy cập với quyền Người dùng. Bạn chỉ có thể xem danh sách.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun AdminInputForm(
    context: android.content.Context,
    productState: ProductUiState,
    productViewModel: ProductViewModel,
    snackbarHostState: SnackbarHostState
) {
    var tenSanPham by rememberSaveable { mutableStateOf("") }
    var loaiSanPham by rememberSaveable { mutableStateOf("") }
    var gia by rememberSaveable { mutableStateOf("") }
    var fileBase64 by rememberSaveable { mutableStateOf("") }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                fileBase64 = ImageUtils.uriToBase64(context, uri)
                Toast.makeText(context, "Đã chọn ảnh", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, e.message ?: "Lỗi xử lý ảnh", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(productState.selectedProduct?.id) {
        val selected = productState.selectedProduct
        if (selected != null) {
            tenSanPham = selected.tenSanPham
            loaiSanPham = selected.loaiSanPham
            gia = selected.gia.toString()
            fileBase64 = selected.file
        }
    }

    Column {
        Text("Thông tin sản phẩm", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFF1565C0))

        OutlinedTextField(
            value = tenSanPham,
            onValueChange = { tenSanPham = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            label = { Text("Tên sản phẩm") },
            singleLine = true
        )

        OutlinedTextField(
            value = loaiSanPham,
            onValueChange = { loaiSanPham = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            label = { Text("Loại sản phẩm") },
            singleLine = true
        )

        OutlinedTextField(
            value = gia,
            onValueChange = { gia = it.filter { c -> c.isDigit() } },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            label = { Text("Giá") },
            singleLine = true
        )

        OutlinedButton(
            onClick = { imagePicker.launch("image/*") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        ) {
            Text(if (fileBase64.isBlank()) "Chọn ảnh" else "Đã chọn ảnh")
        }

        Spacer(modifier = Modifier.height(12.dp))

        val selected = productState.selectedProduct
        if (selected == null) {
            Button(
                onClick = {
                    productViewModel.addProduct(
                        tenSanPham = tenSanPham,
                        loaiSanPham = loaiSanPham,
                        giaText = gia,
                        fileBase64 = fileBase64
                    )
                    tenSanPham = ""
                    loaiSanPham = ""
                    gia = ""
                    fileBase64 = ""
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
            ) {
                Text("Thêm sản phẩm")
            }
        } else {
            Button(
                onClick = {
                    productViewModel.updateProduct(
                        id = selected.id,
                        tenSanPham = tenSanPham,
                        loaiSanPham = loaiSanPham,
                        giaText = gia,
                        fileBase64 = fileBase64
                    )
                    tenSanPham = ""
                    loaiSanPham = ""
                    gia = ""
                    fileBase64 = ""
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
            ) {
                Text("Cập nhật sản phẩm")
            }

            TextButton(
                onClick = {
                    productViewModel.clearSelectedProduct()
                    tenSanPham = ""
                    loaiSanPham = ""
                    gia = ""
                    fileBase64 = ""
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Hủy sửa", color = Color(0xFF1565C0))
            }
        }
    }
}
