package com.example.week2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.week2.ui.theme.Week2_assignmentTheme

// A. Veri Katmanı ve Derleyici Sözleşmesi
@Immutable
data class HardwareItem(
    val id: String,
    val name: String,
    val category: String,
    val cost: Double,
    val stockCount: Int
)

val mockHardwareList = listOf(
    HardwareItem(id = "101", name = "MacBook Pro M3 Max", category = "Geliştirici Bilgisayarı", cost = 3499.0, stockCount = 5),
    HardwareItem(id = "102", name = "NVIDIA RTX 4090 Sunucu", category = "Yapay Zeka / GPU", cost = 1599.0, stockCount = 2),
    HardwareItem(id = "103", name = "Google Pixel 8 Pro", category = "Mobil Test Cihazı", cost = 999.0, stockCount = 8),
    HardwareItem(id = "104", name = "Dell UltraSharp 49\"", category = "Monitör", cost = 1199.0, stockCount = 4),
    HardwareItem(id = "105", name = "Sony WH-1000XM5 Kulaklık", category = "Odaklanma Ekipmanı", cost = 399.0, stockCount = 12),
    HardwareItem(id = "106", name = "Raspberry Pi 5 8GB", category = "IoT Geliştirme Kiti", cost = 85.0, stockCount = 25),
    HardwareItem(id = "107", name = "HHKB Professional Keyboard", category = "Ergonomik Klavye", cost = 299.0, stockCount = 0),
    HardwareItem(id = "108", name = "AMD Ryzen Threadripper", category = "Yapay Zeka / GPU", cost = 4999.0, stockCount = 1)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Week2_assignmentTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DevClusterApp()
                }
            }
        }
    }
}

// B & C. Durum Yönetimi, Merkezi Stateful Kapsayıcı
@Composable
fun DevClusterApp() {
    // Konfigürasyon Değişim Koruması
    var searchQuery by rememberSaveable { mutableStateOf("") }

    // Çalışma Zamanı Hafıza Kilidi
    var totalSpent by remember { mutableDoubleStateOf(0.0) }

    // Turnike Mantığı (Memoization)
    val filteredList = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            mockHardwareList
        } else {
            mockHardwareList.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.category.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "DevCluster Yönetim Paneli",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Toplam Harcanan: $${totalSpent}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        HardwareSearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // C. Liste Mimarisi (Tembel Yükleme ve Nokta Atışı Recomposition)
        LazyColumn {
            items(
                items = filteredList,
                key = { item -> item.id }
            ) { item ->
                HardwareCard(
                    item = item,
                    onClick = {
                        if (item.stockCount > 0) {
                            totalSpent += item.cost
                        }
                    }
                )
            }
        }
    }
}

// D. Mimari Ayrışma (Saf Fonksiyon - SearchBar)
@Composable
fun HardwareSearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange, // Events Up
        label = { Text("Donanım veya Kategori Ara") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
}

// D. Mimari Ayrışma (Saf Fonksiyon - Card)
@Composable
fun HardwareCard(
    item: HardwareItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            // Modifier Sıralaması: Hoca'nın istediği gibi önce clickable, sonra padding
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = item.name, style = MaterialTheme.typography.titleMedium)
            Text(text = item.category, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "$${item.cost}")
                Text(
                    text = if (item.stockCount > 0) "Stok: ${item.stockCount}" else "Tükendi",
                    color = if (item.stockCount > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}