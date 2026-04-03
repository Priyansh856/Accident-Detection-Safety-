package com.example.accidentdetection.ui.alert

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.accidentdetection.R
import com.example.accidentdetection.databinding.ActivityAlertBinding
import com.example.accidentdetection.service.SensorService
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class AlertActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlertBinding
    private val viewModel: AlertViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlertBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonImSafe.setOnClickListener {
            viewModel.cancelAlert()
            SensorService.markSafe(this)
            Toast.makeText(this, R.string.alert_cancelled_toast, Toast.LENGTH_SHORT).show()
            finish()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.countdown.collect { seconds ->
                        binding.textCountdown.text = getString(R.string.countdown_format, seconds)
                        binding.countdownProgress.progress = seconds
                    }
                }
                launch {
                    viewModel.timedOut.collect { timedOut ->
                        if (timedOut) {
                            SensorService.triggerEmergency(this@AlertActivity)
                            Toast.makeText(
                                this@AlertActivity,
                                R.string.alert_sent_toast,
                                Toast.LENGTH_SHORT,
                            ).show()
                            finish()
                        }
                    }
                }
            }
        }

        viewModel.startCountdown()
    }

    companion object {
        const val EXTRA_REASON = "extra_reason"
    }
}
