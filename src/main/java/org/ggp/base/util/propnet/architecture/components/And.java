package org.ggp.base.util.propnet.architecture.components;

import org.ggp.base.util.propnet.architecture.Component;

/**
 * The And class is designed to represent logical AND gates.
 */
@SuppressWarnings("serial")
public final class And extends Component
{
	private boolean value;

	/**
	 * Returns true if and only if every input to the and is true.
	 *
	 *
	 */
	public boolean calcValue()
	{
		for ( Component component : getInputs() )
		{
			if ( !component.getValue() )
			{
				return false;
			}
		}
		return true;
	}

	@Override
	/**
	 * Returns the set value of the And
	 *
	 * @see org.ggp.base.util.propnet.architecture.Component#getValue()
	 */
	public boolean getValue(){
		return this.value;
	}



	/**
	 * @see org.ggp.base.util.propnet.architecture.Component#toString()
	 */
	@Override
	public String toString()
	{
		return toDot("invhouse", "grey", "AND");
	}

	@Override
	public void setValue() {
		value = calcValue();

	}

}
