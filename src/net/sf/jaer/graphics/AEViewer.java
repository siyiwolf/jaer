/*
 * AEViewer.java
 *
 * This is the "main" jAER interface to the user. The main event loop "ViewLoop"
 * is here; see ViewLoop.run()
 *
 * Created on December 24, 2005, 1:58 PM
 */
package net.sf.jaer.graphics;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;

import net.sf.jaer.JAERViewer;
import net.sf.jaer.aemonitor.AEMonitorInterface;
import net.sf.jaer.aemonitor.AEPacketRaw;
import net.sf.jaer.aesequencer.AEMonitorSequencerInterface;
import net.sf.jaer.aesequencer.AESequencerInterface;
import net.sf.jaer.biasgen.Biasgen;
import net.sf.jaer.biasgen.BiasgenFrame;
import net.sf.jaer.biasgen.BiasgenHardwareInterface;
import net.sf.jaer.chip.AEChip;
import net.sf.jaer.chip.Calibratible;
import net.sf.jaer.chip.EventExtractor2D;
import net.sf.jaer.event.EventPacket;
import net.sf.jaer.eventio.AEDataFile;
import net.sf.jaer.eventio.AEFileInputStream;
import net.sf.jaer.eventio.AEFileOutputStream;
import net.sf.jaer.eventio.AEMulticastInput;
import net.sf.jaer.eventio.AEMulticastOutput;
import net.sf.jaer.eventio.AEServerSocket;
import net.sf.jaer.eventio.AEServerSocketOptionsDialog;
import net.sf.jaer.eventio.AESocket;
import net.sf.jaer.eventio.AESocketDialog;
import net.sf.jaer.eventio.AEUnicastDialog;
import net.sf.jaer.eventio.AEUnicastInput;
import net.sf.jaer.eventio.AEUnicastOutput;
import net.sf.jaer.eventprocessing.EventFilter;
import net.sf.jaer.eventprocessing.EventFilter2D;
import net.sf.jaer.eventprocessing.FilterChain;
import net.sf.jaer.eventprocessing.FilterFrame;
import net.sf.jaer.eventprocessing.PacketStream;
import net.sf.jaer.hardwareinterface.BlankDeviceException;
import net.sf.jaer.hardwareinterface.HardwareInterface;
import net.sf.jaer.hardwareinterface.HardwareInterfaceException;
import net.sf.jaer.hardwareinterface.HardwareInterfaceFactory;
import net.sf.jaer.hardwareinterface.HardwareInterfaceFactoryChooserDialog;
import net.sf.jaer.hardwareinterface.HasUpdatableFirmware;
import net.sf.jaer.hardwareinterface.udp.NetworkChip;
import net.sf.jaer.hardwareinterface.udp.UDPInterface;
import net.sf.jaer.hardwareinterface.usb.ReaderBufferControl;
import net.sf.jaer.hardwareinterface.usb.USBInterface;
import net.sf.jaer.hardwareinterface.usb.cypressfx2.CypressFX2;
import net.sf.jaer.hardwareinterface.usb.cypressfx2.CypressFX2EEPROM;
import net.sf.jaer.hardwareinterface.usb.cypressfx2.CypressFX2FirmwareFilennameChooserOkCancelDialog;
import net.sf.jaer.hardwareinterface.usb.cypressfx2.CypressFX2MonitorSequencer;
import net.sf.jaer.stereopsis.StereoPairHardwareInterface;
import net.sf.jaer.util.ClassChooserDialog;
import net.sf.jaer.util.DATFileFilter;
import net.sf.jaer.util.EngineeringFormat;
import net.sf.jaer.util.ExceptionListener;
import net.sf.jaer.util.HexString;
import net.sf.jaer.util.MenuScroller;
import net.sf.jaer.util.RecentFiles;
import net.sf.jaer.util.RemoteControl;
import net.sf.jaer.util.RemoteControlCommand;
import net.sf.jaer.util.RemoteControlled;
import net.sf.jaer.util.SubclassFinder;
import net.sf.jaer.util.TriangleSquareWindowsCornerIcon;
import ch.unizh.ini.jaer.chip.retina.DVS128;

/**
 * This is the main jAER interface to the user. The main event loop "ViewLoop"
 * is here; see ViewLoop.run(). AEViewer
 * shows AE chip live view and allows for controlling view and recording and
 * playing back events from files and network
 * connections.
 * <p>
 * AEViewer supports PropertyChangeListener's and fires PropertyChangeEvents on
 * the following events:
 * <ul>
 * <li>"playmode" - when the player mode changes, e.g. from PlayMode.LIVE to
 * PlayMode.PLAYBACK. The old and new values are the old and new PlayMode values
 * <li>"fileopen" - when a new file is opened; old=null, new=file.
 * <li>"stopme" - when stopme is called; old=new=null.
 * <li>"chip" - when a new AEChip is built for the viewer.
 * <li>"paused" - when paused or resumed - old and new booleans are passed to
 * firePropertyChange.
 * </ul>
 * In addition, when A5EViewer is in PLAYBACK PlayMode, users can register as
 * PropertyChangeListeners on the AEFileInputStream for rewind events, etc.
 *
 * @author tobi
 */
public class AEViewer extends javax.swing.JFrame implements PropertyChangeListener, DropTargetListener,
	ExceptionListener, RemoteControlled {

	/**
	 *
	 */
	private static final long serialVersionUID = -6284222754242166596L;
	/** PropertyChangeEvent fired from this AEViewer */
	public static final String EVENT_PLAYMODE = "playmode", EVENT_FILEOPEN = "fileopen", EVENT_STOPME = "stopme",
		EVENT_CHIP = "chip", EVENT_PAUSED = "paused", EVENT_TIMESTAMPS_RESET = "timestampsReset",
		EVENT_CHECK_NONMONOTONIC_TIMESTAMPS = "checkNonMonotonicTimestamps";
	public static String HELP_URL_USER_GUIDE = "http://jaerproject.net/";
	public static String HELP_URL_HELP_FORUM = "https://sourceforge.net/projects/jaer/forums/forum/631958";
	public static String HELP_URL_JAVADOC_WEB = "http://jaer.sourceforge.net/javadoc";
	public static String HELP_URL_JAVADOC;
	// note filenames cannot have spaces in them for browser to work easily,
	// some problem with space encoding; %20
	// doesn't work as advertized.
	public static String HELP_USER_GUIDE_USB2_MINI = "/doc/USBAERmini2userguide.pdf";
	public static String HELP_USER_GUIDE_AER_CABLING = "/doc/AERHardwareAndCabling.pdf";
	private static final String SET_DEFAULT_FIRMWARE_FOR_BLANK_DEVICE = "Set default firmware for blank device...";

	// set true to force null hardware (None in interface menu) even if only
	// single interface
	private boolean nullInterface = false;

	/**
	 * Utility method to return a URL to a file in the installation.
	 *
	 * @param path
	 *            relative to root of installation, e.g.
	 *            "/doc/USBAERmini2userguide.pdf"
	 * @return the URL string pointing to the local file
	 * @see #addHelpURLItem(java.lang.String, java.lang.String,
	 *      java.lang.String)
	 * @throws MalformedURLException
	 *             if there is something wrong with the URL
	 */
	public String pathToURL(final String path) throws MalformedURLException {
		final String curDir = System.getProperty("user.dir");
		final File f = new File(curDir);
		final File pf = f.getParentFile().getParentFile();
		final String urlString = "file://" + pf.getPath() + path;
		final URL url = new URL(urlString);
		return url.toString();
	}

	/**
	 * Adds item above separator/about
	 *
	 * @param menuItem
	 *            item to add
	 * @see #removeHelpItem(javax.swing.JMenuItem)
	 * @see #addHelpURLItem(java.lang.String, java.lang.String,
	 *      java.lang.String)
	 * @return the component that you added, for later removal
	 */
	public JComponent addHelpItem(final JComponent menuItem) {
		int n = helpMenu.getItemCount();
		if (n <= 2) {
			n = 0;
		}
		else {
			n = n - 2;
		}
		helpMenu.add(menuItem, n);
		return menuItem;
	}

	/**
	 * Registers a new item in the Help menu.
	 *
	 * @param url
	 *            for the item to be opened in the browser, e.g.
	 *            pathToURL("docs/board.pdf"), or
	 *            "http://jaerproject.net/".
	 * @param title
	 *            the menu item title
	 * @param tooltip
	 *            useful tip about help
	 * @return the menu item - useful for removing the help item.
	 * @see #removeHelpItem(javax.swing.JMenuItem)
	 * @see #pathToURL(java.lang.String)
	 */
	final public JComponent addHelpURLItem(final String url, final String title, final String tooltip) {
		final JMenuItem menuItem = new JMenuItem(title);
		menuItem.setToolTipText(tooltip);

		menuItem.addActionListener(new java.awt.event.ActionListener() {

			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				showInBrowser(url);
			}
		});
		addHelpItem(menuItem);
		return menuItem;
	}

	private void showInBrowser(final String url) {
		if (!Desktop.isDesktopSupported()) {
			log.warning("No Desktop support, can't show help from " + url);
			return;
		}
		try {
			Desktop.getDesktop().browse(new URI(url));
		}
		catch (final Exception ex) {
			log.warning("Couldn't show " + url + "; caught " + ex);
		}
	}

	/**
	 * Unregisters an item from the Help menu.
	 *
	 * @param m
	 *            the menu item originally returns from addHelpURLItem or
	 *            addHelpItem.
	 * @see #addHelpURLItem(java.lang.String, java.lang.String,
	 *      java.lang.String)
	 * @see #addHelpItem(javax.swing.JMenuItem)
	 */
	final public void removeHelpItem(final JComponent m) {
		if (m == null) {
			return;
		}
		helpMenu.remove(m);
	}

	/**
	 * Default port number for remote control of this AEViewer.
	 *
	 */
	public final int REMOTE_CONTROL_PORT = 8997; // TODO make this the starting
													// port number but find a
													// free one if not
													// available.

	/**
	 * Returns the frame for configurating chip. Could be null until user
	 * chooses to build it.
	 *
	 * @return the frame.
	 */
	public BiasgenFrame getBiasgenFrame() {
		return biasgenFrame;
	}

	/**
	 * Returns the frame holding the event filters. Could be null until user
	 * builds it.
	 *
	 * @return the frame.
	 */
	public FilterFrame getFilterFrame() {
		return filterFrame;
	}

	/**
	 * Call this method to break the ViewLoop out of a sleep wait, e.g. to force
	 * re-rendering of the data.
	 */
	public void interruptViewloop() {
		viewLoop.interrupt(); // to break it out of blocking operation such as
								// wait on cyclic barrier or socket
	}

	public void reopenSocketInputStream() throws HeadlessException {
		log.info("closing and reopening socket " + aeSocket);
		if (aeSocket != null) {
			try {
				aeSocket.close();
			}
			catch (final Exception e) {
				log.warning("closing existing socket: caught " + e);
			}
		}
		try {
			aeSocket = new AESocket(); // uses preferred settings for
										// port/buffer size, etc.
			aeSocket.connect();
			setPlayMode(PlayMode.REMOTE);
			openSocketInputStreamMenuItem.setText("Close socket input stream from " + aeSocket.getHost() + ":"
				+ aeSocket.getPort());
			log.info("opened socket input stream " + aeSocket);
			socketInputEnabled = true;
		}
		catch (final Exception e) {
			JOptionPane.showMessageDialog(this, "Exception reopening socket: " + e, "AESocket Exception",
				JOptionPane.WARNING_MESSAGE);
		}
	}

	/**
	 * Stores the preferred (startup) AEChip class for the viewer.
	 *
	 * @param clazz
	 *            the class.
	 */
	public void setPreferredAEChipClass(final Class clazz) {
		AEViewer.prefs.put("AEViewer.aeChipClassName", clazz.getName());
	}

	public final String REMOTE_START_LOGGING = "startlogging";
	public final String REMOTE_STOP_LOGGING = "stoplogging";
	public final String REMOTE_TOGGLE_SYNCHRONIZED_LOGGING = "togglesynclogging";

	/**
	 * Processes remote control commands for this AEViewer. A list of commands
	 * can be obtained
	 * from a remote host by sending ? or help. The port number is logged to the
	 * console on startup.
	 *
	 * @param command
	 *            the parsed command (first token)
	 * @param line
	 *            the line sent from the remote host.
	 * @return confirmation of command.
	 */
	@Override
	public String processRemoteControlCommand(final RemoteControlCommand command, final String line) {
		final String[] tokens = line.split("\\s");
		log.finer("got command " + command + " with line=\"" + line + "\"");
		try {
			if (command.getCmdName().equals(REMOTE_START_LOGGING)) {
				if (tokens.length < 2) {
					return "not enough arguments\n";
				}
				final String filename = line.substring(REMOTE_START_LOGGING.length() + 1);
				final File f = startLogging(filename);
				if (f == null) {
					return "Couldn't start logging to filename=" + filename + ", startlogging returned " + f + "\n";
				}
				else {
					return "starting logging to " + f + "\n";
				}
			}
			else if (command.getCmdName().equals(REMOTE_STOP_LOGGING)) {
				stopLogging(false); // don't confirm filename
				return "stopped logging\n";
			}
			else if (command.getCmdName().equals(REMOTE_TOGGLE_SYNCHRONIZED_LOGGING)) {
				if ((jaerViewer != null) && jaerViewer.isSyncEnabled() && (jaerViewer.getViewers().size() > 1)) {
					jaerViewer.toggleSynchronizedLogging();
					return "toggled synchronized logging\n";
				}
				else {
					return "couldn't toggle synchronized logging because there is only 1 viewer or sync is disbled";
				}
			}
		}
		catch (final Exception e) {
			return e.toString() + "\n";
		}
		return null;
	}

	/**
	 * @return the playerControls
	 */
	public AePlayerAdvancedControlsPanel getPlayerControls() {
		return playerControls;
	}

	/**
	 * @param playerControls
	 *            the playerControls to set
	 */
	public void setPlayerControls(final AePlayerAdvancedControlsPanel playerControls) {
		this.playerControls = playerControls;
	}

	/**
	 * @return the frameRater
	 */
	public FrameRater getFrameRater() {
		return frameRater;
	}

	/**
	 * @return the aeFileInputStreamTimestampResetBitmask
	 */
	public int getAeFileInputStreamTimestampResetBitmask() {
		return aeFileInputStreamTimestampResetBitmask;
	}

	/**
	 * @return the checkNonMonotonicTimeExceptionsEnabledCheckBoxMenuItem
	 */
	public javax.swing.JCheckBoxMenuItem getCheckNonMonotonicTimeExceptionsEnabledCheckBoxMenuItem() {
		return checkNonMonotonicTimeExceptionsEnabledCheckBoxMenuItem;
	}

	/**
	 * Returns an ArrayBlockingQueue that may be associated with this viewer;
	 * used for inter-viewer communication.
	 *
	 * @return the blockingQueueInput
	 */
	public ArrayBlockingQueue getBlockingQueueInput() {
		return blockingQueueInput;
	}

	private void closeAESocket() {
		if (aeSocket != null) {
			try {
				aeSocket.close();
				log.info("closed " + aeSocket);
			}
			catch (final IOException e) {
				e.printStackTrace();
			}
			finally {
				openSocketInputStreamMenuItem.setText("Open remote server input stream socket...");
				aeSocketClient = null;
			}
		}
		socketInputEnabled = false;
	}

	private void closeAESocketClient() {
		if (aeSocketClient != null) {
			try {
				aeSocketClient.close();
				log.info("closed " + aeSocketClient);
			}
			catch (final IOException e) {
				e.printStackTrace();
			}
			finally {
				openSocketOutputStreamMenuItem.setText("Open remote server iutput stream socket...");
				aeSocketClient = null;
			}
		}
		socketOutputEnabled = false;
	}

	private void closeUnicastInput() {
		if (unicastInput != null) {
			unicastInput.close();
			removePropertyChangeListener(unicastInput);
			log.info("closed " + unicastInput);
			openUnicastInputMenuItem.setText("Open unicast UDP input...");
			unicastInput = null;
		}
		unicastInputEnabled = false;
	}

	/**
	 * Returns the main viewer image display panel where the ChipCanvas is
	 * shown.
	 * DisplayMethod's can use this getter to add their own display controls.
	 *
	 * @return the imagePanel
	 */
	public javax.swing.JPanel getImagePanel() {
		return imagePanel;
	}

	/**
	 * @return the aeChipClassName
	 */
	public String getAeChipClassName() {
		return aeChipClassName;
	}

	/**
	 * @param aeChipClassName
	 *            the aeChipClassName to set
	 */
	public void setAeChipClassName(final String aeChipClassName) {
		this.aeChipClassName = aeChipClassName;
	}

	/**
	 * Modes of viewing: WAITING means waiting for device or for playback or
	 * remote, LIVE means showing a hardware
	 * interface, PLAYBACK means playing
	 * back a recorded file, SEQUENCING means sequencing a file out on a
	 * sequencer device, REMOTE means playing a remote
	 * stream of AEs
	 */
	public enum PlayMode {

		WAITING,
		LIVE,
		PLAYBACK,
		SEQUENCING,
		REMOTE
	}

	volatile private PlayMode playMode = PlayMode.WAITING;
	static Preferences prefs = Preferences.userNodeForPackage(AEViewer.class);
	Logger log = Logger.getLogger("AEViewer");
	// private PropertyChangeSupport support = new PropertyChangeSupport(this);
	// // already has support as Componenent!!!
	EventExtractor2D extractor = null;
	private BiasgenFrame biasgenFrame = null;
	Biasgen biasgen = null;
	EventFilter2D filter1 = null, filter2 = null;
	private AEChipRenderer renderer = null;
	AEMonitorInterface aemon = null;
	private final ViewLoop viewLoop = new ViewLoop();
	FilterChain filterChain = null;
	private FilterFrame filterFrame = null;
	RecentFiles recentFiles = null;
	File lastFile = null;
	public File lastLoggingFolder = null;// changed pol
	File lastImageFile = null;
	File currentFile = null;
	private final FrameRater frameRater = new FrameRater();
	ChipCanvas chipCanvas;
	volatile boolean loggingEnabled = false;
	/** The date formatter used by AEViewer for logged data files */
	private File loggingFile;
	AEFileOutputStream loggingOutputStream;
	private boolean activeRenderingEnabled = AEViewer.prefs.getBoolean("AEViewer.activeRenderingEnabled", false);
	private boolean renderBlankFramesEnabled = AEViewer.prefs.getBoolean("AEViewer.renderBlankFramesEnabled", false);
	// number of packets to skip over rendering, used to speed up real time
	// processing
	private int skipPacketsRenderingNumber = AEViewer.prefs.getInt("AEViewer.skipPacketsRenderingNumber", 0);
	int skipPacketsRenderingCount = 0; // render first packet always
	DropTarget dropTarget;
	File draggedFile;
	private boolean loggingPlaybackImmediatelyEnabled = AEViewer.prefs.getBoolean(
		"AEViewer.loggingPlaybackImmediatelyEnabled", false);
	private boolean enableFiltersOnStartup = AEViewer.prefs.getBoolean("AEViewer.enableFiltersOnStartup", false);
	private long loggingTimeLimit = 0, loggingStartTime = System.currentTimeMillis();
	private boolean logFilteredEventsEnabled = AEViewer.prefs.getBoolean("AEViewer.logFilteredEventsEnabled", false);
	private final DynamicFontSizeJLabel statisticsLabel;
	private boolean filterFrameBuilt = false; // flag to signal that the frame
												// should be rebuilt when
												// initially shown or
												// when chip is changed
	private AEChip chip;
	/** The default AEChip class. */
	public static String DEFAULT_CHIP_CLASS = DVS128.class.getName();
	/** The class name of the aeChipClass */
	private String aeChipClassName = null;
	/**
	 * The class we are displaying - this is the root object for practically
	 * everything display in an AEViewer.
	 */
	protected Class aeChipClass = null;
	// WindowSaver windowSaver;
	private JAERViewer jaerViewer;
	// multicast connections
	private AEMulticastInput aeMulticastInput = null;
	private AEMulticastOutput aeMulticastOutput = null;
	private boolean multicastInputEnabled = false, multicastOutputEnabled = false;
	// blockingQueue input
	private ArrayBlockingQueue blockingQueueInput = null;
	private boolean blockingQueueInputEnabled = false;
	// unicast dataqgram data xfer
	private volatile AEUnicastOutput unicastOutput = null;
	private volatile AEUnicastInput unicastInput = null;
	private boolean unicastInputEnabled = false, unicastOutputEnabled = false;
	// socket connections
	private volatile AEServerSocket aeServerSocket = null; // this server socket
															// accepts
															// connections from
															// clients who
															// want events from
															// us
	private volatile AESocket aeSocket = null; // this socket is used to
												// getString events from a
												// server to us
	private volatile AESocket aeSocketClient = null; // this socket is used send
														// events to a TCP
														// server
	private boolean socketInputEnabled = false; // flags that we are using
												// socket input stream
	private boolean socketOutputEnabled = false; // flags that we are using
													// socket input stream
	private boolean blankDeviceMessageShown = false; // flags that we have
														// warned about blank
														// device, don't show
														// message
														// again
	AEViewerLoggingHandler loggingHandler;
	private RemoteControl remoteControl = null; // TODO move to JAERViewer
	private int aeFileInputStreamTimestampResetBitmask = AEViewer.prefs.getInt(
		"AEViewer.aeFileInputStreamTimestampResetBitmask", 0);
	private AePlayerAdvancedControlsPanel playerControls;

	/**
	 * Constructs a new AEViewer using a default AEChip.
	 *
	 * @param jAERViewer
	 *            the containing top level JAERViewer
	 */
	public AEViewer(final JAERViewer jAERViewer) {
		this(jAERViewer, null);
	}

	/**
	 * Constructs a new instance and then sets class name of device to show in
	 * it
	 *
	 * @param jaerViewer
	 *            the manager of all viewers
	 * @param chipClassName
	 *            the AEChip to use
	 */
	public AEViewer(final JAERViewer jaerViewer, final String chipClassName) {
		loggingHandler = new AEViewerLoggingHandler(this); // handles log
															// messages globally
		loggingHandler.getSupport().addPropertyChangeListener(this); // logs to
																		// Console
																		// handler
																		// in
																		// AEViewer
		Logger.getLogger("").addHandler(loggingHandler);

		log.info("AEViewer starting up...");

		if (chipClassName == null) {
			aeChipClassName = AEViewer.prefs.get("AEViewer.aeChipClassName", AEViewer.DEFAULT_CHIP_CLASS);
		}
		else {
			aeChipClassName = chipClassName;
		}
		log.info("AEChip class name is " + aeChipClassName);
		try {
			// log.info("getting class for "+aeChipClassName);
			aeChipClass = FastClassFinder.forName(getAeChipClassName()); // throws
																			// exception
																			// if
																			// class
																			// not
																			// found
			if (java.lang.reflect.Modifier.isAbstract(aeChipClass.getModifiers())) {
				log.warning(aeChipClass + " is abstract, setting chip class to default " + AEViewer.DEFAULT_CHIP_CLASS);
				setAeChipClassName(AEViewer.DEFAULT_CHIP_CLASS);
				aeChipClass = FastClassFinder.forName(getAeChipClassName());
			}
		}
		catch (final ClassNotFoundException e) {
			log.warning(getAeChipClassName()
				+ " class not found or not a valid AEChip, setting preferred chip class to default "
				+ AEViewer.DEFAULT_CHIP_CLASS + " and using that class");
			AEViewer.prefs.put("AEViewer.aeChipClassName", AEViewer.DEFAULT_CHIP_CLASS);
			try {
				AEViewer.prefs.flush();
			}
			catch (final BackingStoreException ex) {
				log.warning("couldnt' flush the preferences to save preferred chip class: " + ex.toString());
			}
			try {
				aeChipClass = FastClassFinder.forName(AEViewer.DEFAULT_CHIP_CLASS);
			}
			catch (final ClassNotFoundException ex) {
				log.warning("could not even find the default chip class " + AEViewer.DEFAULT_CHIP_CLASS + ", exiting");
				System.exit(1);
			}
		}
		setLocale(Locale.US); // to avoid problems with other language support
								// in JOGL
		// try {
		// UIManager.setLookAndFeel(
		// UIManager.getCrossPlatformLookAndFeelClassName());
		// // UIManager.setLookAndFeel(new WindowsLookAndFeel());
		// } catch (Exception e) {
		// log.warning(e.getMessage());
		// }
		setName("AEViewer");

		initComponents();
		playerControls = new AePlayerAdvancedControlsPanel(this);
		playerControlPanel.add(playerControls, BorderLayout.NORTH);
		this.jaerViewer = jaerViewer;
		if (jaerViewer != null) {
			// all stuff having to do with synchronizing player buttons here,
			// binding components
			// TODO rework binding of jAERViewer player, AEViewer player, and
			// player GUI. The whole MVC idea is too
			// convoluted now.
			jaerViewer.addViewer(this); // register outselves, build menus here
										// that sync views, e.g. synchronized
										// playback // TODO, dependency, depends
										// on existing player control panel
			if (jaerViewer.getSyncPlayer() != null) {
				// now bind player control panel to SyncPlayer and bind jaer
				// sync player to player control panel.
				playerControls.addPropertyChangeListener(jaerViewer.getSyncPlayer());
				jaerViewer.getSyncPlayer().getSupport().addPropertyChangeListener(playerControls);
				if (jaerViewer.isSyncEnabled()) {
					playerControls.setAePlayer(jaerViewer.getSyncPlayer());
				}
			}
		}
		validate();

		statisticsLabel = new DynamicFontSizeJLabel();
		// statisticsLabel.setFont(new
		// java.awt.Font("Bitstream Vera Sans Mono 11 Bold", 0, 8));
		statisticsLabel
			.setToolTipText("Time slice/Absolute time, NumEvents/NumFiltered, events/sec, Frame rate acheived/desired, Time expansion X contraction /, delay after frame, color scale");
		statisticsPanel.add(statisticsLabel);
		final PropertyChangeListener[] list = statisticsLabel.getPropertyChangeListeners();
		for (final PropertyChangeListener p : list) {
			statisticsLabel.removePropertyChangeListener(p);
		}

		// HardwareInterfaceException.addExceptionListener(this);
		final int n = menuBar.getMenuCount();
		for (int i = 0; i < n; i++) {
			final JMenu m = menuBar.getMenu(i);
			m.getPopupMenu().setLightWeightPopupEnabled(false);
		}
		filtersSubMenu.getPopupMenu().setLightWeightPopupEnabled(false); // otherwise
																			// can't
																			// see
																			// on
																			// canvas
		graphicsSubMenu.getPopupMenu().setLightWeightPopupEnabled(false);
		remoteMenu.getPopupMenu().setLightWeightPopupEnabled(false); // make
																		// remote
																		// submenu
																		// heavy
																		// to
																		// show
																		// over
																		// glcanvas

		ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false); // to
																			// show
																			// menu
																			// tips
																			// over
																			// GLCanvas

		final String lastFilePath = AEViewer.prefs.get("AEViewer.lastFile", "");
		lastFile = new File(lastFilePath);

		final String defaultLoggingFolderName = System.getProperty("user.dir");
		// lastLoggingFolder starts off at user.dir which is startup folder
		// "host/java" where .exe launcher lives
		lastLoggingFolder = new File(AEViewer.prefs.get("AEViewer.lastLoggingFolder", defaultLoggingFolderName));

		// check lastLoggingFolder to see if it really exists, if not, default
		// to user.dir
		if (!lastLoggingFolder.exists() || !lastLoggingFolder.isDirectory()) {
			log.warning("lastLoggingFolder " + lastLoggingFolder + " no good, defaulting to "
				+ defaultLoggingFolderName);
			lastLoggingFolder = new File(defaultLoggingFolderName);
		}

		// recent files tracks recently used files *and* folders. recentFiles
		// adds the anonymous listener
		// built here to open the selected file
		recentFiles = new RecentFiles(AEViewer.prefs, fileMenu, new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent evt) {
				final File f = new File(evt.getActionCommand());
				// log.info("opening "+evt.getActionCommand());
				try {
					if ((f != null) && f.isFile()) {
						getAePlayer().startPlayback(f);
						recentFiles.addFile(f);
					}
					else if ((f != null) && f.isDirectory()) {
						AEViewer.prefs.put("AEViewer.lastFile", f.getCanonicalPath());
						aePlayer.openAEInputFileDialog();
						recentFiles.addFile(f);
					}
				}
				catch (final Exception fnf) {
					fnf.printStackTrace();
					// exceptionOccurred(fnf,this);
					recentFiles.removeFile(f);
				}
			}
		});

		// additional help
		try {
			addHelpURLItem(AEViewer.HELP_URL_USER_GUIDE, "jAER wiki and user guide",
				"Opens the jAER wiki and user guide");
			addHelpURLItem(AEViewer.HELP_URL_HELP_FORUM, "jAER help forum",
				"Opens the Help Forum on sourceforge.  Post your questions and look for answers there.");
			addHelpURLItem(AEViewer.HELP_URL_JAVADOC_WEB, "jAER javadoc", "jAER online javadoc (probably out of date)");

			addHelpItem(new JSeparator());
			addHelpURLItem(pathToURL(AEViewer.HELP_USER_GUIDE_USB2_MINI), "USBAERmini2 board",
				"User guide for USB2AERmini2 AER monitor/sequencer interface board");
			addHelpURLItem(pathToURL(AEViewer.HELP_USER_GUIDE_AER_CABLING), "AER protocol and cabling guide",
				"Guide to AER pin assignment and cabling for the Rome and CAVIAR standards");
			addHelpURLItem(pathToURL("/deviceFirmwarePCBLayout/SiLabsC8051F320/ServoUSBPCB/ServoUSB.pdf"),
				"USB Servo board", "Layout and schematics for the USB servo controller board");
			addHelpItem(new JSeparator());
		}
		catch (final Exception e) {
			log.warning("could register help item: " + e.toString());
		}

		HardwareInterfaceFactory.instance().buildInterfaceList(); // once only
																	// to start
		buildInterfaceMenu();
		buildDeviceMenu();
		// we need to do this after building device menu so that proper menu
		// item radio button can be selected
		cleanup(); // close sockets if they are open
		setAeChipClass(aeChipClass);

		playerControlPanel.setVisible(false);
		timestampResetBitmaskMenuItem.setText("Set timestamp reset bitmask... (currently 0x"
			+ Integer.toHexString(aeFileInputStreamTimestampResetBitmask) + ")");

		// pack(); // seems to make no difference

		// tobi removed following oct 2008 because it was somehow apparently
		// causing deadlock on exit, don't know why
		// Runtime.getRuntime().addShutdownHook(new Thread() {
		//
		// public void run() {
		// if (aemon != null && aemon.isOpen()) {
		// aemon.close();
		// }
		// }
		// });

		setFocusable(true);
		requestFocus();
		// viewLoop = new ViewLoop(); // declared final for synchronization
		dropTarget = new DropTarget(getImagePanel(), this);

		fixLoggingControls();

		// init menu items that are checkboxes to correct initial state
		viewActiveRenderingEnabledMenuItem.setSelected(isActiveRenderingEnabled());
		loggingPlaybackImmediatelyCheckBoxMenuItem.setSelected(isLoggingPlaybackImmediatelyEnabled());
		if (chip == null) {
			log.warning("null chip object - some problem occurred constructing chip instance");
		}
		else {
			subsampleEnabledCheckBoxMenuItem.setSelected(chip.isSubSamplingEnabled());
		}
		acccumulateImageEnabledCheckBoxMenuItem.setSelected(getRenderer().isAccumulateEnabled());
		autoscaleContrastEnabledCheckBoxMenuItem.setSelected(getRenderer().isAutoscaleEnabled());
		pauseRenderingCheckBoxMenuItem.setSelected(false);// not isPaused
															// because aePlayer
															// doesn't exist yet
		viewRenderBlankFramesCheckBoxMenuItem.setSelected(isRenderBlankFramesEnabled());
		logFilteredEventsCheckBoxMenuItem.setSelected(logFilteredEventsEnabled);
		enableFiltersOnStartupCheckBoxMenuItem.setSelected(enableFiltersOnStartup);

		fixSkipPacketsRenderingMenuItems();

		checkNonMonotonicTimeExceptionsEnabledCheckBoxMenuItem.setSelected(AEViewer.prefs.getBoolean(
			"AEViewer.checkNonMonotonicTimeExceptionsEnabled", true));

		// start the server thread for incoming socket connections for remote
		// consumers of events
		if (aeServerSocket == null) {
			try {
				aeServerSocket = new AEServerSocket();
				aeServerSocket.start();
			}
			catch (final IOException ex) {
				log.warning(ex.toString()
					+ ": While constructing AEServerSocket. Another viewer or process has already bound this port or some other error. This viewer will not have a server socket for AE data.");
				aeServerSocket = null;
			}
		}
		viewLoop.start();

		// add remote control commands
		// TODO encapsulate all this and command processor
		try {
			remoteControl = new RemoteControl(REMOTE_CONTROL_PORT);
			remoteControl.addCommandListener(this, REMOTE_START_LOGGING + " <filename>",
				"starts logging ae data to a file");
			remoteControl.addCommandListener(this, REMOTE_STOP_LOGGING, "stops logging ae data to a file");
			remoteControl.addCommandListener(this, REMOTE_TOGGLE_SYNCHRONIZED_LOGGING,
				"starts synchronized logging ae data to a set of files with aeidx filename automatically timstamped"); // TODO
																														// allow
																														// sync
																														// logging
																														// to
																														// a
																														// chosen
																														// file
																														// -
																														// change
																														// startLogging
																														// to
																														// do
																														// sync
																														// logging
																														// if
																														// viewers
																														// are
																														// synchronized
			log.info("created " + remoteControl + " for remote control of some AEViewer functions");
		}
		catch (final SocketException ex) {
			log.warning(ex.toString());
		}
		setTitleAccordingToState();

	}

	/**
	 * Closes hardware interface and network sockets.
	 * Register all cleanup here for other classes, e.g. Chip classes that open
	 * sockets.
	 */
	private void cleanup() {
		stopLogging(true); // in case logging, make sure we give chance to save
							// file
		if ((aemon != null) && aemon.isOpen()) {
			log.info("closing " + aemon);
			aemon.close();
		}

		if (aeServerSocket != null) {
			log.info("closing " + aeServerSocket);
			try {
				aeServerSocket.close();
			}
			catch (final IOException e) {
				log.warning(e.toString());
			}
		}
		if (unicastInput != null) {
			log.info("closing unicast input" + unicastInput);
			unicastInput.close();
		}
		if (unicastOutput != null) {
			log.info("closing unicastOutput " + unicastOutput);
			unicastOutput.close();
		}
		if (aeMulticastInput != null) {
			log.info("closing aeMulticastInput " + aeMulticastInput);
			aeMulticastInput.close();
		}
		if (aeMulticastOutput != null) {
			log.info("closing multicastOutput " + aeMulticastOutput);
			aeMulticastOutput.close();
		}
	}

	private int showMultipleInterfacesMessageCount = 0;

	/**
	 * If we are are the only viewer, automatically set
	 * interface to the hardware interface if there is only 1 of them and there
	 * is not already
	 * a hardware interface (e.g. StereoPairHardwareInterface which consists of
	 * two interfaces). otherwise force user choice.
	 */
	private void openHardwareIfNonambiguous() {
		// TODO doesn't open an AEMonitor if there is a ServoInterface plugged
		// in.
		// Should check to see if there is only 1 AEMonitorInterface, but this
		// check is not possible currently without
		// opening the interface.
		// HardwareInterfaceFactory.instance().buildInterfaceList(); // TODO
		// this burns up a lot of heap memory because
		// the PnpListeners
		// check to see if null interface required instead
		if (nullInterface) {
			return;
		}

		final int ninterfaces = HardwareInterfaceFactory.instance().getNumInterfacesAvailable();
		if (ninterfaces > 1) {
			if ((showMultipleInterfacesMessageCount++ % 100) == 0) {
				log.info("found " + ninterfaces + " hardware interfaces, choose one from Interface menu to connect");
			}
		}
		if ((jaerViewer != null) && (jaerViewer.getViewers().size() == 1) && (chip.getHardwareInterface() == null)
			&& (ninterfaces == 1)) {
			final HardwareInterface hw = HardwareInterfaceFactory.instance().getFirstAvailableInterface();
			// UDP interfaces should only be opened if the chip is a NetworkChip
			if (UDPInterface.class.isInstance(hw)) {
				if (NetworkChip.class.isInstance(chip)) {
					log.info("opening unambiguous network device");
					chip.setHardwareInterface(hw);
				}
			}
			else {
				if (!NetworkChip.class.isInstance(chip)) {
					log.info("setting hardware interface for unambiguous device to " + hw.toString());
					chip.setHardwareInterface(hw); // if blank cypress, returns
													// bare CypressFX2
				}
			}
		}
	}

	private ArrayList<String> chipClassNames;
	private ArrayList<Class> chipClasses;

	@SuppressWarnings("unchecked")
	void getChipClassPrefs() {
		// Deserialize from a byte array
		try {
			final byte[] bytes = AEViewer.prefs.getByteArray("chipClassNames", null);
			if (bytes != null) {
				final ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes));
				chipClassNames = (ArrayList<String>) in.readObject();
				in.close();
			}
			else {
				log.warning("building list of all AEChip classses - this takes some time. To reduce startup time, use AEChip/Customize to specify desired classes");
				makeDefaultChipClassNames();
			}
		}
		catch (final Exception e) {
			makeDefaultChipClassNames();
			putChipClassPrefs(); // added this to cache chip classes to speed
									// startup for subsequent launches
		}
	}

	private void makeDefaultChipClassNames() {
		chipClassNames = SubclassFinder.findSubclassesOf(AEChip.class.getName());
	}

	private void putChipClassPrefs() {
		try {
			// Serialize to a byte array
			final ByteArrayOutputStream bos = new ByteArrayOutputStream();
			final ObjectOutput out = new ObjectOutputStream(bos);
			out.writeObject(chipClassNames);
			out.close();

			// Get the bytes of the serialized object
			final byte[] buf = bos.toByteArray();
			AEViewer.prefs.putByteArray("chipClassNames", buf);
		}
		catch (final IOException e) {
			e.printStackTrace();
		}
		catch (final IllegalArgumentException e2) {
			log.warning("too many classes in Preferences, " + chipClassNames.size() + " class names");
		}
	}

	private static class FastClassFinder {

		static HashMap<String, Class> map = new HashMap<String, Class>();

		private static Class forName(final String name) throws ClassNotFoundException {
			Class c = null;
			if ((c = FastClassFinder.map.get(name)) == null) {
				c = Class.forName(name);
				FastClassFinder.map.put(name, c);
				return c;
			}
			else {
				return c;
			}
		}
	}

	private void buildDeviceMenu() {
		final ButtonGroup deviceGroup = new ButtonGroup();
		deviceMenu.removeAll();
		chipClasses = new ArrayList<Class>();
		deviceMenu.addSeparator();
		deviceMenu.add(customizeDevicesMenuItem);
		getChipClassPrefs();
		final ArrayList<String> notFoundClasses = new ArrayList<String>();
		for (final String deviceClassName : chipClassNames) {
			try {
				final Class c = FastClassFinder.forName(deviceClassName);
				chipClasses.add(c);
				final JRadioButtonMenuItem b = new JRadioButtonMenuItem(deviceClassName);
				deviceMenu.insert(b, deviceMenu.getItemCount() - 2);
				b.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(final ActionEvent evt) {
						try {
							final String name = evt.getActionCommand();
							final Class cl = FastClassFinder.forName(name);
							try {
								setCursor(new Cursor(Cursor.WAIT_CURSOR));
								setAeChipClass(cl);
							}
							finally {
								setCursor(Cursor.getDefaultCursor());
							}
						}
						catch (final Exception e) {
							e.printStackTrace();
						}
					}
				});
				deviceGroup.add(b);
			}
			catch (final ClassNotFoundException e) {
				log.warning("couldn't find device class " + e.getMessage() + ", removing from preferred classes");
				if (deviceClassName != null) {
					notFoundClasses.add(deviceClassName);
				}
			}
		}
		if (notFoundClasses.size() > 0) {
			chipClassNames.removeAll(notFoundClasses);
			putChipClassPrefs();
		}
		/*
		 * add scroll arrows to menu
		 * arguments are: items to show, scrolling interval,
		 * froozen items top, frozen items bottom
		 */
		MenuScroller.setScrollerFor(deviceMenu, 15, 100, 4, 2);
	}

	/**
	 * If the AEMonitor is open, tells it to resetTimestamps.
	 *
	 * @see AEMonitorInterface#resetTimestamps()
	 */
	public void zeroTimestamps() {
		if ((aemon != null) && aemon.isOpen()) {
			aemon.resetTimestamps();
		}
		firePropertyChange(AEViewer.EVENT_TIMESTAMPS_RESET, null, AEViewer.EVENT_TIMESTAMPS_RESET);
	}

	/**
	 * Gets the AEchip class from the internal aeChipClassName
	 *
	 * @return the AEChip subclass. DEFAULT_CHIP_CLASS is returned if there is
	 *         no stored preference.
	 */
	public Class getAeChipClass() {

		return aeChipClass;
	}

	private long lastTimeTitleSet = 0;
	PlayMode lastTitlePlayMode = null;

	/** this sets window title according to actual state */
	public void setTitleAccordingToState() {
		if ((lastTitlePlayMode == getPlayMode()) && ((System.currentTimeMillis() - lastTimeTitleSet) < 1000)) {
			return; // don't bother with this expenive window operation more
					// than 1/second
		}
		lastTimeTitleSet = System.currentTimeMillis();
		lastTitlePlayMode = getPlayMode();
		String ts = null;
		switch (getPlayMode()) {
			case LIVE:
				ts = "LIVE - " + getAeChipClass().getSimpleName() + " - " + aemon + " - AEViewer";
				break;
			case PLAYBACK:
				ts = "PLAYING - " + currentFile.getName() + " - " + getAeChipClass().getSimpleName() + " - AEViewer";
				break;
			case WAITING:
				ts = "WAITING - " + getAeChipClass().getSimpleName() + " - AEViewer";
				break;
			case SEQUENCING:
				ts = " LIVE SEQUENCE-MONITOR - " + getAeChipClass().getSimpleName() + " - " + aemon + " - AEViewer";
				break;
			case REMOTE:
				ts = "REMOTE - " + getAeChipClass().getSimpleName() + " - AEViewer";
				break;
			default:
				ts = "Unknown state";
		}
		final String fts = ts;
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				setTitle(fts);
			}
		});
	}

	/**
	 * Sets the device class, e.g. DVS127, from the
	 * fully qualified class name which is provided by the menu item itself.
	 *
	 * @param deviceClass
	 *            the Class of the AEChip to add to the AEChip menu
	 */
	public void setAeChipClass(final Class deviceClass) {
		// log.infox("AEViewer.setAeChipClass("+deviceClass+")");
		try {
			if (filterFrame != null) {
				filterFrame.dispose();
				filterFrame = null;
			}
			filterFrameBuilt = false;
			filtersToggleButton.setVisible(false);
			viewFiltersMenuItem.setEnabled(false);
			showBiasgen(false);
			cleanup(); // close sockets so they can be reused
			final Constructor<AEChip> constructor = deviceClass.getConstructor();
			if (constructor == null) {
				log.warning("null chip constructer, need to select valid chip class");
				return;
			}
			final AEChip oldChip = getChip();
			if (oldChip != null) {
				oldChip.onDeregistration();
				if ((oldChip.getCanvas() != null) && (oldChip.getCanvas().getDisplayMethod() != null)) {
					oldChip.getCanvas().getDisplayMethod().onDeregistration();
				}
			}
			if (getChip() == null) { // handle initial case
				constructChip(constructor);
			}
			else {
				synchronized (chip) { // TODO handle live case -- this is not
										// ideal thread programming - better to
										// sync
										// on a lock object in the run loop
					synchronized (extractor) {
						synchronized (getRenderer()) {
							getChip().cleanup();
							constructChip(constructor);
						}
					}
				}
			}
			if (chip == null) {
				log.warning("null chip, not continuing");
				return;
			}
			aeChipClass = deviceClass;
			setPreferredAEChipClass(aeChipClass);
			// chip constructed above, should have renderer already constructed
			// as well
			if ((chip.getRenderer() != null) && (chip.getRenderer() instanceof Calibratible)) {
				// begin added by Philipp
				// if (aeChipClass.renderer instanceof
				// AdaptiveIntensityRenderer){ // that does not work since the
				// renderer is obviously not defined before a chip gets
				// instanciated
				// if
				// (aeChipClass.getName().equals("no.uio.ifi.jaer.chip.foveated.UioFoveatedImager")
				// ||
				// aeChipClass.getName().equals("no.uio.ifi.jaer.chip.staticbiovis.UioStaticBioVis"))
				// {
				calibrationStartStop.setVisible(true);
				calibrationStartStop.setEnabled(true);
			}
			else {
				calibrationStartStop.setVisible(false);
				calibrationStartStop.setEnabled(false);
			}
			// end added by Philipp
			if (aemon != null) { // force reopen
				aemon.close();
			}
			makeCanvas();
			// setTitleAccordingToState(); // called anyhow in ViewLoop.run
			final Component[] devMenuComps = deviceMenu.getMenuComponents();
			for (final Component devMenuComp : devMenuComps) {
				if (devMenuComp instanceof JRadioButtonMenuItem) {
					final JMenuItem item = (JRadioButtonMenuItem) devMenuComp;
					if (item.getActionCommand().equals(aeChipClass.getName())) {
						item.setSelected(true);
						break;
					}
				}
			}
			fixLoggingControls();
			filterChain = chip.getFilterChain();
			if (filterChain == null) {
				filtersToggleButton.setVisible(false);
				viewFiltersMenuItem.setEnabled(false);
			}
			else {
				filterChain.reset();
				filtersToggleButton.setVisible(true);
				viewFiltersMenuItem.setEnabled(true);
			}
			final HardwareInterface hw = chip.getHardwareInterface();
			if (hw != null) {
				log.info("setting hardware interface of " + chip + " to " + hw);
				aemon = (AEMonitorInterface) hw;
			}

			showFilters(enableFiltersOnStartup);
			if (enableFiltersOnStartup) {
				getFilterFrame().setState(Frame.ICONIFIED); // set the filter
															// frame iconified
															// at first (but
															// open) so
															// that it doesn't
															// obscure view
			} // fix selected radio button for chip class
			if (deviceMenu.getItemCount() == 0) {
				log.warning("tried to select device in menu but no device menu has been built yet");
			}
			for (int i = 0; i < deviceMenu.getItemCount(); i++) {
				final JMenuItem m = deviceMenu.getItem(i);
				if ((m != null) && (m instanceof JRadioButtonMenuItem) && (m.getText() == aeChipClass.getName())) {
					m.setSelected(true);
					break;
				}
			}
			// getSupport().firePropertyChange("chip", oldChip, getChip());
			firePropertyChange(AEViewer.EVENT_CHIP, oldChip, getChip());

			chip.onRegistration();

		}
		catch (final Exception e) {
			e.printStackTrace();
		}
	}

	private void constructChip(final Constructor<AEChip> constructor) {
		try {
			// System.out.println("AEViewer.constructChip(): constructing chip with constructor "+constructor);
			setChip(constructor.newInstance((java.lang.Object[]) null));

		}
		catch (final Exception e) {
			log.warning("AEViewer.constructChip exception " + e.getMessage());
			e.printStackTrace();
		}
	}

	synchronized void makeCanvas() {
		if (chipCanvas != null) {
			getImagePanel().remove(chipCanvas.getCanvas());
		}
		if (chip == null) {
			log.warning("null chip, not making canvas");
			return;
		}
		chipCanvas = chip.getCanvas();
		getImagePanel().add(chipCanvas.getCanvas(), BorderLayout.CENTER);

		// chipCanvas.getCanvas().invalidate();
		// find display menu reference and fill it with display menu for this
		// canvas
		viewMenu.remove(displayMethodMenu);
		viewMenu.add(chipCanvas.getDisplayMethodMenu());
		displayMethodMenu = chipCanvas.getDisplayMethodMenu();
		viewMenu.invalidate();

		// validate();
		// pack();
		// causes a lot of flashing ...
		// Toolkit.getDefaultToolkit().setDynamicLayout(true); // dynamic
		// resizing -- see
		// if this bombs!
	}

	/**
	 * This method sets the "current file" which sets the field, the preferences
	 * of the last file, and the window title.
	 * It does not
	 * actually start playing the file.
	 */
	protected void setCurrentFile(final File f) {
		currentFile = new File(f.getPath());
		lastFile = currentFile;
		AEViewer.prefs.put("AEViewer.lastFile", lastFile.toString());
		// System.out.println("putString AEViewer.lastFile="+lastFile);
		setTitleAccordingToState();
	}

	/**
	 * If the AEViewer is playing (or has played) a file, then this method
	 * returns it.
	 *
	 * @return the File
	 * @see PlayMode
	 */
	public File getCurrentFile() {
		return currentFile;
	}

	private synchronized void buildInterfaceMenu() {
		buildInterfaceMenu(interfaceMenu);
	}

	/**
	 * Builds list of attached hardware interfaces by asking the
	 * hardware interface factories for the interfaces. Populates the Interface
	 * menu with these items,
	 * and with a "None" item to close and set the chip's HardwareInterface to
	 * null.
	 * Various specialized interfaces customize the code below.
	 */
	synchronized void buildInterfaceMenu(final JMenu interfaceMenu) {
		final ButtonGroup bg = new ButtonGroup();
		interfaceMenu.removeAll();

		// create a list of available hardware interfaces from enumerated
		// devices
		final int n = HardwareInterfaceFactory.instance().getNumInterfacesAvailable(); // TODO
																						// this
																						// rebuilds
																						// the
																						// entire
																						// list
		JRadioButtonMenuItem interfaceButton = null;
		for (int i = 0; i < n; i++) {
			final HardwareInterface hw = HardwareInterfaceFactory.instance().getInterface(i);
			if (hw == null) {
				continue;
			} // in case it disappeared

			if ((!UDPInterface.class.isInstance(hw) && !NetworkChip.class.isInstance(chip))
				|| (UDPInterface.class.isInstance(hw) && NetworkChip.class.isInstance(chip))) {
				// if the chip is a normal AEChip with regular (not network)
				// hardware interface, and the interface is
				// not a network interface,
				// then add a menu item to select this interface.
				interfaceButton = new JRadioButtonMenuItem(hw.getClass().getSimpleName());
				interfaceButton.putClientProperty("HardwareInterfaceNumber", new Integer(i));
				interfaceMenu.add(interfaceButton);
				bg.add(interfaceButton);
				interfaceButton.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(final ActionEvent evt) {
						final JComponent comp = (JComponent) evt.getSource();
						final int interfaceNumber = (Integer) comp.getClientProperty("HardwareInterfaceNumber");
						final HardwareInterface hw = HardwareInterfaceFactory.instance().getInterface(interfaceNumber);
						// only select an interface if it is not the same as
						// already selected
						if (((hw != null) && (chip != null) && (chip.getHardwareInterface() == null))
							|| !hw.toString().equals(chip.getHardwareInterface().toString())) {
							// close interface on chip if there is one and it's
							// open
							if ((chip.getHardwareInterface() != null) && chip.getHardwareInterface().isOpen()) {
								log.info("closing " + chip.getHardwareInterface().toString());
								chip.getHardwareInterface().close();
							}
							log.info("selected interface " + evt.getActionCommand() + " with HardwareInterface number"
								+ interfaceNumber + " which is " + hw);
							chip.setHardwareInterface(hw);
						}
					}
				});
				// if(chip!=null && chip.getHardwareInterface()==hw)
				// b.setSelected(true);
				// sb.append("\n").append(hw.toString());
			}
		}
		boolean addedSep = false;
		// now make items for HardwareInterfaceFactoryChooserDialog factories
		// these HardwareInterfaceFactories allow choice of multiple alternative
		// interfaces, e.g. for a serial port or
		// network interface
		for (final Class c : HardwareInterfaceFactory.factories) {
			if (HardwareInterfaceFactoryChooserDialog.class.isAssignableFrom(c)) {
				// log.log(Level.INFO, "found hardware chooser class {0}", c);
				if (!addedSep) {
					interfaceMenu.add(new JSeparator());
					addedSep = true;
				}
				try {
					final Method m = (c.getMethod("instance")); // get singleton
																// instance of
																// factory
					final HardwareInterfaceFactoryChooserDialog inst = (HardwareInterfaceFactoryChooserDialog) m
						.invoke(c);
					final JRadioButtonMenuItem mi = new JRadioButtonMenuItem(inst.getName());
					mi.setToolTipText("Shows a chooser dialog for making this type of HardwareInterface");
					interfaceMenu.add(mi);
					bg.add(mi);
					mi.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(final ActionEvent e) {
							final JDialog fac = inst.getInterfaceChooser(chip);
							fac.setVisible(true);
							if (inst.getChosenHardwareInterface() != null) {
								// close interface on chip if there is one and
								// it's open
								if (chip.getHardwareInterface() != null) {
									log.info("before opening new interface, closing "
										+ chip.getHardwareInterface().toString());
									chip.getHardwareInterface().close();
									aemon = null; // TODO aemon is a bad hack
								}
								final HardwareInterface hw = inst.getChosenHardwareInterface();
								log.info("setting new interface " + hw);
								chip.setHardwareInterface(hw);
								if (e.getSource() instanceof JMenuItem) {
									final JMenuItem item = (JMenuItem) e.getSource();
									item.setSelected(true); // doesn't work
															// because menu is
															// contantly rebuilt
															// TODO
								}
							}
						}
					});
				}
				catch (final Exception e) {
					log.warning(c + " threw Exception when trying to get HardwareInterfaceChooserFactory: "
						+ e.toString());
					e.printStackTrace();
				}

			}
		}
		// make a 'none' item (only there is no interface) // TOTO tobi changed
		// to always make one
		final JRadioButtonMenuItem noneInterfaceButton = new JRadioButtonMenuItem("None");
		noneInterfaceButton.putClientProperty("HardwareInterface", null);
		interfaceMenu.add(new JSeparator());
		interfaceMenu.add(noneInterfaceButton);
		bg.add(noneInterfaceButton);
		noneInterfaceButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent evt) {
				// log.info("selected null interface");
				if (chip.getHardwareInterface() != null) {
					chip.getHardwareInterface().close();
				}
				chip.setHardwareInterface(null);
				// force null interface
				nullInterface = true;
			}
		});
		interfaceMenu.add(new JSeparator());
		noneInterfaceButton.setSelected(true);
		if ((chip != null) && (chip.getHardwareInterface() != null)) {
			final String chipInterfaceClass = chip.getHardwareInterface().getClass().getSimpleName();
			// System.out.println("chipInterface="+chipInterface);
			for (final Component c : interfaceMenu.getMenuComponents()) {
				if (!(c instanceof JMenuItem)) {
					continue;
				}
				final JMenuItem item = (JMenuItem) c;
				// set the button on for the actual interface of the chip if
				// there is one already
				// if (chipInterface != null) {
				// //
				// log.info("chip.getHardwareInterface="+chip.getHardwareInterface());
				// }
				// if (hw != null) {
				// // log.info("hw="+hw);
				// }
				// check if device in menu is already one assigned to this chip,
				// by String comparison. Checking by
				// object doesn't work because
				// new device objects are created by HardwareInterfaceFactory's'
				// System.out.println("item.getText()="+item.getText());
				if (item.getText().equals(chipInterfaceClass)) {
					item.setSelected(true);
					// normal interface selected
					nullInterface = false;
				}
			}
		}
		// log.info(sb.toString());

		// TODO add menu item for choosers for things that cannot be easily
		// enumerated like serial port devices, e.g.
		// where enumeration is very expensive because

	}

	void fixBiasgenControls() {

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {// // debug
				// biasesToggleButton.setEnabled(true);
				// biasesToggleButton.setVisible(true);
				// viewBiasesMenuItem.setEnabled(true);
				if (chip == null) {
					return;
				}
				if (chip.getBiasgen() == null) {
					log.info("setting hardware config / biasgen buttons false");
					biasesToggleButton.setEnabled(false);
					biasesToggleButton.setVisible(false);
					viewBiasesMenuItem.setEnabled(false);
					return;
				}
				else {
					biasesToggleButton.setEnabled(true);
					biasesToggleButton.setVisible(true);
					viewBiasesMenuItem.setEnabled(true);
				}
				if (biasgenFrame != null) {
					final boolean vis = biasgenFrame.isVisible();
					biasesToggleButton.setSelected(vis);
				}
			}
		});
	}

	// nulls out all hardware interfaces to start fresh
	private void nullifyHardware() {
		aemon = null; // if device is blank a bare interface may have been
						// constructed and we must ensure the deivce is
						// reinstantiated after programming
		if (chip != null) {
			chip.setHardwareInterface(null); // should set chip's biasgen to
												// null also
			// if(chip.getBiasgen()!=null)
			// chip.getBiasgen().setHardwareInterface(null);
		}
	}

	/**
	 * Tries to open the AE interface.
	 *
	 */
	private void openAEMonitor() {
		synchronized (viewLoop) { // TODO grabs lock on viewLoop so that other
									// methods, e.g. startPlayback, which also
									// grab this lock, will not race to set
									// playMode. touchy design.
			if (getPlayMode() == PlayMode.PLAYBACK) { // don't open hardware if
														// playing a file
				return;
			}
			if ((aemon != null) && aemon.isOpen()) {
				if (getPlayMode() != PlayMode.SEQUENCING) {
					// log.info("Play mode: Live");
					setPlayMode(PlayMode.LIVE);
				}
				// playMode=PlayMode.LIVE; // in case (like
				// StereoPairHardwareInterface) where device can be open but
				// not by AEViewer
				return;
			}
			try {
				openHardwareIfNonambiguous();
				// openHardwareIfNonambiguous will set chip's hardware
				// interface, here we store local reference
				// if it's an aemon, then its an event monitor
				if ((chip.getHardwareInterface() != null)
					&& (chip.getHardwareInterface() instanceof AEMonitorInterface)) {
					aemon = (AEMonitorInterface) chip.getHardwareInterface();
					if ((aemon == null) || !(aemon instanceof AEMonitorInterface)) {
						fixDeviceControlMenuItems();
						fixLoggingControls();
						fixBiasgenControls();
						return;
					}

					aemon.setChip(chip);
					aemon.open(); // will throw BlankDeviceException if device
									// is blank.
					fixLoggingControls();
					fixBiasgenControls();
					fixDeviceControlMenuItems();
					tickUs = aemon.getTimestampTickUs();
					// note it is important that this openAEMonitor succeeed
					// BEFORE aemon is assigned to biasgen,
					// which immeiately tries to openAEMonitor and download
					// biases, creating a storm of complaints if
					// not sucessful!

					if (aemon instanceof BiasgenHardwareInterface) {
						chip.getBiasgen();
						if ((chip.getBiasgen() != null) && !chip.getBiasgen().isInitialized()) {
							chip.getBiasgen().showUnitializedBiasesWarningDialog(this);
						}
						// TODO tobi commented out this redundant call , must
						// check that devices still work correctly
						// if (bg == null) {
						// log.warning(chip +
						// " is BiasgenHardwareInterface but has null biasgen object, not setting biases");
						// } else {
						// chip.getBiasgen().sendConfiguration(chip.getBiasgen());
						// }
					}

					if ((chip.getHardwareInterface() != null)
						&& (chip.getHardwareInterface() instanceof AESequencerInterface)) {
						// the 'chip's' hardware interface is a pure sequencer
						enableMonSeqMenu(true);
					}
					if (getPlayMode() != PlayMode.SEQUENCING) {
						setPlayMode(PlayMode.LIVE);
					}
					// TODO interface should do this check nonmonotonic
					// timestamps automatically
					if ((aemon != null) && (aemon instanceof StereoPairHardwareInterface)) {
						((StereoPairHardwareInterface) aemon)
							.setIgnoreTimestampNonmonotonicity(!checkNonMonotonicTimeExceptionsEnabledCheckBoxMenuItem
								.isSelected());
					}
				}
				else if ((chip.getHardwareInterface() != null)
					&& (chip.getHardwareInterface() instanceof AESequencerInterface)) {
					// the 'chip's' hardware interface is a pure sequencer
					enableMonSeqMenu(true);
				}
				// setPlaybackControlsEnabledState(true); // TODO why set this
				// true here? commented out

			}
			catch (final BlankDeviceException bd) {
				if (!blankDeviceMessageShown) {
					log.info(bd.getMessage() + " suppressing further blank device messages");
					blankDeviceMessageShown = true;
					final int v = JOptionPane
						.showConfirmDialog(
							this,
							"<html>Blank Cypress FX2 found ("
								+ aemon
								+ "). <br>Caught exception "
								+ bd.getMessage()
								+ ".<br>Do you want to open the Cypress FX2 Programming utility?<p>Otherwise set the default firmware in the USB menu to download desired firmware to RAM for CypressFX2 devices");

					if (v == JOptionPane.YES_OPTION) {
						if (aemon instanceof CypressFX2) {
							final CypressFX2EEPROM instance = new CypressFX2EEPROM();
							instance.setExitOnCloseEnabled(false);
							instance.setVisible(true);
						}
						else {
							final net.sf.jaer.hardwareinterface.usb.cypressfx2libusb.CypressFX2EEPROM instance = new net.sf.jaer.hardwareinterface.usb.cypressfx2libusb.CypressFX2EEPROM();
							instance.setExitOnCloseEnabled(false);
							instance.setVisible(true);
						}
					}
				}
				log.warning(bd.toString());
				aemon.close();
				nullifyHardware();

			}
			catch (final Exception e) {
				log.warning(e.toString());
				e.printStackTrace();
				if (aemon != null) {
					log.info("closing Monitor" + aemon);
					aemon.close();
				}
				nullifyHardware();
				setPlaybackControlsEnabledState(false);
				fixDeviceControlMenuItems();
				fixLoggingControls();
				fixBiasgenControls();
				setPlayMode(PlayMode.WAITING);
			}
			fixDeviceControlMenuItems();
		}
	}

	void setPlaybackControlsEnabledState(final boolean yes) {
		// log.info("*****************************************************       setting playback controls enabled = "+yes);
		loggingButton.setEnabled(yes);
		biasesToggleButton.setEnabled(yes);
		closeMenuItem.setEnabled(yes);
		increasePlaybackSpeedMenuItem.setEnabled(yes);
		decreasePlaybackSpeedMenuItem.setEnabled(yes);
		rewindPlaybackMenuItem.setEnabled(yes);
		flextimePlaybackEnabledCheckBoxMenuItem.setEnabled(yes);
		togglePlaybackDirectionMenuItem.setEnabled(yes);
		toggleMarkCheckBoxMenuItem.setEnabled(yes);
		// if ( !playerControlPanel.isVisible() ){ // TODO why only do this if
		// not visible?
		playerControlPanel.setVisible(yes);
		// }
	}

	// volatile boolean stop=false; // volatile because multiple threads will
	// access
	int renderCount = 0;
	int numEvents;
	AEPacketRaw aeRaw;
	// AEPacket2D ae;
	private EventPacket packet;
	// EventPacket packetFiltered;
	boolean skipRender = false;
	// volatile private boolean paused=false; // multiple threads will access
	boolean overrunOccurred = false;
	int tickUs = 1;
	public AEPlayer aePlayer = new AEPlayer(this, this);
	int noEventCounter = 0;

	/**
	 * This thread isthe main animation loop that acquires events and renders
	 * them to the canvas for active rendering.
	 * The other components render themselves
	 * on the usual Swing rendering thread.
	 */
	class ViewLoop extends Thread {
		volatile boolean singleStepEnabled = false, doSingleStep = false;
		int numRawEvents, numFilteredEvents;

		// volatile boolean rerenderFlagDone=false; // used by view loop to
		// signal to other methods that it has finished
		// a rendering. view loop sets this true after each loop.

		public ViewLoop() {
			super();
			setName("AEViewer.ViewLoop");
		}

		private void renderPacket(final EventPacket ae) {
			if (aePlayer.isChoosingFile() || (ae == null) || (!isRenderBlankFramesEnabled() && (ae.getSize() == 0))) {
				return;
			} // don't render while filechooser is active
			getRenderer().isSubsamplingEnabled();
			// if (isPaused()) {
			// getRenderer().setSubsamplingEnabled(false);
			// } // not needed and always overwrites the preference value
			if (!(getRenderer().isAccumulateEnabled() && isPaused())) {
				getRenderer().render(packet);
			}
			// if (isPaused()) {
			// getRenderer().setSubsamplingEnabled(subsamplingEnabled);
			// }
			// if(renderImageEnabled) {
			if (isActiveRenderingEnabled()) {
				chipCanvas.paintFrame(); // actively paint frame with OpenGL
			}
			else {
				// log.info("repaint by "+1000/frameRater.getDesiredFPS()+" ms");
				chipCanvas.repaint();
				// chipCanvas.repaint(1000 / frameRater.getDesiredFPS()); // ask
				// for repaint within frame time
			}

		} // renderEvents

		private EventPacket extractPacket(final AEPacketRaw aeRaw) {
			final boolean subsamplingEnabled = getRenderer().isSubsamplingEnabled();
			if (isPaused()) {
				extractor.setSubsamplingEnabled(false);
			}
			final EventPacket packet = extractor.extractPacket(aeRaw);
			packet.setRawPacket(aeRaw);
			if (isPaused()) {
				extractor.setSubsamplingEnabled(subsamplingEnabled);
			}

			return packet;
		}

		private final EngineeringFormat engFmt = new EngineeringFormat();
		volatile boolean stop = false;

		/**
		 * Sets the stop flag so that the ViewLoop exits the run method on the
		 * next iteration.
		 *
		 */
		public void stopThread() {
			stop = true;
		}

		/**
		 * The main loop of AEViewer - this is the 'game loop' of the program.
		 */
		@Override
		public void run() { // don't know why this needs to be thread-safe
			/*
			 * TODO synchronized tobi removed sync because it was causing
			 * deadlocks on exit.
			 */

			// BasicEvent lastEvent=new BasicEvent();

			while (!isVisible() && !globalized) {
				try {
					log.info("sleeping until isVisible()==true");
					Thread.sleep(1000); // sleep to let components realize on
										// screen - may be crashing opengl on
										// nvidia
										// drivers if we draw to unrealized
										// components
				}
				catch (final InterruptedException e) {
				}
			}
			while (stop == false/* && !isInterrupted() */) { // the only way to
																// break out of
																// the run loop
																// is either
																// setting stop
																// true or by
																// some uncaught
																// exception.
				// now getString the data to be displayed
				if (!isPaused() || (isSingleStep() && !isInterrupted())) { // we
																			// check
																			// interrupted
																			// to
																			// make
																			// sure
																			// we
																			// are
																			// not
																			// getting
																			// data
																			// after
																			// being
																			// interrupted
					// if(isSingleStep()){
					// log.info("getting data for single step");
					// }
					// if !paused we always get data. below, if
					// singleStepEnabled, we set paused after getting data.
					// when the user unpauses via menu, we disable
					// singleStepEnabled
					// another flag, doSingleStep, tells loop to do a single
					// data acquisition and then pause again
					// in this branch, getString new data to show
					getFrameRater().takeBefore();

					// Grab input from one of various sources
					boolean skipTheRest = grabInput();

					// If there's nothing to do:
					if (skipTheRest) {
						continue;
					}

					numRawEvents = aeRaw.getNumEvents();

					// new style packet with reused event objects
					// if(aeRaw.getNumEvents()>0){ // we should always extract
					// even if the packet is empty to be sure we
					// get a valid packet!

					packet = extractPacket(aeRaw);
					numEvents = packet.getSize();

					// synchronized(packet.class()){}

					// System.out.println(packet.getEventClass());

					// Monotonicity test code
					// for (int i = 0; i < packet.getSize(); i++) {
					// BasicEvent currentEvent = packet.getEvent(i);
					// if ((lastEvent != null) && (currentEvent.timestamp <
					// lastEvent.timestamp)) {
					// System.out.println("TimeStamp Nonmonotonicity: "+currentEvent.timestamp+" < "+
					// lastEvent.timestamp);
					// }
					// lastEvent.copyFrom(currentEvent);
					// }

					// Filter Packet (unless part of global viewer, in which
					// case it's done later)
					if (!globalized) {
						skipTheRest = filterPacket();
						if (skipTheRest) {
							continue;
						}
					}
					numFilteredEvents = packet.getSize();

					chip.setLastData(packet);// set the rendered data for use by
												// various methods

					// if we are logging data to disk do it here
					if (loggingEnabled) {
						logPacket();
					}

					// Write the ouput to whatever streams need it
					final boolean breakout = writeOutputStreams();
					if (breakout) {
						break;
					}

					singleStepDone();
					// if (packet.isEmpty() && !isRenderBlankFramesEnabled()) {
					// // log.info("blank frame, not rendering it");
					// fpsDelay();
					// continue;
					// }

					// Don't know what this is for
					if (numEvents == 0) {
						noEventCounter++;
					}
					else {
						noEventCounter = 0;
					}

				} // if (!isPaused() || isSingleStep())

				if (skipPacketsRenderingCount-- == 0) {
					// we only got new events if we were NOT paused. but now we
					// can apply filters, different rendering
					// methods, etc in 'paused' condition
					makeStatisticsLabel(packet);
					renderPacket(packet);
					skipPacketsRenderingCount = skipPacketsRenderingNumber;
				}
				getFrameRater().takeAfter();
				renderCount++;

				// --------------------------------------------------------
				// Peter's addition: Enable write to global

				if (globalized) {

					try {
						waitFlag.await();

					}
					catch (final InterruptedException ex) {
						Logger.getLogger(AEViewer.class.getName()).log(Level.SEVERE, null, ex);
					}
					catch (final BrokenBarrierException ex) {
						Logger.getLogger(AEViewer.class.getName()).log(Level.SEVERE, null, ex);
					}

				}

				// --------------------------------------------------------
				// System.out.println("Viewer Loop end");
				fpsDelay();

				// rerenderFlagDone=true;
			} // while (stop == false): end of run() loop - main loop of
				// AEViewer.ViewLoop

			// Loop Cleanup
			log.info("AEViewer.run() ending: stop=" + stop + " isInterrupted=" + isInterrupted());
			if (aemon != null) {
				aemon.close();
			}
			if (unicastOutput != null) {
				unicastOutput.close();
			}
			if (unicastInput != null) {
				unicastInput.close();
			}

			// <editor-fold desc="Some Vestigal Organs.">
			// if(windowSaver!=null)
			// try {
			// windowSaver.saveSettings();
			// } catch (IOException e) {
			// e.printStackTrace();
			// }

			// chipCanvas.getCanvas().setVisible(false);
			// remove(chipCanvas.getCanvas());
			// if (getJaerViewer() != null) {
			// log.info("removing " + AEViewer.this +
			// " viewer from caviar viewer list");
			// getJaerViewer().removeViewer(AEViewer.this); // we want to remove
			// the viewer, not this inner class
			// }
			// // dispose();
			//
			// if (getJaerViewer() == null ||
			// getJaerViewer().getViewers().isEmpty()) {
			// if (biasgenFrame != null && biasgenFrame.isModificationsSaved())
			// {
			// System.exit(0);
			// }
			// }
			// </editor-fold>

		} // viewLoop.run()

		/**
		 * Grab the input.
		 *
		 * @return returns true if the code should continue from here and skip
		 *         future processing
		 */
		boolean grabInput() {

			switch (getPlayMode()) {
				case SEQUENCING:
					final HardwareInterface chipHardwareInterface = chip.getHardwareInterface();

					if (chipHardwareInterface == null) {
						log.warning("AE monitor/sequencer became null while sequencing");
						setPlayMode(PlayMode.WAITING);
						break;
					}
					final AESequencerInterface aemonseq = (AESequencerInterface) chip.getHardwareInterface();
					final int nToSend = aemonseq.getNumEventsToSend();
					int position = 0;
					if (nToSend != 0) {
						position = (int) ((playerControls.getPlayerSlider().getMaximum() * (float) aemonseq
							.getNumEventsSent()) / nToSend);
					}

					sliderDontProcess = true;
					playerControls.getPlayerSlider().setValue(position);
					if (!(chip.getHardwareInterface() instanceof AEMonitorInterface)) {
						return true; // if we're a monitor plus sequencer than
										// go on to monitor events, otherwise
										// break
										// out since there are no events to
										// monitor
					}
				case LIVE:
					openAEMonitor();
					if ((aemon == null) || !aemon.isOpen()) {
						setPlayMode(PlayMode.WAITING);
						try {
							Thread.sleep(300);
						}
						catch (final InterruptedException e) {
							log.warning("LIVE openAEMonitor sleep interrupted");
						}
						return true;
					}
					overrunOccurred = aemon.overrunOccurred();
					try {
						// try to getString an event to avoid rendering empty
						// (black) frames
						// int triesLeft = 15;
						// do {
						// if (!isInterrupted()) {
						aemon = (AEMonitorInterface) chip.getHardwareInterface(); // TODOkeep
																					// setting
																					// aemon
																					// to
																					// be
																					// chip's
																					// interface,
																					// this
																					// is
																					// kludge
						if (aemon == null) {
							log.warning("AEViewer.ViewLoop.run(): AEMonitorInterface became null during acquisition");
							throw new HardwareInterfaceException("hardware interface became null");
						}
						aeRaw = aemon.acquireAvailableEventsFromDriver();
						// System.out.println("got "+aeRaw);
						// }

						// if (aeRaw.getNumEvents() > 0) {
						// break;
						// }
						// System.out.print("."); System.out.flush();
						// try {
						// Thread.currentThread().sleep(3);
						// } catch (InterruptedException e) {
						// log.warning("LIVE attempt to getString data loop interrupted");
						// }
						// } while (triesLeft-- > 0);
						// // if(aeRaw.getNumEvents()==0)
						// {System.out.print("0 events ...");
						// System.out.flush();}

					}
					catch (final HardwareInterfaceException e) {
						if (stop) {
							break; // break out of loop if this aquisition
									// thread got HardwareInterfaceException
									// because
									// we are exiting
						}
						setPlayMode(PlayMode.WAITING);
						log.warning("while acquiring data caught " + e.toString());
						if (aemon != null) {
							aemon.close(); // TODO check if this is OK -tobi
						}// e.printStackTrace();
						nullifyHardware();

						return true;
					}
					catch (final ClassCastException cce) {
						setPlayMode(PlayMode.WAITING);
						log.warning("Interface changed out from under us: " + cce.toString());
						cce.printStackTrace();
						nullifyHardware();
						return true;
					}
					break;
				case PLAYBACK:
					// Thread thisThread=Thread.currentThread();
					// System.out.println("thread "+thisThread+" getting events for renderCount="+renderCount);
					aeRaw = getAePlayer().getNextPacket(aePlayer);
					getAePlayer().adjustTimesliceForRealtimePlayback();
					// System.out.println("."); System.out.flush();
					break;
				case REMOTE:
					if (unicastInputEnabled) {
						if (unicastInput == null) {
							log.warning("null unicastInput, going to WAITING state");
							setPlayMode(PlayMode.WAITING);
						}
						else {
							aeRaw = unicastInput.readPacket(); // TODO should
																// throw
																// interruptedexception
						}

					}
					if (socketInputEnabled) {
						if (getAeSocket() == null) {
							log.warning("null socketInputStream, going to WAITING state");
							setPlayMode(PlayMode.WAITING);
							socketInputEnabled = false;
						}
						else {
							try {
								aeRaw = getAeSocket().readPacket(); // reads a
																	// packet if
																	// there is
																	// data
																	// available
																	// // TODO
																	// should
																	// throw
																	// interrupted
																	// excpetion
							}
							catch (final IOException e) {
								if (stop) {
									break;
								}
								log.warning(e.toString() + ": closing and reconnecting...");
								try {
									getAeSocket().close();
									aeSocket = new AESocket(); // uses last
																// values stored
																// in
																// preferences
									aeSocket.connect();
									log.info("connected " + aeSocket);
								}
								catch (final IOException ex3) {
									log.warning(ex3 + ": failed reconnection, sleeping 1 s before trying again");
									try {
										Thread.sleep(1000);
									}
									catch (final InterruptedException ex2) {
									}
								}
							}
						}
					}

					if (multicastInputEnabled) {
						if (aeMulticastInput == null) {
							log.warning("null aeMulticastInput, going to WAITING state");
							setPlayMode(PlayMode.WAITING);
						}
						else {
							aeRaw = aeMulticastInput.readPacket();
						}
					}
					if (blockingQueueInputEnabled) {
						if (getBlockingQueueInput() == null) {
							log.warning("null blockingQueueInput, going to WAITING state");
							setPlayMode(PlayMode.WAITING);
						}
						else {
							// try {
							// aeRaw = (AEPacketRaw)
							// getBlockingQueueInput().take();
							// } catch (InterruptedException ex) {
							// Logger.getLogger(AEViewer.class.getName()).log(Level.SEVERE,
							// null, ex);
							// }
							final Collection<AEPacketRaw> tempPackets = new ArrayList<AEPacketRaw>();
							getBlockingQueueInput().drainTo(tempPackets);
							for (final AEPacketRaw packet : tempPackets) {
								if (packet.getNumEvents() != 0) {
									if ((packet.addresses[0] & 0x8000) == 0) {
									}
									else {
									}
								}
							}
							// log.info(String.format("remote received %d cochlea and %d retina packets.",numOfCochleaPackets,numOfRetinaPackets));
							aeRaw = new AEPacketRaw(tempPackets);

						}
					}
					break;
				case WAITING:
					// notify(); // notify waiter on this thread that we have
					// gone to WAITING state
					if (unicastInputEnabled || multicastInputEnabled || socketInputEnabled) {
						// if were were playing back a recording and a remote
						// interface is active, then we go back to it
						// here.
						setPlayMode(PlayMode.REMOTE);
						break;
					}
					openAEMonitor();
					if ((aemon == null) || !aemon.isOpen()) {
						statisticsLabel.setText("Choose desired HardwareInterface from Interface menu");
						// setPlayMode(PlayMode.WAITING); // we don't need to
						// set it again

						try {
							Thread.sleep(600);
						}
						catch (final InterruptedException e) {
							log.info("WAITING interrupted");
						}
						return true;
					}
			} // playMode switch to getString data

			// If input is null, delay and continue
			if (aeRaw == null) {
				fpsDelay();
				return true;
			}

			return false;
		}

		/**
		 * Filters packet through processing chain if ProcessingMode is
		 * RENDERING or LIVE.
		 * If any filter throws an exception, all filters are disabled.
		 *
		 * @return true if packet is null, otherwise false.
		 */
		boolean filterPacket() {

			// filter events, do processing on them in rendering loop here
			if ((filterChain.getProcessingMode() == FilterChain.ProcessingMode.RENDERING)
				|| (playMode != PlayMode.LIVE)) {
				try {
					packet = filterChain.filterPacket(packet);
				}
				catch (final Exception e) {
					log.warning("Caught " + e + ", disabling all filters. See following stack trace.");
					log.log(Level.WARNING, "Filter exception", e);
					for (final EventFilter f : filterChain) {
						f.setFilterEnabled(false);
					}
				}
				if (packet == null) {
					// log.warning("null packet after filtering");
					return true;
				}
			}
			return false;
		}

		void logPacket() {
			synchronized (loggingOutputStream) {
				try {
					if (!isLogFilteredEventsEnabled()) {
						loggingOutputStream.writePacket(aeRaw); // log all
																// events
					}
					else {
						// log the reconstructed packet after filtering
						final AEPacketRaw aeRawRecon = extractor.reconstructRawPacket(packet);
						loggingOutputStream.writePacket(aeRawRecon);
					}
				}
				catch (final IOException e) {
					e.printStackTrace();
					loggingEnabled = false;
					try {
						loggingOutputStream.close();
					}
					catch (final IOException e2) {
						e2.printStackTrace();
					}
				}
			}
			if (loggingTimeLimit > 0) { // we may have a defined time for
										// logging, if so, check here and abort
										// logging
				if ((System.currentTimeMillis() - loggingStartTime) > loggingTimeLimit) {
					log.info("logging time limit reached, stopping logging");
					try {
						SwingUtilities.invokeAndWait(new Runnable() {

							@Override
							public void run() {
								stopLogging(true); // must run this in AWT
													// thread because it messes
													// with file menu
							}
						});
					}
					catch (final Exception e) {
						e.printStackTrace();
					}
				}
			}
		}

		/**
		 * Write data to output streams
		 * Returns true if the run loop should break.
		 *
		 * @return
		 */
		boolean writeOutputStreams() {
			// write to network socket if a client has opened a socket to us
			// we serve up events on this socket

			if ((getAeServerSocket() != null) && (getAeServerSocket().getAESocket() != null)) {
				final AESocket s = getAeServerSocket().getAESocket();
				try {
					if (!isLogFilteredEventsEnabled()) {
						s.writePacket(aeRaw);
					}
					else {
						// send the reconstructed packet after filtering
						final AEPacketRaw aeRawRecon = extractor.reconstructRawPacket(packet);
						s.writePacket(aeRawRecon);
					}
				}
				catch (final IOException e) {
					// e.printStackTrace();
					log.warning("sending packet " + aeRaw + " from " + this + " to " + s + " failed, closing socket");
					try {
						s.close();
					}
					catch (final IOException e2) {
						e2.printStackTrace();
					}
					finally {
						getAeServerSocket().setSocket(null);
					}
				}
			}

			if (socketOutputEnabled) {
				if (getAeSocketClient() == null) {
					log.warning("null socketInputStream, going to WAITING state");
					setPlayMode(PlayMode.WAITING);
					socketOutputEnabled = false;
				}
				else {
					try {
						if (!isLogFilteredEventsEnabled()) {
							getAeSocketClient().writePacket(aeRaw);
						}
						else {
							// send the reconstructed packet after filtering
							final AEPacketRaw aeRawRecon = extractor.reconstructRawPacket(packet);
							getAeSocketClient().writePacket(aeRawRecon);
						}
						// reads a packet if there is data available // TODO
						// should throw interrupted excpetion
					}
					catch (final IOException e) {
						if (stop) {
							return true;
						}
						log.warning(e.toString() + ": closing and reconnecting...");
						try {
							getAeSocketClient().close();
							aeSocketClient = new AESocket(); // uses last values
																// stored in
																// preferences
							aeSocketClient.connect();
							log.info("connected " + aeSocketClient);
						}
						catch (final IOException ex3) {
							log.warning(ex3 + ": failed reconnection, sleeping 1 s before trying again");
							try {
								Thread.sleep(1000);
							}
							catch (final InterruptedException ex2) {
							}
						}
					}
				}
			}

			// if we are multicasting output send it out here
			if (multicastOutputEnabled && (aeMulticastOutput != null)) {
				try {
					if (!isLogFilteredEventsEnabled()) {
						aeMulticastOutput.writePacket(aeRaw);
					}
					else {
						// log the reconstructed packet after filtering
						final AEPacketRaw aeRawRecon = extractor.reconstructRawPacket(packet);
						aeMulticastOutput.writePacket(aeRawRecon);
					}
				}
				catch (final IOException e) {
					e.printStackTrace();
				}
			}

			if (unicastOutputEnabled && (unicastOutput != null)) {
				try {
					if (!isLogFilteredEventsEnabled()) {
						unicastOutput.writePacket(aeRaw);
					}
					else {
						// log the reconstructed packet after filtering.
						// TODO handle reconstructed packet with filtering that
						// transforms events. At present the
						// original raw addresses are sent out, so e.g. rotation
						// will not appear
						// in the output.
						final AEPacketRaw aeRawRecon = extractor.reconstructRawPacket(packet);
						unicastOutput.writePacket(aeRawRecon);
					}
				}
				catch (final IOException e) {
					e.printStackTrace();
				}
			}

			return false;

		}

		void fpsDelay() {
			if (!isPaused()) {
				getFrameRater().delayForDesiredFPS();
			}
			else if (!Thread.interrupted()) {
				synchronized (this) { // reason for grabbing monitor is because
										// if we are sliding the slider, we need
										// to
										// make sure we have control of the view
										// loop
					try {
						wait(1000);
					}
					catch (final java.lang.InterruptedException e) {
						log.log(Level.INFO, "viewLoop idle wait() was interrupted: {0}", e.toString());
					}
				}
			}
		}

		// returns delta time in ms of the current packet, or 0 if there is less
		// than two events
		private float getDtMs(final EventPacket packet) {
			if ((packet == null) || ((packet.getSize()) < 2)) {
				return 0;
			}

			final float dtMs = (float) ((packet.getLastTimestamp() - packet.getFirstTimestamp()) / (tickUs * 1e3));
			return dtMs;
		}

		// private int lastPacketLastTs = 0; // last timestamp of previous
		// packet, used by getDtMs
		// private float lastDtMs=0;
		//
		// // returns delta time in ms from last time this method was called,
		// uses lastts
		// private float getDtMs (EventPacket packet){
		// int numEvents=0;
		// if(packet==null || (numEvents=packet.getSize())==0 ||
		// packet.getLastTimestamp()==lastPacketLastTs) return
		// lastDtMs;
		//
		// int t=packet.getLastTimestamp();
		// float dtMs = (float)( ( t-lastPacketLastTs ) / ( tickUs * 1e3 ) );
		// lastPacketLastTs = t; // save last timestamp of this packet
		// lastDtMs=dtMs;
		// return dtMs;
		// }
		private float getTimeExpansion(final float dtMs) {
			lastTimeExpansionFactor = (getFrameRater().getAverageFPS() * dtMs) / 1000f;
			return lastTimeExpansionFactor;
		}

		// private String statLabel = null;
		private final StringBuilder sb = new StringBuilder(100);
		private float thisTime = Float.NaN;

		private void makeStatisticsLabel(final EventPacket packet) {
			if (((renderCount % 20) == 0) || isPaused() || isSingleStep() || (getFrameRater().getDesiredFPS() <= 30)) { // don't
																														// draw
																														// stats
																														// too
																														// fast
				if (getAePlayer().isChoosingFile()) {
					return;
				} // don't render stats while user is choosing file
				if (packet == null) {
					return;
				}
				if (packet.getSize() == 0) {
					return;
				}
				final float dtMs = getDtMs(packet);
				final String timeSliceString = String.format("%10ss", engFmt.format(dtMs / 1000));

				final float ratekeps = packet.getEventRateHz() / 1e3f;
				switch (getPlayMode()) {
					case SEQUENCING:
					case LIVE:
						if (aemon == null) {
							return;
						}
						// ratekeps=aemon.getEstimatedEventRate()/1000f;
						if (packet.getSize() > 0) { // only update time if there
													// is an event in the packet
							thisTime = packet.getLastTimestamp() * aemon.getTimestampTickUs() * 1e-6f;
						}
						// thisTimeString =
						// String.format("%5.3fs ",packet.getLastTimestamp() *
						// aemon.getTimestampTickUs() * 1e-6f);
						break;
					case PLAYBACK:
						// if(ae.getNumEvents()>2)
						// ratekeps=(float)ae.getNumEvents()/(float)dtMs;
						// if(packet.getSize()>2)
						// ratekeps=(float)packet.getSize()/(float)dtMs;
						// thisTimeString =
						// String.format("%5.3fs",getAePlayer().getTime() *
						// 1e-6f); // hack here, we
						// don't know timestamp from data file, we assume 1us
						thisTime = packet.getLastTimestamp() * 1e-6f; // just
																		// use
																		// the
																		// raw
																		// timestamp
																		// from
																		// the
																		// data
																		// file,
																		// but
																		// this
																		// will
																		// not
																		// account
																		// for
																		// wrapping.
						// doesn't do the right thing when playing back by
						// constant number of events
						// timeSliceString=String.format("%10ss",engFmt.format(getAePlayer().getTimesliceUs()*1e-6f));
						break;
					case REMOTE:
						thisTime = packet.getLastTimestamp() * 1e-6f;
						break;
				}
				final String thisTimeString = String.format("%5.3fs ", thisTime);

				String rateString = null;
				if (ratekeps >= 40e3f) {
					rateString = "   >40 Meps ";
				}
				else {
					rateString = String.format("%5.1fkeps", ratekeps); // String.format("%6.2fkeps ",ratekeps);
				}

				final int cs = getRenderer().getColorScale();

				String ovstring;
				if (overrunOccurred) {
					ovstring = "(overrun)";
				}
				else {
					ovstring = "";
				}

				// if(numEvents==0) s=thisTimeString+ "s: No events";
				// else {
				String timeExpansionString;
				if (isPaused()) {
					timeExpansionString = "Paused ";
				}
				else if ((getPlayMode() == PlayMode.LIVE) || (getPlayMode() == PlayMode.SEQUENCING)) {
					timeExpansionString = "Live/Seq ";
				}
				else {
					final float expansion = getTimeExpansion(dtMs);
					if (expansion == 0) {
						timeExpansionString = "??? ";
					}
					else if (expansion > 1) {
						timeExpansionString = String.format("%5.1fX ", expansion);
					}
					else {
						timeExpansionString = String.format("%5.1f/ ", 1 / expansion);
					}
				}

				String numEventsString;
				if (chip.getFilterChain().isAnyFilterEnabled()) {
					if (filterChain.isTimedOut()) {
						numEventsString = String.format("%5d/%-5d TO  ", numEvents, numFilteredEvents);
					}
					else {
						numEventsString = String.format("%5d/%-5devts", numEvents, numFilteredEvents);
					}
				}
				else {
					numEventsString = String.format("%5devts", numEvents);
				}

				final FrameRater fr = getFrameRater();

				final String frameRateString = String.format("%3.0f/%dfps,%dms ", fr.getAverageFPS(),
					fr.getDesiredFPS(), fr.getLastDelayMs());

				final String colorScaleString = (getRenderer().isAutoscaleEnabled() ? "AS=" : "FS=")
					+ Integer.toString(cs);

				sb.delete(0, sb.length());
				sb.append(timeSliceString).append('@').append(thisTimeString).append(numEventsString).append(ovstring)
					.append(rateString).append(timeExpansionString).append(frameRateString).append(colorScaleString);

				// statLabel =
				// String.format("%8ss@%-8s,%s%s,%s,%3.0f/%dfps,%4s,%2dms,%s=%2d",
				// timeSliceString,
				// thisTimeString,
				// numEventsString,
				// ovstring,
				// rateString,
				// frameRateString,
				// timeExpansionString,
				// frameRateString,
				// colorScaleString // auto or fullscale rendering color
				// );
				// }
				// System.out.println(statLabel.length());
				setStatisticsLabel(sb.toString());
				if (overrunOccurred) {
					statisticsLabel.setForeground(Color.RED);
				}
				else {
					statisticsLabel.setForeground(Color.BLACK);
				}
			}
		}
	}

	private javax.swing.Timer statusTimer = null;

	/**
	 * Sets the viewer's status message at the bottom of the window.
	 *
	 * @param s
	 *            the string
	 * @see #setStatusMessage(String)
	 */
	public void setStatusMessage(final String s) {
		SwingUtilities.invokeLater(new Runnable() { // invoke in Swing thread to
													// avoid Errors thrown by
													// getLock when the
													// viewloop (which is
													// calling setStatusMessage)
													// is interrupted by
													// playMode change

				@Override
				public void run() {
					statusTextField.setText(s);
					statusTextField.setToolTipText(s);
					if (statusTimer != null) {
						statusTimer.stop();
					}
					statusTimer = new javax.swing.Timer(5000, new ActionListener() {

						@Override
						public void actionPerformed(final ActionEvent e) {
							setStatusColor(Color.gray);
						}
					});
					statusTimer.setRepeats(false);
					statusTimer.setCoalesce(true);
					statusTimer.start();
				}
			});
	}

	private float lastTimeExpansionFactor = 1;

	/**
	 * Returns the most recent time dilation/contraction factor for display.
	 *
	 * @return the time expansion factor. 1 means real time, >1 means faster
	 *         than real time.
	 */
	public float getTimeExpansion() {
		return lastTimeExpansionFactor;
	}

	/**
	 * Sets the color of the status field text - e.g. to highlight it.
	 *
	 * @param c
	 */
	public void setStatusColor(final Color c) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				statusTextField.setForeground(c);
			}
		});
	}

	@Override
	public void exceptionOccurred(final Exception x, final Object source) {
		if (x.getMessage() != null) {
			setStatusMessage(x.getMessage());
			startStatusClearer(Color.RED);
		}
		else {
			if ((statusClearerThread != null) && statusClearerThread.isAlive()) {
				return;
			}
			setStatusMessage(null);
			final Color c = Color.GREEN;
			final Color c2 = c.darker();
			startStatusClearer(c2);
		}
	}

	private void startStatusClearer(final Color color) {
		setStatusColor(color);
		if ((statusClearerThread != null) && statusClearerThread.isAlive()) {
			statusClearerThread.renew();
		}
		else {
			statusClearerThread = new StatusClearerThread();
			statusClearerThread.start();
		}

	}

	StatusClearerThread statusClearerThread = null;
	/** length of exception highlighting in status bar in ms */
	public final long STATUS_DURATION = 1000;

	class StatusClearerThread extends Thread {

		long endTime;

		public StatusClearerThread() {
			super("AEViewerStatusClearerThread");
		}

		public void renew() {
			// System.out.println("renewing status change");
			endTime = System.currentTimeMillis() + STATUS_DURATION;
		}

		@Override
		public void run() {
			// System.out.println("start status clearer thread");
			endTime = System.currentTimeMillis() + STATUS_DURATION;
			try {
				while (System.currentTimeMillis() < endTime) {
					Thread.sleep(STATUS_DURATION);
				}
				setStatusColor(Color.DARK_GRAY);
			}
			catch (final InterruptedException e) {
			}

		}
	}

	void setStatisticsLabel(final String s) {
		// statisticsLabel.setText(s); // can cause flashing if label changes
		// size
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				statisticsLabel.setText(s);
			}
		});
		// for some reason invoking in swing thread (as it seems you should)
		// doesn't always update the label... mystery
		// try {
		// SwingUtilities.invokeAndWait(new Runnable(){
		// public void run(){
		// statisticsLabel.setText(s);
		// // if(statisticsLabel.getWidth()>statisticsPanel.getWidth()) {
		// ////
		// System.out.println("statisticsLabel width="+statisticsLabel.getWidth()+" > statisticsPanel width="+statisticsPanel.getWidth());
		// // // possibly resize statistics font size
		// // formComponentResized(null);
		// // }
		// }
		// });
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
	}

	int getScreenRefreshRate() {
		int rate = 60;
		final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		if (ge == null) {
			return rate;
		}
		final GraphicsDevice[] gs = ge.getScreenDevices();
		for (int i = 0; i < gs.length; i++) {
			final DisplayMode dm = gs[i].getDisplayMode();
			// Get refresh rate in Hz
			if (dm == null) {
				return rate;
			}
			int refreshRate = dm.getRefreshRate();
			if (refreshRate == DisplayMode.REFRESH_RATE_UNKNOWN) {
				log.warning("AEViewer.getScreenRefreshRate: got unknown refresh rate for screen " + i + ", assuming 60");
				refreshRate = 60;
			}
			else {
				// log.info("AEViewer.getScreenRefreshRate: screen "+i+" has refresh rate "+refreshRate);
			}
			if (i == 0) {
				rate = refreshRate;
			}
		}
		return rate;
	}// computes and executes appropriate delayForDesiredFPS to try to maintain
		// constant rendering rate

	/**
	 * Measure actual rendering frame rate and creates appropriate frame delay.
	 *
	 */
	public class FrameRater {

		final int MAX_FPS = 120;
		int desiredFPS = AEViewer.prefs.getInt("AEViewer.FrameRater.desiredFPS", getScreenRefreshRate());
		final int nSamples = 10;
		long[] samplesNs = new long[nSamples];
		int index = 0;
		int delayMs = 1;
		int desiredPeriodMs = (int) (1000f / desiredFPS);
		private long beforeTimeNs = System.nanoTime(), lastdt, afterTimeNs;

		final void setDesiredFPS(int fps) {
			if (fps < 1) {
				fps = 1;
			}
			else if (fps > MAX_FPS) {
				fps = MAX_FPS;
			}
			desiredFPS = fps;
			AEViewer.prefs.putInt("AEViewer.FrameRater.desiredFPS", fps);
			desiredPeriodMs = 1000 / fps;
		}

		final int getDesiredFPS() {
			return desiredFPS;
		}

		final float getAveragePeriodNs() {
			long sum = 0;
			for (int i = 0; i < nSamples; i++) {
				sum += samplesNs[i];
			}
			return (float) sum / nSamples;
		}

		public final float getAverageFPS() {
			return 1f / (getAveragePeriodNs() / 1e9f);
		}

		final float getLastFPS() {
			return 1f / (lastdt / 1e9f);
		}

		final int getLastDelayMs() {
			return delayMs;
		}

		final long getLastDtNs() {
			return lastdt;
		}

		// call this ONCE after capture/render. it will store the time since the
		// last call
		void takeBefore() {
			beforeTimeNs = System.nanoTime();
		}

		private long lastAfterTime = System.nanoTime();

		// call this ONCE after capture/render. it will store the time since the
		// last call
		final void takeAfter() {
			afterTimeNs = System.nanoTime();
			lastdt = afterTimeNs - beforeTimeNs;
			samplesNs[index++] = afterTimeNs - lastAfterTime;
			lastAfterTime = afterTimeNs;
			if (index >= nSamples) {
				index = 0;
			}
		}

		// call this to delayForDesiredFPS enough to make the total time
		// including last sample period equal to
		// desiredPeriodMs
		final void delayForDesiredFPS() {
			if (Thread.interrupted()) {
				return; // clear the interrupt flag here to make sure we don't
						// just pass through with no one clearing
						// the flag
			}

			delayMs = Math.round(desiredPeriodMs - ((float) lastdt / 1000000));
			if (delayMs < 0) {
				delayMs = 1;
			}
			try {
				Thread.sleep(delayMs);
			}
			catch (final java.lang.InterruptedException e) {
			}
		}
	}

	/**
	 * Fires a property change {@link #EVENT_STOPME}, and then stops playback or
	 * closes device
	 */
	public void stopMe() {
		stopLogging(true); // in case logging, make sure we give chance to save
							// file
		firePropertyChange(AEViewer.EVENT_STOPME, null, null);
		// log.info(Thread.currentThread()+ "AEViewer.stopMe() called");
		switch (getPlayMode()) {
			case PLAYBACK:
				getAePlayer().stopPlayback(); // TODO can lead to deadlock if
												// stopMe is called from a
												// thread that
				break;
			case LIVE:
			case WAITING:
				viewLoop.stopThread();
				showBiasgen(false);
				break;
			case REMOTE:
				if (unicastInputEnabled) {
					closeUnicastInput();
				}
				if (multicastInputEnabled) {
					aeMulticastInput.close();
					multicastInputEnabled = false;
				}
				if (blockingQueueInputEnabled) {
					blockingQueueInput = null;
					blockingQueueInputEnabled = false;
				}
				if (socketInputEnabled) {
					closeAESocket();
				}
		}
		// viewer is removed by WindowClosing event
		// if(caviarViewer!=null ){
		// log.info(this+" being removed from caviarViewer viewers list");
		// caviarViewer.getViewers().remove(this);
		// }
	}

	/**
	 * This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	// <editor-fold defaultstate="collapsed"
	// desc="Generated Code">//GEN-BEGIN:initComponents
	private void initComponents() {

		jProgressBar1 = new javax.swing.JProgressBar();
		renderModeButtonGroup = new javax.swing.ButtonGroup();
		monSeqOpModeButtonGroup = new javax.swing.ButtonGroup();
		statisticsPanel = new javax.swing.JPanel();
		imagePanel = new javax.swing.JPanel();
		bottomPanel = new javax.swing.JPanel();
		buttonsPanel = new javax.swing.JPanel();
		biasesToggleButton = new javax.swing.JToggleButton();
		filtersToggleButton = new javax.swing.JToggleButton();
		dontRenderToggleButton = new javax.swing.JToggleButton();
		loggingButton = new javax.swing.JToggleButton();
		multiModeButton = new javax.swing.JToggleButton();
		playerControlPanel = new javax.swing.JPanel();
		jPanel1 = new javax.swing.JPanel();
		statusTextField = new javax.swing.JTextField();
		showConsoleOutputButton = new javax.swing.JButton();
		resizePanel = new javax.swing.JPanel();
		resizeLabel = new javax.swing.JLabel();
		menuBar = new javax.swing.JMenuBar();
		fileMenu = new javax.swing.JMenu();
		newViewerMenuItem = new javax.swing.JMenuItem();
		openMenuItem = new javax.swing.JMenuItem();
		closeMenuItem = new javax.swing.JMenuItem();
		timestampResetBitmaskMenuItem = new javax.swing.JMenuItem();
		jSeparator8 = new javax.swing.JSeparator();
		loggingMenuItem = new javax.swing.JMenuItem();
		loggingPlaybackImmediatelyCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
		loggingSetTimelimitMenuItem = new javax.swing.JMenuItem();
		logFilteredEventsCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
		networkSeparator = new javax.swing.JSeparator();
		remoteMenu = new javax.swing.JMenu();
		openSocketInputStreamMenuItem = new javax.swing.JMenuItem();
		openSocketOutputStreamMenuItem = new javax.swing.JMenuItem();
		reopenSocketInputStreamMenuItem = new javax.swing.JMenuItem();
		serverSocketOptionsMenuItem = new javax.swing.JMenuItem();
		jSeparator15 = new javax.swing.JSeparator();
		multicastOutputEnabledCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
		openMulticastInputMenuItem = new javax.swing.JCheckBoxMenuItem();
		jSeparator14 = new javax.swing.JSeparator();
		unicastOutputEnabledCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
		openUnicastInputMenuItem = new javax.swing.JMenuItem();
		jSeparator17 = new javax.swing.JPopupMenu.Separator();
		openBlockingQueueInputMenuItem = new javax.swing.JCheckBoxMenuItem();
		syncSeperator = new javax.swing.JSeparator();
		syncEnabledCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
		jSeparator16 = new javax.swing.JSeparator();
		checkNonMonotonicTimeExceptionsEnabledCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
		exitSeperator = new javax.swing.JSeparator();
		exitMenuItem = new javax.swing.JMenuItem();
		viewMenu = new javax.swing.JMenu();
		viewBiasesMenuItem = new javax.swing.JMenuItem();
		dataWindowMenu = new javax.swing.JMenuItem();
		filtersSubMenu = new javax.swing.JMenu();
		viewFiltersMenuItem = new javax.swing.JMenuItem();
		enableFiltersOnStartupCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
		jSeparator1 = new javax.swing.JSeparator();
		graphicsSubMenu = new javax.swing.JMenu();
		viewActiveRenderingEnabledMenuItem = new javax.swing.JCheckBoxMenuItem();
		viewRenderBlankFramesCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
		jSeparator2 = new javax.swing.JSeparator();
		skipPacketsRenderingCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
		subsampleEnabledCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
		subSampleSizeMenuItem = new javax.swing.JMenuItem();
		jSeparator3 = new javax.swing.JSeparator();
		cycleColorRenderingMethodMenuItem = new javax.swing.JMenuItem();
		increaseContrastMenuItem = new javax.swing.JMenuItem();
		decreaseContrastMenuItem = new javax.swing.JMenuItem();
		autoscaleContrastEnabledCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
		calibrationStartStop = new javax.swing.JMenuItem();
		jSeparator4 = new javax.swing.JSeparator();
		cycleDisplayMethodButton = new javax.swing.JMenuItem();
		displayMethodMenu = new javax.swing.JMenu();
		jSeparator12 = new javax.swing.JSeparator();
		acccumulateImageEnabledCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
		viewIgnorePolarityCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
		increaseFrameRateMenuItem = new javax.swing.JMenuItem();
		decreaseFrameRateMenuItem = new javax.swing.JMenuItem();
		pauseRenderingCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
		viewSingleStepMenuItem = new javax.swing.JMenuItem();
		zeroTimestampsMenuItem = new javax.swing.JMenuItem();
		jSeparator11 = new javax.swing.JSeparator();
		increasePlaybackSpeedMenuItem = new javax.swing.JMenuItem();
		decreasePlaybackSpeedMenuItem = new javax.swing.JMenuItem();
		rewindPlaybackMenuItem = new javax.swing.JMenuItem();
		flextimePlaybackEnabledCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
		togglePlaybackDirectionMenuItem = new javax.swing.JMenuItem();
		toggleMarkCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
		measureTimeMenuItem = new javax.swing.JMenuItem();
		jSeparator10 = new javax.swing.JSeparator();
		zoomInMenuItem = new javax.swing.JMenuItem();
		zoomOutMenuItem = new javax.swing.JMenuItem();
		zoomCenterMenuItem = new javax.swing.JMenuItem();
		unzoomMenuItem = new javax.swing.JMenuItem();
		deviceMenu = new javax.swing.JMenu();
		deviceMenuSpparator = new javax.swing.JSeparator();
		customizeDevicesMenuItem = new javax.swing.JMenuItem();
		interfaceMenu = new javax.swing.JMenu();
		refreshInterfaceMenuItem = new javax.swing.JMenuItem();
		controlMenu = new javax.swing.JMenu();
		increaseBufferSizeMenuItem = new javax.swing.JMenuItem();
		decreaseBufferSizeMenuItem = new javax.swing.JMenuItem();
		increaseNumBuffersMenuItem = new javax.swing.JMenuItem();
		decreaseNumBuffersMenuItem = new javax.swing.JMenuItem();
		jSeparator9 = new javax.swing.JSeparator();
		changeAEBufferSizeMenuItem = new javax.swing.JMenuItem();
		jSeparator5 = new javax.swing.JSeparator();
		updateFirmwareMenuItem = new javax.swing.JMenuItem();
		cypressFX2EEPROMMenuItem = new javax.swing.JMenuItem();
		cypressFX2EEPROMLibUsbMenuItem = new javax.swing.JMenuItem();
		setDefaultFirmwareMenuItem = new javax.swing.JMenuItem();
		monSeqMenu = new javax.swing.JMenu();
		sequenceMenuItem = new javax.swing.JMenuItem();
		enableMissedEventsCheckBox = new javax.swing.JCheckBoxMenuItem();
		monSeqMissedEventsMenuItem = new javax.swing.JMenuItem();
		jSeparator13 = new javax.swing.JSeparator();
		monSeqOperationModeMenu = new javax.swing.JMenu();
		monSeqOpMode0 = new javax.swing.JRadioButtonMenuItem();
		monSeqOpMode1 = new javax.swing.JRadioButtonMenuItem();
		helpMenu = new javax.swing.JMenu();
		jSeparator7 = new javax.swing.JSeparator();
		aboutMenuItem = new javax.swing.JMenuItem();

		setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
		setTitle("AEViewer");
		addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(final java.awt.event.WindowEvent evt) {
				formWindowClosing(evt);
			}
		});

		statisticsPanel.setFocusable(false);
		statisticsPanel.setLayout(new javax.swing.BoxLayout(statisticsPanel, javax.swing.BoxLayout.LINE_AXIS));
		getContentPane().add(statisticsPanel, java.awt.BorderLayout.NORTH);

		imagePanel.setEnabled(false);
		imagePanel.setFocusable(false);
		imagePanel.setPreferredSize(new java.awt.Dimension(200, 200));
		imagePanel.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
			@Override
			public void mouseWheelMoved(final java.awt.event.MouseWheelEvent evt) {
				imagePanelMouseWheelMoved(evt);
			}
		});
		imagePanel.setLayout(new java.awt.BorderLayout());
		getContentPane().add(imagePanel, java.awt.BorderLayout.CENTER);

		bottomPanel.setLayout(new java.awt.BorderLayout());

		buttonsPanel.setMaximumSize(new java.awt.Dimension(1002, 200));
		buttonsPanel.setLayout(new javax.swing.BoxLayout(buttonsPanel, javax.swing.BoxLayout.X_AXIS));

		biasesToggleButton.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
		biasesToggleButton.setText("HW Configuration");
		biasesToggleButton
			.setToolTipText("Shows or hides the hardware configuration (e.g. bias generator, scanner, ADC, etc) control panel");
		biasesToggleButton.setAlignmentY(0.0F);
		biasesToggleButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
		biasesToggleButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				biasesToggleButtonActionPerformed(evt);
			}
		});
		buttonsPanel.add(biasesToggleButton);

		filtersToggleButton.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
		filtersToggleButton.setText("Filters");
		filtersToggleButton.setToolTipText("Shows or hides the filter window");
		filtersToggleButton.setAlignmentY(0.0F);
		filtersToggleButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
		filtersToggleButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				filtersToggleButtonActionPerformed(evt);
			}
		});
		buttonsPanel.add(filtersToggleButton);

		dontRenderToggleButton.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
		dontRenderToggleButton.setText("Don't render");
		dontRenderToggleButton.setToolTipText("Disables rendering to spped up processing");
		dontRenderToggleButton.setAlignmentY(0.0F);
		dontRenderToggleButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
		dontRenderToggleButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				dontRenderToggleButtonActionPerformed(evt);
			}
		});
		buttonsPanel.add(dontRenderToggleButton);

		loggingButton.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
		loggingButton.setMnemonic('l');
		loggingButton.setText("Start logging");
		loggingButton.setToolTipText("Starts or stops logging or relogging");
		loggingButton.setAlignmentY(0.0F);
		loggingButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
		buttonsPanel.add(loggingButton);

		multiModeButton.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
		multiModeButton.setMnemonic('l');
		multiModeButton.setText("Multi-Input Mode");
		multiModeButton.setToolTipText("Starts or stops logging or relogging");
		multiModeButton.setAlignmentY(0.0F);
		multiModeButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
		multiModeButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				multiModeButtonActionPerformed(evt);
			}
		});
		buttonsPanel.add(multiModeButton);

		playerControlPanel.setToolTipText("");
		playerControlPanel.setAlignmentY(0.0F);
		playerControlPanel.setMaximumSize(new java.awt.Dimension(32000, 32000));
		playerControlPanel.setLayout(new java.awt.BorderLayout());
		buttonsPanel.add(playerControlPanel);

		bottomPanel.add(buttonsPanel, java.awt.BorderLayout.CENTER);

		jPanel1.setLayout(new java.awt.BorderLayout());

		statusTextField.setEditable(false);
		statusTextField.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
		statusTextField.setToolTipText("Status messages show here");
		statusTextField.setFocusable(false);
		jPanel1.add(statusTextField, java.awt.BorderLayout.CENTER);

		showConsoleOutputButton.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
		showConsoleOutputButton.setText("Console");
		showConsoleOutputButton.setToolTipText("Shows console output window");
		showConsoleOutputButton.setFocusable(false);
		showConsoleOutputButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
		showConsoleOutputButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
		showConsoleOutputButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
		showConsoleOutputButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				showConsoleOutputButtonActionPerformed(evt);
			}
		});
		jPanel1.add(showConsoleOutputButton, java.awt.BorderLayout.EAST);

		bottomPanel.add(jPanel1, java.awt.BorderLayout.NORTH);

		resizePanel.setMinimumSize(new java.awt.Dimension(0, 0));
		resizePanel.setPreferredSize(new java.awt.Dimension(24, 24));
		resizePanel.setLayout(new java.awt.BorderLayout());

		resizeLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
		resizeLabel.setIcon(new TriangleSquareWindowsCornerIcon());
		new TriangleSquareWindowsCornerIcon();
		resizeLabel.setToolTipText("Resizes window");
		resizeLabel.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseEntered(final java.awt.event.MouseEvent evt) {
				resizeLabelMouseEntered(evt);
			}

			@Override
			public void mouseExited(final java.awt.event.MouseEvent evt) {
				resizeLabelMouseExited(evt);
			}

			@Override
			public void mousePressed(final java.awt.event.MouseEvent evt) {
				resizeLabelMousePressed(evt);
			}
		});
		resizeLabel.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
			@Override
			public void mouseDragged(final java.awt.event.MouseEvent evt) {
				resizeLabelMouseDragged(evt);
			}
		});
		resizePanel.add(resizeLabel, java.awt.BorderLayout.SOUTH);

		bottomPanel.add(resizePanel, java.awt.BorderLayout.EAST);

		getContentPane().add(bottomPanel, java.awt.BorderLayout.SOUTH);

		fileMenu.setMnemonic('f');
		fileMenu.setText("File");

		newViewerMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N,
			java.awt.event.InputEvent.CTRL_MASK));
		newViewerMenuItem.setMnemonic('N');
		newViewerMenuItem.setText("New viewer");
		newViewerMenuItem.setToolTipText("Opens a new viewer");
		newViewerMenuItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				newViewerMenuItemActionPerformed(evt);
			}
		});
		fileMenu.add(newViewerMenuItem);

		openMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, 0));
		openMenuItem.setMnemonic('o');
		openMenuItem.setText("Open logged data file...");
		openMenuItem.setToolTipText("Opens a logged data file for playback");
		openMenuItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				openMenuItemActionPerformed(evt);
			}
		});
		fileMenu.add(openMenuItem);

		closeMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_W,
			java.awt.event.InputEvent.CTRL_MASK));
		closeMenuItem.setMnemonic('C');
		closeMenuItem.setText("Close");
		closeMenuItem.setToolTipText("Closes this viewer or the playing data file");
		closeMenuItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				closeMenuItemActionPerformed(evt);
			}
		});
		fileMenu.add(closeMenuItem);

		timestampResetBitmaskMenuItem.setMnemonic('t');
		timestampResetBitmaskMenuItem.setText("dummy, set in constructor");
		timestampResetBitmaskMenuItem
			.setToolTipText("Setting a bitmask here will memorize and subtract timestamps when address  & bitmask != 0");
		timestampResetBitmaskMenuItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				timestampResetBitmaskMenuItemActionPerformed(evt);
			}
		});
		fileMenu.add(timestampResetBitmaskMenuItem);
		fileMenu.add(jSeparator8);

		loggingMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, 0));
		loggingMenuItem.setText("Start logging data");
		loggingMenuItem.setToolTipText("Starts or stops logging to disk");
		fileMenu.add(loggingMenuItem);

		loggingPlaybackImmediatelyCheckBoxMenuItem.setText("Playback logged data immediately after logging enabled");
		loggingPlaybackImmediatelyCheckBoxMenuItem.setToolTipText("If enabled, logged data plays back immediately");
		loggingPlaybackImmediatelyCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				loggingPlaybackImmediatelyCheckBoxMenuItemActionPerformed(evt);
			}
		});
		fileMenu.add(loggingPlaybackImmediatelyCheckBoxMenuItem);

		loggingSetTimelimitMenuItem.setText("Set logging time limit...");
		loggingSetTimelimitMenuItem.setToolTipText("Sets a time limit for logging");
		loggingSetTimelimitMenuItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				loggingSetTimelimitMenuItemActionPerformed(evt);
			}
		});
		fileMenu.add(loggingSetTimelimitMenuItem);

		logFilteredEventsCheckBoxMenuItem.setText("Enable filtering of logged or network output events");
		logFilteredEventsCheckBoxMenuItem
			.setToolTipText("Logging or network writes apply active filters first (reduces file size or network traffi)");
		logFilteredEventsCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				logFilteredEventsCheckBoxMenuItemActionPerformed(evt);
			}
		});
		fileMenu.add(logFilteredEventsCheckBoxMenuItem);
		fileMenu.add(networkSeparator);

		remoteMenu.setMnemonic('r');
		remoteMenu.setText("Remote");

		openSocketInputStreamMenuItem.setMnemonic('r');
		openSocketInputStreamMenuItem.setText("Open remote server input stream socket...");
		openSocketInputStreamMenuItem.setToolTipText("Opens a remote connection for stream (TCP) packets of  events ");
		openSocketInputStreamMenuItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				openSocketInputStreamMenuItemActionPerformed(evt);
			}
		});
		remoteMenu.add(openSocketInputStreamMenuItem);

		openSocketOutputStreamMenuItem.setText("Open remote server output stream socket...");
		openSocketOutputStreamMenuItem.setToolTipText("Opens a remote connection for stream (TCP) packets of  events ");
		openSocketOutputStreamMenuItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				openSocketOutputStreamMenuItemActionPerformed(evt);
			}
		});
		remoteMenu.add(openSocketOutputStreamMenuItem);

		reopenSocketInputStreamMenuItem.setMnemonic('l');
		reopenSocketInputStreamMenuItem.setText("Reopen last or preferred stream socket input stream");
		reopenSocketInputStreamMenuItem
			.setToolTipText("After an input socket has been opened (and preferences set), this quickly closes and reopens it");
		reopenSocketInputStreamMenuItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				reopenSocketInputStreamMenuItemActionPerformed(evt);
			}
		});
		remoteMenu.add(reopenSocketInputStreamMenuItem);

		serverSocketOptionsMenuItem.setText("Stream socket server options...");
		serverSocketOptionsMenuItem.setToolTipText("Sets options for server sockets");
		serverSocketOptionsMenuItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				serverSocketOptionsMenuItemActionPerformed(evt);
			}
		});
		remoteMenu.add(serverSocketOptionsMenuItem);
		remoteMenu.add(jSeparator15);

		multicastOutputEnabledCheckBoxMenuItem.setMnemonic('s');
		multicastOutputEnabledCheckBoxMenuItem.setText("Enable Multicast (UDP) AE Output");
		multicastOutputEnabledCheckBoxMenuItem
			.setToolTipText("<html>Enable multicast AE output (datagrams)<br><strong>Warning! Will flood network if there are no listeners.</strong></html>");
		multicastOutputEnabledCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				multicastOutputEnabledCheckBoxMenuItemActionPerformed(evt);
			}
		});
		remoteMenu.add(multicastOutputEnabledCheckBoxMenuItem);

		openMulticastInputMenuItem.setMnemonic('s');
		openMulticastInputMenuItem.setText("Enable Multicast (UDP) AE input");
		openMulticastInputMenuItem.setToolTipText("Enable multicast AE input (datagrams)");
		openMulticastInputMenuItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				openMulticastInputMenuItemActionPerformed(evt);
			}
		});
		remoteMenu.add(openMulticastInputMenuItem);
		remoteMenu.add(jSeparator14);

		unicastOutputEnabledCheckBoxMenuItem.setMnemonic('o');
		unicastOutputEnabledCheckBoxMenuItem.setText("Enable unicast datagram (UDP) output...");
		unicastOutputEnabledCheckBoxMenuItem
			.setToolTipText("Enables unicast datagram (UDP) outputs to a single receiver");
		unicastOutputEnabledCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				unicastOutputEnabledCheckBoxMenuItemActionPerformed(evt);
			}
		});
		remoteMenu.add(unicastOutputEnabledCheckBoxMenuItem);

		openUnicastInputMenuItem.setMnemonic('i');
		openUnicastInputMenuItem.setText("Open Unicast (UDP) remote AE input...");
		openUnicastInputMenuItem.setToolTipText("Opens a remote UDP unicast AE input");
		openUnicastInputMenuItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				openUnicastInputMenuItemActionPerformed(evt);
			}
		});
		remoteMenu.add(openUnicastInputMenuItem);
		remoteMenu.add(jSeparator17);

		openBlockingQueueInputMenuItem.setText("Enable BlockingQueue input from another viewer");
		openBlockingQueueInputMenuItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				openBlockingQueueInputMenuItemActionPerformed(evt);
			}
		});
		remoteMenu.add(openBlockingQueueInputMenuItem);

		fileMenu.add(remoteMenu);
		fileMenu.add(syncSeperator);

		syncEnabledCheckBoxMenuItem.setSelected(false);
		syncEnabledCheckBoxMenuItem.setText("Synchronized logging/playback enabled");
		syncEnabledCheckBoxMenuItem
			.setToolTipText("All viwers start/stop logging in synchrony and playback times are synchronized");
		syncEnabledCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				syncEnabledCheckBoxMenuItemActionPerformed(evt);
			}
		});
		fileMenu.add(syncEnabledCheckBoxMenuItem);
		fileMenu.add(jSeparator16);

		checkNonMonotonicTimeExceptionsEnabledCheckBoxMenuItem.setSelected(true);
		checkNonMonotonicTimeExceptionsEnabledCheckBoxMenuItem.setText("Check for non-monotonic time in input streams");
		checkNonMonotonicTimeExceptionsEnabledCheckBoxMenuItem
			.setToolTipText("If enabled, nonmonotonic time stamps are checked for in input streams from file or network");
		checkNonMonotonicTimeExceptionsEnabledCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				checkNonMonotonicTimeExceptionsEnabledCheckBoxMenuItemActionPerformed(evt);
			}
		});
		fileMenu.add(checkNonMonotonicTimeExceptionsEnabledCheckBoxMenuItem);
		fileMenu.add(exitSeperator);

		exitMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, 0));
		exitMenuItem.setMnemonic('x');
		exitMenuItem.setText("Exit");
		exitMenuItem.setToolTipText("Exits all viewers");
		exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				exitMenuItemActionPerformed(evt);
			}
		});
		fileMenu.add(exitMenuItem);

		menuBar.add(fileMenu);

		viewMenu.setMnemonic('v');
		viewMenu.setText("View");

		viewBiasesMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_B,
			java.awt.event.InputEvent.CTRL_MASK));
		viewBiasesMenuItem.setMnemonic('b');
		viewBiasesMenuItem.setText("Biases/HW Configuration");
		viewBiasesMenuItem.setToolTipText("Shows chip or board configuration controls");
		viewBiasesMenuItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				viewBiasesMenuItemActionPerformed(evt);
			}
		});
		viewMenu.add(viewBiasesMenuItem);

		dataWindowMenu.setText("Data Window");
		dataWindowMenu.setToolTipText("Shows a general purpose data window (including log output)");
		dataWindowMenu.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				dataWindowMenuActionPerformed(evt);
			}
		});
		viewMenu.add(dataWindowMenu);

		filtersSubMenu.setMnemonic('f');
		filtersSubMenu.setText("Event Filtering");

		viewFiltersMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F,
			java.awt.event.InputEvent.CTRL_MASK));
		viewFiltersMenuItem.setMnemonic('f');
		viewFiltersMenuItem.setText("Filters");
		viewFiltersMenuItem.setToolTipText("Shows filter controls");
		viewFiltersMenuItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				viewFiltersMenuItemActionPerformed(evt);
			}
		});
		filtersSubMenu.add(viewFiltersMenuItem);

		enableFiltersOnStartupCheckBoxMenuItem.setText("Enable filters on startup");
		enableFiltersOnStartupCheckBoxMenuItem
			.setToolTipText("Enables creation of event processing filters on startup");
		enableFiltersOnStartupCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				enableFiltersOnStartupCheckBoxMenuItemActionPerformed(evt);
			}
		});
		filtersSubMenu.add(enableFiltersOnStartupCheckBoxMenuItem);

		viewMenu.add(filtersSubMenu);
		viewMenu.add(jSeparator1);

		graphicsSubMenu.setMnemonic('g');
		graphicsSubMenu.setText("Graphics options");

		viewActiveRenderingEnabledMenuItem.setText("Active rendering enabled");
		viewActiveRenderingEnabledMenuItem
			.setToolTipText("Enables active display of each rendered frame if enabled.\nIf disabled, then  chipCanvas.repaint(1000 / frameRater.getDesiredFPS()) is called for repaint.");
		viewActiveRenderingEnabledMenuItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				viewActiveRenderingEnabledMenuItemActionPerformed(evt);
			}
		});
		graphicsSubMenu.add(viewActiveRenderingEnabledMenuItem);

		viewRenderBlankFramesCheckBoxMenuItem.setText("Render blank frames");
		viewRenderBlankFramesCheckBoxMenuItem.setToolTipText("If enabled, frames without events are rendered");
		viewRenderBlankFramesCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				viewRenderBlankFramesCheckBoxMenuItemActionPerformed(evt);
			}
		});
		graphicsSubMenu.add(viewRenderBlankFramesCheckBoxMenuItem);
		graphicsSubMenu.add(jSeparator2);

		skipPacketsRenderingCheckBoxMenuItem.setText("Skip packets rendering enabled...");
		skipPacketsRenderingCheckBoxMenuItem.setToolTipText("Enables skipping rendering of packets");
		skipPacketsRenderingCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				skipPacketsRenderingCheckBoxMenuItemActionPerformed(evt);
			}
		});
		graphicsSubMenu.add(skipPacketsRenderingCheckBoxMenuItem);

		subsampleEnabledCheckBoxMenuItem.setText("Enable subsample rendering");
		subsampleEnabledCheckBoxMenuItem.setToolTipText("use to speed up rendering");
		subsampleEnabledCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				subsampleEnabledCheckBoxMenuItemActionPerformed(evt);
			}
		});
		graphicsSubMenu.add(subsampleEnabledCheckBoxMenuItem);

		subSampleSizeMenuItem.setText("Choose rendering subsample limit...");
		subSampleSizeMenuItem.setToolTipText("Sets the number of events rendered in subsampling mode");
		subSampleSizeMenuItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				subSampleSizeMenuItemActionPerformed(evt);
			}
		});
		graphicsSubMenu.add(subSampleSizeMenuItem);

		viewMenu.add(graphicsSubMenu);
		viewMenu.add(jSeparator3);

		cycleColorRenderingMethodMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
			java.awt.event.KeyEvent.VK_C, 0));
		cycleColorRenderingMethodMenuItem.setText("Cycle color rendering mode");
		cycleColorRenderingMethodMenuItem.setToolTipText("Changes rendering mode (gray, contrast, RG, color-time)");
		cycleColorRenderingMethodMenuItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				cycleColorRenderingMethodMenuItemActionPerformed(evt);
			}
		});
		viewMenu.add(cycleColorRenderingMethodMenuItem);

		increaseContrastMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_UP, 0));
		increaseContrastMenuItem.setText("Increase contrast");
		increaseContrastMenuItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				increaseContrastMenuItemActionPerformed(evt);
			}
		});
		viewMenu.add(increaseContrastMenuItem);

		decreaseContrastMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DOWN, 0));
		decreaseContrastMenuItem.setText("Decrease contrast");
		decreaseContrastMenuItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				decreaseContrastMenuItemActionPerformed(evt);
			}
		});
		viewMenu.add(decreaseContrastMenuItem);

		autoscaleContrastEnabledCheckBoxMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
			java.awt.event.KeyEvent.VK_A, 0));
		autoscaleContrastEnabledCheckBoxMenuItem.setText("Autoscale contrast enabled");
		autoscaleContrastEnabledCheckBoxMenuItem.setToolTipText("Tries to autoscale histogram values");
		autoscaleContrastEnabledCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				autoscaleContrastEnabledCheckBoxMenuItemActionPerformed(evt);
			}
		});
		viewMenu.add(autoscaleContrastEnabledCheckBoxMenuItem);

		calibrationStartStop.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_K, 0));
		calibrationStartStop.setText("Start Calibration");
		calibrationStartStop
			.setToolTipText("Hold uniform surface in front of lens and start calibration. Wait a few seconds and stop calibration.");
		calibrationStartStop.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				calibrationStartStopActionPerformed(evt);
			}
		});
		viewMenu.add(calibrationStartStop);
		viewMenu.add(jSeparator4);

		cycleDisplayMethodButton.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_3, 0));
		cycleDisplayMethodButton.setText("Cycle display method");
		cycleDisplayMethodButton.setToolTipText("Cycles the display method");
		cycleDisplayMethodButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				cycleDisplayMethodButtonActionPerformed(evt);
			}
		});
		viewMenu.add(cycleDisplayMethodButton);

		displayMethodMenu.setText("display methods (placeholder)");
		viewMenu.add(displayMethodMenu);
		viewMenu.add(jSeparator12);

		acccumulateImageEnabledCheckBoxMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
			java.awt.event.KeyEvent.VK_P, 0));
		acccumulateImageEnabledCheckBoxMenuItem.setText("Accumulate image");
		acccumulateImageEnabledCheckBoxMenuItem.setToolTipText("Rendered data accumulates over 2d hisograms");
		acccumulateImageEnabledCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				acccumulateImageEnabledCheckBoxMenuItemActionPerformed(evt);
			}
		});
		viewMenu.add(acccumulateImageEnabledCheckBoxMenuItem);

		viewIgnorePolarityCheckBoxMenuItem.setText("Ignore cell type");
		viewIgnorePolarityCheckBoxMenuItem.setToolTipText("Throws away cells type for rendering");
		viewIgnorePolarityCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				viewIgnorePolarityCheckBoxMenuItemActionPerformed(evt);
			}
		});
		viewMenu.add(viewIgnorePolarityCheckBoxMenuItem);

		increaseFrameRateMenuItem.setAccelerator(javax.swing.KeyStroke
			.getKeyStroke(java.awt.event.KeyEvent.VK_RIGHT, 0));
		increaseFrameRateMenuItem.setText("Increase rendering frame rate");
		increaseFrameRateMenuItem.setToolTipText("Increases frames/second target for rendering");
		increaseFrameRateMenuItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				increaseFrameRateMenuItemActionPerformed(evt);
			}
		});
		viewMenu.add(increaseFrameRateMenuItem);

		decreaseFrameRateMenuItem
			.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_LEFT, 0));
		decreaseFrameRateMenuItem.setText("Decrease rendering frame rate");
		decreaseFrameRateMenuItem.setToolTipText("Decreases frames/second target for rendering");
		decreaseFrameRateMenuItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				decreaseFrameRateMenuItemActionPerformed(evt);
			}
		});
		viewMenu.add(decreaseFrameRateMenuItem);

		pauseRenderingCheckBoxMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
			java.awt.event.KeyEvent.VK_SPACE, 0));
		pauseRenderingCheckBoxMenuItem.setText("Paused");
		pauseRenderingCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				pauseRenderingCheckBoxMenuItemActionPerformed(evt);
			}
		});
		viewMenu.add(pauseRenderingCheckBoxMenuItem);

		viewSingleStepMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_PERIOD, 0));
		viewSingleStepMenuItem.setText("Single step");
		viewSingleStepMenuItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				viewSingleStepMenuItemActionPerformed(evt);
			}
		});
		viewMenu.add(viewSingleStepMenuItem);

		zeroTimestampsMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_0, 0));
		zeroTimestampsMenuItem.setText("Zero timestamps");
		zeroTimestampsMenuItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				zeroTimestampsMenuItemActionPerformed(evt);
			}
		});
		viewMenu.add(zeroTimestampsMenuItem);
		viewMenu.add(jSeparator11);

		increasePlaybackSpeedMenuItem.setAccelerator(javax.swing.KeyStroke
			.getKeyStroke(java.awt.event.KeyEvent.VK_F, 0));
		increasePlaybackSpeedMenuItem.setText("Increase playback speed");
		increasePlaybackSpeedMenuItem.setToolTipText("Makes the time slice longer");
		increasePlaybackSpeedMenuItem.setEnabled(false);
		increasePlaybackSpeedMenuItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				increasePlaybackSpeedMenuItemActionPerformed(evt);
			}
		});
		viewMenu.add(increasePlaybackSpeedMenuItem);

		decreasePlaybackSpeedMenuItem.setAccelerator(javax.swing.KeyStroke
			.getKeyStroke(java.awt.event.KeyEvent.VK_S, 0));
		decreasePlaybackSpeedMenuItem.setText("Decrease playback speed");
		decreasePlaybackSpeedMenuItem.setToolTipText("Makes the time slice shorter");
		decreasePlaybackSpeedMenuItem.setEnabled(false);
		decreasePlaybackSpeedMenuItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				decreasePlaybackSpeedMenuItemActionPerformed(evt);
			}
		});
		viewMenu.add(decreasePlaybackSpeedMenuItem);

		rewindPlaybackMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, 0));
		rewindPlaybackMenuItem.setText("Rewind");
		rewindPlaybackMenuItem.setEnabled(false);
		rewindPlaybackMenuItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				rewindPlaybackMenuItemActionPerformed(evt);
			}
		});
		viewMenu.add(rewindPlaybackMenuItem);

		flextimePlaybackEnabledCheckBoxMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
			java.awt.event.KeyEvent.VK_T, 0));
		flextimePlaybackEnabledCheckBoxMenuItem.setText("Flextime playback enabled");
		flextimePlaybackEnabledCheckBoxMenuItem.setToolTipText("Enables playback with constant number of events");
		flextimePlaybackEnabledCheckBoxMenuItem.setEnabled(false);
		flextimePlaybackEnabledCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				flextimePlaybackEnabledCheckBoxMenuItemActionPerformed(evt);
			}
		});
		viewMenu.add(flextimePlaybackEnabledCheckBoxMenuItem);

		togglePlaybackDirectionMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_B,
			0));
		togglePlaybackDirectionMenuItem.setText("Toggle playback direction");
		togglePlaybackDirectionMenuItem.setEnabled(false);
		togglePlaybackDirectionMenuItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				togglePlaybackDirectionMenuItemActionPerformed(evt);
			}
		});
		viewMenu.add(togglePlaybackDirectionMenuItem);

		toggleMarkCheckBoxMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_M, 0));
		toggleMarkCheckBoxMenuItem.setText("Toggle mark present location for rewind");
		toggleMarkCheckBoxMenuItem.setEnabled(false);
		toggleMarkCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				toggleMarkCheckBoxMenuItemActionPerformed(evt);
			}
		});
		viewMenu.add(toggleMarkCheckBoxMenuItem);

		measureTimeMenuItem.setMnemonic('m');
		measureTimeMenuItem.setText("Measure time");
		measureTimeMenuItem.setToolTipText("Each click reports statistics about timing since last click");
		viewMenu.add(measureTimeMenuItem);
		viewMenu.add(jSeparator10);

		zoomInMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_PAGE_UP, 0));
		zoomInMenuItem.setText("Zoom in");
		zoomInMenuItem.setToolTipText("Zooms in around mouse point");
		zoomInMenuItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				zoomInMenuItemActionPerformed(evt);
			}
		});
		viewMenu.add(zoomInMenuItem);

		zoomOutMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_PAGE_DOWN, 0));
		zoomOutMenuItem.setText("Zoom out");
		zoomOutMenuItem.setToolTipText("Zooms out around mouse point");
		zoomOutMenuItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				zoomOutMenuItemActionPerformed(evt);
			}
		});
		viewMenu.add(zoomOutMenuItem);

		zoomCenterMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_END, 0));
		zoomCenterMenuItem.setText("Center display here");
		zoomCenterMenuItem.setToolTipText("Centers display on mouse point");
		zoomCenterMenuItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				zoomCenterMenuItemActionPerformed(evt);
			}
		});
		viewMenu.add(zoomCenterMenuItem);

		unzoomMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_HOME, 0));
		unzoomMenuItem.setText("Unzoom");
		unzoomMenuItem.setToolTipText("Goes to default display zooming");
		unzoomMenuItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				unzoomMenuItemActionPerformed(evt);
			}
		});
		viewMenu.add(unzoomMenuItem);

		menuBar.add(viewMenu);

		deviceMenu.setMnemonic('a');
		deviceMenu.setText("AEChip");
		deviceMenu.setToolTipText("Specifies which chip is connected");
		deviceMenu.add(deviceMenuSpparator);

		customizeDevicesMenuItem.setMnemonic('C');
		customizeDevicesMenuItem.setText("Customize...");
		customizeDevicesMenuItem.setToolTipText("Let's you customize which AEChip's are available");
		customizeDevicesMenuItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				customizeDevicesMenuItemActionPerformed(evt);
			}
		});
		deviceMenu.add(customizeDevicesMenuItem);

		menuBar.add(deviceMenu);

		interfaceMenu.setMnemonic('i');
		interfaceMenu.setText("Interface");
		interfaceMenu.setToolTipText("Select the HW interface to use");
		interfaceMenu.addMenuListener(new javax.swing.event.MenuListener() {
			@Override
			public void menuCanceled(final javax.swing.event.MenuEvent evt) {
			}

			@Override
			public void menuSelected(final javax.swing.event.MenuEvent evt) {
				interfaceMenuMenuSelected(evt);
			}

			@Override
			public void menuDeselected(final javax.swing.event.MenuEvent evt) {
			}
		});

		refreshInterfaceMenuItem.setText("Refresh");
		refreshInterfaceMenuItem.addComponentListener(new java.awt.event.ComponentAdapter() {
			@Override
			public void componentShown(final java.awt.event.ComponentEvent evt) {
				refreshInterfaceMenuItemComponentShown(evt);
			}
		});
		refreshInterfaceMenuItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				refreshInterfaceMenuItemActionPerformed(evt);
			}
		});
		interfaceMenu.add(refreshInterfaceMenuItem);

		menuBar.add(interfaceMenu);

		controlMenu.setMnemonic('c');
		controlMenu.setText("USB");
		controlMenu.setToolTipText("control CypresFX2 driver parameters");

		increaseBufferSizeMenuItem.setText("Increase hardware buffer size");
		increaseBufferSizeMenuItem.setToolTipText("Increases the host USB fifo size");
		increaseBufferSizeMenuItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				increaseBufferSizeMenuItemActionPerformed(evt);
			}
		});
		controlMenu.add(increaseBufferSizeMenuItem);

		decreaseBufferSizeMenuItem.setText("Decrease hardware buffer size");
		decreaseBufferSizeMenuItem.setToolTipText("Decreases the host USB fifo size");
		decreaseBufferSizeMenuItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				decreaseBufferSizeMenuItemActionPerformed(evt);
			}
		});
		controlMenu.add(decreaseBufferSizeMenuItem);

		increaseNumBuffersMenuItem.setText("Increase number of hardware buffers");
		increaseNumBuffersMenuItem.setToolTipText("Increases the host number of USB read buffers");
		increaseNumBuffersMenuItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				increaseNumBuffersMenuItemActionPerformed(evt);
			}
		});
		controlMenu.add(increaseNumBuffersMenuItem);

		decreaseNumBuffersMenuItem.setText("Decrease num hardware buffers");
		decreaseNumBuffersMenuItem.setToolTipText("Decreases the host number of USB read buffers");
		decreaseNumBuffersMenuItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				decreaseNumBuffersMenuItemActionPerformed(evt);
			}
		});
		controlMenu.add(decreaseNumBuffersMenuItem);
		controlMenu.add(jSeparator9);

		changeAEBufferSizeMenuItem.setMnemonic('b');
		changeAEBufferSizeMenuItem.setText("Set rendering AE buffer size");
		changeAEBufferSizeMenuItem
			.setToolTipText("sets size of host raw event buffers used for render/capture data exchnage");
		changeAEBufferSizeMenuItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				changeAEBufferSizeMenuItemActionPerformed(evt);
			}
		});
		controlMenu.add(changeAEBufferSizeMenuItem);
		controlMenu.add(jSeparator5);

		updateFirmwareMenuItem.setMnemonic('u');
		updateFirmwareMenuItem.setText("Update firmware...");
		updateFirmwareMenuItem.setToolTipText("Updates device firmware with confirm dialog");
		updateFirmwareMenuItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				updateFirmwareMenuItemActionPerformed(evt);
			}
		});
		controlMenu.add(updateFirmwareMenuItem);

		cypressFX2EEPROMMenuItem.setMnemonic('e');
		cypressFX2EEPROMMenuItem.setText("(Advanced users only) CypressFX2 EEPPROM Utility");
		cypressFX2EEPROMMenuItem.setToolTipText("(advanced users) Opens dialog to download device firmware ");
		cypressFX2EEPROMMenuItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				cypressFX2EEPROMMenuItemActionPerformed(evt);
			}
		});
		controlMenu.add(cypressFX2EEPROMMenuItem);

		cypressFX2EEPROMLibUsbMenuItem.setMnemonic('e');
		cypressFX2EEPROMLibUsbMenuItem.setText("(Advanced users only) CypressFX2 EEPPROM Utility (libusb)");
		cypressFX2EEPROMLibUsbMenuItem.setToolTipText("(advanced users) Opens dialog to download device firmware ");
		cypressFX2EEPROMLibUsbMenuItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				cypressFX2EEPROMLibUsbMenuItemActionPerformed(evt);
			}
		});
		controlMenu.add(cypressFX2EEPROMLibUsbMenuItem);

		setDefaultFirmwareMenuItem.setMnemonic('f');
		setDefaultFirmwareMenuItem.setText("Set default firmware for blank device...");
		setDefaultFirmwareMenuItem.setToolTipText("Sets the firmware that is downloaded to a blank CypressFX2");
		setDefaultFirmwareMenuItem.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseEntered(final java.awt.event.MouseEvent evt) {
				setDefaultFirmwareMenuItemMouseEntered(evt);
			}
		});
		setDefaultFirmwareMenuItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				setDefaultFirmwareMenuItemActionPerformed(evt);
			}
		});
		controlMenu.add(setDefaultFirmwareMenuItem);

		menuBar.add(controlMenu);

		monSeqMenu.setText("MonSeq");
		monSeqMenu.setToolTipText("For sequencer or monitor+sequencer devices");
		monSeqMenu.setEnabled(false);

		sequenceMenuItem.setMnemonic('f');
		sequenceMenuItem.setText("Sequence data file...");
		sequenceMenuItem.setToolTipText("You can select a recorded data file to sequence");
		sequenceMenuItem.setActionCommand("start");
		sequenceMenuItem.setEnabled(false);
		sequenceMenuItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				sequenceMenuItemActionPerformed(evt);
			}
		});
		monSeqMenu.add(sequenceMenuItem);

		enableMissedEventsCheckBox.setText("Enable Missed Events");
		enableMissedEventsCheckBox.setEnabled(false);
		enableMissedEventsCheckBox.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				enableMissedEventsCheckBoxActionPerformed(evt);
			}
		});
		monSeqMenu.add(enableMissedEventsCheckBox);

		monSeqMissedEventsMenuItem.setText("Get number of missed events");
		monSeqMissedEventsMenuItem
			.setToolTipText("If the device is a monitor, this will show how many events were missed");
		monSeqMissedEventsMenuItem.setEnabled(false);
		monSeqMissedEventsMenuItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				monSeqMissedEventsMenuItemActionPerformed(evt);
			}
		});
		monSeqMenu.add(monSeqMissedEventsMenuItem);
		monSeqMenu.add(jSeparator13);

		monSeqOperationModeMenu.setText("Timestamp tick");

		monSeqOpModeButtonGroup.add(monSeqOpMode0);
		monSeqOpMode0.setSelected(true);
		monSeqOpMode0.setText("1 microsecond ");
		monSeqOpMode0.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				monSeqOpMode0ActionPerformed(evt);
			}
		});
		monSeqOperationModeMenu.add(monSeqOpMode0);

		monSeqOpModeButtonGroup.add(monSeqOpMode1);
		monSeqOpMode1.setText("0.2 microsecond");
		monSeqOpMode1.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				monSeqOpMode1ActionPerformed(evt);
			}
		});
		monSeqOperationModeMenu.add(monSeqOpMode1);

		monSeqMenu.add(monSeqOperationModeMenu);

		menuBar.add(monSeqMenu);

		helpMenu.setMnemonic('h');
		helpMenu.setText("Help");
		helpMenu.add(jSeparator7);

		aboutMenuItem.setText("About");
		aboutMenuItem.setToolTipText("Version information");
		aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				aboutMenuItemActionPerformed(evt);
			}
		});
		helpMenu.add(aboutMenuItem);

		menuBar.add(helpMenu);

		setJMenuBar(menuBar);

		pack();
	}// </editor-fold>//GEN-END:initComponents
		// volatile boolean playerSliderMousePressed=false;

	volatile boolean playerSliderWasPaused = false;

	private void resizeLabelMouseExited(final java.awt.event.MouseEvent evt) {// GEN-FIRST:event_resizeLabelMouseExited
		setCursor(preResizeCursor);
	}// GEN-LAST:event_resizeLabelMouseExited

	Cursor preResizeCursor = Cursor.getDefaultCursor();

	private void resizeLabelMouseEntered(final java.awt.event.MouseEvent evt) {// GEN-FIRST:event_resizeLabelMouseEntered
		preResizeCursor = getCursor();
		setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
	}// GEN-LAST:event_resizeLabelMouseEntered

	private void resizeLabelMousePressed(final java.awt.event.MouseEvent evt) {// GEN-FIRST:event_resizeLabelMousePressed
		oldSize = getSize();
		startResizePoint = evt.getPoint();
	}// GEN-LAST:event_resizeLabelMousePressed

	private void resizeLabelMouseDragged(final java.awt.event.MouseEvent evt) {// GEN-FIRST:event_resizeLabelMouseDragged
		final Point resizePoint = evt.getPoint();
		final int widthInc = resizePoint.x - startResizePoint.x;
		final int heightInc = resizePoint.y - startResizePoint.y;
		setSize(getWidth() + widthInc, getHeight() + heightInc);
	}// GEN-LAST:event_resizeLabelMouseDragged

	private void dataWindowMenuActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_dataWindowMenuActionPerformed
		JAERViewer.globalDataViewer.setVisible(!JAERViewer.globalDataViewer.isVisible());
	}// GEN-LAST:event_dataWindowMenuActionPerformed

	private void enableFiltersOnStartupCheckBoxMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_enableFiltersOnStartupCheckBoxMenuItemActionPerformed
		enableFiltersOnStartup = enableFiltersOnStartupCheckBoxMenuItem.isSelected();
		AEViewer.prefs.putBoolean("AEViewer.enableFiltersOnStartup", enableFiltersOnStartup);
	}// GEN-LAST:event_enableFiltersOnStartupCheckBoxMenuItemActionPerformed

	private void dontRenderToggleButtonActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_dontRenderToggleButtonActionPerformed
		if (dontRenderToggleButton.isSelected()) {
			skipPacketsRenderingNumber = 100;
		}
		else {
			skipPacketsRenderingNumber = 0;
			skipPacketsRenderingCount = 0; // added to restart rendering
											// immediately
		}
	}// GEN-LAST:event_dontRenderToggleButtonActionPerformed

	void fixSkipPacketsRenderingMenuItems() {
		skipPacketsRenderingCheckBoxMenuItem.setSelected(skipPacketsRenderingNumber > 0);
		skipPacketsRenderingCheckBoxMenuItem.setText("Skip rendering packets (skipping " + skipPacketsRenderingNumber
			+ " packets)");
	}

	private void skipPacketsRenderingCheckBoxMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_skipPacketsRenderingCheckBoxMenuItemActionPerformed
		// come here when user wants to skip rendering except every n packets
		if (!skipPacketsRenderingCheckBoxMenuItem.isSelected()) {
			skipPacketsRenderingNumber = 0;
			AEViewer.prefs.putInt("AEViewer.skipPacketsRenderingNumber", skipPacketsRenderingNumber);
			fixSkipPacketsRenderingMenuItems();
			return;
		}
		final String s = "Number of packets to skip over between rendering (currently " + skipPacketsRenderingNumber
			+ ")";
		boolean gotIt = false;
		while (!gotIt) {
			final String retString = JOptionPane.showInputDialog(this, s, Integer.toString(skipPacketsRenderingNumber));
			if (retString == null) {
				return;
			} // cancelled
			try {
				skipPacketsRenderingNumber = Integer.parseInt(retString);
				gotIt = true;
			}
			catch (final NumberFormatException e) {
				log.warning(e.toString());
			}
		}
		AEViewer.prefs.putInt("AEViewer.skipPacketsRenderingNumber", skipPacketsRenderingNumber);
		fixSkipPacketsRenderingMenuItems();
	}// GEN-LAST:event_skipPacketsRenderingCheckBoxMenuItemActionPerformed

	private void customizeDevicesMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_customizeDevicesMenuItemActionPerformed
		// log.info("customizing chip classes");
		ClassChooserDialog dlg;
		try {
			// setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			dlg = new ClassChooserDialog(this, AEChip.class, chipClassNames, null);
		}
		finally {
			// setCursor(Cursor.getDefaultCursor());
		}
		dlg.setVisible(true);
		final int ret = dlg.getReturnStatus();
		if (ret == ClassChooserDialog.RET_OK) {
			chipClassNames = dlg.getList();
			putChipClassPrefs();
			buildDeviceMenu();
		}
	}// GEN-LAST:event_customizeDevicesMenuItemActionPerformed

	private void sequenceMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_sequenceMenuItemActionPerformed

		if (evt.getActionCommand().equals("start")) {
			chipCanvas.getScale();
			final AESequencerInterface aemonseq = (AESequencerInterface) chip.getHardwareInterface();
			try {
				if ((aemonseq != null) && (aemonseq instanceof AEMonitorSequencerInterface)) {
					((AEMonitorSequencerInterface) aemonseq).stopMonitoringSequencing();
				}
			}
			catch (final HardwareInterfaceException e) {
				e.printStackTrace();
			}

			JFileChooser fileChooser = new JFileChooser();
			final ChipDataFilePreview preview = new ChipDataFilePreview(fileChooser, chip); // from
																							// book
																							// swing
																							// hacks
			fileChooser.addPropertyChangeListener(preview);
			fileChooser.setAccessory(preview);

			final String lastFilePath = AEViewer.prefs.get("AEViewer.lastFile", ""); // getString
																						// the
																						// last
																						// folder

			lastFile = new File(lastFilePath);

			final DATFileFilter datFileFilter = new DATFileFilter();
			fileChooser.addChoosableFileFilter(datFileFilter);
			fileChooser.setCurrentDirectory(lastFile); // sets the working
														// directory of the
														// chooser
			// boolean wasPaused=isPaused();
			// setPaused(true);
			final int retValue = fileChooser.showOpenDialog(this);
			if (retValue == JFileChooser.APPROVE_OPTION) {
				lastFile = fileChooser.getSelectedFile();
				if (lastFile != null) {
					recentFiles.addFile(lastFile);
				}
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						sequenceFile(lastFile);
					}
				});
			}
			fileChooser = null;
			// setPaused(false);
			// chipCanvas.setScale(oldScale);
		}
		else if (evt.getActionCommand().equals("stop")) {
			setPlayMode(PlayMode.LIVE);
			stopSequencing();
		}
	}// GEN-LAST:event_sequenceMenuItemActionPerformed

	private void sequenceFile(final File file) {
		try {
			setCurrentFile(file);
			final AEFileInputStream fileAEInputStream = new AEFileInputStream(file);
			fileAEInputStream.setFile(file);
			fileAEInputStream.setNonMonotonicTimeExceptionsChecked(false); // the
																			// code
																			// below
																			// has
																			// to
																			// take
																			// care
																			// about
																			// non-monotonic
																			// time
																			// anyway

			final int numberOfEvents = (int) fileAEInputStream.size();

			AEPacketRaw seqPkt = fileAEInputStream.readPacketByNumber(numberOfEvents);

			if (seqPkt.getNumEvents() < numberOfEvents) {
				final int[] ad = new int[numberOfEvents];
				final int[] ts = new int[numberOfEvents];
				int remainingevents = numberOfEvents;
				int ind = 0;
				do {
					remainingevents = remainingevents - AEFileInputStream.MAX_BUFFER_SIZE_EVENTS;
					System.arraycopy(seqPkt.getTimestamps(), 0, ts, ind * AEFileInputStream.MAX_BUFFER_SIZE_EVENTS,
						seqPkt.getNumEvents());
					System.arraycopy(seqPkt.getAddresses(), 0, ad, ind * AEFileInputStream.MAX_BUFFER_SIZE_EVENTS,
						seqPkt.getNumEvents());
					seqPkt = fileAEInputStream.readPacketByNumber(remainingevents);
					ind++;

				}
				while (remainingevents > AEFileInputStream.MAX_BUFFER_SIZE_EVENTS);

				seqPkt = new AEPacketRaw(ad, ts);
			}
			// calculate interspike intervals
			final int[] ts = seqPkt.getTimestamps();
			final int[] isi = new int[seqPkt.getNumEvents()];

			isi[0] = ts[0];

			for (int i = 1; i < seqPkt.getNumEvents(); i++) {
				isi[i] = ts[i] - ts[i - 1];
				if (isi[i] < 0) {
					// if (!(ts[i-1]>0 && ts[i]<0)) //if it is not an overflow,
					// it is non-monotonic time, so set isi to
					// zero
					// {
					log.info("non-monotonic time at event " + i + ", set interspike interval to zero");
					isi[i] = 0;
					// }
				}
			}
			seqPkt.setTimestamps(isi);

			final AESequencerInterface aemonseq = (AESequencerInterface) chip.getHardwareInterface();

			setPaused(false);

			if (aemonseq instanceof AEMonitorSequencerInterface) {
				((AEMonitorSequencerInterface) aemonseq).startMonitoringSequencing(seqPkt);
			}
			else {
				aemonseq.startSequencing(seqPkt);
			}
			aemonseq.setLoopedSequencingEnabled(true);
			setPlayMode(PlayMode.SEQUENCING);
			sequenceMenuItem.setActionCommand("stop");
			sequenceMenuItem.setText("Stop sequencing data file");

			if (!playerControlPanel.isVisible()) {
				playerControlPanel.setVisible(true);
			}
			// playerSlider.setVisible(true);
			playerControls.getPlayerSlider().setEnabled(false);
			// System.gc(); // garbage collect...
		}
		catch (final Exception e) {
			e.printStackTrace();
		}
	}

	/** Stops sequencing. */
	public void stopSequencing() {
		try {
			if ((chip != null) && (chip.getHardwareInterface() != null)) {
				((AESequencerInterface) chip.getHardwareInterface()).stopSequencing();
			}

		}
		catch (final HardwareInterfaceException e) {
			e.printStackTrace();
		}
		sequenceMenuItem.setActionCommand("start");
		sequenceMenuItem.setText("Sequence data file...");
		playerControlPanel.setVisible(false);
		// playerSlider.setVisible(true);
		playerControls.getPlayerSlider().setEnabled(true);
	}

	Dimension oldSize;
	Point startResizePoint;

	private void cycleDisplayMethodButtonActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_cycleDisplayMethodButtonActionPerformed
		chipCanvas.cycleDisplayMethod();
		chip.setPreferredDisplayMethod(chipCanvas.getDisplayMethod().getClass());
	}// GEN-LAST:event_cycleDisplayMethodButtonActionPerformed

	private void unzoomMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_unzoomMenuItemActionPerformed
		chipCanvas.unzoom();
	}// GEN-LAST:event_unzoomMenuItemActionPerformed

	private void viewIgnorePolarityCheckBoxMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_viewIgnorePolarityCheckBoxMenuItemActionPerformed
		chip.getRenderer().setIgnorePolarityEnabled(viewIgnorePolarityCheckBoxMenuItem.isSelected());
	}// GEN-LAST:event_viewIgnorePolarityCheckBoxMenuItemActionPerformed

	private void cypressFX2EEPROMMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_cypressFX2EEPROMMenuItemActionPerformed
		new CypressFX2EEPROM().setVisible(true);
	}// GEN-LAST:event_cypressFX2EEPROMMenuItemActionPerformed

	private void cypressFX2EEPROMLibUsbMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_cypressFX2EEPROMLibUsbMenuItemActionPerformed
		new net.sf.jaer.hardwareinterface.usb.cypressfx2libusb.CypressFX2EEPROM().setVisible(true);
	}// GEN-LAST:event_cypressFX2EEPROMLibUsbMenuItemActionPerformed

	private void viewRenderBlankFramesCheckBoxMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_viewRenderBlankFramesCheckBoxMenuItemActionPerformed
		setRenderBlankFramesEnabled(viewRenderBlankFramesCheckBoxMenuItem.isSelected());
	}// GEN-LAST:event_viewRenderBlankFramesCheckBoxMenuItemActionPerformed

	private void monSeqMissedEventsMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_monSeqMissedEventsMenuItemActionPerformed
		if (aemon instanceof CypressFX2MonitorSequencer) {
			final CypressFX2MonitorSequencer fx = (CypressFX2MonitorSequencer) aemon;
			try {
				JOptionPane
					.showMessageDialog(this, fx + " missed approximately " + fx.getNumMissedEvents() + " events");
			}
			catch (final Exception e) {
				e.printStackTrace();
				aemon.close();
			}
		}
	}// GEN-LAST:event_monSeqMissedEventsMenuItemActionPerformed

	volatile boolean doSingleStepEnabled = false;

	synchronized public void doSingleStep() {
		// log.info("doSingleStep");
		setPaused(true); // better to set paused before single step starts
		setDoSingleStepEnabled(true);
		// interruptViewloop();
	}

	public void setDoSingleStepEnabled(final boolean yes) {
		doSingleStepEnabled = yes;
	}

	synchronized public boolean isSingleStep() {
		// boolean isSingle=caviarViewer.getSyncPlayer().isSingleStep();
		// return isSingle;
		return doSingleStepEnabled;
	}

	synchronized public void singleStepDone() {
		if (isSingleStep()) {
			setDoSingleStepEnabled(false);
			// setPaused(true); // it is already set by doSingleStep
		}
		// caviarViewer.getSyncPlayer().singleStepDone();
	}

	private void viewSingleStepMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_viewSingleStepMenuItemActionPerformed
		jaerViewer.getSyncPlayer().doSingleStep();
		interruptViewloop();
	}// GEN-LAST:event_viewSingleStepMenuItemActionPerformed

	private void enableMonSeqMenu(final boolean state) {
		monSeqMenu.setEnabled(state);
		if (chip.getHardwareInterface() instanceof AEMonitorInterface) {
			monSeqOperationModeMenu.setEnabled(state);
			monSeqOpMode0.setEnabled(state);
			monSeqOpMode1.setEnabled(state);
			monSeqMissedEventsMenuItem.setEnabled(state);
			enableMissedEventsCheckBox.setEnabled(state);
		}
		sequenceMenuItem.setEnabled(state);
	}

	// used to print dt for measuring frequency from playback by using '1'
	// keystrokes

	// class Statistics {
	//
	// JFrame statFrame;
	// JLabel statLabel;
	// int lastTime = 0, thisTime;
	// EngineeringFormat fmt = new EngineeringFormat();
	//
	// {
	// fmt.precision = 2;
	// }
	//
	// void printStats() {
	// synchronized (aePlayer) {
	// thisTime = aePlayer.getTime();
	// int dt = lastTime - thisTime;
	// float dtSec = (float) ((float) dt / 1e6f + java.lang.Float.MIN_VALUE);
	// float freqHz = 1 / dtSec;
	// //
	// System.out.println(String.format("dt=%.2g s, freq=%.2g Hz",dtSec,freqHz));
	// if (statFrame == null) {
	// statFrame = new JFrame("Statistics");
	// statLabel = new JLabel();
	// statLabel.setFont(statLabel.getFont().deriveFont(16f));
	// statLabel.setToolTipText("Type \"1\" to update interval statistics");
	// statFrame.getContentPane().setLayout(new BorderLayout());
	// statFrame.getContentPane().add(statLabel, BorderLayout.CENTER);
	// statFrame.pack();
	// }
	// String s = " dt=" + fmt.format(dtSec) + "s, freq=" + fmt.format(freqHz) +
	// " Hz ";
	// log.info(s);
	// statLabel.setText(s);
	// statLabel.revalidate();
	// statFrame.pack();
	// if (!statFrame.isVisible()) {
	// statFrame.setVisible(true);
	// }
	// requestFocus(); // leave the focus here
	// lastTime = thisTime;
	// }
	// }
	// }
	// Statistics statistics;
	private void formWindowClosing(final java.awt.event.WindowEvent evt) {// GEN-FIRST:event_formWindowClosing
		log.info("window closing");
		if ((biasgenFrame != null) && !biasgenFrame.isModificationsSaved()) {
			return;
		}
		viewLoop.stopThread();
		cleanup();

		if (jaerViewer.getViewers().size() == 1) {
			log.info("window closing event, only 1 viewer, calling System.exit");
			// stopMe(); // TODO seems to deadlock
			System.exit(0);
		}
		else {
			log.info("window closing event with more than one AEViewer window, calling stopMe");
			if ((filterFrame != null) && filterFrame.isVisible()) {
				filterFrame.dispose(); // close this frame if the window is
										// closed
			}

			// TODO should close biasgen window also
			stopMe();
			dispose();
		}
	}// GEN-LAST:event_formWindowClosing

	private void refreshInterfaceMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_refreshInterfaceMenuItemActionPerformed
		// TODO add your handling code here:
	}// GEN-LAST:event_refreshInterfaceMenuItemActionPerformed

	private void toggleMarkCheckBoxMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_toggleMarkCheckBoxMenuItemActionPerformed
		try {
			synchronized (getAePlayer()) {
				if (toggleMarkCheckBoxMenuItem.isSelected()) {
					getAePlayer().mark();
					// // Dictionary<Integer,JLabel> dict=new
					// Dictionary<Integer,JLabel>();
					// Hashtable<Integer,JLabel> markTable = new
					// Hashtable<Integer,JLabel>();
					// markTable.putString(playerSlider.getValue(),new
					// JLabel("^"));
					// playerSlider.setLabelTable(markTable);
					// playerSlider.setPaintLabels(true); // TODO move all this
					// to AePlayerAdvancedControlsPanel
				}
				else {
					getAePlayer().unmark();
					// playerSlider.setPaintLabels(false);
				}
			}
		}
		catch (final IOException e) {
			e.printStackTrace();
		}
	}// GEN-LAST:event_toggleMarkCheckBoxMenuItemActionPerformed

	private void subSampleSizeMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_subSampleSizeMenuItemActionPerformed
		final String ans = JOptionPane.showInputDialog(this, "Enter limit to number of rendered events", getRenderer()
			.getSubsampleThresholdEventCount());
		try {
			final int n = Integer.parseInt(ans);
			getRenderer().setSubsampleThresholdEventCount(n);
			extractor.setSubsampleThresholdEventCount(n);
		}
		catch (final NumberFormatException e) {
			Toolkit.getDefaultToolkit().beep();
		}
	}// GEN-LAST:event_subSampleSizeMenuItemActionPerformed

	private void filtersToggleButtonActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_filtersToggleButtonActionPerformed
		showFilters(filtersToggleButton.isSelected());
	}// GEN-LAST:event_filtersToggleButtonActionPerformed

	private void biasesToggleButtonActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_biasesToggleButtonActionPerformed
		showBiasgen(biasesToggleButton.isSelected());
	}// GEN-LAST:event_biasesToggleButtonActionPerformed

	private void imagePanelMouseWheelMoved(final java.awt.event.MouseWheelEvent evt) {// GEN-FIRST:event_imagePanelMouseWheelMoved
		final int rotation = evt.getWheelRotation();
		getRenderer().setColorScale(getRenderer().getColorScale() + rotation);
	}// GEN-LAST:event_imagePanelMouseWheelMoved

	private void togglePlaybackDirectionMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_togglePlaybackDirectionMenuItemActionPerformed
		getAePlayer().toggleDirection();
	}// GEN-LAST:event_togglePlaybackDirectionMenuItemActionPerformed

	private void flextimePlaybackEnabledCheckBoxMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_flextimePlaybackEnabledCheckBoxMenuItemActionPerformed
		if (jaerViewer == null) {
			return;
		}
		if (!jaerViewer.isSyncEnabled() || (jaerViewer.getViewers().size() == 1)) {
			aePlayer.toggleFlexTime();
		}
		else {
			JOptionPane.showMessageDialog(this, "Flextime playback doesn't make sense for sychronized viewing");
			flextimePlaybackEnabledCheckBoxMenuItem.setSelected(false);
		}
	}// GEN-LAST:event_flextimePlaybackEnabledCheckBoxMenuItemActionPerformed

	private void rewindPlaybackMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_rewindPlaybackMenuItemActionPerformed
		getAePlayer().rewind();
	}// GEN-LAST:event_rewindPlaybackMenuItemActionPerformed

	private void decreasePlaybackSpeedMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_decreasePlaybackSpeedMenuItemActionPerformed
		getAePlayer().slowDown();
	}// GEN-LAST:event_decreasePlaybackSpeedMenuItemActionPerformed

	private void increasePlaybackSpeedMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_increasePlaybackSpeedMenuItemActionPerformed
		getAePlayer().speedUp();
	}// GEN-LAST:event_increasePlaybackSpeedMenuItemActionPerformed

	private void autoscaleContrastEnabledCheckBoxMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_autoscaleContrastEnabledCheckBoxMenuItemActionPerformed
		getRenderer().setAutoscaleEnabled(!getRenderer().isAutoscaleEnabled());
	}// GEN-LAST:event_autoscaleContrastEnabledCheckBoxMenuItemActionPerformed

	private void acccumulateImageEnabledCheckBoxMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_acccumulateImageEnabledCheckBoxMenuItemActionPerformed
		getRenderer().setAccumulateEnabled(!getRenderer().isAccumulateEnabled());
	}// GEN-LAST:event_acccumulateImageEnabledCheckBoxMenuItemActionPerformed

	private void zeroTimestampsMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_zeroTimestampsMenuItemActionPerformed
		if ((jaerViewer != null) && jaerViewer.isSyncEnabled()) {
			log.info("zeroing timestamps on all viewers because isSyncEnabled=true");
			jaerViewer.zeroTimestamps();
		}
		else {
			log.info("zeroing timestamps only on current AEViewer " + this);
			zeroTimestamps();
		}
	}// GEN-LAST:event_zeroTimestampsMenuItemActionPerformed

	private void decreaseFrameRateMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_decreaseFrameRateMenuItemActionPerformed
		// case KeyEvent.VK_LEFT: // slower
		setFrameRate(getFrameRate() / 2);
		// break;
		// case KeyEvent.VK_RIGHT: //faster
		// setFrameRate(getFrameRate()*2);
		// break;

	}// GEN-LAST:event_decreaseFrameRateMenuItemActionPerformed

	private void increaseFrameRateMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_increaseFrameRateMenuItemActionPerformed
		// case KeyEvent.VK_LEFT: // slower
		// setFrameRate(getFrameRate()/2);
		// break;
		// case KeyEvent.VK_RIGHT: //faster
		setFrameRate(getFrameRate() * 2);
		// break;

	}// GEN-LAST:event_increaseFrameRateMenuItemActionPerformed

	private void decreaseContrastMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_decreaseContrastMenuItemActionPerformed
		// if(viewLoop.rerenderFlagDone==false) return;
		// viewLoop.rerenderFlagDone=false;
		getRenderer().setColorScale(getRenderer().getColorScale() + 1);
		// System.out.println("interrupting viewloop");
		// repaint();
		interruptViewloop();
	}// GEN-LAST:event_decreaseContrastMenuItemActionPerformed

	private void increaseContrastMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_increaseContrastMenuItemActionPerformed
		// if(viewLoop.rerenderFlagDone==false) return;
		// viewLoop.rerenderFlagDone=false;
		getRenderer().setColorScale(getRenderer().getColorScale() - 1);
		// System.out.println("interrupting viewloop");
		interruptViewloop();
		// repaint();
	}// GEN-LAST:event_increaseContrastMenuItemActionPerformed

	private void cycleColorRenderingMethodMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_cycleColorRenderingMethodMenuItemActionPerformed
		getRenderer().cycleColorMode();
	}// GEN-LAST:event_cycleColorRenderingMethodMenuItemActionPerformed

	private void subsampleEnabledCheckBoxMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_subsampleEnabledCheckBoxMenuItemActionPerformed
		chip.setSubSamplingEnabled(subsampleEnabledCheckBoxMenuItem.isSelected());
	}// GEN-LAST:event_subsampleEnabledCheckBoxMenuItemActionPerformed

	private void viewActiveRenderingEnabledMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_viewActiveRenderingEnabledMenuItemActionPerformed
		setActiveRenderingEnabled(viewActiveRenderingEnabledMenuItem.isSelected());
	}// GEN-LAST:event_viewActiveRenderingEnabledMenuItemActionPerformed

	/**
	 * Fills in the device control menu (the USB menu) so that menu items are
	 * populated with correct values of USB
	 * buffer size and number of buffers, etc.
	 * Runs in the Swing worker thread.
	 */
	public void fixDeviceControlMenuItems() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				final int k = controlMenu.getMenuComponentCount();
				if ((aemon == null) || (!(aemon instanceof ReaderBufferControl) && !aemon.isOpen())) {
					for (int i = 0; i < k; i++) {
						if (controlMenu.getMenuComponent(i) instanceof JMenuItem) {
							((JMenuItem) controlMenu.getMenuComponent(i)).setEnabled(false);
						}
					}
				}
				else if ((aemon != null) && (aemon instanceof ReaderBufferControl) && aemon.isOpen()) {
					final ReaderBufferControl readerControl = (ReaderBufferControl) aemon;
					final PropertyChangeSupport readerSupport = readerControl.getReaderSupport();
					// propertyChange method in this file deals with these
					// events
					if (!readerSupport.hasListeners("readerStarted")) { // TODO
																		// change
																		// to
																		// public
																		// static
																		// String
																		// for
																		// events
																		// in
																		// AEReader
						readerSupport.addPropertyChangeListener("readerStarted", AEViewer.this); // when
																									// the
																									// reader
																									// starts
																									// running,
																									// we
																									// getString
																									// called
																									// back
																									// to
																									// fix
																									// device
																									// control
																									// menu
					}

					final int n = readerControl.getNumBuffers();
					final int f = readerControl.getFifoSize();
					decreaseNumBuffersMenuItem.setText("Decrease num buffers to " + (n - 1));
					increaseNumBuffersMenuItem.setText("Increase num buffers to " + (n + 1));
					decreaseBufferSizeMenuItem.setText("Decrease FIFO size to " + (f / 2));
					increaseBufferSizeMenuItem.setText("Increase FIFO size to " + (f * 2));

					for (int i = 0; i < k; i++) {
						if (controlMenu.getMenuComponent(i) instanceof JMenuItem) {
							((JMenuItem) controlMenu.getMenuComponent(i)).setEnabled(true);
						}
					}
				}

				cypressFX2EEPROMMenuItem.setEnabled(true); // always set the
															// true to be able
															// to launch utility
															// even if
															// the device is not
															// a retina

				cypressFX2EEPROMLibUsbMenuItem.setEnabled(true);

				setDefaultFirmwareMenuItem.setEnabled(true);
				if ((aemon != null) && (aemon instanceof HasUpdatableFirmware)) {
					updateFirmwareMenuItem.setEnabled(true);
				}
				else {
					updateFirmwareMenuItem.setEnabled(false);
				}
			}
		});
		// log.info("fixing device control menu");
	}

	private void decreaseNumBuffersMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_decreaseNumBuffersMenuItemActionPerformed
		if ((aemon != null) && (aemon instanceof ReaderBufferControl) && aemon.isOpen()) {
			final ReaderBufferControl reader = (ReaderBufferControl) aemon;
			int n = reader.getNumBuffers() - 1;
			if (n < 1) {
				n = 1;
			}

			reader.setNumBuffers(n);
			fixDeviceControlMenuItems();

		}

	}// GEN-LAST:event_decreaseNumBuffersMenuItemActionPerformed

	private void increaseNumBuffersMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_increaseNumBuffersMenuItemActionPerformed
		if ((aemon != null) && (aemon instanceof ReaderBufferControl) && aemon.isOpen()) {
			final ReaderBufferControl reader = (ReaderBufferControl) aemon;
			final int n = reader.getNumBuffers() + 1;
			reader.setNumBuffers(n);
			fixDeviceControlMenuItems();

		}
	}// GEN-LAST:event_increaseNumBuffersMenuItemActionPerformed

	private void decreaseBufferSizeMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_decreaseBufferSizeMenuItemActionPerformed
		if ((aemon != null) && (aemon instanceof ReaderBufferControl) && aemon.isOpen()) {
			final ReaderBufferControl reader = (ReaderBufferControl) aemon;
			final int n = reader.getFifoSize() / 2;
			reader.setFifoSize(n);
			fixDeviceControlMenuItems();

		}
	}// GEN-LAST:event_decreaseBufferSizeMenuItemActionPerformed

	private void increaseBufferSizeMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_increaseBufferSizeMenuItemActionPerformed
		if ((aemon != null) && (aemon instanceof ReaderBufferControl) && aemon.isOpen()) {
			final ReaderBufferControl reader = (ReaderBufferControl) aemon;
			final int n = reader.getFifoSize() * 2;
			reader.setFifoSize(n);
			fixDeviceControlMenuItems();

		}
	}// GEN-LAST:event_increaseBufferSizeMenuItemActionPerformed

	private void viewFiltersMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_viewFiltersMenuItemActionPerformed
		showFilters(true);
	}// GEN-LAST:event_viewFiltersMenuItemActionPerformed

	private void viewBiasesMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_viewBiasesMenuItemActionPerformed
		showBiasgen(true);
	}// GEN-LAST:event_viewBiasesMenuItemActionPerformed
		// avoid stateChanged events from slider that is set by player

	volatile boolean sliderDontProcess = false;

	/**
	 * messages come back here from e.g. programmatic state changes, like a new
	 * aePlayer file posiiton.
	 * This methods sets the GUI components to a consistent state, using a flag
	 * to tell the slider that it has not been
	 * set by
	 * a user mouse action
	 */
	@Override
	public void propertyChange(final PropertyChangeEvent evt) {
		if (evt.getSource() instanceof HardwareInterface) {
			if (evt.getPropertyName().equals("readerStarted")) { // comes from
																	// hardware
																	// interface
																	// AEReader
																	// thread
				// log.info("AEViewer.propertyChange: AEReader started, fixing device control menu");
				// cypress reader started, can set device control for cypress
				// usbio reader thread
				fixDeviceControlMenuItems();
			}
		}
		else if (evt.getPropertyName().equals("cleared")) {
			setStatusMessage(null);
		}
		else if (evt.getSource() instanceof AEFileInputStream) {
			firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue()); // forward/refire
																								// events
																								// from
																								// AEFileInputStream
																								// to
																								// listeners
																								// on
																								// AEViewer
		}
		else if (evt.getSource() instanceof AEPlayer) {
			firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue()); // forward/refire
																								// events
																								// from
																								// AEFileInputStream
																								// to
																								// listeners
																								// on
																								// AEViewer
		}
	}

	private void aboutMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_aboutMenuItemActionPerformed
		new AEViewerAboutDialog(new javax.swing.JFrame(), true).setVisible(true);
	}// GEN-LAST:event_aboutMenuItemActionPerformed

	public void showFilters(final boolean yes) {
		if (yes && !filterFrameBuilt) {
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			try {
				filterFrame = new FilterFrame(chip);
			}
			finally {
				setCursor(Cursor.getDefaultCursor());
			}
			filterFrame.addWindowListener(new WindowAdapter() {

				@Override
				public void windowClosed(final WindowEvent e) {
					// log.info(e.toString());
					filtersToggleButton.setSelected(false);
				}
			});
			filterFrameBuilt = true;
		}

		if (filterFrame != null) {
			filterFrame.setVisible(yes);
			filterFrame.setState(Frame.NORMAL);
		}

		filtersToggleButton.setSelected(yes);
	}

	/**
	 * Returns true if configuration frame for controlling biases and other
	 * configuration exists and is visible
	 *
	 * @return true if really visible
	 */
	public boolean isBiasgenVisible() {
		if (getBiasgenFrame() == null) {
			return false;
		}
		return getBiasgenFrame().isVisible();
	}

	/**
	 * Shows the configuration frame. The process to show the frame occurs in
	 * the background Swing thread, so the frame
	 * is not immediately visible.
	 * To check for valid frame, use isBiasgenVisible().
	 *
	 * @param yes
	 *            true to show.
	 */
	public void showBiasgen(final boolean yes) {
		if (chip == null) {
			if (yes) {
				log.warning("null chip, can't try to show biasgen");
			} // only show warning if trying to show biasgen for null chip

			return;
		}

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				if (chip.getBiasgen() == null) { // this chip has no biasgen
													// object defined or
													// registered with
													// setBiasgen
					if (getBiasgenFrame() != null) {
						getBiasgenFrame().dispose();
					}
					// biasesToggleButton.setEnabled(false); // chip don't have
					// biasgen until it has HW interface, which
					// it doesn't at first....

					return;
				}
				else {
					biasesToggleButton.setEnabled(true);
					viewBiasesMenuItem.setEnabled(true);
				}

				if (biasgen != chip.getBiasgen()) { // biasgen changed
					if (getBiasgenFrame() != null) {
						getBiasgenFrame().dispose();
					}

					biasgenFrame = new BiasgenFrame(chip);
					biasgenFrame.addWindowListener(new WindowAdapter() {

						@Override
						public void windowClosed(final WindowEvent e) {
							// log.info(e.toString());
							biasesToggleButton.setSelected(false);
						}
					});
				}

				if (getBiasgenFrame() != null) {
					getBiasgenFrame().setVisible(yes);
				}

				biasesToggleButton.setSelected(yes);
				biasgen = chip.getBiasgen();

			}
		});
	}

	synchronized public void toggleLogging() {
		if ((jaerViewer != null) && jaerViewer.isSyncEnabled() && (jaerViewer.getViewers().size() > 1)) {
			jaerViewer.toggleSynchronizedLogging();
		}
		else {
			if (loggingEnabled) {
				stopLogging(true); // confirms filename dialog when flag true
			}
			else {
				startLogging();
			}

		}
		// if(loggingButton.isSelected()){
		// if(caviarViewer!=null && caviarViewer.isSyncEnabled() )
		// caviarViewer.startSynchronizedLogging(); else
		// startLogging();
		// }else{
		// if(caviarViewer!=null && caviarViewer.isSyncEnabled())
		// caviarViewer.stopSynchronizedLogging(); else
		// stopLogging();
		// }
	}

	void fixLoggingControls() {
		SwingUtilities.invokeLater(new Runnable() { // made this a runnable to
													// run later to fix possible
													// race problems -
													// tobi
				@Override
				public void run() {// System.out.println("fixing logging controls, loggingEnabled="+loggingEnabled);
					if ((playMode != PlayMode.REMOTE) && ((aemon == null) || ((aemon != null) && !aemon.isOpen()))
						&& (playMode != PlayMode.PLAYBACK)) {
						// we can log from live input or from playing file (e.g.
						// after refiltering it) or we can log
						// network data
						// TODO: not ideal logic here, too confusing
						loggingButton.setEnabled(false);
						loggingMenuItem.setEnabled(false);
						return;

					}
					else {
						loggingButton.setEnabled(true);
						loggingMenuItem.setEnabled(true);
					}

					if (!loggingEnabled && (playMode == PlayMode.PLAYBACK)) {
						loggingButton.setText("Start Re-logging");
						loggingMenuItem.setText("Start re-logging data");
					}
					else if (loggingEnabled) {
						loggingButton.setText("Stop logging");
						loggingButton.setSelected(true);
						loggingMenuItem.setText("Stop logging data");
					}
					else {
						loggingButton.setText("Start logging");
						loggingButton.setSelected(false);
						loggingMenuItem.setText("Start logging data");
					}
				}
			});

	}

	public void openLoggingFolderWindow() {
		final String osName = System.getProperty("os.name");
		if (osName == null) {
			log.warning("no OS name property, cannot open browser");
			return;

		}

		final String curDir = System.getProperty("user.dir");
		// log.info("opening folder window for folder "+curDir);
		if (osName.startsWith("Windows")) {
			try {
				Runtime.getRuntime().exec("explorer.exe " + curDir);
			}
			catch (final IOException e) {
				log.warning(e.getMessage());
			}

		}
		else if (System.getProperty("os.name").indexOf("Linux") != -1) {
			log.warning("cannot open linux folder browsing window");
		}

	}

	/**
	 * Starts logging AE data to a file.
	 *
	 * @param filename
	 *            the filename to log to, including all path information.
	 *            Filenames without path
	 *            are logged to the startup folder. The default extension of
	 *            AEDataFile.DATA_FILE_EXTENSION is appended
	 *            if there is no extension.
	 *
	 * @return the file that is logged to.
	 */
	synchronized public File startLogging(String filename) {
		if (filename == null) {
			log.warning("tried to log to null filename, aborting");
			return null;
		}
		if (!filename.toLowerCase().endsWith(AEDataFile.DATA_FILE_EXTENSION)
			&& !filename.toLowerCase().endsWith(AEDataFile.OLD_DATA_FILE_EXTENSION)) {
			// allow both extensions for backward compatibility
			filename = filename + AEDataFile.DATA_FILE_EXTENSION;
			log.info("Appended extension AEDataFile.DATA_FILE_EXTENSION " + AEDataFile.DATA_FILE_EXTENSION
				+ " to make filename=" + filename);
		}
		try {
			loggingFile = new File(filename);

			// loggingOutputStream = new AEFileOutputStream(new
			// FileOutputStream(loggingFile));
			loggingOutputStream = new AEFileOutputStream(new BufferedOutputStream(new FileOutputStream(loggingFile),
				8192)); // tobi changed to 8k buffer (from 400k) because this
						// has measurablly better performance that
						// super large buffer
			loggingEnabled = true;

			if (playMode == PlayMode.PLAYBACK) { // add change listener for
													// rewind to stop logging
				getAePlayer().getAEInputStream().getSupport()
					.addPropertyChangeListener("rewind", new PropertyChangeListener() {

						@Override
						public void propertyChange(final PropertyChangeEvent evt) {
							if ((evt.getSource() == getAePlayer().getAEInputStream())
								&& evt.getPropertyName().equals("rewind")) {
								log.info("recording reached end, stopping re-logging");
								SwingUtilities.invokeLater(new Runnable() {

									@Override
									public void run() {
										stopLogging(true);
									}
								});
							}
						}
					});
			}
			setCurrentFile(loggingFile);
			loggingEnabled = true;

			fixLoggingControls();

			if (loggingTimeLimit > 0) {
				loggingStartTime = System.currentTimeMillis();
			}
			log.info("starting logging to " + loggingFile);
			// aemon.resetTimestamps();

		}
		catch (final FileNotFoundException e) {
			loggingFile = null;
			e.printStackTrace();
		}

		return loggingFile;
	}

	/**
	 * Starts logging data to a default data logging file.
	 *
	 * @return the file that is logged to.
	 */
	synchronized public File startLogging() {
		// if(playMode!=PlayMode.LIVE) return null;
		// first reset timestamps to zero time, and for stereo interfaces, to
		// sychronize them
		/*
		 * TODO : fix so that timestamps are zeroed before recording really
		 * starts
		 */
		// zeroTimestamps();

		final String dateString = AEDataFile.DATE_FORMAT.format(new Date());
		final String className = chip.getClass().getSimpleName();
		int suffixNumber = 0;
		// TODO replace with real serial number code in devices!
		String serialNumber = "";
		if ((chip.getHardwareInterface() != null) && (chip.getHardwareInterface() instanceof USBInterface)) {
			final USBInterface usb = (USBInterface) chip.getHardwareInterface();
			if ((usb.getStringDescriptors() != null) && (usb.getStringDescriptors().length == 3)
				&& (usb.getStringDescriptors()[2] != null)) {
				serialNumber = "-" + usb.getStringDescriptors()[2];
			}
		}
		boolean succeeded = false;
		String filename;

		do {
			// log files to tmp folder initially, later user will move or delete
			// file on end of logging
			filename = lastLoggingFolder + File.separator + className + "-" + dateString + serialNumber + "-"
				+ suffixNumber + AEDataFile.DATA_FILE_EXTENSION;
			final File lf = new File(filename);
			if (!lf.isFile()) {
				succeeded = true;
			}

		}
		while ((succeeded == false) && (suffixNumber++ <= 5));
		if (succeeded == false) {
			log.warning("AEViewer.startLogging(): could not open a unigue new file for logging after trying up to "
				+ filename);
			return null;
		}

		final File lf = startLogging(filename);
		return lf;

	}

	/**
	 * Stops logging and optionally opens file dialog for where to save file.
	 * If number of AEViewers is more than one, dialog is also skipped since we
	 * may be logging from multiple viewers.
	 *
	 * @param confirmFilename
	 *            true to show file dialog to confirm filename, false to skip
	 *            dialog.
	 * @return chosen File
	 */
	synchronized public File stopLogging(final boolean confirmFilename) {
		// the file has already been logged somewhere with a timestamped name,
		// what this method does is
		// to move the already logged file to a possibly different location with
		// a new name, or if cancel is hit,
		// to delete it.
		int retValue = JFileChooser.CANCEL_OPTION;
		if (loggingEnabled) {
			if (loggingButton.isSelected()) {
				loggingButton.setSelected(false);
			}

			loggingButton.setText("Start logging");
			loggingMenuItem.setText("Start logging data");
			try {
				log.info("stopped logging at " + AEDataFile.DATE_FORMAT.format(new Date()));
				synchronized (loggingOutputStream) {
					loggingEnabled = false;
					loggingOutputStream.close();
				}
				// if jaer viewer is logging synchronized data files, then just
				// save the file where it was logged
				// originally

				if (confirmFilename && !jaerViewer.isSyncEnabled()) {
					final JFileChooser chooser = new JFileChooser();
					chooser.setCurrentDirectory(lastLoggingFolder);
					chooser.setFileFilter(new DATFileFilter());
					chooser.setDialogTitle("Save logged data");

					final String fn = loggingFile.getName();
					// System.out.println("fn="+fn);
					// strip off .aedat to make it easier to add comment to
					// filename
					final int extInd = fn.lastIndexOf(AEDataFile.DATA_FILE_EXTENSION);
					String base = fn;
					if (extInd > 0) {
						base = fn.substring(0, extInd); // maybe trying to save
														// old .dat extension
					}// System.out.println("base="+base);
						// we'll add the extension back later
					chooser.setSelectedFile(new File(base));
					// chooser.setAccessory(new ResetFileButton(base,chooser));
					chooser.setDialogType(JFileChooser.SAVE_DIALOG);
					chooser.setMultiSelectionEnabled(false);
					// Component[] comps=chooser.getComponents();
					// for(Component c:comps){
					// if(c.getName().equals("buttonPanel")){
					// ((JPanel)c).add(new ResetFileButton(base,chooser));
					// }
					// }
					boolean savedIt = false;
					do {
						// clear the text input buffer to prevent multiply typed
						// characters from destroying proposed
						// datetimestamped filename
						retValue = chooser.showSaveDialog(AEViewer.this);
						if (retValue == JFileChooser.APPROVE_OPTION) {
							File newFile = chooser.getSelectedFile();
							// make sure filename ends with .aedat
							if (!newFile.getName().endsWith(AEDataFile.DATA_FILE_EXTENSION)) {
								newFile = new File(newFile.getCanonicalPath() + AEDataFile.DATA_FILE_EXTENSION);
							}
							// we'll rename the logged data file to the
							// selection

							final boolean renamed = loggingFile.renameTo(newFile);
							if (renamed) {
								// if successful, cool, save persistence
								savedIt = true;
								lastLoggingFolder = chooser.getCurrentDirectory();
								AEViewer.prefs.put("AEViewer.lastLoggingFolder", lastLoggingFolder.getCanonicalPath());
								recentFiles.addFile(newFile);
								loggingFile = newFile; // so that we play it
														// back if it was saved
														// and playback
														// immediately is
														// selected
								log.info("renamed logging file to " + newFile);
							}
							else {
								// confirm overwrite
								final int overwrite = JOptionPane.showConfirmDialog(chooser, "Overwrite file \""
									+ newFile + "\"?", "Overwrite file?", JOptionPane.WARNING_MESSAGE,
									JOptionPane.OK_CANCEL_OPTION);
								if (overwrite == JOptionPane.OK_OPTION) {
									// we need to delete the file
									final boolean deletedOld = newFile.delete();
									if (deletedOld) {
										savedIt = loggingFile.renameTo(newFile);
										savedIt = true;
										log.info("renamed logging file to " + newFile); // TODO
																						// something
																						// messed
																						// up
																						// here
																						// with
																						// confirmed
																						// overwrite
																						// of
																						// logging
																						// file
										loggingFile = newFile;
									}
									else {
										log.warning("couldn't delete logging file " + newFile);
									}

								}
								else {
									chooser.setDialogTitle("Couldn't save file there, try again");
								}

							}
						}
						else {
							// user hit cancel, delete logged data
							final boolean deleted = loggingFile.delete();
							if (deleted) {
								log.info("Deleted temporary logging file " + loggingFile);
							}
							else {
								log.warning("Couldn't delete temporary logging file " + loggingFile);
							}

							savedIt = true;
						}

					}
					while (savedIt == false); // keep trying until user is happy
												// (unless they deleted some
												// crucial
												// data!)
				}

			}
			catch (final IOException e) {
				e.printStackTrace();
			}

			if ((retValue == JFileChooser.APPROVE_OPTION) && isLoggingPlaybackImmediatelyEnabled()) {
				try {
					getAePlayer().startPlayback(loggingFile);
				}
				catch (final IOException e) {
					log.warning(e.toString());
					e.printStackTrace();
				}

			}
			loggingEnabled = false;
		}

		fixLoggingControls();
		return loggingFile;
	} // doesn't actually reset the test in the dialog'

	class ResetFileButton extends JButton {

		/**
		 *
		 */
		private static final long serialVersionUID = -4569337576300628493L;
		String fn;

		ResetFileButton(final String fn, final JFileChooser chooser) {
			this.fn = fn;
			setText("Reset filename");
			addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(final ActionEvent e) {
					System.out.println("reset file");
					chooser.setSelectedFile(new File(fn));
				}
			});
		}
	}

	@Override
	public String toString() {
		return getTitle();
	}

	private void changeAEBufferSizeMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_changeAEBufferSizeMenuItemActionPerformed
		if (aemon == null) {
			JOptionPane.showMessageDialog(this, "No hardware interface open, can't set size", "Can't set buffer size",
				JOptionPane.WARNING_MESSAGE);
			return;

		}

		final String ans = JOptionPane.showInputDialog(this, "Enter size of render/capture exchange buffer in events",
			aemon.getAEBufferSize());
		try {
			final int n = Integer.parseInt(ans);
			aemon.setAEBufferSize(n);
		}
		catch (final NumberFormatException e) {
			Toolkit.getDefaultToolkit().beep();
		}
	}// GEN-LAST:event_changeAEBufferSizeMenuItemActionPerformed

	private void updateFirmwareMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_updateFirmwareMenuItemActionPerformed
		if (aemon == null) {
			return;
		}

		if (!(aemon instanceof HasUpdatableFirmware)) {
			JOptionPane.showMessageDialog(this, "Device does not have updatable firmware", "Firmware update failed",
				JOptionPane.WARNING_MESSAGE);
			return;

		}

		final int nDevices = HardwareInterfaceFactory.instance().getNumInterfacesAvailable();
		if (nDevices == 0) {
			JOptionPane.showMessageDialog(this, "No devices found", "Firmware update not possible",
				JOptionPane.WARNING_MESSAGE);
			return;
		}
		else if (nDevices > 1) {
			JOptionPane.showMessageDialog(this,
				"Firmware update is only allowed with a single device plugged in. There are " + nDevices
					+ " plugged in now.", "Firmware update not allowed with multiple devices",
				JOptionPane.WARNING_MESSAGE);
			return;
		}

		try {
			final HasUpdatableFirmware d = (HasUpdatableFirmware) aemon;

			final int DID = d.getVersion();
			final int ret = JOptionPane
				.showConfirmDialog(
					this,
					"<html>Current FX2 firmware device ID (firmware version number)="
						+ DID
						+ ": Are you sure you want to update the firmware?\nSee the menu item \"Help\\DVS128 Firmware Change Log\" if you are using a DVS128.",
					"Really update?", JOptionPane.YES_NO_OPTION);
			if (!(ret == JOptionPane.YES_OPTION)) {
				return;
			}

			d.updateFirmware(); // starts a thread in
								// cypressfx2dvs128hardwareinterface, shows
								// progress
		}
		catch (final Exception e) {
			log.warning(e.toString());
		}
		finally {
		}
	}// GEN-LAST:event_updateFirmwareMenuItemActionPerformed

	private void monSeqOpMode0ActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_monSeqOpMode0ActionPerformed
		if (aemon instanceof CypressFX2MonitorSequencer) {
			final CypressFX2MonitorSequencer fx = (CypressFX2MonitorSequencer) aemon;
			try {
				fx.setOperationMode(0);
				JOptionPane.showMessageDialog(this, "Timestamp tick set to " + fx.getOperationMode() + " us.");
			}
			catch (final Exception e) {
				e.printStackTrace();
				aemon.close();
			}

		}
	}// GEN-LAST:event_monSeqOpMode0ActionPerformed

	private void monSeqOpMode1ActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_monSeqOpMode1ActionPerformed
		if (aemon instanceof CypressFX2MonitorSequencer) {
			final CypressFX2MonitorSequencer fx = (CypressFX2MonitorSequencer) aemon;
			try {
				fx.setOperationMode(1);
				JOptionPane.showMessageDialog(this, "Timestamp tick set to " + fx.getOperationMode()
					+ " us. Note that jAER will treat the ticks as 1us anyway.");
			}
			catch (final Exception e) {
				e.printStackTrace();
				aemon.close();
			}
		}
	}// GEN-LAST:event_monSeqOpMode1ActionPerformed

	private void enableMissedEventsCheckBoxActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_enableMissedEventsCheckBoxActionPerformed
		if (aemon instanceof CypressFX2MonitorSequencer) {
			final CypressFX2MonitorSequencer fx = (CypressFX2MonitorSequencer) aemon;
			try {
				fx.enableMissedEvents(enableMissedEventsCheckBox.getState());
				// JOptionPane.showMessageDialog(this, "Timestamp tick set to "
				// + fx.getOperationMode() +
				// " us. Note that jAER will treat the ticks as 1us anyway.");
			}
			catch (final Exception e) {
				e.printStackTrace();
				aemon.close();
			}

		}
	}// GEN-LAST:event_enableMissedEventsCheckBoxActionPerformed

	private void calibrationStartStopActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_calibrationStartStopActionPerformed
		if (getRenderer() instanceof Calibratible) {
			((Calibratible) getRenderer()).setCalibrationInProgress(!((Calibratible) getRenderer())
				.isCalibrationInProgress());
			if (((Calibratible) getRenderer()).isCalibrationInProgress()) {
				calibrationStartStop.setText("Stop Calibration");
			}
			else {
				calibrationStartStop.setText("Start Calibration");
			}

		}
	}// GEN-LAST:event_calibrationStartStopActionPerformed

	private void setDefaultFirmwareMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_setDefaultFirmwareMenuItemActionPerformed
		final CypressFX2FirmwareFilennameChooserOkCancelDialog dialog = new CypressFX2FirmwareFilennameChooserOkCancelDialog(
			this, true, getChip());
		dialog.setVisible(true);
		final int v = dialog.getReturnStatus();
		if (v == CypressFX2FirmwareFilennameChooserOkCancelDialog.RET_OK) {
			getChip().setDefaultFirmwareBixFileForBlankDevice(dialog.getChosenFile());
			log.info("set default firmware file to " + getChip().getDefaultFirmwareBixFileForBlankDevice());
		}
	}// GEN-LAST:event_setDefaultFirmwareMenuItemActionPerformed

	private void refreshInterfaceMenuItemComponentShown(final java.awt.event.ComponentEvent evt) {// GEN-FIRST:event_refreshInterfaceMenuItemComponentShown
		// TODO not used apparently
	}// GEN-LAST:event_refreshInterfaceMenuItemComponentShown

	private void zoomInMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_zoomInMenuItemActionPerformed
		chip.getCanvas().zoomIn();
	}// GEN-LAST:event_zoomInMenuItemActionPerformed

	private void zoomOutMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_zoomOutMenuItemActionPerformed
		chip.getCanvas().zoomOut();
	}// GEN-LAST:event_zoomOutMenuItemActionPerformed

	private void zoomCenterMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_zoomCenterMenuItemActionPerformed
		chip.getCanvas().zoomCenter();
	}// GEN-LAST:event_zoomCenterMenuItemActionPerformed

	private void showConsoleOutputButtonActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_showConsoleOutputButtonActionPerformed
		// log.info("opening logging output window");
		// jaerViewer.globalDataViewer.setVisible(!jaerViewer.globalDataViewer.isVisible());
		loggingHandler.getConsoleWindow().setVisible(!loggingHandler.getConsoleWindow().isVisible());
	}// GEN-LAST:event_showConsoleOutputButtonActionPerformed

	private void setDefaultFirmwareMenuItemMouseEntered(final java.awt.event.MouseEvent evt) {// GEN-FIRST:event_setDefaultFirmwareMenuItemMouseEntered
		if (chip != null) {
			final String s = "<html>" + AEViewer.SET_DEFAULT_FIRMWARE_FOR_BLANK_DEVICE + "<br> (currently "
				+ chip.getDefaultFirmwareBixFileForBlankDevice() + ")";
			if (!s.equals(setDefaultFirmwareMenuItem.getToolTipText())) {
				setDefaultFirmwareMenuItem.setToolTipText(s);
			}
		}
	}// GEN-LAST:event_setDefaultFirmwareMenuItemMouseEntered

	private void multiModeButtonActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_multiModeButtonActionPerformed
		// TODO add your handling code here:
		jaerViewer.setViewMode(true);
		// jaerViewer.launchMultiModeViewer();
	}// GEN-LAST:event_multiModeButtonActionPerformed

	private void pauseRenderingCheckBoxMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_pauseRenderingCheckBoxMenuItemActionPerformed
		setPaused(pauseRenderingCheckBoxMenuItem.isSelected());
	}// GEN-LAST:event_pauseRenderingCheckBoxMenuItemActionPerformed

	private void exitMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_exitMenuItemActionPerformed
		if ((biasgenFrame != null) && !biasgenFrame.isModificationsSaved()) {
			return;
		}

		viewLoop.stopThread();
		cleanup();

		System.exit(0);
	}// GEN-LAST:event_exitMenuItemActionPerformed

	private void checkNonMonotonicTimeExceptionsEnabledCheckBoxMenuItemActionPerformed(
		final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_checkNonMonotonicTimeExceptionsEnabledCheckBoxMenuItemActionPerformed
		if (aePlayer != null) {
			aePlayer.setNonMonotonicTimeExceptionsChecked(checkNonMonotonicTimeExceptionsEnabledCheckBoxMenuItem
				.isSelected());
			AEViewer.prefs.putBoolean("AEViewer.checkNonMonotonicTimeExceptionsEnabled",
				checkNonMonotonicTimeExceptionsEnabledCheckBoxMenuItem.isSelected());
		}

		if ((aemon != null) && (aemon instanceof StereoPairHardwareInterface)) {
			((StereoPairHardwareInterface) aemon)
				.setIgnoreTimestampNonmonotonicity(checkNonMonotonicTimeExceptionsEnabledCheckBoxMenuItem.isSelected());
		}
		firePropertyChange(AEViewer.EVENT_CHECK_NONMONOTONIC_TIMESTAMPS, null,
			checkNonMonotonicTimeExceptionsEnabledCheckBoxMenuItem.isSelected());
	}// GEN-LAST:event_checkNonMonotonicTimeExceptionsEnabledCheckBoxMenuItemActionPerformed

	private void syncEnabledCheckBoxMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_syncEnabledCheckBoxMenuItemActionPerformed
		log.warning("no effect here - this event is handled by jAERViewer, not AEViewer");
	}// GEN-LAST:event_syncEnabledCheckBoxMenuItemActionPerformed

	private void openBlockingQueueInputMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_openBlockingQueueInputMenuItemActionPerformed
		blockingQueueInputEnabled = openBlockingQueueInputMenuItem.isSelected();
		if (blockingQueueInputEnabled) {
			try {
				blockingQueueInput = new ArrayBlockingQueue(100);
				setPlayMode(PlayMode.REMOTE);

				if ((getAeChipClass() == ch.unizh.ini.jaer.chip.retina.DVS128andCochleaAMS1b.class)
					&& (getJaerViewer().getNumViewers() < 2)) {
					// Start the cochlea viewer:
					final AEViewer cochleaViewer = new AEViewer(jaerViewer);
					cochleaViewer.setAeChipClass(ch.unizh.ini.jaer.chip.cochlea.CochleaAMS1b.class);
					final AEChip cochleaChip = cochleaViewer.getChip();

					// start the retina viewer:
					final AEViewer retinaViewer = new AEViewer(jaerViewer);
					retinaViewer.setAeChipClass(ch.unizh.ini.jaer.chip.retina.DVS128.class);
					final AEChip retinaChip = retinaViewer.getChip();

					final int n = HardwareInterfaceFactory.instance().getNumInterfacesAvailable();
					for (int i = 0; i < n; i++) {
						final HardwareInterface hw = HardwareInterfaceFactory.instance().getInterface(i);
						if (hw == null) {
							continue;
						} // in case it disappeared
						if (hw.toString().startsWith("CypressFX2")) {
							cochleaChip.setHardwareInterface(hw);
						}
						else if (hw.toString().startsWith("DVS128")) {
							retinaChip.setHardwareInterface(hw);
							retinaChip
								.addDefaultEventFilter(ch.unizh.ini.jaer.projects.cochsoundloc.multichipviewer.MultichipRetinaEventProducer.class);
							final ch.unizh.ini.jaer.projects.cochsoundloc.multichipviewer.MultichipRetinaEventProducer retinaFilter = (ch.unizh.ini.jaer.projects.cochsoundloc.multichipviewer.MultichipRetinaEventProducer) retinaChip
								.getFilterChain()
								.findFilter(
									ch.unizh.ini.jaer.projects.cochsoundloc.multichipviewer.MultichipRetinaEventProducer.class);
							retinaFilter.initFilter();
							retinaFilter.setFilterEnabled(true);
							retinaFilter.doFindAEViewerConsumer();
							retinaViewer.showFilters(true);
						}
						else if (hw.toString().startsWith("CochleaAMS1b")) {
							cochleaChip.setHardwareInterface(hw);
							cochleaChip
								.addDefaultEventFilter(ch.unizh.ini.jaer.projects.cochsoundloc.multichipviewer.MultichipAMS1bEventProducer.class);
							final ch.unizh.ini.jaer.projects.cochsoundloc.multichipviewer.MultichipAMS1bEventProducer cochleaFilter = (ch.unizh.ini.jaer.projects.cochsoundloc.multichipviewer.MultichipAMS1bEventProducer) cochleaChip
								.getFilterChain()
								.findFilter(
									ch.unizh.ini.jaer.projects.cochsoundloc.multichipviewer.MultichipAMS1bEventProducer.class);
							cochleaFilter.initFilter();
							cochleaFilter.setFilterEnabled(true);
							cochleaFilter.doFindAEViewerConsumer();
							cochleaViewer.showFilters(true);
						}
					}
					retinaViewer.setState(Frame.ICONIFIED);
					retinaViewer.setVisible(true);
					cochleaViewer.setState(Frame.ICONIFIED);
					cochleaViewer.setVisible(true);
				}
			}
			catch (final Exception e) {
				log.warning(e.getMessage());
				openBlockingQueueInputMenuItem.setSelected(false);
			}
		}
		else {
			if (getBlockingQueueInput() != null) {
				blockingQueueInput = null;
			}
			setPlayMode(PlayMode.WAITING);
		}
	}// GEN-LAST:event_openBlockingQueueInputMenuItemActionPerformed

	private void openUnicastInputMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_openUnicastInputMenuItemActionPerformed
		if (unicastInputEnabled) {

			closeUnicastInput();
			setPlayMode(PlayMode.WAITING);
		}
		else {
			try {
				unicastInput = new AEUnicastInput();
				addPropertyChangeListener(AEViewer.EVENT_PAUSED, unicastInput);
				final AEUnicastDialog dlg = new AEUnicastDialog(this, true, unicastInput);
				dlg.setVisible(true);
				final int ret = dlg.getReturnStatus();
				if (ret != AEUnicastDialog.RET_OK) {
					return;
				}
				unicastInput.open();
				setPlayMode(PlayMode.REMOTE);
				openUnicastInputMenuItem.setText("Close unicast input from " + unicastInput.getHost() + ":"
					+ unicastInput.getPort());
				log.info("opened unicast input " + unicastInput);
				unicastInputEnabled = true;

			}
			catch (final Exception e) {
				log.warning(e.toString());
				JOptionPane.showMessageDialog(this, "<html>Couldn't open AEUnicastInput input: <br>" + e.toString()
					+ "</html>");
			}

		}
	}// GEN-LAST:event_openUnicastInputMenuItemActionPerformed

	private void unicastOutputEnabledCheckBoxMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_unicastOutputEnabledCheckBoxMenuItemActionPerformed
		if (unicastOutputEnabled) {
			if (unicastOutput != null) {
				unicastOutput.close();
				log.info("closed " + unicastOutput);
				unicastOutput = null;

			}

			unicastOutputEnabled = false;
			// setPlayMode(PlayMode.WAITING); // don't stop live input or file
			// just because we stop output datagrams
		}
		else {
			try {
				unicastOutput = new AEUnicastOutput();
				final AEUnicastDialog dlg = new AEUnicastDialog(this, true, unicastOutput);
				dlg.setVisible(true);
				final int ret = dlg.getReturnStatus();
				if (ret != AEUnicastDialog.RET_OK) {
					return;
				}
				unicastOutput.open();
				log.info("opened unicast output " + unicastOutput);
				unicastOutputEnabled = true;

			}
			catch (final Exception e) {
				log.warning(e.toString());
				JOptionPane.showMessageDialog(this, "<html>Couldn't open AEUnicastOutput: <br>" + e.toString()
					+ "</html>");
			}

		}
	}// GEN-LAST:event_unicastOutputEnabledCheckBoxMenuItemActionPerformed

	private void openMulticastInputMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_openMulticastInputMenuItemActionPerformed
		multicastInputEnabled = openMulticastInputMenuItem.isSelected(); // TODO
																			// encapsulate
																			// in
																			// method
																			// so
																			// that
																			// stopme
																			// can
																			// call
																			// this
																			// close
																			// method
		if (multicastInputEnabled) {
			try {
				aeMulticastInput = new AEMulticastInput();
				aeMulticastInput.start();
				setPlayMode(PlayMode.REMOTE);
			}
			catch (final IOException e) {
				log.warning(e.getMessage());
				openMulticastInputMenuItem.setSelected(false);
			}
		}
		else {
			if (aeMulticastInput != null) { // TODO replace with close multicast
											// method that fixes menu items
				aeMulticastInput.close();
			}
			setPlayMode(PlayMode.WAITING);
		}
	}// GEN-LAST:event_openMulticastInputMenuItemActionPerformed

	private void multicastOutputEnabledCheckBoxMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_multicastOutputEnabledCheckBoxMenuItemActionPerformed
		multicastOutputEnabled = multicastOutputEnabledCheckBoxMenuItem.isSelected();
		if (multicastOutputEnabled) {
			aeMulticastOutput = new AEMulticastOutput();
		}
		else {
			if (aeMulticastOutput != null) {
				aeMulticastOutput.close();
			}
		}
	}// GEN-LAST:event_multicastOutputEnabledCheckBoxMenuItemActionPerformed

	private void serverSocketOptionsMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_serverSocketOptionsMenuItemActionPerformed
		if (aeServerSocket == null) {
			log.warning("null aeServerSocket");
			JOptionPane.showMessageDialog(this,
				"No server socket to configure - maybe port is already bound? (Check the output logging)",
				"No server socket", JOptionPane.WARNING_MESSAGE);
			return;
		}

		final AEServerSocketOptionsDialog dlg = new AEServerSocketOptionsDialog(this, true, aeServerSocket);
		dlg.setVisible(true);
		final int ret = dlg.getReturnStatus();
		if (ret != AEServerSocketOptionsDialog.RET_OK) {
			return;
		}

		// TODO change options on server socket and reopen it - presently need
		// to restart Viewer
	}// GEN-LAST:event_serverSocketOptionsMenuItemActionPerformed

	private void reopenSocketInputStreamMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_reopenSocketInputStreamMenuItemActionPerformed
		reopenSocketInputStream();
	}// GEN-LAST:event_reopenSocketInputStreamMenuItemActionPerformed

	private void openSocketOutputStreamMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_openSocketOutputStreamMenuItemActionPerformed
		// TODO add your handling code here:
		if (socketOutputEnabled) {
			closeAESocketClient();
			setPlayMode(PlayMode.WAITING);
		}
		else {
			try {
				aeSocketClient = new AESocket();
				final AESocketDialog dlg = new AESocketDialog(this, true, aeSocketClient);
				dlg.setVisible(true);
				final int ret = dlg.getReturnStatus();
				if (ret != AESocketDialog.RET_OK) {
					return;
				}
				aeSocketClient.connect();

				openSocketOutputStreamMenuItem.setText("Close socket output stream from " + aeSocketClient.getHost()
					+ ":" + aeSocketClient.getPort());
				log.info("opened socket output stream " + aeSocketClient);
				socketOutputEnabled = true;
			}
			catch (final Exception e) {
				log.warning(e.toString());
				JOptionPane.showMessageDialog(this, "<html>Couldn't open AESocket output stream: <br>" + e.toString()
					+ "</html>");
			}
		}
	}// GEN-LAST:event_openSocketOutputStreamMenuItemActionPerformed

	private void openSocketInputStreamMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_openSocketInputStreamMenuItemActionPerformed
		if (socketInputEnabled) {
			closeAESocket();
			setPlayMode(PlayMode.WAITING);
		}
		else {
			try {
				aeSocket = new AESocket();
				final AESocketDialog dlg = new AESocketDialog(this, true, aeSocket);
				dlg.setVisible(true);
				final int ret = dlg.getReturnStatus();
				if (ret != AESocketDialog.RET_OK) {
					return;
				}
				aeSocket.connect();
				setPlayMode(PlayMode.REMOTE);
				openSocketInputStreamMenuItem.setText("Close socket input stream from " + aeSocket.getHost() + ":"
					+ aeSocket.getPort());
				// reopenSocketInputStreamMenuItem.setEnabled(true);
				log.info("opened socket input stream " + aeSocket);
				socketInputEnabled = true;
			}
			catch (final Exception e) {
				log.warning(e.toString());
				JOptionPane.showMessageDialog(this, "<html>Couldn't open AESocket input stream: <br>" + e.toString()
					+ "</html>");
			}
		}
		// if(socketInputStream==null){
		// try{
		//
		// // socketInputStream=new AEUnicastInput();
		// String
		// host=JOptionPane.showInputDialog(this,"Hostname to receive from",socketInputStream.getHost());
		// if(host==null) return;
		// aeSocket=new AESocket(host);
		// // socketInputStream.setHost(host);
		// // socketInputStream.start();
		// setPlayMode(PlayMode.REMOTE);
		// openSocketInputStreamMenuItem.setText("Close socket input stream");
		// }catch(Exception e){
		// e.printStackTrace();
		// }
		// }else{
		// if(aeSocket!=null){
		// aeSocket
		// }
		// if(socketInputStream!=null){
		// socketInputStream.close();
		// socketInputStream=null;
		// }
		// log.info("set socketInputStream to null");
		// openSocketInputStreamMenuItem.setText("Open socket input stream");
		// }
	}// GEN-LAST:event_openSocketInputStreamMenuItemActionPerformed

	private void logFilteredEventsCheckBoxMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_logFilteredEventsCheckBoxMenuItemActionPerformed
		setLogFilteredEventsEnabled(logFilteredEventsCheckBoxMenuItem.isSelected());
	}// GEN-LAST:event_logFilteredEventsCheckBoxMenuItemActionPerformed

	private void loggingSetTimelimitMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_loggingSetTimelimitMenuItemActionPerformed
		final String ans = JOptionPane.showInputDialog(this, "Enter logging time limit in ms (0 for no limit)",
			loggingTimeLimit);
		try {
			final int n = Integer.parseInt(ans);
			loggingTimeLimit = n;
		}
		catch (final NumberFormatException e) {
			Toolkit.getDefaultToolkit().beep();
		}
	}// GEN-LAST:event_loggingSetTimelimitMenuItemActionPerformed

	private void loggingPlaybackImmediatelyCheckBoxMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_loggingPlaybackImmediatelyCheckBoxMenuItemActionPerformed
		setLoggingPlaybackImmediatelyEnabled(!isLoggingPlaybackImmediatelyEnabled());
	}// GEN-LAST:event_loggingPlaybackImmediatelyCheckBoxMenuItemActionPerformed

	private void timestampResetBitmaskMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_timestampResetBitmaskMenuItemActionPerformed
		final String ret = (String) JOptionPane
			.showInputDialog(
				this,
				"<html>Enter hex value bitmask for zeroing timestamps, e.g. 8000<br>Whenever any of these bits are set, the time will be zeroed at this point,<br> and subsequent timestamps will have this one subtracted from it.<br>The file must be opened after the mask is set.",
				"Timestamp reset bitmask value", JOptionPane.QUESTION_MESSAGE, null, null,
				Integer.toHexString(aeFileInputStreamTimestampResetBitmask));
		if (ret == null) {
			return;
		}
		try {
			aeFileInputStreamTimestampResetBitmask = Integer.parseInt(ret, 16);
			AEViewer.prefs.putInt("AEViewer.aeFileInputStreamTimestampResetBitmask",
				aeFileInputStreamTimestampResetBitmask);
			log.info("set aeFileInputStreamTimestampResetBitmask="
				+ HexString.toString(aeFileInputStreamTimestampResetBitmask));
			timestampResetBitmaskMenuItem.setText("Set timestamp reset bitmask... (currently 0x"
				+ Integer.toHexString(aeFileInputStreamTimestampResetBitmask) + ")");
		}
		catch (final Exception e) {
			log.warning(e.toString());
		}
	}// GEN-LAST:event_timestampResetBitmaskMenuItemActionPerformed

	private void closeMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_closeMenuItemActionPerformed
		stopMe();
	}// GEN-LAST:event_closeMenuItemActionPerformed

	private void openMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_openMenuItemActionPerformed
		/* getAePlayer(). */aePlayer.openAEInputFileDialog();
	}// GEN-LAST:event_openMenuItemActionPerformed

	private void newViewerMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_newViewerMenuItemActionPerformed
		new AEViewer(jaerViewer).setVisible(true);
	}// GEN-LAST:event_newViewerMenuItemActionPerformed

	private void interfaceMenuMenuSelected(final javax.swing.event.MenuEvent evt) {// GEN-FIRST:event_interfaceMenuMenuSelected
		buildInterfaceMenu();
	}// GEN-LAST:event_interfaceMenuMenuSelected

	/**
	 * Returns desired frame rate of FrameRater
	 *
	 * @return desired frame rate in Hz.
	 */
	public int getFrameRate() {
		return frameRater.getDesiredFPS();
	}

	/**
	 * Sets desired frame rate of FrameRater
	 *
	 * @param renderDesiredFrameRateHz
	 *            frame rate in Hz
	 */
	public void setFrameRate(final int renderDesiredFrameRateHz) {
		frameRater.setDesiredFPS(renderDesiredFrameRateHz);
	}

	/**
	 * Returns true if viewer is paused
	 *
	 * @return true if paused
	 */
	public boolean isPaused() {
		if (jaerViewer == null) {
			return false; // not yet initialized
		}
		return jaerViewer.getSyncPlayer().isPaused();
	}

	/**
	 * Sets paused. If viewing is synchronized, then all viwewers will be
	 * paused.
	 * Fires PropertyChangeEvent "paused". Interrupts the ViewLoop.
	 *
	 * @param paused
	 *            true to pause
	 */
	public void setPaused(final boolean paused) {
		// log.info("settings paused=" + paused);
		final boolean old = isPaused();
		jaerViewer.getSyncPlayer().setPaused(paused);
		pauseRenderingCheckBoxMenuItem.setSelected(paused);
		interruptViewloop(); // to break out of exchangeers that might be
								// waiting, problem is that it also interrupts a
								// singleStep ....
		firePropertyChange(AEViewer.EVENT_PAUSED, old, isPaused());
	}

	public boolean isActiveRenderingEnabled() {
		return activeRenderingEnabled;
	}

	public void setActiveRenderingEnabled(final boolean activeRenderingEnabled) {
		this.activeRenderingEnabled = activeRenderingEnabled;
		AEViewer.prefs.putBoolean("AEViewer.activeRenderingEnabled", activeRenderingEnabled);
	}

	/**
	 * Drag and drop data file onto frame to play it.
	 * Called while a drag operation is ongoing, when the mouse pointer
	 * enters the operable part of the drop site for the DropTarget registered
	 * with this listener.
	 *
	 * @param dtde
	 *            the event.
	 * */
	@Override
	public void dragEnter(final DropTargetDragEvent dtde) {
		final Transferable transferable = dtde.getTransferable();
		try {
			if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
				final java.util.List<File> files = (java.util.List<File>) transferable
					.getTransferData(DataFlavor.javaFileListFlavor);
				for (final File f : files) {
					if (f.getName().endsWith(AEDataFile.DATA_FILE_EXTENSION)
						|| f.getName().endsWith(AEDataFile.INDEX_FILE_EXTENSION)
						|| f.getName().endsWith(AEDataFile.OLD_DATA_FILE_EXTENSION)
						|| f.getName().endsWith(AEDataFile.OLD_INDEX_FILE_EXTENSION)) {
						draggedFile = f;
					}
					else {
						draggedFile = null;
					}

				}
				// System.out.println("AEViewer.dragEnter(): draqged file="+draggedFile);
			}
		}
		catch (final UnsupportedFlavorException e) {
			e.printStackTrace();
		}
		catch (final IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Called while a drag operation is ongoing,
	 * when the mouse pointer has exited the operable part of the
	 * drop site for the DropTarget registered with this listener.
	 *
	 * @param dte
	 *            the event.
	 */
	@Override
	public void dragExit(final DropTargetEvent dte) {
		draggedFile = null;
	}

	// Called when a drag operation is ongoing, while the mouse pointer is still
	// over the operable part of the drop site
	// for the DropTarget registered with this listener.

	@Override
	public void dragOver(final DropTargetDragEvent dtde) {
	}

	/**
	 * Called when the drag operation has terminated with a drop on the
	 * operable part of the drop site for the DropTarget
	 * registered with this listener.
	 *
	 * @param dtde
	 *            the drop event.
	 */
	@Override
	public void drop(final DropTargetDropEvent dtde) {
		if (draggedFile != null) {
			// log.info("AEViewer.drop(): opening file "+draggedFile);
			try {
				recentFiles.addFile(draggedFile);
				getAePlayer().startPlayback(draggedFile);
			}
			catch (final IOException e) {
				log.warning(e.toString());
				e.printStackTrace();
			}

		}
	}

	// Called if the user has modified the current drop gesture.
	@Override
	public void dropActionChanged(final DropTargetDragEvent dtde) {
	}

	public boolean isLoggingPlaybackImmediatelyEnabled() {
		return loggingPlaybackImmediatelyEnabled;
	}

	public void setLoggingPlaybackImmediatelyEnabled(final boolean loggingPlaybackImmediatelyEnabled) {
		this.loggingPlaybackImmediatelyEnabled = loggingPlaybackImmediatelyEnabled;
		AEViewer.prefs.putBoolean("AEViewer.loggingPlaybackImmediatelyEnabled", loggingPlaybackImmediatelyEnabled);
	}

	/** @return the chip we are displaying */
	public AEChip getChip() {
		return chip;
	}

	public void setChip(final AEChip chip) {
		if (chip != this.chip) {
			this.chip = chip;

			getChip().setAeViewer(this); // set this now so that chip has
											// AEViewer for building
											// BiasgenFrame etc
											// properly
			extractor = chip.getEventExtractor();
			renderer = chip.getRenderer();

			extractor.setSubsamplingEnabled(subsampleEnabledCheckBoxMenuItem.isSelected());
			extractor.setSubsampleThresholdEventCount(getRenderer().getSubsampleThresholdEventCount()); // awkward
																										// connection
																										// between
																										// components
																										// here
																										// -
																										// ideally
																										// chip
																										// should
																										// contrain
																										// info
																										// about
																										// subsample
																										// limit
		}

	}

	public boolean isRenderBlankFramesEnabled() {
		return renderBlankFramesEnabled;
	}

	public void setRenderBlankFramesEnabled(final boolean renderBlankFramesEnabled) {
		this.renderBlankFramesEnabled = renderBlankFramesEnabled;
		AEViewer.prefs.putBoolean("AEViewer.renderBlankFramesEnabled", renderBlankFramesEnabled);
		// log.info("renderBlankFramesEnabled="+renderBlankFramesEnabled);
	}

	public javax.swing.JMenu getFileMenu() {
		return fileMenu;
	}

	/** used in CaviarViewer to control sync'ed logging */
	public javax.swing.JMenuItem getLoggingMenuItem() {
		return loggingMenuItem;
	}

	public void setLoggingMenuItem(final javax.swing.JMenuItem loggingMenuItem) {
		this.loggingMenuItem = loggingMenuItem;
	}

	/**
	 * this toggle button is used in CaviarViewer to assign an action to start
	 * and stop logging for (possibly) all
	 * viewers
	 */
	public javax.swing.JToggleButton getLoggingButton() {
		return loggingButton;
	}

	public void setLoggingButton(final javax.swing.JToggleButton b) {
		loggingButton = b;
	}

	public JCheckBoxMenuItem getSyncEnabledCheckBoxMenuItem() {
		return syncEnabledCheckBoxMenuItem;
	}

	public void setSyncEnabledCheckBoxMenuItem(final javax.swing.JCheckBoxMenuItem syncEnabledCheckBoxMenuItem) {
		this.syncEnabledCheckBoxMenuItem = syncEnabledCheckBoxMenuItem;
	}

	/**
	 * Returns the proper AbstractAEPlayer: either the local AEPlayer or the
	 * delegated-to JAERViewer.SyncPlayer.
	 *
	 * @return the local player, unless we are part of a synchronized playback
	 *         gruop.
	 */
	public AbstractAEPlayer getAePlayer() {
		if ((jaerViewer == null) || !jaerViewer.isSyncEnabled() || (jaerViewer.getViewers().size() == 1)) {
			return aePlayer;
		}

		return jaerViewer.getSyncPlayer();
	}

	/**
	 * returns the playing mode
	 *
	 * @return the mode
	 */
	public PlayMode getPlayMode() {
		return playMode;
	}

	/**
	 * Sets mode, LIVE, PLAYBACK, WAITING, etc,
	 * sets window title, and fires property change event
	 *
	 * @param playMode
	 *            the new play mode
	 */
	public void setPlayMode(final PlayMode playMode) {
		// TODO there can be a race condition where user tries to open file,
		// this sets
		// playMode to PLAYBACK but run() method in ViewLoop sets it back to
		// WAITING or LIVE
		if (this.playMode.equals(playMode)) {
			return;
		}
		final PlayMode oldMode = this.playMode;
		log.info("Changing PlayMode from " + this.playMode + " to " + playMode);

		synchronized (viewLoop) {
			this.playMode = playMode;
			if (globalized) {
				getJaerViewer().globalViewer.refreshPlayingLocks();
			}
			interruptViewloop();
		}
		// if(this.playMode==PlayMode.WAITING){
		// log.info("went to waiting state");
		// }
		setTitleAccordingToState();
		fixLoggingControls();
		firePropertyChange(AEViewer.EVENT_PLAYMODE, oldMode.toString(), playMode.toString());
		// won't fire if old and new are the same,
		// e.g. playing a file and then start playing a new one
	}

	public boolean isLogFilteredEventsEnabled() {
		return logFilteredEventsEnabled;
	}

	public void setLogFilteredEventsEnabled(final boolean logFilteredEventsEnabled) {
		// log.info("logFilteredEventsEnabled="+logFilteredEventsEnabled);
		this.logFilteredEventsEnabled = logFilteredEventsEnabled;
		AEViewer.prefs.putBoolean("AEViewer.logFilteredEventsEnabled", logFilteredEventsEnabled);
		logFilteredEventsCheckBoxMenuItem.setSelected(logFilteredEventsEnabled);
	}

	/**
	 * Returns the enclosing JAERViewer, which is the top level object in jAER.
	 *
	 * @return the top-level owner
	 */
	public JAERViewer getJaerViewer() {
		return jaerViewer;
	}

	public void setJaerViewer(final JAERViewer jaerViewer) {
		this.jaerViewer = jaerViewer;
	}

	/**
	 * Finds the top-level menu named s.
	 *
	 * @param s
	 *            the text of the menu. If null, returns null.
	 * @return the menu, if there is one, or null if not found.
	 */
	public JMenu getMenu(final String s) {
		if (s == null) {
			return null;
		}
		final JMenuBar b = getJMenuBar();
		final int n = b.getMenuCount();
		for (int i = 0; i < n; i++) {
			final JMenu m = b.getMenu(i);
			if (m.getText().equals(s)) {
				return m;
			}
		}
		return null;
	}

	/**
	 * Adds (or replaces existing) JMenu to AEViewer, just before the Help menu.
	 *
	 * @param menu
	 *            the menu
	 */
	public void setMenu(final JMenu menu) {
		final JMenuBar b = getJMenuBar();
		final int n = b.getMenuCount();
		// check for existing and replace
		for (int i = 0; i < n; i++) {
			final JMenu m = b.getMenu(i);
			if ((m != null) && m.getText().equals(menu.getText())) {
				b.remove(i);
				b.add(menu, i);
				return;
			}
		}
		// otherwise add just before Help menu
		boolean didit = false;
		for (int i = 0; i < n; i++) {
			final JMenu m = b.getMenu(i);
			if ((m != null) && m.getText().equals("Help")) {
				b.add(menu, i);
				didit = true;
			}
		}
		if (!didit) { // if no help menu, add to end
			b.add(menu, -1);
		}
	}

	/**
	 * AEViewer makes a ServerSocket that accepts incoming connections. A
	 * connecting client
	 * gets served the events being rendered.
	 *
	 * @return the server socket. This holds the client socket.
	 */
	public AEServerSocket getAeServerSocket() {
		return aeServerSocket;
	}

	/**
	 * If we have opened a socket to a server of events, then this is it
	 *
	 * @return the input socket
	 */
	public AESocket getAeSocket() {
		return aeSocket;
	}

	public AESocket getAeSocketClient() {
		return aeSocketClient;
	}

	/**
	 * gets the RecentFiles handler for use, e.g. in storing sychronized logging
	 * index files
	 *
	 * @return refernce to RecentFiles object
	 */
	public RecentFiles getRecentFiles() {
		return recentFiles;
	}

	/**
	 * @return the renderer
	 */
	protected AEChipRenderer getRenderer() {
		return chip.getRenderer();
	}

	public CyclicBarrier waitFlag;
	public boolean globalized;

	Ambassador ambassador = new Ambassador();

	/**
	 * Return the ambassador object from this AEViewer. It contains the methods
	 * for communication between threads
	 *
	 * @return
	 */
	public Ambassador getAmbassador() {
		return ambassador;
	}

	/** This class allows the AEviewer to serve as a global communicator. */
	public class Ambassador implements PacketStream {
		int id; // Number identifying this viewer

		JPanel displayPanel;

		public PlayMode getMode() {
			return getPlayMode();
		}

		public void setID(final int ident) {
			id = ident;
		}

		public void resetTimeStamps() {
			zeroTimestamps();
		}

		public JMenuBar getToolBar() {
			final JMenuBar jt = new JMenuBar();
			// JMenu jm=new JMenu("Interface");
			// buildInterfaceMenu(jm);

			jt.add(fileMenu);
			jt.add(viewMenu);
			// jt.add(deviceMenu);
			jt.add(interfaceMenu);
			jt.add(controlMenu);
			jt.add(monSeqMenu);

			return jt;
		}

		public void setPanel(final JPanel imagePanel) {
			// imagePanel.

			// Component canv=chip.getCanvas().getCanvas();
			//
			// canv.getParent().remove(canv);
			//
			// canv.setBounds(imagePanel.getBounds());
			// // imagePanel.add(chip.getCanvas().getCanvas());
			//
			// displayPanel=imagePanel;
			// imagePanel.add(canv);
			//
			//
			// throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public void setSemaphore(final CyclicBarrier barr) {
			waitFlag = barr;
		}

		/**
		 * Set the viewer as being "watched". This means its loop is
		 * synchronized
		 * with the global loop
		 *
		 * @param displayed
		 */
		public void setWatched(final boolean displayed) {
			globalized = displayed;
		}

		// @Override
		public Component getPanel() {
			// return getRootPane();
			return chip.getCanvas().getCanvas();
			// return displayPanel;
			// return chip.getCanvas().getCanvas();
		}

		@Override
		public EventPacket getPacket() {
			return packet;
		}

		@Override
		public String getName() {

			return "V" + id + ": " + chip.getClass().getSimpleName();
		}

		@Override
		public boolean isReady() {
			return true; // AEViewer streams are always considered ready.
		}

		public void display() { // chip.getRenderer().render(packet);
			viewLoop.renderPacket(packet);
		}

		@Override
		public boolean process() {
			throw new UnsupportedOperationException("This call should never be made");
		}

		// @Override
		// public void setDisplayEnabled(boolean state) {
		// // TODO: disable rendering
		// }

	}

	/**
	 * This method takes in an hardware interface and tries to find the
	 * appropriate chip class. You could use it before initializing an AEViewer.
	 *
	 * It will return null if it does not find the appropriate chipclassname.
	 *
	 * @return
	 */
	public static Class hardwareInterface2chipClassName(final HardwareInterface hw) {
		if (hw == null) {
			return null;
		}

		if (hw.toString().contains("DVS128")) {
			return ch.unizh.ini.jaer.chip.retina.DVS128.class;
		}
		else if (hw.toString().contains("Cochlea")) {
			return ch.unizh.ini.jaer.chip.cochlea.CochleaAMS1b.class;
		}
		else if (hw.toString().contains("Retina")) {
			return ch.unizh.ini.jaer.chip.retina.DVS128.class;
		}
		else {
			JOptionPane.showConfirmDialog(null, "Unknown hardware, cannot find appropriate chip class.",
				"null hardware", JOptionPane.WARNING_MESSAGE);
			Logger.getAnonymousLogger().warning("Unknown hardware, can't find chip class.");
			return null;
		}
	}

	// AEViewer is a Swing Component and already has PropertyChangeSupport!!!
	// /** AEViewer supports property change events. See the class description
	// for supported events
	// @return the support
	// */
	// public PropertyChangeSupport getSupport() {
	// return support;
	// }
	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JMenuItem aboutMenuItem;
	private javax.swing.JCheckBoxMenuItem acccumulateImageEnabledCheckBoxMenuItem;
	private javax.swing.JCheckBoxMenuItem autoscaleContrastEnabledCheckBoxMenuItem;
	private javax.swing.JToggleButton biasesToggleButton;
	private javax.swing.JPanel bottomPanel;
	private javax.swing.JPanel buttonsPanel;
	private javax.swing.JMenuItem calibrationStartStop;
	private javax.swing.JMenuItem changeAEBufferSizeMenuItem;
	private javax.swing.JCheckBoxMenuItem checkNonMonotonicTimeExceptionsEnabledCheckBoxMenuItem;
	private javax.swing.JMenuItem closeMenuItem;
	private javax.swing.JMenu controlMenu;
	private javax.swing.JMenuItem customizeDevicesMenuItem;
	private javax.swing.JMenuItem cycleColorRenderingMethodMenuItem;
	private javax.swing.JMenuItem cycleDisplayMethodButton;
	private javax.swing.JMenuItem cypressFX2EEPROMMenuItem;
	private javax.swing.JMenuItem cypressFX2EEPROMLibUsbMenuItem;
	private javax.swing.JMenuItem dataWindowMenu;
	private javax.swing.JMenuItem decreaseBufferSizeMenuItem;
	private javax.swing.JMenuItem decreaseContrastMenuItem;
	private javax.swing.JMenuItem decreaseFrameRateMenuItem;
	private javax.swing.JMenuItem decreaseNumBuffersMenuItem;
	private javax.swing.JMenuItem decreasePlaybackSpeedMenuItem;
	private javax.swing.JMenu deviceMenu;
	private javax.swing.JSeparator deviceMenuSpparator;
	private javax.swing.JMenu displayMethodMenu;
	private javax.swing.JToggleButton dontRenderToggleButton;
	private javax.swing.JCheckBoxMenuItem enableFiltersOnStartupCheckBoxMenuItem;
	private javax.swing.JCheckBoxMenuItem enableMissedEventsCheckBox;
	private javax.swing.JMenuItem exitMenuItem;
	private javax.swing.JSeparator exitSeperator;
	private javax.swing.JMenu fileMenu;
	private javax.swing.JMenu filtersSubMenu;
	private javax.swing.JToggleButton filtersToggleButton;
	private javax.swing.JCheckBoxMenuItem flextimePlaybackEnabledCheckBoxMenuItem;
	private javax.swing.JMenu graphicsSubMenu;
	private javax.swing.JMenu helpMenu;
	private javax.swing.JPanel imagePanel;
	private javax.swing.JMenuItem increaseBufferSizeMenuItem;
	private javax.swing.JMenuItem increaseContrastMenuItem;
	private javax.swing.JMenuItem increaseFrameRateMenuItem;
	private javax.swing.JMenuItem increaseNumBuffersMenuItem;
	private javax.swing.JMenuItem increasePlaybackSpeedMenuItem;
	private javax.swing.JMenu interfaceMenu;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JProgressBar jProgressBar1;
	private javax.swing.JSeparator jSeparator1;
	private javax.swing.JSeparator jSeparator10;
	private javax.swing.JSeparator jSeparator11;
	private javax.swing.JSeparator jSeparator12;
	private javax.swing.JSeparator jSeparator13;
	private javax.swing.JSeparator jSeparator14;
	private javax.swing.JSeparator jSeparator15;
	private javax.swing.JSeparator jSeparator16;
	private javax.swing.JPopupMenu.Separator jSeparator17;
	private javax.swing.JSeparator jSeparator2;
	private javax.swing.JSeparator jSeparator3;
	private javax.swing.JSeparator jSeparator4;
	private javax.swing.JSeparator jSeparator5;
	private javax.swing.JSeparator jSeparator7;
	private javax.swing.JSeparator jSeparator8;
	private javax.swing.JSeparator jSeparator9;
	private javax.swing.JCheckBoxMenuItem logFilteredEventsCheckBoxMenuItem;
	private javax.swing.JToggleButton loggingButton;
	private javax.swing.JMenuItem loggingMenuItem;
	private javax.swing.JCheckBoxMenuItem loggingPlaybackImmediatelyCheckBoxMenuItem;
	private javax.swing.JMenuItem loggingSetTimelimitMenuItem;
	private javax.swing.JMenuItem measureTimeMenuItem;
	private javax.swing.JMenuBar menuBar;
	private javax.swing.JMenu monSeqMenu;
	private javax.swing.JMenuItem monSeqMissedEventsMenuItem;
	private javax.swing.JRadioButtonMenuItem monSeqOpMode0;
	private javax.swing.JRadioButtonMenuItem monSeqOpMode1;
	private javax.swing.ButtonGroup monSeqOpModeButtonGroup;
	private javax.swing.JMenu monSeqOperationModeMenu;
	private javax.swing.JToggleButton multiModeButton;
	private javax.swing.JCheckBoxMenuItem multicastOutputEnabledCheckBoxMenuItem;
	private javax.swing.JSeparator networkSeparator;
	private javax.swing.JMenuItem newViewerMenuItem;
	private javax.swing.JCheckBoxMenuItem openBlockingQueueInputMenuItem;
	private javax.swing.JMenuItem openMenuItem;
	private javax.swing.JCheckBoxMenuItem openMulticastInputMenuItem;
	private javax.swing.JMenuItem openSocketInputStreamMenuItem;
	private javax.swing.JMenuItem openSocketOutputStreamMenuItem;
	private javax.swing.JMenuItem openUnicastInputMenuItem;
	private javax.swing.JCheckBoxMenuItem pauseRenderingCheckBoxMenuItem;
	private javax.swing.JPanel playerControlPanel;
	private javax.swing.JMenuItem refreshInterfaceMenuItem;
	private javax.swing.JMenu remoteMenu;
	private javax.swing.ButtonGroup renderModeButtonGroup;
	private javax.swing.JMenuItem reopenSocketInputStreamMenuItem;
	private javax.swing.JLabel resizeLabel;
	private javax.swing.JPanel resizePanel;
	private javax.swing.JMenuItem rewindPlaybackMenuItem;
	private javax.swing.JMenuItem sequenceMenuItem;
	private javax.swing.JMenuItem serverSocketOptionsMenuItem;
	private javax.swing.JMenuItem setDefaultFirmwareMenuItem;
	private javax.swing.JButton showConsoleOutputButton;
	private javax.swing.JCheckBoxMenuItem skipPacketsRenderingCheckBoxMenuItem;
	private javax.swing.JPanel statisticsPanel;
	private javax.swing.JTextField statusTextField;
	private javax.swing.JMenuItem subSampleSizeMenuItem;
	private javax.swing.JCheckBoxMenuItem subsampleEnabledCheckBoxMenuItem;
	private javax.swing.JCheckBoxMenuItem syncEnabledCheckBoxMenuItem;
	private javax.swing.JSeparator syncSeperator;
	private javax.swing.JMenuItem timestampResetBitmaskMenuItem;
	private javax.swing.JCheckBoxMenuItem toggleMarkCheckBoxMenuItem;
	private javax.swing.JMenuItem togglePlaybackDirectionMenuItem;
	private javax.swing.JCheckBoxMenuItem unicastOutputEnabledCheckBoxMenuItem;
	private javax.swing.JMenuItem unzoomMenuItem;
	private javax.swing.JMenuItem updateFirmwareMenuItem;
	private javax.swing.JCheckBoxMenuItem viewActiveRenderingEnabledMenuItem;
	private javax.swing.JMenuItem viewBiasesMenuItem;
	private javax.swing.JMenuItem viewFiltersMenuItem;
	private javax.swing.JCheckBoxMenuItem viewIgnorePolarityCheckBoxMenuItem;
	private javax.swing.JMenu viewMenu;
	private javax.swing.JCheckBoxMenuItem viewRenderBlankFramesCheckBoxMenuItem;
	private javax.swing.JMenuItem viewSingleStepMenuItem;
	private javax.swing.JMenuItem zeroTimestampsMenuItem;
	private javax.swing.JMenuItem zoomCenterMenuItem;
	private javax.swing.JMenuItem zoomInMenuItem;
	private javax.swing.JMenuItem zoomOutMenuItem;
	// End of variables declaration//GEN-END:variables
}
