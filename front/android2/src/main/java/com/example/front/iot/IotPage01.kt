package com.example.front.iot

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
import com.google.android.gms.common.api.ApiException

class IotPage01 : Fragment() {

    private val TAG = "Iot_page01"

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
            sendData(requireContext(), "key1", "value2") // Wear OS 기기 여부 확인 없이 바로 데이터 전송
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
            Log.d(TAG, "데이터 전송 성공: $key = $value")
            Toast.makeText(context, "데이터 전송 성공", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { exception ->
            Log.e(TAG, "데이터 전송 실패: ${exception.message}", exception)
            Toast.makeText(context, "데이터 전송 실패: ${exception.message}", Toast.LENGTH_SHORT).show()

            if (exception is ApiException) {
                val apiException = exception as ApiException
                Log.e(TAG, "API Exception Status Code: ${apiException.statusCode}")
                // 필요에 따라 추가적인 오류 처리 로직 구현 (예: 특정 상태 코드에 따른 다른 UI 표시)
            }
        }
    }
}