package alpv_ws1112.ub1.webradio.ui.cmd;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import alpv_ws1112.ub1.webradio.ui.ClientUI;

public class ClientCMD implements ClientUI {

	private String _username;
	
	public ClientCMD(String username) {
		_username = username;
	}
	
	@Override
	public void run() {
		
		System.out.print("Welcome to webradio client.\n" +
				"Possible commands:\n" +
				"N: <new_name> - to change your nickname\n" +
				"QUIT - quite the client\n");
		
		while(true) {
			
			String line;
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
				line = br.readLine();
				
				// Change nickname
				if(line.startsWith("N: ")) {
					_username = line.substring(3);
				}
				else if(line.equals("QUIT")) {
					break;
				}
				
				br.close();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		System.out.println("Shutdown the client.");
		
	}

	public String getUserName() {
		return _username;
	}

	@Override
	public void pushChatMessage(String message) {
		// TODO Auto-generated method stub
		
	}

}
