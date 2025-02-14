package com.example.front.Iot

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.front.R
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.DataItem
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable

class Iot_page01 : Fragment() {

    private val TAG = "Iot_page01" // 로그 태그 추가

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_iot_page01, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val dataBtn = view.findViewById<Button>(R.id.data_btn1)

        dataBtn.setOnClickListener {
            sendData(requireContext(), "key1", "value2")
        }
    }

    private fun sendData(context: Context, key: String, value: String) {
        val dataClient = Wearable.getDataClient(context)
        val putDataReq = PutDataMapRequest.create("/my_data").run {
            dataMap.putString(key, value)
            asPutDataRequest()
        }

        val putDataTask: Task<DataItem> = dataClient.putDataItem(putDataReq)

        putDataTask.addOnSuccessListener {
            // 데이터 전송 성공
            Log.d(TAG, "데이터 전송 성공: $key = $value")
            Toast.makeText(context, "데이터 전송 성공", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { exception ->
            // 데이터 전송 실패
            Log.e(TAG, "데이터 전송 실패", exception)
            Toast.makeText(context, "데이터 전송 실패", Toast.LENGTH_SHORT).show()
        }
    }
}