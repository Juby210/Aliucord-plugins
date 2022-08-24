package io.github.juby210.acplugins;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.aliucord.Utils;
import com.aliucord.fragments.ConfirmDialog;
import com.aliucord.views.Button;
import com.aliucord.views.DangerButton;
import com.aliucord.widgets.BottomSheet;
import com.discord.views.CheckedSetting;

import java.io.*;

public class PluginSettings extends BottomSheet {

    public PluginSettings() {}

    Boolean isWhitelist;
    Boolean isChannelWhitelist;
    Boolean logEdits;
    Boolean logDeletes;
    SQLite sqlite;

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        sqlite = new SQLite(requireContext());

        isWhitelist = sqlite.getBoolSetting("whitelist", true);
        isChannelWhitelist = sqlite.getBoolSetting("channelWhitelist", false);
        logEdits = sqlite.getBoolSetting("logEdits", true);
        logDeletes = sqlite.getBoolSetting("logDeletes", true);

        sqlite.close();
        var context = requireContext();
        setPadding(20);

        TextView title = new TextView(context, null, 0, com.lytefast.flexinput.R.i.UiKit_Settings_Item_Header);
        title.setText("Message Logger");
        title.setGravity(Gravity.START);

        TextView logging = new TextView(context, null, 0, com.lytefast.flexinput.R.i.UiKit_Settings_Item_Header);
        logging.setText("Logging");
        logging.setGravity(Gravity.CENTER_HORIZONTAL);

        Button guildWhitelist = new Button(context);
        guildWhitelist.setText("Toggle Whitelist / Blacklist For Guilds (Current Is " + (isWhitelist ? "Whitelist" : "Blacklist") + ")");
        guildWhitelist.setOnClickListener((v) -> {
            sqlite = new SQLite(requireContext());
            sqlite.setBoolSetting("whitelist", !isWhitelist);
            sqlite.close();
            isWhitelist = !isWhitelist;
            Utils.showToast("Guilds will now need to be " + (isWhitelist ? "whitelisted" : "blacklisted"));
            dismiss();
        });

        Button channelWhitelist = new Button(context);
        channelWhitelist.setText("Toggle Whitelist / Blacklist For Channels (Current Is " + (isChannelWhitelist ? "Whitelist" : "Blacklist") + ")");
        channelWhitelist.setOnClickListener((v) -> {
            sqlite = new SQLite(requireContext());
            sqlite.setBoolSetting("channelWhitelist", !isChannelWhitelist);
            sqlite.close();
            isChannelWhitelist = !isChannelWhitelist;
            Utils.showToast("Channels will now need to be " + (isChannelWhitelist ? "whitelisted" : "blacklisted"));
            dismiss();
        });

        CheckedSetting logEditsSwitch = Utils.createCheckedSetting(context, CheckedSetting.ViewType.SWITCH, "Log Edits", "Log edited messages to the database");
        logEditsSwitch.setChecked(logEdits);
        logEditsSwitch.setOnCheckedListener((v) -> {
            sqlite = new SQLite(requireContext());
            sqlite.setBoolSetting("logEdits", !logEdits);
            sqlite.close();
            logEdits = !logEdits;
            Utils.showToast(logEdits ? "Now logging edited messages" : "No longer logging edited messages");
        });

        CheckedSetting logDeletesSwitch = Utils.createCheckedSetting(context, CheckedSetting.ViewType.SWITCH, "Log Deletes", "Log deleted messages to the database");
        logDeletesSwitch.setChecked(logDeletes);
        logDeletesSwitch.setOnCheckedListener((v) -> {
            sqlite = new SQLite(requireContext());
            sqlite.setBoolSetting("logDeletes", !logDeletes);
            sqlite.close();
            logDeletes = !logDeletes;
            Utils.showToast(logDeletes ? "Now logging deleted messages" : "No longer logging deleted messages");
        });

        TextView database = new TextView(context, null, 0, com.lytefast.flexinput.R.i.UiKit_Settings_Item_Header);
        database.setText("Database");
        database.setGravity(Gravity.CENTER_HORIZONTAL);

        Button exportDatabase = new Button(context);
        exportDatabase.setText("Export Database");
        exportDatabase.setOnClickListener((v) -> {
            sqlite = new SQLite(requireContext());
            sqlite.exportDatabase();
            sqlite.close();
        });

        Button importDatabase = new Button(context);
        importDatabase.setText("Import Database (Requires Restart)");
        importDatabase.setOnClickListener((v) -> {
            sqlite = new SQLite(requireContext());
            sqlite.importDatabase();
            sqlite.close();
        });

        DangerButton clearEditLogs = new DangerButton(context);
        clearEditLogs.setText("Clear Edit Logs (Requires Restart)");
        clearEditLogs.setOnClickListener((v) -> {
            ConfirmDialog confirmDelete = new ConfirmDialog()
                .setTitle("Clear All Edited Messages?")
                .setIsDangerous(true)
                .setDescription("Are you sure you want to clear all edited messages from the logs?");
            confirmDelete.setOnOkListener((v_) -> {
                sqlite = new SQLite(requireContext());
                sqlite.clearEditedMessages();
                sqlite.close();
                Utils.showToast("Cleared all edited messages from the database (restart required)");
                confirmDelete.dismiss();
                dismiss();
            });
            confirmDelete.setOnCancelListener((v_) -> confirmDelete.dismiss());
            confirmDelete.show(getParentFragmentManager(), "ClearEditedMessages");
        });
        DangerButton clearDeleteLogs = new DangerButton(context);
        clearDeleteLogs.setText("Clear Delete Logs (Requires Restart)");
        clearDeleteLogs.setOnClickListener((v) -> {
            ConfirmDialog confirmDelete = new ConfirmDialog()
                .setTitle("Clear All Deleted Messages?")
                .setIsDangerous(true)
                .setDescription("Are you sure you want to clear all deleted messages from the logs?");
            confirmDelete.setOnOkListener((v_) -> {
                sqlite = new SQLite(requireContext());
                sqlite.clearDeletedMessages();
                sqlite.close();
                Utils.showToast("Cleared all deleted messages from the database (restart required)");
                confirmDelete.dismiss();
                dismiss();
            });
            confirmDelete.setOnCancelListener((v_) -> confirmDelete.dismiss());
            confirmDelete.show(getParentFragmentManager(), "ClearDeletedMessages");
        });
        DangerButton clearGuilds = new DangerButton(context);
        clearGuilds.setText("Clear Guild " + (isWhitelist ? "Whitelist" : "Blacklist") + " (Requires Restart)");
        clearGuilds.setOnClickListener((v) -> {
            ConfirmDialog confirmDelete = new ConfirmDialog()
                .setTitle("Clear Guild " + (isWhitelist ? "Whitelist" : "Blacklist"))
                .setIsDangerous(true)
                .setDescription("Are you sure you want to clear all " + (isWhitelist ? "whitelisted" : "blacklisted") + " guilds?");
            confirmDelete.setOnOkListener((v_) -> {
                sqlite = new SQLite(requireContext());
                sqlite.clearGuilds();
                sqlite.close();
                Utils.showToast("Cleared all " + (isWhitelist ? "whitelisted" : "blacklisted") + " guilds from the database");
                confirmDelete.dismiss();
                dismiss();
            });
            confirmDelete.setOnCancelListener((v_) -> confirmDelete.dismiss());
            confirmDelete.show(getParentFragmentManager(), "ClearGuilds");
        });
        DangerButton clearChannels = new DangerButton(context);
        clearChannels.setText("Clear Channel " + (isWhitelist ? "Whitelist" : "Blacklist") + " (Requires Restart)");
        clearChannels.setOnClickListener((v) -> {
            ConfirmDialog confirmDelete = new ConfirmDialog()
                .setTitle("Clear Channel " + (isChannelWhitelist ? "Whitelist" : "Blacklist"))
                .setIsDangerous(true)
                .setDescription("Are you sure you want to clear all " + (isChannelWhitelist ? "whitelisted" : "blacklisted") + " channels?");
            confirmDelete.setOnOkListener((v_) -> {
                sqlite = new SQLite(requireContext());
                sqlite.clearChannels();
                sqlite.close();
                Utils.showToast("Cleared all " + (isChannelWhitelist ? "whitelisted" : "blacklisted") + " channels from the database");
                confirmDelete.dismiss();
                dismiss();
            });
            confirmDelete.setOnCancelListener((v_) -> confirmDelete.dismiss());
            confirmDelete.show(getParentFragmentManager(), "ClearChannels");
        });
        addView(title);
        addView(logging);
        addView(guildWhitelist);
        addView(channelWhitelist);
        addView(logEditsSwitch);
        addView(logDeletesSwitch);
        addView(database);
        addView(exportDatabase);
        addView(importDatabase);
        addView(clearEditLogs);
        addView(clearDeleteLogs);
        addView(clearGuilds);
        addView(clearChannels);
    }
}
