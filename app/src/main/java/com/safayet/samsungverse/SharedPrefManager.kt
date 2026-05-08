package com.safayet.samsungverse

import android.content.Context

class SharedPrefManager(context: Context) {

    private val pref = context.getSharedPreferences("user_pref", Context.MODE_PRIVATE)

    fun saveUser(user: UserData) {
        val editor = pref.edit()
        editor.putString("username", user.username)
        editor.putString("email", user.email)
        editor.putString("password", user.password)
        editor.apply()
    }

    fun getUser(): UserData? {
        val username = pref.getString("username", null)
        val email = pref.getString("email", null)
        val password = pref.getString("password", null)

        return if (username != null && email != null && password != null) {
            UserData(username, email, password)
        } else null
    }
}
