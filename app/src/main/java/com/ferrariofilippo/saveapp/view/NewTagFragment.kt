// Copyright (c) 2024 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.ferrariofilippo.saveapp.MainActivity
import com.ferrariofilippo.saveapp.R
import com.ferrariofilippo.saveapp.SaveAppApplication
import com.ferrariofilippo.saveapp.databinding.FragmentNewTagBinding
import com.ferrariofilippo.saveapp.model.entities.Tag
import com.ferrariofilippo.saveapp.util.ColorUtil
import com.ferrariofilippo.saveapp.util.TagUtil
import com.ferrariofilippo.saveapp.view.adapters.ColorDropdownAdapter
import com.ferrariofilippo.saveapp.view.adapters.TagsDropdownAdapter
import com.ferrariofilippo.saveapp.view.viewmodels.NewTagViewModel
import kotlinx.coroutines.runBlocking

class NewTagFragment : Fragment() {
    private lateinit var viewModel: NewTagViewModel

    private val _noneString = requireContext().getString(R.string.none)
    private val _whiteColor = requireContext().getColor(R.color.white)

    private var _binding: FragmentNewTagBinding? = null
    private val binding get(): FragmentNewTagBinding = _binding!!

    // Overrides
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewTagBinding
            .inflate(inflater, container, false)
            .apply {
                lifecycleOwner = viewLifecycleOwner
                vm = viewModel
            }

        checkIfEditing()
        setupUI()

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[NewTagViewModel::class.java]
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    // Methods
    private fun checkIfEditing() {
        val tagId = arguments?.getInt("tagId") ?: 0
        if (tagId == 0)
            return

        val application = requireActivity().application as SaveAppApplication
        val tag = runBlocking { application.tagRepository.getById(tagId) }
        viewModel.oldTag = tag
        viewModel.tagName.value = tag?.name
        viewModel.tagColor.value = tag?.color ?: viewModel.defaultColor

        viewModel.isIncomeTag.value = tag?.isIncome
        viewModel.isIncomeTagSwitchEnabled.value = false
    }

    private fun setupUI() {
        binding.saveButton.setOnClickListener { viewModel.insert() }
        binding.cancelButton.setOnClickListener { (activity as MainActivity).popLastView() }

        setupColorPicker()
        setupParentTagPicker()

        viewModel.onNameChanged = { manageNameError() }
    }

    private fun setupColorPicker() {
        val colorAutoComplete = binding.colorInput.editText as AutoCompleteTextView

        val adapter = ColorDropdownAdapter(
            binding.colorInput.context,
            R.layout.color_dropdown_item,
            ColorUtil.colors
        )

        colorAutoComplete.setAdapter(adapter)
        colorAutoComplete.setOnItemClickListener { parent, _, position, _ ->
            val selection = parent.adapter.getItem(position) as Int
            viewModel.tagColor.value = ColorUtil.getColorOrDefault(requireContext(), selection)
        }
    }

    private fun setupParentTagPicker() {
        val parentTagAutoComplete = binding.tagParentInput.editText as AutoCompleteTextView
        viewModel.tags.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {
                val tags = it.toMutableList()
                if (viewModel.oldTag != null) {
                    TagUtil.removeAllChildrenTags(tags, viewModel.oldTag!!)
                    tags.remove(viewModel.oldTag)
                }

                tags.add(0, Tag(0, _noneString, _whiteColor, false))

                val adapter = TagsDropdownAdapter(
                    binding.tagParentInput.context,
                    R.layout.tag_dropdown_item,
                    tags.toTypedArray()
                )

                parentTagAutoComplete.setAdapter(adapter)
                parentTagAutoComplete.setOnItemClickListener { parent, _, position, _ ->
                    val selection = parent.adapter.getItem(position) as Tag
                    if (selection.id == 0) {
                        viewModel.parentTag = null
                        viewModel.isIncomeTagSwitchEnabled.value = viewModel.oldTag != null && true
                    } else {
                        viewModel.parentTag = selection
                        viewModel.isIncomeTagSwitchEnabled.value = false
                        viewModel.isIncomeTag.value = selection.isIncome
                    }
                }
            }
        })
    }



    private fun manageNameError() {
        val res = requireContext().resources

        if (viewModel.tagName.value == null || viewModel.tagName.value!!.isBlank()) {
            binding.tagNameInput.error = res.getString(R.string.name_error)
        } else if (viewModel.tags.value?.indexOfFirst { it.name == viewModel.tagName.value } != -1) {
            binding.tagNameInput.error = res.getString(R.string.name_already_used)
        } else {
            binding.tagNameInput.error = null
        }
    }
}
