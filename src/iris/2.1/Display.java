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
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.Timer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

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
	// ***** Constants *****
	
	// Graphics constants
	static final int WINDOW_WIDTH = 1200, WINDOW_HEIGHT = 600;
	static final int NUM_KEYS = 128;
	static final int WHITE_KEY_WIDTH = 20;
	static final int WHITE_KEY_HEIGHT = 100;
	static final int BLACK_KEY_WIDTH = 10;
	static final int BLACK_KEY_HEIGHT = 60;
	static final int FRONT_LINE = WINDOW_HEIGHT - WHITE_KEY_HEIGHT; // y-coordinate
	
	// Technical piano constants
	static final String[] NOTE = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
	static final char[] NOTE_TYPE = {'w', 'b', 'w', 'b', 'w', 'w', 'b', 'w', 'b', 'w', 'b', 'w'};
	static final int[] WHITE_KEY_NUMBER = {0, 1, 1, 2, 2, 3, 4, 4, 5, 5, 6, 6};
	
	// Graphical piano constants
	static final Color[] NOTE_COLOR = {
		new Color(255, 0, 0),
			new Color(128, 0, 0),
		new Color(255, 128, 0),
			new Color(128, 64, 0),
		new Color(255, 255, 0),
		new Color(0, 255, 0),
			new Color(0, 128, 0),
		new Color(0, 255, 255),
			new Color(0, 64, 128),
		new Color(0, 128, 255),
			new Color(128, 0, 128),
		new Color(255, 0, 255)
	};
	
	// Timing constants
	static final int MS_PER_PIXEL = 5; // *** MS = MILLISECONDS (not microseconds) ***
	static int CUR_PIXEL;
	static double MS_PER_TICK;
	static float SPEEDUP = 1.0f;
	
	// Control keys
	static final char KEY_FASTER = 'f';
	static final char KEY_SLOWER = 's';
	static final char KEY_RESET_SPEED = 'd';
	static final char KEY_PLAY_PAUSE = 'p';
	static final char KEY_QUIT = 'q';
	static final char KEY_REFRESH = 'r';
	// note: all savestates are 0 initially, so you can reserve '0' for reset
	static long[] savestates = new long[10];
	static final char[] SAVE_STATE = {')', '!', '@', '#', '$', '%', '^', '&', '*', '('};
	static final char[] LOAD_STATE = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
	
	// ***** Global note data *****
	
	// Midi note data
	static Sequence sequence;
	static Sequencer sequencer;
	static ArrayList<Note> notes = new ArrayList<Note>();
	static ArrayList<Rectangle> rects = new ArrayList<Rectangle>();
	
	
	// ***** Helper functions *****
	
	static int locate_key(int key) // locates x-coordinate of left side of key
	{
		int octave = key / 12;
		int note = key % 12;
		int position = (octave * 7 + WHITE_KEY_NUMBER[note]) * WHITE_KEY_WIDTH;
		if(NOTE_TYPE[note] == 'b') // shift to black key
			position -= BLACK_KEY_WIDTH / 2;
		return position;
	}
	
	
	// ***** Setup functions *****
	
	static void setup() throws Exception
	{
		sequence = MidiSystem.getSequence(new File("test.mid"));
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
			Rectangle rect = new Rectangle();
			int left_position = locate_key(note.key);
			int key_width = (NOTE_TYPE[note.key % 12] == 'w' ? WHITE_KEY_WIDTH : BLACK_KEY_WIDTH);
			rect.x0 = left_position;
			rect.y0 = (int) Math.round(note.start_tick * MS_PER_TICK / MS_PER_PIXEL);
			rect.x1 = left_position + key_width;
			rect.y1 = (int) Math.round(note.end_tick * MS_PER_TICK / MS_PER_PIXEL);
			rect.color = NOTE_COLOR[note.key % 12];
			//System.out.println(note.start_tick + ": " + note.key);
			rects.add(rect);
		}

		sequencer = MidiSystem.getSequencer();
		sequencer.open();
		sequencer.setSequence(sequence);
	}
	
	
	// ***** Graphic functions *****
	
	void draw_notes(Graphics G)
	{
		G.clearRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
		
		for(Rectangle rect : rects)
		{
			int x = rect.x0;
			int y = FRONT_LINE - (rect.y1 - CUR_PIXEL);
			int w = rect.x1 - rect.x0;
			int h = rect.y1 - rect.y0;
			
			if(y > FRONT_LINE || y + h < 0) continue;
			
			G.setColor(rect.color);
			G.fillRoundRect(x, y, w, h, w, w);
			G.setColor(Color.black);
			G.drawRoundRect(x, y, w, h, w, w);
		}
	}
	
	void draw_piano(Graphics G)
	{
		// white key pass
		G.setColor(Color.white);
		for(int key = 0; key < NUM_KEYS; key++)
		{
			int note = key % 12;
			int left_position = locate_key(key);
			
			if(NOTE_TYPE[note] == 'w')
			{
				G.setColor(NOTE_COLOR[note]);
				G.fillRect(left_position, FRONT_LINE, WHITE_KEY_WIDTH, WHITE_KEY_HEIGHT);
				G.setColor(Color.black);
				G.drawRect(left_position, FRONT_LINE, WHITE_KEY_WIDTH, WHITE_KEY_HEIGHT);
			}
		}

		// black key pass
		for(int key = 0; key < NUM_KEYS; key++)
		{
			int note = key % 12;
			int left_position = locate_key(key);
			
			if(NOTE_TYPE[note] == 'b')
			{
				G.setColor(NOTE_COLOR[note]);
				G.fillRect(left_position, FRONT_LINE, BLACK_KEY_WIDTH, BLACK_KEY_HEIGHT);
				G.setColor(Color.black);
				G.drawRect(left_position, FRONT_LINE, BLACK_KEY_WIDTH, BLACK_KEY_HEIGHT);
			}
		}
		
		G.setColor(Color.black);
		G.drawLine(0, FRONT_LINE, WINDOW_WIDTH, FRONT_LINE);
	}
	
	public void paintComponent(Graphics G)
	{
		draw_notes(G);
		
		draw_piano(G);
	}
	
	
	// ***** Main function Display *****
	
	public Display()
	{
		addKeyListener(new KeyAdapter()
		{
			public void keyPressed(KeyEvent e)
			{
				char c = e.getKeyChar();
				
				switch(c)
				{
				case KEY_FASTER:
					SPEEDUP += 0.1;
					sequencer.setTempoFactor(SPEEDUP);
					break;
				case KEY_SLOWER:
					SPEEDUP -= 0.1;
					sequencer.setTempoFactor(SPEEDUP);
					break;
				case KEY_RESET_SPEED:
					SPEEDUP = 1.0f;
					sequencer.setTempoFactor(SPEEDUP);
					break;
				case KEY_PLAY_PAUSE:
					if(sequencer.isRunning()) sequencer.stop();
					else sequencer.start();
					break;
				case KEY_QUIT:
					System.exit(0);
					break;
				case KEY_REFRESH:
					sequencer.setTickPosition(sequencer.getTickPosition());
					break;
				}
				
				for(int i = 0; i < savestates.length; i++)
				{
					if(c == SAVE_STATE[i])
					{
						savestates[i] = sequencer.getTickPosition();
						break;
					}
					else if(c == LOAD_STATE[i])
					{
						sequencer.setTickPosition(savestates[i]);
						break;
					}
				}
			}
		});
		
		setFocusable(true);
		Runnable motion = new Runnable()
		{
			public void run()
			{
				sequencer.start();

				Timer player = new Timer(15, new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						CUR_PIXEL = (int)(sequencer.getMicrosecondPosition() / 1000.0 / MS_PER_PIXEL);
						repaint();
					}
				});
				player.start();
			}
		};
		motion.run();
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
