package rts;

import rts.*;
import rts.units.Unit;
import util.Pair;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Enumerates the PlayerActions for a given game state.
 * Modified to enumerate PlayerActions in a specific order.
 *
 * @author santi
 */
public class CustomPlayerActionGenerator {
    static Random random = new Random();
    
    GameState gameState;
    PhysicalGameState physicalGameState;
    ResourceUsage baseResourceUsage;
    List<Pair<Unit,List<UnitAction>>> choices;
    PlayerAction lastAction = null;
    long size = 1;  // this will be capped at Long.MAX_VALUE;
    long generated = 0;
    int[] choiceSizes = null;
    int[] currentChoice = null;
    boolean moreActions = true;

	long inactionCombinationsCount = 0;
	long allowedActionsCount = 0;

	public static long IPAsRemoved = 0;

	/**
	 * Generating all possible actions for a player in a given state
	 * @param gameState
	 * @param player
	 * @param waitProbability if equals 1.0f or more it will generate actions normally. if less, it generates wait
	 *                        actions according to the probability expressed by the given value.
	 * @throws Exception
	 */
	public CustomPlayerActionGenerator(GameState gameState, int player, int noneDuration) throws Exception {

		// Generate the reserved resources into baseResourceUsage (for all players)
		this.gameState = gameState;
		physicalGameState = this.gameState.getPhysicalGameState();
		baseResourceUsage = new ResourceUsage();

		for (Unit unit : physicalGameState.getUnits()) {
			UnitActionAssignment assignment = this.gameState.unitActions.get(unit);
			if (assignment != null) { // Unit assigned an action. Not available.
				ResourceUsage resourceUsage = assignment.action.resourceUsage(unit, physicalGameState);
				baseResourceUsage.merge(resourceUsage);
			}
		}

		// Generate choices list. The list of unit actions available for each unit.
		choices = new ArrayList<>();
		for (Unit unit : physicalGameState.getUnits()) {
			if (unit.getPlayer() == player) { // Iterate over all this player units.
				if (this.gameState.unitActions.get(unit) == null) { // Unit not assigned an action, therefore available.
					List<UnitAction> actions = unit.getUnitActions(gameState, noneDuration);

					/*if (waitProbability < 1f) {
						// Return Unit Actions without an idle action. For Inverse Filtering.
						actions = unit.getUnitActionsWithoutWait(this.gameState);
						// Add wait action according to the given wait probability.
						float chance = random.nextFloat();
						if (chance < waitProbability || actions.size() == 0)
							actions.add(new UnitAction(UnitAction.TYPE_NONE, 10));
					} // Remove HERE.
					else
						actions = unit.getUnitActions(this.gameState); // Return Unit Actions with an idle action.*/

					choices.add(new Pair<>(unit, actions));

					// make sure we don't overflow:
					long unitActionsCount = actions.size();
					if (Long.MAX_VALUE / size <= unitActionsCount) { // To avoid overflow after any possible multiplication.
						size = Long.MAX_VALUE;
					} else {
						size *= (long) actions.size(); // The number of possible unit actions combinations.
					}
					// System.out.println("size = " + size);
				}
			}
		}
		// System.out.println("---");

		// Throw Exception if the generated choices list is empty.
		if (choices.size() == 0) {
			System.err.println("Problematic game state:");
			System.err.println(gameState);
			throw new Exception(
					"Move generator for player " + player + " created with no units that can execute actions! (status: "
							+ gameState.canExecuteAnyAction(0) + ", " + gameState.canExecuteAnyAction(1) + ")"
			);
		}

		// Initialize choiceSizes and currentChoice array.
		choiceSizes = new int[choices.size()];
		currentChoice = new int[choices.size()];
		int i = 0;
		for (Pair<Unit,List<UnitAction>> choice : choices) {
			choiceSizes[i] = choice.m_b.size();
			currentChoice[i] = 0;
			i++;
		}

		// Distribute idle actions randomly according to waitProbability. For Inverse Filtering.
//		if (waitProbability < 1f)
//			distributeWaitActions(waitProbability);
	}

	public CustomPlayerActionGenerator(GameState gameState, int player, float waitProbability) throws Exception {

		// Generate the reserved resources into baseResourceUsage (for all players)
		this.gameState = gameState;
		physicalGameState = this.gameState.getPhysicalGameState();
		baseResourceUsage = new ResourceUsage();

		for (Unit unit : physicalGameState.getUnits()) {
			UnitActionAssignment assignment = this.gameState.unitActions.get(unit);
			if (assignment != null) { // Unit assigned an action. Not available.
				ResourceUsage resourceUsage = assignment.action.resourceUsage(unit, physicalGameState);
				baseResourceUsage.merge(resourceUsage);
			}
		}

		// Generate choices list. The list of unit actions available for each unit.
		choices = new ArrayList<>();
		for (Unit unit : physicalGameState.getUnits()) {
			if (unit.getPlayer() == player) { // Iterate over all this player units.
				if (this.gameState.unitActions.get(unit) == null) { // Unit not assigned an action, therefore available.
					List<UnitAction> actions;

					if (waitProbability < 1f) {
						// Return Unit Actions without an idle action. For Inverse Filtering.
						actions = unit.getUnitActionsWithoutWait(this.gameState);
						// Add wait action according to the given wait probability.
						float chance = random.nextFloat();
						if (chance < waitProbability || actions.size() == 0)
							actions.add(new UnitAction(UnitAction.TYPE_NONE, 10));
					} // Remove HERE.
					else
						actions = unit.getUnitActions(this.gameState); // Return Unit Actions with an idle action.

					choices.add(new Pair<>(unit, actions));

					// make sure we don't overflow:
					long unitActionsCount = actions.size();
					if (Long.MAX_VALUE / size <= unitActionsCount) { // To avoid overflow after any possible multiplication.
						size = Long.MAX_VALUE;
					} else {
						size *= (long) actions.size(); // The number of possible unit actions combinations.
					}
					// System.out.println("size = " + size);
				}
			}
		}
		// System.out.println("---");

		// Throw Exception if the generated choices list is empty.
		if (choices.size() == 0) {
			System.err.println("Problematic game state:");
			System.err.println(gameState);
			throw new Exception(
					"Move generator for player " + player + " created with no units that can execute actions! (status: "
							+ gameState.canExecuteAnyAction(0) + ", " + gameState.canExecuteAnyAction(1) + ")"
			);
		}

		// Initialize choiceSizes and currentChoice array.
		choiceSizes = new int[choices.size()];
		currentChoice = new int[choices.size()];
		int i = 0;
		for (Pair<Unit,List<UnitAction>> choice : choices) {
			choiceSizes[i] = choice.m_b.size();
			currentChoice[i] = 0;
			i++;
		}

		// Distribute idle actions randomly according to waitProbability. For Inverse Filtering.
//		if (waitProbability < 1f)
//			distributeWaitActions(waitProbability);
	}

	/**
	 * Returns the next PlayerAction for the state stored in this object
	 * @param cutOffTime time to stop generating the action
	 * @return
	 * @throws Exception
	 */
	public PlayerAction getNextAction(long cutOffTime) throws Exception {

		int count = 0;

		while (moreActions) {

			boolean consistent = true;
			PlayerAction playerAction = new PlayerAction();
			playerAction.setResourceUsage(baseResourceUsage.clone());

			int unitIndex = choices.size();
			if (unitIndex == 0)
				throw new Exception("Move generator created with no units that can execute actions!");

			while (unitIndex > 0) { // Building the player action, unit by unit.
				unitIndex--;
				Pair<Unit, List<UnitAction>> unitChoices = choices.get(unitIndex); // Get the choices of the unit in unitIndex
				int choice = currentChoice[unitIndex]; // return the unit action choice for the unit in unitIndex.

				Unit unit = unitChoices.m_a; // The relevant Unit.
				UnitAction unitAction = unitChoices.m_b.get(choice); // The chosen action.

				ResourceUsage resourceUsage = unitAction.resourceUsage(unit, physicalGameState); // retrieve action resource usage.

				// Check for consistency with the global resource usage.
				if (playerAction.getResourceUsage().consistentWith(resourceUsage, gameState)) {
					playerAction.getResourceUsage().merge(resourceUsage);
					playerAction.addUnitAction(unit, unitAction);
				} else {
					consistent = false;
					break;
				}
			}

			// Increment the current choice for the unit in unitIndex. unitIndex vary between 0 and choices.size()-1
			incrementCurrentChoice(unitIndex);
			if (consistent) { // return the built playerAction if the chosen unit action combination is consistent with the global resource usage.
				lastAction = playerAction; // last returned action.
				generated++; // increment number of generated actions.
				return playerAction;
			}

			// check if we are over time (only check once every 1000 actions, since currentTimeMillis is a slow call):
			if (cutOffTime > 0 && (count % 1000 == 0) && System.currentTimeMillis() > cutOffTime) {
				lastAction = null;
				System.out.println("Null Action ! Time Out.");
				return null;
			}
			count++;

		}

		lastAction = null;
		return null;
	}

	/**
	 * Returns the next PlayerAction for the state stored in this object.
	 * PlayerActions are filtered out of no-op idle actions, and only a predefined amount of idle actions is allowed.
	 * @param cutOffTime Time to stop generating actions.
	 * @param allowedWaitActions The number of inaction combinations to generate before disallowing them.
	 * @return
	 * @throws Exception
	 */
	public PlayerAction getNextInactionFilteredAction(long cutOffTime, long allowedWaitActions) throws Exception {

		int count = 0;

		while (moreActions) {

			boolean consistent = true;
			boolean inactionAllowedForThisPlayerAction = false;
			PlayerAction playerAction = new PlayerAction();
			playerAction.setResourceUsage(baseResourceUsage.clone());

			int unitIndex = choices.size();
			if (unitIndex == 0)
				throw new Exception("Move generator created with no units that can execute actions!");

			while (unitIndex > 0) {
				unitIndex--;
				Pair<Unit, List<UnitAction>> actionChoices = choices.get(unitIndex);
				int choice = currentChoice[unitIndex];
				Unit unit = actionChoices.m_a;
				UnitAction unitAction = actionChoices.m_b.get(choice);

				// Check whether this unitAction is an idle action and only allow it if the provided
				// limit of allowed wait actions is reached. Inaction only checked once per PlayerAction.
				if (choiceSizes[unitIndex] > 1 && allowedWaitActions != -1 && !inactionAllowedForThisPlayerAction &&
					unitAction.getType() == UnitAction.TYPE_NONE) {
					if (inactionCombinationsCount >= allowedWaitActions) {
//						consistent = false; // Added
						IPAsRemoved++;
						playerAction.actions.clear();
						break;
					}
					else {
						inactionCombinationsCount++;
						inactionAllowedForThisPlayerAction = true;
					}
				}

				ResourceUsage resourceUsage = unitAction.resourceUsage(unit, physicalGameState);

				if (playerAction.getResourceUsage().consistentWith(resourceUsage, gameState)) {
					playerAction.getResourceUsage().merge(resourceUsage);
					playerAction.addUnitAction(unit, unitAction);
				} else {
					consistent = false;
					break;
				}
			}

			incrementCurrentChoice(unitIndex);
			if (consistent && !playerAction.isEmpty()) {
				lastAction = playerAction;
				generated++;
				return playerAction;
			}

			// Changed from 1000 to 500
			if (cutOffTime > 0 && (count % 1000) == 0 && System.currentTimeMillis() > cutOffTime) {
				lastAction = null;
				return null;
			}
			count++;
		}

		lastAction = null;
		return null;
	}

	/**
	 * Returns the next PlayerAction for the state stored in this object.
	 * PlayerActions are filtered out of no-op idle actions following a given filtering probability.
	 * @param cutOffTime Time to stop generating actions.
	 * @param allowProbability The probability of allowing an inaction combination in the next PlayerAction. [0,1]
	 * @return
	 * @throws Exception
	 */
	public PlayerAction getNextInactionFilteredActionProbabilistic(long cutOffTime, float allowProbability) throws Exception {

		int count = 0;

		while (moreActions) {

			boolean consistent = true;
			boolean inactionAllowedForThisPlayerAction = false;
			PlayerAction playerAction = new PlayerAction();
			playerAction.setResourceUsage(baseResourceUsage.clone());

			int unitIndex = choices.size();
			if (unitIndex == 0)
				throw new Exception("Move generator created with no units that can execute actions!");

			while (unitIndex > 0) {
				unitIndex--;
				Pair<Unit, List<UnitAction>> actionChoices = choices.get(unitIndex);
				Unit unit = actionChoices.m_a;
				int choice = currentChoice[unitIndex];
				UnitAction unitAction = actionChoices.m_b.get(choice);

				// Check whether this unitAction is an idle action and only allow it if the provided
				// limit of allowed wait actions is reached. Inaction only checked once per PlayerAction.
				if (choiceSizes[unitIndex] > 1 && // for cases where the only unit-action possible is wait.
					unitAction.getType() == UnitAction.TYPE_NONE &&
					!inactionAllowedForThisPlayerAction &&
					allowProbability < 1f) {

					float chance = random.nextFloat();
					if (chance < allowProbability)
						inactionAllowedForThisPlayerAction = true;
					else {
//						consistent = false;
//						if (playerAction.actions.size() < choices.size()) consistent = false;
//						System.out.println("PlayerAction.Size : " + playerAction.actions.size() +
//								" Choices.Size : " + choices.size());
//						unitAction = new UnitAction(UnitAction.TYPE_NONE, random.nextInt(3));
//						continue;
						playerAction.actions.clear();
						IPAsRemoved++;
						break;
					}

				}

				ResourceUsage resourceUsage = unitAction.resourceUsage(unit, physicalGameState);

				if (playerAction.getResourceUsage().consistentWith(resourceUsage, gameState)) {
					playerAction.getResourceUsage().merge(resourceUsage);
					playerAction.addUnitAction(unit, unitAction);
				} else {
					consistent = false;
					break;
				}
			}

			incrementCurrentChoice(unitIndex);
			if (consistent && !playerAction.isEmpty()) {
				lastAction = playerAction;
				generated++;
				return playerAction;
			}

			// Changed from 1000 to 500. Check for timeout every 500 action.o
			if (cutOffTime > 0 && (count % 500) == 0 && System.currentTimeMillis() > cutOffTime) {
				lastAction = null;
				return null;
			}
			count++;
		}

		lastAction = null;
		return null;
	}

	public PlayerAction getNextActionRandomlyPruned(long cutOffTime, float allowProbability) throws Exception {

		int count = 0;

		while (moreActions) {

			boolean consistent = true;
			boolean keepCurrentPlayerAction = false;
			PlayerAction playerAction = new PlayerAction();
			playerAction.setResourceUsage(baseResourceUsage.clone());

			int unitIndex = choices.size();
			if (unitIndex == 0)
				throw new Exception("Move generator created with no units that can execute actions!");

			while (unitIndex > 0) {
				unitIndex--;
				Pair<Unit, List<UnitAction>> actionChoices = choices.get(unitIndex);
				int choice = currentChoice[unitIndex];
				Unit unit = actionChoices.m_a;
				UnitAction unitAction = actionChoices.m_b.get(choice);

				// Check if the current player action being generated is to prune or not.
				if (choiceSizes[unitIndex] > 1 && !keepCurrentPlayerAction && allowProbability < 1f &&
						unitAction.getType() != UnitAction.TYPE_NONE) {
					float chance = random.nextFloat();
					if (chance < allowProbability) { //Keep
						keepCurrentPlayerAction = true;
					}
					else { //Prune
						playerAction.actions.clear();
						break;
					}
				}

				ResourceUsage resourceUsage = unitAction.resourceUsage(unit, physicalGameState);

				if (playerAction.getResourceUsage().consistentWith(resourceUsage, gameState)) {
					playerAction.getResourceUsage().merge(resourceUsage);
					playerAction.addUnitAction(unit, unitAction);
				} else {
					consistent = false;
					break;
				}
			}

			incrementCurrentChoice(unitIndex);
			if (consistent && !playerAction.isEmpty()) {
				lastAction = playerAction;
				generated++;
				return playerAction;
			}

			// Changed from 1000 to 500. Check for timeout every 500 action.o
			if (cutOffTime > 0 && (count % 500) == 0 && System.currentTimeMillis() > cutOffTime) {
				lastAction = null;
				return null;
			}
			count++;
		}

		lastAction = null;
		return null;
	}

	// WIP
	public PlayerAction getNextActionRandomlyPrunedFixed(long cutOffTime, long allowedActions) throws Exception {

		int count = 0;

		while (moreActions) {

			boolean consistent = true;
			boolean keepCurrentPlayerAction = false;
			PlayerAction playerAction = new PlayerAction();
			playerAction.setResourceUsage(baseResourceUsage.clone());

			int unitIndex = choices.size();
			if (unitIndex == 0)
				throw new Exception("Move generator created with no units that can execute actions!");

			while (unitIndex > 0) {
				unitIndex--;
				Pair<Unit, List<UnitAction>> actionChoices = choices.get(unitIndex);
				int choice = currentChoice[unitIndex];
				Unit unit = actionChoices.m_a;
				UnitAction unitAction = actionChoices.m_b.get(choice);

				// Check whether this unitAction is an idle action and only allow it if the provided
				// limit of allowed wait actions is reached. Inaction only checked once per PlayerAction.
				if (choiceSizes[unitIndex] > 1 && allowedActions > -1 && !keepCurrentPlayerAction &&
						unitAction.getType() != UnitAction.TYPE_NONE) {
					if (allowedActionsCount >= allowedActions) {
//						consistent = false; // Added
						playerAction.actions.clear();
						break;
					}
					else {
						allowedActionsCount++;
						keepCurrentPlayerAction = true;
					}
				}

				ResourceUsage resourceUsage = unitAction.resourceUsage(unit, physicalGameState);

				if (playerAction.getResourceUsage().consistentWith(resourceUsage, gameState)) {
					playerAction.getResourceUsage().merge(resourceUsage);
					playerAction.addUnitAction(unit, unitAction);
				} else {
					consistent = false;
					break;
				}
			}

			incrementCurrentChoice(unitIndex);
			if (consistent && !playerAction.isEmpty()) {
				lastAction = playerAction;
				generated++;
				return playerAction;
			}

			// Changed from 1000 to 500. Check for timeout every 500 action.o
			if (cutOffTime > 0 && (count % 500) == 0 && System.currentTimeMillis() > cutOffTime) {
				lastAction = null;
				return null;
			}
			count++;
		}

		lastAction = null;
		return null;
	}

	/**
	 * Increases the index that tracks the next action to be returned.
	 * Creates new action combinations by incrementing currentChoice values.
	 * by {@link #getNextAction(long)}
	 * @param startPosition
	 */
	public void incrementCurrentChoice(int startPosition) {
		for (int i = 0; i < startPosition; i++) // All previous positions in currentChoice back to zero.
			currentChoice[i] = 0;

		currentChoice[startPosition]++; //
		if (currentChoice[startPosition] >= choiceSizes[startPosition]) {
			if (startPosition < currentChoice.length - 1) {
				incrementCurrentChoice(startPosition + 1);
			} else {
				moreActions = false;
			}
		}
	}

	/**
	 * Returns a random player action for the game state in this object
	 * @return
	 */
	public PlayerAction getRandom() {

		Random random = new Random();
		PlayerAction playerAction = new PlayerAction();
		playerAction.setResourceUsage(baseResourceUsage.clone());

		for (Pair<Unit, List<UnitAction>> unitChoices : choices) {
			List<UnitAction> unitActions = new LinkedList<UnitAction>();
			unitActions.addAll(unitChoices.m_b);
			Unit unit = unitChoices.m_a;

			boolean consistent = false;
			do {
				UnitAction unitAction = unitActions.remove(random.nextInt(unitActions.size()));
				ResourceUsage resourceUsage = unitAction.resourceUsage(unit, physicalGameState);

				if (playerAction.getResourceUsage().consistentWith(resourceUsage, gameState)) {
					playerAction.getResourceUsage().merge(resourceUsage);
					playerAction.addUnitAction(unit, unitAction);
					consistent = true;
				}
			} while (!consistent);
		}
		return playerAction;
	}

	/**
	 * Shuffles the list of unit actions for each unit in choices list.
	 */
	public void randomizeOrder() {
		for (Pair<Unit, List<UnitAction>> choice : choices) {
			List<UnitAction> tmp = new LinkedList<>();
			tmp.addAll(choice.m_b);
			choice.m_b.clear();

			while (!tmp.isEmpty())
				choice.m_b.add(tmp.remove(random.nextInt(tmp.size())));
		}
	}

	/**
	 * Shuffles the list of unit actions for each unit in the choices list, but keep
	 * the idle action last.
	 */
	public void randomizeOrderKeepWaitLast() {
		for (Pair<Unit, List<UnitAction>> choice : choices) {
			List<UnitAction> actions = new LinkedList<>(choice.m_b);
			UnitAction waitAction = actions.remove(actions.size() - 1); // The no-op/idle action is always last.
			choice.m_b.clear();

			while (!actions.isEmpty())
				choice.m_b.add(actions.remove(random.nextInt(actions.size())));

			choice.m_b.add(waitAction);
		}
	}

	/**
	 * Inverse Filtering.
	 * Distributes wait (idle/none) actions among the available units in choices list. Relies on a probability of distribution.
	 * To use with Unit.getActionsWithoutWait(gs) since the generated action won't have a wait action.
	 * @param waitProbability The probability of assigning a wait action to a unit's list of available actions.
	 */
	public void distributeWaitActions(float waitProbability) {
		int index = 0;

		if (waitProbability > 0f) {
			for (Pair<Unit, List<UnitAction>> choice : choices) {
				float chance = random.nextFloat();
				if (chance < waitProbability) {
					choice.m_b.add(new UnitAction(UnitAction.TYPE_NONE, 10));
					choiceSizes[index]++;
				}
				index++;
			}
		}
	}

	/**
	 * Finds the index of a given PlayerAction within the list of PlayerActions
	 * @param playerAction
	 * @return
	 */
	public long getActionIndex(PlayerAction playerAction) {

		int[] choice = new int[choices.size()];

		for (Pair<Unit, UnitAction> unitUnitActionPair : playerAction.actions) {

			int index = 0;
			Pair<Unit, List<UnitAction>> currentUnitActionList = null;

			for (Pair<Unit, List<UnitAction>> unitActionList : choices) {
				if (unitUnitActionPair.m_a == unitActionList.m_a) {
					currentUnitActionList = unitActionList;
					break;
				}
				index++;
			}

			if (currentUnitActionList == null)
				return -1;
			choice[index] = currentUnitActionList.m_b.indexOf(unitUnitActionPair.m_b);
		}

		long index = 0;
		long multiplier = 1;
		for (int i = 0; i < choice.length; i++) {
			index += choice[i] * multiplier;
			multiplier *= choiceSizes[i];
		}
		return index;
	}

	public String toString() {
		String ret = "PlayerActionGenerator:\n";
		for(Pair<Unit,List<UnitAction>> choice:choices) {
			ret = ret + "  (" + choice.m_a + "," + choice.m_b.size() + ")\n";
		}
		ret += "currentChoice: ";
		for(int i = 0;i<currentChoice.length;i++) {
			ret += currentChoice[i] + " ";
		}
		ret += "\nactions generated so far: " + generated;
		return ret;
	}

    /**
     *
     * @return
     */
    public long getGenerated() {
        return generated;
    }

    public long getSize() {
        return size;
    }

    public PlayerAction getLastAction() {
        return lastAction;
    }

    public List<Pair<Unit,List<UnitAction>>> getChoices() {
        return choices;
    }

    public PlayerAction getNextActionV2() {
    	return new PlayerAction();
	}
}
