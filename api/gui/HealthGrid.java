/*
	Title:  Health/Infection Grid (COT 3210 Programming Project)
	Author: Matthew Boyette
	Date:   3/22/2014
	
	This class provides a graphical display to the user that shows a simulation of an infection spreading through a population operating under 
	certain fundamental rules.
*/

package api.gui;

import api.util.*;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridLayout;

import javax.swing.JPanel;

public class HealthGrid extends JPanel
{
	private static final long	serialVersionUID	= 1L;
	
	private Cell[][]	gridOfCells	= null;
	private int			numColumns	= 0;
	private int			numRows		= 0;
	
	public static enum State
	{
		ALIVE, EMPTY, INFECTED
	}
	
	public static class Cell extends JPanel
	{
		private static final long	serialVersionUID	= 1L;
		
		private CellContent content = null;
		private int 		column	= 0;
		private int 		row		= 0;
		
		public Cell(final int x, final int y)
		{
			this.setColumn(x);
			this.setRow(y);
			this.setContent(new CellContent(this));
			this.setLayout(new FlowLayout());
			this.add(this.getContent());
		}
		
		public Cell(final int x, final int y, final State initialState)
		{
			this.setColumn(x);
			this.setRow(y);
			this.setContent(new CellContent(this, initialState));
			this.setLayout(new FlowLayout());
			this.add(this.getContent());
		}

		public final CellContent getContent()
		{
			return this.content;
		}
		
		public final int getColumn()
		{
			return this.column;
		}
		
		public final int getRow()
		{
			return this.row;
		}
		
		public final void setContent(final CellContent content)
		{
			this.content = content;
		}
		
		public final void setColumn(final int column)
		{
			this.column = column;
		}
		
		public final void setRow(final int row)
		{
			this.row = row;
		}
	}
	
	public static class CellContent extends JPanel
	{
		private static final long	serialVersionUID	= 1L;
		
		private Cell	parent	= null;
		private State	state	= null;
		
		public CellContent(final Cell parent)
		{
			this.setParent(parent);
			this.setState(State.EMPTY);
		}
		
		public CellContent(final Cell parent, final State initialState)
		{
			this.setParent(parent);
			this.setState(initialState);
		}
		
		public final Cell getParent()
		{
			return this.parent;
		}
		
		public final State getState()
		{
			return this.state;
		}
		
		protected void paintComponent(final Graphics g)
		{
			super.paintComponent(g);
			
			switch (this.getState())
			{
				case ALIVE:
					
					g.setColor(Color.GREEN);
					break;
					
				case EMPTY:
					
					g.setColor(Color.BLACK);
					break;
					
				case INFECTED:
					
					g.setColor(Color.RED);
					break;
					
				default:
					
					break;
			}
			
			g.drawRect(this.getX(), this.getY(), this.getWidth(), this.getHeight());
			g.fillRect(this.getX(), this.getY(), this.getWidth(), this.getHeight());
		}
		
		public void randomize()
		{
			final int STATE = Games.getRandomInteger(1, 2, true);
			
			switch (STATE)
			{
				case 1:
					
					this.setState(State.ALIVE);
					break;
					
				case 2:
					
					this.setState(State.EMPTY);
					break;
					
				case 3:
					
					this.setState(State.INFECTED);
					break;
					
				default:
					
					break;
			}
		}
		
		public final void setParent(final Cell parent)
		{
			this.parent = parent;
		}
		
		public final void setState(final State state)
		{
			this.state = state;
			this.repaint();
		}
	}
	
	public HealthGrid(final int rows, final int columns)
	{
		int newRows = 1, newColumns = 1;
		
		if (rows > 1)
		{
			newRows = rows;
		}
		
		if (columns > 1)
		{
			newColumns = columns;
		}
		
		this.setLayout(new GridLayout(newRows, newColumns, 0, 0));
		this.setNumberOfColumns(newColumns);
		this.setNumberOfRows(newRows);
		this.initializeConfiguration();
	}
	
	protected int countNeighbors(final Cell[][] grid, final Cell cell, final State state, final int numPossibleNeighbors)
	{
		int count = 0;
		
		for (int i = 1; i <= numPossibleNeighbors; i++)
		{
			Cell neighbor;
			
			try
			{
				int x = cell.getColumn(), y = cell.getRow();
				
				switch (i)
				{
					case 1:
						
						neighbor = grid[x][y+1]; // N
						break;
						
					case 2:
						
						neighbor = grid[x][y-1]; // S
						break;
						
					case 3:
						
						neighbor = grid[x-1][y]; // E
						break;
						
					case 4:
						
						neighbor = grid[x+1][y]; // W
						break;
						
					case 5:
						
						neighbor = grid[x-1][y+1]; // NW
						break;
						
					case 6:
						
						neighbor = grid[x+1][y+1]; // NE
						break;
						
					case 7:
						
						neighbor = grid[x-1][y-1]; // SW
						break;
						
					case 8:
						
						neighbor = grid[x+1][y-1]; // SE
						break;
						
					default:
						
						neighbor = null;
						break;
						
				}
			}
			catch (final Exception e)
			{
				neighbor = null;
			}
			
			if ((neighbor != null) && (neighbor.getContent().getState() == state))
			{
				count++;
			}
		}
		
		return count;
	}
	
	public final Cell[][] getGridOfCells()
	{
		return this.gridOfCells;
	}
	
	public final int getNumberOfColumns()
	{
		return this.numColumns;
	}
	
	public final int getNumberOfRows()
	{
		return this.numRows;
	}
	
	public void infectNeighbors(final Cell[][] grid, final Cell cell, final int numPossibleNeighbors)
	{	
		for (int i = 1; i <= numPossibleNeighbors; i++)
		{
			Cell neighbor;
			
			try
			{
				int x = cell.getColumn(), y = cell.getRow();
				
				switch (i)
				{
					case 1:
						
						neighbor = grid[x][y+1]; // N
						break;
						
					case 2:
						
						neighbor = grid[x][y-1]; // S
						break;
						
					case 3:
						
						neighbor = grid[x-1][y]; // E
						break;
						
					case 4:
						
						neighbor = grid[x+1][y]; // W
						break;
						
					case 5:
						
						neighbor = grid[x-1][y+1]; // NW
						break;
						
					case 6:
						
						neighbor = grid[x+1][y+1]; // NE
						break;
						
					case 7:
						
						neighbor = grid[x-1][y-1]; // SW
						break;
						
					case 8:
						
						neighbor = grid[x+1][y-1]; // SE
						break;
						
					default:
						
						neighbor = null;
						break;
						
				}
			}
			catch (final Exception e)
			{
				neighbor = null;
			}
			
			if ((neighbor != null) && (neighbor.getContent().getState() == State.ALIVE))
			{
				neighbor.getContent().setState(State.INFECTED);
			}
		}
	}
	
	public void initializeConfiguration()
	{
		this.removeAll();
		
		final int		COLS	= this.getNumberOfColumns();
		final int		ROWS	= this.getNumberOfRows();
		final Cell[][]	GRID	= new Cell[COLS][ROWS];
		
		this.setGridOfCells(GRID);
		
		for (int y = 0; y < ROWS; y++)
		{
			for (int x = 0; x < COLS; x++)
			{
				GRID[x][y] = new Cell(x, y);
				this.add(GRID[x][y]);
			}
		}
		
		this.validate();
	}
	
	public void injectInfection()
	{
		final int		COLS	= this.getNumberOfColumns();
		final int		ROWS	= this.getNumberOfRows();
		final int		MAX_Z	= ((Double)Math.pow(Math.max((double)COLS, (double)ROWS), 3.0)).intValue();
		final Cell[][]	GRID	= this.getGridOfCells();
		
		int x = 0, y = 0, z = 0;
		
		do
		{
			x = Games.getRandomInteger(0, COLS, false);
			y = Games.getRandomInteger(0, ROWS, false);
			z++;
		}
		while ((GRID[x][y].getContent().getState() != State.ALIVE) && (z < MAX_Z));
		
		if (z < (MAX_Z - 1))
		{
			GRID[x][y].getContent().setState(State.INFECTED);
		}
	}
	
	protected void iterateCell(final Cell[][] grid, final Cell cell, final int num1, final int num2, final int num3, final boolean diagonalNeighbors)
	{
		int numPossibleNeighbors = 4, numNeighbors = 0;
		
		if (diagonalNeighbors)
		{
			numPossibleNeighbors = 8;
		}
		
		switch (cell.getContent().getState())
		{
			case ALIVE: // TODO: An alive cell with less than num2 or more than num3 alive neighbors becomes empty.
				
				numNeighbors = this.countNeighbors(grid, cell, State.ALIVE, numPossibleNeighbors);
				
				if ((numNeighbors < num2) || (numNeighbors > num3))
				{
					cell.getContent().setState(State.EMPTY);
				}
				break;
				
			case EMPTY: // TODO: An empty cell with num1 or more alive neighbors becomes alive.
				
				numNeighbors = this.countNeighbors(grid, cell, State.ALIVE, numPossibleNeighbors);
				
				if (numNeighbors >= num1)
				{
					cell.getContent().setState(State.ALIVE);
				}
				break;
				
			case INFECTED: // TODO: An infected cell spreads its infection to its alive neighbors and then becomes empty.
				
				this.infectNeighbors(grid, cell, numPossibleNeighbors);
				cell.getContent().setState(State.EMPTY);
				break;
				
			default:
				
				break;
			
		}
	}
	
	public void iterateConfiguration(final String command)
	{
		int num1 = 3, num2 = 2, num3 = 3;
		boolean diagonalNeighbors = false;
		
		switch (command)
		{
			case "Iterate B":
				
				num1 = 2;
				num2 = 2;
				num3 = 2;
				break;
				
			case "Iterate C":
				
				diagonalNeighbors = true;
				break;
				
			default:
				
				break;
		}
		
		final int		COLS	= this.getNumberOfColumns();
		final int		ROWS	= this.getNumberOfRows();
		final Cell[][]	GRID	= this.getGridOfCells();
		
		for (int y = 0; y < ROWS; y++)
		{
			for (int x = 0; x < COLS; x++)
			{
				iterateCell(GRID, GRID[x][y], num1, num2, num3, diagonalNeighbors);
			}
		}
	}
	
	public void randomizeConfiguration()
	{
		final int		COLS	= this.getNumberOfColumns();
		final int		ROWS	= this.getNumberOfRows();
		final Cell[][]	GRID	= this.getGridOfCells();
		
		for (int y = 0; y < ROWS; y++)
		{
			for (int x = 0; x < COLS; x++)
			{
				GRID[x][y].getContent().randomize();
			}
		}
	}
	
	public final void setGridOfCells(final Cell[][] gridOfCells)
	{
		this.gridOfCells = gridOfCells;
	}
	
	public final void setNumberOfColumns(final int numberOfColumns)
	{
		this.numColumns = numberOfColumns;
	}
	
	public final void setNumberOfRows(final int numberOfRows)
	{
		this.numRows = numberOfRows;
	}
}