package org.ggp.base.player.gamer.statemachine.shrek;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
		LOGGER.setLevel(Level.INFO);
	}

	private static final String GRAPH_FOLDER = "/home/jbeasley/Dropbox/Documents/College/CS227B/graphs/";

	/** The underlying proposition network */
	private PropNet propNet;
	/** The topological ordering of the propositions */
	private List<Proposition> ordering;
	/** The player roles */
	private List<Role> roles;

	private boolean OUTPUT_GRAPHS = false;

	// Inc when create graph
	private int propNetNum = 0;

	/**
	 * Initializes the PropNetStateMachine. You should compute the topological
	 * ordering here. Additionally you may compute the initial state here, at
	 * your discretion.
	 */
	@Override
	public void initialize(List<Gdl> description) {
		try {
			propNet = OptimizingPropNetFactory.create(description);
			roles = propNet.getRoles();
			ordering = getOrdering();
		} catch (InterruptedException e) {
			LOGGER.log(Level.SEVERE, "Prop net initialie exception", e);
		}
		//clearGraphOutputFolder();
		createGraphFile();
	}

	private void createGraphFile() {
		if (OUTPUT_GRAPHS) {
			String filename = GRAPH_FOLDER + "propNet-" + propNetNum;
			this.propNet.renderToFile(filename + ".dot");
			propNetNum++;
		}
	}

	private void clearGraphOutputFolder(){
		File dir = new File(GRAPH_FOLDER);
		for(File file: dir.listFiles())
		    if (!file.isDirectory())
		        file.delete();
	}

	/**
	 * Computes if the state is terminal. Should return the value of the
	 * terminal proposition for the state.
	 */
	@Override
	public boolean isTerminal(MachineState state) {

		markBases(state);
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

		markBases(state);

		// Get legal propositions for the current role
		Set<Proposition> legalProps = propNet.getGoalPropositions().get(role);

		// If the goal propositon is true, return the goal value, else zero
		for (Proposition prop : legalProps) {
			if (propSet(prop)) {
				return getGoalValue(prop);
			}
		}

		// No soup for you
		return 0;
	}

	/**
	 * Returns the initial state. The initial state can be computed by only
	 * setting the truth value of the INIT proposition to true, and then
	 * computing the resulting state.
	 */
	@Override
	public MachineState getInitialState() {
		clearPropNet();
		Proposition initProp = propNet.getInitProposition();
		for (Proposition prop : propNet.getPropositions()) {
			if (prop.equals(initProp)) {
				prop.setValue(true);
			}
		}

		return computeState();
	}

	/**
	 * Computes all possible actions for role.
	 */
	@Override
	public List<Move> findActions(Role role) throws MoveDefinitionException {
		Set<Proposition> legalProps = propNet.getLegalPropositions().get(role);

		List<Move> moves = new ArrayList<Move>();
		for (Proposition prop : legalProps) {
			moves.add(getMoveFromProposition(prop));
		}

		return moves;
	}

	/**
	 * Computes the legal moves for role in state.
	 */
	// TODO: BUG HERE: propSet never returns true so there are never any legal moves
	@Override
	public List<Move> getLegalMoves(MachineState state, Role currentRole) throws MoveDefinitionException {
		clearPropNet();
		markBases(state);
		// Might need to mark inputs here

		List<Move> legalMoves = new ArrayList<Move>();

		// Get legal propositions for the current role
		Set<Proposition> legalProps = propNet.getLegalPropositions().get(currentRole);

		// Check and see which of the legal propositions is true given the set
		// bases and add the legal ones to the legalMoves list as a Move
		LOGGER.fine("============ CHECKING LEGAL PROPS =============");
		for (Proposition prop : legalProps) {


			boolean isPropSet = propSet(prop);
			LOGGER.fine(prop.getName() + " | ^^ is set: " + isPropSet);
			if (isPropSet) {

				legalMoves.add(getMoveFromProposition(prop));
			}
		}

		LOGGER.fine("Legal Moves: " + legalMoves + "(Should not be empty)");
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
		String origState = computeState().toString();
		String origBaseProps = propNet.getBasePropositions().toString();
		markInputs(moves);
		markBases(state);
		String afterState =  computeState().toString();
		String afterBaseProps = propNet.getBasePropositions().toString();


		if (origBaseProps.equals(afterBaseProps)) {
			LOGGER.fine("INPUTS/BASES DID NOT CHANGE");
		}

		MachineState nextState = computeState();
		if(nextState.toString().equals(origState)) {
			LOGGER.fine("Compute state didn't do anything");
		}

		createGraphFile();
		return nextState;

	}

	public MachineState computeState() {
		// TODO: better name

		// Loop through each base prop and calculate a new value for it given
		// its inputs
		for (Proposition prop : propNet.getBasePropositions().values()) {
			if (propSet(prop.getSingleInput().getSingleInput()) != prop.getValue()) {
				LOGGER.fine("Prop val changed!");
			}
			prop.setValue(propSet(prop.getSingleInput().getSingleInput()));
		}

		// Creates a MachineState from the BasePropositons
		return getStateFromBase();
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
	public List<Proposition> getOrdering() {
		// List to contain the topological ordering.
		List<Proposition> order = new LinkedList<Proposition>();

		// All of the components in the PropNet
		List<Component> components = new ArrayList<Component>(propNet.getComponents());

		// All of the propositions in the PropNet.
		List<Proposition> propositions = new ArrayList<Proposition>(propNet.getPropositions());

		// TODO: Compute the topological ordering.

		return order;
	}

	/* Already implemented for you */
	@Override
	public List<Role> getRoles() {
		return roles;
	}

	/* Start Helper methods */ // ------------------------------------------------------------------------------------------

	/*
	 * markBases sets all the base propositions to match those in the given
	 * state
	 */
	private void markBases(MachineState state) {

		for (GdlSentence sent : propNet.getBasePropositions().keySet()) {
			// Get out previous proposition
			Proposition prop = propNet.getBasePropositions().get(sent);
			boolean prevVal = prop.getValue();

			// Set prop true for values in the state, false for everything else
			prop.setValue(state.getContents().contains(sent));

			if(prevVal != prop.getValue()) {
				LOGGER.info("BASE VAL CHANGED");
			}

			// Place modified proposition back in
			//propNet.getBasePropositions().put(sent, prop);
		}
	}

	/*
	 * The markInputs functions goes through the given boolean of input props
	 * and marks them on the provided set in propNet
	 */
	private void markInputs(List<Move> moveSet) {
		for (GdlSentence sent : propNet.getInputPropositions().keySet()) {
			// Get out previous proposition
			Proposition prop = propNet.getInputPropositions().get(sent);
			boolean prevVal = prop.getValue();

			// Set prop true for values in the set of moves, false for
			// everything else
			// toDoes maps a list of moves to a list of GdlSentences
			prop.setValue(toDoes(moveSet).contains(sent));
			LOGGER.info("" + toDoes(moveSet).contains(sent));

			if(prevVal != prop.getValue()) {
				LOGGER.info("INPUT VAL CHANGED");
			}

			// Place modified proposition back in
			//propNet.getInputPropositions().put(sent, prop);
		}
	}

	/*
	 * clears the propNet bases
	 */
	private void clearPropNet() {
		for (GdlSentence sent : propNet.getInputPropositions().keySet()) {
			// Get out previous proposition
			Proposition prop = propNet.getInputPropositions().get(sent);

			// Set everything to false
			prop.setValue(false);

			// Place modified proposition back in, now false
			//propNet.getInputPropositions().put(sent, prop);
		}
	}

	/**
	 * The propSet function computes the value of view Propositions
	 *
	 * @param p
	 */
	private boolean propSet(Component p) {
		LOGGER.fine("propMark Type of Proposition: " + p.getClass() + "and value "+ p.getValue());

		// Base and input values are standalone and hold the values of states
		// and sets of moves respectively
		// Transitions always lead to base propositions, but this function will
		// never get called on Transitions
		if (isBase(p)){
			LOGGER.fine("BASE FOUND");
			return p.getValue();
		} else if (isInput(p)){
			LOGGER.fine("INPUT FOUND");
			return p.getValue();
		} else if (p instanceof Not){
			LOGGER.fine("NOT FOUND");
			return propMarkNegation(p);
		} else if (p instanceof And) {
			LOGGER.fine("AND FOUND");
			return propMarkConjunction(p);
		} else if (p instanceof Or){
			LOGGER.fine("OR FOUND");
			return propMarkDisjunction(p);
		} else {
			// Must be view as that is the only category left
			LOGGER.fine("SHOULD BE VIEW");
			return propSet(p.getSingleInput());
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
		return !propSet(p.getSingleInput());
	}

	private boolean propMarkConjunction(Component p) {
		Set<Component> sources = p.getInputs();
		for (Component component : sources) {
			if (!propSet(component)){
				return false;
			}
		}
		return true;
	}

	private boolean propMarkDisjunction(Component p) {
		Set<Component> sources = p.getInputs();
		for (Component component : sources) {
			if (!propSet(component)) {
				return true;
			}
		}
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
		List<GdlSentence> doeses = new ArrayList<GdlSentence>(moves.size());
		Map<Role, Integer> roleIndices = getRoleIndices();

		for (int i = 0; i < roles.size(); i++) {
			int index = roleIndices.get(roles.get(i));
			doeses.add(ProverQueryBuilder.toDoes(roles.get(i), moves.get(index)));
		}
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
		Set<GdlSentence> contents = new HashSet<GdlSentence>();
		for (Proposition p : propNet.getBasePropositions().values()) {
			p.setValue(p.getSingleInput().getValue());
			if (p.getValue()) {
				contents.add(p.getName());
			}

		}
		return new MachineState(contents);
	}
}
