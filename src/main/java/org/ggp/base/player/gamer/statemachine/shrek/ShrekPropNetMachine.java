package org.ggp.base.player.gamer.statemachine.shrek;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.gdl.grammar.GdlConstant;
import org.ggp.base.util.gdl.grammar.GdlRelation;
import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.propnet.architecture.Component;
import org.ggp.base.util.propnet.architecture.PropNet;
import org.ggp.base.util.propnet.architecture.components.And;
import org.ggp.base.util.propnet.architecture.components.Not;
import org.ggp.base.util.propnet.architecture.components.Or;
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

	private static final Logger LOGGER = Logger.getLogger(ShrekPropNetMachine.class.getName());
	static {
		// FINE is very detailed info alikin to prints
		//ConsoleHandler ch = new ConsoleHandler();
		//ch.setLevel(Level.ALL);
		//LOGGER.addHandler(ch);
		LOGGER.setLevel(Level.ALL);
	}

	/** The underlying proposition network */
	private PropNet propNet;
	/** The topological ordering of the propositions */
	private List<Proposition> ordering;
	/** The player roles */
	private List<Role> roles;

	private Set<Proposition> setSinceClear;

	/**
	 * Initializes the PropNetStateMachine. You should compute the topological
	 * ordering here. Additionally you may compute the initial state here, at
	 * your discretion.
	 */
	@Override
	public void initialize(List<Gdl> description) {
		LOGGER.entering(this.getClass().getName(), "initialize");
		try {
			propNet = OptimizingPropNetFactory.create(description);
			roles = propNet.getRoles();
			ordering = getOrdering();
			setSinceClear = new HashSet<Proposition>();
		} catch (InterruptedException e) {
			LOGGER.log(Level.SEVERE, "Prop net initialie exception", e);
		}
		//this.propNet.renderToFile("test.graph");
		//LOGGER.fine(this.propNet);
		LOGGER.exiting(this.getClass().getName(), "initialize");
	}

	/**
	 * Computes if the state is terminal. Should return the value of the
	 * terminal proposition for the state.
	 */
	@Override
	public boolean isTerminal(MachineState state) {
		LOGGER.entering(this.getClass().getName(), "isTerminal");

		markBases(state);
		LOGGER.exiting(this.getClass().getName(), "isTerminal");
		return propSet(propNet.getTerminalProposition());

	}



	/**
	 * Computes the goal for a role in the current state. Should return the
	 * value of the goal proposition that is true for that role. If there is not
	 * exactly one goal proposition true for that role, then you should throw a
	 * GoalDefinitionException because the goal is ill-defined.
	 */
	@Override
	public int getGoal(MachineState state, Role role) throws GoalDefinitionException {
		LOGGER.entering(this.getClass().getName(), "getGoal");
		markBases(state);

		// Get legal propositions for the current role
		Set<Proposition> legalProps = propNet.getGoalPropositions().get(role);

		// If the goal propositon is true, return the goal value, else zero
		for (Proposition prop : legalProps) {
			if (propSet(prop)) {
				LOGGER.exiting(this.getClass().getName(), "getGoal");
				return getGoalValue(prop);
			}
		}

		// No soup for you
		LOGGER.exiting(this.getClass().getName(), "getGoal");
		return 0;
	}

	/**
	 * Returns the initial state. The initial state can be computed by only
	 * setting the truth value of the INIT proposition to true, and then
	 * computing the resulting state.
	 */
	@Override
	public MachineState getInitialState() {
		LOGGER.entering(this.getClass().getName(), "getInitialState");

		clearPropNet();
		propNet.getInitProposition().setValue(true);

    	MachineState initialState = getStateFromBase();

    	propNet.getInitProposition().setValue(false);
		LOGGER.exiting(this.getClass().getName(), "getInitialState");
        return initialState;

	}

	/**
	 * Computes all possible actions for role.
	 */
	@Override
	public List<Move> findActions(Role role) throws MoveDefinitionException {
		LOGGER.entering(this.getClass().getName(), "findActions");

		clearPropNet();
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
	@Override
	public List<Move> getLegalMoves(MachineState state, Role currentRole) throws MoveDefinitionException {
		LOGGER.entering(this.getClass().getName(), "getLegalMoves");

		clearPropNet();
		markBases(state);
		List<Move> legalMoves = new ArrayList<Move>();
		Set<Proposition> legalProps = propNet.getLegalPropositions().get(currentRole);
		for (Proposition prop : legalProps) {
			boolean isPropSet = propSet(prop);
			if (isPropSet) {
				legalMoves.add(getMoveFromProposition(prop));
			}
		}

		LOGGER.fine("Legal Moves: " + legalMoves + "(Should not be empty)");

		LOGGER.exiting(this.getClass().getName(), "getLegalMoves");

		return legalMoves;
	}

	/**
	 * Get Joint Legal Moves
	 */
	/*@Override
	public List<List<Move>> getJointLegalMoves()
	{

	}*/

	/**
	 * Computes the next state given state and the list of moves.
	 */
	@Override
	public MachineState getNextState(MachineState state, List<Move> moves) throws TransitionDefinitionException {
		LOGGER.entering(this.getClass().getName(), "getNextState");

		clearPropNet();
		markInputs(moves);
		markBases(state);
		Map<GdlSentence, Proposition> basePropMap = propNet.getBasePropositions();
        Set<GdlSentence> nextStateContents = new HashSet<GdlSentence>();
        for (GdlSentence s : basePropMap.keySet()) {
        	Proposition currProp = basePropMap.get(s);
        	if (propSet(currProp.getSingleInput().getSingleInput()))
        		nextStateContents.add(s);
        }

		LOGGER.exiting(this.getClass().getName(), "getNextState");
        return new MachineState(nextStateContents);
//		LOGGER.info("Computed next state: " + nextState.toString());
//		return nextState;

	}

	public MachineState computeState() {

		LOGGER.entering(this.getClass().getName(), "computeState");

		for (Proposition prop : propNet.getBasePropositions().values()) {
			prop.setValue(propSet(prop.getSingleInput().getSingleInput()));
		}

		LOGGER.exiting(this.getClass().getName(), "computeState");
		// Creates a MachineState from the BasePropositons
		return getStateFromBase();
	}
	/**
	 * Recursive helper function for getOrdering, marks current vertex as visited and then recurses on the
	 * vertex next to it in the adjacency list
	 *
	 * @param v: The integer corresponding to node
	 */


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
	public List<Proposition> getOrdering() {
		LOGGER.entering(this.getClass().getName(), "getOrdering");

		// List to contain the topological ordering.
		List<Proposition> order = new LinkedList<Proposition>();

		// All of the components in the PropNet
		List<Component> components = new ArrayList<Component>(propNet.getComponents());

		// All of the propositions in the PropNet.
		List<Proposition> propositions = new ArrayList<Proposition>(propNet.getPropositions());

		//compute the in-degree of each node, create a map from each prop to its indegree
		//get number of components in the graph
		int numNodes = components.size();
		Map <Component, Integer> inDegrees = new HashMap<Component, Integer> (numNodes);
		for (Proposition prop: propositions)
		{
			int inDegree = prop.getOutputs().size();
			inDegrees.put(prop, inDegree);
		}
		System.out.println(inDegrees.toString());

		Queue<Proposition> q = new LinkedList<Proposition>();
		//enqueue all input propositions and base propositions (indegree 0)
		for (Proposition p: propNet.getInputPropositions().values())
		{
			q.add(p);
		}
		for (Proposition p: propNet.getBasePropositions().values())
		{
			q.add(p);
		}
		System.out.println(q.toString());

		//add each proposition to ordering list and decrease indegree order of neighboring propositions
		while (!q.isEmpty())
		{
			Proposition p = q.poll();
			order.add(p);

			//get outputs of p
			Set<Component> outputs = p.getOutputs();

			//decrease indegree of each output by 1
			for (Component c: outputs)
			{
				inDegrees.put(c,inDegrees.get(c)-1);
				//if new value of indegrees for c == 0, add to queue
				if (inDegrees.get(c) == 0)
				{
					q.add((Proposition) c);
				}
			}
		}

		for (int i = 0; i < order.size(); i++)
		{
			System.out.println(order.get(i));
		}

		LOGGER.exiting(this.getClass().getName(), "getOrdering");

		return order;
	}

	/* Already implemented for you */
	@Override
	public List<Role> getRoles() {
		return roles;
	}

	/* Start Helper methods */ // ------------------------------------------------------------------------------------------

	/*
	 * markBases sets all the base propostitions to match those in the given
	 * state
	 */
	private void markBases(MachineState state) {
		LOGGER.entering(this.getClass().getName(), "markBases");

		for (GdlSentence sent : propNet.getBasePropositions().keySet()) {
			// Get out previous proposition
			Proposition prop = propNet.getBasePropositions().get(sent);

			// Set prop true for values in the state, false for everything else
			prop.setValue(state.getContents().contains(sent));


		}
		LOGGER.exiting(this.getClass().getName(), "markBases");

	}

	/*
	 * The markInputs functions goes through the given boolean of input props
	 * and marks them on the provided set in propNet
	 */
	private void markInputs(List<Move> moveSet) {
		LOGGER.entering(this.getClass().getName(), "markInputs");

		List<GdlSentence> moveSents = toDoes(moveSet);
		for (GdlSentence sent : propNet.getInputPropositions().keySet()) {
			// Get out previous proposition
			Proposition prop = propNet.getInputPropositions().get(sent);

			// Set prop true for values in the set of moves, false for
			// everything else
			// toDoes maps a list of moves to a list of GdlSentences
			prop.setValue(moveSents.contains(sent));
		}
		LOGGER.exiting(this.getClass().getName(), "markInputs");
	}

	/*
	 * clears the propNet bases
	 */
	private void clearPropNet() {
		LOGGER.entering(this.getClass().getName(), "clearPropNet");

		setSinceClear.clear();
		for (GdlSentence sent : propNet.getInputPropositions().keySet()) {
			// Get out previous proposition
			Proposition prop = propNet.getInputPropositions().get(sent);

			// Set everything to false
			prop.setValue(false);

			// Place modified proposition back in, now false
//			propNet.getInputPropositions().put(sent, prop);
		}
		LOGGER.exiting(this.getClass().getName(), "markInputs");

	}

	/**
	 * The propSet function computes the value of view Propositions
	 *
	 * @param p
	 */
	private boolean propSet(Component p) {
		LOGGER.entering(this.getClass().getName(), "propSet");

		LOGGER.fine("propMark Type of Proposition: " + p.getClass() + "and value "+ p.getValue());

		// Base and input values are standalone and hold the values of states
		// and sets of moves respectively
		// Transitions always lead to base propositions, but this function will
		// never get called on Transitions
		if (isBase(p)){
			LOGGER.fine("BASE FOUND");
			LOGGER.exiting(this.getClass().getName(), "propSet");
			return p.getValue();
		} else if (isInput(p)){
			LOGGER.fine("INPUT FOUND");
			LOGGER.exiting(this.getClass().getName(), "propSet");
			return p.getValue();
		} else if (p instanceof Not){
			LOGGER.fine("NOT FOUND");
			LOGGER.exiting(this.getClass().getName(), "propSet");
			return propMarkNegation(p);
		} else if (p instanceof And) {
			LOGGER.fine("AND FOUND");
			LOGGER.exiting(this.getClass().getName(), "propSet");
			return propMarkConjunction(p);
		} else if (p instanceof Or){
			LOGGER.fine("OR FOUND");
			LOGGER.exiting(this.getClass().getName(), "propSet");
			return propMarkDisjunction(p);
		} else {
			// Must be view as that is the only category left
			LOGGER.fine("SHOULD BE VIEW");
			Proposition prop = (Proposition) p;
			if (!setSinceClear.contains(p)){
				prop.setValue(propSet(p.getSingleInput()));
				setSinceClear.add(prop);
			}
			LOGGER.exiting(this.getClass().getName(), "propSet");
			return p.getValue();
		}


	}

	private boolean isView(Component p) {
		// Checks to see if there is an input from a connective
		if (!(p instanceof Proposition)) {
			return false;
		}
		Set<Component> components = p.getInputs();
		for (Component component : components) {
			if (component instanceof Transition) {
				return false;
			}
		}
		return true;
	}

	private boolean isBase(Component p) {
		// Has one single input from a Transition
		return (p instanceof Proposition && p.getInputs().size() == 1 && p.getSingleInput() instanceof Transition);
	}

	private boolean isInput(Component p) {
		// Has no inputs as is simply a representation of a move
		return (p instanceof Proposition && p.getInputs().size() == 0);
	}




	private boolean propMarkNegation(Component p) {
		//should return the negation of the component before p
		LOGGER.entering(this.getClass().getName(), "propMarkNegation");
		LOGGER.exiting(this.getClass().getName(), "propMarkNegation");
		return !propSet(p.getSingleInput());
	}

	private boolean propMarkConjunction(Component p) {
		LOGGER.entering(this.getClass().getName(), "propMarkConjunction");
		Set<Component> sources = p.getInputs();
		for (Component component : sources) {
			if (!propSet(component)){
				LOGGER.exiting(this.getClass().getName(), "propMarkConjunction");
				return false;
			}
		}
		LOGGER.exiting(this.getClass().getName(), "propMarkConjunction");
		return true;
	}

	private boolean propMarkDisjunction(Component p) {
		LOGGER.entering(this.getClass().getName(), "propMarkDisjunction");

		Set<Component> sources = p.getInputs();
		for (Component component : sources) {
			if (propSet(component)) {
				LOGGER.exiting(this.getClass().getName(), "propMarkDisjunction");
				return true;
			}
		}
		LOGGER.exiting(this.getClass().getName(), "propMarkDisjunction");
		return false;
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
