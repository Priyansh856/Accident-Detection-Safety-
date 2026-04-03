package com.example.accidentdetection.ui.contacts

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.example.accidentdetection.R
import com.example.accidentdetection.data.local.EmergencyContact
import com.example.accidentdetection.databinding.DialogEditContactBinding

class EditContactDialogFragment : DialogFragment() {

    private var _binding: DialogEditContactBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogEditContactBinding.inflate(LayoutInflater.from(requireContext()))
        val contact = arguments?.getParcelableCompat<EmergencyContact>(ARG_CONTACT)

        binding.inputName.setText(contact?.name.orEmpty())
        binding.inputPhone.setText(contact?.phoneNumber.orEmpty())
        binding.inputRelationship.setText(contact?.relationship.orEmpty())

        return AlertDialog.Builder(requireContext())
            .setTitle(if (contact == null) R.string.add_contact else R.string.edit_contact)
            .setView(binding.root)
            .setPositiveButton(R.string.save, null)
            .setNegativeButton(R.string.cancel, null)
            .create()
            .also { dialog ->
                dialog.setOnShowListener {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val name = binding.inputName.text?.toString()?.trim().orEmpty()
                        val phone = binding.inputPhone.text?.toString()?.trim().orEmpty()
                        val relationship = binding.inputRelationship.text?.toString()?.trim().orEmpty()
                        if (name.isBlank() || phone.isBlank()) {
                            Toast.makeText(
                                requireContext(),
                                R.string.contact_required_error,
                                Toast.LENGTH_SHORT,
                            ).show()
                        } else {
                            parentFragmentManager.setFragmentResult(
                                REQUEST_KEY,
                                bundleOf(
                                    BUNDLE_CONTACT to EmergencyContact(
                                        id = contact?.id ?: 0L,
                                        name = name,
                                        phoneNumber = phone,
                                        relationship = relationship,
                                    ),
                                ),
                            )
                            dismiss()
                        }
                    }
                }
            }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        const val REQUEST_KEY = "contact_result"
        const val BUNDLE_CONTACT = "bundle_contact"
        private const val ARG_CONTACT = "arg_contact"

        fun newInstance(contact: EmergencyContact? = null): EditContactDialogFragment {
            return EditContactDialogFragment().apply {
                arguments = bundleOf(ARG_CONTACT to contact)
            }
        }
    }
}
