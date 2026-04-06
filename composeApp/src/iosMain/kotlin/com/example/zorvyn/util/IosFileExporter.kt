package com.example.zorvyn.util

import platform.UIKit.*
import platform.Foundation.*
import platform.QuickLook.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class IosFileExporter : FileExporter, QLPreviewControllerDataSourceProtocol, QLPreviewControllerDelegateProtocol {
    private var fileURLToPreview: NSURL? = null

    override suspend fun saveAndShare(fileName: String, content: String) = withContext(Dispatchers.Main) {
        val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController ?: return@withContext
        
        val paths = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, true)
        val cacheDirectory = paths.first() as String
        val filePath = (cacheDirectory as String).let { (it as NSString).stringByAppendingPathComponent(fileName) }
        val fileURL = NSURL.fileURLWithPath(filePath)
        
        content.let { (it as NSString).writeToURL(
            url = fileURL,
            atomically = true,
            encoding = NSUTF8StringEncoding,
            error = null
        )}
        
        fileURLToPreview = fileURL
        
        val previewController = QLPreviewController()
        previewController.dataSource = this@IosFileExporter
        previewController.delegate = this@IosFileExporter
        
        rootViewController.presentViewController(previewController, animated = true, completion = null)
    }

    override fun numberOfPreviewItemsInPreviewController(controller: QLPreviewController): Long = 1L

    override fun previewController(controller: QLPreviewController, previewItemAtIndex: Long): QLPreviewItemProtocol? {
        return fileURLToPreview as? QLPreviewItemProtocol
    }
}
