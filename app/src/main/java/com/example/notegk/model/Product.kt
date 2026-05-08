package com.example.notegk.model

data class Product(
    val id: String = "", // Dung de dinh danh document tren Firestore
    val tenSanPham: String = "",
    val loaiSanPham: String = "",
    val gia: Long = 0L,
    val file: String = "" // Luu chuoi Base64 cua hinh anh
)
