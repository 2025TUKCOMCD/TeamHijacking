package com.example.front.audioguide

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat










//----------------------------권한 요청용 코드 -------------------------------
//필요한 권한들을 쭉 적어놓음 나중에 사용 예정
//버전에 따라 요구해야 하는 permission 구분
private val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_ADVERTISE,
        Manifest.permission.BLUETOOTH_ADMIN,  //관리자 정도의 기능 제공 일단 ADMIN으로 씀
        //  Manifest.permission.BLUETOOTH,  //간단한 기능 제공
        // Manifest.permission.ACCESS_FINE_LOCATION // 필요에 따라 추가
    )
} else {
    arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.BLUETOOTH_ADMIN
        //Manifest.permission.BLUETOOTH,
    )
}

// 권한을 확인하는 함수로 위에 넣어둔 requiredPermissions들을 토대로 권한 검사
fun checkPermissions(activity: Activity) {
    val rejectedPermissionList = ArrayList<String>()
    //권한들을 하나씩 검사
    for (permission in requiredPermissions) {
        if (ActivityCompat.checkSelfPermission(
                activity,
                permission
            ) != PackageManager.PERMISSION_GRANTED   //만약 권한이 부여되어있지 않다면
        ) {
            Log.d("bluetoothConnect", permission.toString())
            rejectedPermissionList.add(permission) //rejectedPermissionList에 추가해 둠
        }
    }
    if (rejectedPermissionList.isNotEmpty()) {  //만약 거부된 권한 이 있다면
        val array = arrayOfNulls<String>(rejectedPermissionList.size)
        Log.d("bluetoothConnect", "권한요청")
        ActivityCompat.requestPermissions(activity, rejectedPermissionList.toArray(array), 2)  //권한을 요청함
    } else {
        Toast.makeText(activity, "권한 허용", Toast.LENGTH_SHORT).show()  // 거부된 권한이 없다면 블루투스 권한성공을 출력
    }
}