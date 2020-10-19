package festusyuma.com.glaid

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import festusyuma.com.glaid.request.LoadingAndErrorHandler


class MainActivity : AppCompatActivity() {

    private val rcSignIn: Int = 55
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var loadingError: LoadingAndErrorHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.server_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        loadingError = LoadingAndErrorHandler(this)
    }

    fun signInWithGoogle(view: View) {
        if (!loadingError.operationRunning) {
            loadingError.setLoading(true)

            val account = GoogleSignIn.getLastSignedInAccount(this)
            if (account == null) {
                mGoogleSignInClient.signOut()
                    .addOnCompleteListener{ startGoogleSignInActivity() }
            }else startGoogleSignInActivity()
        }
    }

    private fun startGoogleSignInActivity() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, rcSignIn)
        loadingError.setLoading(false)
    }

    fun signUpWithMail(view: View){
        val signUpMailIntent = Intent(this, SignUpActivity::class.java)
        startActivity(signUpMailIntent)
    }

    fun signInMethod(view: View){
        val signInIntent = Intent(this, LogInActivity::class.java)
        startActivity(signInIntent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == rcSignIn) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            Log.v(API_LOG_TAG, "id token ${account?.idToken}")
        } catch (e: ApiException) {
            Log.w(API_LOG_TAG, "signInResult:failed code=" + e.statusCode)
        }
    }
}
