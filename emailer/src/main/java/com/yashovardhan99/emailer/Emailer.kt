package com.yashovardhan99.emailer

import android.content.ClipData
import android.content.ClipDescription
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri

/**
 * Query a list of package names of available email clients
 */
private fun ContextWrapper.getEmailClients(): List<String> {
    val emailFilterIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"))
    return packageManager.queryIntentActivities(emailFilterIntent, 0)
        .map { it.activityInfo.packageName }
}

private fun buildClipData(
    data: List<Uri>,
    label: CharSequence,
    mimeTypes: Array<String>
): ClipData {
    val clipDescription = ClipDescription(label, mimeTypes)
    val clipItems = data.map { ClipData.Item(it) }
    return ClipData(clipDescription, clipItems.first()).apply {
        clipItems.drop(1).forEach { addItem(it) }
    }
}

data class EmailAttachments(
    val list: List<Uri>,
    val mimeTypes: List<String>,
    val label: CharSequence
) {
    fun toClipData() = buildClipData(list, label, mimeTypes.toTypedArray())
}

class EmailIntentBuilder {
    var chooserTitle: CharSequence = "Send email"
    var receipients = listOf<String>()
    var cc = listOf<String>()
    var bcc = listOf<String>()
    var subject: String? = null
    var emailMessage: String? = null
    var attachments: EmailAttachments? = null
    var mimeType: String? = null
    var grantUriPermission = false
}

private fun EmailIntentBuilder.buildIntent(): Intent {
    require(!subject.isNullOrBlank()) { "Email subject must be set" }
    require(!emailMessage.isNullOrBlank()) { "Email message must be set" }
    return Intent().apply {
        if (receipients.isNotEmpty()) putExtra(Intent.EXTRA_EMAIL, receipients.toTypedArray())
        if (cc.isNotEmpty()) putExtra(Intent.EXTRA_CC, cc.toTypedArray())
        if (bcc.isNotEmpty()) putExtra(Intent.EXTRA_BCC, bcc.toTypedArray())
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, emailMessage)
        if (mimeType != null) type = mimeType
        if (grantUriPermission) flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        if (attachments == null) {
            action = Intent.ACTION_SENDTO
            data = if (receipients.isNotEmpty())
                Uri.fromParts("mailto", receipients.first(), null)
            else
                Uri.parse("mailto:")
        }
        attachments?.let {
            clipData = it.toClipData()
            action = Intent.ACTION_SEND_MULTIPLE
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(it.list))
        }
    }
}

private fun ContextWrapper.getFinalIntent(emailIntent: Intent, chooserTitle: CharSequence): Intent {
    if (emailIntent.action == Intent.ACTION_SENDTO) {
        return Intent.createChooser(emailIntent, chooserTitle)
    }
    val shareTargets =
        packageManager.queryIntentActivities(emailIntent, 0).map { it.activityInfo.packageName }
    val emailClients = getEmailClients()
    val targetedIntents = shareTargets.filter { target ->
        emailClients.any { target == it }
    }.map { Intent(emailIntent).apply { `package` = it } }
    val finalIntent = Intent.createChooser(targetedIntents.first(), chooserTitle)
    finalIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedIntents.toTypedArray())
    return finalIntent
}

private fun ContextWrapper.buildEmailIntent(build: EmailIntentBuilder.() -> Unit): Intent {
    val emailIntentBuilder = EmailIntentBuilder()
    emailIntentBuilder.build()
    val emailIntent = emailIntentBuilder.buildIntent()
    return getFinalIntent(emailIntent, emailIntentBuilder.chooserTitle)
}

fun ContextWrapper.sendEmail(build: EmailIntentBuilder.() -> Unit) {
    startActivity(buildEmailIntent(build))
}
