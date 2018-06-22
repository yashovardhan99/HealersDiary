package com.yashovardhan99.healersdiary.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.yashovardhan99.healersdiary.R;

public class Login extends AppCompatActivity {

    GoogleSignInClient mGoogleSignInClient;
    FirebaseAuth mAuth;
    final int GOOGLE_SIGN_IN_RC = 1;
    LinearLayout signInProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        signInProgress = findViewById(R.id.signInProgress);

        //configuring google sign in
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
         mGoogleSignInClient = GoogleSignIn.getClient(this,gso);
         mAuth = FirebaseAuth.getInstance();

        SignInButton gsb = findViewById(R.id.GoogleSignInButton);
        gsb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent,GOOGLE_SIGN_IN_RC);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        signInProgress.setVisibility(View.VISIBLE);
        switch(requestCode){
            case GOOGLE_SIGN_IN_RC:
                try{
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    //Google Sign In successful
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    //Now authenticate with firebase
                    FirebaseAuthWithGoogle(account);
                    Log.d("GOOGLE","SIGNED IN");

                }catch (Exception e){
                    Log.d("GOOGLE","SIGN IN FAILED");
                    signInProgress.setVisibility(View.INVISIBLE);
                }
                break;
        }
    }
    void FirebaseAuthWithGoogle(GoogleSignInAccount account){
        Log.d("GOOGLE","FirebaseAuth ID = "+account.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(),null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Log.d("FIREBASE","Login Succesful. Name = "+ mAuth.getCurrentUser().getDisplayName());
                            signedIn();
                        }
                    }
                });
    }
    void signedIn()
    {
        signInProgress.setVisibility(View.GONE);
        Intent done = new Intent(this,MainActivity.class);
        done.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(done);
    }
}
