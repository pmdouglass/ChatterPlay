package com.example.chatterplay.data_class




enum class AlertType (val string: String) {
    none("none"),
    new_player("new_player"),
    fresh_player("fresh_player"),
    game("game"),
    game_results("game_results"),
    ranking("ranking"),
    rank_results("rank_results"),
    top_discuss("top_discuss"),
    blocking("blocking"),
    last_message("last_message")
}