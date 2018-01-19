/*
 *     This file is part of snapcast
 *     Copyright (C) 2014-2017  Johannes Pohl
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.badaix.snapcast;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Vector;

import de.badaix.snapcast.control.json.Client;
import de.badaix.snapcast.control.json.Group;
import de.badaix.snapcast.control.json.ServerStatus;
import de.badaix.snapcast.control.json.Stream;
import de.badaix.snapcast.control.json.Volume;

/**
 * Created by johannes on 04.12.16.
 */


public class GroupItem extends LinearLayout implements SeekBar.OnSeekBarChangeListener, View.OnClickListener, ClientItem.ClientItemListener, View.OnTouchListener {

    private static final String TAG = "GroupItem";

    //    private TextView title;
    private LinearLayout llClient;
    private LinearLayout llVolume;
    private TextView tvStreamName = null;
    private SeekBar volumeSeekBar;
    private ImageButton ibMute;
    private ImageButton ibSettings;

    private Group group;
    private ServerStatus server;
    private GroupItemListener listener = null;
    private boolean hideOffline = false;
    private Vector<ClientItem> clientItems = null;
    private Vector<Integer> clientVolumes = null;
    private int groupVolume = 0;

    public GroupItem(Context context, ServerStatus server, Group group) {
        super(context);
        LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        vi.inflate(R.layout.group_item, this);
//        title = (TextView) findViewById(R.id.title);

        this.server = server;
        clientItems = new Vector<>();
        clientVolumes = new Vector<>();

        llClient =      (LinearLayout) findViewById(R.id.llClient);
        llVolume =      (LinearLayout) findViewById(R.id.llVolume);
        tvStreamName =  (TextView) findViewById(R.id.tvStreamName);
        volumeSeekBar = (SeekBar) findViewById(R.id.volumeSeekBar);
        ibMute =        (ImageButton) findViewById(R.id.ibMute);
        ibSettings =    (ImageButton) findViewById(R.id.ibSettings);

        ibMute.setOnClickListener(this);
        ibSettings.setOnClickListener(this);
        volumeSeekBar.setOnSeekBarChangeListener(this);
        volumeSeekBar.setOnTouchListener(this);

        ibMute.setImageResource(R.drawable.ic_speaker_icon);
        llVolume.setVisibility(GONE);
        llClient.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        setGroup(group);
    }

    private int clientCount() {
        return clientItems.size();
    }

    private Volume getVolume(Client client) {
        return client.getConfig().getVolume();
    }

    private Volume getVolume(ClientItem clientItem) {
        return getVolume(clientItem.getClient());
    }

    /*
    private Volume getVolume(Group group) {
        return group.getConfig().getVolume();
    }
    */

    private boolean isMuted() {
        return group.isMuted();
    }

    private void update() {
//        title.setText(group.getName());
        llClient.removeAllViews();
        clientItems.clear();
        for (Client client : group.getClients()) {
            if ((client == null) || client.isDeleted() || (hideOffline && !client.isConnected()))
                continue;

            ClientItem clientItem = new ClientItem(this.getContext(), client);
            clientItem.setListener(this);
            clientItems.add(clientItem);
            llClient.addView(clientItem);
        }

        ibMute.setImageResource(isMuted() ?
            R.drawable.ic_mute_icon :
            R.drawable.ic_speaker_icon);

        llVolume.setVisibility(((clientCount() >= 2) || ((clientCount() == 1) && isMuted())) ?
            VISIBLE :
            GONE);
        updateVolume();

        Stream stream = server.getStream(group.getStreamId());
        if (stream == null)
            return;
        tvStreamName.setText(stream.getName());
/*        String codec = stream.getUri().getQuery().get("codec");
        if (codec.contains(":"))
            codec = codec.split(":")[0];
        tvStreamState.setText(stream.getUri().getQuery().get("sampleformat") + " - " + codec + " - " + stream.getStatus().toString());

        title.setEnabled(group.isConnected());
        volumeSeekBar.setProgress(getVolume(group).getPercent());

        ibMute.setImageResource(getVolume(client).isMuted() ?
            R.drawable.ic_mute_icon :
            R.drawable.ic_speaker_icon);
*/
    }

    private void updateVolume() {
        double meanVolume = 0;
        for (ClientItem clientItem : clientItems) {
            meanVolume += getVolume(clientItem).getPercent();
        }
        meanVolume /= clientCount();
        volumeSeekBar.setProgress((int) (Math.ceil(meanVolume)));
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(final Group group) {
        this.group = group;
        update();
    }

    public void setListener(GroupItemListener listener) {
        this.listener = listener;
    }

    public void setHideOffline(boolean hideOffline) {
        if (this.hideOffline == hideOffline)
            return;
        this.hideOffline = hideOffline;
        update();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (!fromUser)
            return;

        int delta = progress - groupVolume;
        if (delta == 0)
            return;

        double ratio;
        if (delta < 0)
            ratio = (double) (groupVolume - progress) / (double) groupVolume;
        else
            ratio = (double) (progress - groupVolume) / (double) (100 - groupVolume);

        for (int i = 0; i < clientCount(); ++i) {
            ClientItem clientItem = clientItems.get(i);
            int clientVolume = clientVolumes.get(i);
            int newVolume = clientVolume;
            if (delta < 0)
                newVolume -= ratio * clientVolume;
            else
                newVolume += ratio * (100 - clientVolume);
            Volume volume = getVolume(clientItem);
            volume.setPercent(newVolume);
            clientItem.update();
        }
        if (listener != null)
            listener.onGroupVolumeChanged(this);
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            clientVolumes.clear();
            for (ClientItem clientItem : clientItems)
                clientVolumes.add(getVolume(clientItem).getPercent());
            groupVolume = volumeSeekBar.getProgress();
            Log.d(TAG, "onTouch: " + groupVolume);
        }
        return false;
    }


    @Override
    public void onClick(View v) {
        if (v == ibMute) {
            group.setMuted(!isMuted());
            update();
            listener.onMute(this, isMuted());
        } else if (v == ibSettings) {
            listener.onPropertiesClicked(this);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onVolumeChanged(ClientItem clientItem, int percent, boolean mute) {
        if (listener != null)
            listener.onVolumeChanged(this, clientItem, percent, mute);
        updateVolume();
    }

    @Override
    public void onDeleteClicked(ClientItem clientItem) {
        if (listener != null)
            listener.onDeleteClicked(this, clientItem);
    }

    @Override
    public void onPropertiesClicked(ClientItem clientItem) {
        if (listener != null)
            listener.onClientPropertiesClicked(this, clientItem);
    }


    public interface GroupItemListener {
        void onGroupVolumeChanged(GroupItem group);

        void onMute(GroupItem group, boolean mute);

        void onVolumeChanged(GroupItem group, ClientItem clientItem, int percent, boolean mute);

        void onDeleteClicked(GroupItem group, ClientItem clientItem);

        void onClientPropertiesClicked(GroupItem group, ClientItem clientItem);

        void onPropertiesClicked(GroupItem group);
    }

}
