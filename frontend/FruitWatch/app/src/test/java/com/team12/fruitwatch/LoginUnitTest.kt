package com.team12.fruitwatch

import com.team12.fruitwatch.controllers.NetworkRequestController
import com.team12.fruitwatch.data.model.LoggedInUser
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito
import org.mockito.Mockito

/**
 * Login related local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class LoginUnitTest {

    val correctEmail = "janedoe@mailbox.com"
    val correctPassword = "janedoe"

    lateinit var networkRequestController: NetworkRequestController

    @Before
    fun setup(){
        networkRequestController = Mockito.mock(NetworkRequestController::class.java)
        BDDMockito.given(
            networkRequestController.loginUser(
                correctEmail,
                correctPassword
            )
        ).willReturn(
            NetworkRequestController.ResponseData(200,
                LoggedInUser("1","Jane Doe","jwt=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE2NTQ0OTg1MjIsImlzcyI6IjIifQ.bPsJ3s5t7EtRc-BfImZlqIo45tbYLo6IGTZpHNpb0QA"),null)
        )

        BDDMockito.given(
            networkRequestController.loginUser(
                "",
                correctPassword
            )
        ).willReturn(
            NetworkRequestController.ResponseData(400,null,null)
        )
        BDDMockito.given(
            networkRequestController.loginUser(
                correctEmail,
                ""
            )
        ).willReturn(
            NetworkRequestController.ResponseData(400,null,null)
        )
    }

    @Test
    fun loginSuccess() {
        val result = networkRequestController.loginUser(correctEmail,correctPassword)
        assertEquals(200,result.statusCode)
    }


    @Test
    fun loginMissingEmail() {
        val result = networkRequestController.loginUser("",correctPassword)
        assertEquals(400,result.statusCode)
    }

    @Test
    fun loginMissingPassword() {
        val result = networkRequestController.loginUser(correctEmail,"")
        assertEquals(400,result.statusCode)
    }
}