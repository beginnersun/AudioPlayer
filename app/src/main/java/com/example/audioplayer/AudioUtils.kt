
@file:JvmName("AudioUtilsKt")
@file:JvmMultifileClass

package com.example.audioplayer

import android.content.Context
import android.os.Environment
import com.reinhard.wcvcodec.WcvCodec
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStream

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
 * @param context 此处的context最好使用applicationContext
 */
fun changeAmrToMp3AndMerge(context: Context,sourcePaths:MutableList<String>,mergeMp3Path:String):Boolean{
    if (sourcePaths.size == 0) return false
    val tmPcmList = mutableListOf<String>()
    for (source in sourcePaths){
        val tmpPcm = getTmpPcmPath(context)
        val targetMp3 = getTargetPath(context)
        val success = changeAmrToMp3(source,tmpPcm,targetMp3)
        if (!success){
            return false
        }
        tmPcmList.add(tmpPcm)
    }
    val resultTmpPcm = "${Environment.getExternalStorageDirectory()}${File.separator}mediaCode${File.separator}ccffccffccff.pcm"
    mergePcmFiles(tmPcmList,resultTmpPcm)
    val tmpAmrPath = getTmpAmrPath(context)
    val pcmToAmr = changePcmToAmr(resultTmpPcm, tmpAmrPath)
    if (!pcmToAmr){
        return false
    }
    return changeAmrToMp3(tmpAmrPath, getTmpPcmPath(context), mergeMp3Path)
}

fun changeAmrToMp3(sourcePath:String,context: Context):Boolean{
    return changeAmrToMp3(sourcePath, getTmpPcmPath(context), getTargetPath(context))
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
            writeByteToOutPutStream(outPutStream,file)
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