package safayet.samsungverse
import com.safayet.samsungverse.R
import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class DiscoverFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private val posts = mutableListOf<PostModel>()
    private val db by lazy { FirebaseFirestore.getInstance() }
    private var listener: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        recyclerView = RecyclerView(requireContext())
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = PostAdapter(posts)
        return recyclerView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listener = db.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot == null) return@addSnapshotListener
                posts.clear()

                val tempList = mutableListOf<PostModel>()

                for (doc in snapshot.documents) {
                    val uid = doc.getString("uid") ?: continue
                    val content = doc.getString("content") ?: ""
                    val time = doc.getLong("timestamp") ?: System.currentTimeMillis()

                    db.collection("users").document(uid).get()
                        .addOnSuccessListener { userDoc ->
                            val username = userDoc.getString("username") ?: "User"
                            tempList.add(PostModel(username, content, time))

                            // Sort newest post on top
                            val sortedList = tempList.sortedByDescending { it.timestamp }
                            posts.clear()
                            posts.addAll(sortedList)
                            recyclerView.adapter?.notifyDataSetChanged()
                        }
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listener?.remove()
    }

    data class PostModel(val username: String, val content: String, val timestamp: Long)

    class PostAdapter(private val posts: List<PostModel>) :
        RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

        class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvUser: TextView = view.findViewById(R.id.tvPostUser)
            val tvContent: TextView = view.findViewById(R.id.tvPostContent)
            val tvTime: TextView = view.findViewById(R.id.tvPostTime)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.post_item, parent, false)
            return PostViewHolder(view)
        }

        override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
            val post = posts[position]
            holder.tvUser.text = post.username
            holder.tvContent.text = post.content
            holder.tvTime.text =
                DateUtils.getRelativeTimeSpanString(post.timestamp)
        }

        override fun getItemCount(): Int = posts.size
    }
}
