package com.example.accidentdetection.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.accidentdetection.AccidentSafetyApp
import com.example.accidentdetection.R
import com.example.accidentdetection.databinding.FragmentSettingsBinding
import com.example.accidentdetection.service.SensorService

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val store = (requireActivity().application as AccidentSafetyApp).monitoringStateStore

        binding.switchAutoCall.isChecked = store.isAutoCallEnabled()
        binding.sliderImpactThreshold.value = store.getImpactThreshold()
        binding.textThresholdValue.text = getString(
            R.string.settings_threshold_value,
            binding.sliderImpactThreshold.value,
        )

        binding.switchAutoCall.setOnCheckedChangeListener { _, isChecked ->
            store.setAutoCallEnabled(isChecked)
        }

        binding.sliderImpactThreshold.addOnChangeListener { _, value, _ ->
            store.setImpactThreshold(value)
            binding.textThresholdValue.text = getString(R.string.settings_threshold_value, value)
        }

        binding.buttonBatterySettings.setOnClickListener {
            SensorService.openBatteryOptimizationSettings(requireContext())
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
