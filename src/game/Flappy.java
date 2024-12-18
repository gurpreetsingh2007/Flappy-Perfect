package game;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Random;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;




class FlappyBird extends JPanel implements ActionListener, KeyListener {
	Timer timer;
	//elements on screen
	ArrayList<Bird> birdList = new ArrayList<>();
	ArrayList<pipes> pipesList = new ArrayList<>();
	Background background = new Background();
	ground ground = new ground();
	int score = 0;
	int best_score = 0;
	float game_speed = 5.0f;
	//images
	loadImages images = new loadImages();

	//game
	boolean gameStart = false;

	static int waitTime = 10; //milli secondi, time to wait between frames -> frame rate
	void onWindowResize(){
		background.width = (int)((float)getWidth() * background.scale_x);
		background.height = (int)((float)getHeight() * background.scale_y);

		ground.width = (int)((float)getWidth() * ground.scale_x);
		ground.height = (int)((float)getHeight() * ground.scale_y);
		ground.y = (int)((float)getHeight() * ground.y_start);
		
	}

	public FlappyBird() {
		setPreferredSize(new Dimension(800, 600));
		timer = new Timer(waitTime, this);
	    timer.start();
	     
		//images
		images.uploadImages();
		//birdList.add(new Bird());
		//pipesList.add(new pipes());
		// Add ComponentListener to detect window size changes
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                onWindowResize();
            }
        });
	    addKeyListener(this);
	    setFocusable(true);
	}
	public void paintComponent(Graphics g) {
        super.paintComponent(g);
		background.draw((Graphics2D)g);
		ground.draw((Graphics2D)g);
		for(Bird bird: birdList)
			bird.draw((Graphics2D)g);
		for (pipes pipe : pipesList) {
        	pipe.draw((Graphics2D) g);
    	} 
		g.setColor(Color.BLACK);
        g.setFont(new Font("Consolas", Font.ITALIC, 40)); // Set font for the score
        g.drawString(""+score, 50, 50); // Draw the score at position (50, 50)
		
	}
	int count = 0;
	int index = 0;
	@Override
	public void actionPerformed(ActionEvent e) {
		background.update();
		for (int i = 0; i < birdList.size(); i++) {
			if((best_score < score && score != 0)){
				best_score = score;
				birdList.get(0).neuralNet.recordWeights("weights.txt");
			}
			Bird bird = birdList.get(i);
			bird.update();
			
			boolean remove = false;
			if((bird.y+bird.height >= ground.y || bird.y <= 0) && gameStart == true){	
				remove = true;
			}
			if(pipesList.size() > 0){
				if(!pipesList.get(0).pipeScoreAssigned)
					index = 0;
				else
					index = 1;
				
				ArrayList<Float> result = bird.neuralNet.testNetwork(bird.y, bird.y + bird.height, pipesList.get(index).corSpaceY, pipesList.get(index).bottomPipeY);
				if(result.get(0)>0.5f){
					bird.flap();
				}
				//System.out.println(index);
				if(pipesList.get(0).bottomPipeX < bird.x + bird.width && pipesList.get(0).bottomPipeX + pipesList.get(0).width > bird.x){
					if(pipesList.get(0).corSpaceY > bird.y || pipesList.get(0).bottomPipeY < bird.y + bird.height){
						remove = true;
						
						//System.out.println("Collision");
					}
					
					
				}
				else if(bird.x > pipesList.get(0).bottomPipeX + pipesList.get(0).width){
						if(pipesList.get(0).pipeScoreAssigned == false){
							score++;
							pipesList.get(0).pipeScoreAssigned = true;
						}
					}
			}
			if(remove == true){
				
				if(birdList.size() > 1){
					birdList.remove(i);
					i--;
				}
				else{
					for(int x = 0; x<500; x++){
						
						birdList.add(new Bird());
						birdList.get(x+1).neuralNet.copyWeights(birdList.get(x).neuralNet);
						birdList.get(x+1).neuralNet.perturbWeights(0.1f);
					}
					score = 0;
					count = 125;
					pipesList.clear();
					birdList.remove(i);
					i--;
				}
			}
			
		}
		if(birdList.size() == 0){
			gameStart = false;
			birdList.add(new Bird());
			birdList.get(0).neuralNet.loadWeights("weights.txt");
			count = 125;
			score = 0;
			
		}
		ground.update();
		
		if (gameStart) {
			if (count*game_speed >= 125) {
				pipesList.add(new pipes()); // Add a new pipe every 100 frames
				count = 0;
			}
			count++;
			
			for (pipes pipe : pipesList) {
				pipe.update(); // Update pipe position
			}
			//System.out.println(pipesList.size());
			// Remove pipes that move off the screen
			pipesList.removeIf(pipe -> pipe.bottomPipeX + pipe.width < 0);
		}

		repaint(); // Refresh the screen
	}

	
	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	 public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();

        if (keyCode == KeyEvent.VK_SPACE) {
            // If the spacebar is pressed, the bird should flap
            if (!gameStart) {
               	gameStart = true; // Start the game on the first press of spacebar
            }/* 
			else{
				for(Bird bird: birdList){
					bird.flap();
				}
			}*/
    	}
	 }

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	class Bird{

		NeuralNetwork neuralNet = new NeuralNetwork(4, 6, 36, 6, 3, 1);
		float scale_x = 0.071f, scale_y = 0.071f, x_start = 0.2f, y_start = 0.4f;
		int x = 0, y = 240, height = 0, width = 0; //variabili per il scaling dell'image
		int drawImage = 0;
		boolean isFalling = false;
		int frameCounter = 0;
		float count = 0.0f;
		float currentSpeed = 0.0f, gravity = 9.81f*game_speed;
		int contFall = 0;
		void update(){
			height = (int)((float)getHeight() * scale_y);
			width = (int)((float)getWidth() * scale_x);
			x = (int)((float)getWidth() * x_start);
			y = (int)((float)getHeight() * y_start);
			if(!isFalling){
				if(frameCounter == 5){
					drawImage = (drawImage+1)%3;
					frameCounter = 0;
				}
				frameCounter++;
			}
			if(!gameStart){
				y_start += Math.sin(count)/2000;
				count += 0.01f;
				if(count>=6.28) {
					count = 0;
				}
			}else{
				if(isFalling){
					y_start +=  ((float)(currentSpeed * waitTime/100 + 0.5f * gravity * waitTime/100 * waitTime/100)/(float)(getHeight()))*game_speed;
					currentSpeed += gravity * waitTime/100;
				}else{
					currentSpeed = 0.0f;
					y_start -=  0.01*game_speed;
					
					contFall++;
					if(contFall >= 10/game_speed){
						isFalling = true;
						contFall = 0;
					}
				}
			}
			y = (int)((float)getHeight() * y_start);
		}
		void draw(Graphics2D g2d) {
			g2d.drawImage(images.flappyImage[drawImage], x, y, width, height, getFocusCycleRootAncestor());
		}

		void flap(){
			if(!isFalling && contFall >= 10){
				contFall = 0;
			}
			isFalling = false;
			
		}
		}

	


	class Background{
		float scale_x = 1, scale_y = 0.96f; //variabili per il scaling del image
		int x = 0, y = 0, height = 0, width = 0;
		float opacity = 1.0f;  // Initial opacity
		int backgroundNumber = 0;
		//frame counter 
		int frameCounter = 0; //reset dopo 300 frame, per fare frame di animazione di background e bird
		void update() {
			// Increment opacity
			if(frameCounter == 100){
            	opacity -= 0.01f;
            	opacity -= 0.01f;
				frameCounter = 0;
			}
            // If opacity reaches zero, switch to the next image
            if (opacity <= 0.0f) {
            	backgroundNumber = (backgroundNumber+1)%3;
                opacity = 1.0f;  // Reset opacity for next blend
            }
		}
		void draw(Graphics2D g2d) {
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

	class ground{
		float scale_x = 1, scale_y = 0.041f, y_start = 0.96f, normalizeX = 1; //variabili per il scaling del image
		int x = 0, y = (int)((float)getHeight() * y_start), height = 0, width = 0;
		float grass_speed = 0.0033f;
		void draw(Graphics2D g2d) {
			
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
			
	     	g2d.drawImage(images.grass, x, y, width, height, getFocusCycleRootAncestor());
	     	g2d.drawImage(images.grass, x-width, y, width, height, getFocusCycleRootAncestor());
			
			//g2d.drawImage(images.grass, 0, 0, 100, 100, getFocusCycleRootAncestor());
		}
		void update(){
			normalizeX -= grass_speed*game_speed;
			x = (int)(normalizeX*(float)getWidth());
	     	if(normalizeX < 0)
	    	 	normalizeX = 1.0f;
		}
	}

	class pipes{
		boolean pipeScoreAssigned = false;
		//Rectangle hitbox = new Rectangle(0, 0, 0, 0);
		float scale_x = 0.101f, scale_y = 0.327f, x_start = 0, y_start = 0, tube_speed = 0.0033f*game_speed;
		float scale_head_x = 0.108f, scale_head_y = 0.075f;
		int headWidth = 0, headHeight = 0;
		int height = 0, width = 0;
		float space = 0.25f;
		float hitBoxY;

		//cor space
		int corSpaceY = 0;

		//bottom pipe start
		int bottomPipeX = 0, bottomPipeY = 0;
		public pipes(){
			x_start = 1;
			hitBoxY = new Random().nextFloat(scale_head_y+0.009f, 1.0f-scale_head_y-space-0.05f);
			//hitBoxY = scale_head_y+0.009f;
		}
		void update() {
			width = (int)((float)getWidth()*scale_x);
			height = (int)((float)getHeight() * scale_y);
			headHeight = (int)((float)getHeight() * scale_head_y);
			headWidth = (int)((float)getWidth() * scale_head_x);

			x_start -= tube_speed;
			bottomPipeX = (int)(x_start*(float)getWidth());
			//define y
			corSpaceY = (int)(hitBoxY*(float)getHeight());
			//bottom pipeHead
			bottomPipeY = (int)(space*(float)getHeight()) + corSpaceY;
			//hitbox = new Rectangle(bottomPipeX, corSpaceY, headWidth, bottomPipeY-corSpaceY);
			
		}
		void draw(Graphics2D g2d) {
			g2d.drawImage(images.pipe, bottomPipeX+(headWidth/2-width/2), 0, width, corSpaceY-headHeight, getFocusCycleRootAncestor());
			//draw upper head
			g2d.drawImage(images.pipeHead, bottomPipeX, corSpaceY-headHeight, headWidth, headHeight, getFocusCycleRootAncestor());

			//draw rect
			//g2d.fillRect(bottomPipeX, corSpaceY, headWidth, bottomPipeY-corSpaceY);
			//draw head bottom
			g2d.drawImage(images.pipeHead, bottomPipeX, bottomPipeY, headWidth, headHeight, getFocusCycleRootAncestor());
			g2d.drawImage(images.pipe, bottomPipeX+(headWidth/2-width/2), bottomPipeY+headHeight, width,getHeight()-bottomPipeY-headHeight-(int)((float)getHeight()*0.041f), getFocusCycleRootAncestor());
		}		
	}

	class loadImages{
		Image[] Background = new Image[3];
		Image[] flappyImage = new Image[3];
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
			for(int i = 1; i<4; i++) {
				try {
					flappyImage[i-1] = ImageIO.read(new File("Images/Bird/2bird" + i + ".png"));
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
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
	}
}