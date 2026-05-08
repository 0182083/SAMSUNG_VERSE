package com.safayet.samsungverse

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ClubWelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val clubName = intent.getStringExtra("club_name") ?: "Our Club"

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setBackgroundColor(Color.WHITE)
        layout.gravity = Gravity.TOP
        layout.setPadding(32, 200, 32, 32) // Text appears lower

        val tv = TextView(this)
        tv.text = "Welcome to $clubName"
        tv.textSize = 28f
        tv.setTextColor(Color.BLACK)
        tv.gravity = Gravity.CENTER_HORIZONTAL

        layout.addView(tv)
        setContentView(layout)
    }
}
