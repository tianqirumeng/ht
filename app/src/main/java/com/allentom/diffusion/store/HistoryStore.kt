package com.allentom.diffusion.store

import android.content.Context
import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import com.allentom.diffusion.Util
import com.allentom.diffusion.api.ControlNetParam
import com.allentom.diffusion.ui.screens.home.tabs.draw.DrawViewModel
import java.io.Serializable

enum class PromptType(val value: Int) {
    Prompt(0),
    NegativePrompt(1),
    LoraPrompt(2),
    LoraTrigger(4),
    EmbeddingPrompt(3),
}

class SaveHrParam(
    val enableScale: Boolean,
    val hrScale: Float,
    val hrDenosingStrength: Float,
    val hrUpscaler: String,
) : Serializable {
    fun toEntity(): HrHistoryEntity {
        return HrHistoryEntity(
            enableScale = enableScale,
            hrScale = hrScale,
            hrDenosingStrength = hrDenosingStrength,
            hrUpscaler = hrUpscaler,
            historyId = 0,
        )
    }
}

class ImageHistory(
    val imageHistoryId: Long = 0,
    val path: String,
    val name: String,
    val seed: Int,
    val favourite: Boolean = false,
    val historyId: Long
) : Serializable

class SaveHistory(
    val id: Long = 0,
    val prompt: List<Prompt>,
    val negativePrompt: List<Prompt>,
    var loraPrompt: List<LoraPrompt>,
    val embeddingPrompt: List<EmbeddingPrompt> = emptyList(),
    val steps: Int,
    val samplerName: String,
    val sdModelCheckpoint: String,
    val width: Int,
    val height: Int,
    val batchSize: Int,
    val cfgScale: Float,
    val time: Long,
    val imagePaths: List<ImageHistory>,
    val hrParam: SaveHrParam,
    val img2imgParam: Img2imgParam? = null,
    var controlNetParam: ControlNetParam? = null,
    val model: ModelEntity? = null,
    val regionRatio:String? = "",
    val regionCount:Int? = 0,
    val regionUseCommon:Boolean? = false,
    val regionEnable: Boolean? = false
) : Serializable

@Entity(primaryKeys = ["promptId", "historyId"], tableName = "prompt_history")
data class PromptHistoryCrossRef(
    val promptId: Long,
    val historyId: Long
)

@Entity
@Dao
interface PromptHistoryDao {
    @Insert
    fun insert(promptHistoryCrossRef: PromptHistoryCrossRef)

    @Update
    fun update(promptHistoryCrossRef: PromptHistoryCrossRef)

    @Query("SELECT * FROM prompt_history WHERE promptId = :promptId")
    fun getPromptHistory(promptId: Long): List<PromptHistoryCrossRef>
}

@Entity(primaryKeys = ["promptId", "historyId"], tableName = "negative_prompt_history")
data class NegativePromptHistoryCrossRef(
    val promptId: Long,
    val historyId: Long
)

@Dao
interface NegativePromptHistoryDao {
    @Insert
    fun insert(negativePromptHistoryCrossRef: NegativePromptHistoryCrossRef)

    @Update
    fun update(negativePromptHistoryCrossRef: NegativePromptHistoryCrossRef)

    @Query("SELECT * FROM negative_prompt_history WHERE promptId = :promptId and historyId = :historyId")
    fun getNegativePromptHistory(promptId: Long, historyId: Long): NegativePromptHistoryCrossRef?


}

@Entity(primaryKeys = ["loraPromptId", "historyId"], tableName = "lora_prompt_history")
data class LoraPromptHistoryCrossRef(
    val loraPromptId: Long,
    val historyId: Long
)

@Dao
interface LoraPromptHistoryDao {
    @Insert
    fun insert(loraPromptHistoryCrossRef: LoraPromptHistoryCrossRef)

    @Update
    fun update(loraPromptHistoryCrossRef: LoraPromptHistoryCrossRef)
}

@Entity(primaryKeys = ["embeddingId", "historyId"], tableName = "embedding_history")
data class EmbeddingHistoryCrossRef(
    val embeddingId: Long,
    val historyId: Long
)

@Dao
interface EmbeddingHistoryDao {
    @Insert
    fun insert(embeddingHistoryCrossRef: EmbeddingHistoryCrossRef)

    @Update
    fun update(embeddingHistoryCrossRef: EmbeddingHistoryCrossRef)
}

@Entity(tableName = "hr_history")
data class HrHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val hrHistoryId: Long = 0,
    val enableScale: Boolean,
    val hrScale: Float,
    val hrDenosingStrength: Float,
    val hrUpscaler: String,
    val historyId: Long,
) {
    companion object {
        fun fromHrParam(hrParam: SaveHrParam, historyId: Long): HrHistoryEntity {
            return HrHistoryEntity(
                enableScale = hrParam.enableScale,
                hrScale = hrParam.hrScale,
                hrDenosingStrength = hrParam.hrDenosingStrength,
                hrUpscaler = hrParam.hrUpscaler,
                historyId = historyId,
            )
        }

    }

    fun toSaveHrParam(): SaveHrParam {
        return SaveHrParam(
            enableScale = enableScale,
            hrScale = hrScale,
            hrDenosingStrength = hrDenosingStrength,
            hrUpscaler = hrUpscaler,
        )
    }
}

@Dao
interface HrHistoryDao {
    @Insert
    fun insert(hrHistoryEntity: HrHistoryEntity)

    @Update
    fun update(hrHistoryEntity: HrHistoryEntity)

    @Query("SELECT * FROM hr_history WHERE historyId = :historyId limit 1")
    fun getHrHistory(historyId: Long): HrHistoryEntity?
}

@Entity(tableName = "image_history")
data class ImageHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val imageHistoryId: Long = 0,
    val name: String,
    val path: String,
    val favourite: Boolean,
    val seed: Int,
    val historyId: Long,
) {
    companion object {
        fun fromImageHistory(imageHistory: ImageHistory, historyId: Long): ImageHistoryEntity {
            return ImageHistoryEntity(
                imageHistoryId = imageHistory.imageHistoryId,
                path = imageHistory.path,
                seed = imageHistory.seed,
                historyId = historyId,
                favourite = imageHistory.favourite,
                name = imageHistory.name,
            )
        }
    }

    fun toImageHistory(): ImageHistory {
        return ImageHistory(
            imageHistoryId = imageHistoryId,
            path = path,
            seed = seed,
            name = name,
            favourite = favourite,
            historyId = historyId,
        )
    }
}

@Entity(tableName = "prompt_extra")
data class PromptExtraEntity(
    @PrimaryKey(autoGenerate = true)
    val promptExtraId: Long = 0,
    val promptId: Long,
    val priority: Int,
    val weight: Float = 0f,
    val historyId: Long,
    val promptType: Int,
    val loraPromptId: Long = 0,
    val regionIndex:Int? = 0
)

@Dao
interface PromptExtraDao {
    @Insert
    fun insert(promptExtraEntity: PromptExtraEntity)

    @Update
    fun update(promptExtraEntity: PromptExtraEntity)

    @Query("SELECT * FROM prompt_extra WHERE historyId = :historyId")
    fun getPromptExtra(historyId: Long): List<PromptExtraEntity>
}

@Dao
interface ImageHistoryDao {
    @Insert
    fun insert(imageHistoryEntity: ImageHistoryEntity)

    @Update
    fun update(imageHistoryEntity: ImageHistoryEntity)

    @Query("SELECT * FROM image_history WHERE name = :name")
    fun getImageHistoryWithName(name: String): ImageHistoryEntity?

    @Query("SELECT * FROM image_history WHERE favourite = 1 ORDER BY imageHistoryId DESC")
    fun getFavouriteImageHistory(): List<ImageHistoryEntity>

}

class Img2imgParam(
    val denoisingStrength: Float,
    val resizeMode: Int,
    val scaleBy: Float,
    val width: Int,
    val height: Int,
    val cfgScale: Float,
    val path: String,
    val historyId: Long,
) : Serializable

@Entity(tableName = "img2img")
data class Img2ImgEntity(
    @PrimaryKey(autoGenerate = true)
    val img2ImgId: Long = 0,
    val denoisingStrength: Float,
    val resizeMode: Int,
    val scaleBy: Float,
    val width: Int,
    val height: Int,
    val cfgScale: Float,
    val path: String,
    val historyId: Long,
) {
    companion object {
        fun fromImg2imgParam(img2imgParam: Img2imgParam, historyId: Long): Img2ImgEntity {
            return Img2ImgEntity(
                denoisingStrength = img2imgParam.denoisingStrength,
                resizeMode = img2imgParam.resizeMode,
                scaleBy = img2imgParam.scaleBy,
                width = img2imgParam.width,
                height = img2imgParam.height,
                cfgScale = img2imgParam.cfgScale,
                path = img2imgParam.path,
                historyId = historyId,
            )
        }
    }

    fun toImg2imgParam(): Img2imgParam {
        return Img2imgParam(
            denoisingStrength = denoisingStrength,
            resizeMode = resizeMode,
            scaleBy = scaleBy,
            width = width,
            height = height,
            cfgScale = cfgScale,
            path = path,
            historyId = historyId,
        )
    }
}

@Dao
interface Img2ImgDao {
    @Insert
    fun insert(img2ImgEntity: Img2ImgEntity)

    @Update
    fun update(img2ImgEntity: Img2ImgEntity)

    @Query("SELECT * FROM img2img WHERE historyId = :historyId")
    fun getImg2ImgParam(historyId: Long): Img2ImgEntity?
}

data class HistoryWithRelation(
    @Embedded val historyEntity: HistoryEntity,
    @Relation(
        parentColumn = "historyId",
        entityColumn = "promptId",
        associateBy = Junction(PromptHistoryCrossRef::class)
    )
    val prompts: List<SavePrompt> = emptyList(),

    @Relation(
        parentColumn = "historyId",
        entityColumn = "promptId",
        associateBy = Junction(NegativePromptHistoryCrossRef::class)
    )
    val negativePrompts: List<SavePrompt> = emptyList(),

    @Relation(
        parentColumn = "historyId",
        entityColumn = "loraPromptId",
        associateBy = Junction(LoraPromptHistoryCrossRef::class)
    )
    val loraPrompts: List<LoraPromptEntity> = emptyList(),

    @Relation(
        parentColumn = "historyId",
        entityColumn = "embeddingId",
        associateBy = Junction(EmbeddingHistoryCrossRef::class)
    )
    val embeddingPrompts: List<EmbeddingEntity> = emptyList(),

    @Relation(
        parentColumn = "historyId",
        entityColumn = "historyId",
    )
    val imagePaths: List<ImageHistoryEntity> = emptyList(),

    @Relation(
        parentColumn = "historyId",
        entityColumn = "historyId",
    )
    val hrParamEntity: HrHistoryEntity? = null,
    @Relation(
        parentColumn = "historyId",
        entityColumn = "historyId",
    )
    val img2imgParam: Img2ImgEntity? = null,

    @Relation(
        parentColumn = "historyId",
        entityColumn = "historyId",
    )
    val controlNetHistoryEntity: ControlNetHistoryEntity? = null,

    @Relation(
        parentColumn = "historyId",
        entityColumn = "historyId",
    )
    val promptExtraEntity: List<PromptExtraEntity> = emptyList(),

    @Relation(
        parentColumn = "modelId",
        entityColumn = "modelId",
    )
    val modelEntity: ModelEntity? = null,
) {
    fun toSaveHistory(): SaveHistory {
        val result = SaveHistory(
            id = historyEntity.historyId,
//            prompt = prompts.map {
//                val obj = it.toPrompt()
//                promptExtraEntity.find { promptExtra ->
//                    promptExtra.promptId == it.promptId && promptExtra.promptType == PromptType.Prompt.value
//                }?.let { extra ->
//                    obj.piority = extra.priority
//                    obj.regionIndex = extra.regionIndex ?: 0
//                }
//                obj
//            },
            prompt = promptExtraEntity.mapNotNull { promptExtraEntity ->
                val prompt = prompts.find { it.promptId == promptExtraEntity.promptId }
                prompt?.let {
                    val obj = it.toPrompt()
                    obj.piority = promptExtraEntity.priority
                    obj.regionIndex = promptExtraEntity.regionIndex ?: 0
                    obj
                }
            },
            negativePrompt = negativePrompts.map {
                val obj = it.toPrompt()
                promptExtraEntity.find { promptExtra ->
                    promptExtra.promptId == it.promptId && promptExtra.promptType == PromptType.NegativePrompt.value
                }?.let { extra ->
                    obj.piority = extra.priority
                }
                obj
            },
            loraPrompt = loraPrompts.map {
                val obj = it.toPrompt()
                promptExtraEntity.find { promptExtra ->
                    promptExtra.promptId == it.loraPromptId && promptExtra.promptType == PromptType.LoraPrompt.value
                }?.let { extra ->
                    obj.weight = extra.weight
                }
                obj
            },
            embeddingPrompt = embeddingPrompts.map {
                val obj = it.toPrompt()
                promptExtraEntity.find { promptExtra ->
                    promptExtra.promptId == it.embeddingId && promptExtra.promptType == PromptType.EmbeddingPrompt.value
                }?.let { extra ->
                    obj.piority = extra.priority
                }
                obj
            },
            steps = historyEntity.steps,
            samplerName = historyEntity.samplerName,
            sdModelCheckpoint = historyEntity.sdModelCheckpoint,
            width = historyEntity.width,
            height = historyEntity.height,
            batchSize = historyEntity.batchSize,
            cfgScale = historyEntity.cfgScale,
            time = historyEntity.time,
            imagePaths = imagePaths.map { it.toImageHistory() },
            hrParam = hrParamEntity?.toSaveHrParam() ?: SaveHrParam(
                enableScale = false,
                hrScale = 1.0f,
                hrDenosingStrength = 0.0f,
                hrUpscaler = "None",
            ),
            img2imgParam = img2imgParam?.toImg2imgParam(),
            controlNetParam = controlNetHistoryEntity?.toControlNetParam(),
            model = modelEntity,
            regionCount = historyEntity.regionCount,
            regionRatio = historyEntity.regionRatio,
            regionUseCommon = historyEntity.regionUseCommon,
            regionEnable = historyEntity.regionEnable
        )
        return result
    }
}

@Entity(tableName = "control_net_history")
data class ControlNetHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val controlNetHistoryId: Long = 0,
    val controlNetId: Long,
    val historyId: Long,
    val processorRes: Int,
    val thresholdA: Int,
    val thresholdB: Int,
    val guidanceStart: Float,
    val guidanceEnd: Float,
    val controlMode: Int,
    val weight: Float,
    val model: String,
) {
    fun toControlNetParam(): ControlNetParam {
        return ControlNetParam(
            enabled = true,
            processorRes = processorRes,
            thresholdA = thresholdA,
            thresholdB = thresholdB,
            guidanceStart = guidanceStart,
            guidanceEnd = guidanceEnd,
            controlMode = controlMode,
            weight = weight,
            model = model,
            historyId = historyId,
            controlNetId = controlNetId,
        )
    }
}

@Dao
interface ControlNetHistoryDao {
    @Insert
    fun insert(controlNetHistoryEntity: ControlNetHistoryEntity)

    @Update
    fun update(controlNetHistoryEntity: ControlNetHistoryEntity)

    @Query("SELECT * FROM control_net_history WHERE historyId = :historyId")
    fun getControlNetHistory(historyId: Long): ControlNetHistoryEntity?

    @Query("SELECT * FROM control_net_history WHERE controlNetId = :controlNetId")
    fun getControlNetHistoryWithControlNetByControlNetId(controlNetId: Long): ControlNetHistoryWithRelation?
}

data class ControlNetHistoryWithRelation(
    @Embedded val controlNetHistoryEntity: ControlNetHistoryEntity,
    @Relation(
        parentColumn = "controlNetId",
        entityColumn = "controlNetId",
    )
    val controlNetEntity: ControlNetEntity,
)


@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val historyId: Long = 0,
    val steps: Int,
    val samplerName: String,
    val sdModelCheckpoint: String,
    val width: Int,
    val height: Int,
    val batchSize: Int,
    val cfgScale: Float,
    val time: Long,
    var modelId: Long? = null,
    var regionRatio:String? = "",
    var regionCount:Int? = 0,
    var regionUseCommon:Boolean? = false,
    var regionEnable:Boolean? = false
) : Serializable {

    companion object {
        fun fromSaveHistory(saveHistory: SaveHistory): HistoryEntity {
            return HistoryEntity(
                steps = saveHistory.steps,
                samplerName = saveHistory.samplerName,
                sdModelCheckpoint = saveHistory.sdModelCheckpoint,
                width = saveHistory.width,
                height = saveHistory.height,
                batchSize = saveHistory.batchSize,
                cfgScale = saveHistory.cfgScale,
                time = saveHistory.time,
                regionCount = saveHistory.regionCount,
                regionRatio = saveHistory.regionRatio,
                regionUseCommon = saveHistory.regionUseCommon,
                regionEnable = saveHistory.regionEnable
            )
        }
    }
}

@Dao
interface HistoryDao {
    @Insert
    fun insert(historyEntity: HistoryEntity): Long

    @Transaction
    @Query("SELECT * FROM history ORDER BY historyId DESC")
    fun getAllHistory(): List<HistoryWithRelation>

    @Transaction
    @Query("SELECT * FROM history ORDER BY historyId DESC  limit 1")
    fun getLatestHistory(): HistoryWithRelation?

    @Transaction
    @Query("SELECT * FROM history where historyId = :id ORDER BY historyId DESC  limit 1")
    fun getHistoryById(id: Long): HistoryWithRelation?

    @Transaction
    @Query("SELECT * FROM history where historyId in (:ids) ORDER BY historyId DESC")
    fun getHistoryByIds(ids: List<Long>): List<HistoryWithRelation>
}

object HistoryStore {
    fun saveHistoryToDatabase(context: Context, history: SaveHistory) {
        val database = AppDatabaseHelper.getDatabase(context)
        val historyEntity = HistoryEntity.fromSaveHistory(history)
        with(history.sdModelCheckpoint) {
            // save model
            val sdModel = DrawViewModel.models.find { it.title == this }
            sdModel?.let {
                val modelEntity = ModelStore.getOrCreate(context, it.modelName)
                historyEntity.modelId = modelEntity.modelId
                modelEntity.let { saveModelEntity ->
                    history.imagePaths.firstOrNull()?.let { saveImageHistory ->
                        if (modelEntity.civitaiApiId != null) {
                            return@let
                        }
                        val previewPath = Util.saveModelPreviewToAppData(
                            context,
                            saveImageHistory.path,
                            modelEntity.name
                        )
                        ModelStore.update(
                            context,
                            saveModelEntity.copy(
                                coverPath = previewPath
                            )
                        )
                    }
                }
            }
        }
        val savedHistoryId = database.historyDao().insert(
            historyEntity
        )
        val savedPromptRelList = mutableListOf<SavePrompt>()
        history.prompt.forEach { prompt ->
            val promptEntity = database.promptDao().getPrompt(prompt.text)
            val promptId = if (promptEntity != null) {
                database.promptDao().update(promptEntity)
                promptEntity.promptId
            } else {
                database.promptDao().insert(SavePrompt.fromPrompt(prompt))
            }
            if (promptEntity == null) {
                return@forEach
            }
            if (savedPromptRelList.none { it.promptId == promptId }) {
                database.promptHistoryDao().insert(
                    PromptHistoryCrossRef(
                        promptId = promptId,
                        historyId = savedHistoryId
                    )
                )
                savedPromptRelList += promptEntity
            }
            database.promptExtraDao().insert(
                PromptExtraEntity(
                    promptId = promptId,
                    priority = prompt.piority,
                    historyId = savedHistoryId,
                    promptType = PromptType.Prompt.value,
                    regionIndex = prompt.regionIndex
                )
            )
        }

        history.negativePrompt.forEach { prompt ->
            val promptEntity = database.promptDao().getPrompt(prompt.text)
            val promptId = if (promptEntity != null) {
                database.promptDao().update(promptEntity)
                promptEntity.promptId
            } else {
                database.promptDao().insert(SavePrompt.fromPrompt(prompt))
            }


            database.negativePromptHistoryDao().getNegativePromptHistory(promptId, savedHistoryId)
                ?.let {
                    database.negativePromptHistoryDao().update(
                        it.copy(
                            promptId = promptId,
                            historyId = savedHistoryId
                        )
                    )
                } ?: database.negativePromptHistoryDao().insert(
                NegativePromptHistoryCrossRef(
                    promptId = promptId,
                    historyId = savedHistoryId
                )
            )
            database.promptExtraDao().insert(
                PromptExtraEntity(
                    promptId = promptId,
                    priority = prompt.piority,
                    historyId = savedHistoryId,
                    promptType = PromptType.NegativePrompt.value,
                )
            )
        }

        history.loraPrompt.forEach { prompt ->
            val promptEntity = PromptStore.getOrCreateLoraPromptByName(context, prompt.name)
            val promptId = promptEntity.loraPromptId

            database.loraPromptHistoryDao().insert(
                LoraPromptHistoryCrossRef(
                    loraPromptId = promptId,
                    historyId = savedHistoryId
                )
            )
            database.promptExtraDao().insert(
                PromptExtraEntity(
                    promptId = promptId,
                    priority = 0,
                    weight = prompt.weight,
                    historyId = savedHistoryId,
                    promptType = PromptType.LoraPrompt.value,
                )
            )

            prompt.prompts.forEach { loraPrompt ->
                val textPrompt = PromptStore.getOrCreatePromptByName(context, loraPrompt.text)
                val textPromptId = textPrompt.promptId
                database.promptExtraDao().insert(
                    PromptExtraEntity(
                        promptId = textPromptId,
                        priority = loraPrompt.piority,
                        historyId = savedHistoryId,
                        promptType = PromptType.LoraTrigger.value,
                        loraPromptId = promptId
                    )
                )

            }

            // save lora preview
            history.imagePaths.firstOrNull()?.let { imgHistory ->
                if (!promptEntity.lockPreview) {
                    val previewPath = Util.saveLoraPreviewToAppData(
                        context,
                        imgHistory.path,
                        promptId
                    )
                    database.loraPromptDao().update(
                        promptEntity.copy(
                            previewPath = previewPath
                        )
                    )
                }

            }
        }

        history.embeddingPrompt.forEach { prompt ->
            val promptEntity = database.embeddingDao().getPrompt(prompt.text)
            val promptId = promptEntity?.embeddingId ?: database.embeddingDao().insert(
                EmbeddingEntity.fromPrompt(prompt)
            )
            database.embeddingHistoryDao().insert(
                EmbeddingHistoryCrossRef(
                    embeddingId = promptId,
                    historyId = savedHistoryId
                )
            )
            database.promptExtraDao().insert(
                PromptExtraEntity(
                    promptId = promptId,
                    priority = prompt.piority,
                    historyId = savedHistoryId,
                    promptType = PromptType.LoraPrompt.value,
                )
            )
        }

        history.imagePaths.forEach { imageHistory ->
            database.imageHistoryDao().insert(
                ImageHistoryEntity.fromImageHistory(imageHistory, savedHistoryId)
            )
        }

        history.hrParam.let { hrParam ->
            database.hrHistoryDao().insert(
                HrHistoryEntity.fromHrParam(hrParam, savedHistoryId)
            )
        }
        history.img2imgParam?.let {
            database.img2ImgDao().insert(
                Img2ImgEntity(
                    denoisingStrength = it.denoisingStrength,
                    resizeMode = it.resizeMode,
                    scaleBy = it.scaleBy,
                    width = it.width,
                    height = it.height,
                    cfgScale = it.cfgScale,
                    path = it.path,
                    historyId = savedHistoryId,
                )
            )
        }
        history.controlNetParam?.let { controlNetParam ->
            val md5 = Util.getMd5FromImageBase64(controlNetParam.inputImage)
            val db = AppDatabaseHelper.getDatabase(context)
            val controlNetEntity = db.controlNetDao().getByMd5(md5)
            val savedControlNetEntity = controlNetEntity
                ?: db.controlNetDao().insert(
                    ControlNetEntity(
                        path = Util.saveControlNetToAppData(
                            context,
                            Uri.parse(controlNetParam.inputImagePath!!),
                        ),
                        md5 = md5,
                        time = System.currentTimeMillis()
                    )
                ).let {
                    db.controlNetDao().getById(it)
                }
            savedControlNetEntity?.let {
                val controlNetId = savedControlNetEntity.controlNetId
                history.imagePaths.firstOrNull()?.let { imgHistory ->
                    controlNetEntity?.let {
                        if (it.previewPath.isNotEmpty()) {
                            return@let
                        }
                        val previewPath = Util.saveControlNetPreviewToAppData(
                            context,
                            imgHistory.path,
                            md5
                        )
                        database.controlNetDao().update(
                            it.copy(
                                previewPath = previewPath
                            )
                        )
                    }

                }
                db.controlNetHistoryDao().insert(
                    ControlNetHistoryEntity(
                        controlNetId = controlNetId,
                        historyId = savedHistoryId,
                        processorRes = controlNetParam.processorRes,
                        thresholdA = controlNetParam.thresholdA,
                        thresholdB = controlNetParam.thresholdB,
                        guidanceStart = controlNetParam.guidanceStart,
                        guidanceEnd = controlNetParam.guidanceEnd,
                        controlMode = controlNetParam.controlMode,
                        weight = controlNetParam.weight,
                        model = controlNetParam.model,
                    )
                )
            }
        }

    }

    fun getAllHistory(context: Context): List<SaveHistory> {
        val database = AppDatabaseHelper.getDatabase(context)
        return database.historyDao().getAllHistory().map { it.toSaveHistory() }
    }

    fun getLatestHistory(context: Context): SaveHistory? {
        val database = AppDatabaseHelper.getDatabase(context)
        return database.historyDao().getLatestHistory()?.toSaveHistory()
    }

    fun getFavoriteImageHistory(context: Context): List<ImageHistory> {
        val database = AppDatabaseHelper.getDatabase(context)
        return database.imageHistoryDao().getFavouriteImageHistory().map { it.toImageHistory() }
    }

    fun getImageHistoryWithName(context: Context, name: String): ImageHistory? {
        val database = AppDatabaseHelper.getDatabase(context)
        return database.imageHistoryDao().getImageHistoryWithName(name)?.toImageHistory()
    }

    fun getHistoryById(context: Context, id: Long): SaveHistory? {
        val database = AppDatabaseHelper.getDatabase(context)
        val raw = database.historyDao().getHistoryById(id)
        val result = raw?.toSaveHistory()
        // load control net
        result?.controlNetParam?.let {
            val controlNet = database.controlNetHistoryDao()
                .getControlNetHistoryWithControlNetByControlNetId(it.controlNetId)
            result.controlNetParam = result.controlNetParam?.copy(
                inputImagePath = controlNet?.controlNetEntity?.path,
                inputImage = Util.readImageWithPathToBase64(
                    controlNet?.controlNetEntity?.path ?: ""
                )
            )
        }
        // load lora data
        result?.loraPrompt = raw?.loraPrompts?.map { loraPromptEntity: LoraPromptEntity ->
            val loraWithRelation =
                database.loraPromptDao().getPromptWithRelate(loraPromptEntity.loraPromptId)
            val loraPromptList = raw.promptExtraEntity.filter {
                it.promptType == PromptType.LoraTrigger.value && it.loraPromptId == loraPromptEntity.loraPromptId
            }.mapNotNull {
                loraWithRelation?.triggerText?.find { triggerText ->
                    triggerText.promptId == it.promptId
                }
            }
            val promptObj = loraPromptEntity.toPrompt().copy(
                triggerText = loraWithRelation?.triggerText?.map { it.toPrompt() } ?: emptyList(),
                prompts = loraPromptList.map { it.toPrompt() },
            )
            promptObj
        } ?: emptyList()
        return result
    }

    fun findLatestControlNetUse(context: Context, controlNetId: Long): ControlNetHistoryEntity? {
        val database = AppDatabaseHelper.getDatabase(context)
        val result = database.controlNetHistoryDao()
            .getControlNetHistoryWithControlNetByControlNetId(controlNetId)
        return result?.controlNetHistoryEntity
    }

}
