// Copyright (c) 2024 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.model.entities

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "tags")
data class Tag(
    @PrimaryKey(autoGenerate = true) var id: Int,
    var name: String,
    var color: Int,
    var isIncome: Boolean,
    var parentTagId: Int = 0,
    var rootTagId: Int = 0,
    var path: String = ""
) {
    @Ignore var fullName: String = ""
}
