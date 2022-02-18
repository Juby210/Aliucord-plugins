/*
 * Copyright (c) 2021-2022 Juby210
 * Licensed under the Open Software License version 3.0
 */

package io.github.juby210.acplugins.bsi

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.view.Gravity
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import androidx.core.widget.TextViewCompat
import com.aliucord.Utils
import com.aliucord.api.SettingsAPI
import com.aliucord.utils.DimenUtils.dp
import com.discord.utilities.color.ColorCompat
import com.discord.utilities.colors.ColorPickerUtils
import com.lytefast.flexinput.R

class ColorPickerListener(private val onSelect: (Int) -> Unit, private val onReset: () -> Unit) : b.k.a.a.f {
    override fun onDialogDismissed(id: Int) {}

    override fun onColorReset(id: Int) = onReset()
    override fun onColorSelected(id: Int, color: Int) = onSelect(color)
}

@SuppressLint("ViewConstructor")
class ColorView(
    context: Context,
    settings: SettingsAPI,
    key: String,
    label: String,
    default: Int
) : LinearLayout(context) {
    private val colorCircleView: LinearLayout

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL

        val onClick = OnClickListener {
            ColorPickerUtils.INSTANCE.buildColorPickerDialog(
                context,
                Utils.getResId("color_picker_title", "string"),
                settings.getInt(key, default)
            ).apply {
                k = ColorPickerListener({
                    settings.setInt(key, it)
                    setColor(it)
                    Utils.promptRestart()
                }, {
                    settings.remove(key)
                    setColor(default)
                    Utils.promptRestart()
                })
                show(Utils.appActivity.supportFragmentManager, "Color Picker")
            }
        }

        addView(TextView(context, null, 0, R.i.UiKit_Settings_Item).apply {
            layoutParams = LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
                weight = 1f
            }
            setOnClickListener(onClick)

            TextViewCompat.setAutoSizeTextTypeWithDefaults(this, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM)
            text = label
        })

        addView(LinearLayout(context).apply {
            background = ShapeDrawable(OvalShape())
            layoutParams = LayoutParams(32.dp, 32.dp).apply {
                marginEnd = 16.dp
            }
            setOnClickListener(onClick)
            addView(View(context).apply {
                layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT).apply {
                    1.dp.let { setMargins(it, it, it, it) }
                }
                background = ShapeDrawable(OvalShape())
            })

            colorCircleView = this
        })

        setColor(settings.getInt(key, default))
    }

    private fun setColor(color: Int) {
        val bgColor = ColorUtils.setAlphaComponent(
            ColorCompat.getThemedColor(colorCircleView.context, R.c.primary_dark_600),
            0xFF
        )

        val contrastColor = when {
            ColorUtils.calculateContrast(color, bgColor) > 2f -> color
            Color.luminance(color) < 0.5f -> Color.WHITE
            else -> Color.BLACK
        }

        colorCircleView.background.colorFilter = PorterDuffColorFilter(contrastColor, PorterDuff.Mode.SRC_ATOP)
        colorCircleView.getChildAt(0).background.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP)
    }
}
