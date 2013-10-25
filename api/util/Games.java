/*
	Title:  Games
	Author: Matthew Boyette
	Date:   1/21/2012
	
	This class is merely a collection of useful static methods that support code recycling. Specifically, this 
	class offers methods which would be useful for games, animation, or utilities that are meant to accompany 
	games and animation.
*/
package api.util;

import api.util.Support;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;

public class Games
{
	public static class Vertex2D extends Point2D.Double
	{
		private static final long serialVersionUID = 1L;
		
		private LinkedList<Vertex2D> verticesWithinLOS = null;
		
		public Vertex2D(final double x, final double y)
		{
			super(x,y);
			this.verticesWithinLOS = new LinkedList<Vertex2D>();
		}
		
		public LinkedList<Vertex2D> getVerticesWithinLOS()
		{
			return this.verticesWithinLOS;
		}
		
		public boolean isWithinLineOfSightOf(final Vertex2D destination, final LinkedList<Edge2D> edgesOfObstacles)
		{
			/*
				If the path (the imaginary line segment from this vertex to a destination vertex) intersects the edge of an obstacle,
				there are two possible main cases:
					(a) the path intersects the edge at one or both endpoints, and the destination could possibly be within LOS.
					(b) the path intersects the edge at a point somewhere between the two endpoints, and the destination is not within LOS.
				
				If (a) is true, there are three possible sub cases:
					(1) the path intersects only one endpoint and runs around the edge and away from the obstacle; the destination is within LOS.
					(2) the path intersects both of the endpoints and runs parallel along the edge of the obstacle; the destination is within LOS
					(3) the path intersects the endpoint of an even number of edges greater than two and cuts through the obstacle; the destination is not within LOS.
			*/
			
			// Since there are fewer bad cases where the destination is not within LOS, we will default to within LOS and test only for the two bad cases.
			// If there were more bad cases than good cases, we would do the opposite and default false and test for cases that are true.
			boolean isWithinLOS = true;
			
			// Keep track of the number of times that the path intersects an edge at only one endpoint.
			int numEdgesIntersectedAtOnlyOneEndpoint = 0;
			
			// Imaginary edge representing the path. 
			Edge2D e1 = new Edge2D(this, destination);
			
			// Test the path against each edge of the obstacles.
			for (int i = 0; i < edgesOfObstacles.size(); i++)
			{
				Edge2D e2 = edgesOfObstacles.get(i);
				
				// If the path intersects the edge...
				if (e1.intersectsLine(e2))
				{
					// Determine some facts about the endpoints of the path and how they compare with the endpoints for the edges.
					boolean destinationIsP1 = ((destination.getX() == e2.getX1()) && (destination.getY() == e2.getY1()));
					boolean destinationIsP2 = ((destination.getX() == e2.getX2()) && (destination.getY() == e2.getY2()));
					boolean sourceIsP1      = ((this.getX() == e2.getX1()) && (this.getY() == e2.getY1()));
					boolean sourceIsP2      = ((this.getX() == e2.getX2()) && (this.getY() == e2.getY2()));
					
					// If either the source or destination are equal to both endpoints of a single edge then an error has occurred.
					boolean destinationError = (destinationIsP1 && destinationIsP2);
					boolean sourceError      = (sourceIsP1 && sourceIsP2);
					boolean pathfindingError = (destinationError || sourceError);
					
					if (pathfindingError)
					{
						Support.displayException(null, new RuntimeException("Critical error: pathfinding intersection test failed."), true);
					}
					
					// Good case: Path intersects both endpoints of the edge and runs along the side of an obstacle.
					if ((destinationIsP1 && sourceIsP2) ^ (destinationIsP2 && sourceIsP1))
					{
						// We ignore good cases.
						continue;
					}
					
					// Good case: Path intersects only one endpoint of the edge.
					if ((destinationIsP1 ^ sourceIsP2) ^ (destinationIsP2 ^ sourceIsP1))
					{
						// Keep track of these so we can determine another bad case further along in the test but otherwise ignore them.
						numEdgesIntersectedAtOnlyOneEndpoint++;
						continue;
					}
					
					// Bad case: Base case for intersections. If an intersection occurred and didn't qualify for either above good case, it's marked as bad.
					isWithinLOS = false;
				}
			}
			
			// Bad case: a single edge intersected more than two edges of obstacles at only one endpoint which means it cut through the obstacle.
			if (numEdgesIntersectedAtOnlyOneEndpoint > 2)
			{
				isWithinLOS = false;
			}
			
			return isWithinLOS;
		}
	}
	
	public static class Edge2D extends Line2D.Double
	{
		private static final long serialVersionUID = 1L;
		
		private double weight = 0;
		
		public Edge2D(final Vertex2D v1, final Vertex2D v2)
		{
			super(v1, v2);
			this.weight = this.getP1().distance(this.getP2());
		}
		
		public double getWeight()
		{
			return this.weight;
		}
		
		public String toString()
		{
			return "(" + this.getP1().toString() + " => " + this.getP2().toString() + ")";
		}
	}
	
	/*
		This class uses the A* search algorithm to find the shortest path from the start point to the goal point
		within a 2-dimensional plane while avoiding polygonal obstacles.
	*/
	public static class AStarShortestPath2D
	{
		// This sub-class is merely an extension of the Vertex2D class specifically for the A* algorithm.
		public static class Node extends Vertex2D
		{
			private static final long serialVersionUID = 1L;
			
			private LinkedList<Node> nodesWithinLOS = null;
			private Node             predecessor = null;
			private double           cost = 0;
			
			public Node(final double x, final double y)
			{
				super(x,y);
				this.nodesWithinLOS = new LinkedList<Node>();
				this.convertVerticesToNodes(this.getVerticesWithinLOS(), this.getNodesWithinLOS());
			}
			
			protected void convertVerticesToNodes(final LinkedList<Vertex2D> vertices, final LinkedList<Node> nodes)
			{
				Vertex2D vertex = null;
				Node     node   = null;
				
				for (int i = 0; i < vertices.size(); i++)
				{
					vertex = vertices.get(i);
					node = (Node)vertex;
					nodes.add(node);
				}
			}
			
			public double getCost()
			{
				return this.cost;
			}
			
			public Node getPredecessor()
			{
				return this.predecessor;
			}
			
			public LinkedList<Node> getNodesWithinLOS()
			{
				return this.nodesWithinLOS;
			}
			
			public void setCost(final double cost)
			{
				this.cost = cost;
			}
			
			public void setPredecessor(final Node predecessor)
			{
				this.predecessor = predecessor;
			}
		}
		
		// This sub-class is a ready-made template used to display the results from the AStarShortestPath2D algorithm graphically.
		public static class MapDisplay extends Canvas
		{
			private static final long serialVersionUID = 1L;
			
			private AStarShortestPath2D shortestPath  = null;
			private boolean             debugMode     = false;
			private double              magnification = 1;
			
			public MapDisplay(final AStarShortestPath2D shortestPath)
			{
				super();
				this.setBackground(Color.WHITE);
				this.setForeground(Color.BLACK);
				this.shortestPath = shortestPath;
				this.debugMode = shortestPath.getDebugMode();
				this.repaint();
			}
			
			public MapDisplay(final Color background, final Color foreground, final AStarShortestPath2D shortestPath)
			{
				super();
				this.setBackground(background);
				this.setForeground(foreground);
				this.shortestPath = shortestPath;
				this.debugMode = shortestPath.getDebugMode();
				this.repaint();
			}
			
			public MapDisplay(final Color background, final Color foreground, final AStarShortestPath2D shortestPath, final double magnification)
			{
				super();
				this.setBackground(background);
				this.setForeground(foreground);
				this.shortestPath = shortestPath;
				this.debugMode = shortestPath.getDebugMode();
				this.magnification = magnification;
				this.repaint();
			}
			
			public void paint(final Graphics g)
			{
				Graphics2D g2D = (Graphics2D)g;
				Edge2D     e   = null;
				
				for (int i = 0; i < this.shortestPath.getEdges().size(); i++)
				{
					e = this.shortestPath.getEdges().get(i);
					
					double origX1 = e.getX1();
					double origX2 = e.getX2();
					double origY1 = e.getY1();
					double origY2 = e.getY2();
					
					e.setLine(origX1*this.magnification, origY1*this.magnification, origX2*this.magnification, origY2*this.magnification);
					g2D.draw(e);
				}
				
				Ellipse2D v = new Ellipse2D.Double(this.shortestPath.getStartPoint().getX()*this.magnification, this.shortestPath.getStartPoint().getY()*this.magnification, 3, 3);
				
				g2D.draw(v);
				g2D.fill(v);
				g2D.drawString("S", (float)(v.getCenterX()-4), (float)(v.getCenterY()+15.0));
				
				v = new Ellipse2D.Double(this.shortestPath.getGoalPoint().getX()*this.magnification, this.shortestPath.getGoalPoint().getY()*this.magnification, 3, 3);
				
				g2D.draw(v);
				g2D.fill(v);
				g2D.drawString("G", (float)(v.getCenterX()-4), (float)(v.getCenterY()+15.0));
				
				if (this.debugMode)
				{
					Node withinLOS = null;
					
					for (int i = 0; i < this.shortestPath.getStartPoint().getNodesWithinLOS().size(); i++)
					{
						withinLOS = this.shortestPath.getStartPoint().getNodesWithinLOS().get(i);
						v = new Ellipse2D.Double(withinLOS.getX()*this.magnification, withinLOS.getY()*this.magnification, 3, 3);
						
						g2D.draw(v);
						g2D.fill(v);
					}
					
					for (int i = 0; i < this.shortestPath.getGoalPoint().getNodesWithinLOS().size(); i++)
					{
						withinLOS = this.shortestPath.getGoalPoint().getNodesWithinLOS().get(i);
						v = new Ellipse2D.Double(withinLOS.getX()*this.magnification, withinLOS.getY()*this.magnification, 3, 3);
						
						g2D.draw(v);
						g2D.fill(v);
					}
				}
				
				// TODO: Draw solution on the canvas.
			}
		}
		
		private Node                 startPoint = null; // Start point.
		private Node                 goalPoint  = null; // Goal point.
		private LinkedList<Node>     vertices   = null; // List of all vertices/points/nodes of interest (including start and goal point).
		private LinkedList<Node>     solution   = new LinkedList<Node>(); // List of vertices which comprise the solution.
		private LinkedList<Edge2D>   edges      = null; // List of all edges for the polygonal obstacles.
		private boolean              debugMode  = false;
		
		public AStarShortestPath2D(final boolean debugMode, final Node startPoint, final Node goalPoint, final LinkedList<Node> vertices, final LinkedList<Edge2D> edges)
		{
			this.startPoint = startPoint;
			this.goalPoint = goalPoint;
			this.vertices = vertices;
			this.edges = edges;
			this.debugMode = debugMode;
			this.determineLineOfSight();
			this.solve();
		}
		
		public AStarShortestPath2D(final boolean debugMode, final String filePath)
		{
			this.vertices = new LinkedList<Node>();
			this.edges = new LinkedList<Edge2D>();
			this.debugMode = debugMode;
			this.parseMapFile(filePath);
			this.determineLineOfSight();
			this.solve();
		}
		
		public LinkedList<Node> actions(final Node n)
		{
			return n.getNodesWithinLOS();
		}
		
		// This method determines which vertices are within line of sight of each other.
		protected void determineLineOfSight()
		{	
			for (int i = 0; i < this.getVertices().size(); i++)
			{
				for (int j = 0; j < this.getVertices().size(); j++)
				{
					if (i != j)
					{
						if (this.getVertices().get(i).isWithinLineOfSightOf(this.getVertices().get(j), this.getEdges()))
						{
							this.getVertices().get(i).getVerticesWithinLOS().add(this.getVertices().get(j));
						}
					}
				}
			}
		}
		
		// Evaluation function. This method takes a node 'n' and calculates the estimated total cost of the path through 'n' to the goal.
		protected double f(final Node n)
		{
			return (this.g(n) + this.h(n));
		}
		
		// This method takes a node 'n' and calculates the cost so far to reach 'n'. Without this, A* search becomes greedy best-first search.
		protected double g(final Node n)
		{
			return n.getCost();
		}
		
		public LinkedList<Edge2D> getEdges()
		{
			return this.edges;
		}
		
		public boolean getDebugMode()
		{
			return this.debugMode;
		}
		
		public Node getGoalPoint()
		{
			return this.goalPoint;
		}
		
		public LinkedList<Node> getSolution()
		{
			return this.solution;
		}
		
		public Node getStartPoint()
		{
			return this.startPoint;
		}
		
		public LinkedList<Node> getVertices()
		{
			return this.vertices;
		}
		
		// Heuristic function. This method takes a node 'n' and calculates the estimated cost (straight line distance) from 'n' to the goal.
		protected double h(final Node n)
		{
			return n.distance(this.getGoalPoint());
		}
		
		protected void parseMapFile(final String filePath) throws IllegalArgumentException
		{
			if ((filePath == null) || filePath.isEmpty())
			{
				throw new IllegalArgumentException();
			}
			
			Scanner        inputStream       = null;
			String         lineOfText        = null;
			String[]       coordinateStrings = null;
			String[]       vertexStrings     = null;
			LinkedList<Vertex2D> tempVertexList    = null;
			
			try
			{
				inputStream = new Scanner(new File(filePath));
				
				// The first line is the pair of coordinates for the start point, separated by a comma.
				if (inputStream.hasNextLine())
				{
					lineOfText = inputStream.nextLine().trim();
					coordinateStrings = lineOfText.split(",");
					this.startPoint = new Node(Double.parseDouble(coordinateStrings[0]), Double.parseDouble(coordinateStrings[1]));
				}
				
				// The second line is the pair of coordinates for the goal point, separated by a comma.
				if (inputStream.hasNextLine())
				{
					lineOfText = inputStream.nextLine().trim();
					coordinateStrings = lineOfText.split(",");
					this.goalPoint = new Node(Double.parseDouble(coordinateStrings[0]), Double.parseDouble(coordinateStrings[1]));
				}
				
				// Add the start point to the list of vertices.
				this.getVertices().add(this.getStartPoint());
				
				
				// Each additional line contains the pairs of coordinates for the vertices in each polygon.
				// Each coordinate pair is separated by a semi-colon. The actual coordinates are separated by commas.
				while (inputStream.hasNextLine())
				{
					tempVertexList = new LinkedList<Vertex2D>();
					lineOfText = inputStream.nextLine().trim();
					vertexStrings = lineOfText.split(";");
					
					for (int i = 0; i < vertexStrings.length; i++)
					{
						coordinateStrings = vertexStrings[i].split(",");
						Node v = new Node(Double.parseDouble(coordinateStrings[0]), Double.parseDouble(coordinateStrings[1]));
						tempVertexList.add(v);
						this.getVertices().add(v);
					}
					
					for (int i = 0; i < tempVertexList.size(); i++)
					{
						if (i == (tempVertexList.size() - 1))
						{
							Edge2D e = new Edge2D(tempVertexList.get(i), tempVertexList.get(0));
							this.getEdges().add(e);
						}
						else
						{
							Edge2D e = new Edge2D(tempVertexList.get(i), tempVertexList.get(i+1));
							this.getEdges().add(e);
						}
					}
				}
				
				// Add the goal point to the list of vertices.
				this.getVertices().add(this.getGoalPoint());
			}
			catch(FileNotFoundException exception)
			{
				Support.displayException(null, exception, false);
			}
			finally
			{
				if (inputStream != null)
				{
					inputStream.close();
					inputStream = null;
				}
			}
		}
		
		protected void reconstruct_path()
		{
			
		}
		
		// This method is my implementation of the A* algorithm. It returns true if it reaches the desired destination, otherwise it returns false.
		// TODO: http://en.wikipedia.org/wiki/A*_search_algorithm
		protected void solve()
		{	
			
		}
	}
	
	// Pretty self-explanatory. Returns a random integer between 'min' and 'max'.
	// The user tells the method whether the desired maximum is inclusive or exclusive.
	// Exclusive range in interval notation: [min, max)
	// Inclusive range in interval notation: [min, max]
	public static int getRandomInteger(final int min, final int max, final boolean isMaxInclusive)
	{
		Random randomGenerator = new Random(System.nanoTime());

		if (isMaxInclusive)
		{
			return (randomGenerator.nextInt(max - min + 1) + min);
		}
		else
		{
			return (randomGenerator.nextInt(max - min) + min);
		}
	}
	
	// Pretty self-explanatory. Takes an array of integers, and returns the sum.
	public static int getSumFromIntegerArray(final int[] arrayOfIntegers)
	{
		int sum = 0;

		for (int i = 0; i < arrayOfIntegers.length; i++)
		{
			sum += arrayOfIntegers[i];
		}

		return sum;
	}
	
	/*
		This method is useful in games like Dungeons and Dragons. The user provides the number of dice and the number 
		of sides each die has. The resulting array contains the result of each die separately and the final element in 
		the array is the sum of all the dice.
	*/
	public static int[] throwDice(final int numberOfDice, final int numberOfSides)
	{
		int[] resultsArray = new int[numberOfDice+1];

		for (int i = 0; i < numberOfDice; i++)
		{
			resultsArray[i] = getRandomInteger(1, numberOfSides, true);
		}

		resultsArray[numberOfDice] = getSumFromIntegerArray(resultsArray);

		return resultsArray;
	}
}