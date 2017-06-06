package org.ggp.base.player.gamer.statemachine.shrek;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.gdl.grammar.GdlConstant;
import org.ggp.base.util.gdl.grammar.GdlRelation;
import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.propnet.architecture.Component;
import org.ggp.base.util.propnet.architecture.PropNet;
import org.ggp.base.util.propnet.architecture.components.Proposition;
import org.ggp.base.util.propnet.architecture.components.Transition;
import org.ggp.base.util.propnet.factory.OptimizingPropNetFactory;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.implementation.prover.query.ProverQueryBuilder;

public class ShrekPropNetMachine extends StateMachine {
	//TODO: clear propnet?

	private static final Logger LOGGER = Logger.getLogger(ShrekPropNetMachine.class.getName());
	static {
		// FINE is very detailed info like prints
		SimpleFormatter fmt = new SimpleFormatter();
		StreamHandler sh = new StreamHandler(System.out, fmt);
		sh.setLevel(Level.SEVERE);
		LOGGER.addHandler(sh);
		LOGGER.setLevel(Level.SEVERE);
	}

	/** The underlying proposition network */
	private PropNet propNet;
	/** The topological ordering of the propositions */
	private List<Component> ordering;
	/** The player roles */
	private List<Role> roles;

	/**
	 * Initializes the PropNetStateMachine. You should compute the topological
	 * ordering here. Additionally you may compute the initial state here, at
	 * your discretion.
	 */
	// PROBABLY RIGHT
	@Override
	public void initialize(List<Gdl> description) {
		LOGGER.entering(this.getClass().getName(), "initialize");
		try {
			//subgoal reordering - simplest subgoals first

			propNet = OptimizingPropNetFactory.create(description);
			roles = propNet.getRoles();
			ordering = getOrdering();
			Collections.shuffle(ordering);
		} catch (InterruptedException e) {
			LOGGER.log(Level.SEVERE, "Prop net initialie exception", e);
		}
		// this.propNet.renderToFile("test.graph");
		// LOGGER.fine(this.propNet);
		LOGGER.exiting(this.getClass().getName(), "initialize");
	}

	/**
	 * Computes if the state is terminal. Should return the value of the
	 * terminal proposition for the state.
	 */
	@Override
	public boolean isTerminal(MachineState state) {
		LOGGER.entering(this.getClass().getName(), "isTerminal");

		computeAllStates(state, null);

		LOGGER.exiting(this.getClass().getName(), "isTerminal");
		return propNet.getTerminalProposition().getValue();

	}

	/**
	 * Computes the goal for a role in the current state. Should return the
	 * value of the goal proposition that is true for that role. If there is not
	 * exactly one goal proposition true for that role, then you should throw a
	 * GoalDefinitionException because the goal is ill-defined.
	 */
	// PROBABLY RIGHT
	@Override
	public int getGoal(MachineState state, Role role) throws GoalDefinitionException {
		LOGGER.entering(this.getClass().getName(), "getGoal");

		computeAllStates(state, null);

		// Get legal propositions for the current role
		Set<Proposition> goalProps = propNet.getGoalPropositions().get(role);

		// If the goal proposition is true, return the goal value, else zero
		for (Proposition prop : goalProps) {
			if (prop.getValue()) {
				LOGGER.exiting(this.getClass().getName(), "getGoal");
				return getGoalValue(prop);
			}
		}

		LOGGER.exiting(this.getClass().getName(), "getGoal");
		return 0;
	}

	/**
	 * Returns the initial state. The initial state can be computed by only
	 * setting the truth value of the INIT proposition to true, and then
	 * computing the resulting state.
	 */
	// REALLY PROBABLY RIGHT
	@Override
	public MachineState getInitialState() {
		LOGGER.entering(this.getClass().getName(), "getInitialState");

		Proposition init = propNet.getInitProposition();
		init.setValue(true);

		Set<Component> toProcess = new HashSet<Component>();
		toProcess.add(propNet.getInitProposition());
		// Compute all the states from the init component
		processComponents(toProcess);

		MachineState initialState = getStateFromBase();
		LOGGER.info(initialState.toString());

		init.setValue(false);
		LOGGER.exiting(this.getClass().getName(), "getInitialState");
		return initialState;
	}

	/**
	 * Computes all possible actions for role.
	 */
	// Probably irrelevant
	@Override
	public List<Move> findActions(Role role) throws MoveDefinitionException {
		LOGGER.entering(this.getClass().getName(), "findActions");

		Set<Proposition> legalProps = propNet.getLegalPropositions().get(role);

		List<Move> moves = new ArrayList<Move>();
		for (Proposition prop : legalProps) {
			moves.add(getMoveFromProposition(prop));
		}

		LOGGER.exiting(this.getClass().getName(), "findActions");
		return moves;
	}

	/**
	 * Computes the legal moves for role in state.
	 */
	// PROBABLY RIGHT
	@Override
	public List<Move> getLegalMoves(MachineState state, Role currentRole) throws MoveDefinitionException {
		LOGGER.entering(this.getClass().getName(), "getLegalMoves");

		computeAllStates(state, null);

		List<Move> legalMoves = new ArrayList<Move>();
		Set<Proposition> legalProps = propNet.getLegalPropositions().get(currentRole);
		for (Proposition prop : legalProps) {
			LOGGER.info("IS_LEGAL?: " + prop.getValue());
			// If prop is true
			if (prop.getValue()) {
				legalMoves.add(getMoveFromProposition(prop));
			}
		}

		LOGGER.fine("Legal Moves: " + legalMoves + "(Should not be empty)");

		LOGGER.exiting(this.getClass().getName(), "getLegalMoves");
		return legalMoves;
	}

	/**
	 * Computes the next state given state and the list of moves.
	 */
	// PROBABLY RIGHT (BUT MAYBE NOT)
	@Override
	public MachineState getNextState(MachineState state, List<Move> moves) throws TransitionDefinitionException {
		LOGGER.entering(this.getClass().getName(), "getNextState");

		// Compute the next state
		computeAllStates(state, moves);

		LOGGER.exiting(this.getClass().getName(), "getNextState");

		return getStateFromBase();
	}

	/*
	 * Uses differential propagation to build out the whole propnet efficiently
	 * from the state or moveSet given to it.
	 */
	/*
	public void deltaComputeState(MachineState state, List<Move> moveSet) {
		LOGGER.entering(this.getClass().getName(), "deltaComputeState");

		Set<Component> toProcess = new HashSet<Component>();

		// Only add the bases and inputs that changed
		if (state != null) {
			toProcess.addAll(markBases(state));
		}
		if (moveSet != null) {
			toProcess.addAll(markInputs(moveSet));
		}

		processComponents(toProcess);

		LOGGER.exiting(this.getClass().getName(), "deltaComputeState");
	}
	*/

	/*
	 * Simply computes all states from the given machinestate and moveset
	 */
	// PROBABLY RIGHT
	public void computeAllStates(MachineState state, List<Move> moveSet) {
		LOGGER.entering(this.getClass().getName(), "computeAllStates");

		Set<Component> toProcess = new HashSet<Component>();

		if (state != null) {
			markBases(state);
		}

		if (moveSet != null) {
			markInputs(moveSet);
		}

		// add all the bases and inputs that changed
		//toProcess.addAll(propNet.getBasePropositions().values());
		//toProcess.addAll(propNet.getInputPropositions().values());

		toProcess.addAll(ordering);

		processComponents(toProcess);

		LOGGER.exiting(this.getClass().getName(), "computeAllStates");
	}

	// NOT SURE WHY THIS EXISTS
	private void processTransitions ()
	{
		for (Proposition p: propNet.getBasePropositions().values())
		{
			Component input = p.getSingleInput();
			if (input instanceof Transition)
			{
				p.setValue(input.getValue());
			}
		}
	}

	// PROBABLY WRONG
	private void processComponents(Set<Component> toProcess) {
		LOGGER.entering(this.getClass().getName(), "f");

		LOGGER.fine(toProcess.toString());

		for (Component c : ordering) {
			//LOGGER.fine("checking: " + c.getClass().getName());

			// Sets the value of AND, OR, NOT and Transition components and
			// leaves propositions alone
			// The proposition and constant setValue() method simply does
			// nothing
			if (toProcess.contains(c)) {
				LOGGER.fine("Processing:" + c.getClass().getName() + " val=" + c.getValue());
				if(!(propNet.getBasePropositions().containsValue(c) || propNet.getInputPropositions().containsValue(c) || propNet.getInitProposition() == c)) {
					c.setValue();
				}

				LOGGER.fine("New Val:" + c.getValue());
				if(!(c instanceof Transition)){
					toProcess.addAll(c.getOutputs());
				//	LOGGER.fine("TO PROCESS:" + toProcess.toString());
				}

			}
		}
		processTransitions();

		LOGGER.exiting(this.getClass().getName(), "processComponents");
	}

	/**
	 * This should compute the topological ordering of propositions. Each
	 * component is either a proposition, logical gate, or transition. Logical
	 * gates and transitions only have propositions as inputs.
	 *
	 * The base propositions and input propositions should always be exempt from
	 * this ordering.
	 *
	 * The base propositions values are set from the MachineState that
	 * operations are performed on and the input propositions are set from the
	 * Moves that operations are performed on as well (if any).
	 *
	 * @return The order in which the truth values of propositions need to be
	 *         set.
	 */

	// COULD DEFINITELY BE WRONG
	public List<Component> getOrdering() {
		LOGGER.entering(this.getClass().getName(), "getOrdering");
		// List to contain the topological ordering.
		List<Component> order = new LinkedList<Component>();

		// All of the components in the PropNet
		List<Component> components = new ArrayList<Component>(propNet.getComponents());

		//components.removeAll(propNet.getBasePropositions().values());
		//components.removeAll(propNet.getInputPropositions().values());
		//components.remove(propNet.getInitProposition());


		Set<Component> tempMarks = new HashSet<Component>();

		System.out.println(propNet.getPropositions().size());


		//visit bases and inputs
		while (!components.isEmpty()) {
			Component c = components.get(0);
			visit(c, order, components, tempMarks);
		}


		// Check that all components are in order
		for (Component c : propNet.getPropositions()) {
			if (!order.contains(c)) {
				LOGGER.severe("ordering does not contain all components");
			}
		}

		LOGGER.exiting(this.getClass().getName(), "getOrdering");

		//remove base and input props from ordering
		components.removeAll(propNet.getBasePropositions().values());
		components.removeAll(propNet.getInputPropositions().values());

		validateOrdering(order);

		return order;

	}

	// COULD DEFINITELY BE WRONG
	private void visit(Component c, List<Component> order, List<Component> unmarked, Set<Component> tmpMarks) {
		LOGGER.entering(this.getClass().getName(), "visit");

		if (tmpMarks.contains(c)) {
			LOGGER.severe("CYCLE found");
			return;
		}
		if (unmarked.contains(c)) {
			tmpMarks.add(c);
			for (Component n : c.getInputs()) {
				if(!(c instanceof Transition)){
					visit(n, order, unmarked, tmpMarks);
				}
			}
			unmarked.remove(c); // Mark c
			tmpMarks.remove(c);
			order.add(0, c);
		}
		LOGGER.exiting(this.getClass().getName(), "visit");
	}

	private void validateOrdering(List<Component> order) {
		Set<Component> visited = new HashSet<Component>();
		for(Component c : order) {
			if(!visited.containsAll(c.getInputs())) {
				LOGGER.severe("TOPO SORT INVALID");
				//return false;
			}
			visited.add(c);
		}
		//return true;
	}

	/* Already implemented for you */
	@Override
	public List<Role> getRoles() {
		return roles;
	}

	/* Start Helper methods */ // ------------------------------------------------------------------------------------------

	/*
	 * markBases sets all the base propositions to match those in the given
	 * state and returns a list of the propositions that changed
	 */
	// PROBABLY RIGHT
	private List<Proposition> markBases(MachineState state) {
		LOGGER.entering(this.getClass().getName(), "markBases");

//		LOGGER.fine("state: " + state);

		List<Proposition> changed = new ArrayList<Proposition>();

		for (GdlSentence sent : propNet.getBasePropositions().keySet()) {
			// Get out previous proposition
			Proposition prop = propNet.getBasePropositions().get(sent);

			// prop true for values in the state, false for everything else
			boolean newValue = state.getContents().contains(sent);

			if (newValue != prop.getValue()) {
				// Value has changed
	//			LOGGER.fine("Setting Base Value to " + newValue);
				prop.setValue(newValue);
				changed.add(prop);
			}
		}

		LOGGER.exiting(this.getClass().getName(), "markBases");
		return changed;
	}

	/*
	 * The markInputs functions goes through the given boolean of input props
	 * and marks them on the provided set in propNet
	 */
	// PROBABLY RIGHT
	private List<Proposition> markInputs(List<Move> moveSet) {
		LOGGER.entering(this.getClass().getName(), "markInputs");

		List<Proposition> changed = new ArrayList<Proposition>();

//		LOGGER.fine("moveSet: " + moveSet);

		List<GdlSentence> moveSents = toDoes(moveSet);
		for (GdlSentence sent : propNet.getInputPropositions().keySet()) {
			// Get out previous proposition
			Proposition prop = propNet.getInputPropositions().get(sent);

			// prop true for values in the state, false for everything else
			boolean newValue = moveSents.contains(sent);

			if (newValue != prop.getValue()) {
//				LOGGER.fine("Setting Input Value to " + newValue);
				// Value has changed
				prop.setValue(newValue);
				changed.add(prop);
			}
			// Set prop true for values in the set of moves, false for
			// everything else

		}
		LOGGER.exiting(this.getClass().getName(), "markInputs");
		return changed;
	}

	/* End Helper methods */ // ------------------------------------------------------------------------------------------

	/**
	 * The Input propositions are indexed by (does ?player ?action).
	 *
	 * This translates a list of Moves (backed by a sentence that is simply
	 * ?action) into GdlSentences that can be used to get Propositions from
	 * inputPropositions. and accordingly set their values etc. This is a naive
	 * implementation when coupled with setting input values, feel free to
	 * change this for a more efficient implementation.
	 *
	 * @param moves
	 * @return
	 */
	private List<GdlSentence> toDoes(List<Move> moves) {
		LOGGER.entering(this.getClass().getName(), "toDoes");

		List<GdlSentence> doeses = new ArrayList<GdlSentence>(moves.size());
		Map<Role, Integer> roleIndices = getRoleIndices();

		for (int i = 0; i < roles.size(); i++) {
			int index = roleIndices.get(roles.get(i));
			doeses.add(ProverQueryBuilder.toDoes(roles.get(i), moves.get(index)));
		}
		LOGGER.exiting(this.getClass().getName(), "toDoes");

		return doeses;
	}

	/**
	 * Takes in a Legal Proposition and returns the appropriate corresponding
	 * Move
	 *
	 * @param p
	 * @return a PropNetMove
	 */
	public static Move getMoveFromProposition(Proposition p) {
		return new Move(p.getName().get(1));
	}

	/**
	 * Helper method for parsing the value of a goal proposition
	 *
	 * @param goalProposition
	 * @return the integer value of the goal proposition
	 */
	private int getGoalValue(Proposition goalProposition) {
		GdlRelation relation = (GdlRelation) goalProposition.getName();
		GdlConstant constant = (GdlConstant) relation.get(1);
		return Integer.parseInt(constant.toString());
	}

	/**
	 * A Naive implementation that computes a PropNetMachineState from the true
	 * BasePropositions. This is correct but slower than more advanced
	 * implementations You need not use this method!
	 *
	 * @return PropNetMachineState
	 */
	public MachineState getStateFromBase() {
		LOGGER.entering(this.getClass().getName(), "getStateFromBase");

		Set<GdlSentence> contents = new HashSet<GdlSentence>();
		for (Proposition p : propNet.getBasePropositions().values()) {
			p.setValue(p.getSingleInput().getValue());
			if (p.getValue()) {
				contents.add(p.getName());
			}

		}
		LOGGER.exiting(this.getClass().getName(), "getStateFromBase");

		return new MachineState(contents);
	}
}
