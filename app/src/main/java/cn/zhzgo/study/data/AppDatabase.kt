package cn.zhzgo.study.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [ToolEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun toolDao(): ToolDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "zhzgo_study_database"
                )
                .addCallback(AppDatabaseCallback())
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback : RoomDatabase.Callback() {
        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateDatabase(database.toolDao())
                }
            }
        }

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateDatabase(database.toolDao())
                }
            }
        }

        suspend fun populateDatabase(toolDao: ToolDao) {
            val initialTools = listOf(
                ToolEntity("base_converter", "进制转换", "icon_base_converter", "tools/base_converter", category = "calc"),
                ToolEntity("calculator", "计算器", "icon_calculator", "tools/calculator", category = "calc"),
                ToolEntity("bmi_calculator", "BMI 计算", "icon_bmi", "tools/bmi_calculator", category = "health"),
                ToolEntity("date_calculator", "日期计算", "icon_date", "tools/date_calculator", category = "time"),
                ToolEntity("random_generater", "随机数", "icon_random", "tools/random_generator", category = "general"),
                ToolEntity("color_picker", "颜色转换", "icon_color", "tools/color_picker", category = "dev"),
                ToolEntity("password_generator", "密码生成", "icon_password", "tools/password_generator", category = "security"),
                ToolEntity("text_counter", "字数统计", "icon_text", "tools/text_counter", category = "text"),
                ToolEntity("stopwatch", "秒表计时", "icon_timer", "tools/stopwatch", category = "time"),
                ToolEntity("unit_converter", "单位换算", "icon_swap", "tools/unit_converter", category = "calc"),
                ToolEntity("timestamp_converter", "时间戳转换", "icon_clock", "tools/timestamp_converter", category = "dev"),
                ToolEntity("kinship_calculator", "亲戚称呼", "icon_family", "tools/kinship_calculator", category = "life"),
                ToolEntity("image_compress", "图片压缩", "icon_image", "tools/image_compress", category = "image"),
                ToolEntity("image_segment", "智能抠图", "icon_auto_fix_high", "tools/image_segment", category = "image"),
                ToolEntity("image_watermark", "抹除水印", "icon_brush", "tools/image_watermark", category = "image"),
                ToolEntity("image_filter", "一键滤镜", "icon_color_lens", "tools/image_filter", category = "image"),
                ToolEntity("doc_image_to_pdf", "图片转 PDF", "icon_picture_as_pdf", "tools/doc_image_to_pdf", category = "document"),
                ToolEntity("doc_pdf_to_image", "PDF 转图片", "icon_collections", "tools/doc_pdf_to_image", category = "document"),
                ToolEntity("doc_markdown", "Markdown 渲染", "icon_integration_instructions", "tools/doc_markdown", category = "document"),
                ToolEntity("media_video_to_audio", "提取音频", "icon_music_note", "tools/media_video_to_audio", category = "media"),
                ToolEntity("media_video_compress", "视频压缩", "icon_video_file", "tools/media_video_compress", category = "media"),
                ToolEntity("media_audio_format", "音频转换", "icon_graphic_eq", "tools/media_audio_format", category = "media"),
                ToolEntity("security_codec", "编码转换", "icon_pin", "tools/security_codec", category = "security"),
                ToolEntity("security_crypto", "加解密", "icon_security", "tools/security_crypto", category = "security"),
                ToolEntity("security_jwt", "JWT 解析", "icon_key", "tools/security_jwt", category = "security"),
                ToolEntity("dev_json_regex", "JSON/正则", "icon_code", "tools/dev_json_regex", category = "dev"),
                ToolEntity("dev_http_test", "网络测试", "icon_http", "tools/dev_http_test", category = "dev"),
                ToolEntity("video_parser", "视频解析", "icon_video_parser", "tools/video_parser", category = "media")
            )
            toolDao.insertAll(initialTools)
            // Cleanup legacy tools
            toolDao.deleteToolById("image_toolbox")
        }
    }
}
