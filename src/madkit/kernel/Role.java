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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import madkit.kernel.AbstractAgent.ReturnCode;

import static madkit.kernel.Utils.*;
import static madkit.kernel.AbstractAgent.ReturnCode.*;

/**
/** Reifying the notion of Role in AGR
 * 
 * @author Fabien Michel
 * @since MadKit 3.0
 * @version 5.0
 * 
 */
class Role implements Serializable{//TODO test with arraylist

	private static final long serialVersionUID = 4447153943733812916L;
	//	private transient HashSet<Overlooker<? extends AbstractAgent>> overlookers;
	//	private transient List<AbstractAgent> referenceableAgents; //TODO test copyonarraylist and linkedhashset
	final protected transient ArrayList<AbstractAgent> players;
	private transient ArrayList<AbstractAgent> tmpReferenceableAgents;
	private transient ArrayList<AgentAddress> agentAddresses;

	private transient boolean modified=true;
	final transient private Logger logger;

	final private transient Set<Overlooker<? extends AbstractAgent>> overlookers;
	protected final transient Group myGroup;

	private final String communityName;
	private final String groupName;
	private final String roleName;
	private final KernelAddress kernelAddress;


	/**
	 * @return the kernelAddress
	 */
	KernelAddress getKernelAddress() {
		return kernelAddress;
	}


	Role(final Group groupObject,final String roleName){
		players = new ArrayList<AbstractAgent>();
		communityName = groupObject.getMyCommunity().getName();
		groupName = groupObject.getName();
		this.roleName = roleName;
		final MadkitKernel k = groupObject.getMyCommunity().getMyKernel();
		logger = groupObject.getMyCommunity().getLogger();
		myGroup = groupObject;
		kernelAddress = k.getKernelAddress();
		if(logger != null){
			logger.finer(toString()+" created");
		}
		overlookers = new LinkedHashSet<Overlooker<? extends AbstractAgent>>();
		for(final Overlooker<? extends AbstractAgent> o : k.getOperatingOverlookers()){
			if(o.getCommunity().equals(communityName) && o.getGroup().equals(groupName) && o.getRole().equals(roleName))
				overlookers.add(o);
		}
		initializeOverlookers();
	}

	private synchronized void initializeOverlookers() {//TODO init process
		for (final Overlooker<? extends AbstractAgent> o : overlookers) {
			o.setOverlookedRole(this);
		}
	}


	/**
	 * this is dirty but... This represents a Group
	 * @param community
	 * @param group
	 */
	Role(final String community, final String group){
		communityName = community;
		groupName = group;
		roleName = null;
		players = null;
		overlookers = null;
		myGroup = null;
		logger = null;
		kernelAddress = null;
	}


	/**
	 * @return the players
	 */
	ArrayList<AbstractAgent> getPlayers() {
		return players;
	}


	/**
	 * @return the myGroup
	 */
	Group getMyGroup() {
		return myGroup;
	}


	/**
	 * @return the communityName
	 */
	final String getCommunityName() {
		return communityName;
	}

	/**
	 * @return the groupName
	 */
	final String getGroupName() {
		return groupName;
	}

	final void addOverlooker(final Overlooker<? extends AbstractAgent> o)
	{
		overlookers.add(o);
		o.setOverlookedRole(this);
	}

	final void removeOverlooker(final Overlooker<? extends AbstractAgent> o)
	{
		o.setOverlookedRole(null);
	}
	/**
	 * @return the roleName
	 */
	final String getRoleName() {
		return roleName;
	}

	@Override
	public String toString() {
		return printCGR(communityName, groupName, roleName);
	}

	/**
	 * add the agent to the role
	 * @param roleName 
	 * @param groupName 
	 * @param communityName 
	 * 
	 * @param agent the agent
	 * @return 
	 * 
	 * @return true, if the agent has been added.
	 */
	boolean addMember(final AbstractAgent requester) {
		synchronized (players) {
			if (players.contains(requester)) {
				return false;
			}
			players.add(requester);
			if (logger != null) {
				logger.finest(requester.getName() + " is now playing " + printCGR(communityName, groupName, roleName));
			}
			if (agentAddresses != null) {
				agentAddresses.add(new AgentAddress(requester, this, kernelAddress));
			}
			modified = true;
		}
		updateOverlookers(requester,true);
		return true;
		//		requester.setRoleObject(this);
		//		referenceableAgents.add(requester.getAgent());
	}

	final void addMembers(final ArrayList<AbstractAgent> bucket, final boolean roleJustCreated){
		synchronized (players) {
			players.addAll(bucket);
			if (agentAddresses != null) {
				final ArrayList<AgentAddress> addresses = new ArrayList<AgentAddress>(bucket.size());
				for (final AbstractAgent a : bucket) {
					addresses.add(new AgentAddress(a, this, kernelAddress));
				}
				addresses.addAll(agentAddresses);//TODO test vs assignment
				agentAddresses = addresses;
			}
			modified = true;
		}
		if (roleJustCreated) {
			initializeOverlookers();
		}
		else{
			updateOverlookers(bucket,true);
		}
	}

	/**
	 * @param content
	 */
	final void addDistantMember(final AgentAddress content) {
		buildAgentAddressesList();
		if(! agentAddresses.contains(content)){
			content.setRoleObject(this);
			agentAddresses.add(content);
		}
	}

	private void buildAgentAddressesList(){
		if(agentAddresses == null){
			agentAddresses = new ArrayList<AgentAddress>(players.size());
			synchronized (players) {
				for (final AbstractAgent a : players) {
					agentAddresses.add(new AgentAddress(a, this, kernelAddress));
				}
			}
		}
	}


	ReturnCode removeMember(final AbstractAgent requester){
		synchronized (players) {
			if (!players.remove(requester)) {
				return ROLE_NOT_HANDLED;
			}
			if (agentAddresses != null) {
				removeAgentAddressOf(requester, agentAddresses);
			}
			//		referenceableAgents.remove(requester.getAgent());
			if (logger != null) {
				logger.finest(requester.getName() + " has leaved role " + printCGR(communityName, groupName, roleName) + "\n");
			}
			modified = true;
		}
		updateOverlookers(requester,false);
		if(empty()){
			deleteMySelfFromOrg(requester);
		}
		return SUCCESS;
	}


	/**
	 * @param kernelAddress2
	 */
	void removeAgentsFromDistantKernel(final KernelAddress kernelAddress2) {
		if(logger != null)
			logger.finest("Removing all agents from distant kernel "+kernelAddress2+" in"+this);
		if (agentAddresses != null)
			synchronized (players) {
				for (Iterator<AgentAddress> iterator = getAgentAddresses().iterator(); iterator.hasNext();) {//TODO return if no agent addresses
					if (iterator.next().getKernelAddress().equals(kernelAddress2))
						iterator.remove();
				}
			}
		if(empty()){
			deleteMySelfFromOrg(null);
		}
	}


	/**
	 * @param requester the agent by which I am now empty
	 * 
	 */
	private void deleteMySelfFromOrg(AbstractAgent requester) {
		for (final Overlooker<? extends AbstractAgent> o : overlookers) {
			o.setOverlookedRole(null);
		}
		myGroup.removeRole(roleName);
	}


	/**
	 * @param content
	 */
	void removeDistantMember(final AgentAddress content) {
		if (agentAddresses != null) {
			synchronized (players) {
				if (agentAddresses.remove(content)) {
					if (logger != null) {
						logger.finest(content + " has leaved role " + printCGR(communityName, groupName, roleName) + "\n");
					}
				}
			}
		}
		if(empty()){
			deleteMySelfFromOrg(null);
		}
	}


	/**
	 * @param requester
	 */
	static AgentAddress removeAgentAddressOf(final AbstractAgent requester,final List<AgentAddress> list) {
		for (final Iterator<AgentAddress> iterator = list.iterator();iterator.hasNext();){
			try {
				final AgentAddress aa = iterator.next();
				if (aa.getAgent() == requester) {//TODO test speed with hashcode test
					iterator.remove();
					return aa;		//optimize test by setting roleObject to null
				}
			} catch (NullPointerException e) {
				//TODO this should not happen : some AA are null !!!!!
			}
		}
		return null;
	}

	AgentAddress getAgentAddressOf(final AbstractAgent a){
		synchronized (players) {
			buildAgentAddressesList();
			for (final AgentAddress aa : agentAddresses) {//TODO when offline second part is useless
				if (aa.hashCode() == a.hashCode() && a.getKernel().getKernelAddress(a).equals(aa.getKernelAddress()))
					return aa;
			}
		}
		return null;
	}

	boolean empty() {
		//players == null is not possible TODO
		return ( (players == null || players.isEmpty()) && (agentAddresses == null || agentAddresses.isEmpty()) );//simply not possible if not following remove A
	}


	/**
	 * @return all the agent addresses: This list is never null because an empty role does not exist
	 */
	List<AgentAddress> getAgentAddresses() {//Lazy creation //TODO this has to be done more wisely
		//		if(empty())//TODO this is not possible
		//			return null;
		buildAgentAddressesList();
		return agentAddresses;
	}

	/**
	 * @param abstractAgent
	 * @return the AA of the abstractAgent in this Role
	 */
	final AgentAddress getAgentAddressInGroup(final AbstractAgent abstractAgent) {
		final AgentAddress aa = getAgentAddressOf(abstractAgent);
		if(aa != null)
			return aa;
		return myGroup.getAgentAddressOf(abstractAgent);
	}

	/**
	 * @param receiver
	 * @return
	 */
	boolean containsAddress(final AgentAddress receiver) {
		if(getAgentAddresses().contains(receiver))
			return true;
		else{
			final Collection<Role> roles = new ArrayList<Role>(myGroup.values());
			roles.remove(this);
			for(final Role r : roles){
				if(r.getAgentAddresses().contains(receiver))
					return true;
			}
		}
		return false;
	}

	final boolean isPlayingRole(final AgentAddress agent){
		return getAgentAddresses().contains(agent);
	}

	final List<AbstractAgent> getAgentsList()
	{
		if(modified){
			synchronized (players) {
				modified = false;//TODO do a bench : new seems a little bit better
				//long startTime = System.nanoTime();
				tmpReferenceableAgents = new ArrayList<AbstractAgent>(players);
				//tmpReferenceableAgents = (ArrayList<AbstractAgent>)referenceableAgents.clone();
				//long estimatedTime = System.nanoTime() - startTime;	   System.err.println(estimatedTime);
			}
		}
		return tmpReferenceableAgents;
	}


	final private void updateOverlookers(final AbstractAgent theReference,final boolean added) {
		for (final Overlooker<? extends AbstractAgent> o : overlookers){
			o.update(theReference,added);// TODO choose solution on updateAgent
		}
	}

	/**
	 * @param bucket
	 */
	final private void updateOverlookers(final ArrayList<AbstractAgent> bucket,final boolean added) {
		for (final AbstractAgent abstractAgent : bucket) {
			updateOverlookers(abstractAgent, added);
		}
	}


	/**
	 * importation when connecting to other kernel
	 * @param list
	 */
	void importDistantOrg(List<AgentAddress> list) {
		buildAgentAddressesList();
		synchronized (players) {
			for (final AgentAddress aa : list) {
				if (! agentAddresses.contains(aa)) {
					aa.setRoleObject(this);
					agentAddresses.add(aa); //TODO overlookers in distributed mode
				}
			}
		}


	}

	/**
	 * @param aa
	 * @return the AbstractAgent corresponding to the aa agentAddress in this role, null if it does no longer play this role
	 */
	AbstractAgent getAbstractAgentWithAddress(AgentAddress aa) {
		synchronized (players) {
			for (final AbstractAgent agent : players) {
				if (agent.hashCode() == aa.hashCode())
					return agent;
			}
		}
		return null;
	}


}