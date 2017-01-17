import java.util.ArrayList;


public class Node {
	
	private Integer x;
	private Integer y;
	private ArrayList<Node> connectedNodes;
	private boolean isBlocked; // does this node fall within an obstacle etc.
	
	// A Star details
	private int fCost = 0;
	private int gCost = 0;
	private int hCost = 0;
	private Node aStarParent;
	
	Node(Integer x, Integer y)
	{
		this.x = x;
		this.y = y;
		
		this.connectedNodes = new ArrayList<Node>();
		
		setBlocked(false);
	}
	
	public Integer getX()
	{
		return this.x;
	}
	
	public Integer getY()
	{
		return this.y;
	}
	
	public void addPointer(Node node)
	{
		this.connectedNodes.add(node);
	}
	
	public String toString()
	{
		String toString = "";
		toString = "<" + this.x.toString() + ", " + this.y;
		toString += "> G="+this.gCost+" H="+this.hCost+" F="+this.fCost;
		
		return toString;
	}
	
	public boolean equals(Node compareNode)
	{
		// two nodes are equal if they have the same coordinates
		if(this.x == compareNode.getX() && this.y == compareNode.getY())
			return true;
		return false;
	}

	public void setfCost(int fCost) {
		this.fCost = fCost;
	}

	public int getfCost() {
		return fCost;
	}

	public void setgCost(int gCost) {
		this.gCost = gCost;
	}

	public int getgCost() {
		return gCost;
	}

	public void sethCost(int hCost) {
		this.hCost = hCost;
	}

	public int gethCost() {
		return hCost;
	}
	

	public ArrayList<Node> getconnectedNodes() {
		return connectedNodes;
	}

	public void setaStarParent(Node aStarParent) {
		this.aStarParent = aStarParent;
	}

	public Node getaStarParent() {
		return aStarParent;
	}
	
	public void resetaStar()
	{
		this.gCost = 0;
		this.hCost = 0;
		this.fCost = 0;
		this.aStarParent = null;
	}
	

	public ArrayList<Node> getConnectedNodes() {
		return connectedNodes;
	}

	public void setBlocked(boolean isBlocked) {
		this.isBlocked = isBlocked;
	}

	public boolean isBlocked() {
		return isBlocked;
	}
}
