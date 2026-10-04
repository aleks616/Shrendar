package com.example.client

import platform.UIKit.UIDevice

class IOSPlatform:Platform {
    override val name:String=UIDevice.currentDevice.systemName()+" "+UIDevice.currentDevice.systemVersion
}

actual fun getPlatform():Platform=IOSPlatform()

actual val BASE_URL:String="https://shrendar.shares.zrok.io/api"