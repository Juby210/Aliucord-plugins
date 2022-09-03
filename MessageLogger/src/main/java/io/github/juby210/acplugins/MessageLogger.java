/*
 * Copyright (c) 2021-2022 Juby210
 * Licensed under the Open Software License version 3.0
 */

package io.github.juby210.acplugins;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.widget.NestedScrollView;

import com.aliucord.*;
import com.aliucord.annotations.AliucordPlugin;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.Hook;
import com.aliucord.patcher.PreHook;
import com.aliucord.wrappers.ChannelWrapper;
import com.discord.databinding.WidgetGuildContextMenuBinding;
import com.discord.models.deserialization.gson.InboundGatewayGsonParser;
import com.discord.models.message.Message;
import com.discord.stores.*;
import com.discord.utilities.color.ColorCompat;
import com.discord.utilities.textprocessing.*;
import com.discord.utilities.textprocessing.node.EditedMessageNode;
import com.discord.utilities.time.ClockFactory;
import com.discord.utilities.time.TimeUtils;
import com.discord.utilities.view.text.SimpleDraweeSpanTextView;
import com.discord.widgets.channels.list.WidgetChannelsListItemChannelActions;
import com.discord.widgets.chat.list.WidgetChatList;
import com.discord.widgets.chat.list.actions.WidgetChatListActions;
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemMessage;
import com.discord.widgets.chat.list.adapter.WidgetChatListItem;
import com.discord.widgets.chat.list.entries.MessageEntry;
import com.discord.widgets.guilds.contextmenu.GuildContextMenuViewModel;
import com.discord.widgets.guilds.contextmenu.WidgetGuildContextMenu;
import com.facebook.drawee.span.DraweeSpanStringBuilder;
import com.google.gson.stream.JsonReader;

import java.io.StringReader;
import java.util.*;

import io.github.juby210.acplugins.messagelogger.*;
import kotlin.jvm.functions.Function1;

@AliucordPlugin
@SuppressLint("UseCompatLoadingForDrawables")
@SuppressWarnings({ "unchecked", "CommentedOutCode" })
public final class MessageLogger extends Plugin {
    public MessageLogger() {
        settingsTab = new SettingsTab(PluginSettings.class, SettingsTab.Type.BOTTOM_SHEET);
    }

    public static WidgetChatList chatList;
    public final Map<Long, Message> cachedMessages = new HashMap<>();
    public final Map<Long, List<Long>> deletedMessagesRecord = new HashMap<>();
    public final Map<Long, List<Long>> editedMessagesRecord = new HashMap<>();
    public final Map<Long, MessageRecord> messageRecord = new HashMap<>();
    private final Logger logger = new Logger("MessageLogger");
    private Context context;

    @Override
    public void start(Context context) throws Throwable {
        this.context = context;

        deletedMessagesRecord.clear();
        messageRecord.clear();
        editedMessagesRecord.clear();
        SQLite sqlite = new SQLite(context);
        Cursor deletedMessages = sqlite.getAllDeletedMessages();
        if (deletedMessages.moveToFirst() && deletedMessages.getCount() > 0) {
            do {
                long messageId = deletedMessages.getLong(0);
                var deleteData = InboundGatewayGsonParser.fromJson(new JsonReader(new StringReader(deletedMessages.getString(1))), MessageRecord.DeleteData.class);
                var deletedMessageRecord = InboundGatewayGsonParser.fromJson(new JsonReader(new StringReader(deletedMessages.getString(2))), MessageRecord.class);
                long channelId = deletedMessageRecord.message.getChannelId();
                deletedMessagesRecord.computeIfAbsent(channelId, k -> new ArrayList<>()).add(messageId);
                var record = messageRecord.computeIfAbsent(messageId, k -> new MessageRecord());
                record.message = deletedMessageRecord.message;
                record.editHistory = deletedMessageRecord.editHistory;
                record.deleteData = deleteData;
            } while (deletedMessages.moveToNext());
        }
        deletedMessages.close();
        Cursor editedMessages = sqlite.getAllEditedMessages();
        if (editedMessages.moveToFirst() && editedMessages.getCount() > 0) {
            do {
                long messageId = editedMessages.getLong(0);
                var editedMessageRecord = InboundGatewayGsonParser.fromJson(new JsonReader(new StringReader(editedMessages.getString(1))), MessageRecord.class);
                long channelId = editedMessageRecord.message.getChannelId();
                var record = messageRecord.computeIfAbsent(messageId, k -> new MessageRecord());
                record.editHistory = editedMessageRecord.editHistory;
                record.message = editedMessageRecord.message;
                var editRecord = editedMessagesRecord.computeIfAbsent(channelId, k -> new ArrayList<>());
                editRecord.add(messageId);
            } while (editedMessages.moveToNext());
        }
        editedMessages.close();
        sqlite.close();
        new ReAdder(this, patcher);

        patcher.patch(WidgetChatList.class.getDeclaredConstructor(), new Hook(param -> chatList = (WidgetChatList) param.thisObject));

        patchWidgetChatListActions();
        patchWidgetChannelsListItemChannelActions();
        patchWidgetGuildContextMenu();
        patchAddMessages();
        patchDeleteMessages();
        patchUpdateMessages();
        patchProcessMessageText();
    }

    @Override
    public void stop(Context context) {
        patcher.unpatchAll();
    }

    private void patchWidgetChatListActions() throws Throwable {
        var hideIcon = context.getDrawable(com.lytefast.flexinput.R.e.design_ic_visibility_off).mutate();

        patcher.patch(WidgetChatListActions.class.getDeclaredMethod("configureUI", WidgetChatListActions.Model.class),
            new Hook((cf) -> {
                var modal = (WidgetChatListActions.Model) cf.args[0];
                var message = modal.getMessage();
                var sqlite = new SQLite(context);
                var messageId = message.getId();
                var isDeleted = sqlite.isMessageDeleted(messageId);
                var isEdited = sqlite.isMessageEdited(messageId);
                sqlite.close();
                if (isDeleted || isEdited) {
                    var viewID = View.generateViewId();
                    var actions = (WidgetChatListActions) cf.thisObject;
                    var scrollView = (NestedScrollView) actions.getView();
                    var lay = (LinearLayout) scrollView.getChildAt(0);
                    if (lay.findViewById(viewID) == null) {
                        TextView tw = new TextView(lay.getContext(), null, 0, com.lytefast.flexinput.R.i.UiKit_Settings_Item_Icon);
                        tw.setId(viewID);
                        tw.setText(isDeleted ? "Remove Deleted Message" : "Remove Edit History");
                        hideIcon.setTint(ColorCompat.getThemedColor(tw, com.lytefast.flexinput.R.b.colorInteractiveNormal));
                        tw.setCompoundDrawablesRelativeWithIntrinsicBounds(hideIcon, null, null, null);
                        lay.addView(tw, lay.getChildCount());
                        tw.setOnClickListener((v) -> {
                            var db = new SQLite(context);
                            if (isDeleted) {
                                db.removeDeletedMessage(messageId);
                            }
                            if (isEdited) {
                                db.removeEditedMessage(messageId);
                            }
                            db.close();
                            Utils.showToast("Removed From Logs");
                            ((WidgetChatListActions) cf.thisObject).dismiss();
                        });
                    }
                }
            })
        );
    }

    private void patchWidgetChannelsListItemChannelActions() throws Throwable {
        var whitelistedIcon = context.getDrawable(com.lytefast.flexinput.R.e.design_ic_visibility_off).mutate();
        var blacklistedIcon = context.getDrawable(com.lytefast.flexinput.R.e.design_ic_visibility).mutate();

        patcher.patch(WidgetChannelsListItemChannelActions.class.getDeclaredMethod("configureUI", WidgetChannelsListItemChannelActions.Model.class),
            new Hook((cf) -> {
                var modal = (WidgetChannelsListItemChannelActions.Model) cf.args[0];
                var channel = modal.getChannel();
                var channelId = ChannelWrapper.getId(channel);
                var sqlite = new SQLite(context);
                if (sqlite.getBoolSetting("ignoreMutedChannels", true) && UtilsKt.isChannelMuted(ChannelWrapper.getGuildId(channel), channelId)) {
                    sqlite.close();
                    return;
                }
                var isWhitelisted = sqlite.isChannelWhitelisted(channel);
                sqlite.close();
                var viewID = View.generateViewId();
                var icon = isWhitelisted ? whitelistedIcon : blacklistedIcon;
                var actions = (WidgetChannelsListItemChannelActions) cf.thisObject;
                var scrollView = (NestedScrollView) actions.getView();
                var lay = (LinearLayout) scrollView.getChildAt(0);
                if (lay.findViewById(viewID) == null) {
                    TextView tw = new TextView(lay.getContext(), null, 0, com.lytefast.flexinput.R.i.UiKit_Settings_Item_Icon);
                    tw.setId(viewID);
                    tw.setText(isWhitelisted ? "Disable Logging" : "Enable Logging");
                    icon.setTint(ColorCompat.getThemedColor(tw, com.lytefast.flexinput.R.b.colorInteractiveNormal));
                    tw.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null);
                    lay.addView(tw, lay.getChildCount());
                    tw.setOnClickListener((v) -> {
                        var db = new SQLite(context);
                        if (isWhitelisted) {
                            db.removeChannelFromWhitelist(channelId);
                        } else {
                            db.addChannelToWhitelist(channelId);
                        }
                        db.close();
                        ((WidgetChannelsListItemChannelActions) cf.thisObject).dismiss();
                    });
                }
            })
        );
    }

    private void patchWidgetGuildContextMenu() throws Throwable {
        var whitelistedIcon = context.getDrawable(com.lytefast.flexinput.R.e.design_ic_visibility_off).mutate();
        var blacklistedIcon = context.getDrawable(com.lytefast.flexinput.R.e.design_ic_visibility).mutate();

        var getBinding = WidgetGuildContextMenu.class.getDeclaredMethod("getBinding");
        getBinding.setAccessible(true);
        patcher.patch(WidgetGuildContextMenu.class.getDeclaredMethod("configureUI", GuildContextMenuViewModel.ViewState.class),
            new Hook((cf) -> {
                var state = (GuildContextMenuViewModel.ViewState.Valid) cf.args[0];
                WidgetGuildContextMenuBinding binding = null;
                try {
                    binding = (WidgetGuildContextMenuBinding) getBinding.invoke(cf.thisObject);
                } catch (Throwable e) {
                    logger.error("Failed to get binding", e);
                }
                var lay = (LinearLayout) binding.e.getParent();
                var guild = state.getGuild();
                var sqlite = new SQLite(context);
                var guildId = guild.getId();
                if (sqlite.getBoolSetting("ignoreMutedServers", true) && UtilsKt.isGuildMuted(guildId)) {
                    sqlite.close();
                    return;
                }
                var isWhitelisted = sqlite.isGuildWhitelisted(guildId);
                sqlite.close();
                var viewID = View.generateViewId();
                var icon = isWhitelisted ? whitelistedIcon : blacklistedIcon;
                if (lay.findViewById(viewID) == null) {
                    TextView tw = new TextView(lay.getContext(), null, 0, com.lytefast.flexinput.R.i.ContextMenuTextOption);
                    tw.setId(viewID);
                    tw.setText(isWhitelisted ? "Disable Logging" : "Enable Logging");
                    icon.setTint(ColorCompat.getThemedColor(tw, com.lytefast.flexinput.R.b.colorInteractiveNormal));
                    tw.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null);
                    lay.addView(tw, lay.getChildCount());
                    tw.setOnClickListener((v) -> {
                        var db = new SQLite(context);
                        if (isWhitelisted) {
                            db.removeGuildFromWhitelist(guildId);
                        } else {
                            db.addGuildToWhitelist(guildId);
                        }
                        db.close();
                        lay.setVisibility(View.GONE);
                    });
                }
            })
        );
    }

    private void patchAddMessages() {
        patcher.patch(StoreMessagesHolder.class, "addMessages", new Class<?>[]{ List.class }, new Hook(param -> {
            var messages = (List<Message>) param.args[0];
            var sqlite = new SQLite(context);
            for (var message : messages) {
                var channel = StoreStream.getChannels().getChannel(message.getChannelId());
                var guildId = channel != null ? ChannelWrapper.getGuildId(channel) : 0;
                if (guildId != 0 && !sqlite.isGuildWhitelisted(guildId)) continue;
                if (!sqlite.isChannelWhitelisted(channel)) continue;
                updateCached(message.getId(), message);
            }
            sqlite.close();
        }));
    }

    private void patchDeleteMessages() {
        patcher.patch(StoreMessagesHolder.class, "deleteMessages", new Class<?>[]{ long.class, List.class }, new PreHook(param -> {
            var sqlite = new SQLite(context);
            if (!sqlite.getBoolSetting("logDeletes", true)) {
                sqlite.close();
                return;
            }
            var channelId = (long) param.args[0];
            var channel = StoreStream.getChannels().getChannel(channelId);
            if (!sqlite.isChannelWhitelisted(channel)) {
                sqlite.close();
                return;
            }
            var guildId = channel != null ? ChannelWrapper.getGuildId(channel) : 0;
            if (guildId != 0 && !sqlite.isGuildWhitelisted(guildId)) {
                sqlite.close();
                return;
            }

            var newDeleted = (List<Long>) param.args[1];
            var updateMessages = StoreStream.getChannelsSelected().getId() == channelId;
            for (var id : newDeleted) {
                var msg = getCachedMessage(channelId, id);
                if (msg == null) continue;
                // User author;
                // if (!selfDelete && (author = msg.getAuthor()) != null && new CoreUser(author).getId() == StoreStream.getUsers().getMe().getId()) selfDelete = true;
                var channelDeletes = deletedMessagesRecord.computeIfAbsent(channelId, k -> new ArrayList<>());
                channelDeletes.add(id);
                var record = messageRecord.computeIfAbsent(id, k -> new MessageRecord());
                record.message = msg;
                record.deleteData = new MessageRecord.DeleteData(System.currentTimeMillis());
                Cursor editHistory = sqlite.getAllMessageEdits(msg.getId());
                if (editHistory.moveToFirst()) {
                    do {
                        var editedMessage = InboundGatewayGsonParser.fromJson(new JsonReader(new StringReader(editHistory.getString(1))), MessageRecord.class);
                        record.editHistory = editedMessage.editHistory;
                    } while (editHistory.moveToNext());
                }
                if (sqlite.getBoolSetting("saveLogs", true)) sqlite.addNewMessage(record);
                if (updateMessages) updateMessages(id);
            }

            sqlite.close();
            param.setResult(null);
        }));
    }

    private void patchUpdateMessages() {
        patcher.patch(StoreMessagesHolder.class, "updateMessages", new Class<?>[]{ com.discord.api.message.Message.class }, new PreHook(param -> {
            var msg = new Message((com.discord.api.message.Message) param.args[0]);
            var id = msg.getId();
            var edited = msg.getEditedTimestamp();

            if (edited != null && edited.g() > 0) {
                var channelId = msg.getChannelId();
                var origMsg = getCachedMessage(channelId, id);

                SQLite sqlite = new SQLite(context);
                var channel = StoreStream.getChannels().getChannel(msg.getChannelId());
                var guildId = channel != null ? ChannelWrapper.getGuildId(channel) : 0;

                String content;
                if (
                    origMsg != null &&
                        (content = origMsg.getContent()) != null &&
                        !content.equals(msg.getContent()) &&
                        sqlite.isChannelWhitelisted(channel) &&
                        sqlite.getBoolSetting("logEdits", true)
                ) {
                    if (guildId == 0 || sqlite.isGuildWhitelisted(guildId)) {
                        var channelEdits = editedMessagesRecord.computeIfAbsent(channelId, k -> new ArrayList<>());
                        channelEdits.add(id);
                        var record = messageRecord.computeIfAbsent(id, k -> new MessageRecord());
                        record.message = msg;
                        record.editHistory.add(new MessageRecord.EditHistory(content, System.currentTimeMillis()));
                        if (sqlite.getBoolSetting("saveLogs", true)) sqlite.addNewMessageEdit(record);
                    }
                }
                sqlite.close();
            }

            updateCached(id, msg);
        }));
    }

    private void patchProcessMessageText() throws Throwable {
        var clock = ClockFactory.get();

        var c = WidgetChatListAdapterItemMessage.class;
        var getMessagePreprocessor = c.getDeclaredMethod("getMessagePreprocessor",
            long.class, Message.class, StoreMessageState.State.class);
        getMessagePreprocessor.setAccessible(true);
        var getSpoilerClickHandler = c.getDeclaredMethod("getSpoilerClickHandler", Message.class);
        getSpoilerClickHandler.setAccessible(true);
        var getMessageRenderContext = c.getDeclaredMethod("getMessageRenderContext",
            Context.class, MessageEntry.class, Function1.class);
        getMessageRenderContext.setAccessible(true);
        var mDraweeStringBuilder = SimpleDraweeSpanTextView.class.getDeclaredField("mDraweeStringBuilder");
        mDraweeStringBuilder.setAccessible(true);

        patcher.patch(WidgetChatListAdapterItemMessage.class, "processMessageText", new Class<?>[]{ SimpleDraweeSpanTextView.class, MessageEntry.class }, new Hook(param -> {
            var messageEntry = (MessageEntry) param.args[1];
            var message = messageEntry.getMessage();
            if (message == null) return;
            Long channelId = message.getChannelId();
            Long id = message.getId();

            var channelDeletes = deletedMessagesRecord.get(channelId);
            var channelEdits = editedMessagesRecord.get(channelId);
            if ((channelDeletes == null || !channelDeletes.contains(id)) && (channelEdits == null || !channelEdits.contains(id))) return;
            var record = messageRecord.get(id);
            if (record == null) return;

            try {
                var textView = (SimpleDraweeSpanTextView) param.args[0];
                var builder = (DraweeSpanStringBuilder) mDraweeStringBuilder.get(textView);
                if (builder == null) return;
                var context = textView.getContext();

                if (record.deleteData != null) {
                    var origLength = builder.length();
                    var spans = builder.getSpans(0, origLength, ForegroundColorSpan.class);
                    if (spans.length == 0) markDeleted(builder, 0, origLength);
                    else {
                        int lastEnd = 0;
                        for (ForegroundColorSpan span : spans) {
                            markDeleted(builder, lastEnd, builder.getSpanStart(span));
                            lastEnd = builder.getSpanEnd(span);
                        }
                    }
                    customEditedLikeText(textView.getContext(), builder,
                        " (deleted: " + TimeUtils.toReadableTimeString(context, record.deleteData.time, clock) + ")");
                }

                if (record.editHistory.size() > 0) {
                    var data = ((WidgetChatListItem) param.thisObject).adapter.getData();
                    if (data != null) {
                        MessagePreprocessor messagePreprocessor = (MessagePreprocessor) getMessagePreprocessor.invoke(
                            param.thisObject, data.getUserId(), message, messageEntry.getMessageState());
                        MessageRenderContext renderContext = (MessageRenderContext) getMessageRenderContext.invoke(
                            param.thisObject, context, messageEntry, getSpoilerClickHandler.invoke(param.thisObject, message));
                        DiscordParser.ParserOptions options = message.isWebhook() ?
                            DiscordParser.ParserOptions.ALLOW_MASKED_LINKS : DiscordParser.ParserOptions.DEFAULT;
                        int added = 0;
                        for (MessageRecord.EditHistory edit : record.editHistory) {
                            DraweeSpanStringBuilder parsed = DiscordParser.parseChannelMessage(
                                context,
                                edit.content,
                                renderContext,
                                messagePreprocessor,
                                options,
                                false
                            );
                            customEditedLikeText(context, parsed,
                                " (edited: " + TimeUtils.toReadableTimeString(context, edit.time, clock) + ")\n");
                            builder.insert(added, parsed);
                            added += parsed.length();
                        }
                        setEditedColor(context, builder, 0, added);
                    }
                }
                textView.setDraweeSpanStringBuilder(builder);
            } catch (Throwable e) {logger.error(e);}
        }));
    }

    private void updateMessages(long id) {
        if (chatList != null) {
            var adapter = WidgetChatList.access$getAdapter$p(chatList);
            var data = adapter.getInternalData();
            var i = CollectionUtils.findIndex(data, e -> e instanceof MessageEntry && ((MessageEntry) e).getMessage().getId() == id);
            if (i != -1) adapter.notifyItemChanged(i);
        }
    }

    // cache
    private Message getCachedMessage(long channelId, long id) {
        return cachedMessages.containsKey(id) ? cachedMessages.get(id) : StoreStream.getMessages().getMessage(channelId, id);
    }

    private void updateCached(Long id, Message message) {
        cachedMessages.put(id, message);
    }

    // some display utils
    private void markDeleted(SpannableStringBuilder builder, int start, int end) {
        if (start != end) builder.setSpan(new ForegroundColorSpan(0xfff04747), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private void customEditedLikeText(Context context, SpannableStringBuilder builder, CharSequence text) {
        int len = builder.length();
        builder.append(text);
        builder.setSpan(new RelativeSizeSpan(0.75f), len, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        setEditedColor(context, builder, len, builder.length());
    }

    @SuppressWarnings("AccessStaticViaInstance")
    private void setEditedColor(Context context, SpannableStringBuilder builder, int start, int end) {
        if (start != end) builder.setSpan(EditedMessageNode.Companion.access$getForegroundColorSpan(EditedMessageNode.Companion, context),
            start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
}
