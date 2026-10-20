package com.mmunki.chess.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import java.util.HashMap;
import java.util.Map;

/**
 * The sounds of the game.
 *
 * <p>The sounds are made on the spot, there is no sound file in the source
 * tree.  A short wave is calculated and played through the sound card of the
 * computer.
 */
public final class SoundEngine {

    private static final float SAMPLE_RATE = 44100f;
    private static final int BITS = 16;
    private static final int CHANNELS = 1;

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

    private final Map<Sound, byte[]> samples = new HashMap<>();
    private boolean enabled = true;
    private float volume = 0.7f;
    private boolean available = true;

    public SoundEngine() {
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
        if (!enabled || !available) {
            return;
        }
        byte[] data = samples.get(sound);
        if (data == null) {
            return;
        }
        try {
            AudioFormat format = new AudioFormat(SAMPLE_RATE, BITS, CHANNELS, true, false);
            Clip clip = AudioSystem.getClip();
            clip.open(format, data, 0, data.length);
            setVolume(clip, volume);
            clip.start();
        } catch (LineUnavailableException | IllegalArgumentException e) {
            // no sound card, the game works without a sound
            available = false;
        }
    }

    private void setVolume(Clip clip, float value) {
        try {
            javax.sound.sampled.FloatControl gain =
                    (javax.sound.sampled.FloatControl) clip.getControl(javax.sound.sampled.FloatControl.Type.MASTER_GAIN);
            float min = gain.getMinimum();
            float max = gain.getMaximum();
            gain.setValue(min + (max - min) * value);
        } catch (IllegalArgumentException | UnsupportedOperationException e) {
            // the card does not know a volume, the sound is played as it is
        }
    }

    /** A wave that goes from 0 to 1 and back to 0, the body of every sound. */
    private static byte[] tone(double frequency, double millis, double strength, double sweep) {
        int samples = (int) (SAMPLE_RATE * millis / 1000.0);
        byte[] data = new byte[samples * 2];
        for (int i = 0; i < samples; i++) {
            double t = i / (double) samples;
            double envelope = Math.sin(Math.PI * t);
            envelope = Math.pow(envelope, 1.6);
            double f = frequency + sweep * t;
            double value = Math.sin(2 * Math.PI * f * i / SAMPLE_RATE) * envelope * strength;
            put(data, i, value);
        }
        return data;
    }

    /** Two waves one after the other. */
    private static byte[] two(double first, double firstMillis, double second, double secondMillis) {
        byte[] a = tone(first, firstMillis, 0.35, 0);
        byte[] b = tone(second, secondMillis, 0.35, 0);
        byte[] out = new byte[a.length + b.length];
        System.arraycopy(a, 0, out, 0, a.length);
        System.arraycopy(b, 0, out, a.length, b.length);
        return out;
    }

    /** A series of waves, the sound of a won or a lost game. */
    private static byte[] arpeggio(double[] frequencies, double millis) {
        int total = 0;
        byte[][] parts = new byte[frequencies.length][];
        for (int i = 0; i < frequencies.length; i++) {
            parts[i] = tone(frequencies[i], millis, 0.32, 0);
            total += parts[i].length;
        }
        byte[] out = new byte[total];
        int offset = 0;
        for (byte[] part : parts) {
            System.arraycopy(part, 0, out, offset, part.length);
            offset += part.length;
        }
        return out;
    }

    private static void put(byte[] data, int index, double value) {
        int sample = (int) (value * Short.MAX_VALUE);
        if (sample > Short.MAX_VALUE) {
            sample = Short.MAX_VALUE;
        }
        if (sample < Short.MIN_VALUE) {
            sample = Short.MIN_VALUE;
        }
        data[index * 2] = (byte) (sample & 0xff);
        data[index * 2 + 1] = (byte) ((sample >> 8) & 0xff);
    }
}
