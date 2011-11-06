package alpv_ws1112.ub1.webradio.ui.swing;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import alpv_ws1112.ub1.webradio.communication.Client;
import alpv_ws1112.ub1.webradio.ui.ClientUI;

/**
 * GUI for controlling the client
 */
public class ClientSwing extends JFrame implements ClientUI {

	private static final long serialVersionUID = 1L;
	private String _username;
	private Client _client;

	protected JTextField inputTextField;
	protected JTextArea textArea;

	public ClientSwing(Client client, String username) {
		super();
		_username = username;
		_client = client;
	}

	@Override
	public String getUserName() {
		return _username;
	}

	@Override
	public void pushChatMessage(String message) {
		textArea.append(message + "\n");
	}

	public void sendChatMessage(String message) {
		try {
			_client.sendChatMessage(message);
		} catch (IOException e) {
			textArea.append("Message could not be send. Error: "
					+ e.getMessage() + "\n");
			e.printStackTrace();
		}
	}

	@Override
	/**
	 * Starts the GUI.
	 */
	public void run() {
		this.setSize(400, 600);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new Exit());

		JPanel panel = new JPanel(new BorderLayout());
		add(panel);

		textArea = new JTextArea(5, 20);
		textArea.setEditable(false);
		panel.add(textArea);
		inputTextField = new JTextField(2);
		inputTextField.addActionListener(sendMessageListener);
		panel.add(inputTextField, BorderLayout.PAGE_END);

		setVisible(true);
	}

	/**
	 * Send message if "enter" was pressed.
	 */
	private ActionListener sendMessageListener = new ActionListener() {
		public void actionPerformed(ActionEvent evt) {
			String line = inputTextField.getText();
			if ((line.trim()).length() > 0) {
				sendChatMessage(line);
			}
			inputTextField.setText("");
			textArea.setCaretPosition(textArea.getDocument().getLength());
		}
	};

	/**
	 * Close the client socket before closing the GUI.
	 */
	private class Exit extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			System.out.println("exiting");
			_client.close();
			System.exit(0);
		}
	}
}
