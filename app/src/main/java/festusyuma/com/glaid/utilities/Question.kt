package festusyuma.com.glaid.utilities


/**
 * Created by Chidozie Henry on Saturday, May 30, 2020.
 * Email: okebugwuchidozie@gmail.com
 */
class Question(var id: Int, var title: String, var body: String, var image: String) {
    override fun toString(): String {
        return title
    }
}