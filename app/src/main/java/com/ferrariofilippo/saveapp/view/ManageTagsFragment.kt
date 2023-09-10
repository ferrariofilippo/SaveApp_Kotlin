package com.ferrariofilippo.saveapp.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ferrariofilippo.saveapp.MainActivity
import com.ferrariofilippo.saveapp.R
import com.ferrariofilippo.saveapp.SaveAppApplication
import com.ferrariofilippo.saveapp.databinding.FragmentManageTagsBinding
import com.ferrariofilippo.saveapp.model.entities.Tag
import com.ferrariofilippo.saveapp.util.CustomRecyclerDecorator
import com.ferrariofilippo.saveapp.util.RecyclerEditAndDeleteGestures
import com.ferrariofilippo.saveapp.view.adapters.TagsAdapter
import com.ferrariofilippo.saveapp.view.viewmodels.ManageTagsViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class ManageTagsFragment : Fragment() {
    companion object {
        private const val INCOME_ID = 1
    }

    private lateinit var viewModel: ManageTagsViewModel

    private var _binding: FragmentManageTagsBinding? = null
    private val binding get() = _binding!!

    // Overrides
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManageTagsBinding
            .inflate(inflater, container, false)
            .apply {
                lifecycleOwner = viewLifecycleOwner
                vm = viewModel
            }

        setupUI()

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[ManageTagsViewModel::class.java]
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    // Methods
    private fun setupUI() {
        binding.newTagButton.setOnClickListener {
            (activity as MainActivity).gotToAddOrEditTag(0)
        }

        setupRecycler()
    }

    private fun setupRecycler() {
        val adapter = TagsAdapter(requireContext())

        binding.tagsRecyclerView.adapter = adapter
        binding.tagsRecyclerView.layoutManager = LinearLayoutManager(context)

        viewModel.tags.observe(viewLifecycleOwner, Observer { tags ->
            tags?.let {
                adapter.submitList(tags)
            }
        })

        setupRecyclerGestures()
        setupRecyclerDecorator()
    }

    private fun setupRecyclerGestures() {
        val gestureCallback = object : RecyclerEditAndDeleteGestures(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val adapter = binding.tagsRecyclerView.adapter as TagsAdapter
                val tag = adapter.getItemAt(position)

                if (direction == ItemTouchHelper.RIGHT)
                    (activity as MainActivity).gotToAddOrEditTag(tag.id)
                else if (direction == ItemTouchHelper.LEFT)
                    onRemoveTagInvoked(tag, position)
            }
        }
        gestureCallback.editOnlyRow = 0

        val itemTouchHelper = ItemTouchHelper(gestureCallback)
        itemTouchHelper.attachToRecyclerView(binding.tagsRecyclerView)
    }

    private fun setupRecyclerDecorator() {
        binding.tagsRecyclerView.addItemDecoration(
            CustomRecyclerDecorator(
                requireContext()
            )
        )
    }

    private fun onRemoveTagInvoked(item: Tag, position: Int) {
        val app = requireActivity().application as SaveAppApplication
        if (item.id == INCOME_ID)
            return

        val activity = requireActivity()
        lifecycleScope.launch {
            val firstMatchingTag = app.movementRepository.getFirstWithTag(item.id)

            if (firstMatchingTag != null) {
                (binding.tagsRecyclerView.adapter as TagsAdapter).notifyItemChanged(position)

                Snackbar.make(
                    activity.findViewById(R.id.containerView),
                    R.string.cannot_delete_tag,
                    Snackbar.LENGTH_SHORT
                ).setAnchorView(activity.findViewById(R.id.bottomAppBar)).show()
            } else {
                app.tagRepository.delete(item)

                Snackbar.make(
                    activity.findViewById(R.id.containerView),
                    R.string.tag_deleted,
                    Snackbar.LENGTH_SHORT
                )
                    .setAction(R.string.undo) {
                        lifecycleScope.launch {
                            app.tagRepository.insert(item)
                        }
                    }.setAnchorView(activity.findViewById(R.id.bottomAppBar)).show()
            }
        }
    }
}
