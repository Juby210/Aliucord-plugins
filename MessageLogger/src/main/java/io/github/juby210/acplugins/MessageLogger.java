/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package io.github.juby210.acplugins;

import android.content.Context;
import android.database.Cursor;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;

import com.aliucord.CollectionUtils;
import com.aliucord.Logger;
import com.aliucord.Utils;
import com.aliucord.annotations.AliucordPlugin;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.Hook;
import com.aliucord.patcher.PreHook;
import com.aliucord.utils.ReflectUtils;
import com.aliucord.wrappers.ChannelWrapper;
import com.discord.api.channel.Channel;
import com.discord.databinding.WidgetGuildContextMenuBinding;
import com.discord.models.deserialization.gson.InboundGatewayGsonParser;
import com.discord.models.domain.ModelMessageDelete;
import com.discord.models.message.Message;
import com.discord.stores.StoreMessageState;
import com.discord.stores.StoreMessagesHolder;
import com.discord.stores.StoreStream;
import com.discord.utilities.textprocessing.DiscordParser;
import com.discord.utilities.textprocessing.MessagePreprocessor;
import com.discord.utilities.textprocessing.MessageRenderContext;
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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.juby210.acplugins.messagelogger.MessageRecord;
import io.github.juby210.acplugins.messagelogger.ReAdder;
import kotlin.jvm.functions.Function1;

@AliucordPlugin
@SuppressWarnings({ "unchecked", "CommentedOutCode" })
public final class MessageLogger extends Plugin {

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

        settingsTab = new SettingsTab(PluginSettings.class, SettingsTab.Type.BOTTOM_SHEET);

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

    // Requires app to be restarted after removing from log
    private void patchWidgetChatListActions() throws Throwable {
        patcher.patch(WidgetChatListActions.class.getDeclaredMethod("configureUI", WidgetChatListActions.Model.class),
            new Hook((cf) -> {
                var modal = (WidgetChatListActions.Model) cf.args[0];
                var message = modal.getMessage();
                SQLite sqlite = new SQLite(context);
                var messageId = message.getId();
                var isDeleted = sqlite.isMessageDeleted(messageId);
                if (isDeleted || sqlite.isMessageEdited(messageId)) {
                    var viewID = View.generateViewId();
                    var hideIcon = ContextCompat.getDrawable(context, com.lytefast.flexinput.R.e.design_ic_visibility_off).mutate();
                    var actions = (WidgetChatListActions) cf.thisObject;
                    var scrollView = (NestedScrollView) actions.getView();
                    var lay = (LinearLayout) scrollView.getChildAt(0);
                    if (lay.findViewById(viewID) == null) {
                        TextView tw = new TextView(lay.getContext(), null, 0, com.lytefast.flexinput.R.i.UiKit_Settings_Item_Icon);
                        tw.setId(viewID);
                        tw.setText(isDeleted ? "Remove Deleted Message" : "Remove Edit History");
                        tw.setCompoundDrawablesRelativeWithIntrinsicBounds(hideIcon, null, null, null);
                        lay.addView(tw, lay.getChildCount());
                        tw.setOnClickListener((v) -> {
                            if (isDeleted) {
                                sqlite.removeDeletedMessage(messageId);
                            } else {
                                sqlite.removeEditedMessage(messageId);
                            }
                            sqlite.close();
                            Utils.showToast("Removed From Logs");
                            ((WidgetChatListActions) cf.thisObject).dismiss();
                        });
                    }
                }
            })
        );
    }

    private void patchWidgetChannelsListItemChannelActions() throws Throwable {
        patcher.patch(WidgetChannelsListItemChannelActions.class.getDeclaredMethod("configureUI", WidgetChannelsListItemChannelActions.Model.class),
            new Hook((cf) -> {
                var modal = (WidgetChannelsListItemChannelActions.Model) cf.args[0];
                var channel = modal.getChannel();
                SQLite sqlite = new SQLite(context);
                var channelId = channel.k();
                var isWhitelisted = sqlite.isChannelWhitelisted(channelId);
                var viewID = View.generateViewId();
                var icon = isWhitelisted ? ContextCompat.getDrawable(context, com.lytefast.flexinput.R.e.design_ic_visibility_off).mutate() : ContextCompat.getDrawable(context, com.lytefast.flexinput.R.e.design_ic_visibility).mutate();
                var actions = (WidgetChannelsListItemChannelActions) cf.thisObject;
                var scrollView = (NestedScrollView) actions.getView();
                var lay = (LinearLayout) scrollView.getChildAt(0);
                if (lay.findViewById(viewID) == null) {
                    TextView tw = new TextView(lay.getContext(), null, 0, com.lytefast.flexinput.R.i.UiKit_Settings_Item_Icon);
                    tw.setId(viewID);
                    tw.setText(isWhitelisted ? "Disable Logging" : "Enable Logging");
                    tw.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null);
                    lay.addView(tw, lay.getChildCount());
                    tw.setOnClickListener((v) -> {
                        if (isWhitelisted) {
                            sqlite.removeChannelFromWhitelist(channelId);
                        } else {
                            sqlite.addChannelToWhitelist(channelId);
                        }
                        sqlite.close();
                        ((WidgetChannelsListItemChannelActions) cf.thisObject).dismiss();
                    });
                }
            })
        );
    }

    private void patchWidgetGuildContextMenu() throws Throwable {
        patcher.patch(WidgetGuildContextMenu.class.getDeclaredMethod("configureUI", GuildContextMenuViewModel.ViewState.class),
            new Hook((cf) -> {
                var state = (GuildContextMenuViewModel.ViewState.Valid) cf.args[0];
                Method method = null;
                WidgetGuildContextMenuBinding binding = null;
                try {
                    method = ReflectUtils.getMethodByArgs(WidgetGuildContextMenu.class, "getBinding");
                    binding = (WidgetGuildContextMenuBinding) method.invoke(cf.thisObject);
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
                LinearLayout lay = (LinearLayout) binding.e.getParent();
                var guild = state.getGuild();
                SQLite sqlite = new SQLite(context);
                var guildId = guild.getId();
                var isWhitelisted = sqlite.isGuildWhitelisted(guildId);
                var viewID = View.generateViewId();
                var icon = isWhitelisted ? ContextCompat.getDrawable(context, com.lytefast.flexinput.R.e.design_ic_visibility_off).mutate() : ContextCompat.getDrawable(context, com.lytefast.flexinput.R.e.design_ic_visibility).mutate();
                if (lay.findViewById(viewID) == null) {
                    TextView tw = new TextView(lay.getContext(), null, 0, com.lytefast.flexinput.R.i.ContextMenuTextOption);
                    tw.setId(viewID);
                    tw.setText(isWhitelisted ? "Disable Logging" : "Enable Logging");
                    tw.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null);
                    lay.addView(tw, lay.getChildCount());
                    tw.setOnClickListener((v) -> {
                        if (isWhitelisted) {
                            sqlite.removeGuildFromWhitelist(guildId);
                        } else {
                            sqlite.addGuildToWhitelist(guildId);
                        }
                        sqlite.close();
                        lay.setVisibility(View.GONE);
                    });
                }
            })
        );
    }

    private void patchAddMessages() {
        patcher.patch(StoreMessagesHolder.class, "addMessages", new Class<?>[]{ List.class }, new Hook(param -> {
            var messages = (List<Message>) param.args[0];
            for (var message : messages) {
                SQLite sqlite = new SQLite(context);
                var channel = StoreStream.getChannels().getChannel(message.getChannelId());
                var guildId = channel != null ? ChannelWrapper.getGuildId(channel) : 0;
                if (guildId != 0 && !sqlite.isGuildWhitelisted(guildId)) continue;
                if (!sqlite.isChannelWhitelisted(message.getChannelId())) continue;
                sqlite.close();
                updateCached(message.getId(), message);
            }
        }));
    }

    private void patchDeleteMessages() {
        patcher.patch(StoreMessagesHolder.class, "deleteMessages", new Class<?>[]{ long.class, List.class }, new PreHook(param -> {
            var channelId = (long) param.args[0];
            var newDeleted = (List<Long>) param.args[1];
            var updateMessages = StoreStream.getChannelsSelected().getId() == channelId;
            for (var id : newDeleted) {
                var msg = getCachedMessage(channelId, id);
                if (msg == null) continue;
                SQLite sqlite = new SQLite(context);
                var channel = StoreStream.getChannels().getChannel(msg.getChannelId());
                var guildId = channel != null ? ChannelWrapper.getGuildId(channel) : 0;
                if (guildId != 0 && !sqlite.isGuildWhitelisted(guildId)) {
                    StoreStream.getMessages().handleMessageDelete(new ModelMessageDelete(channelId, id));
                    continue;
                }
                if (!sqlite.isChannelWhitelisted(msg.getChannelId())) {
                    StoreStream.getMessages().handleMessageDelete(new ModelMessageDelete(channelId, id));
                    continue;
                }
                // User author;
                // if (!selfDelete && (author = msg.getAuthor()) != null && new CoreUser(author).getId() == StoreStream.getUsers().getMe().getId()) selfDelete = true;
                var channelDeletes = deletedMessagesRecord.computeIfAbsent(channelId, k -> new ArrayList<>());
                channelDeletes.add(id);
                var record = messageRecord.computeIfAbsent(id, k -> new MessageRecord());
                record.message = msg;
                record.deleteData = new MessageRecord.DeleteData(System.currentTimeMillis());
                if (updateMessages && chatList != null) {
                    var adapter = WidgetChatList.access$getAdapter$p(chatList);
                    var data = adapter.getInternalData();
                    var i = CollectionUtils.findIndex(data, e -> e instanceof MessageEntry && ((MessageEntry) e).getMessage().getId() == msg.getId());
                    if (i != -1) adapter.notifyItemChanged(i);
                    Cursor editHistory = sqlite.getAllMessageEdits(msg.getId());
                    if (editHistory.moveToFirst()) {
                        do {
                            var editedMessage = InboundGatewayGsonParser.fromJson(new JsonReader(new StringReader(editHistory.getString(1))), MessageRecord.class);
                            record.editHistory = editedMessage.editHistory;
                        } while (editHistory.moveToNext());
                    }
                    sqlite.addNewMessage(record);
                    sqlite.close();
                }
            }

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
                        sqlite.isChannelWhitelisted(channelId)
                ) {
                    if (guildId == 0 || sqlite.isGuildWhitelisted(guildId)) {
                        var channelEdits = editedMessagesRecord.computeIfAbsent(channelId, k -> new ArrayList<>());
                        channelEdits.add(id);
                        var record = messageRecord.computeIfAbsent(id, k -> new MessageRecord());
                        record.message = msg;
                        record.editHistory.add(new MessageRecord.EditHistory(content, System.currentTimeMillis()));
                        sqlite.addNewMessageEdit(record);
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
