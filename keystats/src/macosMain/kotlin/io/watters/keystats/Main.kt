package io.watters.keystats

import kotlinx.cinterop.*
import platform.CoreFoundation.*
import platform.CoreGraphics.*
import platform.darwin.UniCharCountVar
import platform.darwin.UniCharVar
import platform.posix.exit
import kotlin.experimental.ExperimentalNativeApi

fun main() {
    /* https://developer.apple.com/documentation/coregraphics/quartz_event_services */

    // https://developer.apple.com/documentation/coregraphics/cgeventmask
    // TODO: figure out how to generate the correct mask for just the events we want
    // val eventMask = kCGEventKeyDown or kCGEventFlagsChanged
    val eventMask = kCGEventMaskForAllEvents

    val stats = ActionStats()

    // https://developer.apple.com/documentation/coregraphics/1454426-cgeventtapcreate
    val eventTap = CGEventTapCreate(
        kCGSessionEventTap,
        kCGHeadInsertEventTap,
        0.convert(),
        eventMask.convert(),
        staticCFunction(::callback),
        StableRef.create(stats).asCPointer()
    )

    if (eventTap == null) {
        println("Event tap not created!")
        exit(1)
    }

    // https://developer.apple.com/documentation/corefoundation/1400928-cfmachportcreaterunloopsource
    val source = CFMachPortCreateRunLoopSource(
        kCFAllocatorDefault, eventTap, 0
    )

    // https://developer.apple.com/documentation/corefoundation/1543356-cfrunloopaddsource
    CFRunLoopAddSource(CFRunLoopGetCurrent(), source, kCFRunLoopCommonModes)
    CGEventTapEnable(eventTap, true)

    CFRunLoopRun()
}

// https://developer.apple.com/documentation/coregraphics/cgeventtapcallback
@OptIn(ExperimentalNativeApi::class)
@Suppress("UNUSED_PARAMETER")
fun callback(
    proxy: CGEventTapProxy?,
    type: CGEventType,
    event: CGEventRef?,
    refcon: COpaquePointer?,
): CGEventRef? {
    if (type == kCGEventKeyDown) {

        val flags = CGEventGetFlags(event)

        val keyCode =
            CGEventGetIntegerValueField(event, kCGKeyboardEventKeycode)
        val keyboardType =
            CGEventGetIntegerValueField(event, kCGKeyboardEventKeyboardType)

        val stats: ActionStats? = refcon?.asStableRef<ActionStats>()?.get()

        val action = memScoped {
            val actualStringLength = alloc<UniCharCountVar>()
            val unicodeString = alloc<UniCharVar>()

            CGEventKeyboardGetUnicodeString(
                event = event,
                maxStringLength = 4.convert(),
                actualStringLength = actualStringLength.ptr,
                unicodeString = unicodeString.ptr,
            )
            KeyboardAction(
                keyCode = keyCode,
                keyboardType = keyboardType,
                modifiers = flags,
                unicodeCodePoint = unicodeString.value
            )
        }
        stats?.recordAction(action)
    }

    return event
}

@OptIn(ExperimentalNativeApi::class)
private data class KeyboardAction(
    val keyCode: Long,
    val keyboardType: Long,
    val modifiers: ULong,
    val unicodeCodePoint: UShort,
) {
    fun getUnicodeString(): String {
        return Char.toChars(unicodeCodePoint.convert())
            .concatToString()
    }
}

private data class ActionStats(
    private val map: MutableMap<KeyboardAction, Int> = mutableMapOf(),
) {
    fun recordAction(action: KeyboardAction) {
        map[action] = (map[action] ?: 0) + 1
        print()
    }

    private fun print() {
        println("---")
        map.forEach { println(it) }
        println("---")
    }
}
