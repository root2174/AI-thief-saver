package algoritmo;

import java.awt.*;

public class Ladrao extends ProgramaLadrao {

	// Map to be used in the future to store information about the game.
	private final int[][] map = new int[30][30];
	// Thief vision
    // Thief current position
	Point position;
	Point lastPosition;

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

	int[] vision = new int[24];
	int[] smell = new int[8];

	int counter = 0;


	public int acao() {

		position = sensor.getPosicao();

		recordPositionOnMap(position);

		vision = getVision();
		smell = getSmell();

		// ------------------ BOOLEANS -----------------
		Boolean isSaverNearBy = checkSaverNearBy(vision);
		Boolean isSaverNearByVision = checkSaverNearByVision(vision);
		Boolean isSaverNearBySmell = checkSaverBySmell(smell);
		Boolean isObstacleNear = checkObstaclesNear(vision);
		Boolean isThiefNear = checkThiefNear(vision);

		if (lastPosition != null &&
				position.x == lastPosition.x &&
				position.y == lastPosition.y)
			counter = 7;

		lastPosition = position;

		if (counter > 0)
			counter--;

		if (isSaverNearBy && counter == 0) {
			System.out.println("SAVER IS NEAR BY");
			return steal(vision);
		}

		if (isSaverNearByVision && counter == 0) {
			System.out.println("SAVER NEAR BY VISION");
			return chaseSaverByVision(vision);
		}

//		System.out.println("NO SAVER DETECTED, EVALUATING MOVE");

		return evaluateMove();

	}

	private int chaseSaverByVision(int[] vision) {
		if (canISeeASaverOnDirection(vision, MAP_UP)) {
			if (isObstacleOnDirection(vision, MAP_UP)) {
				// Improvement idea: Instead of this **exploratory function**, we
				// could do a search for the shortest path to the thief.
				return evaluateMove();
			} else {
				System.out.println("CHASING SAVER UP");
				return MOVE_UP;
			}
		} else if (canISeeASaverOnDirection(vision, MAP_RIGHT)) {
			if (isObstacleOnDirection(vision, MAP_RIGHT)) {
				// Improvement idea: Instead of this **exploratory function**, we
				// could do a search for the shortest path to the thief.
				return evaluateMove();
			} else {
				System.out.println("CHASING SAVER RIGHT");
				return MOVE_RIGHT;
			}
		} else if (canISeeASaverOnDirection(vision, MAP_DOWN)) {
			if (isObstacleOnDirection(vision, MAP_DOWN)) {
				// Improvement idea: Instead of this **exploratory function**, we
				// could do a search for the shortest path to the thief.
				return evaluateMove();
			} else {
				System.out.println("CHASING SAVER DOWN");
				return MOVE_DOWN;
			}
		} else if (canISeeASaverOnDirection(vision, MAP_LEFT)) {
			if (isObstacleOnDirection(vision, MAP_LEFT)) {
				// Improvement idea: Instead of this **exploratory function**, we
				// could do a search for the shortest path to the thief.
				return evaluateMove();
			} else {
				System.out.println("CHASING SAVER LEFT");
				return MOVE_LEFT;
			}
		} else {
			return evaluateMove();
		}
	}

	private int evaluateMove() {
		int x = position.x;
		int y = position.y;
		int weightUp = 0;
		int weightRight = 0;
		int weightDown = 0;
		int weightLeft = 0;

		if (isWithinBounds(x, y)) {
			weightUp = map[x - 1][y];
			weightRight = map[x][y + 1];
			weightDown = map[x + 1][y];
			weightLeft = map[x][y - 1];
		}

		if (weightUp != 0 &&
				weightUp < weightRight &&
				weightUp < weightDown &&
				weightUp < weightLeft) {
			System.out.println("EVALUATED UP");
			return MOVE_UP;
		}

		if (weightRight != 0 &&
				weightRight < weightUp &&
				weightRight < weightDown &&
				weightRight < weightLeft) {
			System.out.println("EVALUATED RIGHT");
			return MOVE_RIGHT;
		}

		if (weightDown != 0 &&
				weightDown < weightUp &&
				weightDown < weightRight &&
				weightDown < weightLeft) {
			System.out.println("EVALUATED DOWN");
			return MOVE_DOWN;
		}

		if (weightLeft != 0 &&
				weightLeft < weightUp &&
				weightLeft < weightRight &&
				weightLeft < weightDown) {
			System.out.println("EVALUATED LEFT");
			return MOVE_LEFT;
		}


		// Random movement
		return Randomizer.generate(1, 4);
	}

	private boolean isWithinBounds(int x, int y) {
		return x > 0 && x < map.length - 1 && y > 0 && y < map.length - 1;
	}

	private boolean canISeeASaverOnDirection(int[] vision, int direction) {
		System.out.println("I SEE A SAVER");
		if (direction == MAP_UP) {
			return (vision[2] >= SAVER && vision[2] <= THIEF) ||
					vision[3] == SAVER ||
					vision[4] == SAVER ||
					vision[8] == SAVER ||
					vision[9] == SAVER;
		} else if (direction == MAP_RIGHT) {
			return vision[22] == SAVER ||
					vision[23] == SAVER ||
					vision[18] == SAVER ||
					vision[17] == SAVER ||
					vision[13] == SAVER;
		} else if (direction == MAP_DOWN) {
			return vision[14] == SAVER ||
					vision[15] == SAVER ||
					vision[19] == SAVER ||
					vision[20] == SAVER ||
					vision[21] == SAVER;
		} else if (direction == MAP_LEFT) {
			return  vision[0] == SAVER ||
					vision[1] == SAVER ||
					vision[5] == SAVER ||
					vision[6] == SAVER ||
					vision[10] == SAVER;
		}

		return false;
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
		System.out.println("STEALING FROM SAVER");
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

		return evaluateMove();
	}

	private Boolean checkSaverNearBy(int[] vision) {
		return vision[MAP_UP] == SAVER ||
				vision[MAP_LEFT] == SAVER ||
				vision[MAP_RIGHT] == SAVER ||
				vision[MAP_DOWN] == SAVER;
	}


   // ------------------- AWARENESS FUNCTION -----------------------

	private Boolean checkSaverBySmell(int[] smell) {

		for (int j : smell) {
			if (j > 0) {
				return true;
			}
		}

		return false;
	}

	private Boolean checkSaverNearByVision(int[] vision) {
		for (int j : vision) {
			if (j == SAVER) {
				return true;
			}
		}

		return false;
	}

	private Boolean checkThiefNear(int[] vision) {
		for (int j : vision) {
			if (j == THIEF)
				return true;
		}

		return false;
	}

	private Boolean checkObstaclesNear(int[] vision) {
		return isObstacleOnDirection(vision, MAP_UP) ||
				isObstacleOnDirection(vision, MAP_RIGHT) ||
				isObstacleOnDirection(vision, MAP_DOWN) ||
				isObstacleOnDirection(vision, MAP_LEFT);
	}

	private Boolean isObstacleOnDirection(int[] vision, int direction) {
		return vision[direction] == WALL || vision[direction] == COIN || vision[direction] == POWER_TABLETS
				|| vision[direction] == BANK || vision[direction] == THIEF
				|| vision[direction] == NO_VISION;
	}

	// -------------------------------------------------------------


	private int[] getVision() {
		vision = sensor.getVisaoIdentificacao();
		for (int i = 0; i < vision.length; i++) {
			if (vision[i] >= SAVER && vision[i] <= THIEF) {
				vision[i] = SAVER;
			}

			if (vision[i] >= THIEF) {
				vision[i] = THIEF;
			}
		}
		return vision;
	}

	private int[] getSmell() {
		smell = sensor.getAmbienteOlfatoPoupador();
		return smell;
	}

	public static class Randomizer {
		public static int generate(int min, int max) {
			return min + (int) (Math.random() * ((max - min) + 1));
		}
	}
}