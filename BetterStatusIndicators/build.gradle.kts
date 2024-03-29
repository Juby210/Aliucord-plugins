version = "1.0.5"
description = "Adds a lot of customization for status indicators (platform indicators, radial status, etc)."

aliucord {
    author("Butterfly3ffect", 575606699553980430L)

    changelog.set("""
        Known issues: {fixed}
        ======================

        * avatar cutout is almost always circular
        * scaling bug in dms list (fixes itself when you start scrolling)
        * animated avatars with **AlwaysAnimate** are always square

        1.0.5
        ======================

        * add support for friends list

        1.0.4
        ======================

        * add option to disable radial status in selected areas
        * add options to change size of indicators

        1.0.3
        ======================

        * fix default online color to match Discord's
        * fix radial status for offline members on members list
        * add option to change status colors
        * fix wrong size of filled colors status in user profile (not tested)

        1.0.1 & 1.0.2
        ======================

        * use hacky fix for Themer plugin users
    """.trimIndent())
}
