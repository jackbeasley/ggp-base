package org.ggp.base.util.statemachine;

import java.util.HashSet;
import java.util.Set;

import org.ggp.base.util.gdl.grammar.GdlSentence;

public class MachineState {
    public MachineState() {
        this.contents = null;
        this.visits = 0;
        this.utility = 0;
    }

    /**
     * Starts with a simple implementation of a MachineState. StateMachines that
     * want to do more advanced things can subclass this implementation, but for
     * many cases this will do exactly what we want.
     */
    private int visits;
    private int utility;
    private final Set<GdlSentence> contents;

    public MachineState(Set<GdlSentence> contents)
    {
        this.contents = contents;
    }

    /**
     * getVisits returns the number of times the MachineState has been visited
     */
    public int getVisits ()
    {
    	return visits;
    }

    /**
     * setVisits sets the number of visits to an integer passed in to the function
     */
    public void setVisits (int visits)
    {
    	this.visits = visits;
    }

    /**
     * getUtility returns the number of points (utility)
     */
    public int getUtility ()
    {
    	return utility;
    }

    /**
     * setUtility sets the points for any given move to an integer passed in to the function
     */
    public void setUtility (int utility)
    {
    	this.utility = utility;
    }

    /**
     * getContents returns the GDL sentences which determine the current state
     * of the game being played. Two given states with identical GDL sentences
     * should be identical states of the game.
     */
    public Set<GdlSentence> getContents()
    {
        return contents;
    }

    @Override
    public MachineState clone() {
        return new MachineState(new HashSet<GdlSentence>(contents));
    }

    /* Utility methods */
    @Override
    public int hashCode()
    {
        return getContents().hashCode();
    }

    @Override
    public String toString()
    {
        Set<GdlSentence> contents = getContents();
        if(contents == null)
            return "(MachineState with null contents)";
        else
            return contents.toString();
    }

    @Override
    public boolean equals(Object o)
    {
        if ((o != null) && (o instanceof MachineState))
        {
            MachineState state = (MachineState) o;
            return state.getContents().equals(getContents());
        }

        return false;
    }
}