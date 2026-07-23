package escape.audio;

import javax.sound.sampled.*;
import java.io.*;
import java.net.URL;

/**
 * Handles background music playback for the game.
 *
 * This class loads a {@code music.wav} file from several possible locations
 * and plays it continuously in a loop. The music can also be stopped when the
 * game ends or closes.
 */
public class MusicPlayer {

    /** The audio clip currently being used for background music playback. */
    private static Clip clip;

    /**
     * Starts background music playback.
     *
     * This method attempts to load the music file, open it as an audio clip,
     * and play it continuously. If the file cannot be found or loaded, an error
     * message is printed instead.
     */
    public static void start() {
        try {
            AudioInputStream audio = loadAudioStream();

            if (audio == null) {
                System.out.println("ERROR: music.wav not found. Make sure it is in your resources folder.");
                return;
            }

            clip = AudioSystem.getClip();
            clip.open(audio);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();
            System.out.println("Music started successfully!");

        } catch (Exception e) {
            System.out.println("Could not load music: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Loads the music file as an {@link AudioInputStream}.
     *
     * This method first finds the music file as an {@link InputStream},
     * then wraps it so it can be read by the Java sound system.
     *
     * @return the loaded audio stream, or {@code null} if the file could not be found
     * @throws Exception if the file is found but cannot be converted into an audio stream
     */
    private static AudioInputStream loadAudioStream() throws Exception {
        InputStream is = loadMusicFile();
        if (is == null) {
            return null;
        }
        return AudioSystem.getAudioInputStream(new BufferedInputStream(is));
    }

    /**
     * Searches for the {@code music.wav} file in several possible locations.
     *
     * The search order is:
     * 1. classpath root
     * 2. same package as {@code MusicPlayer}
     * 3. class loader resources
     * 4. common file system paths
     *
     * @return an input stream for the music file, or {@code null} if not found
     * @throws IOException if a file is found but cannot be opened
     */
    private static InputStream loadMusicFile() throws IOException {
        InputStream is = MusicPlayer.class.getResourceAsStream("/music.wav");

        if (is == null) {
            is = MusicPlayer.class.getResourceAsStream("music.wav");
        }

        if (is == null) {
            URL url = MusicPlayer.class.getClassLoader().getResource("music.wav");
            if (url != null) {
                is = url.openStream();
            }
        }

        if (is == null) {
            File f = new File("src/main/resources/music.wav");
            if (!f.exists()) f = new File("resources/music.wav");
            if (!f.exists()) f = new File("music.wav");

            if (f.exists()) {
                System.out.println("Loading music from file: " + f.getAbsolutePath());
                is = new FileInputStream(f);
            }
        }

        return is;
    }

    /**
     * Stops the background music if it is currently playing.
     *
     * If a clip has been created and is running, it is stopped and closed
     * to release the audio resources.
     */
    public static void stop() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
            clip.close();
        }
    }
}