package com.team12.fruitwatch

import com.team12.fruitwatch.controllers.NetworkRequestController
import com.team12.fruitwatch.data.model.LoggedInUser
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito
import org.mockito.Mockito
import java.io.File

/**
 * Search related local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class SearchUnitTest {

    val fileBanana = Mockito.mock(File::class.java)
    val filePomegranate = Mockito.mock(File::class.java)
    val fileMangosteen = Mockito.mock(File::class.java)

    val user :LoggedInUser = LoggedInUser("1","Jane Doe","jwt=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE2NTQ0OTg1MjIsImlzcyI6IjIifQ.bPsJ3s5t7EtRc-BfImZlqIo45tbYLo6IGTZpHNpb0QA")

    lateinit var networkRequestController: NetworkRequestController

    @Before
    fun setup(){
        networkRequestController = Mockito.mock(NetworkRequestController::class.java)
        BDDMockito.given(
            networkRequestController.startSearch(user, fileBanana)
        ).willReturn(
            NetworkRequestController.SearchResults(null,"Banana",null,null,null,null,null,null,null,null,null,null,null)
        )

        BDDMockito.given(
            networkRequestController.startSearch(user, filePomegranate)
        ).willReturn(
            NetworkRequestController.SearchResults(null,"Pomegranate",null,null,null,null,null,null,null,null,null,null,null)
        )
        BDDMockito.given(
            networkRequestController.startSearch(user, fileMangosteen)
        ).willReturn(
            NetworkRequestController.SearchResults(null,"Mangosteen",null,null,null,null,null,null,null,null,null,null,null)
        )
    }

    @Test
    fun loginSuccess() {
        val result = networkRequestController.startSearch(user, fileBanana)
        assertEquals("Banana",result.name)
    }


    @Test
    fun loginMissingEmail() {
        val result = networkRequestController.startSearch(user, filePomegranate)
        assertEquals("Pomegranate",result.name)
    }

    @Test
    fun loginMissingPassword() {
        val result = networkRequestController.startSearch(user, fileMangosteen)
        assertEquals("Mangosteen",result.name)
    }
}