package nimbus.arcane;

import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.text.format.DateFormat;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Richard Aldrich on 19/9/2017.
 *
 * Last edited by Richard Aldrich 14/10/2017
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>{

    private List<Messages> mMessageList;
    private FirebaseAuth mAuth;
    private GetTimeAgo mTime;
    private String mChatUser;

    public MessageAdapter(List<Messages> mMessageList, String chat_user) {

        this.mMessageList = mMessageList;
        this.mChatUser = chat_user;

    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v1 = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_layout, parent, false);

        return new MessageViewHolder(v1);

    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView messageText, senderText;
        public CircleImageView profileImage, locationProfile;
        public Button locationReceiverText, locationSenderText;
        public TextView messageTime, senderTime, locationReceiverTime, locationSenderTime;
        public TextView messageFrom, locationReceiverFrom;
        public RelativeLayout relativeLayout;

        public MessageViewHolder(View view) {
            super(view);

            messageText = (TextView) view.findViewById(R.id.message_text_layout);
            messageFrom = (TextView) view.findViewById(R.id.message_from);
            profileImage = (CircleImageView) view.findViewById(R.id.message_profile_layout);
            messageTime = (TextView) view.findViewById(R.id.message_time_layout);

            senderText = (TextView) view.findViewById(R.id.sender_text_layout);
            senderTime = (TextView) view.findViewById(R.id.sender_time_layout);

            locationReceiverText = (Button) view.findViewById(R.id.location_receiver_text_layout);
            locationReceiverTime = (TextView) view.findViewById(R.id.location_receiver_time_layout);
            locationProfile = (CircleImageView) view.findViewById(R.id.location_receiver_profile_layout);
            locationReceiverFrom = (TextView) view.findViewById(R.id.location_receiver_from);

            locationSenderText = (Button) view.findViewById(R.id.location_sender_text_layout);
            locationSenderTime = (TextView) view.findViewById(R.id.location_sender_time_layout);

            relativeLayout = (RelativeLayout) view.findViewById(R.id.message_single_layout);

        }
    }

    @Override
    public void onBindViewHolder(final MessageViewHolder viewHolder, int i) {

        mAuth = FirebaseAuth.getInstance();
        String current_user_id = mAuth.getCurrentUser().getUid();
        Messages c = mMessageList.get(i);
        String from_user = c.getFrom();
        String message_type = c.getType();

        if (from_user.equals(current_user_id)) {

            viewHolder.messageFrom.setVisibility(View.INVISIBLE);
            viewHolder.messageText.setVisibility(View.INVISIBLE);
            viewHolder.messageTime.setVisibility(View.INVISIBLE);
            viewHolder.profileImage.setVisibility(View.INVISIBLE);

            viewHolder.locationReceiverText.setVisibility(View.INVISIBLE);
            viewHolder.locationReceiverTime.setVisibility(View.INVISIBLE);
            viewHolder.locationReceiverFrom.setVisibility(View.INVISIBLE);
            viewHolder.locationProfile.setVisibility(View.INVISIBLE);

            if (message_type.equals("text")) {

                viewHolder.senderText.setText(c.getMessage());
                viewHolder.senderTime.setText(DateFormat.format("HH:mm", c.getTime()));

                viewHolder.locationSenderText.setVisibility(View.INVISIBLE);
                viewHolder.locationSenderTime.setVisibility(View.INVISIBLE);

            } else {

                viewHolder.senderText.setVisibility(View.INVISIBLE);
                viewHolder.senderTime.setVisibility(View.INVISIBLE);

                viewHolder.locationSenderText.setText("location");
                viewHolder.locationSenderTime.setText(DateFormat.format("HH:mm", c.getTime()));

            }

        } else {

            viewHolder.senderText.setVisibility(View.INVISIBLE);
            viewHolder.senderTime.setVisibility(View.INVISIBLE);

            viewHolder.locationSenderText.setVisibility(View.INVISIBLE);
            viewHolder.locationSenderTime.setVisibility(View.INVISIBLE);

            if (message_type.equals("text")) {

                viewHolder.locationReceiverText.setVisibility(View.INVISIBLE);
                viewHolder.locationReceiverTime.setVisibility(View.INVISIBLE);
                viewHolder.locationReceiverFrom.setVisibility(View.INVISIBLE);
                viewHolder.locationProfile.setVisibility(View.INVISIBLE);

                viewHolder.messageText.setText(c.getMessage());
                viewHolder.messageFrom.setText(c.getName());
                viewHolder.messageTime.setText(DateFormat.format("HH:mm", c.getTime()));
//                viewHolder.profileImage.

            } else {

                viewHolder.messageFrom.setVisibility(View.INVISIBLE);
                viewHolder.messageText.setVisibility(View.INVISIBLE);
                viewHolder.messageTime.setVisibility(View.INVISIBLE);
                viewHolder.profileImage.setVisibility(View.INVISIBLE);

                viewHolder.locationReceiverText.setText("location");
                viewHolder.locationReceiverTime.setText(c.getName());
                viewHolder.locationReceiverFrom.setText(DateFormat.format("HH:mm", c.getTime()));
//                viewHolder.locationProfile.setVisibility(View.INVISIBLE);

            }

        }

    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

}