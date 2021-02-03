package com.yashovardhan99.healersdiary.online

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.ktx.Firebase
import com.yashovardhan99.healersdiary.OnlineModuleDependencies
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.online.databinding.FragmentImportFirebaseBinding
import dagger.hilt.android.EntryPointAccessors
import timber.log.Timber
import javax.inject.Inject

class ImportFirebaseFragment : Fragment() {
    @Inject
    lateinit var viewModel: ImportFirebaseViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        val coreModuleDependencies = EntryPointAccessors.fromApplication(
                requireActivity().applicationContext,
                OnlineModuleDependencies::class.java)
        DaggerOnlineComponent.builder()
                .context(requireContext())
                .appDependencies(coreModuleDependencies)
                .build().inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
//        val user = viewModel.firebaseAuth.currentUser
//        if (user != null) {
//            viewModel.setUser(user)
//        } else {
//            signIn()
//        }
    }

    private fun signIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        val client = GoogleSignIn.getClient(requireContext(), gso)
        val intent = client.signInIntent
        val contract = ActivityResultContracts.StartActivityForResult()
//        registerForActivityResult(contract, ::onSignIn).launch(intent)
    }

//    private fun onSignIn(result: ActivityResult?) {
//        if (result != null) {
//            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
//            try {
//                val account = task.getResult(ApiException::class.java)
//                Timber.d("Signed in : ${account?.id}")
//                val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
//                viewModel.firebaseAuth.signInWithCredential(credential).addOnCompleteListener { task ->
//                    val user = task.result?.user
//                    if (task.isSuccessful && user != null) {
//                        viewModel.setUser(user)
//                    } else {
//                        Timber.w(task.exception, "Sign In failed")
//                    }
//                }
//            } catch (e: Exception) {
//                Timber.e(e, "Sign in error")
//            }
//        }
//    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = FragmentImportFirebaseBinding.inflate(inflater, container, false)
        binding.login.setOnClickListener {
            viewModel.importCompleted()
        }
        return binding.root
    }
}