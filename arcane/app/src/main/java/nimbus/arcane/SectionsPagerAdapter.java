package nimbus.arcane;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * @author Richard Aldrich
 * Last edited by Nico Dinata
 */

class SectionsPagerAdapter extends FragmentPagerAdapter {

    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {

            case 0:
                FriendFragment friendFragment = new FriendFragment();
                return friendFragment;

            case 1:
                ChatsFragment chatsFragment = new ChatsFragment();
                return chatsFragment;

            case 2:
                MapFragment mapFragment = new MapFragment();
                return mapFragment;

            case 3:
                RequestFragment requestFragment = new RequestFragment();
                return requestFragment;

            default:
                return null;
        }

    }

    @Override
    public int getCount() {
        return 4;
    }

    public CharSequence getPageTitle(int position) {

        switch (position) {

            case 0:
                return "FRIENDS";

            case 1:
                return "CHATS";

            case 2:
                return "MAP";

            case 3:
                return "REQUEST";

            default:
                return null;
        }
        
    }
}
