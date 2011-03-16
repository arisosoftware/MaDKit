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

/**
 * 
 */
package madkit.kernel;

import java.net.DatagramPacket;
import java.net.Socket;

import madkit.messages.ObjectMessage;

/**
 * @author Fabien Michel
 * @since MadKit 5
 * @version 0.9
 * @param <T> a code or a message to convey
 *
 */
class NetworkMessage<T> extends ObjectMessage<T> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3146557063162460766L;
	final static int TO_SEND = 1;
	final static int TO_INJECT = 2;
	final static int STOP_NETWORK = 3;
	final static int NEW_PEER_DETECTED = 4;
	static final int NEW_PEER_REQUEST = 5;
	
	private int code = TO_SEND;

	/**
	 * 
	 */
	public NetworkMessage(T content) {
		super(content);
	}

	/**
	 * @param code the code to set
	 */
	public void setCode(int code) {
		this.code = code;
	}

	/**
	 * @return the code
	 */
	public int getCode() {
		return code;
	}
}

class MessageConveyor extends NetworkMessage<Message> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6494081559780926565L;

	/**
	 * @param content
	 */
	MessageConveyor(Message content) {
		super(content);
	}
	
	/* (non-Javadoc)
	 * @see madkit.messages.ObjectMessage#toString()
	 */
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return getContent().getID()+"-"+super.toString();
	}
	
}

class NewPeerMessage extends NetworkMessage<DatagramPacket>{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1804809279823744173L;

	/**
	 * @param content
	 */
	public NewPeerMessage(DatagramPacket content) {
		super(content);
		setCode(NEW_PEER_DETECTED);
	}
	
}

class NewPeerConnectionRequest extends NetworkMessage<Socket>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6092436677566809561L;

	/**
	 * @param content
	 */
	public NewPeerConnectionRequest(Socket content) {
		super(content);
		setCode(NEW_PEER_REQUEST);
	}
	
}