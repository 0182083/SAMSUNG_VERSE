package safayet.samsungverse
import com.safayet.samsungverse.R
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.safayet.samsungverse.ClubWelcomeActivity

class ClubFragment : Fragment(R.layout.fragment_club) {

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val container = view.findViewById<LinearLayout>(R.id.clubContainer)

        val isDarkMode = resources.configuration.uiMode and 0x30 == 0x20

        FirebaseFirestore.getInstance()
            .collection("clubs")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->
                if (error != null) { error.printStackTrace(); return@addSnapshotListener }
                if (snapshot == null) return@addSnapshotListener

                container.removeAllViews()

                for ((index, doc) in snapshot.documents.reversed().withIndex()) {
                    val name = doc.getString("name") ?: continue
                    val joinedUsers = doc.get("joinedUsers") as? List<*> ?: emptyList<Any>()

                    // Card layout
                    val boxLayout = LinearLayout(requireContext())
                    boxLayout.orientation = LinearLayout.VERTICAL
                    boxLayout.setPadding(32, 32, 32, 32)
                    boxLayout.setMargins(0, 0, 0, 24)
                    boxLayout.elevation = 8f
                    boxLayout.isClickable = true
                    boxLayout.isFocusable = true
                    boxLayout.foreground = ContextCompat.getDrawable(requireContext(), android.R.drawable.list_selector_background)

                    val bgColors = if(isDarkMode) intArrayOf(Color.parseColor("#2C2C2C"), Color.parseColor("#3A3A3A"))
                    else intArrayOf(Color.parseColor("#FFFFFF"), Color.parseColor("#F8F8F8"))
                    val gradient = GradientDrawable(GradientDrawable.Orientation.TL_BR, bgColors)
                    gradient.cornerRadius = 24f
                    boxLayout.background = gradient

                    // Animate card
                    boxLayout.translationY = 100f
                    boxLayout.alpha = 0f
                    boxLayout.animate().alpha(1f).translationY(0f).setStartDelay((index*100).toLong()).setDuration(400).setInterpolator(AccelerateDecelerateInterpolator()).start()

                    // Horizontal layout for icon + name
                    val titleLayout = LinearLayout(requireContext())
                    titleLayout.orientation = LinearLayout.HORIZONTAL
                    titleLayout.gravity = Gravity.CENTER_VERTICAL

                    val icon = ImageView(requireContext())
                    icon.setImageResource(android.R.drawable.ic_menu_myplaces)
                    icon.setColorFilter(if(isDarkMode) Color.parseColor("#BB86FC") else Color.parseColor("#6200EE"))
                    icon.layoutParams = LinearLayout.LayoutParams(80, 80)
                    icon.setPadding(0,0,24,0)
                    titleLayout.addView(icon)

                    val tv = TextView(requireContext())
                    tv.text = name
                    tv.textSize = 20f
                    tv.setTextColor(if(isDarkMode) Color.WHITE else Color.BLACK)
                    tv.gravity = Gravity.CENTER_VERTICAL
                    titleLayout.addView(tv)

                    boxLayout.addView(titleLayout)

                    // Buttons layout
                    val buttonLayout = LinearLayout(requireContext())
                    buttonLayout.orientation = LinearLayout.HORIZONTAL
                    buttonLayout.setPadding(0,24,0,0)

                    // Public/Join button (Crash-safe)
                    val joinBtn = MaterialButton(requireContext())
                    joinBtn.textSize = 16f
                    joinBtn.setTextColor(Color.WHITE)
                    joinBtn.iconPadding = 8
                    joinBtn.rippleColor = ContextCompat.getColorStateList(requireContext(), android.R.color.darker_gray)
                    val joinGradient = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                        intArrayOf(Color.parseColor("#8E2DE2"), Color.parseColor("#4A00E0")))
                    joinGradient.cornerRadius = 16f
                    joinBtn.background = joinGradient

                    // View Group button
                    val viewBtn = MaterialButton(requireContext())
                    viewBtn.textSize = 16f
                    viewBtn.text = "View Group"
                    viewBtn.setTextColor(Color.WHITE)
                    viewBtn.iconPadding = 8
                    val viewGradient = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                        intArrayOf(Color.parseColor("#00C6FF"), Color.parseColor("#0072FF")))
                    viewGradient.cornerRadius = 16f
                    viewBtn.background = viewGradient
                    viewBtn.elevation = 4f

                    // Determine join state
                    var isJoined = currentUserId != null && joinedUsers.contains(currentUserId)
                    joinBtn.text = if(isJoined) "Joined" else "Join"
                    viewBtn.isEnabled = isJoined

                    // Crash-safe Public/Join button logic
                    joinBtn.setOnClickListener {
                        val uid = currentUserId ?: run {
                            context?.let { ctx -> Toast.makeText(ctx, "Please login first", Toast.LENGTH_SHORT).show() }
                            return@setOnClickListener
                        }
                        if (!isAdded) return@setOnClickListener

                        val docRef = FirebaseFirestore.getInstance().collection("clubs").document(doc.id)

                        if (!isJoined) {
                            docRef.update("joinedUsers", FieldValue.arrayUnion(uid))
                                .addOnSuccessListener {
                                    if (isAdded) {
                                        isJoined = true
                                        joinBtn.text = "Joined"
                                        viewBtn.isEnabled = true
                                        context?.let { ctx -> Toast.makeText(ctx, "You joined $name!", Toast.LENGTH_SHORT).show() }
                                    }
                                }
                        } else {
                            docRef.update("joinedUsers", FieldValue.arrayRemove(uid))
                                .addOnSuccessListener {
                                    if (isAdded) {
                                        isJoined = false
                                        joinBtn.text = "Join"
                                        viewBtn.isEnabled = false
                                        context?.let { ctx -> Toast.makeText(ctx, "You unjoined $name!", Toast.LENGTH_SHORT).show() }
                                    }
                                }
                        }
                    }

                    // View Group button click (only if joined)
                    viewBtn.setOnClickListener {
                        if (isJoined && isAdded) {
                            val intent = Intent(requireContext(), ClubWelcomeActivity::class.java)
                            intent.putExtra("club_name", name)
                            startActivity(intent)
                        }
                    }

                    val joinParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    joinParams.setMargins(0,0,16,0)
                    joinBtn.layoutParams = joinParams

                    buttonLayout.addView(joinBtn)
                    buttonLayout.addView(viewBtn)
                    boxLayout.addView(buttonLayout)

                    container.addView(boxLayout)
                }
            }
    }

    private fun LinearLayout.setMargins(left:Int, top:Int, right:Int, bottom:Int){
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        params.setMargins(left,top,right,bottom)
        layoutParams=params
    }
}
