package com.example.android_practice.profile.presentation.platform

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.android_practice.profile.domain.model.ResumeDocument
import com.example.android_practice.profile.presentation.view.ResumeUiError
import java.io.File

internal class ResumeDocumentOpener(private val context: Context) {
    fun open(document: ResumeDocument): ResumeUiError? {
        return try {
            val file = File(document.localPath)
            if (!file.isFile) return ResumeUiError.STORAGE
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
            context.startActivity(Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                clipData = ClipData.newRawUri("resume", uri)
            })
            null
        } catch (_: ActivityNotFoundException) {
            ResumeUiError.NO_VIEWER
        } catch (_: SecurityException) {
            ResumeUiError.STORAGE
        } catch (_: IllegalArgumentException) {
            ResumeUiError.STORAGE
        }
    }
}
