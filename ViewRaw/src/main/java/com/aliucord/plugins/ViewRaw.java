/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.plugins;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.widget.NestedScrollView;

import com.aliucord.*;
import com.aliucord.entities.Plugin;
import com.aliucord.fragments.SettingsPage;
import com.aliucord.patcher.PinePatchFn;
import com.aliucord.utils.ReflectUtils;
import com.aliucord.views.Divider;
import com.discord.databinding.WidgetChatListActionsBinding;
import com.discord.models.message.Message;
import com.discord.models.user.CoreUser;
import com.discord.simpleast.code.CodeNode;
import com.discord.simpleast.core.node.Node;
import com.discord.simpleast.core.parser.Parser;
import com.discord.simpleast.core.parser.Rule;
import com.discord.utilities.color.ColorCompat;
import com.discord.utilities.textprocessing.*;
import com.discord.utilities.textprocessing.node.BasicRenderContext;
import com.discord.utilities.textprocessing.node.BlockBackgroundNode;
import com.discord.widgets.chat.list.actions.WidgetChatListActions;
import com.lytefast.flexinput.R;

import java.util.List;
import java.util.Map;

@SuppressWarnings({"unchecked", "unused"})
public class ViewRaw extends Plugin {
    public ViewRaw() {
        needsResources = true;
    }

    public static class Page extends SettingsPage {
        public final Message message;
        public Page(Message message) {
            this.message = message;
        }

        @Override
        @SuppressLint("SetTextI18n")
        public void onViewBound(View view) {
            super.onViewBound(view);
            setActionBarTitle("Raw message by " + new CoreUser(message.getAuthor()).getUsername());
            setActionBarSubtitle("View Raw");

            var context = view.getContext();
            var layout = getLinearLayout();

            var content = message.getContent();
            if (content != null && !content.equals("")) {
                var textView = new TextView(context);
                var builder = new SpannableStringBuilder();
                renderCodeBlock(context, builder, content, false);
                textView.setText(builder);
                textView.setTextIsSelectable(true);
                layout.addView(textView);
                layout.addView(new Divider(context));
            }

            var header = new TextView(context, null, 0, R.h.UiKit_Settings_Item_Header);
            header.setText("All Raw Data");
            header.setTypeface(ResourcesCompat.getFont(context, Constants.Fonts.whitney_semibold));
            header.setPadding(0, header.getPaddingTop(), header.getPaddingRight(), header.getPaddingBottom());
            layout.addView(header);

            var textView = new TextView(context);
            var builder = new SpannableStringBuilder();
            renderCodeBlock(context, builder, Utils.toJsonPretty(message), true);
            textView.setText(builder);
            textView.setTextIsSelectable(true);
            layout.addView(textView);
        }
    }

    public static class RenderContext implements BasicRenderContext {
        private final Context context;
        public RenderContext(Context ctx) {
            context = ctx;
        }

        @Override
        public Context getContext() {
            return context;
        }
    }

    @NonNull
    @Override
    public Manifest getManifest() {
        var manifest = new Manifest();
        manifest.authors = new Manifest.Author[]{ new Manifest.Author("Juby210", 324622488644616195L) };
        manifest.description = "View & Copy raw message and markdown.";
        manifest.version = "1.0.5";
        manifest.updateUrl = "https://raw.githubusercontent.com/Juby210/Aliucord-plugins/builds/updater.json";
        return manifest;
    }

    @Override
    @SuppressLint("SetTextI18n")
    public void start(Context ctx) throws Throwable {
        var icon = ResourcesCompat.getDrawable(resources,
                resources.getIdentifier("ic_viewraw", "drawable", "com.aliucord.plugins"), null);
        var id = View.generateViewId();

        var c = WidgetChatListActions.class;
        var getBinding = c.getDeclaredMethod("getBinding");
        getBinding.setAccessible(true);

        patcher.patch(c, "configureUI", new Class<?>[]{ WidgetChatListActions.Model.class }, new PinePatchFn(callFrame -> {
            try {
                var binding = (WidgetChatListActionsBinding) getBinding.invoke(callFrame.thisObject);
                if (binding == null) return;
                TextView viewRaw = binding.a.findViewById(id);
                viewRaw.setOnClickListener(e ->
                    Utils.openPageWithProxy(e.getContext(), new Page(((WidgetChatListActions.Model) callFrame.args[0]).getMessage())));
            } catch (Throwable ignored) {}
        }));

        patcher.patch(c, "onViewCreated", new Class<?>[]{ View.class, Bundle.class }, new PinePatchFn(callFrame -> {
            var linearLayout = (LinearLayout) ((NestedScrollView) callFrame.args[0]).getChildAt(0);
            var context = linearLayout.getContext();
            var viewRaw = new TextView(context, null, 0, R.h.UiKit_Settings_Item_Icon);
            viewRaw.setText("View Raw");
            if (icon != null) icon.setTint(ColorCompat.getThemedColor(context, R.b.colorInteractiveNormal));
            viewRaw.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null);
            viewRaw.setId(id);
            linearLayout.addView(viewRaw);
        }));
    }

    @Override
    public void stop(Context context) {
        patcher.unpatchAll();
    }

    private static Parser<MessageRenderContext, Node<MessageRenderContext>, MessageParseState> parser;
    private static List<? extends Rule<MessageRenderContext, ? extends Node<MessageRenderContext>, MessageParseState>> jsRules;

    public static void renderCodeBlock(Context context, SpannableStringBuilder builder, String content, boolean js) {
        if (js && jsRules == null) try {
            parser = (Parser<MessageRenderContext, Node<MessageRenderContext>, MessageParseState>) ReflectUtils.getField(DiscordParser.class, null, "SAFE_LINK_PARSER");
            var languageRules = (Map<String, List<? extends Rule<MessageRenderContext, ? extends Node<MessageRenderContext>, MessageParseState>>>) ReflectUtils.getField(Rules.INSTANCE.createCodeBlockRule(), "a");
            //noinspection ConstantConditions
            jsRules = languageRules.get("js");
        } catch (Throwable e) {
            Main.logger.error("Failed to get parser and js rules", e);
        }
        var node = new BlockBackgroundNode<>(false, new CodeNode<>(
            js && jsRules != null ?
                new CodeNode.a.a<>(content, parser.parse(content, MessageParseState.access$getInitialState$cp(), jsRules)) :
                new CodeNode.a.b(content),
            js ? "js" : null,
            Rules$createCodeBlockRule$codeStyleProviders$1.INSTANCE
        ));
        node.render(builder, new RenderContext(context));
    }
}
