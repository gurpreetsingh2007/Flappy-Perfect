package game;

import java.awt.AlphaComposite;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.io.File;
import java.io.IOException;
import java.awt.image.ImageObserver;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;


class FlappyBird extends JPanel implements ActionListener, KeyListener {
	//original width = 960
	//original height = 644 + 28 = 672

	boolean gameStart = false;
	static int waitTime = 10; //milli secondi, time to wait between frames -> frame rate
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Timer timer;
	
	

	//elements on screen
	ArrayList<Bird> birdList = new ArrayList<>();
	ArrayList<pipes> pipesList = new ArrayList<>();
	
	//images
	loadImages images = new loadImages();

	//Background
	Background background = new Background();

	//ground
	ground ground = new ground();
	
	public FlappyBird() {
		setPreferredSize(new Dimension(800, 600));
		timer = new Timer(waitTime, this);
	    timer.start();
	     
		//images
		images.uploadImages();

	     addKeyListener(this);
	     setFocusable(true);
	}
	
	public void paintComponent(Graphics g) {
        super.paintComponent(g);
		background.draw((Graphics2D)g);
		ground.draw((Graphics2D)g);
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
		Image[] birdImg;
		Rectangle hitbox;
		float scale_x, scale_y; //variabili per il scaling del image
		int x, y, height, width;

		void draw(Graphics2D g2d) {
			
		}
	}
	
	class pipes{
		Image testa;
		Image corpo;
		Rectangle hitbox;

		float scale_x, scale_y; //variabili per il scaling del image
		int x, y, height, width;

		public pipes() {
		}
		
		void draw(Graphics2D g2d) {
			
		}		
	}
	
	class ground{
		float scale_x = 1, scale_y = 0.041f, y_start = 0.96f; //variabili per il scaling del image
		int x = 0, y = (int)((float)getHeight() * y_start), height = 0, width = 0;
		int grass_speed = 2;
		void draw(Graphics2D g2d) {
			height = (int)((float)getHeight() * scale_y);
			width = (int)((float)getWidth() * scale_x);
			y = (int)((float)getHeight() * y_start);
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
			
	     	g2d.drawImage(images.grass, x, y, width, height, getFocusCycleRootAncestor());
	     	g2d.drawImage(images.grass, x+width, y, width, height, getFocusCycleRootAncestor());
			
			//g2d.drawImage(images.grass, 0, 0, 100, 100, getFocusCycleRootAncestor());
	     	x -= grass_speed;
	     	if(x <= -width)
	    	 	x = 0;
		}		
	}
	
	class Background{
		float scale_x = 1, scale_y = 0.96f; //variabili per il scaling del image
		int x = 0, y = 0, height = 0, width = 0;
		float opacity = 1.0f;  // Initial opacity
		int backgroundNumber = 0;
		//frame counter 
		int frameCounter = 0; //reset dopo 300 frame, per fare frame di animazione di background e bird
		void draw(Graphics2D g2d) {
			height = (int)((float)getHeight() * scale_y);
			width = (int)((float)getWidth() * scale_x);
			// Increment opacity
			if(frameCounter == 100){
            	opacity -= 0.01f;
				frameCounter = 0;
			}
            // If opacity reaches zero, switch to the next image
            if (opacity <= 0.0f) {
            	backgroundNumber = (backgroundNumber+1)%3;
                opacity = 1.0f;  // Reset opacity for next blend
            }
			//bg1
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
			g2d.drawImage(images.Background[backgroundNumber], 0, 0, width, height, getFocusCycleRootAncestor());
			//bg2
			int nextImageIndex = (backgroundNumber + 1) % 3;
	    	g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f - opacity));
	    	g2d.drawImage(images.Background[nextImageIndex], 0, 0, width, height, getFocusCycleRootAncestor());
			frameCounter++;
		}
	}
	
	void reset() {
		
	}

	class loadImages{
		Image[] Background = new Image[3];
		Image[] flappyImage = new Image[4];
		Image grass, pipe, pipeHead;


		void uploadImages() {
			//background
			for(int i = 0; i<3; i++) {
				try {
					Background[i] = ImageIO.read(new File("Images/background/bg" + i + ".png"));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			/* 
			//land
			try {
				ground = ImageIO.read(new File("Images/sfondo/ground.png"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			*/
			//grass
			try {
				grass = ImageIO.read(new File("Images/Ground/grass.png"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//pipe
			try {
				pipe = ImageIO.read(new File("Images/Pipe/pipe.png"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//pipeHead
			try {
				pipeHead = ImageIO.read(new File("Images/Pipe/pipeHead.png"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//flappy image
			for(int i = 0; i<4; i++) {
				try {
					flappyImage[i] = ImageIO.read(new File("Images/Bird/bird.png"));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
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
