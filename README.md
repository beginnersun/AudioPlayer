# AudioPlayer

```implementation 'com.github.beginnersun:AudioPlayer:1.0.0'```
```Kotlin
   private val scannerCallback = object : DiscoverCallback(){
       override fun onReceived(file: File,userCode:String) {
           Log.e("找到amr","${file.absolutePath}")
       }
       override fun onError(error: String) {
       }
   }
   WeChatScannerImpl().discoverUsersVoice(this@MainActivity,scannerCallback)
```
