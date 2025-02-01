package com.example.chatterplay.data_class




enum class AlertType (val string: String) {
    none("none"),
    new_player("new_player"),
    game("game"),
    game_results("game_results"),
    ranking("ranking"),
    rank_results("rank_results"),
    blocking("blocking")
}