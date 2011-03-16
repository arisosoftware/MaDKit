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

import java.util.concurrent.ThreadFactory;

/**
 * @author Fabien Michel
 * @version 0.9
 * @since MadKit 5.0
 *
 */
final class AgentThreadFactory extends Object implements ThreadFactory {

	final private static int MKRA_PRIORITY = Thread.NORM_PRIORITY-1;
	final private static int MKDA_PRIORITY = Thread.NORM_PRIORITY+1;
	final private boolean daemonThreads;
    final private ThreadGroup group;
//	static private int nbthread=0;
    
    AgentThreadFactory(final String name, final boolean isDaemonThreadFactory) {
    	daemonThreads = isDaemonThreadFactory;
    	group = new ThreadGroup(name);
    	if (isDaemonThreadFactory) {
			group.setMaxPriority(MKDA_PRIORITY);
		}
    	else{
			group.setMaxPriority(MKRA_PRIORITY);    		
    	}
    }
	/**
	 * @see java.util.concurrent.ThreadFactory#newThread(java.lang.Runnable)
	 */
	@Override
    public Thread newThread(final Runnable r) {
//    	System.err.println("\n\n\new thread "+(++nbthread));
        final Thread t = new Thread(group, r, group.getName());
        t.setDaemon(daemonThreads);
        t.setPriority(t.getThreadGroup().getMaxPriority());
        return t;
    }
	
	ThreadGroup getThreadGroup(){
		return group;
	}
	
}