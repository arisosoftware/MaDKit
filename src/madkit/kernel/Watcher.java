/*
 * Copyright 1997-2011 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MadKit.
 * 
 * MadKit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MadKit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MadKit. If not, see <http://www.gnu.org/licenses/>.
 */

package madkit.kernel;

import java.util.LinkedHashSet;
import java.util.Set;

/** 
 * This class defines a generic watcher agent. It holds a collection of probes to explore agents' internal properties.
 * 
 * @author Fabien Michel since V.2
 * @author Olivier Gutknecht 
 * @since MadKit 2.0
 * @version 5.0
*/
public class Watcher extends AbstractAgent
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4999802785768614838L;
	final private Set<Probe<? extends AbstractAgent>> probes = new LinkedHashSet<Probe<? extends AbstractAgent>> ();

	public void addProbe(final Probe<? extends AbstractAgent> probe)
	{
		if(kernel.addOverlooker(this, probe))
			probes.add(probe);
		if(logger != null)
			logger.fine("Probe added: "+probe);
	}
	
	public void removeProbe(final Probe<? extends AbstractAgent> probe)
	{
		kernel.removeOverlooker(this, probe);
		probes.remove(probe);
	}
	
	/**
	 * @see madkit.kernel.AbstractAgent#terminate()
	 */
	@Override
	final void terminate() {
		removeAllProbes();
		super.terminate();
	}

	/**
	 * 
	 */
	public void removeAllProbes() {
		for(final Probe<? extends AbstractAgent> p : probes ){
			kernel.removeOverlooker(this,p);
		}
		probes.clear();
	}

}