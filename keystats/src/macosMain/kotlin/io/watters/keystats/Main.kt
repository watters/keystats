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
    println("eventMask: $eventMask (${eventMask.convert<ULong>()})")

    // https://developer.apple.com/documentation/coregraphics/1454426-cgeventtapcreate
    val eventTap = CGEventTapCreate(
        kCGSessionEventTap,
        kCGHeadInsertEventTap,
        0.convert(),
        eventMask.convert(),
        staticCFunction(::callback),
        null
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
fun callback(
    proxy: CGEventTapProxy?,
    type: CGEventType,
    event: CGEventRef?,
    refcon: COpaquePointer?,
): CGEventRef? {
    if (type == kCGEventKeyDown || type == kCGEventFlagsChanged) {
        println("eventType: $type")

        val flags = CGEventGetFlags(event)

        val keyCode =
            CGEventGetIntegerValueField(event, kCGKeyboardEventKeycode)
        val keyboardType =
            CGEventGetIntegerValueField(event, kCGKeyboardEventKeyboardType)

        memScoped {
            var actualStringLength = alloc<UniCharCountVar>()
            var unicodeString = alloc<UniCharVar>()

            CGEventKeyboardGetUnicodeString(
                event = event,
                maxStringLength = 4.convert(),
                actualStringLength = actualStringLength.ptr,
                unicodeString = unicodeString.ptr,
            )
            val action = KeyboardAction(
                keyCode = keyCode,
                keyboardType = keyboardType,
                modifiers = flags,
                unicodeString = Char.toChars(unicodeString.value.convert())
                    .concatToString()
            )
            println(action)
        }
    }

    return event
}

private data class KeyboardAction(
    val keyCode: Long,
    val keyboardType: Long,
    val modifiers: ULong,
    val unicodeString: String? = null,
)
