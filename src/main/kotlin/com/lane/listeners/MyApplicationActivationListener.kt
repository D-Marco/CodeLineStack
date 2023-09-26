package com.lane.listeners

import com.intellij.openapi.application.ApplicationActivationListener
import com.intellij.openapi.wm.IdeFrame


internal class MyApplicationActivationListener : ApplicationActivationListener {

    override fun applicationActivated(ideFrame: IdeFrame) {
//        VirtualFileManager.VFS_CHANGES.subscribe(null, object : BulkFileListener {
//
//
//        })
    }
}
