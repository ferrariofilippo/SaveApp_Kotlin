package com.ferrariofilippo.saveapp.view.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ferrariofilippo.saveapp.MainActivity
import com.ferrariofilippo.saveapp.R
import com.ferrariofilippo.saveapp.SaveAppApplication
import com.ferrariofilippo.saveapp.model.entities.Tag
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class NewTagViewModel(application: Application) : AndroidViewModel(application) {
    private val saveAppApplication = application as SaveAppApplication

    val tags: MutableLiveData<List<Tag>> = MutableLiveData(listOf())

    val colors: List<Int>

    val tagName: MutableLiveData<String> = MutableLiveData("")

    val tagColor: MutableLiveData<Int> = MutableLiveData(R.color.emerald_700)

    val displayColor: MutableLiveData<Int> =
        MutableLiveData(application.getColor(R.color.emerald_700))

    var oldTag: Tag? = null

    var onNameChanged: () -> Unit = { }

    init {
        colors = initializeColors()

        viewModelScope.launch {
            tags.value = saveAppApplication.tagRepository.allTags.first()
        }
    }

    fun insert() = viewModelScope.launch {
        if (tagName.value != null && tagName.value!!.isNotBlank()) {
            val tag = Tag(oldTag?.id ?: 0, tagName.value!!, tagColor.value ?: R.color.emerald_700)
            if (tag.id == 0)
                saveAppApplication.tagRepository.insert(tag)
            else
                saveAppApplication.tagRepository.update(tag)

            val activity = saveAppApplication.getCurrentActivity() as MainActivity
            activity.goBackToManageTags()
            Snackbar.make(
                activity.findViewById(R.id.containerView),
                if (tag.id == 0) R.string.tag_created else R.string.tag_updated,
                Snackbar.LENGTH_SHORT
            ).setAnchorView(activity.findViewById(R.id.bottomAppBar)).show()
        } else {
            onNameChanged()
        }
    }

    private fun initializeColors(): List<Int> {
        return listOf(
            R.color.cyan_50,
            R.color.cyan_100,
            R.color.cyan_200,
            R.color.cyan_300,
            R.color.cyan_400,
            R.color.cyan_500,
            R.color.cyan_600,
            R.color.cyan_700,
            R.color.cyan_800,
            R.color.cyan_900,
            R.color.blue_50,
            R.color.blue_100,
            R.color.blue_200,
            R.color.blue_300,
            R.color.blue_400,
            R.color.blue_500,
            R.color.blue_600,
            R.color.blue_700,
            R.color.blue_800,
            R.color.blue_900,
            R.color.emerald_50,
            R.color.emerald_100,
            R.color.emerald_200,
            R.color.emerald_300,
            R.color.emerald_400,
            R.color.emerald_500,
            R.color.emerald_600,
            R.color.emerald_700,
            R.color.emerald_800,
            R.color.emerald_900,
            R.color.green_50,
            R.color.green_100,
            R.color.green_200,
            R.color.green_300,
            R.color.green_400,
            R.color.green_500,
            R.color.green_600,
            R.color.green_700,
            R.color.green_800,
            R.color.green_900,
            R.color.red_50,
            R.color.red_100,
            R.color.red_200,
            R.color.red_300,
            R.color.red_400,
            R.color.red_500,
            R.color.red_600,
            R.color.red_700,
            R.color.red_800,
            R.color.red_900,
            R.color.purple_50,
            R.color.purple_100,
            R.color.purple_200,
            R.color.purple_300,
            R.color.purple_400,
            R.color.purple_500,
            R.color.purple_600,
            R.color.purple_700,
            R.color.purple_800,
            R.color.purple_900
        )
    }
}
