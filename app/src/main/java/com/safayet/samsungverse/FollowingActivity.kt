package com.safayet.samsungverse

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import android.view.Gravity

class FollowingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tv = TextView(this)
        tv.text = "Coming Soon"
        tv.textSize = 24f
        tv.gravity = Gravity.CENTER

        setContentView(tv)
    }
}
