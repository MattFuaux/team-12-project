package com.team12.fruitwatch.ui.main.fragments.results

import com.team12.fruitwatch.controllers.NetworkRequestController
import com.team12.fruitwatch.database.entities.PastSearch

object RecentResults {

    var mostRecentSearchResults : NetworkRequestController.SearchResults? = null
    var mostRecentPastSearch : PastSearch? = null

}