package com.mmunki.chess.android;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.Looper;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The sounds of the game.
 *
 * <p>The sounds are made on the spot, there is no sound file in the source
 * tree.  A short wave is calculated and played through the speaker of the
 * phone.
 */
public final class AudioEngine {

    private static final int SAMPLE_RATE = 44100;

    /** The sounds of the game. */
    public enum Sound {
        MOVE,
        CAPTURE,
        CASTLE,
        CHECK,
        WIN,
        LOSE,
        DRAW,
        ILLEGAL,
        CLICK
    }

    private final Map<Sound, short[]> samples = new HashMap<>();
    private final ExecutorService player =
            Executors.newSingleThreadExecutor(runnable -> {
                Thread thread = new Thread(runnable, "chess-sound");
                thread.setDaemon(true);
                return thread;
            });
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean enabled = true;
    private float volume = 0.7f;

    public AudioEngine() {
        build();
    }

    private void build() {
        samples.put(Sound.MOVE, tone(520, 60, 0.35, 0.0));
        samples.put(Sound.CAPTURE, tone(300, 90, 0.5, 0.0));
        samples.put(Sound.CASTLE, two(520, 60, 700, 60));
        samples.put(Sound.CHECK, two(880, 90, 1174, 110));
        samples.put(Sound.WIN, arpeggio(new double[]{523.25, 659.25, 783.99, 1046.5}, 110));
        samples.put(Sound.LOSE, arpeggio(new double[]{523.25, 415.3, 349.23, 261.63}, 120));
        samples.put(Sound.DRAW, two(587.33, 110, 440, 140));
        samples.put(Sound.ILLEGAL, tone(150, 140, 0.4, 0.0));
        samples.put(Sound.CLICK, tone(760, 35, 0.25, 0.0));
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setVolume(float volume) {
        this.volume = Math.max(0f, Math.min(1f, volume));
    }

    public float volume() {
        return volume;
    }

    /** Plays a sound, nothing happens when the sound is switched off. */
    public void play(Sound sound) {
        if (!enabled) {
            return;
        }
        short[] data = samples.get(sound);
        if (data == null) {
            return;
        }
        player.execute(() -> {
            AudioTrack track = null;
            try {
                track = createTrack(data.length);
                track.write(data, 0, data.length);
                track.setVolume(volume);
                track.play();
            } catch (RuntimeException e) {
                // no sound in the phone, the game works without a sound
                release(track);
                return;
            }
            final AudioTrack played = track;
            long millis = (long) (data.length * 1000L / SAMPLE_RATE) + 200L;
            handler.postDelayed(() -> release(played), millis);
        });
    }

    /** A track that holds the sound, it is played as a whole. */
    @SuppressWarnings("deprecation")
    private AudioTrack createTrack(int samples) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            AudioTrack track = new AudioTrack.Builder()
                    .setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build())
                    .setAudioFormat(new AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build())
                    .setBufferSizeInBytes(samples * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build();
            return track;
        }
        return new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                samples * 2, AudioTrack.MODE_STATIC);
    }

    private void release(AudioTrack track) {
        if (track == null) {
            return;
        }
        try {
            track.stop();
        } catch (IllegalStateException e) {
            // the track was not playing
        }
        track.release();
    }

    /** A wave that goes from 0 to 1 and back to 0, the body of every sound. */
    private static short[] tone(double frequency, double millis, double strength, double sweep) {
        int count = (int) (SAMPLE_RATE * millis / 1000.0);
        short[] data = new short[count];
        for (int i = 0; i < count; i++) {
            double t = i / (double) count;
            double envelope = Math.sin(Math.PI * t);
            envelope = Math.pow(envelope, 1.6);
            double f = frequency + sweep * t;
            double value = Math.sin(2 * Math.PI * f * i / SAMPLE_RATE) * envelope * strength;
            data[i] = clamp(value);
        }
        return data;
    }

    /** Two waves one after the other. */
    private static short[] two(double first, double firstMillis, double second, double secondMillis) {
        short[] a = tone(first, firstMillis, 0.35, 0);
        short[] b = tone(second, secondMillis, 0.35, 0);
        short[] out = new short[a.length + b.length];
        System.arraycopy(a, 0, out, 0, a.length);
        System.arraycopy(b, 0, out, a.length, b.length);
        return out;
    }

    /** A series of waves, the sound of a won or a lost game. */
    private static short[] arpeggio(double[] frequencies, double millis) {
        int total = 0;
        short[][] parts = new short[frequencies.length][];
        for (int i = 0; i < frequencies.length; i++) {
            parts[i] = tone(frequencies[i], millis, 0.32, 0);
            total += parts[i].length;
        }
        short[] out = new short[total];
        int offset = 0;
        for (short[] part : parts) {
            System.arraycopy(part, 0, out, offset, part.length);
            offset += part.length;
        }
        return out;
    }

    private static short clamp(double value) {
        int sample = (int) (value * Short.MAX_VALUE);
        if (sample > Short.MAX_VALUE) {
            sample = Short.MAX_VALUE;
        }
        if (sample < Short.MIN_VALUE) {
            sample = Short.MIN_VALUE;
        }
        return (short) sample;
    }
}
