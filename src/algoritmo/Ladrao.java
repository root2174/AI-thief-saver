package algoritmo;

import java.awt.*;
import java.util.ArrayList;
import java.util.Optional;

public class Ladrao extends ProgramaLadrao {

	static int thiefExp1 = 0;
	static int thiefExp2 = 0;
	static int thiefExp3 = 0;
	static int thiefExp4 = 0;

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
	int thiefIndex = -1;


	public int acao() {
		thiefIndex++;
		if (thiefIndex > 3) {
			thiefIndex = 0;
		}

		position = sensor.getPosicao();

		recordPositionOnMap(position);

		vision = getVision();
		smell = getSmell();

		// ------------------ BOOLEANS -----------------
		Boolean isSaverNearBy = checkSaverNearBy(vision);
		Boolean isSaverNearByVision = checkSaverNearByVision(vision);
		Boolean isSaverNearBySmell = checkSaverBySmell(smell);
		Boolean isThiefNear = checkThiefNear(vision);

//		If this is a new position,
		if (lastPosition != null &&
				position.x == lastPosition.x &&
				position.y == lastPosition.y)
			counter = 5;

		lastPosition = position;

		if (counter > 0)
			counter--;

		if (isSaverNearBy && counter == 0) {
			return steal(vision);
		}

		if (isSaverNearByVision && counter == 0) {
			return chaseSaverByVision(vision);
		}

		if (isSaverNearBySmell && counter == 0) {
			return chaseSaverBySmell(smell);
		}

		if (isThiefNear) {
			return chaseThief(vision);
		}
		return evaluateMove();

	}

	private int chaseThief(int[] vision) {
		ArrayList<Integer> thievesInVision = getAllThievesNear(vision);
		int myExp = getThiefExp(thiefIndex);
		Optional<Integer> maxThiefExp = thievesInVision
				.stream()
				.map(this::getThiefExp)
				.max(Integer::compareTo);

		if (maxThiefExp.isPresent()) {
			if (myExp >= maxThiefExp.get()) {
				return evaluateMove();
			}
		}

		if (isThiefOnDirection(vision, MAP_UP)) {
			if (isObstacleOnDirection(vision, MAP_UP)) {
				evaluateMove();
			} else {
				return MOVE_UP;
			}
		}

		if (isThiefOnDirection(vision, MAP_RIGHT)) {
			if (isObstacleOnDirection(vision, MAP_RIGHT)) {
				evaluateMove();
			} else {
				return MOVE_RIGHT;
			}
		}

		if (isThiefOnDirection(vision, MAP_DOWN)) {
			if (isObstacleOnDirection(vision, MAP_DOWN)) {
				evaluateMove();
			} else {
				return MAP_DOWN;
			}
		}

		if (isThiefOnDirection(vision, MAP_LEFT)) {
			if (isObstacleOnDirection(vision, MAP_LEFT)) {
				evaluateMove();
			} else {
				return MAP_LEFT;
			}
		}

		return evaluateMove();
	}

	private int chaseSaverBySmell(int[] smell) {
		increaseThiefExp(thiefIndex);
		int smallerSmell = Integer.MAX_VALUE;
		int smellPosition = 100;

//		Getting the position with the smallest smell (the saver is nearest)
		for (int i = 0; i < smell.length; i++) {
			if (smell[i] != 0 && smell[i] != -1 && smell[i] < smallerSmell) {
				smallerSmell = smell[i];
				smellPosition = i;
			}
		}

		if (smellPosition == 0 || smellPosition == 1) {
//			Is it an empty cell?
			if (vision[MAP_UP] == 0)
				return MOVE_UP;
			else
				return MOVE_LEFT;
		}

		if (smellPosition == 2 || smellPosition == 4) {
			if (vision[MAP_RIGHT] == 0)
				return MOVE_RIGHT;
			else
				return MOVE_UP;
		}

		if (smellPosition == 6 || smellPosition == 7) {
			if (vision[MAP_DOWN] == 0)
				return MOVE_DOWN;
			else
				return MOVE_RIGHT;
		}

		if (smellPosition == 3 || smellPosition == 5) {
			if (vision[MAP_LEFT] == 0)
				return MOVE_LEFT;
			else
				return MOVE_DOWN;
		}


		return evaluateMove();
	}

	private int chaseSaverByVision(int[] vision) {
		increaseThiefExp(thiefIndex);
		if (isObjectOnDirection(vision, MAP_UP, SAVER)) {
			if (isObstacleOnDirection(vision, MAP_UP)) {
				// Improvement idea: Instead of this **exploratory function**, we
				// could do a search for the shortest path to the thief.
				return evaluateMove();
			} else {
				return MOVE_UP;
			}
		} else if (isObjectOnDirection(vision, MAP_RIGHT, SAVER)) {
			if (isObstacleOnDirection(vision, MAP_RIGHT)) {
				// Improvement idea: Instead of this **exploratory function**, we
				// could do a search for the shortest path to the thief.
				return evaluateMove();
			} else {
				return MOVE_RIGHT;
			}
		} else if (isObjectOnDirection(vision, MAP_DOWN, SAVER)) {
			if (isObstacleOnDirection(vision, MAP_DOWN)) {
				// Improvement idea: Instead of this **exploratory function**, we
				// could do a search for the shortest path to the thief.
				return evaluateMove();
			} else {
				return MOVE_DOWN;
			}
		} else if (isObjectOnDirection(vision, MAP_LEFT, SAVER)) {
			if (isObstacleOnDirection(vision, MAP_LEFT)) {
				// Improvement idea: Instead of this **exploratory function**, we
				// could do a search for the shortest path to the thief.
				return evaluateMove();
			} else {
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
			if (isObstacleOnDirection(vision, MAP_UP)) {
				return checkEmptyCells(vision);
			} else {
				return MOVE_UP;
			}

		}

		if (weightRight != 0 &&
				weightRight < weightUp &&
				weightRight < weightDown &&
				weightRight < weightLeft) {
			if (isObstacleOnDirection(vision, MAP_RIGHT)) {
				return checkEmptyCells(vision);
			} else {
				return MOVE_RIGHT;
			}
		}

		if (weightDown != 0 &&
				weightDown < weightUp &&
				weightDown < weightRight &&
				weightDown < weightLeft) {
			if (isObstacleOnDirection(vision, MAP_DOWN)) {
				return checkEmptyCells(vision);
			} else {
				return MOVE_DOWN;
			}
		}

		if (weightLeft != 0 &&
				weightLeft < weightUp &&
				weightLeft < weightRight &&
				weightLeft < weightDown) {
			if (isObstacleOnDirection(vision, MAP_LEFT)) {
				return checkEmptyCells(vision);
			} else {
				return MOVE_LEFT;
			}
		}


		// Random movement
		return Randomizer.generate(1, 4);
	}

	private boolean isWithinBounds(int x, int y) {
		return x > 0 && x < map.length - 1 && y > 0 && y < map.length - 1;
	}

	private boolean isObjectOnDirection(int[] vision, int direction, int object) {
		if (direction == MAP_UP) {
			return  vision[2] == object ||
					vision[3] == object ||
					vision[4] == object ||
					vision[8] == object ||
					vision[9] == object;
		} else if (direction == MAP_RIGHT) {
			return vision[22] == object ||
					vision[23] == object ||
					vision[18] == object ||
					vision[17] == object ||
					vision[13] == object;
		} else if (direction == MAP_DOWN) {
			return vision[14] == object ||
					vision[15] == object ||
					vision[19] == object ||
					vision[20] == object ||
					vision[21] == object;
		} else if (direction == MAP_LEFT) {
			return  vision[0] == object ||
					vision[1] == object ||
					vision[5] == object ||
					vision[6] == object ||
					vision[10] == object;
		}

		return false;
	}

	private boolean isThiefOnDirection(int[] vision, int direction) {
		if (direction == MAP_UP) {
			return  vision[2] >= THIEF ||
					vision[3] >= THIEF ||
					vision[4] >= THIEF ||
					vision[8] >= THIEF ||
					vision[9] >= THIEF;
		} else if (direction == MAP_RIGHT) {
			return vision[22] >= THIEF ||
					vision[23] >= THIEF ||
					vision[18] >= THIEF ||
					vision[17] >= THIEF ||
					vision[13] >= THIEF;
		} else if (direction == MAP_DOWN) {
			return vision[14] >= THIEF ||
					vision[15] >= THIEF ||
					vision[19] >= THIEF ||
					vision[20] >= THIEF ||
					vision[21] >= THIEF;
		} else if (direction == MAP_LEFT) {
			return  vision[0] >= THIEF ||
					vision[1] >= THIEF ||
					vision[5] >= THIEF ||
					vision[6] >= THIEF ||
					vision[10] >= THIEF;
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
		increaseThiefExp(thiefIndex);

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

	private void increaseThiefExp(int thiefIndex) {
		switch (thiefIndex) {
			case 0:
				thiefExp1++;
				break;
			case 1:
				thiefExp2++;
				break;
			case 2:
				thiefExp3++;
				break;
			case 3:
				thiefExp4++;
				break;
		}
	}

	private int getThiefExp(int thiefIndex) {
		switch (thiefIndex) {
			case 0:
			case 200:
				return thiefExp1;
			case 1:
			case 210:
				return thiefExp2;
			case 2:
			case 220:
				return thiefExp3;
			case 3:
			case 230:
				return thiefExp4;
			default:
				return 0;
		}
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
			if (j >= THIEF)
				return true;
		}

		return false;
	}

	private ArrayList<Integer> getAllThievesNear(int[] vision) {
		ArrayList<Integer> thieves = new ArrayList<>(4);

		for (int j : vision) {
			if (j >= THIEF) {
				thieves.add(j);
			}
		}

		return thieves;
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

	private int checkEmptyCells(int[] vision) {
		boolean visited;
		boolean isUpFree = false;
		boolean isRightFree = false;
		boolean isDownFree = false;
		boolean isLeftFree = false;



		if (vision[MAP_UP] == NO_AGENT)
			isUpFree = true;

		if (vision[MAP_RIGHT] == NO_AGENT)
			isRightFree = true;

		if (vision[MAP_DOWN] == NO_AGENT)
			isDownFree = true;

		if (vision[MAP_LEFT] == NO_AGENT)
			isLeftFree = true;


		if(isUpFree) {
			visited = isCellAlreadyVisited(position, MAP_UP);
			if (visited) {
				if (isRightFree) {
					visited = isCellAlreadyVisited(position, MAP_RIGHT);
					if (visited) {
						if (isDownFree) {
							visited = isCellAlreadyVisited(position, MAP_DOWN);
							if (visited) {
								if (isLeftFree) {
									return MOVE_LEFT;
								} else {
									return Randomizer.generate(1, 4);
								}
							} else {
								return MOVE_DOWN;
							}
						}
					} else {
						return MOVE_RIGHT;
					}
				}
			}
			return MOVE_UP;
		}

		if(isRightFree) {
			visited = isCellAlreadyVisited(position, MAP_RIGHT);
			if (visited) {
				if (isDownFree) {
					visited = isCellAlreadyVisited(position, MAP_DOWN);
					if (visited) {
						if (isLeftFree) {
							return MOVE_LEFT;
						} else {
							return Randomizer.generate(1, 3);
						}
					} else {
						return MOVE_DOWN;
					}
				}
			}
			return MOVE_RIGHT;
		}

		if(isDownFree) {
			visited = isCellAlreadyVisited(position, MAP_DOWN);
			if (visited) {
				return MOVE_LEFT;
			}
			return MOVE_DOWN;
		}

		if (isLeftFree) {
			return MOVE_LEFT;
		}

		return Randomizer.generate(1, 4);
	}

	private boolean isCellAlreadyVisited(Point position, int direction) {
		if (direction == MAP_UP) {
			if (map[position.y -1][position.x] >= 1) {
				return true;
			}
		}

		if (direction == MAP_RIGHT) {
			if (map[position.y][position.x + 1] >= 1) {
				return true;
			}
		}

		if (direction == MAP_DOWN) {
			if (map[position.y + 1][position.x] >= 1) {
				return true;
			}
		}

		if (direction == MAP_LEFT) {
			return map[position.y][position.x - 1] >= 1;
		}



		return false;
	}

	// -------------------------------------------------------------


	private int[] getVision() {
		vision = sensor.getVisaoIdentificacao();
		for (int i = 0; i < vision.length; i++) {
			if (vision[i] >= SAVER && vision[i] <= THIEF) {
				vision[i] = SAVER;
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