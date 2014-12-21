package iris;

import java.io.File;
import java.io.IOException;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;

/*public class Main {
    public static void main(String[] args) throws InvalidMidiDataException, IOException, MidiUnavailableException{
    Sequence sequence = MidiSystem.getSequence(new File("test.mid"));

    // Create a sequencer for the sequence
    Sequencer sequencer = MidiSystem.getSequencer();
    sequencer.open();
    sequencer.setSequence(sequence);

    // Start playing
    sequencer.start();
    }
}*/
public class Player {
	public static void main(String[] args) throws InvalidMidiDataException, IOException, MidiUnavailableException
	{
		Sequence sequence = MidiSystem.getSequence(new File("test.mid"));
		
		Sequencer sequencer = MidiSystem.getSequencer();
		sequencer.open();
		sequencer.setSequence(sequence);
		
		sequencer.start();
	}
}
