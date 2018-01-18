package de.badaix.snapcast.ui;

import de.badaix.snapcast.R;

import android.content.Context;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class DrawerListAdapter extends BaseAdapter {
    static class NavItem {
        String mTitle;
        String mSubtitle;
        int mIcon;
  
        public NavItem(String title, String subtitle, int icon) {
            mTitle = title;
            mSubtitle = subtitle;
            mIcon = icon;
        }
    }
 
    Context mContext;
    ArrayList<NavItem> mNavItems;
 
    public DrawerListAdapter(Context context, ArrayList<NavItem> navItems) {
        mContext = context;
        mNavItems = navItems;
    }
 
    @Override
    public int getCount() {
        return mNavItems.size();
    }
 
    @Override
    public Object getItem(int position) {
        return mNavItems.get(position);
    }
 
    @Override
    public long getItemId(int position) {
        return 0;
    }
 
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
 
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.drawer_item, null);
        }
        else {
            view = convertView;
        }
 
        TextView titleView = (TextView) view.findViewById(R.id.title);
        TextView subtitleView = (TextView) view.findViewById(R.id.subTitle);
        ImageView iconView = (ImageView) view.findViewById(R.id.icon);
 
        titleView.setText(mNavItems.get(position).mTitle);
        subtitleView.setText(mNavItems.get(position).mSubtitle);
        iconView.setImageResource(mNavItems.get(position).mIcon);
 
        return view;
    }

    public static void initDrawer(Context context, ListView drawerList) {
        ArrayList<NavItem> mNavItems = new ArrayList<NavItem>();

        mNavItems.add(new NavItem("Home", "Meetup destination", R.drawable.ic_action_home));
        mNavItems.add(new NavItem("Preferences", "Change your preferences", R.drawable.ic_action_settings));
        mNavItems.add(new NavItem("About", "Get to know about us", R.drawable.ic_action_about));
      
        DrawerListAdapter adapter = new DrawerListAdapter(context, mNavItems);
        drawerList.setAdapter(adapter);
      
        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //selectItemFromDrawer(position);
            }
        });
    }
}
