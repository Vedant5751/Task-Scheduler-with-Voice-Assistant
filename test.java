import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;

public class test {
    public static void main(String[] args) {
        System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
        Voice voice = VoiceManager.getInstance().getVoice("kevin16");
        if (voice != null) {
            voice.allocate();
            voice.speak("This is a test of FreeTTS.");
        }
    }
}
