import org.jbox2d.common.*;
import org.jbox2d.dynamics.*;
import org.jbox2d.collision.*;
import org.jbox2d.collision.shapes.*;
import org.jbox2d.dynamics.contacts.*;
import org.jbox2d.callbacks.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.io.*;

import fr.atis_lab.physicalworld.*;

/*
 * javac -cp ./lib/*:. TestXX.java
 *
 * java -cp ./lib/*:. TestXX
 */

/* import ... */
public class Prgm implements ContactListener, KeyListener, Serializable {
	String nameA, nameB;
	boolean CONTACT = false;
   private boolean needToTeleportBall = false;
	int NoBullet;

    /* PhysicalWorld => contains the World and walls (as PhysicalObject) */
    private PhysicalWorld world;
    /* PhysicalObject => contains the Body, Shape & Fixture */
    //private PhysicalObject ball, ramp, door;
    transient private Body Xwing;
    /* Custom Panel for drawing purpose */
    private DrawingPanel panel;

    public Prgm()  {
    
        /* Allocation of the PhysicalWorld (gravity, dimensions, color of the walls */
        world = new PhysicalWorld(new Vec2(0,0), -25, 25, -25, 25, Color.RED);
        world.setContactListener(this);
			 LinkedList<Vec2> vertices = new LinkedList<Vec2>();
            vertices.add(new Vec2(0,1.3f));
            vertices.add(new Vec2(-0.8f,-0.5f));
       		vertices.add(new Vec2(0,1f));
            vertices.add(new Vec2(0.8f,-0.5f));
        try {

		  /* The Xwing is a static rectangular shape that will rotate and throw bullet from both sides */
		  		try{
	            Xwing = world.addPolygonalObject(vertices, BodyType.DYNAMIC, new Vec2(0, 0), 0.1f, new Sprite("Xwing", 0, Color.BLUE, null));
            } catch(InvalidPolygonException ex) {
                System.err.println(ex.getMessage());
                System.exit(-1);
            }

        } catch (InvalidSpriteNameException ex) {
            ex.printStackTrace();
            System.exit(-1);
        }

        /* Allocation of the drawing panel of  size 640x480 and of scale x10 */
        /* The DrawingPanel is a window on the world and can be of a different size than the world. (with scale x10, the world is currently 960x640) */
        /* The DrawingPanel panel is centered around the camera position (0,0) by default */
        this.panel = new DrawingPanel(world, new Dimension(500,500), 10f);
        /* Setting the color of the background */
        this.panel.setBackGroundIcon(new ImageIcon("./img/paysage.png"));
        
        /* Wrapping JFrame */
        JFrame frame = new JFrame("JBox2D GUI");
        frame.setMinimumSize(this.panel.getPreferredSize());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(this.panel, BorderLayout.CENTER); // Add DrawingPanel Panel to the frame
        frame.pack();
        frame.setVisible(true);
        this.panel.addKeyListener(this);
        this.panel.requestFocus();
   }

    /*
    * Simulation loop
    */
    public void run() {
	int numasteroid =0;
        try {
            float timeStep = 1/30.0f; // Each turn, we advance of 1/6O of second
            int msSleep = Math.round(1000*timeStep); // timeStep in milliseconds
            world.setTimeStep(timeStep); // Set the timeStep of the PhysicalWorld
	       int i = 0;
	       
	
            /* Launch the simulation */
            while(true) { // Infinite loop
					if(needToTeleportBall) {
						if(nameA.equals("topWall"))
							Xwing.setTransform(new Vec2(Xwing.getPosition().x,-23),Xwing.getAngle());
						if(nameA.equals("bottomWall"))
							Xwing.setTransform(new Vec2(Xwing.getPosition().x,23),Xwing.getAngle());
						if(nameA.equals("rightWall"))
							Xwing.setTransform(new Vec2(-23,Xwing.getPosition().y),Xwing.getAngle());
						if(nameA.equals("leftWall"))
							Xwing.setTransform(new Vec2(23,Xwing.getPosition().y),Xwing.getAngle());
                  needToTeleportBall = false;
		}
		if(CONTACT)
		{
			try{ try{
			if((nameA.substring(0,6).equals("bullet") || nameB.substring(0,6).equals("bullet"))){
				if(nameA != "topWall" && nameA != "bottomWall" && nameA != "rightWall" && nameA != "leftWall")
				{
					world.destroyObject(world.getObject(nameA));
					world.destroyObject(world.getObject(nameB));
				}
				System.out.println("caca");			
			}
			if((nameA == "Xwing") || (nameB == "Xwing") && (nameB == "asteroid") || (nameA == "asteroid")){
				world.destroyObject(world.getObject(nameA));
				world.destroyObject(world.getObject(nameB));
				break;
			}
		} catch(ObjectNameNotFoundException eb){}
	}catch (LockedWorldException e){}
		}
		if(world.getStepIdx()%90 == 0)
		{
			CreateAsteroid((int)(Math.random()*40-20), (int)(Math.random()*40-20),  (int)(Math.random()*2+1), (int)(Math.random()*20-10), (int)(Math.random()*20-10), numasteroid++);
			System.out.println("bou");
		}
              
                world.step(); // Move all objects
                Thread.sleep(msSleep);
                this.panel.updateUI();
            }
        } catch(InterruptedException ex) {
            System.err.println(ex.getMessage());
        }
	
    }



    public static void main(String[] args) {
        new Prgm().run();
    }
    
    public void keyPressed(KeyEvent e) {
      //  System.out.println("keyPressed "+ e.getKeyCode());
        boolean ctrl_pressed = false;
        if ((KeyEvent.CTRL_MASK & e.getModifiers()) != 0) { // Detect the CTRL modifier
            ctrl_pressed = true;
        }
        switch (e.getKeyCode()) {
        case KeyEvent.VK_Q:
        	if(ctrl_pressed) { // If Q AND CTRL are both pressed
        		System.out.println("Bye bye");
        		System.out.println(world);
        		System.exit(-1);
        		}
            break;
        	case KeyEvent.VK_UP:
        	  	Xwing.applyForceToCenter(Xwing.getWorldPoint(new Vec2(0,50f)));
            break;
        	case KeyEvent.VK_RIGHT:
        	  Xwing.setAngularVelocity(-5);
            break;
        	case KeyEvent.VK_LEFT:
        	 Xwing.setAngularVelocity(5);
            break;
         case KeyEvent.VK_SPACE:
				GunFire(NoBullet++);
            break;
        }
    }
    public void keyTyped(KeyEvent e) {
    }
    public void keyReleased(KeyEvent e) {
      switch (e.getKeyCode()) {
    		case KeyEvent.VK_RIGHT:
        	  Xwing.setAngularVelocity(0);
            break;
        case KeyEvent.VK_LEFT:
        	 Xwing.setAngularVelocity(0);
            break;
       }
    }
    
    public void beginContact(Contact contact) {
    		CONTACT = true;
        System.out.println("Objects are touching "+Sprite.extractSprite(contact.getFixtureA().getBody()).getName() +" "+Sprite.extractSprite(contact.getFixtureB().getBody()).getName() );

        /* Detect game logic */
        nameA = Sprite.extractSprite(contact.getFixtureA().getBody()).getName();
        nameB = Sprite.extractSprite(contact.getFixtureB().getBody()).getName();
        if(nameB.equals("Xwing")) {
            needToTeleportBall=true;
        }
              
    }

    /* Event when object are leaving */
    public void endContact(Contact contact) {
        System.out.println("Objects are leaving "+Sprite.extractSprite(contact.getFixtureA().getBody()).getName() +" "+Sprite.extractSprite(contact.getFixtureB().getBody()).getName() );
        CONTACT = false;
    }
    
    /* Advanced stuff */
    public void postSolve(Contact contact, ContactImpulse impulse) {}
    public void preSolve(Contact contact, Manifold oldManifold) {}
   

public void CreateAsteroid(int x, int y, int size, int dirx, int diry, int numasteroid){
    	try {
		     	Body asteroid = world.addCircularObject(size, BodyType.DYNAMIC, new Vec2(x,y) , 0, new Sprite("asteroid"+numasteroid, 1, Color.YELLOW, null));
			asteroid.setLinearVelocity(new Vec2(dirx, diry));

		           } catch (InvalidSpriteNameException ex) {
		           	System.err.println(ex.getMessage());
		           	System.exit(-1);
		           }
    
    }

    public void GunFire(int i){
    	try {
		     	Body bullet = world.addCircularObject(0.1f, BodyType.DYNAMIC, Xwing.getWorldPoint(new Vec2(0,3)) , 0, new Sprite("bullet"+(i), 1, Color.YELLOW, null));
				bullet.setLinearVelocity(Xwing.getWorldVector(new Vec2(0,40)));

		           } catch (InvalidSpriteNameException ex) {
		           	System.err.println(ex.getMessage());
		           	System.exit(-1);
		           }
    
    }
}
