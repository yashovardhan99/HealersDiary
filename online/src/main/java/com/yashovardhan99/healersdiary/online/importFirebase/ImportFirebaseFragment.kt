package com.yashovardhan99.healersdiary.online.importFirebase

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
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
import com.yashovardhan99.core.OnlineModuleDependencies
import com.yashovardhan99.healersdiary.online.DaggerOnlineComponent
import com.yashovardhan99.healersdiary.online.R
import com.yashovardhan99.healersdiary.online.databinding.FragmentImportFirebaseBinding
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.Job
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.isActive
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

class ImportFirebaseFragment : Fragment() {
    @Inject
    lateinit var viewModel: ImportFirebaseViewModel
    private val contract = ActivityResultContracts.StartActivityForResult()
    private val launcher = registerForActivityResult(contract, ::onSignIn)
    private var indeterminateProgressJob: Job? = null
    private val gso by lazy {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(com.yashovardhan99.healersdiary.R.string.default_web_client_id))
                .requestEmail()
                .build()
    }

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
        val client = GoogleSignIn.getClient(requireContext(), gso)
        client.signOut()
        val intent = client.signInIntent
        launcher.launch(intent)
    }

    private fun signOut() {
        val client = GoogleSignIn.getClient(requireContext(), gso)
        Firebase.auth.signOut()
        client.signOut()
    }

    private fun onSignIn(signInResult: ActivityResult?) {
        if (signInResult != null) {
            Timber.d("Sign in result = $signInResult")
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
        binding.progressHorizontal.setProgressCompat(0, true)
        binding.progressHorizontal.max = ImportWorker.MAX_PROGRESS
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
                    setProgressIndeterminate(binding)
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
                    indeterminateProgressJob?.cancel()
                    if (!binding.progressHorizontal.isVisible) binding.progressHorizontal.show()
                    binding.progressHorizontal.setProgressCompat(workInfo.progress.getInt(ImportWorker.OVERALL_PROGRESS, 0), true)

                    binding.login.visibility = View.GONE
                    binding.illustration.visibility = View.VISIBLE
                }
                WorkInfo.State.SUCCEEDED -> {
                    binding.pageTitle.setText(com.yashovardhan99.healersdiary.R.string.import_completed)
                    binding.progressText.setText(R.string.import_done_message)
                    indeterminateProgressJob?.cancel()
                    binding.progressHorizontal.setProgressCompat(ImportWorker.MAX_PROGRESS, true)
                    Snackbar.make(binding.root, com.yashovardhan99.healersdiary.R.string.import_completed, Snackbar.LENGTH_LONG).show()
                    signOut()
                    findNavController().navigate(ImportFirebaseFragmentDirections.actionImportFirebaseFragmentToOnboardingFragment())
//                    viewModel.importCompleted()
                }
                WorkInfo.State.FAILED -> {
                    binding.pageTitle.setText(R.string.import_v1)
                    binding.progressText.setText(R.string.import_failed)
                    indeterminateProgressJob?.cancel()
                    binding.progressHorizontal.setProgressCompat(0, true)
                    binding.login.visibility = View.VISIBLE
                    binding.illustration.visibility = View.GONE
                }
                WorkInfo.State.BLOCKED -> {
                    binding.pageTitle.setText(R.string.import_v1)
                    binding.progressText.setText(R.string.waiting_for_constraints)
                    setProgressIndeterminate(binding)
                    binding.login.visibility = View.GONE
                    binding.illustration.visibility = View.VISIBLE
                }
                WorkInfo.State.CANCELLED -> {
                    binding.pageTitle.setText(R.string.import_v1)
                    binding.progressText.setText(R.string.import_failed)
                    indeterminateProgressJob?.cancel()
                    binding.progressHorizontal.setProgressCompat(0, true)
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
                    indeterminateProgressJob?.cancel()
                    binding.progressHorizontal.setProgressCompat(0, true)
                }
            }
        }
        return binding.root
    }

    private fun setProgressIndeterminate(binding: FragmentImportFirebaseBinding) {
        indeterminateProgressJob?.cancel()
        binding.progressHorizontal.hide()
        indeterminateProgressJob = lifecycleScope.launchWhenStarted {
            while (binding.progressHorizontal.isVisible && isActive) {
                awaitFrame()
            }
            if (isActive) binding.progressHorizontal.isIndeterminate = true
            binding.progressHorizontal.show()
        }
    }

    private fun startImport(binding: FragmentImportFirebaseBinding) {
        viewModel.startImport()
        binding.login.visibility = View.GONE
        setProgressIndeterminate(binding)
        binding.progressText.setText(R.string.connecting)
    }
}