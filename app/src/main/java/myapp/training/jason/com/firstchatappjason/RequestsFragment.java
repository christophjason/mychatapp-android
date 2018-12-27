package myapp.training.jason.com.firstchatappjason;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment {

    private RecyclerView mFriendReqList;

    //Firebase
    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference mUsersDatabase;
    private DatabaseReference mRootRef;
    private FirebaseAuth mAuth;

    private String mCurrentUserId;

    private View mMainView;

    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView = inflater.inflate(R.layout.fragment_requests, container, false);

        mFriendReqList = (RecyclerView) mMainView.findViewById(R.id.friendreq_list);
        mAuth = FirebaseAuth.getInstance();

        mCurrentUserId = mAuth.getCurrentUser().getUid();

        mRootRef = FirebaseDatabase.getInstance().getReference();

        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_Requests").child(mCurrentUserId);
        mFriendReqDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        mFriendReqList.setHasFixedSize(true);
        mFriendReqList.setLayoutManager(linearLayoutManager);

        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Requests> options =
                new FirebaseRecyclerOptions.Builder<Requests>()
                        .setQuery(mFriendReqDatabase, Requests.class)
                        .build();

        FirebaseRecyclerAdapter firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Requests, RequestsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final RequestsViewHolder holder, int position, @NonNull Requests model) {

                final String list_users_id = getRef(position).getKey();

                mUsersDatabase.child(list_users_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            String userName = dataSnapshot.child("name").getValue().toString();
                            String userThumb = dataSnapshot.child("thumb_image").getValue().toString();
                            String status = dataSnapshot.child("status").getValue().toString();

                            holder.setName(userName);
                            holder.setThumbImage(userThumb);
                            holder.setStatus(status);

                            holder.mAcceptBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                                    Map friendsMap = new HashMap();
                                    friendsMap.put("Friends" + "/" + mCurrentUserId + "/" + list_users_id + "/" + "date", currentDate);
                                    friendsMap.put("Friends" + "/" + list_users_id + "/" + mCurrentUserId + "/" + "date", currentDate);

                                    friendsMap.put("Friend_Requests" + mCurrentUserId + "/" + list_users_id, null);
                                    friendsMap.put("Friend_Requests" + "/" + list_users_id + "/" + mCurrentUserId, null);

                                    mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                                            if(databaseError == null){

                                                holder.setViewGone();
                                                Toast.makeText(getContext(), "Friend Request Accepted", Toast.LENGTH_SHORT).show();

                                            } else {
                                                String error = databaseError.getMessage();
                                                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                                            }

                                        }
                                    });

                                }
                            });

                            holder.mDeclineBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    Map friendReqMap = new HashMap();
                                    friendReqMap.put("Friend_Requests" + "/" + mCurrentUserId + "/" + list_users_id, null);
                                    friendReqMap.put("Friend_Requests" + "/" + list_users_id + "/" + mCurrentUserId, null);

                                    mRootRef.updateChildren(friendReqMap, new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                                            if(databaseError == null){

                                                holder.setViewGone();
                                                Toast.makeText(getContext(), "Friend Request Declined", Toast.LENGTH_SHORT).show();

                                            } else {

                                                String error = databaseError.getMessage();
                                                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();

                                            }

                                        }
                                    });

                                }
                            });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @NonNull
            @Override
            public RequestsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.friendreq_single_layout, viewGroup, false);
                return new RequestsViewHolder(view);
            }
        };
        mFriendReqList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class RequestsViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public ImageButton mAcceptBtn;
        public ImageButton mDeclineBtn;

        public RequestsViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;
            mAcceptBtn = (ImageButton) mView.findViewById(R.id.friendreq_accept_btn);
            mDeclineBtn = (ImageButton) mView.findViewById(R.id.friendreq_decline_btn);

        }

        public void setName(String name) {

            TextView userNameView = mView.findViewById(R.id.friendreq_single_name);
            userNameView.setText(name);

        }

        public void setStatus(String status){

            TextView userStatusView = mView.findViewById(R.id.friendreq_single_status);
            userStatusView.setText(status);

        }

        public void setThumbImage(String thumb_image) {

            CircleImageView userImage = mView.findViewById(R.id.friendreq_single_image);
            Picasso.get().load(thumb_image).placeholder(R.drawable.default_avatar).into(userImage);

        }

        public void setViewGone(){
            mView.setVisibility(View.GONE);
        }

    }
}
