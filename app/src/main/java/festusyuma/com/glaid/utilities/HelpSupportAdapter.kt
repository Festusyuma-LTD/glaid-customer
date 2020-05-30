package festusyuma.com.glaid.utilities

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import festusyuma.com.glaid.R


/**
 * Created by Chidozie Henry on Saturday, May 30, 2020.
 * Email: okebugwuchidozie@gmail.com
 */
class HelpSupportAdapter(
    val context: Context,
    val questions: List<Question>,
    val itemClicked: (Question) -> Unit
) :
    RecyclerView.Adapter<HelpSupportAdapter.Holder>() {

    //Add a view holder
    inner class Holder(itemView: View?, val itemClicked: (Question) -> Unit) :
        RecyclerView.ViewHolder(itemView!!) {
        private val questionText = itemView?.findViewById<TextView>(R.id.helpQuestionBtn)
        fun bindData(context: Context, question: Question) {
            //getting a resource id for use
//            val resourceId =
//                context.resources.getIdentifier(category.image, "drawable", context.packageName)
//            categoryImage?.setImageResource(resourceId)
            questionText?.text = question.title
            itemView.setOnClickListener {
                itemClicked(question)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.help_support_btn, parent, false)
        return Holder(view, itemClicked)
    }

    override fun getItemCount(): Int {
        return questions.count()
    }

    // Bind the inner class Holder here to reuse
    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bindData(context, questions[position])
    }
}