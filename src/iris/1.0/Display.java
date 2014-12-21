package iris;

import java.util.*;
import java.awt.Color;
import java.awt.Graphics;
import java.io.File;
import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

class Note
{
	int key, start_tick, end_tick;
}
class CompareNoteStart implements Comparator<Note>
{
	public int compare(Note a, Note b)
	{
		return a.start_tick - b.start_tick;
	}
}
class Rectangle
{
	int x0, y0; // bottom-left
	int x1, y1; // top-right
	Color color;
}

public class Display extends JPanel
{
	static final int WINDOW_WIDTH = 1000, WINDOW_HEIGHT = 500;
	static final int NUM_KEYS = 128;
	static final int KEY_WIDTH = 10;
	
	static final String[] NOTE = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
	static final char[] NOTE_TYPE = {'w', 'b', 'w', 'b', 'w', 'w', 'b', 'w', 'b', 'w', 'b', 'w'};
	static final Color[] NOTE_COLOR = {
		Color.red,
			Color.red.darker(),
		Color.orange,
			Color.yellow.darker(),
		Color.yellow,
		Color.green,
			Color.green.darker(),
		Color.cyan,
			Color.blue.darker(),
		Color.blue,
			Color.magenta.darker(),
		Color.magenta
		};
	
	static ArrayList<Note> notes = new ArrayList<Note>();
	
	static ArrayList<Rectangle> rects = new ArrayList<Rectangle>();
	
	static final int MS_PER_PIXEL = 5; // *** MS = MILLISECONDS (not microseconds) ***
	static int CUR_TICK;
	static double MS_PER_TICK;
	
	static void setup() throws Exception
	{
		Sequence sequence = MidiSystem.getSequence(new File("test.mid"));
		MS_PER_TICK = sequence.getMicrosecondLength() / (1000.0 * sequence.getTickLength());
		System.out.println("ms per tick = " + MS_PER_TICK);
		
		for(Track track : sequence.getTracks())
		{
			int[] last_key = new int[NUM_KEYS];
			Arrays.fill(last_key, -1);
			for(int notei = 0; notei < track.size(); notei++)
			{
				MidiEvent event = track.get(notei);
				int tick = (int) event.getTick();
				MidiMessage message = event.getMessage();
				if(message instanceof ShortMessage)
				{
					ShortMessage data = (ShortMessage) message;
					int key = data.getData1();
					int volume = data.getData2();
					if(data.getCommand() == ShortMessage.NOTE_ON && volume > 0)
					{
						last_key[key] = tick;
					}
					else if(data.getCommand() == ShortMessage.NOTE_OFF || (data.getCommand() == ShortMessage.NOTE_ON && volume == 0))
					{
						if(last_key[key] != -1)
						{
							Note note = new Note();
							note.key = key;
							note.start_tick = last_key[key];
							note.end_tick = tick;
							notes.add(note);
							last_key[key] = -1;
						}
					}
				}
			}
		}
		
		Collections.sort(notes, new CompareNoteStart());
		
		for(Note note : notes)
		{
			//rects.add(new Rectangle(note.key * KEY_WIDTH, note.start_tick, (note.key + 1) * KEY_WIDTH - 1, note.end_tick));
			Rectangle rect = new Rectangle();
			rect.x0 = note.key * KEY_WIDTH;
			rect.y0 = (int) Math.round(note.start_tick * MS_PER_TICK / MS_PER_PIXEL);
			rect.x1 = (note.key + 1) * KEY_WIDTH - 1;
			rect.y1 = (int) Math.round(note.end_tick * MS_PER_TICK / MS_PER_PIXEL);
			rect.color = NOTE_COLOR[note.key % 12];
			rects.add(rect);
		}
		
		for(Rectangle rect : rects)
		{
			//System.out.println("(" + rect.x0 + "," + rect.y0 + ") (" + rect.x1 + "," + rect.y1 + ")");
		}
		
		/*int oni = 0, offi = 0;
		while(oni < note_ons.size() || offi < note_offs.size())
		{
			if(offi == note_offs.size() || (oni < note_ons.size() && note_ons.get(oni).time < note_offs.get(offi).time))
			{
				System.out.println(note_ons.get(oni).time + ":  on " + NOTES[note_ons.get(oni).key % 12]);
				oni++;
			}
			else
			{
				System.out.println(note_offs.get(offi).time + ": off " + NOTES[note_offs.get(offi).key % 12]);
				offi++;
			}
		}*/
	}
	
	public void paintComponent(Graphics G)
	{
		super.paintComponent(G);
		//System.out.println("repainting...");
		G.clearRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
		
		for(Rectangle rect : rects)
		{
			G.setColor(rect.color);
			G.fillRect(rect.x0, rect.y0 - CUR_TICK, rect.x1 - rect.x0, rect.y1 - rect.y0);
		}
	}
	
	public Display()
	{
		setFocusable(true);
		//System.out.println("starting...");
		Thread motion = new Thread(new Runnable()
		{
			public void run()
			{
				for(CUR_TICK = 0; true; CUR_TICK++)
				{
					repaint();
					try
					{
						Thread.sleep(MS_PER_PIXEL);
					}
					catch (InterruptedException e) {
						System.exit(0);
					}
				}
			}
		});
		motion.start();
	}
	
	public static void main(String[] args) throws Exception
	{
		setup();
		
		JFrame display = new JFrame();
		display.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		display.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		display.setResizable(false);

		display.setLocationRelativeTo(null);
		
		display.add(new Display());
		
		display.setVisible(true);
	}
}
