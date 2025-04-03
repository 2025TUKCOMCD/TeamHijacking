package com.example.front.transportation.DetailRouteFragment

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat

private val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_ADVERTISE,
        Manifest.permission.BLUETOOTH_ADMIN,  //관리자 정도의 기능 제공 일단 ADMIN으로 씀
        //  Manifest.permission.BLUETOOTH,  //간단한 기능 제공
        Manifest.permission.ACCESS_FINE_LOCATION, // 필요에 따라 추가
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
} else {
    arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.BLUETOOTH_ADMIN
        //Manifest.permission.BLUETOOTH,
    )
}

/*
기능 1. 블루투스 권한을 체크해 준다. 크게 Api30을 기준으로 각각 요구하는 권한에 대해 요청하고 또 받아온다.
기능 2. 권한을 다 검사 한 후 권한이 다 부여되어있다면 true 값을 아니라면 false 값을 반환한다.
 */
fun checkPermissions(activity: Activity) : Boolean {
    val rejectedPermissionList = ArrayList<String>()
    //권한들을 하나씩 검사
    for (permission in com.example.front.transportation.DetailRouteFragment.requiredPermissions) {
        if (ActivityCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {//만약 권한이 부여되어있지 않다
            Log.d("bluetoothConnect", permission.toString())
            rejectedPermissionList.add(permission) //rejectedPermissionList에 추가해 둠
        }
    }
    if (rejectedPermissionList.isNotEmpty()) {  //만약 거부된 권한 이 있다면
        val array = arrayOfNulls<String>(rejectedPermissionList.size)
        Log.d("bluetoothConnect", "권한요청")
        ActivityCompat.requestPermissions(activity, rejectedPermissionList.toArray(array), 2)  //권한을 요청함
        return false
    } else {
        return true  // 거부된 권한이 없다면 블루투스 권한성공을 출력
    }
}