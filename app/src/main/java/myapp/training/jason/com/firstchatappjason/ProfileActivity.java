package myapp.training.jason.com.firstchatappjason;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private ImageView mProfileImage;
    private TextView mProfileName, mProfileStatus, mProfileFriendsCount;
    private Button mProfileSendReqBtn, mDeclineBtn;

    private ProgressDialog mProgressDialog;

    private int mCurrent_state;

    //Firebase
    private DatabaseReference mUsersDatabase;
    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference mNotificationDatabase;

    private DatabaseReference mRootRef;

    private FirebaseUser mCurrentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String uid = getIntent().getStringExtra("uid");

        mRootRef = FirebaseDatabase.getInstance().getReference();

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_Requests");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("Notifications");
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        mProfileImage = (ImageView) findViewById(R.id.profile_image);
        mProfileName = (TextView) findViewById(R.id.profile_displayName);
        mProfileStatus = (TextView) findViewById(R.id.profile_status);
        mProfileFriendsCount = (TextView) findViewById(R.id.profile_friendsCount);
        mProfileSendReqBtn = (Button) findViewById(R.id.profile_send_btn);
        mDeclineBtn = (Button) findViewById(R.id.profile_decline_btn);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading User Data");
        mProgressDialog.setMessage("Please wait while we load the user data");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        mCurrent_state = 0;
        mDeclineBtn.setEnabled(false);
        mDeclineBtn.setVisibility(View.INVISIBLE);

        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String display_name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                mProfileName.setText(display_name);
                mProfileStatus.setText(status);
                Picasso.get().load(image).placeholder(R.drawable.default_avatar).into(mProfileImage);

                //Friends List/Request Feautre
                mFriendReqDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild(uid)){
                            String request_type = dataSnapshot.child(uid).child("request_type").getValue().toString();
                            if(request_type.equals("received")){

                                //Received Friend Request
                                mCurrent_state = 2;
                                mProfileSendReqBtn.setText("Accept Friend Request");

                                mDeclineBtn.setVisibility(View.VISIBLE);
                                mDeclineBtn.setEnabled(true);

                            } else if (request_type.equals("sent")) {

                                //Sent Friend Request
                                mCurrent_state = 1;
                                mProfileSendReqBtn.setText("Cancel Friend Request");

                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);

                            }

                            mProgressDialog.dismiss();

                        } else {

                            mFriendDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    if(dataSnapshot.hasChild(uid)){

                                        mCurrent_state = 3;
                                        mProfileSendReqBtn.setText("Unfriend");

                                    }

                                    mProgressDialog.dismiss();

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                    mProgressDialog.dismiss();

                                }
                            });

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mProfileSendReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mProfileSendReqBtn.setEnabled(false);

                //Not Friends State or Can Send Friend Request
                if(mCurrent_state == 0){

                    DatabaseReference newNotificationRef = mRootRef.child("Notifications").child(uid).push();
                    String newNotificationId = newNotificationRef.getKey();

                    HashMap<String, String> notificationData = new HashMap<>();
                    notificationData.put("from", mCurrentUser.getUid());
                    notificationData.put("type", "request");

                    Map requestMap = new HashMap<>();
                    requestMap.put("Friend_Requests" + "/" + mCurrentUser.getUid() + "/" + uid + "/" + "request_type", "sent");
                    requestMap.put("Friend_Requests" + "/" + uid + "/" + mCurrentUser.getUid() + "/" + "request_type", "received");
                    requestMap.put("Notifications" + "/" + uid + "/" + newNotificationId, notificationData);

                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                            if (databaseError != null) {
                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();

                            }

                            mProfileSendReqBtn.setEnabled(true);
                            mCurrent_state = 1; // Request Sent
                            mProfileSendReqBtn.setText("Cancel Friend Request");

                        }
                    });

                }

                //Cancel Request or Request is Sent
                if(mCurrent_state == 1){

                    Map friendReqMap = new HashMap();
                    friendReqMap.put("Friend_Requests" + "/" + mCurrentUser.getUid() + "/" + uid, null);
                    friendReqMap.put("Friend_Requests" + "/" + uid + "/" + mCurrentUser.getUid(), null);

                    mRootRef.updateChildren(friendReqMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                            if(databaseError == null) {

                                mProfileSendReqBtn.setEnabled(true);
                                mCurrent_state = 0; //Not Friends
                                mProfileSendReqBtn.setText("Send Friend Request");

                            } else {
                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                }

                //Request Received State
                if(mCurrent_state == 2){

                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                    Map friendsMap = new HashMap();
                    friendsMap.put("Friends" + "/" + mCurrentUser.getUid() + "/" + uid + "/" + "date", currentDate);
                    friendsMap.put("Friends" + "/" + uid + "/" + mCurrentUser.getUid() + "/" + "date", currentDate);

                    friendsMap.put("Friend_Requests" + mCurrentUser.getUid() + "/" + uid, null);
                    friendsMap.put("Friend_Requests" + "/" + uid + "/" + mCurrentUser.getUid(), null);

                    mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                            if(databaseError == null){

                                mProfileSendReqBtn.setEnabled(true);
                                mCurrent_state = 3; //Friends
                                mProfileSendReqBtn.setText("Unfriend");

                            } else {
                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();
                            }

                        }
                    });

                }

                //Friends State
                if(mCurrent_state == 3){

                    Map friendsMap = new HashMap();
                    friendsMap.put("Friends" + "/" + mCurrentUser.getUid() + "/" + uid, null);
                    friendsMap.put("Friends" + "/" + uid + "/" + mCurrentUser.getUid(), null);

                    mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                            if(databaseError == null){

                                mProfileSendReqBtn.setEnabled(true);
                                mCurrent_state = 0; //Not Friends
                                mProfileSendReqBtn.setText("Send Friend Request");

                            } else {
                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();
                            }

                        }
                    });

                }


            }
        });

        mDeclineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map friendReqMap = new HashMap();
                friendReqMap.put("Friend_Requests" + "/" + mCurrentUser.getUid() + "/" + uid, null);
                friendReqMap.put("Friend_Requests" + "/" + uid + "/" + mCurrentUser.getUid(), null);

                mRootRef.updateChildren(friendReqMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                        if(databaseError == null){

                            mProfileSendReqBtn.setEnabled(true);
                            mCurrent_state = 0;
                            mProfileSendReqBtn.setText("Send Friend Request");

                            mDeclineBtn.setVisibility(View.INVISIBLE);
                            mDeclineBtn.setEnabled(false);

                        } else {

                            String error = databaseError.getMessage();
                            Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();

                        }

                    }
                });
            }
        });

    }
}
