version = "1.1.5"
description = "Saves deleted / edited messages to a SQLite database and restores them when the channel is opened."

aliucord.changelog.set("""
    1.1.5
    ======================

    * fix crash when exporting database
    * fix not being able to toggle logging for channels
    * SQLite optimization

    1.1.4
    ======================

    * fix setting not saving correctly

    1.1.3
    ======================

    * fix spamming debug logs with NPE

    1.1.2
    ======================

    * fix rare reply crash
    * fix re-adding edit record to the database instead of updating

    1.1.1
    ======================

    * try to fix loading old messages

    1.1.0
    ======================

    * Fix various memory leaks
    * Fix some thread safety bugs

    1.0.0
    ======================

    * Added SQLite saving for deleted messages and edit history
    * Added the ability to clear data in plugin settings
    * Added guild whitelist / blacklist (default is blacklist) and channel whitelist / blacklist (default is blacklist)
    * Added the ability to import / export the database to / from the Aliucord folder
    * Added the ability to toggle logging deleted messages and toggle logging edited messages
""".trimIndent())
