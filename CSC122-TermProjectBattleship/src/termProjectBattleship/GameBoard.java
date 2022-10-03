package termProjectBattleship;

/**
 * Battleship Term Project: Game Board Class
 * @author taterosen
 * November 2020
 */

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import termProjectBattleship.Boat.Type;

import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import javafx.scene.media.*;

public class GameBoard extends Parent {
	private VBox rows = new VBox();
	private boolean opponent = false;
	public int boats = 5;

	/**
	 * Creates a basic GameBoard object.
	 * @param enemy
	 * @param handler
	 */
	public GameBoard(boolean enemy, EventHandler<? super MouseEvent> handler) {
		this.opponent = enemy;
		for (int y = 0; y < 10; y++) {
			HBox row = new HBox();
			for (int x = 0; x < 10; x++) {
				Space c = new Space(x, y, this);
				c.setOnMouseClicked(handler);
				row.getChildren().add(c);
			}
			rows.getChildren().add(row);
		}
		getChildren().add(rows);
	}

	/**
	 * Place a certain boat on the indicated space(s).
	 * @param boat
	 * @param x
	 * @param y
	 * @return true if boat was placed; false otherwise
	 */
	public boolean placeBoat(Boat boat, int x, int y) {
		if (canPlaceShip(boat, x, y)) {
			if (boat.vertical) {
				placeVertical(boat, x, y);
			}
			else {
				placeHorizontal(boat, x, y);
			}
			return true;
		}
		return false;
	}

	/**
	 * Called if a boat needs to be placed vertically.
	 * @param boat
	 * @param x
	 * @param y
	 */
	public void placeVertical(Boat boat, int x, int y) {
		int length = boat.length;
		for (int i = y; i < y + length; i++) {
			Space cell = getSpace(x, i);
			cell.current = boat;
			if (!opponent) {
				cell.setFill(Color.WHITE);
				cell.setStroke(Color.GREEN);

			}
		}
	}

	/**
	 * Called if a boat needs to be placed horizontally.
	 * @param boat
	 * @param x
	 * @param y
	 */
	public void placeHorizontal(Boat boat, int x, int y) {
		int length = boat.length;
		for (int i = x; i < x + length; i++) {
			Space cell = getSpace(i, y);
			cell.current = boat;
			if (!opponent) {
				cell.setFill(Color.WHITE);
				cell.setStroke(Color.GREEN);
			}
		}
	}

	/**
	 * Place a bomb on the indicated space.
	 * @param bomb
	 * @param x
	 * @param y
	 * @return true if the bomb was placed; false otherwise
	 */
	public boolean placeBomb(Boat bomb, int x, int y)
	{
		if(canPlaceShip(bomb, x, y))
		{
			Space cell = getSpace(x, y);
			if(opponent) {
				cell.current = bomb;
			}
			return true;
		}
		return false;
	}

	/**
	 * Place a powerUp on the indicated space.
	 * @param powerUp
	 * @param x
	 * @param y
	 * @return true if the powerUp was placed; false otherwise
	 */
	public boolean placePowerUp(Boat powerUp, int x, int y)
	{
		if(canPlaceShip(powerUp, x, y))
		{
			Space cell = getSpace(x, y);
			cell.current = powerUp;
			if(!opponent) {
				cell.setFill(Color.WHEAT);
				cell.setStroke(Color.FIREBRICK);
			}
			return true;
		}
		return false;
	}

	/**
	 * Checks whether a boat is able to be placed on the 
	 * indicated space(s).
	 * @param boat
	 * @param x
	 * @param y
	 * @return true if the boat can be placed; false otherwise
	 */
	public boolean canPlaceShip(Boat boat, int x, int y) {
		boolean canPlace = false;
		if (boat.vertical) {
			canPlace = canPlaceVertical(boat, x, y);
		}
		else {
			canPlace = canPlaceHorizontal(boat, x, y);
		}
		return canPlace;
	}

	/**
	 * Called to check if a boat can be placed vertically.
	 * @param boat
	 * @param x
	 * @param y
	 * @return true if the boat can be placed vertically; false otherwise
	 */
	private boolean canPlaceVertical(Boat boat, int x, int y)
	{
		int length = boat.length;
		for (int i = y; i < y + length; i++) {
			if (!isValidPoint(x, i))
				return false;

			Space cell = getSpace(x, i);
			if (cell.current.type != Type.OPEN)
				return false;

			for (Space neighbor : getNeighbors(x, i)) {
				if (!isValidPoint(x, i))
					return false;
				if (neighbor.current.type != Type.OPEN)
					return false;
			}
		}
		return true;
	}

	/**
	 * Called to check if a boat can be placed horizontally.
	 * @param boat
	 * @param x
	 * @param y
	 * @return true if the boat can be placed horizontally; false otherwise
	 */
	private boolean canPlaceHorizontal(Boat boat, int x, int y)
	{
		int length = boat.length;
		for (int i = x; i < x + length; i++) {
			if (!isValidPoint(i, y))
				return false;

			Space cell = getSpace(i, y);
			if (cell.current.type != Type.OPEN)
				return false;

			for (Space neighbor : getNeighbors(i, y)) {
				if (!isValidPoint(i, y))
					return false;
				if (neighbor.current.type != Type.OPEN)
					return false;
			}
		}
		return true;
	}

	/**
	 * 
	 * @param x
	 * @param y
	 * @return the Space indicated by the input integers
	 */
	public Space getSpace(int x, int y) {
		return (Space)((HBox)rows.getChildren().get(y)).getChildren().get(x);
	}

	/**
	 * 
	 * @param x
	 * @param y
	 * @return the neighbors in each direction to the Space indicated by the input integers
	 */
	private Space[] getNeighbors(int x, int y) {
		Point2D[] points = new Point2D[] {
				new Point2D(x - 1, y),
				new Point2D(x + 1, y),
				new Point2D(x, y - 1),
				new Point2D(x, y + 1)
		};
		List<Space> neighbors = new ArrayList<Space>();

		for (Point2D p : points) {
			if (isValidPoint(p)) {
				neighbors.add(getSpace((int)p.getX(), (int)p.getY()));
			}
		}
		return neighbors.toArray(new Space[0]);
	}

	/**
	 * Checks if a point is on the grid.
	 * @param point
	 * @return true if the point is valid; false otherwise.
	 */
	private boolean isValidPoint(Point2D point) {
		return isValidPoint(point.getX(), point.getY());
	}

	/**
	 * Checks if a point is on the grid.
	 * @param x
	 * @param y
	 * @return true if the point is valid; false otherwise.
	 */
	private boolean isValidPoint(double x, double y) {
		return x >= 0 && x < 10 && y >= 0 && y < 10;
	}   

	/**
	 * Inner class for the spaces on a boards.
	 * @author taterosen & claytoncoulter
	 *
	 */
	public class Space extends Rectangle {
		public int x;
		public int y;
		public Boat current = new Boat(1, true, Type.OPEN);
		public boolean wasShot = false;
		private GameBoard board;

		// Four audio files that play depending on what space is clicked:
		private static final String BOOM = "Explosion1.mp3";
		Media boom = new Media(new File(BOOM).toURI().toString());
		MediaPlayer boomPlayer = new MediaPlayer(boom);

		private static final String MISS = "WaterDrop.mp3";
		Media miss = new Media(new File(MISS).toURI().toString());
		MediaPlayer missPlayer = new MediaPlayer(miss);

		private static final String POWER_UP = "powerUp.mp3";
		Media powerUp = new Media(new File(POWER_UP).toURI().toString());
		MediaPlayer powerUpPlayer = new MediaPlayer(powerUp);

		private static final String BOMB = "fail.wav";
		Media bomb = new Media(new File(BOMB).toURI().toString());
		MediaPlayer bombPlayer = new MediaPlayer(bomb);


		/**
		 * Create a basic Space.
		 * @param x
		 * @param y
		 * @param board
		 */
		public Space(int x, int y, GameBoard board) {
			super(33, 33);
			this.x = x;
			this.y = y;
			this.board = board;
			setFill(Color.CORNFLOWERBLUE);
			setStroke(Color.BLACK);
		}

		/**
		 * Shoot the clicked Space: fill it with the corresponding 
		 * color and play the correct sound.
		 * @return true if the Space was a powerUp; false otherwise
		 */
		public boolean shoot() {
			wasShot = true;
			if(current.type == Type.OPEN) {
				setFill(Color.BLACK);
				missPlayer.seek(Duration.ZERO);
				missPlayer.play();
			}
			else if(current.type == Type.BOMB){
				if(opponent)
					setFill(Color.DARKGREEN);
				bombPlayer.seek(Duration.ZERO);
				bombPlayer.play();

			}
			else if(current.type == Type.POWERUP) {
				setFill(Color.GOLD);
				powerUpPlayer.seek(Duration.ZERO);
				powerUpPlayer.play();
				return true;
			}
			else {
				current.isHit();
				setFill(Color.RED);
				boomPlayer.seek(Duration.ZERO);
				boomPlayer.play();

				if (!current.isAlive()) {
					board.boats--;
				}
			}
			return false;
		}
	}
}