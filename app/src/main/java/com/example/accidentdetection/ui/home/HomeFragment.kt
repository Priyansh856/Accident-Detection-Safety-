package com.example.accidentdetection.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.accidentdetection.AccidentSafetyApp
import com.example.accidentdetection.MainActivity
import com.example.accidentdetection.R
import com.example.accidentdetection.databinding.FragmentHomeBinding
import com.example.accidentdetection.service.SensorService
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val app = requireActivity().application as AccidentSafetyApp

        binding.buttonPermissions.setOnClickListener {
            (activity as? MainActivity)?.requestCorePermissions()
        }

        binding.buttonStart.setOnClickListener {
            val host = activity as? MainActivity ?: return@setOnClickListener
            if (!host.hasCorePermissions()) {
                Toast.makeText(requireContext(), R.string.missing_permissions, Toast.LENGTH_SHORT).show()
                host.requestCorePermissions()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                if (app.repository.getContacts().isEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        R.string.monitoring_requires_contacts,
                        Toast.LENGTH_SHORT,
                    ).show()
                } else {
                    SensorService.start(requireContext())
                    updateMonitoringState()
                }
            }
        }

        binding.buttonStop.setOnClickListener {
            SensorService.stop(requireContext())
            updateMonitoringState()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            app.repository.observeCrashHistory().collectLatest { events ->
                binding.textCrashHistory.text = if (events.isEmpty()) {
                    getString(R.string.none_recorded)
                } else {
                    events.joinToString(separator = "\n") { event ->
                        val date = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault())
                            .format(Date(event.timestamp))
                        "$date - ${event.reason}"
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateMonitoringState()
    }

    private fun updateMonitoringState() {
        val store = (requireActivity().application as AccidentSafetyApp).monitoringStateStore
        val isActive = store.isMonitoringActive()
        binding.textStatus.text =
            getString(if (isActive) R.string.monitoring_active else R.string.monitoring_inactive)
        binding.textStatus.setTextColor(
            requireContext().getColor(if (isActive) R.color.status_active else R.color.status_inactive),
        )
        binding.textLastDetection.text = getString(R.string.last_detection, store.getLastDetection())
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
