package alpv_ws1112.ub1.webradio.ui.cmd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import alpv_ws1112.ub1.webradio.ui.ServerUI;

public class ServerCMD implements ServerUI {

	@Override
	public void run() {
		System.out.print("Welcome to webradio server.\n" +
				"Possible commands:\n" +
				"QUIT - shutdown the server\n");
		
		while(true) {
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
				String line = br.readLine();
				br.close();
				
				if(line.equals("QUIT")) {
					break;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		System.out.println("Shutdown the server.");
	}
}
