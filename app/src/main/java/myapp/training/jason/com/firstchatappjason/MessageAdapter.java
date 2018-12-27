package myapp.training.jason.com.firstchatappjason;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> mMessageList;
    private FirebaseAuth mAuth;
    private DatabaseReference mRootRef;

    public MessageAdapter(List<Messages> mMessageList) {
        this.mMessageList = mMessageList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        mAuth = FirebaseAuth.getInstance();
        mRootRef = FirebaseDatabase.getInstance().getReference();

        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.message_single_layout, viewGroup, false);

        return new MessageViewHolder(view);

    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView messageText;
        public CircleImageView profileImage;
        public TextView messageTime;
        public TextView messageName;
        public ImageView messageImage;

        public MessageViewHolder(View view){
            super(view);

            messageText = (TextView) view.findViewById(R.id.message_text_layout);
            profileImage = (CircleImageView) view.findViewById(R.id.message_profile_layout);
            messageTime = (TextView) view.findViewById(R.id.message_time_layout);
            messageName = (TextView) view.findViewById(R.id.message_name_layout);
            messageImage = (ImageView) view.findViewById(R.id.message_image_layout);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, int i) {
        Messages c = mMessageList.get(i);
        String from_user = c.getFrom();
        String message_type = c.getType();
        Long time_user = c.getTime();

        GetTimeAgo getTimeAgo = new GetTimeAgo();
        String lastTime = getTimeAgo.getTimeAgo(time_user, messageViewHolder.messageTime.getContext());

        String current_user_id = mAuth.getCurrentUser().getUid();
        mRootRef.child("Users").child(from_user).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("thumb_image").getValue().toString();

                messageViewHolder.messageName.setText(name);

                Picasso.get().load(image).placeholder(R.drawable.default_avatar)
                        .into(messageViewHolder.messageImage);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if(message_type.equals("text")){

            if(from_user.equals(current_user_id)){

                messageViewHolder.messageText.setBackgroundResource(R.drawable.message_text_background2);
                messageViewHolder.messageText.setTextColor(Color.WHITE);

            } else {

                messageViewHolder.messageText.setBackgroundResource(R.drawable.message_text_background);
                messageViewHolder.messageText.setTextColor(Color.WHITE);

            }
            messageViewHolder.messageText.setText(c.getMessage());
            messageViewHolder.messageTime.setText(lastTime);
            messageViewHolder.messageImage.setVisibility(View.INVISIBLE);

        } else {

            messageViewHolder.messageText.setVisibility(View.INVISIBLE);
            messageViewHolder.messageTime.setText(lastTime);

            Picasso.get().load(c.getMessage()).placeholder(R.drawable.default_avatar)
                    .into(messageViewHolder.messageImage);

        }


    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }
}
