package io.github.juby210.acplugins

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.View
import androidx.cardview.widget.CardView
import androidx.core.widget.NestedScrollView
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.api.rn.user.RNUserProfile
import com.aliucord.entities.Plugin
import com.aliucord.patcher.after
import com.discord.stores.StoreStream
import com.discord.widgets.user.usersheet.WidgetUserSheet
import com.discord.widgets.user.usersheet.WidgetUserSheetViewModel

@Suppress("unused")
@AliucordPlugin
class NewProfiles : Plugin() {
    override fun start(ctx: Context?) {
        val userSettings = StoreStream.getUserSettingsSystem()
        patcher.after<WidgetUserSheet>("configureDeveloperSection", WidgetUserSheetViewModel.ViewState.Loaded::class.java) {
            val model = it.args[0] as WidgetUserSheetViewModel.ViewState.Loaded
            val profile = model.userProfile
            if (profile is RNUserProfile) {
                val themeColors = profile.guildMemberProfile?.run { themeColors ?: accentColor?.let { c -> intArrayOf(c, c) } }
                    ?: profile.userProfile.run { themeColors ?: accentColor?.let { c -> intArrayOf(c, c) } } ?: return@after
                val binding = WidgetUserSheet.`access$getBinding$p`(this)
                val actionsContainer = binding.D
                val root = actionsContainer.parent.parent.parent as NestedScrollView
                val alpha = 0x50000000

                // make non-transparent containers transparent or opaque
                actionsContainer.setBackgroundColor(0)
                binding.J.apply { // header
                    setBackgroundColor(0)
                    (parent as View).setBackgroundColor(0)
                }
                if (userSettings.theme != "light") {
                    listOf(binding.b, binding.R, binding.j).forEach { view -> // about me, voice settings, and admin actions
                        view.setCardBackgroundColor(alpha)
                    }
                    listOf(binding.h, binding.I).forEach { view -> // activity and edit profile buttons
                        view.setBackgroundColor(0)
                    }
                    binding.n.apply { // connections
                        setBackgroundColor(0x20000000)
                        (parent as CardView).setCardBackgroundColor(0x20000000)
                    }
                    binding.B.apply { // note
                        boxBackgroundColor = alpha
                        (parent as CardView).setCardBackgroundColor(alpha)
                    }
                    binding.A.setBackgroundColor(0) // note
                }

                val colors = intArrayOf(alpha + themeColors[0], alpha + themeColors[0], alpha + themeColors[1])
                root.background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors)
            }
        }
    }

    override fun stop(ctx: Context?) = patcher.unpatchAll()
}
