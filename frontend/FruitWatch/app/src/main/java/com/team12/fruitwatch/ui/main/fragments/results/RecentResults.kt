package com.team12.fruitwatch.ui.main.fragments.results

import com.team12.fruitwatch.controllers.NetworkRequestController
import com.team12.fruitwatch.database.entities.PastSearch

// This keeps the most recent search results and past search available to any
// class that needs it so screens can be loaded and re-loaded at will
object RecentResults {
    var mostRecentSearchResults : NetworkRequestController.SearchResults? = null
    var mostRecentPastSearch : PastSearch? = null
}