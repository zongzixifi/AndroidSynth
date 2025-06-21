package com.example.project2

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

sealed interface NavigationRoute

@Serializable
object FrontScreenPage:NavigationRoute{
    const val PageName = "frontscreen"
}

@Serializable
object ChatScreenPage{
    const val PageName = "chatscreen"
}

@Serializable
object SynthScreenPage{
    const val PageName = "synthScreen"
}

@Serializable
object MusicGenScreenPage{
    const val PageName = "MusicGenScreen"
}

@Serializable
object LoginScreenPage{
    const val PageName = "LoginPage"
}

@Serializable
object TitleSelectScreenPage{
    const val PageName = "TitleSelect"
}


object Destinations{
    val FrontScreenPageRoute = Json.encodeToString(FrontScreenPage)
    val ChatScreenPageRoute = Json.encodeToString(ChatScreenPage)
    val SynthScreenPageRoute = Json.encodeToString(SynthScreenPage)
    val MusicGenScreenPageRoute = Json.encodeToString(MusicGenScreenPage)
    val LoginScreenPageRoute = Json.encodeToString(LoginScreenPage)
    val TitleSelectScreenPageRote = Json.encodeToString(TitleSelectScreenPage)
}
