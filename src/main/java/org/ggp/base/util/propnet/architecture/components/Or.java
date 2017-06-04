package org.ggp.base.util.propnet.architecture.components;

import org.ggp.base.util.propnet.architecture.Component;

/**
 * The Or class is designed to represent logical OR gates.
 */
@SuppressWarnings("serial")
public final class Or extends Component
{

	private boolean value;

	/**
	 * Returns true if and only if at least one of the inputs to the or is true.
	 *
	 * @see org.ggp.base.util.propnet.architecture.Component#getValue()
	 */
	public boolean calcValue()
	{
		for ( Component component : getInputs() )
		{
			if ( component.getValue() )
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * @see org.ggp.base.util.propnet.architecture.Component#toString()
	 */
	@Override
	public String toString()
	{
		return toDot("ellipse", "grey", "OR");
	}

	@Override
	public boolean getValue() {
		return this.value;
	}

	@Override
	public void setValue() {
		this.value = calcValue();
	}
}