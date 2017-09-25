package nimbus.arcane;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private View mMainView;

    private TextView mGroupName;
    private CircleImageView mImageView;

    public ChatsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
              Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView = inflater.inflate(R.layout.fragment_chats, container, false);

        mGroupName = (TextView) mMainView.findViewById(R.id.group_name);
        mImageView = (CircleImageView) mMainView.findViewById(R.id.group_image);

        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();


    }
}
