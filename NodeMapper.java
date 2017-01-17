import java.util.ArrayList;
import java.util.Stack;
import java.awt.Rectangle;


public class NodeMapper
{
	private ArrayList<ArrayList<Node>> map = new ArrayList<ArrayList<Node>>();
	
	private ArrayList<Node> openList;
	private ArrayList<Node> closedList;
	
	
	NodeMapper(int total_width, int total_height, int seperation)
	{
		// construct 2 dimensional array of node coordinates
		for(int i=0; i <= total_width; i = i + seperation) // X
		{
			ArrayList<Node> innermap = new ArrayList<Node>();
			for(int j=0; j <= total_height; j = j + seperation)
			{
				innermap.add(new Node(new Integer(i), new Integer(j)));
			}
			map.add(innermap);
		}
		
		
		// draw node map
		for(int i=0; i < map.size(); i++)
		{
			for(int j=0; j < map.get(i).size(); j++)
			{
				if(i > 0)
				{
					if(j > 0)
						map.get(i).get(j).addPointer(map.get(i - 1).get(j - 1)); // add node above left
					
					map.get(i).get(j).addPointer(map.get(i - 1).get(j)); // add node left 
					
					if(j < map.get(i).size() - 1)
						map.get(i).get(j).addPointer(map.get(i - 1).get(j + 1)); // add node below left
				}
				
				if(j > 0)
					map.get(i).get(j).addPointer(map.get(i).get(j - 1)); // add node above
				
				if(j < map.get(i).size() - 1)
					map.get(i).get(j).addPointer(map.get(i).get(j + 1)); // add node below
				
				if(i < map.size() - 1)
				{
					if(j > 0)
						map.get(i).get(j).addPointer(map.get(i + 1).get(j - 1)); // add node above right
					
					map.get(i).get(j).addPointer(map.get(i + 1).get(j)); // add node right 
					
					if(j < map.get(i).size() - 1)
						map.get(i).get(j).addPointer(map.get(i + 1).get(j + 1)); // add node below right
				}
				
			}
		}
	}
	
	public void applyObstacle(Rectangle obstacle)
	{
		// update the nodes accessibility information
		for(int i=0; i < map.size(); i++)
		{
			for(int j=0; j < map.get(i).size(); j++)
			{
				Node node = map.get(i).get(j);
				if(obstacle.contains(node.getX(), node.getY()))
					node.setBlocked(true);
			}
		}
		
	}
	
	public ArrayList<ArrayList<Node>> getMap()
	{
		return map;
	}
	
	public Stack<Node> aStar(Node startNode, Node endNode)
	{
		openList = new ArrayList<Node>();
		closedList = new ArrayList<Node>();
		Stack<Node> path = new Stack<Node>(); // the shortest path
		
		// add start node to open list
		Node currentNode = startNode;
		closedList.add(currentNode);
		
		
		while(!currentNode.equals(endNode))
		{
			// get the open list for the current node
			ArrayList<Node> connectedNodes = currentNode.getconnectedNodes();
			for(int i=0; i < connectedNodes.size(); i++)
			{
				int xdiff;
				int ydiff;
				int gcost;
				int hcost;
				Node cNode = connectedNodes.get(i);
				
				if(cNode.getgCost() == 0) // gcost
				{
					// this is the difference between the current node x and y and this node x and y
					xdiff = Math.abs(cNode.getX() - currentNode.getX());
					ydiff = Math.abs(cNode.getY() - currentNode.getY());
					gcost = 0;
					if(ydiff > 0 && xdiff > 0)
						gcost = (int)((double)(xdiff + ydiff) / 1.4); // 1.4 is rough diagonal length of a square
					else 
						gcost = xdiff + ydiff; // length of one side
					
					cNode.setgCost(gcost);
				}
				if(cNode.gethCost() == 0) // h cost
				{
					// this is the difference between the oNode and the destination node
					xdiff = cNode.getX() - endNode.getX();
					ydiff = cNode.getY() - endNode.getY();
					hcost = (int)Math.sqrt((double)( Math.pow(xdiff, 2) + Math.pow(ydiff, 2) ));
					cNode.sethCost(hcost);
				}
				if(cNode.getfCost() == 0)
				{
					// this is the gcost and the hcost combined
					cNode.setfCost(cNode.getgCost() + cNode.gethCost());
				}
				
				
				// if the connected node is not within an obstacle
				if(!cNode.isBlocked())
				{
					// if the connected node is not in the closed list
					if(!closedList.contains(cNode))
					{
						
						// if the connected node is not in the open list
						if(!openList.contains(cNode)) 
						{
							
							
							cNode.setaStarParent(currentNode);
							openList.add(cNode); // add it to the open list
						}
						else{ // if it is already in the open list
							
							// check to see if its current gcost is less than the new gcost of the parent and the old gcost
							gcost = cNode.getgCost() + currentNode.getgCost();
							if(gcost < cNode.getgCost())
							{
								// if so, make the current node its new parent and recalculate the gcost, and fcost
								cNode.setaStarParent(currentNode);
								cNode.setgCost(gcost);
								cNode.setfCost(cNode.getgCost() + cNode.gethCost());
							}
						}
					} // end closed list check
				} // end blocked nodes
			}
			
			// at this point the open list has been updated to reflect new parents and costs
			
			// loop through the open list
			Node cheapOpenNode = null;
			for(int i=0; i < openList.size(); i++)
			{
				// compare the openList nodes for the lowest F Cost
				Node oNode = openList.get(i);
				if(cheapOpenNode == null) // initialize our cheapest open node
				{
					cheapOpenNode = oNode;
					continue;
				}
				
				if(oNode.getfCost() < cheapOpenNode.getfCost())
				{
					// we found a cheaper open list node
					cheapOpenNode = oNode;
				}
			}
			
			// now we have the node from the open list that has the cheapest f cost
			// move it to the closed list and set it as the current node
			closedList.add(cheapOpenNode);
			openList.remove(cheapOpenNode);
			currentNode = cheapOpenNode;
		}
		
		// we have found the end node
		// Loop from the current/end node moving back through the parents until we reach the start node
		// add those to the list and we have our path
		boolean moreParents = true;
		Node workingNode = currentNode;
		while(moreParents)
		{
			if(workingNode.getaStarParent() == null)
			{
				path.push(workingNode);
				moreParents = false;
			}
			else{
				path.push(workingNode);
				workingNode = workingNode.getaStarParent();
			}
		}
		
		// before we end, reset all our costs and parents in our nodes
		resetNodes();
		
		// reverse so we can have starting node going to end node
		//Collections.reverse(path);
		
		return path;
	}
	
	private void resetNodes()
	{
		for(int i=0; i < map.size(); i++)
		{
			for(int j=0; j < map.get(i).size(); j++)
			{
				map.get(i).get(j).resetaStar();
			}
		}
	}
	
	public Node findNode(int x, int y)
	{
		for(int i=0; i < map.size(); i++)
		{
			for(int j=0; j < map.get(i).size(); j++)
			{
				if(map.get(i).get(j).getX().equals(x) && map.get(i).get(j).getY().equals(y))
					return map.get(i).get(j);
			}
		}
		
		return null;
	}
}
