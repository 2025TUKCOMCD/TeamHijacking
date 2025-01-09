package com.example.front.transportation

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.front.data.RouteProcessor
import kotlinx.coroutines.launch

class TransportationMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TransportationScreen()
        }
    }
}

@Composable
fun TransportationScreen() {
    var routeDataList by remember { mutableStateOf(listOf("Fetching routes...")) }
    val coroutineScope = rememberCoroutineScope()

    // 비동기 데이터 로드
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val result = RouteProcessor.fetchAndProcessRoutes(
                    startLat = 37.513841, // 잠실역
                    startLng = 127.101823,
                    endLat = 37.476813, // 낙성대역
                    endLng = 126.964156
                )
                routeDataList = result.split("\n")
            } catch (e: Exception) {
                Log.e("TransportationScreen", "Error fetching routes", e)
                routeDataList = listOf("Error fetching routes")
            }
        }
    }

    // UI 구성
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.TopStart
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(routeDataList) { route ->
                Text(text = route)
            }
        }
    }
}
