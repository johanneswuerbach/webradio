package alpv_ws1112.ub1.webradio.ui.swing;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;

import alpv_ws1112.ub1.webradio.communication.Server;
import alpv_ws1112.ub1.webradio.ui.ServerUI;

/**
 * GUI for controlling the client
 */
public class ServerSwing extends JFrame implements ServerUI {

	private static final long serialVersionUID = 1L;

	protected JButton _selectSoundFileButton;
	private Server _server;
	private JFileChooser fileDialog;
	private ActionListener _buttonListener = new ActionListener() {
		public void actionPerformed(ActionEvent evt) {
			System.out.println("foo");
			int returnVal = fileDialog.showOpenDialog(getParent());
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fileDialog.getSelectedFile();
				try {
					System.out.println("New audio file selected: "
							+ file.getCanonicalPath());
					_server.playSong(file.getCanonicalPath());
				} catch (IOException e) {
					System.err.println("Can't choose new audio file.");
				} catch (UnsupportedAudioFileException e) {
					System.err.println("File type not supported.");
				}
			}
		}
	};

	private Thread _serverThread;

	public ServerSwing(Server server, Thread serverThread) {
		_server = server;
		_serverThread = serverThread;
		fileDialog = new JFileChooser();
	}

	@Override
	/**
	 * Starts the GUI.
	 */
	public void run() {
		this.setSize(400, 200);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new Exit());

		JPanel panel = new JPanel(new BorderLayout());
		add(panel);

		_selectSoundFileButton = new JButton("Select Sound File");
		_selectSoundFileButton.addActionListener(_buttonListener);
		panel.add(_selectSoundFileButton);

		setVisible(true);
	}

	/**
	 * Close the client socket before closing the GUI.
	 */
	private class Exit extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			_server.close();
			System.out.println("Shutdown the server.");
			System.exit(0);
		}
	}
}
