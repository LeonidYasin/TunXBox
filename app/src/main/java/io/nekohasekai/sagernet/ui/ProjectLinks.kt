package io.nekohasekai.sagernet.ui

object ProjectLinks {
    const val SUPPORT_EMAIL = "leonidyasin@gmail.com"
    const val SUPPORT_TELEGRAM = "https://t.me/LEONI_YA"
    const val REPOSITORY = "https://github.com/LeonidYasin/TunXBox"
    const val RELEASES = "$REPOSITORY/releases"
    val PREVIEW_API get() = "https://api.github.com/repos/LeonidYasin/TunXBox/releases/tags/v${io.nekohasekai.sagernet.BuildConfig.VERSION_NAME.substringBefore('-')}-rc"
    const val RELEASE_API = "https://api.github.com/repos/LeonidYasin/TunXBox/releases/latest"
    const val ISSUES = "$REPOSITORY/issues"
    const val DOCUMENTATION = "$REPOSITORY#readme"
    const val UPSTREAM = "https://github.com/MatsuriDayo/NekoBoxForAndroid"
}
