package safayet.samsungverse
import com.safayet.samsungverse.MainActivity
import com.safayet.samsungverse.R
import android.Manifest
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment

import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.safayet.samsungverse.AboutActivity
import com.safayet.samsungverse.FollowersActivity
import com.safayet.samsungverse.FollowingActivity
import com.safayet.samsungverse.PostsActivity
import com.safayet.samsungverse.SettingsActivity
import java.util.*

class MeFragment : Fragment(R.layout.fragment_me) {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private lateinit var imgProfile: ImageView
    private lateinit var tvUsername: TextView
    private lateinit var btnLogout: Button

    // Image picker
    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                imgProfile.setImageURI(it)
                uploadProfileImage(it)
            }
        }

    // Permission launcher
    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                imagePickerLauncher.launch("image/*")
            } else {
                Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        imgProfile = view.findViewById(R.id.imgProfile)
        tvUsername = view.findViewById(R.id.tvUsername)
        btnLogout = view.findViewById(R.id.btnLogout)

        val rowSettings = view.findViewById<TextView>(R.id.rowSettings)
        val rowUpdate = view.findViewById<TextView>(R.id.rowUpdate)
        val rowAbout = view.findViewById<TextView>(R.id.rowAbout)

        val rowPosts = view.findViewById<TextView>(R.id.rowPosts)
        val rowMyClub = view.findViewById<TextView>(R.id.rowMyClub)
        val rowFollowers = view.findViewById<TextView>(R.id.rowFollowers)
        val rowFollowing = view.findViewById<TextView>(R.id.rowFollowing)

        val layoutDarkMode = view.findViewById<LinearLayout>(R.id.layoutDarkMode)
        val tvDarkMode = view.findViewById<TextView>(R.id.tvDarkMode)
        val imgDarkMode = view.findViewById<ImageView>(R.id.imgDarkMode)

        val prefs = requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        var isDark = prefs.getBoolean("dark_mode", false)
        updateDarkUI(isDark, tvDarkMode, imgDarkMode)

        val user = auth.currentUser ?: return
        val uid = user.uid

        // Load user data
        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                val username = doc.getString("username")
                tvUsername.text = formatUsername(username)

                val imageUrl = doc.getString("profileImage")
                if (!imageUrl.isNullOrEmpty()) {
                    Glide.with(requireContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_profile_user)
                        .error(R.drawable.ic_profile_user)
                        .circleCrop()
                        .into(imgProfile)
                } else {
                    imgProfile.setImageResource(R.drawable.ic_profile_user)
                }
            }

        // Profile image click
        imgProfile.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
            } else {
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        tvUsername.setOnClickListener { showEditUsernameDialog() }

        btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(requireContext(), MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        rowSettings.setOnClickListener {
            startActivity(Intent(requireContext(), SettingsActivity::class.java))
        }

        rowAbout.setOnClickListener {
            startActivity(Intent(requireContext(), AboutActivity::class.java))
        }

        rowUpdate.setOnClickListener {
            Toast.makeText(requireContext(), "You are using the latest version", Toast.LENGTH_SHORT).show()
        }

        // Posts → নতুন page
        rowPosts.setOnClickListener {
            startActivity(Intent(requireContext(), PostsActivity::class.java))
        }

        // Followers
        rowFollowers.setOnClickListener {
            startActivity(Intent(requireContext(), FollowersActivity::class.java))
        }

        // Following
        rowFollowing.setOnClickListener {
            startActivity(Intent(requireContext(), FollowingActivity::class.java))
        }

        // My Club
        rowMyClub.setOnClickListener {
            showCreateClubDialog()
        }

        layoutDarkMode.setOnClickListener {
            isDark = !isDark
            prefs.edit().putBoolean("dark_mode", isDark).apply()
            AppCompatDelegate.setDefaultNightMode(
                if (isDark) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
            updateDarkUI(isDark, tvDarkMode, imgDarkMode)
        }
    }

    private fun showCreateClubDialog() {
        val editText = EditText(requireContext())
        editText.hint = "Enter club name"

        AlertDialog.Builder(requireContext())
            .setTitle("Create Club")
            .setView(editText)
            .setPositiveButton("Public") { _, _ ->
                val clubName = editText.text.toString().trim()
                if (clubName.isNotEmpty()) {
                    createClub(clubName)
                } else {
                    Toast.makeText(requireContext(), "Enter club name", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun createClub(name: String) {
        val data = hashMapOf(
            "name" to name,
            "timestamp" to System.currentTimeMillis()
        )

        FirebaseFirestore.getInstance()
            .collection("clubs")
            .add(data)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Club created", Toast.LENGTH_SHORT).show()
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, ClubFragment())
                    .commit()
            }
    }

    private fun updateDarkUI(isDark: Boolean, tv: TextView, img: ImageView) {
        if (isDark) {
            tv.text = "Dark mode on"
            img.setColorFilter(0xFFFFFFFF.toInt())
        } else {
            tv.text = "Dark mode off"
            img.setColorFilter(0xFF333333.toInt())
        }
    }

    private fun uploadProfileImage(imageUri: Uri) {
        val uid = auth.currentUser?.uid ?: return

        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Uploading...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        val imageRef = storage.reference.child("profileImages/$uid.jpg")

        imageRef.putFile(imageUri)
            .continueWithTask { task ->
                if (!task.isSuccessful) throw task.exception ?: Exception("Upload failed")
                imageRef.downloadUrl
            }
            .addOnSuccessListener { downloadUrl ->
                db.collection("users")
                    .document(uid)
                    .update("profileImage", downloadUrl.toString())
                    .addOnSuccessListener {
                        Glide.with(requireContext())
                            .load(downloadUrl.toString())
                            .placeholder(R.drawable.ic_profile_user)
                            .error(R.drawable.ic_profile_user)
                            .circleCrop()
                            .into(imgProfile)

                        progressDialog.dismiss()
                        Toast.makeText(requireContext(), "Profile image updated", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
            }
    }

    private fun showEditUsernameDialog() {
        val editText = EditText(requireContext())
        editText.setText(tvUsername.text.toString())
        editText.setSelection(editText.text.length)

        AlertDialog.Builder(requireContext())
            .setTitle("Edit username")
            .setView(editText)
            .setPositiveButton("Save") { _, _ ->
                val newName = editText.text.toString().trim()
                if (newName.length >= 3) saveUsername(newName)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveUsername(name: String) {
        val uid = auth.currentUser!!.uid
        val formatted = formatUsername(name)

        db.collection("users")
            .document(uid)
            .set(mapOf("username" to formatted), SetOptions.merge())
            .addOnSuccessListener {
                tvUsername.text = formatted
            }
    }

    private fun formatUsername(name: String?): String {
        if (name.isNullOrEmpty()) return "User"
        return name.lowercase(Locale.getDefault()).replaceFirstChar { it.uppercase() }
    }
}
