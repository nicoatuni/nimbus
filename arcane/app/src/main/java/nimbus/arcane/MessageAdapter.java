package nimbus.arcane;

import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>{


    private List<Messages> mMessageList;
    private FirebaseAuth mAuth;
    private GetTimeAgo mTime;

    public MessageAdapter(List<Messages> mMessageList) {

        this.mMessageList = mMessageList;

    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v1 = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_layout, parent, false);

        return new MessageViewHolder(v1);

    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView messageText, senderText;
        public CircleImageView profileImage;
        public TextView messageTime, senderTime;
        public TextView messageFrom;
        public RelativeLayout relativeLayout, senderLayout;

        public MessageViewHolder(View view) {
            super(view);

            messageText = (TextView) view.findViewById(R.id.message_text_layout);
            messageFrom = (TextView) view.findViewById(R.id.message_from);
            profileImage = (CircleImageView) view.findViewById(R.id.message_profile_layout);
            messageTime = (TextView) view.findViewById(R.id.message_time_layout);
            relativeLayout = (RelativeLayout) view.findViewById(R.id.message_single_layout);

            senderText = (TextView) view.findViewById(R.id.sender_text_layout);
            senderTime = (TextView) view.findViewById(R.id.sender_time_layout);
            senderLayout = (RelativeLayout) view.findViewById(R.id.message_sender_single_layout);

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

            viewHolder.senderText.setText(c.getMessage());
            viewHolder.senderTime.setText(DateFormat.format("HH:mm", c.getTime()));

        } else {

            viewHolder.senderText.setVisibility(View.INVISIBLE);
            viewHolder.senderTime.setVisibility(View.INVISIBLE);

            viewHolder.messageFrom.setText(c.getName());
            viewHolder.messageText.setText(c.getMessage());
            viewHolder.messageTime.setText(DateFormat.format("HH:mm", c.getTime()));

        }

    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

}