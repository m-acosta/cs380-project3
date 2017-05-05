import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * @author Michael Acosta
 *
 */
public class Ipv4Client {

	public static void main(String[] args) {
		try {
			Socket mySocket = new Socket("codebank.xyz", 38003);
			for (int i = 2; i <= 4096; i *= 2) {
				String s = "";
				s += "0100"; // Version
				s += "0101"; // HLen
				s += "00000000"; // TOS
				s += String.format("%16s", Integer.toBinaryString(20 + i)).replace(" ", "0"); // Length
				s += "0000000000000000"; // Ident
				s += "010"; // Flags
				s += "0000000000000"; // Offset
				s += "00110010"; // TTL, 50
				s += "00000110"; // Protocol, TCP
				s += "0000000000000000"; // Checksum, all zeros for its own purposes
				s += "00000000000000000000000000000000"; // SourceAddr
				byte[] b = InetAddress.getByName("codebank.xyz").getAddress(); // DestinationAddr
				byte[] packet = new byte[20 + i]; // Data segment is allocated with all 0s
				
				// Convert binary string to an array of bytes
				int j = 0, k = 0;
				while (j + 8 <= s.length()) {
					packet[k] = (byte)Integer.parseInt(s.substring(j, j + 8), 2);
					j += 8;
					k++;
				}
				
				// Copy destination address array into packet array
				for (int x = 0; x < b.length; x++) {
					packet[16 + x] = b[x];
				}
				
				// Add checksum back into the packet
				short checkSum = checksum(packet);
				packet[10] = (byte) (checkSum >> 8);
				packet[11] = (byte) checkSum;
				
				DataOutputStream output = new DataOutputStream(mySocket.getOutputStream());
				output.write(packet);
				
				// Print out the response from the server
				DataInputStream input = new DataInputStream(mySocket.getInputStream());
				System.out.println("data length: " + i);
				System.out.println(input.readLine());
			}
			mySocket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static short checksum(byte[] b) {
		int sum = 0, i = 0, count = b.length;
		
		while (count > 1) {
			sum += ((b[i] << 8) & 0xFF00 | (b[i + 1]) & 0xFF);
			if ((sum & 0xFFFF0000) > 0) {
				sum &= 0xFFFF;
				sum++;
			}
			count -= 2;
			i += 2;
		}
		
		if (count > 0) {
			sum += ((b[i] << 8) & 0xFF00);
			if ((sum & 0xFFFF0000) > 0) {
				sum &= 0xFFFF;
				sum++;
			}
		}
		
		return (short) ~(sum & 0xFFFF);
	}
}