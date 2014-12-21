package iris;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.util.Scanner;

public class ColorTest extends Canvas{
	Scanner sc = new Scanner(System.in);
	public void paint(Graphics G)
	{
		while(true)
		{
			int r = sc.nextInt(), g = sc.nextInt(), b = sc.nextInt();
			System.out.println("processing...");
			Color c = new Color(r, g, b);
			G.setColor(c);
			G.fillOval(0, 0, 100, 100);
		}
	}
	public static void main(String[] args)
	{
		ColorTest graphics = new ColorTest();
		Frame frame = new Frame();
		frame.setSize(100, 100);
		frame.add(graphics);
		frame.setVisible(true);
	}
}
