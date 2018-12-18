package myapp.training.jason.com.firstchatappjason;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private TextInputEditText mLoginEmail;
    private TextInputEditText mLoginPassword;
    private Button mLoginBtn;

    //Progress Dialog
    private ProgressDialog mLogProgress;

    //Firebase Auth
    private FirebaseAuth mAuth;

    //Firebaes
    private DatabaseReference mUserDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mToolbar = (Toolbar) findViewById(R.id.login_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Login");

        mLoginEmail = (TextInputEditText) findViewById(R.id.log_email);
        mLoginPassword = (TextInputEditText) findViewById(R.id.log_password);
        mLoginBtn = (Button) findViewById(R.id.log_login_btn);

        //Progress Dialog
        mLogProgress = new ProgressDialog(this);

        //Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        //Firebase
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String login_email = mLoginEmail.getText().toString();
                String login_password = mLoginPassword.getText().toString();

                if (!TextUtils.isEmpty(login_email) && !TextUtils.isEmpty(login_password)){
                    mLogProgress.setTitle("Logging In");
                    mLogProgress.setMessage("Please wait while we check your credentials");
                    mLogProgress.setCanceledOnTouchOutside(false);
                    mLogProgress.show();
                    loginUser(login_email, login_password);
                }

            }
        });

    }



    private void loginUser(String login_email, String login_password) {
        mAuth.signInWithEmailAndPassword(login_email, login_password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()){

                    mLogProgress.dismiss();

                    String current_user_id = mAuth.getCurrentUser().getUid();

                    //Get token id of device
                    String deviceToken = FirebaseInstanceId.getInstance().getToken();

                    mUserDatabase.child(current_user_id).child("device_token").setValue(deviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(mainIntent);
                            finish();

                        }
                    });


                } else {
                    mLogProgress.hide();
                    Toast.makeText(LoginActivity.this, "Cannot Sign In. Please check the form and try again.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
