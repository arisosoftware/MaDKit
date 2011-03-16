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
package madkit.api.abstractAgent;

import static madkit.kernel.AbstractAgent.ReturnCode.ACCESS_DENIED;
import static madkit.kernel.AbstractAgent.ReturnCode.AGENT_CRASH;
import static madkit.kernel.AbstractAgent.ReturnCode.NOT_COMMUNITY;
import static madkit.kernel.AbstractAgent.ReturnCode.NOT_GROUP;
import static madkit.kernel.AbstractAgent.ReturnCode.NULL_STRING;
import static madkit.kernel.AbstractAgent.ReturnCode.ROLE_ALREADY_HANDLED;
import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static madkit.kernel.Madkit.Roles.GROUP_MANAGER_ROLE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import madkit.kernel.AbstractAgent;
import madkit.kernel.GroupIdentifier;
import madkit.kernel.JunitMadKit;
import madkit.kernel.Madkit;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.7
 * @version 0.9
 * 
 */
@SuppressWarnings("serial")
public class RequestRoleTest  extends JunitMadKit{

	final AbstractAgent helper = new AbstractAgent(){
		protected void activate() {
			assertEquals(ROLE_ALREADY_HANDLED, requestRole(COMMUNITY,GROUP,GROUP_MANAGER_ROLE));
			assertEquals(SUCCESS, requestRole(COMMUNITY,GROUP,ROLE));
		}
	};

	final AbstractAgent helper2 = new AbstractAgent(){
		protected void activate() {
			assertEquals(ROLE_ALREADY_HANDLED, requestRole(COMMUNITY,GROUP,GROUP_MANAGER_ROLE));
			assertEquals(SUCCESS, requestRole(COMMUNITY,GROUP,ROLE));
		}
	};

	final GroupIdentifier alwaysDeny = new GroupIdentifier() {
		@Override
		public boolean allowAgentToTakeRole(String roleName, Object memberCard) {
			return false;
		}
	};
	
	final GroupIdentifier alwaysAccept = new GroupIdentifier() {
		@Override
		public boolean allowAgentToTakeRole(String roleName, Object memberCard) {
			return true;
		}
	};
	
	final GroupIdentifier buggyIdentifier = new GroupIdentifier() {
		@SuppressWarnings("null")
		@Override
		public boolean allowAgentToTakeRole(String roleName, Object memberCard) {
			Object o = null;
			o.toString();
			return true;
		}
	};
	
	@Test
	public void returnSuccess(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertEquals(SUCCESS, createGroup(COMMUNITY,GROUP));
				assertEquals(SUCCESS, requestRole(COMMUNITY,GROUP,ROLE));
				assertTrue(isRole(COMMUNITY,GROUP,GROUP_MANAGER_ROLE));
				assertTrue(isRole(COMMUNITY,GROUP,ROLE));
			}});
	}

	@Test
	public void returnNotCgr(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertEquals(SUCCESS, createGroup(COMMUNITY,GROUP));
				assertEquals(NOT_COMMUNITY, requestRole(aa(),GROUP,ROLE));
				assertEquals(NOT_GROUP, requestRole(COMMUNITY,aa(),ROLE));
			}});
	}

	@Test
	public void returnAlreadyHandled(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertEquals(SUCCESS, createGroup(COMMUNITY,GROUP));
				assertEquals(ROLE_ALREADY_HANDLED, requestRole(COMMUNITY,GROUP,GROUP_MANAGER_ROLE));
				assertEquals(SUCCESS, requestRole(COMMUNITY,GROUP,ROLE));
				assertEquals(ROLE_ALREADY_HANDLED, requestRole(COMMUNITY,GROUP,ROLE));
			}});
	}

	@Test
	public void returnAccessDenied(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertEquals(SUCCESS, createGroup(COMMUNITY,GROUP,false,alwaysDeny));
				assertEquals(ACCESS_DENIED, requestRole(COMMUNITY,GROUP,ROLE));
				assertEquals(ACCESS_DENIED, requestRole(COMMUNITY,GROUP,ROLE,null));
			}});
	}

	@Test
	public void returnAccessGranted(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertEquals(SUCCESS, createGroup(COMMUNITY,GROUP,false,alwaysAccept));
				assertEquals(SUCCESS, requestRole(COMMUNITY,GROUP,ROLE));
				assertEquals(SUCCESS, requestRole(COMMUNITY,GROUP,aa(),null));
			}});
	}

	@Test
	public void buggyIdentifier(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertEquals(SUCCESS, createGroup(COMMUNITY,GROUP,false,buggyIdentifier));//TODO think about that
				assertEquals(SUCCESS, requestRole(COMMUNITY,GROUP,aa(),null));
			}},AGENT_CRASH);
	}

	@Test
	public void returnNullRole(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertEquals(SUCCESS, createGroup(COMMUNITY,GROUP,false,alwaysDeny));
				assertEquals(NULL_STRING, requestRole(COMMUNITY,GROUP,null));
			}});
	}

	@Test
	public void defaultRole(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertEquals(SUCCESS, createGroup(COMMUNITY,GROUP));
				assertTrue(isGroup(COMMUNITY,GROUP));
				assertTrue(isRole(COMMUNITY,GROUP,Madkit.Roles.GROUP_MANAGER_ROLE));
				assertEquals(SUCCESS, leaveGroup(COMMUNITY,GROUP));
				assertFalse(isGroup(COMMUNITY,GROUP));
			}});
	}

	@Test
	public void nullArgs(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertFalse(isCommunity(null));
				assertFalse(isGroup(null,null));
				assertEquals(NOT_COMMUNITY, requestRole(null,null,null));
				assertEquals(NOT_COMMUNITY, requestRole(null,null,null,null));

				assertEquals(SUCCESS, createGroup(COMMUNITY,GROUP));
				assertEquals(NOT_GROUP, requestRole(COMMUNITY,null,null));
				assertEquals(NULL_STRING, requestRole(COMMUNITY,GROUP,null,null));
				assertEquals(NOT_COMMUNITY, requestRole(null,GROUP,null));
				assertEquals(NOT_COMMUNITY, requestRole(null,GROUP,ROLE));
				assertEquals(NOT_COMMUNITY, requestRole(null,null,ROLE));
				assertEquals(NULL_STRING, requestRole(COMMUNITY,GROUP,null,new Object()));
			}
		});
	}

	@Test
	public void onlyOneManager(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertEquals(SUCCESS, createGroup(COMMUNITY,GROUP));
				assertEquals(ROLE_ALREADY_HANDLED, requestRole(COMMUNITY,GROUP,GROUP_MANAGER_ROLE));
				assertEquals(SUCCESS, requestRole(COMMUNITY,GROUP,ROLE));
				assertEquals(SUCCESS, launchAgent(helper));
				assertEquals(SUCCESS, launchAgent(helper2));
			}
		});
	}

	@Test
	public void leaveGroupByLeavingRoles(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertFalse(isCommunity(COMMUNITY));
				assertFalse(isGroup(COMMUNITY,GROUP));
				assertEquals(SUCCESS, createGroup(COMMUNITY,GROUP));
				assertEquals(SUCCESS, requestRole(COMMUNITY,GROUP,ROLE));
			}
		});
	}


}