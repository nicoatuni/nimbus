package nimbus.arcane;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by Richard Aldrich on 17/9/2017.
 */

class SectionsPagerAdapter extends FragmentPagerAdapter {

    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
//            case 0:
//                RequestFragment requestFragment = new RequestFragment();
//                return requestFragment;

            case 0:
                FriendFragment friendFragment = new FriendFragment();
                return friendFragment;

            case 1:
                ChatsFragment chatsFragment = new ChatsFragment();
                return chatsFragment;

            case 2:
                MapFragment mapFragment = new MapFragment();
                return mapFragment;

            default:
                return null;
        }

    }

    @Override
    public int getCount() {
        return 3;
    }

    public CharSequence getPageTitle(int position) {

        switch (position) {
//            case 0:
//                return "REQUESTS";

            case 0:
                return "FRIENDS";

            case 1:
                return "CHATS";

            case 2:
                return "MAP";

            default:
                return null;
        }
        
    }
}
