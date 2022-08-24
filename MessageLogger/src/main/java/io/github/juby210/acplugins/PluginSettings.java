package io.github.juby210.acplugins;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.aliucord.Utils;
import com.aliucord.fragments.ConfirmDialog;
import com.aliucord.views.Button;
import com.aliucord.views.DangerButton;
import com.aliucord.widgets.BottomSheet;

public class PluginSettings extends BottomSheet {

    public PluginSettings() {}

    Boolean isWhitelist;
    Boolean isChannelWhitelist;

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        SQLite sqlite = new SQLite(requireContext());

        isWhitelist = sqlite.getBoolSetting("whitelist", true);
        isChannelWhitelist = sqlite.getBoolSetting("channelWhitelist", false);
        var context = requireContext();
        setPadding(20);

        TextView title = new TextView(context, null, 0, com.lytefast.flexinput.R.i.UiKit_Settings_Item_Header);
        title.setText("Message Logger");
        title.setGravity(Gravity.START);

        Button guildWhitelist = new Button(context);
        guildWhitelist.setText("Toggle Whitelist / Blacklist For Guilds");
        guildWhitelist.setOnClickListener((v) -> {
            sqlite.setBoolSetting("whitelist", !isWhitelist);
            isWhitelist = !isWhitelist;
            Utils.showToast("Guilds will now need to be " + (isWhitelist ? "whitelisted" : "blacklisted"));
        });

        Button channelWhitelist = new Button(context);
        channelWhitelist.setText("Toggle Whitelist / Blacklist For Channels");
        channelWhitelist.setOnClickListener((v) -> {
            sqlite.setBoolSetting("channelWhitelist", !isChannelWhitelist);
            isChannelWhitelist = !isChannelWhitelist;
            Utils.showToast("Channels will now need to be " + (isChannelWhitelist ? "whitelisted" : "blacklisted"));
        });

        DangerButton clearEditLogs = new DangerButton(context);
        clearEditLogs.setText("Clear Edit Logs");
        clearEditLogs.setOnClickListener((v) -> {
            ConfirmDialog confirmDelete = new ConfirmDialog()
                .setTitle("Clear All Edited Messages?")
                .setIsDangerous(true)
                .setDescription("Are you sure you want to clear all edited messages from the logs?");
            confirmDelete.setOnOkListener((v_) -> {
                sqlite.clearEditedMessages();
                Utils.showToast("Cleared all edited messages from the database (restart required)");
                confirmDelete.dismiss();
                dismiss();
            });
            confirmDelete.setOnCancelListener((v_) -> confirmDelete.dismiss());
            confirmDelete.show(getParentFragmentManager(), "ClearEditedMessages");
        });
        DangerButton clearDeleteLogs = new DangerButton(context);
        clearDeleteLogs.setText("Clear Delete Logs");
        clearDeleteLogs.setOnClickListener((v) -> {
            ConfirmDialog confirmDelete = new ConfirmDialog()
                .setTitle("Clear All Deleted Messages?")
                .setIsDangerous(true)
                .setDescription("Are you sure you want to clear all deleted messages from the logs?");
            confirmDelete.setOnOkListener((v_) -> {
                sqlite.clearDeletedMessages();
                Utils.showToast("Cleared all deleted messages from the database (restart required)");
                confirmDelete.dismiss();
                dismiss();
            });
            confirmDelete.setOnCancelListener((v_) -> confirmDelete.dismiss());
            confirmDelete.show(getParentFragmentManager(), "ClearDeletedMessages");
        });
        DangerButton clearGuilds = new DangerButton(context);
        clearGuilds.setText("Clear Guild " + (isWhitelist ? "Whitelist" : "Blacklist"));
        clearGuilds.setOnClickListener((v) -> {
            ConfirmDialog confirmDelete = new ConfirmDialog()
                .setTitle("Clear Guild " + (isWhitelist ? "Whitelist" : "Blacklist"))
                .setIsDangerous(true)
                .setDescription("Are you sure you want to clear all " + (isWhitelist ? "whitelisted" : "blacklisted") + " guilds?");
            confirmDelete.setOnOkListener((v_) -> {
                sqlite.clearGuilds();
                Utils.showToast("Cleared all " + (isWhitelist ? "whitelisted" : "blacklisted") + " guilds from the database (restart required)");
                confirmDelete.dismiss();
                dismiss();
            });
            confirmDelete.setOnCancelListener((v_) -> confirmDelete.dismiss());
            confirmDelete.show(getParentFragmentManager(), "ClearGuilds");
        });
        DangerButton clearChannels = new DangerButton(context);
        clearChannels.setText("Clear Channel " + (isWhitelist ? "Whitelist" : "Blacklist"));
        clearChannels.setOnClickListener((v) -> {
            ConfirmDialog confirmDelete = new ConfirmDialog()
                .setTitle("Clear Channel " + (isChannelWhitelist ? "Whitelist" : "Blacklist"))
                .setIsDangerous(true)
                .setDescription("Are you sure you want to clear all " + (isChannelWhitelist ? "whitelisted" : "blacklisted") + " channels?");
            confirmDelete.setOnOkListener((v_) -> {
                sqlite.clearChannels();
                Utils.showToast("Cleared all " + (isChannelWhitelist ? "whitelisted" : "blacklisted") + " channels from the database (restart required)");
                confirmDelete.dismiss();
                dismiss();
            });
            confirmDelete.setOnCancelListener((v_) -> confirmDelete.dismiss());
            confirmDelete.show(getParentFragmentManager(), "ClearChannels");
        });
        addView(title);
        addView(guildWhitelist);
        addView(channelWhitelist);
        addView(clearEditLogs);
        addView(clearDeleteLogs);
        addView(clearGuilds);
        addView(clearChannels);
    }
}
