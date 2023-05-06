package io.github.juby210.acplugins

import android.content.Context
import android.view.View
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.Hook
import com.aliucord.patcher.PreHook
import com.discord.databinding.VoiceUserListItemUserBinding
import com.discord.stores.StoreApplicationStreamPreviews
import com.discord.utilities.mg_recycler.MGRecyclerDataPayload
import com.discord.views.StreamPreviewView
import com.discord.widgets.media.WidgetMedia
import com.discord.widgets.user.presence.ViewHolderStreamRichPresence
import com.discord.widgets.user.presence.`ViewHolderStreamRichPresence$setOnStreamPreviewClicked$1`
import com.discord.widgets.user.usersheet.WidgetUserSheetViewModel
import com.discord.widgets.user.usersheet.`WidgetUserSheet$configureUI$3`
import com.discord.widgets.voice.sheet.CallParticipantsAdapter
import com.discord.widgets.voice.sheet.`CallParticipantsAdapter$ViewHolderUser$onConfigure$4`
import java.lang.reflect.Method


val launchMethod: Method = WidgetMedia.Companion::class.java.declaredMethods.first {
    it.name == "launch" && it.parameterTypes.size == 8
}.apply { isAccessible = true }

fun getPreviewUrl(preview: StoreApplicationStreamPreviews.StreamPreview) =
    if (preview is StoreApplicationStreamPreviews.StreamPreview.Resolved) preview.url else null

@AliucordPlugin
class ViewStreamPreview : Plugin() {
    class OpenPreview(private val url: String?, private val name: String?) : View.OnClickListener {
        override fun onClick(v: View) {
            launchMethod(WidgetMedia.Companion, v.context, "$name's stream.jpg", null, null, url, 0, 0, null)
        }
    }

    override fun start(ctx: Context?) {
        val c1 = ViewHolderStreamRichPresence::class.java
        val streamPreviewField = c1.getDeclaredField("streamPreview").apply { isAccessible = true }
        patcher.patch(c1, "setOnStreamPreviewClicked", arrayOf(Function0::class.java), PreHook { param ->
            val joinStream = param.args[0] as `WidgetUserSheet$configureUI$3`
            with(joinStream.`$model` as WidgetUserSheetViewModel.ViewState.Loaded) {
                val url = getPreviewUrl(streamContext.preview) ?: return@PreHook
                with(streamPreviewField[param.thisObject] as StreamPreviewView) {
                    setOnClickListener(OpenPreview(url, user.username))
                    j.c.setOnClickListener(`ViewHolderStreamRichPresence$setOnStreamPreviewClicked$1`(joinStream))
                    param.result = null
                }
            }
        })

        val c2 = CallParticipantsAdapter.ViewHolderUser::class.java
        val binding = c2.getDeclaredField("binding").apply { isAccessible = true }
        patcher.patch(c2, "onConfigure", arrayOf(Int::class.java, MGRecyclerDataPayload::class.java), Hook {
            (binding[it.thisObject] as VoiceUserListItemUserBinding).i.run {
                if (visibility != View.VISIBLE) return@Hook
                val participant = (it.args[1] as CallParticipantsAdapter.ListItem.VoiceUser).participant
                val preview = participant.streamContext?.preview ?: return@Hook

                val url = getPreviewUrl(preview) ?: return@Hook
                setOnClickListener(OpenPreview(url, participant.user.username))
                j.c.setOnClickListener(`CallParticipantsAdapter$ViewHolderUser$onConfigure$4`(
                    it.thisObject as CallParticipantsAdapter.ViewHolderUser, participant
                ))
            }
        })
    }

    override fun stop(ctx: Context?) = patcher.unpatchAll()
}
