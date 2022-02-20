package algoritmo;

import com.sun.org.apache.xpath.internal.operations.Bool;

import java.awt.*;

public class Ladrao extends ProgramaLadrao {

	// Map to be used in the future to store information about the game.
	private final int[][] map = new int[30][30];
	// Thief vision
	int[] vision = new int[24];

    // Thief current position
	Point position;

	// ------------ VISION CONSTANTS --------------
	private final int NO_VISION = -2;
	private final int OUTSIDE_WORLD = -1;
	private final int NO_AGENT = 0;
	private final int WALL = 1;
	private final int BANK = 3;
	private final int COIN = 4;
	private final int POWER_TABLETS = 5;
	private final int SAVER = 100;
	private final int THIEF = 200;
	//	---------------------------------------------

	// ------------ MOVEMENT CONSTANTS --------------
	private final int MOVE_UP = 1;
	private final int MOVE_DOWN = 2;
	private final int MOVE_RIGHT = 3;
	private final int MOVE_LEFT = 4;
	//	---------------------------------------------

	// ------------ MAP CONSTANTS --------------
	private final int MAP_UP = 7;
	private final int MAP_LEFT = 11;
	private final int MAP_RIGHT = 12;
	private final int MAP_DOWN = 16;


	public int acao() {

		position = sensor.getPosicao();
		recordPositionOnMap(position);

		// ------------------ BOOLEANS -----------------
		Boolean isSaverNearBy = checkSaverNearBy(vision);

		if (isSaverNearBy) {
			return steal(vision);
		}

		return (int) (Math.random() * 5);

	}


	/**
	 * This function will increase by 1 the current position of the thief
	 * on the map
	 * @param position The current position to be recorded
	 */
	private void recordPositionOnMap(Point position) {
		map[position.y][position.x] += 1;
	}

	/**
	 * Will check for a saver nearby and if there is one, will move towards it to
	 * try to steal from.
	 * @param vision current vision of the map
	 * @return the thief's next movement
	 */
	private int steal(int[] vision) {
		if (vision[MAP_UP] == SAVER) {
			return MOVE_UP;
		}

		if (vision[MAP_LEFT] == SAVER) {
			return MOVE_LEFT;
		}

		if (vision[MAP_RIGHT] == SAVER) {
			return MOVE_RIGHT;
		}

		if (vision[MAP_DOWN] == SAVER) {
			return MOVE_DOWN;
		}

		// TODO: Check vision to create a strategy for the next move based on previous movement.
		return (int) (Math.random() * 5);
	}

	private Boolean checkSaverNearBy(int[] vision) {
		return vision[MAP_UP] == SAVER ||
				vision[MAP_LEFT] == SAVER ||
				vision[MAP_RIGHT] == SAVER ||
				vision[MAP_DOWN] == SAVER;
	}
}