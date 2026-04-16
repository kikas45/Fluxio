package com.example.inprideexchange.ui.exploreScreenFeature

import com.example.inprideexchange.Utils.Constants

sealed class ExploreTabItem(
    val route: String,
    val title: String
) {

    object Following : ExploreTabItem(
        route = Constants.Following,
        title = "Following"
    )

    object ForYou : ExploreTabItem(
        route = Constants.ForYou,
        title = "For You"
    )


    object Search : ExploreTabItem(
        route = Constants.Search,
        title = "Search"
    )
}