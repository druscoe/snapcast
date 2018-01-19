package de.badaix.snapcast.utils;

import android.util.Log;
import android.os.Build;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;

public class Audio
{
    private static final String TAG = "AudioUtils";

    public static void logSampleRates() {
        for (int rate : new int[]{8000, 11025, 16000, 22050, 44100, 48000}) {  // add the rates you wish to check against
            Log.d(TAG, "Samplerate: " + rate);
            int bufferSize = AudioRecord.getMinBufferSize(rate, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);
            if (bufferSize > 0) {
                Log.d(TAG, "Samplerate: " + rate + ", buffer: " + bufferSize);
            }
        }
    }

    public static int getNativeSampleRate(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            String rate = audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE);
            return Integer.valueOf(rate);
            //String size = audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER);
            //tvInfo.setText("Sample rate: " + rate + ", buffer size: " + size);
        }
        return 0;
    }
}
