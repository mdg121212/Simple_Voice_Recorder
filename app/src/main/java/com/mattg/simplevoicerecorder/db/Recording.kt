package com.mattg.simplevoicerecorder.db

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "recordings")
@Parcelize
data class Recording(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    @ColumnInfo(name = "name")
    var name: String?,
    @ColumnInfo(name = "unixTimeMillis")
    val unixTimeMillis: Long,
    @ColumnInfo(name = "length_secs")
    var length_secs: Long?,
    @ColumnInfo(name = "readable_length")
    var readable_length: String?,
    @ColumnInfo(name = "extension")
    val extension: String

) : Parcelable {
    @Ignore
    constructor(
            name: String,
            unixTimeMillis: Long,
            length_secs: Long?,
            readable_length: String?,
            extension: String
    ) : this(
        0, name, unixTimeMillis, length_secs,readable_length, extension
    )
}