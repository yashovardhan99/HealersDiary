package com.yashovardhan99.healersdiary.activities

import android.content.Intent
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.fragments.SignInButtonFragment
import com.yashovardhan99.healersdiary.fragments.SignInProgressFragment
import java.util.*

class Login : FragmentActivity() {

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mFirebaseAnalytics: FirebaseAnalytics
    private lateinit var params: Bundle
    private lateinit var signInFragment: Fragment
    private lateinit var signInProgressFragment: Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        params = Bundle()
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        signInProgressFragment = SignInProgressFragment()
        signInFragment = SignInButtonFragment()

        supportFragmentManager.beginTransaction().add(R.id.signInFragmentHolder, signInFragment).commit()

        //configuring google sign in
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        mAuth = FirebaseAuth.getInstance()

        val privacy = findViewById<TextView>(R.id.privacy_policy_login)
        privacy.movementMethod = LinkMovementMethod.getInstance()

    }

    fun signInWithGoogle() {
        //When Sign in button is clicked
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN_RC)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val fragmentManager = supportFragmentManager

        Log.d("SIGNIN", requestCode.toString())

        when (requestCode) {
            GOOGLE_SIGN_IN_RC -> try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                //Google Sign In successful
                val account = task.getResult(ApiException::class.java)
                //Now authenticate with firebase
                firebaseAuthWithGoogle(account!!)

                val fragmentTransaction = fragmentManager.beginTransaction()
                fragmentTransaction.replace(R.id.signInFragmentHolder, signInProgressFragment)
                fragmentTransaction.commit()

                Log.d("GOOGLE", "SIGNED IN")
            } catch (e: Exception) {
                Log.d("GOOGLE", "SIGN IN FAILED")
                fragmentManager.beginTransaction().replace(R.id.signInFragmentHolder, signInFragment).commit()
            }

        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        //authenticate the google sign in with firebase
        Log.d("GOOGLE", "FirebaseAuth ID = " + account.id!!)

        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        params.putString(FirebaseAnalytics.Param.METHOD, "Google")
                        signedIn()
                    } else {
                        supportFragmentManager.beginTransaction().replace(R.id.signInFragmentHolder, signInFragment).commit()
                    }
                }
    }

    private fun signedIn() {
        //now the user is signed in
        val db = FirebaseFirestore.getInstance()
        //create a user document in firestore
        val user = HashMap<String, Any?>()
        user["uUd"] = mAuth.uid!!
        user["Name"] = mAuth.currentUser?.displayName
        user["Email"] = mAuth.currentUser?.email
        user["Phone"] = mAuth.currentUser?.phoneNumber
        val userDoc = db.collection(MainActivity.USERS).document(mAuth.uid!!)
        var signup = false
        userDoc.get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        signup = if (task.result!!.exists()) {
                            //repeat login
                            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, params)
                            false
                        } else {
                            //sign up
                            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SIGN_UP, params)
                            true
                        }
                    }
                }.continueWith {
                    userDoc.set(user)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    if (signup)
                                        Toast.makeText(this@Login, R.string.tap_plus_to_get_started, Toast.LENGTH_LONG).show()
                                    Log.d("FIRESTORE", "Added user document")

                                } else {
                                    Log.d("FIRESTORE", task.result!!.toString() + " : " + task.exception.toString())
                                    Toast.makeText(this@Login, R.string.something_went_wrong, Toast.LENGTH_LONG).show()
                                }
                            }
                    //now we can start the main activity
                    val done = Intent(this, MainActivity::class.java)
                    done.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(done)
                    finishAffinity()
                }
    }

    companion object {
        const val GOOGLE_SIGN_IN_RC = 1
    }
}
