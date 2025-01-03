package com.example.front.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import com.example.front.data.RouteProcessor
import com.example.front.presentation.theme.FrontTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var routeData by remember { mutableStateOf("Loading...") }

            // API 호출 및 데이터 처리
            LaunchedEffect(Unit) {
                try {
                    val result = RouteProcessor.fetchAndProcessRoutes(
                        startLat = 37.513841, // 잠실역
                        startLng = 127.101823,
                        endLat = 37.476813, // 낙성대역
                        endLng = 126.964156
                    )
                    Log.d("MainActivity", "Received Data from RouteProcessor: $result")

                    withContext(Dispatchers.Main) {
                        routeData = result
                    }
                } catch (e: Exception) {
                    Log.e("MainActivity", "Error fetching routes", e)
                    routeData = "Failed to load data."
                }
            }

            WearApp(routeData)
        }
    }
}

@Composable
fun WearApp(routeData: String) {
    FrontTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            TimeText()
            Greeting(routeData)
        }
    }
}

@Composable
fun Greeting(routeData: String) {
    Text(
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colors.primary,
        text = routeData
    )
}

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearApp("Preview Android")
}