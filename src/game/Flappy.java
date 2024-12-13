package game;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;


class FlappyBird extends JPanel implements ActionListener, KeyListener {
	boolean gameStart = false;
	static int waitTime = 10; //milli secondi, time to wait between frames -> frame rate
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Timer timer;
	
	//elements on screen
	ArrayList<Bird> birdList = new ArrayList<>();
	ArrayList<Bird> pipesList = new ArrayList<>();
	
	
	public FlappyBird() {
		setPreferredSize(new Dimension(800, 600));
		 timer = new Timer(waitTime, this);
	     timer.start();
	     
	     addKeyListener(this);
	     setFocusable(true);
	}
	
	public void paintComponent(Graphics g) {
        super.paintComponent(g);
	
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		repaint();
	}
	
	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	
	
	
	
	class Bird{
		void draw(Graphics2D g2d) {
			
		}
	}
	
	class loadImages{
		void draw(Graphics2D g2d) {
			
		}
	}
	
	class pipes{
		void draw(Graphics2D g2d) {
			
		}		
	}
	
	class background{
		void draw(Graphics2D g2d) {
			
		}		
	}
	
	class ground{
		void draw(Graphics2D g2d) {
			
		}		
	}
	
	void reset() {
		
	}

}






public class Flappy {
	public static void main(String[] args) {
		JFrame frame = new JFrame("Flappy Bird");
		FlappyBird game = new FlappyBird();
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
	}
}
