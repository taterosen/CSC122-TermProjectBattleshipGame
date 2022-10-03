package termProjectBattleship;

/**
 * Battleship Term Project: Boat Class
 * @author taterosen
 * November 2020
 */

import javafx.scene.Parent;

public class Boat extends Parent {
	public int length;
	public boolean vertical = true;
	public Type type;

	private int health;

	public enum Type { BOAT, BOMB, POWERUP, OPEN };

	public Boat(int length, boolean vertical, Type type) {
		this.length = length;
		this.vertical = vertical;
		this.type = type;
		health = length;
	}

	public void isHit() {
		health--;
	}

	public boolean isAlive() {
		return health > 0;
	}
}
