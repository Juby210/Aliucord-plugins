package io.github.juby210.acplugins;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.aliucord.Utils;
import com.aliucord.fragments.ConfirmDialog;
import com.aliucord.views.DangerButton;
import com.aliucord.widgets.BottomSheet;

public class PluginSettings extends BottomSheet {

    public PluginSettings() {}

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        SQLite sqlite = new SQLite(requireContext());

        var context = requireContext();
        setPadding(20);

        TextView title = new TextView(context, null, 0, com.lytefast.flexinput.R.i.UiKit_Settings_Item_Header);
        title.setText("Message Logger");
        title.setGravity(Gravity.START);

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
        addView(title);
        addView(clearEditLogs);
        addView(clearDeleteLogs);
    }
}
