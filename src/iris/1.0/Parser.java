package iris;

import java.util.*;

import java.io.File;
import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

/*public class Test {
    public static final int NOTE_ON = 0x90;
    public static final int NOTE_OFF = 0x80;
    public static final String[] NOTE_NAMES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};

    public static void main(String[] args) throws Exception {
        Sequence sequence = MidiSystem.getSequence(new File("test.mid"));

        int trackNumber = 0;
        for (Track track :  sequence.getTracks()) {
            trackNumber++;
            System.out.println("Track " + trackNumber + ": size = " + track.size());
            System.out.println();
            for (int i=0; i < track.size(); i++) { 
                MidiEvent event = track.get(i);
                System.out.print("@" + event.getTick() + " ");
                MidiMessage message = event.getMessage();
                if (message instanceof ShortMessage) {
                    ShortMessage sm = (ShortMessage) message;
                    System.out.print("Channel: " + sm.getChannel() + " ");
                    if (sm.getCommand() == NOTE_ON) {
                        int key = sm.getData1();
                        int octave = (key / 12)-1;
                        int note = key % 12;
                        String noteName = NOTE_NAMES[note];
                        int velocity = sm.getData2();
                        System.out.println("Note on, " + noteName + octave + " key=" + key + " velocity: " + velocity);
                    } else if (sm.getCommand() == NOTE_OFF) {
                        int key = sm.getData1();
                        int octave = (key / 12)-1;
                        int note = key % 12;
                        String noteName = NOTE_NAMES[note];
                        int velocity = sm.getData2();
                        System.out.println("Note off, " + noteName + octave + " key=" + key + " velocity: " + velocity);
                    } else {
                        System.out.println("Command:" + sm.getCommand());
                    }
                } else {
                    System.out.println("Other message: " + message.getClass());
                }
            }

            System.out.println();
        }

    }
}*/

/*class Note implements Comparable<Note>
{
	int time, key;
	char type;
	public Note(int time2, int key2, char type2)
	{
		time = time2;
		key = key2;
		type = type2;
	}
	public int compareTo(Note note)
	{
		return time - note.time;
	}
}

public class Parser
{
	public static final String[] NOTES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
	public static void main(String[] args) throws Exception
	{
		Sequence sequence = MidiSystem.getSequence(new File("test.mid"));
		ArrayList<Note> notes = new ArrayList<Note>();
		
		for(int tracki = 0; tracki < sequence.getTracks().length; tracki++)
		{
			Track track = sequence.getTracks()[tracki];
			for(int notei = 0; notei < track.size(); notei++)
			{
				MidiEvent event = track.get(notei);
				MidiMessage message = event.getMessage();
				if(message instanceof ShortMessage)
				{
					ShortMessage data = (ShortMessage) message;
					if(data.getCommand() == ShortMessage.NOTE_ON && data.getData2() > 0)
					{
						notes.add(new Note((int)event.getTick(), data.getData1(), '+'));
					}
					else if(data.getCommand() == ShortMessage.NOTE_OFF || (data.getCommand() == ShortMessage.NOTE_ON && data.getData2() == 0))
					{
						notes.add(new Note((int)event.getTick(), data.getData1(), '-'));
					}
				}
			}
		}
		
		Collections.sort(notes);
		
		for(Note note : notes)
		{
			System.out.println(note.time + ": " + note.type + note.key);
		}
	}
}*/
