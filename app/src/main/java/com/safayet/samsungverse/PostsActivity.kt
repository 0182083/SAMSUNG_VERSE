package com.safayet.samsungverse

import android.app.ProgressDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PostsActivity : AppCompatActivity() {

    private lateinit var etPost: EditText
    private lateinit var btnPublic: Button
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_posts)

        etPost = findViewById(R.id.etPost)
        btnPublic = findViewById(R.id.btnPublic)

        btnPublic.setOnClickListener {
            val user = auth.currentUser
            if (user == null) {
                Toast.makeText(this, "You must be logged in to post", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val content = etPost.text.toString().trim()
            if (content.isEmpty()) {
                Toast.makeText(this, "Enter some text", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val progressDialog = ProgressDialog(this)
            progressDialog.setMessage("Posting...")
            progressDialog.setCancelable(false)
            progressDialog.show()

            val data = hashMapOf(
                "uid" to user.uid,
                "content" to content,
                "timestamp" to System.currentTimeMillis()
            )

            db.collection("posts")
                .add(data)
                .addOnSuccessListener {
                    progressDialog.dismiss()
                    Toast.makeText(this, "Post published!", Toast.LENGTH_SHORT).show()
                    etPost.text.clear()
                    finish() // auto return to previous screen (DiscoverFragment)
                }
                .addOnFailureListener { e ->
                    progressDialog.dismiss()
                    Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }
}
