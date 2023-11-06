package com.ferrariofilippo.saveapp.model.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tags")
data class Tag(
    @PrimaryKey(autoGenerate = true) var id: Int,
    var name: String,
    var color: Int
)
