
@file:JvmName("AudioUtilsKt")
@file:JvmMultifileClass

package com.example.audioplayer.util

import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.Environment
import com.example.audioplayer.VoiceConfig
import com.reinhard.wcvcodec.WcvCodec
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStream

const val AUDIO_MP3_TYPE = "mp3"
const val AUDIO_PCM_TYPE = "pcm"

/**
 * amr格式转mp3
 */
fun changeAmrToMp3(sourcePath:String,tmpPcmPath:String,targetMp3Path:String):Boolean{
    if (!sourcePath.toLowerCase().endsWith(".amr")){
        return false
    }
    if (File(tmpPcmPath).exists()){
        File(tmpPcmPath).delete()
    }
    if (File(targetMp3Path).exists()){
        File(targetMp3Path).delete()
    }
    return WcvCodec.decode(sourcePath,tmpPcmPath,targetMp3Path) == 0
}
/**
 * 获取音频文件播放时长
 * 大于0小于1 是为了防止因为时间过短而被忽略
 */
fun getMediaDuration(path:String):Int{
    val extractor = MediaExtractor()
    try {
        extractor.setDataSource(path)
    }catch (t:Throwable){
        return 0
    }
    val format = extractor.getTrackFormat(0)
    val time = format.getLong(MediaFormat.KEY_DURATION)*1f/1000/1000
    return if (time >0 && time <1){
        1
    }else{
        time.toInt()
    }
}
/**
 * 合并pcm并且转换为mp3
 */
fun mergePcmToMp3(fileList:MutableList<String>,outPutPath:String):Boolean{
    return mergePcmToMp3(
        fileList,
        getExternalPath(AUDIO_PCM_TYPE),
        outPutPath
    )
}
/**
 * 合并pcm并且转换为mp3 pcm自己提供
 */
fun mergePcmToMp3(fileList:MutableList<String>,outPcmPath: String,outPutPath:String):Boolean{
    if (fileList.isNullOrEmpty()){
        return false
    }
    mergePcmFiles(fileList, outPcmPath)
    val amrPath = getExternalPath("amr")
    val pcmToAmr =
        changePcmToAmr(outPcmPath, amrPath)
    if (!pcmToAmr){
        return false
    }
    return changeAmrToMp3(
        amrPath,
        getExternalPath(AUDIO_PCM_TYPE),
        outPutPath
    )
}
/**
 * @param context 此处的context最好使用applicationContext
 */
fun changeAmrToMp3AndMerge(context: Context,sourcePaths:MutableList<String>,mergeMp3Path:String):Boolean{
    if (sourcePaths.size == 0) return false
    val tmPcmList = mutableListOf<String>()
    for (source in sourcePaths){
        val tmpPcm = getTmpPcmPath(context)
        val targetMp3 = getTargetPath(context)
        val success = changeAmrToMp3(
            source,
            tmpPcm,
            targetMp3
        )
        if (!success){
            return false
        }
        tmPcmList.add(tmpPcm)
    }
    val resultTmpPcm = "${Environment.getExternalStorageDirectory()}${File.separator}mediaCode${File.separator}ccffccffccff.pcm"
    mergePcmFiles(tmPcmList, resultTmpPcm)
    val tmpAmrPath = getTmpAmrPath(context)
    val pcmToAmr =
        changePcmToAmr(resultTmpPcm, tmpAmrPath)
    if (!pcmToAmr){
        return false
    }
    return changeAmrToMp3(
        tmpAmrPath,
        getTmpPcmPath(context),
        mergeMp3Path
    )
}

fun changeAmrToMp3(sourcePath:String,context: Context):Boolean{
    return changeAmrToMp3(
        sourcePath,
        getTmpPcmPath(context),
        getTargetPath(context)
    )
}

private fun changePcmToAmr(pcmPath:String,amrPath:String):Boolean{
    val success = WcvCodec.encode(pcmPath,amrPath)
    return success == 0
}


private fun reCreateFile(path:String):File{
    val targetFile = File(path)
    if (targetFile.exists()){
        targetFile.delete()
    }
    targetFile.createNewFile()
    return targetFile
}
private fun mergePcmFiles(fileList:MutableList<String>,outPutPath:String){
    val targetFile = reCreateFile(outPutPath)
    val outPutStream = FileOutputStream(targetFile)
    for (file in fileList){
        if (File(file).exists()){
            writeByteToOutPutStream(
                outPutStream,
                file
            )
        }
    }
    outPutStream.flush()
    outPutStream.close()
}
private fun mergeInputStream(path1:String,path2:String,targetPath:String){
    val file1 = File(path1)
    val file2 = File(path2)
    val targetFile = File(targetPath)
    if (!file1.exists() || !file2.exists()){
        return
    }
    if (targetFile.exists()){
        targetFile.delete()
    }
    targetFile.createNewFile()
    val inputStream1 = FileInputStream(file1)
    val inputStream2 = FileInputStream(file2)
    val outputString = FileOutputStream(targetFile,true)
    val bytes = ByteArray(1024)
    var len = 0
    len = inputStream1.read(bytes)
    while (len != -1){
        outputString.write(bytes,0,len)
        len = inputStream1.read(bytes)
    }
    inputStream1.close()
    len = inputStream2.read(bytes)
    while (len != -1){
        outputString.write(bytes,0,len)
        len = inputStream2.read(bytes)
    }
    inputStream2.close()
    outputString.flush()
    outputString.close()
}
private fun writeByteToOutPutStream(outPutStream:OutputStream,path:String){
    val inputStream1 = FileInputStream(path)
    val bytes = ByteArray(1024)
    var len = 0
    len = inputStream1.read(bytes)
    while (len != -1){
        outPutStream.write(bytes,0,len)
        len = inputStream1.read(bytes)
    }
    inputStream1.close()
}
private fun getTmpPcmPath(context:Context):String{
    val file = File("${context.externalCacheDir.absolutePath}${File.separator}pcm")
    if (!file.exists()){
        file.mkdir()
    }
    return "${file.absolutePath}${File.separator}${System.currentTimeMillis()}tmp.pcm"
}
private fun getTmpAmrPath(context:Context):String{
    val file = File("${context.externalCacheDir.absolutePath}${File.separator}pcm")
    if (!file.exists()){
        file.mkdir()
    }
    return "${file.absolutePath}${File.separator}${System.currentTimeMillis()}tmp.amr"
}
private fun getTargetPath(context:Context):String{
    val file = File("${context.externalCacheDir.absolutePath}${File.separator}voice")
    if (!file.exists()){
        file.mkdir()
    }
    return "${file.absolutePath}${File.separator}t${System.currentTimeMillis()}.mp3"
}
fun getExternalPath(type:String) =
    when(type){
        "pcm" -> "${getExternalDir(type)}${File.separator}${System.currentTimeMillis()}tmp.pcm"
        "mp3" -> "${getExternalDir(type)}${File.separator}${System.currentTimeMillis()}tmp.mp3"
        "amr" -> "${getExternalDir(type)}${File.separator}${System.currentTimeMillis()}tmp.amr"
        else -> "${getExternalDir(type)}${File.separator}${System.currentTimeMillis()}tmp.other"
    }
private fun getDirPathOrCreate(path:String):String =
    if (File(path).exists()){
        path
    }else{
        File(path).mkdirs()
        path
    }
fun getExternalDir(type:String) =
    when(type){
        "pcm" -> getDirPathOrCreate("${VoiceConfig.instance.voiceSavePath}pcm")
        "mp3" -> getDirPathOrCreate("${VoiceConfig.instance.voiceSavePath}mp3")
        "amr" -> getDirPathOrCreate("${VoiceConfig.instance.voiceSavePath}amr")
        else -> getDirPathOrCreate("${VoiceConfig.instance.voiceSavePath}")
    }
