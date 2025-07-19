package com.module.notelycompose.platform

import com.module.notelycompose.core.debugPrintln
import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle
import platform.UIKit.UINotificationFeedbackGenerator
import platform.UIKit.UINotificationFeedbackType
import platform.UIKit.UISelectionFeedbackGenerator

actual class HapticFeedback {
    
    actual fun light() {
        performImpactFeedback(UIImpactFeedbackStyle.UIImpactFeedbackStyleLight)
    }

    actual fun medium() {
        performImpactFeedback(UIImpactFeedbackStyle.UIImpactFeedbackStyleMedium)
    }

    actual fun heavy() {
        performImpactFeedback(UIImpactFeedbackStyle.UIImpactFeedbackStyleHeavy)
    }

    actual fun success() {
        performNotificationFeedback(UINotificationFeedbackType.UINotificationFeedbackTypeSuccess)
    }

    actual fun error() {
        performNotificationFeedback(UINotificationFeedbackType.UINotificationFeedbackTypeError)
    }

    private fun performImpactFeedback(style: UIImpactFeedbackStyle) {
        try {
            val impactGenerator = UIImpactFeedbackGenerator(style)
            impactGenerator.prepare()
            impactGenerator.impactOccurred()
            debugPrintln{"iOS haptic feedback: impact style $style"}
        } catch (e: Exception) {
            debugPrintln{"Failed to perform iOS impact haptic feedback: ${e.message}"}
        }
    }

    private fun performNotificationFeedback(type: UINotificationFeedbackType) {
        try {
            val notificationGenerator = UINotificationFeedbackGenerator()
            notificationGenerator.prepare()
            notificationGenerator.notificationOccurred(type)
            debugPrintln{"iOS haptic feedback: notification type $type"}
        } catch (e: Exception) {
            debugPrintln{"Failed to perform iOS notification haptic feedback: ${e.message}"}
        }
    }

    fun performSelectionFeedback() {
        try {
            val selectionGenerator = UISelectionFeedbackGenerator()
            selectionGenerator.prepare()
            selectionGenerator.selectionChanged()
            debugPrintln{"iOS haptic feedback: selection changed"}
        } catch (e: Exception) {
            debugPrintln{"Failed to perform iOS selection haptic feedback: ${e.message}"}
        }
    }
}