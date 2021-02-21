package com.yashovardhan99.healersdiary.online.importFirebase

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.work.WorkInfo
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.yashovardhan99.healersdiary.OnlineModuleDependencies
import com.yashovardhan99.healersdiary.online.DaggerOnlineComponent
import com.yashovardhan99.healersdiary.online.R
import com.yashovardhan99.healersdiary.online.databinding.FragmentImportFirebaseBinding
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

class ImportFirebaseFragment : Fragment() {
    @Inject
    lateinit var viewModel: ImportFirebaseViewModel
    private val contract = ActivityResultContracts.StartActivityForResult()
    private val launcher = registerForActivityResult(contract, ::onSignIn)
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
        Timber.d("Auth = ${Firebase.auth}")
        val user = Firebase.auth.currentUser
        if (user != null) {
            viewModel.setUser(user)
        } else {
            signIn()
        }
    }

    private fun signIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(com.yashovardhan99.healersdiary.R.string.default_web_client_id))
                .requestEmail()
                .build()
        val client = GoogleSignIn.getClient(requireContext(), gso)
        val intent = client.signInIntent
        launcher.launch(intent)
    }

    private fun onSignIn(signInResult: ActivityResult?) {
        if (signInResult != null) {
            lifecycleScope.launchWhenStarted {

                try {
                    val signInTask = GoogleSignIn.getSignedInAccountFromIntent(signInResult.data)
                    val account = signInTask.getResult(ApiException::class.java)
                    Timber.d("Signed in : ${account?.id}")
                    val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
                    val result = Firebase.auth.signInWithCredential(credential).await()
                    val user = result?.user
                    if (user != null) {
                        viewModel.setUser(user)
                    } else {
                        Timber.w("Sign In failed: $result")
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Sign in error")
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = FragmentImportFirebaseBinding.inflate(inflater, container, false)
        binding.pageTitle.setText(R.string.import_v1)
        binding.progressHorizontal.progress = 0
        binding.progressText.setText(R.string.login_to_continue)
        binding.login.visibility = View.VISIBLE
        binding.login.setOnClickListener {
            signIn()
        }
        viewModel.workObserver.observe(viewLifecycleOwner) { workInfo ->
            Timber.d("Work info = $workInfo")
            Timber.d("Progress = ${workInfo.progress}")
            when (workInfo.state) {
                WorkInfo.State.ENQUEUED -> {
                    binding.pageTitle.setText(R.string.import_v1)
                    binding.progressText.setText(R.string.connecting)
                    binding.progressHorizontal.isIndeterminate = true
                    binding.login.visibility = View.GONE
                    binding.illustration.visibility = View.GONE
                }
                WorkInfo.State.RUNNING -> {
                    binding.pageTitle.setText(R.string.importing)
                    if (!workInfo.progress.getBoolean(ImportWorker.PATIENTS_FOUND, false)) {
                        binding.progressText.setText(R.string.looking_for_patients)
                    } else {
                        val patients = workInfo.progress.getInt(ImportWorker.MAX_PATIENTS, 0)
                        val current = workInfo.progress.getInt(ImportWorker.CURRENT_PATIENT, 0)
                        if (current == 0) binding.progressText.text = getString(R.string.patients_found, patients)
                        else binding.progressText.text = getString(R.string.importing_of, current, patients)
                    }
                    binding.progressHorizontal.isIndeterminate = false
                    binding.progressHorizontal.max = ImportWorker.MAX_PROGRESS
                    binding.progressHorizontal.progress = workInfo.progress.getInt(ImportWorker.OVERALL_PROGRESS, 0)

                    binding.login.visibility = View.GONE
                    binding.illustration.visibility = View.VISIBLE
                }
                WorkInfo.State.SUCCEEDED -> {
                    binding.pageTitle.setText(com.yashovardhan99.healersdiary.R.string.import_completed)
                    binding.progressText.setText(R.string.import_done_message)
                    binding.progressHorizontal.progress = ImportWorker.MAX_PROGRESS
                    Snackbar.make(binding.root, com.yashovardhan99.healersdiary.R.string.import_completed, Snackbar.LENGTH_LONG).show()
                    Firebase.auth.signOut()
                    findNavController().popBackStack(com.yashovardhan99.healersdiary.R.id.onboardingFragment, false)
                    viewModel.importCompleted()
                }
                WorkInfo.State.FAILED -> {
                    binding.pageTitle.setText(R.string.import_v1)
                    binding.progressText.setText(R.string.import_failed)
                    binding.progressHorizontal.progress = 0
                    binding.progressHorizontal.isIndeterminate = false
                    binding.login.visibility = View.VISIBLE
                    binding.illustration.visibility = View.GONE
                }
                WorkInfo.State.BLOCKED -> {
                    binding.pageTitle.setText(R.string.import_v1)
                    binding.progressText.setText(R.string.waiting_for_constraints)
                    binding.progressHorizontal.isIndeterminate = true
                    binding.login.visibility = View.GONE
                    binding.illustration.visibility = View.VISIBLE
                }
                WorkInfo.State.CANCELLED -> {
                    binding.pageTitle.setText(R.string.import_v1)
                    binding.progressText.setText(R.string.import_failed)
                    binding.progressHorizontal.progress = 0
                    binding.progressHorizontal.isIndeterminate = false
                    binding.login.visibility = View.VISIBLE
                    binding.illustration.visibility = View.GONE
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.user.collect { user ->
                Timber.d("User = $user")
                if (user != null) startImport(binding)
                else {
                    binding.login.visibility = View.VISIBLE
                    binding.progressText.setText(R.string.login_to_continue)
                    binding.illustration.visibility = View.GONE
                    binding.progressHorizontal.progress = 0
                }
            }
        }
        return binding.root
    }

    private fun startImport(binding: FragmentImportFirebaseBinding) {
        viewModel.startImport()
        binding.login.visibility = View.GONE
        binding.progressHorizontal.isIndeterminate = true
        binding.progressText.setText(R.string.connecting)
    }
}