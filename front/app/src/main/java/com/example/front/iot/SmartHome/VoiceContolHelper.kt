package com.example.front.iot.SmartHome

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.util.Log
import java.util.Locale

class VoiceControlHelper(private val activity: Activity, private val onCommandProcessed: (String) -> Unit) {
    private val VOICE_REQUEST_CODE = 1001

    //음성 인식 시작
    fun startVoiceRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }
        activity.startActivityForResult(intent, VOICE_REQUEST_CODE)
    }

    fun handleVoiceResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == VOICE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val matches = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val command = matches?.get(0) ?: return
            Log.d("VoiceControl", "Recognized command: $command")
            onCommandProcessed(command)
        }
    }
}