import java.applet.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.*;
import java.util.concurrent.Semaphore;



public class Pathfinder extends Applet implements Runnable, KeyListener {
	
	private static final long serialVersionUID = 1L;
	private static int WINDOW_WIDTH = 500;
	private static int WINDOW_HEIGHT = 500;
	
	private Hunter hunter;
	private int x_pos_hunter = 0;
	private int y_pos_hunter = 0;
	private int width_hunter = 6;
	private int height_hunter = 6;
	
	private Prey prey;
	private int x_pos_prey = 480;
	private int y_pos_prey = 400;
	private int width_prey = 4;
	private int height_prey = 4;
	
	private Rectangle obstacle1;
	private Rectangle obstacle2;
	
	ArrayList<ArrayList<Node>> map;
	NodeMapper nodeMapper;
	Stack<Node> shortestPath;
	
	Semaphore nodeAccessSemaphore;
	
	// double buffering
	private Image dbImage;
	private Graphics dbg;
	
	// background
	private Image backImage;
	
	// Sounds
	private AudioClip eaten;
	
	public void init()
	{
		// Create concurrency needs
		nodeAccessSemaphore = new Semaphore(1);
		
		// create node map
		nodeMapper = new NodeMapper(WINDOW_WIDTH, WINDOW_HEIGHT, 1);
		map = nodeMapper.getMap();
		//System.out.println(map);
		
		// create obstacle
		obstacle1 = new Rectangle(250-80, 30, 40, 360);
		obstacle2 = new Rectangle(55, 40, 280, 6);
		
		// apply obstacle to node map
		nodeMapper.applyObstacle(obstacle1);
		nodeMapper.applyObstacle(obstacle2);
		
		// create hunter
		hunter = new Hunter(x_pos_hunter, y_pos_hunter, width_hunter, height_hunter);
		hunter.setNode(nodeMapper.findNode(x_pos_hunter, y_pos_hunter));
		
		// create prey
		prey = new Prey(x_pos_prey, y_pos_prey, width_prey, height_prey);
		prey.setNode(nodeMapper.findNode(x_pos_prey, y_pos_prey));
		
		// create shortest path
		Node stestStart = hunter.getNode();
		Node stestEnd = prey.getNode();
		shortestPath = nodeMapper.aStar(stestStart, stestEnd);
		
		// build window interface
		addKeyListener(this);
		setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		setBackground(Color.gray);
		eaten = getAudioClip(getCodeBase(), "giant_bug_roar.au");
		backImage = getImage(getCodeBase(), "background.jpg");
	}
	
	public void start()
	{
		Thread th = new Thread(this);
		th.start();
	}
	
	public void stop()
	{
		
	}
	
	public void destroy()
	{
		
	}
	
	public void paint(Graphics g)
	{
		// background
		g.drawImage(backImage, 0, 0, this);
		
		
		// draw node map
		/*
		Iterator<ArrayList<Node>> anitr = map.iterator();
		while(anitr.hasNext())
		{
			ArrayList<Node> an = anitr.next();
			Iterator<Node> nitr = an.iterator();
			while(nitr.hasNext())
			{
				Node node = nitr.next();
				
				g.setColor(Color.gray);
				g.fillOval(node.getX(), node.getY(), 2, 2);
				
				Iterator<Node> cnitr = node.getconnectedNodes().iterator();
				while(cnitr.hasNext())
				{
					Node cNode = cnitr.next();
					g.drawLine(node.getX(), node.getY(), cNode.getX(), cNode.getY());
				}
			}
		}
		*/
		
		// draw obstacle
		g.setColor(Color.cyan);
		g.drawRect(obstacle1.x, obstacle1.y, obstacle1.width, obstacle1.height);
		g.drawRect(obstacle2.x, obstacle2.y, obstacle2.width, obstacle2.height);
		
		// draw shortest path test
		Iterator<Node> itr = shortestPath.iterator();
		Node lastNode = null;
		while(itr.hasNext())
		{
			
			Node node = itr.next();
			
			if(lastNode == null)
			{
				lastNode = node;
			}
			else{
				g.setColor(Color.magenta);
				g.drawLine(lastNode.getX(), lastNode.getY(), node.getX(), node.getY());
				lastNode = node;
			}
		}
		
		// prey
		Rectangle preyRectangle = prey.getBounds();
		g.setColor(Color.green);
		g.fillRect(preyRectangle.x, preyRectangle.y,  preyRectangle.width,  preyRectangle.height);
		
		// hunter
		Rectangle hunterRectangle = hunter.getBounds();
		g.setColor(Color.red);
		g.fillRect(hunterRectangle.x, hunterRectangle.y,  hunterRectangle.width,  hunterRectangle.height);
		
		
	}
	
	// This overwrites the AWT update method
	// this is called during repaint
	// this is asynchronous
	// if more than one request is waiting before it is processed, those method calls are collapsed
	// - this means that the paint is drawn as a union of all updates.
	public void update(Graphics g)
	{
		// init buffer
		if(dbImage == null)
		{
			dbImage = createImage(this.getSize().width, this.getSize().height);
			dbg = dbImage.getGraphics();
		}
		
		// clear the screen in the background
		dbg.setColor(this.getBackground());
		dbg.fillRect(0, 0, this.getSize().width, this.getSize().height);
		
		// draw elements in BG
		dbg.setColor(getForeground());
		paint(dbg);
		
		// draw image on screen
		g.drawImage(dbImage, 0, 0, this);
	}
	
	@Override
	public void run() {
		
		// lower thread priority
		Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
		
		// get the shortest path between the hunter and the prey
		shortestPath = nodeMapper.aStar(hunter.getNode(), prey.getNode());
		Node closestNode = null;
		
		int loopCount = 0;
		while(true)
		{

			// get the closest node
			if(closestNode == null)
				closestNode = shortestPath.peek();
			
			// see if we've reached the nearest node on the shortest path
			if(hunter.getX() == closestNode.getX() && hunter.getY() == closestNode.getY())
			{
				// remove that node from the closest path and update the hunters node
				shortestPath.pop();
				hunter.setNode(closestNode);
				if(shortestPath.size() > 0)
					closestNode = shortestPath.peek(); // get next node
			}
			
			// move hunter towards prey
			int x_move_hunter = xHunt(hunter.getX(), closestNode.getX());
			int y_move_hunter = yHunt(hunter.getY(), closestNode.getY());
			hunter.updateLocation(hunter.getX() + x_move_hunter, hunter.getY() + y_move_hunter);
			
			
			
			// move the prey randomly, but only so often
			if( (loopCount % 90) == 1)
			{
				int x_move_prey = xPrey();
				int y_move_prey = yPrey();
				while(obstacle1.contains(x_move_prey, y_move_prey) || obstacle2.contains(x_move_prey, y_move_prey))
				{
					x_move_prey = xPrey();
					y_move_prey = yPrey();
				}
				prey.updateLocation(x_move_prey, y_move_prey);
				prey.setNode(nodeMapper.findNode(x_move_prey, y_move_prey));
				shortestPath = nodeMapper.aStar(hunter.getNode(), prey.getNode());
				closestNode = null;
			}
			
			// detect collision
			Rectangle hR = hunter.getBounds();
			Rectangle pR = prey.getBounds();
			if(hR.intersects(pR))
			{
				// we have collided
				// randomly move the hunter
				eaten.play();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {}
				int x_move_prey = xPrey();
				int y_move_prey = yPrey();
				while(obstacle1.contains(x_move_prey, y_move_prey) || obstacle2.contains(x_move_prey, y_move_prey))
				{
					x_move_prey = xPrey();
					y_move_prey = yPrey();
				}
				hunter.updateLocation(xPrey(), yPrey());
				hunter.setNode(nodeMapper.findNode(hunter.getX(), hunter.getY()));
				shortestPath = nodeMapper.aStar(hunter.getNode(), prey.getNode());
				closestNode = null;
			}
			
			
			// repaint the window
			repaint(); // will call AWT update(), which has been overridden
			
			// sleep
			try
			{
				Thread.sleep(10);
			}
			catch (InterruptedException e)
			{ /* do nothing */ }
			
			Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
			
			loopCount++;
		}

	}
	
	// takes x coordinates for hunter and prey
	// calculates the proper direction to reach the prey (+1, -1)
	private int xHunt(int x_pos_hunter, int x_pos_prey)
	{
		if(x_pos_hunter < x_pos_prey)
		{
			return 1; // move to the right
		}
		else if(x_pos_hunter > x_pos_prey)
		{
			return -1; // move to the left
		}
		else
		{
			return 0; // we are matching
		}
	}
	
	private int yHunt(int y_pos_hunter, int y_pos_prey)
	{
		if(y_pos_hunter < y_pos_prey)
		{
			return 1; // move down
		}
		else if(y_pos_hunter > y_pos_prey)
		{
			return -1; //move up
		}
		else
		{
			return 0; // we are matching
		}
	}
	
	
	private int xPrey()
	{
		// random x
		Random generator = new Random();
		int r = generator.nextInt(WINDOW_WIDTH/10);
		return r*10;
	}
	private int yPrey()
	{
		// random x
		Random generator = new Random();
		int r = generator.nextInt(WINDOW_HEIGHT/10);
		return r*10;
	}
	
	public void keyPressed(KeyEvent e)
	{
		System.out.println(e);
		if(e.getKeyCode() == 87)
		{
			prey.setY(prey.getY() + 10);
		}
		
	}
	
	public void keyReleased(KeyEvent e)
	{
	}
	
	public void keyTyped(KeyEvent e)
	{
	
	}
	
}