package io.watters.keystats

import platform.AppKit.*
import platform.Foundation.NSSelectorFromString

fun main() {
    NSApplication.sharedApplication

    NSApp!!.delegate = AppDelegate()

    val menubar = NSMenu()
    val appMenuItem = NSMenuItem()
    menubar.addItem(appMenuItem)
    NSApp!!.mainMenu = menubar

    val appMenu = NSMenu()
    val quitMenuItem =
        NSMenuItem(
            title = "Quit",
            action = NSSelectorFromString("terminate:"),
            keyEquivalent = "q"
        )
    appMenu.addItem(quitMenuItem)
    appMenuItem.setSubmenu(appMenu)

    NSApp!!.setActivationPolicy(NSApplicationActivationPolicy.NSApplicationActivationPolicyRegular)
    NSApp!!.run()
}
