package com.example.accidentdetection.ui.contacts

import android.Manifest
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.accidentdetection.AccidentSafetyApp
import com.example.accidentdetection.R
import com.example.accidentdetection.data.local.EmergencyContact
import com.example.accidentdetection.databinding.FragmentContactsBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ContactsFragment : Fragment() {

    private var _binding: FragmentContactsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ContactsAdapter

    private val pickContact =
        registerForActivityResult(ActivityResultContracts.PickContact()) { uri ->
            if (uri == null) return@registerForActivityResult
            val resolver = requireContext().contentResolver
            val cursor: Cursor? = resolver.query(
                uri,
                arrayOf(
                    ContactsContract.Contacts.DISPLAY_NAME,
                    ContactsContract.Contacts._ID,
                ),
                null,
                null,
                null,
            )
            cursor?.use {
                if (it.moveToFirst()) {
                    val name = it.getString(0).orEmpty()
                    val contactId = it.getString(1)
                    val phoneCursor = resolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                        "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                        arrayOf(contactId),
                        null,
                    )
                    phoneCursor?.use { phones ->
                        if (phones.moveToFirst()) {
                            showDialog(
                                EmergencyContact(
                                    name = name,
                                    phoneNumber = phones.getString(0).orEmpty(),
                                    relationship = "",
                                ),
                            )
                        }
                    }
                }
            }
        }

    private val requestContactsPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                pickContact.launch(null)
            } else {
                Toast.makeText(requireContext(), R.string.missing_permissions, Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentContactsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val app = requireActivity().application as AccidentSafetyApp

        adapter = ContactsAdapter(
            onEdit = { showDialog(it) },
            onDelete = { contact ->
                viewLifecycleOwner.lifecycleScope.launch {
                    app.repository.deleteContact(contact)
                }
            },
        )
        binding.recyclerContacts.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerContacts.adapter = adapter

        binding.fabAddContact.setOnClickListener { showDialog() }
        binding.buttonImport.setOnClickListener {
            val permission = Manifest.permission.READ_CONTACTS
            if (ContextCompat.checkSelfPermission(requireContext(), permission) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                pickContact.launch(null)
            } else {
                requestContactsPermission.launch(permission)
            }
        }

        parentFragmentManager.setFragmentResultListener(
            EditContactDialogFragment.REQUEST_KEY,
            viewLifecycleOwner,
        ) { _, bundle ->
            val contact = bundle.getParcelableCompat<EmergencyContact>(EditContactDialogFragment.BUNDLE_CONTACT)
                ?: return@setFragmentResultListener
            viewLifecycleOwner.lifecycleScope.launch {
                app.repository.saveContact(contact)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            app.repository.observeContacts().collectLatest { contacts ->
                adapter.submitList(contacts)
                binding.textEmpty.visibility = if (contacts.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    private fun showDialog(contact: EmergencyContact? = null) {
        EditContactDialogFragment.newInstance(contact)
            .show(parentFragmentManager, "EditContactDialog")
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
