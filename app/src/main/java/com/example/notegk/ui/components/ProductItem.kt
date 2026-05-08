package com.example.notegk.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.Alignment
import com.example.notegk.model.Product
import com.example.notegk.utils.ImageUtils
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ProductItem(
    product: Product,
    isAdmin: Boolean = true,
    onEdit: (Product) -> Unit,
    onDelete: (Product) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val bitmap = ImageUtils.base64ToBitmap(product.file)
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = product.tenSanPham,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(16.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(text = product.tenSanPham, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))

                if (isAdmin) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Loại: ${product.loaiSanPham}", style = MaterialTheme.typography.bodyMedium)
                }

                val formattedPrice = NumberFormat.getNumberInstance(Locale.Builder().setLanguage("vi").setRegion("VN").build()).format(product.gia)
                Text(
                    text = "Giá: $formattedPrice VNĐ",
                    style = MaterialTheme.typography.bodyLarge.copy(color = Color(0xFF1565C0), fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (isAdmin) {
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    IconButton(onClick = { onEdit(product) }) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Sửa", tint = Color(0xFF1565C0))
                    }
                    IconButton(onClick = { onDelete(product) }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Xóa", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}
