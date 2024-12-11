package tech.razikus.headlesshaven;

public class PseudoMusicWidget extends PseudoWidget
{
    private final double startTime;

    public PseudoMusicWidget(PseudoWidget original) {
        super(original);
        this.startTime = System.currentTimeMillis() / 1000.0;
    }


    public void play(int note) {
        double now = System.currentTimeMillis() / 1000.0;
        this.WidgetMsg("play", note, (float)(now - startTime));
    }

    public void stop(int note) {
        double now = System.currentTimeMillis() / 1000.0;
        this.WidgetMsg("stop", note, (float)(now - startTime));
    }


    public static int noteToKey(String noteName) {
        String note = noteName.substring(0, noteName.length() - 1);
        int octave = Integer.parseInt(noteName.substring(noteName.length() - 1));

        int baseNote;
        switch(note.toUpperCase()) {
            case "C": baseNote = 0; break;
            case "C#":
            case "Db": baseNote = 1; break;
            case "D": baseNote = 2; break;
            case "D#":
            case "Eb": baseNote = 3; break;
            case "E": baseNote = 4; break;
            case "F": baseNote = 5; break;
            case "F#":
            case "Gb": baseNote = 6; break;
            case "G": baseNote = 7; break;
            case "G#":
            case "Ab": baseNote = 8; break;
            case "A": baseNote = 9; break;
            case "A#":
            case "Bb": baseNote = 10; break;
            case "B": baseNote = 11; break;
            default: throw new IllegalArgumentException("Invalid note: " + note);
        }

        return baseNote + (octave * 12);
    }
    public void playNote(String noteName) {
        play(noteToKey(noteName));
    }

    public void stopNote(String noteName) {
        stop(noteToKey(noteName));
    }


    public void playSequence(Note[] notes) {
        new Thread(() -> {
            for (Note note : notes) {
                play(note.key);
                try {
                    Thread.sleep((long)(note.duration * 1000));
                } catch (InterruptedException e) {
                    break;
                }
                stop(note.key);

                try {
                    Thread.sleep((long)(note.gap * 1000));
                } catch (InterruptedException e) {
                    break;
                }
            }
        }).start();
    }
    public void playNote(char key, int duration) throws InterruptedException {
        this.playKey(key);
        Thread.sleep(duration);
        this.stopKey(key);
    }

    // Helper for shifted (higher octave) notes
    public void playNoteShifted(char key, int duration) throws InterruptedException {
        this.playKeyShifted(key);
        Thread.sleep(duration);
        this.stopKeyShifted(key);
    }


    public void playKey(char key) {
        int baseNote = switch(Character.toLowerCase(key)) {
            case 'z' -> 0;
            case 's' -> 1;
            case 'x' -> 2;
            case 'd' -> 3;
            case 'c' -> 4;
            case 'v' -> 5;
            case 'g' -> 6;
            case 'b' -> 7;
            case 'h' -> 8;
            case 'n' -> 9;
            case 'j' -> 10;
            case 'm' -> 11;
            default -> throw new IllegalArgumentException("Invalid key: " + key);
        };

        play(baseNote + 12); // Adding 12 for base octave offset
    }


    // Helper method to stop a keyboard note
    public void stopKey(char key) {
        int baseNote = switch(Character.toLowerCase(key)) {
            case 'z' -> 0;
            case 's' -> 1;
            case 'x' -> 2;
            case 'd' -> 3;
            case 'c' -> 4;
            case 'v' -> 5;
            case 'g' -> 6;
            case 'b' -> 7;
            case 'h' -> 8;
            case 'n' -> 9;
            case 'j' -> 10;
            case 'm' -> 11;
            default -> throw new IllegalArgumentException("Invalid key: " + key);
        };

        stop(baseNote + 12);
    }


    // Optional: Helper methods for different octaves
    public void playKeyShifted(char key) {
        int baseNote = switch(Character.toLowerCase(key)) {
            case 'z' -> 0;
            case 's' -> 1;
            case 'x' -> 2;
            case 'd' -> 3;
            case 'c' -> 4;
            case 'v' -> 5;
            case 'g' -> 6;
            case 'b' -> 7;
            case 'h' -> 8;
            case 'n' -> 9;
            case 'j' -> 10;
            case 'm' -> 11;
            default -> throw new IllegalArgumentException("Invalid key: " + key);
        };

        play(baseNote + 24);
    }

    public void stopKeyShifted(char key) {
        int baseNote = switch(Character.toLowerCase(key)) {
            case 'z' -> 0;
            case 's' -> 1;
            case 'x' -> 2;
            case 'd' -> 3;
            case 'c' -> 4;
            case 'v' -> 5;
            case 'g' -> 6;
            case 'b' -> 7;
            case 'h' -> 8;
            case 'n' -> 9;
            case 'j' -> 10;
            case 'm' -> 11;
            default -> throw new IllegalArgumentException("Invalid key: " + key);
        };

        stop(baseNote + 24);
    }


    public static class Note {
        public final int key;
        public final double duration;
        public final double gap;

        public Note(int key, double duration, double gap) {
            this.key = key;
            this.duration = duration;
            this.gap = gap;
        }

        public Note(String noteName, double duration, double gap) {
            this(noteToKey(noteName), duration, gap);
        }
    }
}