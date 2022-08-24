version = "1.0.0"
description = "Saves deleted / edited messages to a SQLite database and restores them when the channel is opened."

aliucord.changelog.set("""
    1.0.0
    ======================

    * Added SQLite saving for deleted messages and edit history
    * Added the ability to clear data in plugin settings
    * Added guild whitelist / blacklist (default is whitelist) and channel whitelist / blacklist (default is blacklist)

""".trimIndent())
