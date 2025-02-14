package com.example.front.iot

import android.util.Log
import com.google.android.gms.wearable.*

class DataListenerService : WearableListenerService() {
    override fun onDataChanged(dataEvents: DataEventBuffer) {
        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED) {
                val dataItem = event.dataItem
                if (dataItem.uri.path == "/my_data") {
                    val dataMap = DataMapItem.fromDataItem(dataItem).dataMap
                    val receivedValue = dataMap.getString("key1")
                    Log.d("현빈", "동기화된 데이터: $receivedValue")
                }
            }
        }
    }
}