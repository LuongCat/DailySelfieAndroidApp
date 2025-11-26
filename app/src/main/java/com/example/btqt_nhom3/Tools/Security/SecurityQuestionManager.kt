import android.content.Context
import androidx.core.content.edit

object SecurityQuestionManager {

    private const val PREF = "security_question"
    private const val KEY_Q = "question"
    private const val KEY_A = "answer"

    fun save(context: Context, question: String, answer: String) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit {
                putString(KEY_Q, question)
                    .putString(KEY_A, answer.lowercase().trim())
            }
    }

    fun getQuestion(context: Context): String? =
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY_Q, null)

    fun checkAnswer(context: Context, answer: String): Boolean {
        val saved = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY_A, null)
        return saved != null && saved == answer.lowercase().trim()
    }
}
