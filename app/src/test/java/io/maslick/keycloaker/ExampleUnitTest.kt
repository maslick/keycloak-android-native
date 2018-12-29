package io.maslick.keycloaker

import io.maslick.keycloaker.helper.Helper
import junit.framework.Assert
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    private val jwtToken = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJrLWFHSVJoUVMtR0NKU3JxWkRXQlY4cGZGazNHbFpzWW5fci1JbEtFQzhNIn0.eyJqdGkiOiI1MWU2MzdiYy01MDg4LTQxNmUtOWIwMi1hZTJhZGM4ZDI3YTgiLCJleHAiOjE1NDYwNzMyMzcsIm5iZiI6MCwiaWF0IjoxNTQ2MDcyOTM3LCJpc3MiOiJodHRwczovL2FjdGl2ZWNsb3VkZXIuaWpzLnNpL2F1dGgvcmVhbG1zL2JhcmtvZGVyIiwiYXVkIjoiYmFya29kZXItZnJvbnRlbmQiLCJzdWIiOiJkM2EwOTg5Ny0zNDlhLTRiOTItYTk2Yi04ODhlYWUxZDIzMTgiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJiYXJrb2Rlci1mcm9udGVuZCIsIm5vbmNlIjoiZjA0ODQ0MzYtM2NkNi00OTExLTg3YWEtNWRmZGI4N2YxMGRlIiwiYXV0aF90aW1lIjoxNTQ2MDcyNjQ4LCJzZXNzaW9uX3N0YXRlIjoiMmE3ODQ3M2YtMWU4OS00Yjk5LTg3ODYtNzMzZTI0ODlmNDE4IiwiYWNyIjoiMCIsImNsaWVudF9zZXNzaW9uIjoiNzQ5ZTA1ZDQtZWEwOC00NDQwLWFkNDgtYjM2OWZlOTQ0MzM4IiwiYWxsb3dlZC1vcmlnaW5zIjpbIioiXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbImNyYWZ0cm9vbSJdfSwicmVzb3VyY2VfYWNjZXNzIjp7fSwibmFtZSI6IlBhdmVsIE1hc2xvZmYiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJwYXZlbC5tYXNsb2ZmQGdtYWlsLmNvbSIsImdpdmVuX25hbWUiOiJQYXZlbCIsImZhbWlseV9uYW1lIjoiTWFzbG9mZiIsImVtYWlsIjoicGF2ZWwubWFzbG9mZkBnbWFpbC5jb20ifQ.t_IFwsu__L5bIFHHv1NsA0w6Iffet60dSE-pV_-mr5diXR8iNEwUz4Ks39GseihJC2w759Y0w9Bfl5mODgig-RbV_4STTmIuvFcCM2yOJg849107tkYN7f4L8ZfAB9QkGcSBfqxBLuPJpsmYfJaRypn3ZNuIObSPqu066q_pVJC07Ag_UmdEkoYcIfp2CRPZqeKrHkom32oN2j552vSqGNYGot4hshtnR1HcXAmnFxaTaGM8aSea0WlruuCfWA-3ScOkPrV4Zye8Y5-CUti_0uXGT-ZoZZCSvg-XIr3rIlvZIYv6Y2QkGeRtWOpuz-rCuaTgqq7aQUmQOHcqxauBVQ"

    @Test
    fun testJwtParse() {
        val parsed = Helper.parseJwtToken(jwtToken)
        val userId = parsed[0]
        val email = parsed[1]
        val name = parsed[2]
        val surname = parsed[3]
        val roles = parsed[4]

        Assert.assertEquals("d3a09897-349a-4b92-a96b-888eae1d2318", userId)
        Assert.assertEquals("pavel.masloff@gmail.com", email)
        Assert.assertEquals("Pavel", name)
        Assert.assertEquals("Masloff", surname)
        Assert.assertEquals("craftroom", roles)
    }
}
