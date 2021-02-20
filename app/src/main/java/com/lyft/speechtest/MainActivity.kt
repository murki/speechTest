package com.lyft.speechtest

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.MotionEvent
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.lyft.speechtest.databinding.ActivityMainBinding
import java.util.*


class MainActivity : AppCompatActivity() {

    private val recordAudioRequestCode = 333
    private lateinit var binding: ActivityMainBinding
    private lateinit var speechRecognizer: SpeechRecognizer
    private var isReadyForSpeechCalled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            checkPermission()
        }
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        val intent = setupSpeech()
        binding.button.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                speechRecognizer.stopListening()
            }
            if (event.action == MotionEvent.ACTION_DOWN) {
                binding.button.setImageResource(android.R.drawable.presence_audio_away)
                isReadyForSpeechCalled = false
                speechRecognizer.startListening(intent)
            }
            return@setOnTouchListener false
        }
    }

    private fun setupSpeech() : Intent {
        val speechRecognizerIntent: Intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en-US")
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d("MainActivity", "onReadyForSpeech")
                isReadyForSpeechCalled = true
                binding.text.setText("Ready", TextView.BufferType.EDITABLE)
            }

            override fun onBeginningOfSpeech() {
                Log.d("MainActivity", "onBeginningOfSpeech")
                binding.text.setText("Listening...", TextView.BufferType.EDITABLE)
            }

            override fun onRmsChanged(rmsdB: Float) {
                Log.d("MainActivity", "onRmsChanged")
            }

            override fun onBufferReceived(buffer: ByteArray?) {
                Log.d("MainActivity", "onBufferReceived")
            }

            override fun onEndOfSpeech() {
                Log.d("MainActivity", "onEndOfSpeech")
            }

            override fun onError(error: Int) {
                Log.d("MainActivity", "onError=$error")
                // workaround for https://issuetracker.google.com/issues/37053152
                if (error == SpeechRecognizer.ERROR_NO_MATCH && !isReadyForSpeechCalled) {
                    speechRecognizer.startListening(intent)
                } else {
                    binding.button.setImageResource(android.R.drawable.presence_audio_online)
                    binding.text.setText("Error=$error", TextView.BufferType.EDITABLE)
                }
            }

            override fun onResults(results: Bundle?) {
                binding.button.setImageResource(android.R.drawable.presence_audio_online)
                Log.d("MainActivity", "onResults")
                val data = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val scores = results?.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)
                binding.text.setText("Result=${data?.get(0) ?: "N/A"}. Score=${scores?.get(0) ?: "N/A"}", TextView.BufferType.EDITABLE)
            }

            override fun onPartialResults(partialResults: Bundle?) {
                Log.d("MainActivity", "onPartialResults")
            }

            override fun onEvent(eventType: Int, params: Bundle?) {
                Log.d("MainActivity", "onEvent")
            }

        })
        return speechRecognizerIntent
    }

    override fun onDestroy() {
        speechRecognizer.destroy()
        super.onDestroy()
    }

    private fun checkPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), recordAudioRequestCode)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == recordAudioRequestCode && grantResults.isNotEmpty()){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
        }
    }
}