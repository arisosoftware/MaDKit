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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;

/**
 * @author Fabien Michel
 * @version 0.9
 * @since MadKit 5.0
 *
 */
class KernelConnection extends Thread{

	Socket distantKernelSocket = null;
	boolean activated = false;
	/**
	 * @return the activated
	 */
	boolean isActivated() {
		return activated;
	}

	/**
	 * @param activated the activated to set
	 */
	void setActivated(boolean activated) {
		this.activated = activated;
	}

	/**
	 * @return the distantKernelSocket
	 */
	Socket getDistantKernelSocket() {
		return distantKernelSocket;
	}

	/**
	 * @param address
	 * @param port
	 */
	private NetworkAgent myNetAgent;
	private KernelAddress kernelAddress;
	/**
	 * @param kernelAddress the kernelAddress to set
	 */
	void setKernelAddress(KernelAddress kernelAddress) {
		this.kernelAddress = kernelAddress;
	}

	private ObjectOutputStream oos;
	private ObjectInputStream ois;

	/**
	 * @return the kernelAddress
	 */
	KernelAddress getKernelAddress() {
		return kernelAddress;
	}

	public KernelConnection(NetworkAgent netAgent, InetAddress address, int port) throws UnknownHostException,IOException {
		myNetAgent = netAgent;
		distantKernelSocket = new Socket(address, port);
		initStreams();

	}

	public KernelConnection(NetworkAgent netAgent, Socket kernelClient) throws IOException{
		myNetAgent = netAgent;
		distantKernelSocket = kernelClient;
		initStreams();
	}
	
	/**
	 * @throws IOException 
	 * 
	 */
	private void initStreams() throws IOException {
			oos = new ObjectOutputStream(distantKernelSocket.getOutputStream());
			ois = new ObjectInputStream(distantKernelSocket.getInputStream());
	}

	/**
	 * @param netAgent
	 * @throws ClassNotFoundException 
	 * @throws IOException 
	 */
	@SuppressWarnings("unchecked")
	HashMap<String,HashMap<String,HashMap<String,List<AgentAddress>>>> waitForDistantOrg() throws IOException, ClassNotFoundException {
			return (HashMap<String,HashMap<String,HashMap<String,List<AgentAddress>>>>) ois.readObject();
	}
	
	KernelAddress waitForDistantKernelAddress() throws IOException, ClassNotFoundException{
		return (KernelAddress) ois.readObject();
	}


	/**
	 * @param localOrg
	 * @throws IOException 
	 */
	void sendConnectionInfo(KernelAddress myKA, HashMap<String, HashMap<String, HashMap<String, List<AgentAddress>>>> localOrg) throws IOException {
			oos.writeObject(myKA);
			oos.writeObject(localOrg);
	}



	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		activated = true;
		while(distantKernelSocket.isConnected()){
			try {
				NetworkMessage<?> m = (NetworkMessage<?>) ois.readObject();
				myNetAgent.receiveMessage(m);
//				myNetAgent.receiveMessage((NetworkMessage) ois.readObject());
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				closeConnection();
				myNetAgent.deconnectWith(kernelAddress);
				return;
			}		
		}
	}

	/**
	 * @param m
	 */
	void sendMessage(Message m) {
		try {
			oos.writeObject(m);
		} catch (IOException e) {
			e.printStackTrace();//log this
		}
		
	}

	/**
	 * close the connection by closing the socket and the writing streams
	 */
	synchronized void closeConnection() {
		try {
			oos.close();
			ois.close();
			distantKernelSocket.close();
		} catch (IOException e) {//log this
			e.printStackTrace();
		}
		
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Thread#toString()
	 */
	@Override
	public String toString() {
		return distantKernelSocket.getInetAddress().getHostAddress()+kernelAddress.toString();
	}

	public int getPort(){
		return distantKernelSocket.getPort();
	}
	
	public InetAddress getInetAddress(){
		return distantKernelSocket.getInetAddress();
	}

}