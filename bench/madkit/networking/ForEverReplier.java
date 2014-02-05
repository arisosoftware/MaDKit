package madkit.networking;

import static madkit.kernel.JunitMadkit.COMMUNITY;
import static madkit.kernel.JunitMadkit.GROUP;
import static madkit.kernel.JunitMadkit.ROLE;
import madkit.kernel.Agent;
import madkit.kernel.AgentAddress;
import madkit.kernel.Message;


public class ForEverReplier extends Agent {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;

	@Override
	protected void activate() {
		createGroup(COMMUNITY, GROUP,true);
		requestRole(COMMUNITY, GROUP, ROLE);
		sendMessage(COMMUNITY,GROUP,ROLE,new Message());
//		setLogLevel(Level.ALL);
	}
	
	@Override
	protected void live() {
		AgentAddress aa = waitNextMessage().getSender();
		sendMessage(aa, new Message());
		while (true) {
			waitNextMessage();
			sendMessage(aa, new Message());
		}
	}
}