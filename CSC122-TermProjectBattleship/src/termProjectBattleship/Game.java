package termProjectBattleship;

/**
 * Battleship Term Project: Main Game Class
 * @author taterosen
 * November 2020
 */

import java.io.File;
import java.util.Random;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import termProjectBattleship.Boat.Type;

import termProjectBattleship.GameBoard.Space;

public class Game extends Application {

	private static boolean inGame = false;
	private static boolean gameComplete = false;
	private static boolean opponentWin;
	private static GameBoard opponentBoard;
	private static GameBoard playerBoard;

	private static int boatsToPlace = 5;

	private static boolean opponentTurn = false;

	private static Random random = new Random();

	private static VBox vbox;

	/**
	 * Create a game, set up GameBoards, and place
	 * in a BorderPane.
	 * @return the game BorderPane
	 */
	private Parent newGame() {
		BorderPane game = new BorderPane();
		game.setPrefSize(600, 800);
		game.setRight(Instructions());
		createOpponent(game);
		createPlayer();

		vbox = new VBox(50, opponentBoard, playerBoard);
		vbox.setAlignment(Pos.TOP_LEFT);
		game.setCenter(vbox);
		//update(game);

		game.setCenter(vbox);
		game.setBottom(ScoreBoard());

		return game;
	}

	/**
	 * Create the opponent's board and update after each turn.
	 * @param game
	 */
	private static void createOpponent(BorderPane game) {
		opponentBoard = new GameBoard(true, event -> {
			if (!inGame)
				return;
			Space currentSpace = (Space) event.getSource();
			if (currentSpace.wasShot)
				return;
			opponentTurn = !currentSpace.shoot();
			waitOneSecond(game, currentSpace);
		});
	}

	private static void waitOneSecond(BorderPane game, Space currentSpace) {
		Task<Void> sleeper = new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
				return null;
			}
		};
		sleeper.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				if (opponentBoard.boats == 0) {
					inGame = false;
					opponentWin = false;
					gameComplete = true;
				}
				if(gameComplete == true) displayWinner(game);
				else if (opponentTurn)
				{
					opponentMove();
					if (currentSpace.current.type != null && currentSpace.current.type == Type.BOMB) {
						opponentTurn = true;
						opponentMove();
					}
					if(gameComplete == true) displayWinner(game);
				}

				game.setBottom(ScoreBoard());
			}
		});
		new Thread(sleeper).start();
	}

	/**
	 * Create the player's board.
	 */
	private static void createPlayer() {
		playerBoard = new GameBoard(false, event -> {
			if (inGame)
				return;
			Space currentSpace = (Space) event.getSource();
			if (playerBoard.placeBoat(new Boat(boatsToPlace, event.getButton() == 
					MouseButton.PRIMARY, Type.BOAT), currentSpace.x, currentSpace.y)) {
				if (--boatsToPlace == 0) {
					startGame();
				}
			}
		});
	}

	/**
	 * Randomize and shoot a space on the player's board
	 * as the opponent's move.
	 */
	private static void opponentMove() {
		while (opponentTurn) {
			int x = random.nextInt(10);
			int y = random.nextInt(10);

			Space currentSpace = playerBoard.getSpace(x, y);
			if (currentSpace.wasShot)
				continue;

			opponentTurn = currentSpace.shoot();

			if (playerBoard.boats == 0) {
				inGame = false;
				opponentWin = true;
				gameComplete = true;
			}
		}
	}

	/**
	 * Display text and play sound based on the game's outcome.
	 * @param game
	 */
	private static void displayWinner(BorderPane game) {
		Media outcome = new Media(new File("youwin.wav").toURI().toString());
		MediaPlayer outcomePlayer = new MediaPlayer(outcome);
		Text text;
		if(!opponentWin)  {
			outcomePlayer.seek(Duration.ZERO);
			outcomePlayer.play();
			text = new Text("You win!");
			text.setFont(Font.font("Times New Roman", FontWeight.BOLD, 30));
			game.setRight(text);
		} else if(opponentWin) {
			outcome = new Media(new File("youlose.wav").toURI().toString());
			outcomePlayer = new MediaPlayer(outcome);
			outcomePlayer.seek(Duration.ZERO);
			outcomePlayer.play();
			text = new Text("You lose!");
			text.setFont(Font.font("Times New Roman", FontWeight.BOLD, 30));
			game.setRight(text);
		}
	}

	/**
	 * Randomly place the enemy boats, powerUps, and bombs.
	 * @param numItems
	 * @param boat
	 * @param board
	 */
	private static void opponentPlacement(int numItems, boolean boat, GameBoard board) {
		int num = numItems;
		int boatLength = 5;
		int bombLength = 1;
		while(num > 0) { 
			int x = random.nextInt(10);
			int y = random.nextInt(10);
			if(boat == true) {
				if (board.placeBoat(new Boat(boatLength, Math.random() < 0.5, Type.BOAT), x, y)) {
					num--;
					boatLength--;
				}
			} else {
				int x1 = random.nextInt(10);
				int y1 = random.nextInt(10);
				if (board.canPlaceShip(new Boat(bombLength, true, Type.BOMB), x, y)
						&& board.canPlaceShip(new Boat(bombLength, true, Type.POWERUP), x1, y1)) {
					board.placeBomb(new Boat(bombLength, true, Type.BOMB), x, y);
					board.placePowerUp(new Boat(bombLength, true, Type.POWERUP), x1, y1);
					num--;
				}
			}
		}
	}

	/**
	 * After player boats are placed, call method to 
	 * place randomized objects and initialize game boolean.
	 */
	private static void startGame() {
		int boats = 5;
		int other = 3;

		opponentPlacement(boats, true, opponentBoard);
		opponentPlacement(other, false, opponentBoard);

		opponentPlacement(other, false, playerBoard);

		inGame = true;
	}

	/**
	 * Create the instructions for the game.
	 * @return the pane with the instruction text
	 */
	private Parent Instructions() {
		Text instructions = new Text(25, 25, "1. Place 5 ships on the bottom board "
				+ "\nvertically(click an open space) \nor "
				+ "horizontally(right-click on an open space). \n2. "
				+ "Click a blue space on the opponent's board to fire. "
				+ "\n3. Hitting a power-up (yellow) gives an extra turn,"
				+ " \nand hitting a bomb (green) loses a turn. \n 4. Keep firing"
				+ " at the opponent's board until one side\n has sunk all "
				+ "the opposing ships!\n\n***Please wait until the opponent has "
				+ "moved before \nclicking another space!***\n\n\n\nNote: There are "
				+ "three power ups on each board, but \nthree bombs on only the "
				+ "opponent's board! This is \nto make the game more even "
				+ "for the computer. \nBoth are placed randomly.");
		instructions.setFont(Font.font("Times New Roman", FontWeight.BOLD, 15));
		Pane pane = new Pane();
		pane.getChildren().addAll(instructions);
		return pane;
	}

	/**
	 * Create the scoreboard for the game.
	 * @return the pane with the scoreboard text
	 */
	private static Parent ScoreBoard() {
		Text score = new Text("\nOpponent Boats Remaining: " + opponentBoard.boats 
				+ "\nPlayer Boats Remaining: " + playerBoard.boats);
		Pane pane = new Pane();
		pane.getChildren().addAll(score);
		return pane;
	}

	/**
	 * Initialize the stage and set the scene for the game.
	 */
	@Override
	public void start(Stage primaryStage) throws Exception {
		Scene scene = new Scene(newGame());
		primaryStage.setTitle("Battleship");
		primaryStage.setScene(scene);
		primaryStage.setResizable(false);
		primaryStage.show();

	}

	/**
	 * Main method to launch game.
	 * @param args
	 */
	public static void main(String[] args) {
		launch(args);
	}
}
