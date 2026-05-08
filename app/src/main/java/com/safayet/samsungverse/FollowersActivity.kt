package com.safayet.samsungverse

import android.os.Bundle
import android.view.Gravity
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class FollowersActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tv = TextView(this)
        tv.text = "Coming Soon"
        tv.textSize = 24f
        tv.gravity = Gravity.CENTER

        setContentView(tv)
    }
}
