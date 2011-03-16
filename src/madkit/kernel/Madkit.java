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

import static java.lang.Boolean.parseBoolean;
import static madkit.kernel.Utils.createFileHandler;
import static madkit.kernel.Utils.logSevereException;
import static madkit.kernel.Utils.logWarningException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
/**
 * The brand new version of the starter class of MadKit.
 * <p>
 * <h2>MadKit v.5 new features</h2>
 * <p>
 * <ul>
 * <li>One big change that comes with version 5 is how agents
 * are identified and localized within the artificial society.
 * An agent is no longer binded to a single agent address but 
 * has as many agent addresses as holden positions in the artificial society.
 * see {@link AgentAddress} for more information.</li>
 * <br>
 * <li>With respect to the previous change, a <code><i>withRole</i></code> version
 * of all the messaging methods has been added. 
 * See {@link AbstractAgent#sendMessageWithRole(AgentAddress, Message, String)} for an example
 * of such a method.</li>
 * <br><li>A replying mechanism has been introduced through 
 * <code><i>sendReply</i></code> methods. 
 * It enables the agent with the possibility of replying directly to a given message.
 * Also, it is now possible to get the reply to a message, or to wait for a reply 
 * ( for {@link Agent} subclasses only as they are threaded)
 * See {@link AbstractAgent#sendReply(Message, Message)}
 * for more details.</li>
 * <br><li>Agents now have a <i>formal</i> state during a MadKit session.
 * See the {@link AbstractAgent#getState()} method for detailed information.</li>
 * <br><li>One of the most convenient improvement of v.5 is the logging mechanism which is provided.
 * See the {@link AbstractAgent#logger} attribute for more details.</li>
 * <br><li>Internationalization is being made (fr_fr and en_us for now).</li>
 * <p>

 * @author Fabien Michel
 * @author Jacques Ferber
 * @since MadKit 4.0
 * @version 5.0
 */


final public class Madkit {

	public static final String network = "network";
	/**
	 * Parameterizable option used to launch agents at start up.
	 * This option can be used to launch agents 
	 * from the command line or using the main method of MadKit.
	 * Parameters could be added to (1) launch several different types
	 * of agents, (2) launch the agents with a default GUI and (3) 
	 * specifying the number of desired instances of each type.
	 * <p>
	 * Key value is {@value}.<p>
	 * Default value is <i>"null"</i>, meaning that no agent has to be launched.
	 * <p>
	 * Option parameters :<p> 
	 * --{@value}
	 * <ul>
	 * <li><i>agentClassName</i>,</li> 
	 * <li><i>withDefaultGUI</i>(boolean optional),</li>
	 * <li><i>numberOfdesiredInstances</i>(int optional),</li>
	 * </ul> 
	 * <p>Default values for the optional parameters are
	 * <ul>
	 * <li><i>withDefaultGUI</i> = false</li>
	 * <li><i>numberOfdesiredInstances</i> = 1</li> 
	 * </ul>
	 * <p>Examples:
	 * <ul>
	 * <li> : --{@value} myPackage.MyAgent</li> 
	 * <li> : --{@value} myPackage.MyAgent,true</li>
	 * <li> : --{@value} myPackage.MyAgent,false,3</li> 
	 * <li> : --{@value} myPackage.MyAgent;other.OtherAgent</li> 
	 * <li> : --{@value} myPackage.MyAgent,true;other.OtherAgent,true</li>
	 * <li> : --{@value} myPackage.MyAgent;other.OtherAgent,true,3</li>
	 * </ul>
	 * @see AbstractAgent#getMadkitProperty(String)
	 * @see AbstractAgent#setMadkitProperty(String, String)
	 * @since Madkit 5 
	 */
	public static final String launchAgents = "launchAgents";
	public static final String configFile = "configFile";
	/**
	 * Parameterizable option defining the default warning log level for newly
	 * launched agents. Key value is {@value}.
	 * Default value is "INFO". This value could be overridden
	 * individually by agents using {@link AbstractAgent#setLogLevel(Level, Level)}. 
	 * <p>Example:
	 * <ul>
	 * <li> : --{@value} OFF</li> 
	 * <li> : --{@value} ALL</li> 
	 * <li> : --{@value} FINE</li> 
	 * </ul>
	 * @see AbstractAgent#logger
	 * @see java.util.logging.Logger
	 * @see AbstractAgent#getMadkitProperty(String)
	 * @see AbstractAgent#setMadkitProperty(String, String)
	 * @since Madkit 5 
	 */
	public static final String warningLogLevel = "warningLogLevel";
	public static final String createLogFiles = "createLogFiles";
	public static final String logDirectory = "logDirectory";
	public static final String agentsLogFile = "agentsLogFile";
	/**
	 * Boolean option defining if agent logging should be quiet in the
	 * default console.
	 * Key value is {@value}.
	 * Default value is "false". 
	 * <p>Usage example:
	 * <ul>
	 * <li> : --{@value} (is equivalent to) --{@value} true</li> 
	 * <li> : --{@value} false (useless as this is the default value)</li>
	 * </ul>
	 * @since Madkit 5 
	 */
	public static final String noAgentConsoleLog = "noAgentConsoleLog";
	/**
	 * Parameterizable option defining the default agent log level for newly
	 * launched agents. Key value is {@value}.
	 * Default value is "INFO". This value could be overridden
	 * individually by agents using {@link AbstractAgent#setLogLevel(Level)}. 
	 * <p>Example:
	 * <ul>
	 * <li> : --{@value} OFF</li> 
	 * <li> : --{@value} ALL</li> 
	 * <li> : --{@value} FINE</li> 
	 * </ul>
	 * @see AbstractAgent#logger
	 * @see java.util.logging.Logger
	 * @see AbstractAgent#getMadkitProperty(String)
	 * @see AbstractAgent#setMadkitProperty(String, String)
	 * @since Madkit 5 
	 */
	public final static String agentLogLevel = "agentLogLevel";
	/**
	 * Boolean option defining if organization logging should be quiet in the
	 * default console.
	 * Key value is {@value}.
	 * Default value is "false". 
	 * <p>Usage example:
	 * <ul>
	 * <li> : --{@value} (is equivalent to) --{@value} true</li> 
	 * <li> : --{@value} false (useless as this is the default value)</li>
	 * </ul>
	 * @since Madkit 5 
	 */
	public final static String noOrgConsoleLog = "noOrgConsoleLog";
	/**
	 * Parameterizable option defining the default organization log level. 
	 * Key value is {@value}.
	 * Default value is "OFF". 
	 * <p>Example:
	 * <ul>
	 * <li> : --{@value} OFF</li> 
	 * <li> : --{@value} ALL</li> 
	 * <li> : --{@value} FINE</li> 
	 * </ul>
	 * @since Madkit 5 
	 */
	final static String orgLogLevel = "orgLogLevel";
	public final static String MadkitLogFile = "MadkitLogFile";
	/**
	 * Boolean option defining if MadKit logging should be quiet in the
	 * default console.
	 * Key value is {@value}.
	 * Default value is "false". 
	 * <p>Usage example:
	 * <ul>
	 * <li> : --{@value} (is equivalent to) --{@value} true</li> 
	 * <li> : --{@value} false (useless as this is the default value)</li>
	 * </ul>
	 * @since Madkit 5 
	 */
	public final static String noMadkitConsoleLog = "noMadkitConsoleLog";
	/**
	 * Parameterizable option defining the default MadKit log level. 
	 * Key value is {@value}.
	 * Default value is "INFO". 
	 * <p>Example:
	 * <ul>
	 * <li> : --{@value} OFF</li> 
	 * <li> : --{@value} ALL</li> 
	 * <li> : --{@value} FINE</li> 
	 * </ul>
	 * @since Madkit 5 
	 */
	public final static String MadkitLogLevel = "MadkitLogLevel";


	/**
	 * Default roles within a MadKit organization.
	 */
	final public static class Roles{
		private Roles(){}
		/**
		 * The value of this constant is {@value}.
		 */
		public static final String GROUP_MANAGER_ROLE = "manager";
		/**
		 * The value of this constant is {@value}.
		 */
		public static final String GROUP_CANDIDATE_ROLE = "candidate";
		/**
		 * The value of this constant is {@value}.
		 */
		public static final String LOCAL_COMMUNITY = "local";
		/**
		 * The value of this constant is {@value}.
		 */
		public static final String SYSTEM_GROUP = "system";
		//	public static final String SYSTEM_GROUP = "system";
		/**
		 * The value of this constant is {@value}.
		 */
		public static final String KERNEL_ROLE = "kernel";

		/**
		 * The value of this constant is {@value}.
		 */
		public static final String GUI_MANAGER_ROLE = "gui manager";

	}


	final static Properties defaultConfig = new Properties();
	static{
		try {
			defaultConfig.load(Madkit.class.getResourceAsStream("/madkitKernel.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static final String booterAgentKey = "booterAgent";

	final private Properties madkitConfig;
	final private KernelAddress platformID;

	private Element madkitXMLConfigFile=null;
	private FileHandler aaLogFile;
	private FileHandler madkitLogFileHandler;
	private MadkitKernel myKernel;
	private KernelAgent kernelAgent;
	private Logger logger;
	private MadkitClassLoader madkitClassLoader;
	private static Madkit currentInstance;

	
	static Madkit getCurrentInstance(){
		return currentInstance;
	}

	/**
	 * @return the madkitLogFileHandler
	 */
	final FileHandler getMadkitLogFileHandler() {
		return madkitLogFileHandler;
	}

	/**
	 * @param madkitLogFileHandler the madkitLogFileHandler to set
	 */
	private final void setMadkitLogFileHandler(FileHandler madkitLogFileHandler) {
		this.madkitLogFileHandler = madkitLogFileHandler;
	}

	Madkit(String[] args){
		currentInstance = this;
		platformID = new KernelAddress();
		madkitConfig = new Properties(defaultConfig);
		initMadkitLogging();
		checkI18NFiles();
		loadJarFileArguments();
		logger.fine("** PARSING COMMAND LINE ARGUMENTS **");
		parseArguments(args);
		logCurrentMadkitConfig(Level.FINER);
		buildMadkitClassLoader();
		createLogDirectory();
		buildKernel();
		printWelcomeString();
		launchConfigAgents();
	}
	
	/**
	 * 
	 */
	private void createLogDirectory() {
		if (Boolean.parseBoolean(madkitConfig.getProperty(createLogFiles))) {
			SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
			String logDir = madkitConfig.getProperty(logDirectory) + simpleFormat.format(new Date());
			new File(logDir).mkdirs();
			logger.fine("** CREATE LOG DIRECTORY "+logDir+" **");
			madkitConfig.setProperty(logDirectory, logDir+File.separator);
		}
	}

	/**
	 * 
	 */
	private void loadJarFileArguments() {
		String [] args = null;
		logger.fine("** LOADING JAR FILE ARGUMENTS **");
		try{
			for (Enumeration<URL> urls = Madkit.class.getClassLoader().getResources("META-INF/MANIFEST.MF");urls.hasMoreElements();) {
				Manifest manifest = new Manifest(urls.nextElement().openStream());
				//				logger.info(manifest.toString());
				//				for (Map.Entry<String, Attributes> e : manifest.getEntries().entrySet()) {
				//					System.err.println("\n"+e.getValue().values());
				//				}
				Attributes projectInfo = manifest.getAttributes("MadKit-Project-Info");
				if(projectInfo != null){
					logger.finest("found project info"+projectInfo);
					args = projectInfo.getValue("MadKit-Args").split(" ");
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(args==null){//TODO tell me why I do that ;)
			logger.finest("No arguments found in the manifest files");
			Properties project = new Properties();
			try {
				project.load(Madkit.class.getClassLoader().getResourceAsStream("project.properties"));
				args = project.getProperty("MadKit-Args").split(" ");
			} catch (IOException e) {
				e.printStackTrace();
			} catch (NullPointerException e) {
				// no file (IDE mode)
			}
		}
		if (args != null) {
			parseArguments(args);
		}
	}

	private void start() {
		//setting up default configuration
		//		madkitConfig.putAll(defaultConfig);
		//
		//		//		madkitConfig.put(MADKIT_LOG_LEVEL, Level.FINEST.toString()); // if I want to debug starting phase
		//
		//		logger = Logger.getLogger(mkLoggerName);
		//		logger.setLevel(Level.parse(madkitConfig.getProperty(Madkit.MadkitLogLevel)));
		//		logger.setUseParentHandlers(false);
		//		final ConsoleHandler cs = new ConsoleHandler();
		//		cs.setLevel(logger.getLevel());
		//		cs.setFormatter(new MadkitFormatter());
		//		logger.addHandler(cs);
		//		logger.fine("****** MadKit is starting ******\n"); //TODO i18n
		//		logger.fine("****** MadKit kernel address is "+platformID+" ******\n"); //TODO i18n


	}

	/**
	 * 
	 */
	private void buildMadkitClassLoader() {
		logger.finer("** BUILDING MADKIT CLASS LOADER **");
		final ClassLoader systemCL = getClass().getClassLoader();
		if(systemCL instanceof URLClassLoader){
			madkitClassLoader = new MadkitClassLoader(((URLClassLoader) systemCL).getURLs(),systemCL);			
		}
		else{
			madkitClassLoader = new MadkitClassLoader(new URL[0],systemCL);
		}
		logger.finest("ClassPath is: ");
		for (URL url : madkitClassLoader.getURLs()) {
			logger.finest(" "+url);
		}
		logger.fine("** MADKIT CLASS LOADER INITIALIZED **");
	}

	/**
	 * 
	 */
	private void initMadkitLogging() {
		logger = Logger.getLogger("[*MK_"+platformID.hashCode()+"*]");
		logger.setUseParentHandlers(false);
		logger.setLevel(Level.parse(madkitConfig.getProperty("platformLogLevel")));
		ConsoleHandler cs = new ConsoleHandler();
		cs.setFormatter(new MadkitFormatter());
		cs.setLevel(logger.getLevel());
		logger.addHandler(cs);
		logger.fine("** LOGGING INITIALIZED **");
	}

	private void checkI18NFiles() {
		logger.finer("** LOADING I18N FILES **");
		logger.finest("Current Locale is "+Locale.getDefault().getCountry()+" : "+Locale.getDefault().getDisplayCountry());
		try {
			ResourceBundle.getBundle(madkitConfig.getProperty("madkit.resourceBundle.file"));
		} catch (MissingResourceException e) {
			logSevereException(logger,e, "i18n default files not found: Loading failed");
		}
		logger.fine("** I18N FILES SUCCESSFULLY LOADED **");
	}

	//	/**
	//	 * @returns the default madkit configuration from the madkitkernel jar file or from the classpath (an IDE is used)
	//	 */
	//	private void loadInternalConfig() {
	//		String fileName = defaultConfig.getProperty("madkit.user.properties.file");
	//		logger.fine("** Loading user config file: "+fileName+" **");
	//		//looking into the madkitkernel jar file
	//		InputStream in = getClass().getResourceAsStream("/"+fileName);
	//		//IDE in charge of classpath
	//		if(in==null){
	//			logger.finest("kernel jar file not found: Looking for the madkit internal config file in the classpath");			
	//			try {
	//				in = new FileInputStream(fileName);
	//				logger.finer("MadKit internal config file found on the classpath");
	//			} catch (FileNotFoundException e) {
	//				logWarningException(logger, e, "Did not find the madkit internal config file anywhere");
	//			} catch (SecurityException e) {
	//				logWarningException(logger, e, "Unable to read the madkit internal config file");
	//			}
	//		}
	//		else
	//			logger.finest("MadKit internal config file found in the madkitkernel jar file");
	//		logger.finer("Loading the MadKit internal config file which has been found");
	//		if(in != null){
	//			try {
	//				madkitConfig.load(in);
	//				logCurrentMadkitConfig(Level.FINER);
	//				logger.fine("** MadKit internal config file successfuly loaded **\n");
	//			} catch (IOException e) {
	//				logWarningException(logger, e, "Unable to read the madkit internal config file on the file system ! Please check permissions");
	//			} catch (IllegalArgumentException e) {
	//				logSevereException(logger, e, "madkit internal config file corrupted");
	//			} finally{
	//				try{
	//					in.close();				
	//				} catch (IOException e) {
	//					logSevereException(logger, e, "This is just impossible !!! Oo");
	//				}
	//			}
	//		}
	//		else
	//			logger.warning("* MadKit internal config file loading failed ! *\n");	
	//	}

	private void parseArguments(String[] args) {
		if(args == null){
			logger.finer("** No command line arguments found**\n");
			return;
		}
		Map<String,String> argumentsMap2 = new HashMap<String, String>();
		parseArgumentsIntoMap(args,argumentsMap2);
		if(argumentsMap2.containsKey(Madkit.configFile)){
			logger.finest("Config file option activated");
			if(checkAndValidateOption(Madkit.configFile, argumentsMap2.get(Madkit.configFile))){
				loadConfigFile(madkitConfig.getProperty(Madkit.configFile), argumentsMap2);
			}
			logger.finest("Overriding any option with command line arguments");
			parseArgumentsIntoMap(args,argumentsMap2);
		}
		//overriding options with command line options
		for (Map.Entry<String, String> e : argumentsMap2.entrySet()) {
			checkAndValidateOption(e.getKey(),e.getValue());
		}
		logger.finer("** arguments parsing finished **\n");
		logCurrentMadkitConfig(Level.FINEST);
	}

	private void loadConfigFile(String fileName, Map<String, String> argumentsMap2) {
		logger.fine("** Loading config file "+fileName+" **");

		URL url = getClass().getClassLoader().getResource(fileName);
		//		URL url = Utils.getFileURLResource(fileName);
		if(url == null){
			if(logger != null){
				logger.warning("Config file not found : "+fileName);
				return;
			}
		}
		final File f = new File(url.getFile());
		try {
			logger.finer("Loading config file " +f.getCanonicalPath().toString());
		} catch (IOException e) {
			logWarningException(logger,e,"IO error");
		} catch (SecurityException e) {
			logWarningException(logger, e,"Unable to read -- "+f.getName()+" --: permission denied");
		}
		try {
			madkitXMLConfigFile = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new FileInputStream(f)).getDocumentElement();
			NodeList madkitOptionNodes = madkitXMLConfigFile.getElementsByTagName("madkitOptions");
			for (int i = 0; i < madkitOptionNodes.getLength(); i++) {
				org.w3c.dom.NamedNodeMap options = madkitOptionNodes.item(i).getAttributes();
				for (int j = 0; j < options.getLength(); j++) {
					argumentsMap2.put(options.item(j).getNodeName(),options.item(j).getNodeValue());					
				}
			}
		} catch (FileNotFoundException e) {
			logWarningException(logger,e, "File not found");
		} catch (SAXException e) {
			logWarningException(logger,e, "XML error");
		} catch (IOException e) {
			logSevereException(logger, e,  "IO error");
		} catch (ParserConfigurationException e) {
			logSevereException(logger, e,  "XML parser error");
		}
		if(madkitXMLConfigFile == null){
			logger.warning("** Config file -- "+fileName+" -- not properly loaded !!**");
			return;
		}
		logger.fine("** Config file "+fileName+" successfully loaded **\n");
		logCurrentMadkitConfig(Level.FINEST);
	}

	/**
	 * 
	 */
	private void buildKernel() {
		logger.finer("** INITIALIZING MADKIT KERNEL **");
		myKernel = new MadkitKernel(this);
		LoggedKernel lk = new LoggedKernel(myKernel);
		myKernel.setLoggedKernel(lk);
		kernelAgent = new KernelAgent(myKernel);
		kernelAgent.start();
		logger.fine("** KERNEL AGENT LAUNCHED **");
	}

	private void launchConfigAgents(){
		logger.fine("** LAUNCHING CONFIG AGENTS **");
		final String agentsTolaunch =madkitConfig.getProperty(Madkit.launchAgents);
		if(! agentsTolaunch.equals("null")){
			final String[] agentsClasses = agentsTolaunch.split(";");
			for(final String classNameAndOption : agentsClasses){
				final String[] classAndOptions = classNameAndOption.split(",");
				final String className = classAndOptions[0].trim();//TODO should test if these classes exist
				final boolean withGUI = (classAndOptions.length > 1 ? Boolean.parseBoolean(classAndOptions[1].trim()) : false);
				int nbumber = 1;
				if(classAndOptions.length > 2) {
					nbumber = Integer.parseInt(classAndOptions[2].trim());
				}
				logger.finer("Launching "+nbumber+ " instance(s) of "+className+" with GUI = "+withGUI);
				for (int i = 0; i < nbumber; i++) {
					kernelAgent.launchAgent(className, 1, withGUI); //time out -> MK not blocked//could be optimize especially bucket
				}
			}
		}
	}

	/**
	 * @return
	 */
	String getMadkitLoggerName() {
		return "[*"+platformID.hashCode()+"*]";
	}

	/**
	 * 
	 */
	private void printWelcomeString() {
		if(! (madkitConfig.getProperty(Madkit.noMadkitConsoleLog).equals("true")
				|| Level.parse(madkitConfig.getProperty(Madkit.MadkitLogLevel)).equals(Level.OFF))){
			System.err.println("\n\t-----------------------------------------------------");
			System.err.println("\n\t\t\t   MadKit");
			System.err.println("\n\t\t   version: "+defaultConfig.getProperty("madkit.version")+"\n\t\t  build: "+defaultConfig.getProperty("build.id"));
			System.err.println("\n\t\tby MadKit Team (c) 1997-"+Calendar.getInstance().get(Calendar.YEAR));
			System.err.println("\n\t-----------------------------------------------------\n");			
		}
	}

	private String misuseOptionMessage(String option,String value) {
		return "\n\n-------------MadKit WARNING------------------\n" +
		"Misuse of --"+option+" option\nincorrect value : "+value+
		"\n--------------------------------------------\n";
	}

	/**
	 * @param agentsLogFile the agentsLogFile to set
	 */
	private final void setAgentsLogFile(FileHandler agentsLogFile) {
		this.aaLogFile = agentsLogFile;
	}

	private boolean isOptionWithDifferentValue(String optionName,String option, String value) {
		return option.equals(optionName) && ! madkitConfig.getProperty(optionName).equals(value);
	}

	/**
	 * @return the platformID
	 */
	KernelAddress getPlatformID() {
		return platformID;
	}

	/**
	 * @param option
	 * @param value
	 * @return 
	 */
	private void modifyMadkitOption(String option, String value) { 
		logger.finest("Modifying MadKit option "+option+" from "+madkitConfig.getProperty(option)+" to "+value);
		madkitConfig.put(option,value);
	}

	Logger setLogging(String loggerName, boolean csLoggingOn, Level logLevel,List<FileHandler> fhs, Formatter f) {
		if(logger != null){
			logger.finest("** Initializing logger "+loggerName+" **");
		}
		if(logLevel == Level.OFF){
			if(logger != null){
				logger.finest("Turning off all "+loggerName+" logging");
			}
			return null;
		}
		Logger l = initLogger(loggerName,logLevel, csLoggingOn,fhs,f);
		if(logger != null){
			logger.finest("** "+loggerName+" logging initialized **");
		}
		return l;
	}



	void logCurrentMadkitConfig(Level lvl){
		if(logger != null){
			String message = "MadKit current configuration is\n\n";
			message+="\t--- MadKit regular options ---\n";
			for (String option : defaultConfig.stringPropertyNames()) {
				message+="\t"+String.format("%-" + 30 + "s", option)+madkitConfig.getProperty(option)+"\n";					
			}
			Set<Object> tmp = new HashSet<Object>(madkitConfig.keySet());
			tmp.removeAll(defaultConfig.keySet());
			if(tmp.size()>0){
				message+="\n\t--- Additional non MadKit options ---\n";
				for(Object o : tmp)
					message+="\t"+String.format("%-" + 25 + "s", o)+madkitConfig.get(o)+"\n";
			}
			logger.log(lvl, message);
		}
	}

	//TODO put these properties in System.properties when possible

	private void parseArgumentsIntoMap(String[] args,Map<String,String> currentMap) {//TODO check every option
		String  parameters = "";
		String currentOption = null;
		for(int i = 0; i < args.length ; i++){
			if(args[i].startsWith("--")){
				currentOption = args[i].substring(2);
				currentMap.put(currentOption, "true");
				if(logger != null)
					logger.finest("found option -- "+currentOption);
				parameters="";
			}
			else{
				parameters+=args[i]+" ";
				if(i+1 == args.length || args[i+1].startsWith("--")){
					currentMap.put(currentOption, parameters.trim());
					if(logger != null)
						logger.finest("found option -- "+currentOption+" -- value -- "+parameters.trim());
				}
			}
		}
		if(logger != null)
			logger.finest("build temp map is "+currentMap);
	}


	Logger getLogger() {
		return logger;
	}

	Properties getConfigOption() {
		return madkitConfig;
	}

	Logger initLogger(String loggerName, Level lvl, boolean consoleOn, List<FileHandler> fhs, Formatter formatter) {
		Logger newLogger = Logger.getLogger(loggerName);
		Logger tmpLogger = logger;
		if(logger == newLogger){// in case this is the MK logger
			tmpLogger = Logger.getLogger("[TMP]",defaultConfig.getProperty("madkit.resourceBundle.file"));
			tmpLogger.setUseParentHandlers(false);
			tmpLogger.setLevel(logger.getLevel());
			for(Handler h : tmpLogger.getHandlers()){
				tmpLogger.removeHandler(h);
			}
			for(Handler h : logger.getHandlers()){
				tmpLogger.addHandler(h);
			}
		}
		if(tmpLogger != null){
			tmpLogger.finest("Removing all previous handlers of "+newLogger.getName());
			tmpLogger.finest(newLogger.getName()+" log level changed from "+newLogger.getLevel()+" to "+lvl);
		}
		for(Handler h : newLogger.getHandlers())
			newLogger.removeHandler(h);
		newLogger.setLevel(lvl);
		newLogger.setUseParentHandlers(false);
		if(consoleOn){
			newLogger.addHandler(new ConsoleHandler());
			if(tmpLogger != null){
				tmpLogger.finest("Console handling is on");
			}
		}
		for (FileHandler fh : fhs) {
			if (fh != null) {
				if (logger != null)
					tmpLogger.finest("Creating a log file for logger "+ newLogger.getName());
				fh.setLevel(newLogger.getLevel());
				newLogger.addHandler(fh);
			}
		}
		for(Handler h : newLogger.getHandlers()){
			h.setLevel(newLogger.getLevel());
			if(formatter != null)
				h.setFormatter(formatter);
		}
		if(newLogger.getHandlers().length == 0){
			newLogger = null;
		}
		return newLogger;
	}

	/**
	 * @return the agentsLogFile
	 */
	FileHandler getAgentsLogFile() {
		return aaLogFile;
	}

	/**
	 * @param requester 
	 * @param agentClassName
	 * @return
	 * @throws ClassNotFoundException 
	 */
	@SuppressWarnings("unchecked")
	Class<? extends AbstractAgent> loadClass(AbstractAgent requester, String agentClassName){
		try {
			return (Class<? extends AbstractAgent>) madkitClassLoader.loadClass(agentClassName);
		} catch (ClassNotFoundException e) {//TODO log severe
			logWarningException(requester.getLogger(), e, "Unable to load class: "+agentClassName);//TODO think about that
		} catch (ClassCastException e) {
			logWarningException(requester.getLogger(), e, "Unable to launch "+agentClassName+": Not a MadKit agent class");//TODO think about that
		}
		return null;
	}

	/**
	 * @param className
	 * @return
	 * @throws ClassNotFoundException 
	 */
	void reloadClass(String name) throws ClassNotFoundException {
		madkitClassLoader.reloadClass(this, name);
//		//if class has been already loaded -> need new class loader
//		Class<?> c = madkitClassLoader.reloadClass(this, name);
//		try {
//			c.newInstance();
//		} catch (InstantiationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IllegalAccessException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
////		if (! madkitClassLoader.isNewClassToReload(className)) {
////			madkitClassLoader = new MadkitClassLoader(madkitClassLoader);
////		}
	}

	/**
	 * @param option
	 * @param value
	 * @return true if something has been changed, false otherwise
	 */
	@SuppressWarnings("unchecked")
	boolean checkAndValidateOption(String option, String value){//TODO check all the options + update on what has to be !!
		if(option == null || value == null)
			return false; //TODO log error
		if(! defaultConfig.containsKey(option)){
			if(logger != null){
				logger.finer("Adding a non MadKit option: "+option+", value is "+value);
			}
			madkitConfig.put(option,value);
			return true;
		}
		if(isOptionWithDifferentValue(agentLogLevel, option, value) || 
				isOptionWithDifferentValue(MadkitLogLevel, option, value) || 
				isOptionWithDifferentValue(orgLogLevel, option, value) || 
				isOptionWithDifferentValue(warningLogLevel, option, value)){
			try {
				final Level l = Level.parse(value);
				modifyMadkitOption(option, l.getName());
				if(myKernel != null){
					myKernel.setDefaultAgentLogLevel(Level.parse(madkitConfig.getProperty(agentLogLevel)),Level.parse(madkitConfig.getProperty(warningLogLevel)));
				}
				return true;
			} catch (IllegalArgumentException e1) {
				System.err.println(misuseOptionMessage(option,value));
			}
		}
		if(option.equals(Madkit.configFile)){
			if(! (value.equals("true") || value.equals("null") || value.equals(""))){
				modifyMadkitOption(option, value);
				return true;
			}
		}
		if(option.equals(Madkit.launchAgents)){
			if(! (value.equals("true") || value.equals("null") || value.equals(""))){
				if (launchAgentsOptionValidate(value)) {
					modifyMadkitOption(option, value);
					return true;
				}
			}
		}
		if(option.equals(Madkit.booterAgentKey)){
			if(value.equals("true")){
				if(logger != null){
					logger.warning("Missing argument for option --"+Madkit.booterAgentKey+ ": Using default agent booter");
				}
				return false;
			}
			try {
				@SuppressWarnings("unused")
				//the following line is only a check !
				final Class<? extends AbstractAgent> booterAgent = (Class<? extends AbstractAgent>) madkitClassLoader.loadClass(value);
				modifyMadkitOption(option, value);
				return true;
			} catch (ClassNotFoundException e) {
				logSevereException(logger,e, "Cannot find the booter agent class -- "+value+" -- in the classpath");
			} catch (ClassCastException e) {
				logSevereException(logger,e, value+" is not an AbstractAgent !!");
			}
			if(logger != null){
				logger.warning("Using default agent booter");
			}
		}
		if(isOptionWithDifferentValue(Madkit.agentsLogFile, option, value)){
			modifyMadkitOption(option, value);
			setAgentsLogFile(createFileHandler(value,logger));
			return true;
		}
		if(isOptionWithDifferentValue(Madkit.MadkitLogFile, option, value)){
			modifyMadkitOption(option, value);
			setMadkitLogFileHandler(createFileHandler(value, logger));
			return true;
		}
		if(isOptionWithDifferentValue(Madkit.logDirectory, option, value)){
			if(! value.endsWith(File.separator)){
				value += File.separator;
			}
			modifyMadkitOption(option, value);
			return true;
		}
		//parse boolean options with no immediate updates
		if(isOptionWithDifferentValue(Madkit.createLogFiles, option, value)
				|| isOptionWithDifferentValue(Madkit.noAgentConsoleLog, option, value)
				|| isOptionWithDifferentValue(Madkit.network, option, value)){
			value = value.trim().toLowerCase();
			if((value.equals("true") || value.equals("false"))){
				modifyMadkitOption(option, value);
				return true;
			}
		}
		if(isOptionWithDifferentValue(Madkit.noMadkitConsoleLog, option, value)){ //TODO tolowercase always
			value = value.trim().toLowerCase();
			if((value.equals("true") || value.equals("false"))){
				modifyMadkitOption(option, value);
				return true;
			}
		}
		return false;
	}

	/**
	 * @param value
	 * @return
	 */
	private boolean launchAgentsOptionValidate(String agentsTolaunch) {
		if(! agentsTolaunch.equals("null")){
			final String[] agentsClasses = agentsTolaunch.split(";");
			for(final String classNameAndOption : agentsClasses){
				final String[] classAndOptions = classNameAndOption.split(",");
				//				final String className = classAndOptions[0].trim();//TODO should test if these classes exist
				//				final boolean withGUI = (classAndOptions.length > 1 ? Boolean.parseBoolean(classAndOptions[1].trim()) : false);
				//				int nbumber = 1;
				if(classAndOptions.length > 2) {
					try {
						Integer.parseInt(classAndOptions[2].trim());
					} catch (NumberFormatException e) {
						return false;
					}
				}
			}
		}
		return true;
	}


	public static void main(String[] args) {
		new Madkit(args);
	}
	
	final void kernelLog(String message, Level logLvl, Throwable e) {
			logger.log(logLvl, message, e);
	}

	/**
	 * @return
	 */
	MadkitKernel getKernel() {
		return myKernel;
	}

	/**
	 * @param madkitClassLoader the madkitClassLoader to set
	 */
	final void setMadkitClassLoader(MadkitClassLoader madkitClassLoader) {
		this.madkitClassLoader = madkitClassLoader;
	}

}

final class MadkitFormatter extends Formatter {
	@Override
	public String format(LogRecord record) {
		if(record.getThrown() != null){
			String logHeader = "\n\n---KERNEL LOG--\n\n";
			System.err.print(logHeader);
			record.getThrown().printStackTrace(System.err);
			return record.getLoggerName()+" "+record.getLevel().getLocalizedName()+" : "+formatMessage(record)+logHeader;
		}
		return record.getLoggerName()+" "+record.getLevel().getLocalizedName()+" : "+formatMessage(record)+"\n";
	}
}