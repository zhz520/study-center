package cn.zhzgo.study.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tools")
data class ToolEntity(
    @PrimaryKey val id: String,
    val name: String,
    val iconResName: String, // Store resource name or identifier
    val route: String,       // Navigation route
    val usageCount: Int = 0,
    val lastUsedTime: Long = 0L,
    val isFavorite: Boolean = false,
    val category: String = "general"
)
