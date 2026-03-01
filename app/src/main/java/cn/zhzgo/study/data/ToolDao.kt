package cn.zhzgo.study.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ToolDao {
    @Query("SELECT * FROM tools ORDER BY usageCount DESC, lastUsedTime DESC")
    fun getAllTools(): Flow<List<ToolEntity>>

    @Query("SELECT * FROM tools ORDER BY usageCount DESC, lastUsedTime DESC LIMIT :limit")
    fun getTopTools(limit: Int): Flow<List<ToolEntity>>

    @Query("SELECT * FROM tools WHERE isFavorite = 1 ORDER BY lastUsedTime DESC")
    fun getFavoriteTools(): Flow<List<ToolEntity>>

    @Query("SELECT * FROM tools WHERE id = :toolId")
    fun getToolById(toolId: String): ToolEntity?

    @Query("SELECT * FROM tools WHERE id IN (:toolIds) ORDER BY usageCount DESC, lastUsedTime DESC")
    fun getToolsByIds(toolIds: List<String>): Flow<List<ToolEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(tools: List<ToolEntity>)

    @Update
    fun updateTool(tool: ToolEntity)

    @Query("UPDATE tools SET usageCount = usageCount + 1, lastUsedTime = :timestamp WHERE id = :toolId")
    fun incrementUsage(toolId: String, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE tools SET isFavorite = :isFavorite WHERE id = :toolId")
    fun setFavorite(toolId: String, isFavorite: Boolean)

    @Query("DELETE FROM tools WHERE id = :toolId")
    fun deleteToolById(toolId: String)
}
