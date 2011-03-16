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

import java.util.Collections;
import java.util.List;

/**
 * @author Fabien Michel
 * @since MadKit 2.1
 * @version 5.0
 * @param <A> The agent most generic type for this Overlooker
 * 
 */
abstract class Overlooker <A extends AbstractAgent>
{ 
	private Role overlookedRole;
	final private String community;
	final private String group;
	final private String role;

	Overlooker(final String communityName, final String groupName, final String roleName)
	{
		community=communityName;
		group=groupName;
		role=roleName;
	}

	synchronized final void setOverlookedRole(final Role theRole)
	{
		overlookedRole = theRole;
		if(theRole != null)
			try {
				initialize();
			} catch (Exception e) {
				System.err.println("\n-----MADKIT WARNING : problem during initialize()-----\n-----Problem on "+this.getClass().getSimpleName()+" on <"+community+";"+group+";"+role+">-----\n-----Method call is at:");
				e.printStackTrace(); //TODO find another way
			}
	}

	/**
	 * @return the overlookedRole
	 */
	final Role getOverlookedRole() {
		return overlookedRole;
	}


	public void initialize(){
		for(A agent : getCurrentAgentsList()){
			adding(agent);
		}
	}

	/**
	 * Called when an agent has joined the corresponding group and role.
	 * @param theAgent which has been added to this group/role
	 */
	public void adding(final A theAgent){}

	/**
	 * Called when an agent has leaved the corresponding group and role.
	 * @param theAgent which has been removed from this group/role
	 */
	public void removing(final A theAgent){}

	/** @return a ListIterator which has been previously shuffled
@since MadKit 3.0*/
	final public List<A> getShuffledList()
	{
		try {
			List<A> l = getCurrentAgentsList();
			Collections.shuffle(l);
			return l;
		} catch (NullPointerException e) {
			//		nullRoleErrorMessage(e,"getShuffledList"); //TODO warning if empty ?
		}
		return Collections.emptyList();
	}

	/** @return a list view (a snapshot at moment t) of the agents that handle the group/role couple (in proper sequence)
@since MadKit 3.0
	 */
	@SuppressWarnings("unchecked")
	final public List<A> getCurrentAgentsList()//TODO log if not already added !
	{
		if (overlookedRole != null) {
			return (List<A>) overlookedRole.getAgentsList();
		}
		else{
			return Collections.emptyList();
		}
	}

	final private void nullRoleErrorMessage(final NullPointerException e,final String using) {
		System.err.println("\n-----WARNING : probes and activators should not be used before being added-----\n-----Problem on "+this.getClass().getSimpleName()+" on <"+community+";"+group+";"+role+"> using "+using+"-----\n-----Method call is at:");
		e.printStackTrace();
	}

	/** 
	 * @return the number of the agents that handle the group/role couple
	 */
	final public int size() {
			return getCurrentAgentsList().size();
	}

//	@SuppressWarnings("unchecked")
//	final public A getAgentNb(final int nb)
//	{
//		final List<A> l = getCurrentAgentsList();
//		return l.get(nb);
//	}

	final public String getCommunity()  {	return community;   }
	final public String getGroup()  {	return group;   }
	final public String getRole()   {	return role;    }

	@Override
	public String toString()
	{
		return getClass().getSimpleName()+" on <"+community+";"+group+";"+role+"> ("+size()+" agents)";
	}
	
	@SuppressWarnings("unchecked")
	final void update(final AbstractAgent agent, final boolean added){
		if(added)
			adding((A) agent);//TODO log classcastex
		else
			removing((A) agent);
	}
}