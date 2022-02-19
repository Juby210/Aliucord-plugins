/*
 * Copyright (c) 2021-2022 Juby210
 * Licensed under the Open Software License version 3.0
 */

package io.github.juby210.acplugins.bsi

import android.content.Context
import android.text.*
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.aliucord.Constants
import com.aliucord.Utils
import com.aliucord.fragments.SettingsPage
import com.aliucord.utils.DimenUtils
import com.aliucord.views.Divider
import com.aliucord.views.TextInput
import com.discord.views.CheckedSetting
import com.lytefast.flexinput.R
import io.github.juby210.acplugins.BetterStatusIndicators

class SimpleTextWatcher(private val after: (Editable?) -> Unit) : TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable?) = after(s)
}

class PluginSettings(private val plugin: BetterStatusIndicators) : SettingsPage() {
    private var avatarStatus: CheckedSetting? = null
    private var filledColors: CheckedSetting? = null
    private var radialStatusSwitches: LinearLayout? = null

    override fun onViewBound(view: View) {
        super.onViewBound(view)

        setActionBarTitle("BetterStatusIndicators")
        setPadding(0)

        val context = view.context
        linearLayout.apply {
            addView(createSwitch(
                context,
                "avatarStatus",
                "Avatar Status",
                "Show first platform on avatar.",
                default = true
            ) {
                if (it) {
                    plugin.settings.setBool("filledColors", false)
                    filledColors?.isChecked = false
                }
                plugin.patchStatusView(resources)
            }.also { avatarStatus = it })
            addView(createSwitch(
                context,
                "filledColors",
                "Filled Colors",
                "Use filled circles for avatar status. Incompatible with Avatar Status setting.",
            ) {
                if (it) {
                    plugin.settings.setBool("avatarStatus", false)
                    avatarStatus?.isChecked = false
                }
                plugin.patchStatusView(resources)
            }.also { filledColors = it })
            addView(createSwitch(
                context,
                "chatStatus",
                "Chat Status",
                "Show little status circles in chat next to the username.",
                true
            ) { plugin.patchChatStatus() })
            addView(createSwitch(
                context,
                "chatStatusPlatforms",
                "Chat Status Platforms",
                "Show platforms in chat next to the username."
            ) { plugin.patchChatStatusPlatforms() })
            // addView(createSwitch(
            //     context,
            //     "voiceStatus",
            //     "Voice Status",
            //     "Shows the status ring around the speaking user based on their status instead of only green.",
            //     true
            // ))

            addView(Divider(context))
            addView(createHeader(context, "Radial Status"))
            addView(createSwitch(
                context,
                "radialStatus",
                "Enable Radial Status",
                "Shows a status ring around the user avatar."
            ) {
                plugin.patchRadialStatus(it, isPluginEnabled("SquareAvatars"))
                Utils.promptRestart()
                radialStatusSwitches?.visibility = if (it) View.VISIBLE else View.GONE
            })
            addView(com.aliucord.widgets.LinearLayout(context).apply {
                addView(createSwitch(
                    context,
                    "radialStatusDMs",
                    "DMs List",
                    default = true
                ))
                addView(createSwitch(
                    context,
                    "radialStatusFriendsList",
                    "Friends List",
                    default = true
                ))
                addView(createSwitch(
                    context,
                    "radialStatusMembersList",
                    "Members List",
                    default = true
                ))
                addView(createSwitch(
                    context,
                    "radialStatusUserProfile",
                    "User Profile",
                    default = true
                ) {
                    plugin.patchRadialStatus(plugin.settings.radialStatus, isPluginEnabled("SquareAvatars"))
                })
                addView(createSwitch(
                    context,
                    "radialStatusChat",
                    "Chat",
                    default = true
                ) {
                    plugin.patchRadialStatus(plugin.settings.radialStatus, isPluginEnabled("SquareAvatars"))
                })

                if (!plugin.settings.radialStatus) visibility = View.GONE
                radialStatusSwitches = this
            })

            addView(Divider(context))
            addView(createHeader(context, "Size"))
            addView(createInput(
                context,
                "sizeDMsInd",
                "DMs Indicator Size",
                24
            ))
            addView(createInput(
                context,
                "sizeFriendsListInd",
                "Friends List Indicator Size",
                24
            ))
            addView(createInput(
                context,
                "sizeMembersListInd",
                "Members List Indicator Size",
                24
            ))
            addView(createInput(
                context,
                "sizeUserProfileInd",
                "User Profile Indicator Size",
                32
            ))
            addView(createInput(
                context,
                "sizeChatStatusPlatform",
                "Chat Status Platform Indicator Size",
                24
            ))
            addView(createInput(
                context,
                "sizeChatStatus",
                "Chat Status Indicator Size",
                16
            ))

            addView(Divider(context))
            addView(createHeader(context, "Colors"))
            plugin.settings.run {
                addView(ColorView(
                    context,
                    this,
                    "colorOnline",
                    "Online Color",
                    resources.getColor(R.c.status_green_600, null) - 1
                ))
                addView(ColorView(
                    context,
                    this,
                    "colorIdle",
                    "Idle Color",
                    resources.getColor(R.c.status_yellow, null) - 1
                ))
                addView(ColorView(
                    context,
                    this,
                    "colorDND",
                    "DND Color",
                    resources.getColor(R.c.status_red, null) - 1
                ))
            }
        }
    }

    private fun createSwitch(
        context: Context,
        key: String,
        label: String,
        subtext: String? = null,
        default: Boolean = false,
        onClick: ((Boolean) -> Unit)? = null
    ) = Utils.createCheckedSetting(context, CheckedSetting.ViewType.SWITCH, label, subtext).apply {
        isChecked = plugin.settings.getBool(key, default)
        setOnCheckedListener {
            plugin.settings.setBool(key, it)
            onClick?.invoke(it)
        }
    }

    private fun createHeader(context: Context, label: String) =
        TextView(context, null, 0, R.i.UiKit_Settings_Item_Header).apply {
            typeface = ResourcesCompat.getFont(context, Constants.Fonts.whitney_semibold)
            text = label
        }

    private fun createInput(
        context: Context,
        key: String,
        label: String,
        default: Int
    ) = TextInput(context, "$label (default $default)", plugin.settings.getInt(key, default).toString(), SimpleTextWatcher {
        it?.run {
            val str = toString()
            if (str != "") plugin.settings.setInt(key, str.toInt())
        }
    }).apply {
        editText.inputType = InputType.TYPE_CLASS_NUMBER
        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
            DimenUtils.defaultPadding.let { setMargins(it, 0, it, it) }
        }
    }
}
