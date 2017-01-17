import java.awt.Rectangle;


public abstract class Being
{
	private Rectangle rectangle;
	private int x, y, width, height;
	private Node node;
	Being(int x, int y, int width, int height)
	{
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		
		// create our rectangle
		this.rectangle = new Rectangle((x-(width/2)), (y-(height/2)), width, height);
	}
	
	public void updateLocation(int x, int y)
	{
		this.rectangle.setLocation(x-(width/2), y-(height/2));
		this.x = x;
		this.y = y;
	}
	
	public Rectangle getBounds()
	{
		return rectangle.getBounds();
	}
	
	public Node getNode()
	{
		return this.node;
	}
	
	public void setNode(Node node)
	{
		this.node = node;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getX() {
		return x;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getY() {
		return y;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getWidth() {
		return width;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getHeight() {
		return height;
	}
}
