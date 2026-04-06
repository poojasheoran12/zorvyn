package com.example.zorvyn.util

import platform.UIKit.*
import platform.Foundation.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class IosFileExporter : FileExporter {
    override suspend fun saveAndShare(fileName: String, content: String) = withContext(Dispatchers.Main) {
        val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController ?: return@withContext
        
        val paths = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, true)
        val cacheDirectory = paths.first() as String
        val filePath = (cacheDirectory as NSString).stringByAppendingPathComponent(fileName)
        val fileURL = NSURL.fileURLWithPath(filePath)
        
        (content as NSString).writeToURL(
            url = fileURL,
            atomically = true,
            encoding = NSUTF8StringEncoding,
            error = null
        )
        
        val activityViewController = UIActivityViewController(listOf(fileURL), null)
        
        // Prepare for iPad
        activityViewController.popoverPresentationController()?.sourceView = rootViewController.view
        
        rootViewController.presentViewController(activityViewController, animated = true, completion = null)
    }
}
