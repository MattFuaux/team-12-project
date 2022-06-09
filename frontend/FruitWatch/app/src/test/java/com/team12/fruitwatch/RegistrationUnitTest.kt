package com.team12.fruitwatch

import com.team12.fruitwatch.controllers.NetworkRequestController
import com.team12.fruitwatch.data.model.LoggedInUser
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito
import org.mockito.Mockito.mock

/**
 * Registration related local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class RegistrationUnitTest {

    val correctFirstname = "Jane"
    val correctSurname = "Doe"
    val correctEmail = "janedoe@mailbox.com"
    val correctPassword = "janedoe"

    lateinit var networkRequestController: NetworkRequestController

    @Before
    fun setup(){
        networkRequestController = Mockito.mock(NetworkRequestController::class.java)
        given(networkRequestController.registerUser(correctFirstname,correctSurname,correctEmail,correctPassword)).willReturn(
            NetworkRequestController.ResponseData(200,LoggedInUser("1","Jane Doe","jwt=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE2NTQ0OTg1MjIsImlzcyI6IjIifQ.bPsJ3s5t7EtRc-BfImZlqIo45tbYLo6IGTZpHNpb0QA"),null)
        )
        given(networkRequestController.registerUser("",correctSurname,correctEmail,correctPassword)).willReturn(
            NetworkRequestController.ResponseData(400,null,null)
        )
        given(networkRequestController.registerUser(correctFirstname,"",correctEmail,correctPassword)).willReturn(
            NetworkRequestController.ResponseData(400,null,null)
        )
        given(networkRequestController.registerUser(correctFirstname,correctSurname,"",correctPassword)).willReturn(
            NetworkRequestController.ResponseData(400,null,null)
        )
        given(networkRequestController.registerUser(correctFirstname,correctSurname,correctEmail,"")).willReturn(
            NetworkRequestController.ResponseData(400,null,null)
        )
    }

    @Test
    fun registrationSuccess() {
        val result = networkRequestController.registerUser(correctFirstname,correctSurname,correctEmail,correctPassword)
        assertEquals(200,result.statusCode)
    }

    @Test
    fun registrationMissingFirstname() {
        val result = networkRequestController.registerUser("",correctSurname,correctEmail,correctPassword)
        assertEquals(400,result.statusCode)
    }

    @Test
    fun registrationMissingSurname() {
        val result = networkRequestController.registerUser(correctFirstname,"",correctEmail,correctPassword)
        assertEquals(400,result.statusCode)
    }

    @Test
    fun registrationMissingEmail() {
        val result = networkRequestController.registerUser(correctFirstname,correctSurname,"",correctPassword)
        assertEquals(400,result.statusCode)
    }

    @Test
    fun registrationMissingPassword() {
        val result = networkRequestController.registerUser(correctFirstname,correctSurname,correctEmail,"")
        assertEquals(400,result.statusCode)
    }


}