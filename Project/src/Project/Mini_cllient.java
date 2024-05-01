package Project;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.ws4d.coap.core.CoapClient;
import org.ws4d.coap.core.CoapConstants;
import org.ws4d.coap.core.connection.BasicCoapChannelManager;
import org.ws4d.coap.core.connection.api.CoapChannelManager;
import org.ws4d.coap.core.connection.api.CoapClientChannel;
import org.ws4d.coap.core.enumerations.CoapMediaType;
import org.ws4d.coap.core.enumerations.CoapRequestCode;
import org.ws4d.coap.core.messages.api.CoapRequest;
import org.ws4d.coap.core.messages.api.CoapResponse;
import org.ws4d.coap.core.rest.CoapData;
import org.ws4d.coap.core.tools.Encoder;


public class Mini_cllient extends JFrame implements CoapClient{
	private static final boolean exitAfterResponse = false;
	
	JButton btn_start = new JButton("Start Monitoring");
	JButton btn_send = new JButton("Send MSG");
	
	JLabel payload_label = new JLabel("Sending Message");
	JTextArea payload_text = new JTextArea("", 1,1);
	
	JTextArea display_text = new JTextArea();
	JScrollPane display_text_jp  = new JScrollPane(display_text);
	JLabel display_label = new JLabel("Display");
	
	CoapClientChannel clientChannel = null;
	
	int warning_cnt = 0;
	boolean detection_flag = false;

	
	public Mini_cllient (String serverAddress, int serverPort) {
		//
		super("GUI client");
		//
		this.setLayout(null);
		String sAddress = serverAddress;
		int sPort = serverPort;

		CoapChannelManager channelManager = BasicCoapChannelManager.getInstance();

		try {
			clientChannel = channelManager.connect(this, InetAddress.getByName(sAddress), sPort);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(-1);
		}


		if (null == clientChannel) {
			return;
		}

		// btn
		btn_start.setBounds(20, 670, 300, 50);
		btn_send.setBounds(430, 670, 300, 50);

		
		btn_start.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				String path = "/pir";
				String payload = payload_text.getText();
				CoapRequest request = clientChannel.createRequest(CoapRequestCode.GET, path, true);
				request.setToken(Encoder.StringToByte("ObToken"));
				request.setObserveOption(0);
				displayRequest(request);
				clientChannel.sendMessage(request);
				display_text.append(System.lineSeparator());
				display_text.append("START Traffic Light SYSTEM");
				display_text.append(System.lineSeparator());
			}
		});

		btn_send.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				String path = "lcd";
				String payload = payload_text.getText();
				CoapRequest request = clientChannel.createRequest(CoapRequestCode.PUT, path, true);
				request.setPayload(new CoapData(payload, CoapMediaType.text_plain));
				displayRequest(request);
				clientChannel.sendMessage(request);
				display_text.append(System.lineSeparator());
				display_text.append("DISPLAY:"+payload);
				display_text.append(System.lineSeparator());
			}
		});
		
		payload_label.setBounds(20, 570, 350, 30);
		payload_text.setBounds(20, 600, 440, 30);
		payload_text.setFont(new Font("arian", Font.BOLD, 15));
		
		display_label.setBounds(20, 10, 100, 20);
		display_text.setLineWrap(true);
		display_text.setFont(new Font("arian", Font.BOLD, 15));
		display_text_jp.setBounds(20, 40, 740, 430);
		
				
		this.add(btn_start);
		this.add(btn_send);
		this.add(payload_label);
		this.add(payload_text);
		this.add(display_text_jp);
		this.add(display_label);

		//	
		this.setSize(800, 800);

		//
		this.setVisible(true);

		//
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	@Override
	public void onConnectionFailed(CoapClientChannel channel, boolean notReachable, boolean resetByServer) {
		System.out.println("Connection Failed");
		System.exit(-1);
	}

	@Override
	public void onResponse(CoapClientChannel channel, CoapResponse response) {
		if (Encoder.ByteToString(response.getToken()).equals("ObToken")) {
			chainging_Light(Encoder.ByteToString(response.getPayload()));
		}
	}
	
	public void chainging_Light(String msg) {
		if(msg.equals("Yellow Light")) {
			display_text.append(System.lineSeparator());
			display_text.append("Yellow Light");
			display_text.append(System.lineSeparator());
			CoapRequest request = clientChannel.createRequest(CoapRequestCode.PUT, "/lcd", true);
			request.setPayload(new CoapData("Please Wait", CoapMediaType.text_plain));
			displayRequest(request);
			clientChannel.sendMessage(request);
		}
		else if (msg.equals("Red Light")){
			display_text.append(System.lineSeparator());
			display_text.append("Red Light");
			display_text.append(System.lineSeparator());
			CoapRequest request = clientChannel.createRequest(CoapRequestCode.PUT, "/lcd", true);
			request.setPayload(new CoapData("Please Cross", CoapMediaType.text_plain));
			displayRequest(request);
			clientChannel.sendMessage(request);
		}
		else {
			display_text.append(System.lineSeparator());
			display_text.append("Green Light");
			display_text.append(System.lineSeparator());
			CoapRequest request = clientChannel.createRequest(CoapRequestCode.PUT, "/lcd", true);
			request.setPayload(new CoapData("Press The Button", CoapMediaType.text_plain));
			displayRequest(request);
			clientChannel.sendMessage(request);
		}
	}
	
	@Override
	public void onMCResponse(CoapClientChannel channel, CoapResponse response, InetAddress srcAddress, int srcPort) {
		// TODO Auto-generated method stub
	}
	
	private void displayRequest(CoapRequest request){
		//if(request.getPayload() != null){
		//	display_text.append("Request: "+request.toString() + " payload: " + Encoder.ByteToString(request.getPayload()) + " resource: " + request.getUriPath());
		//	display_text.setCaretPosition(display_text.getDocument().getLength());  
		//} 
		//else{
		//	display_text.append("Request: "+request.toString() + " resource: " + request.getUriPath());
		//	display_text.setCaretPosition(display_text.getDocument().getLength());  
		//}
		//display_text.append(System.lineSeparator());
		//display_text.append("");
		//display_text.append(System.lineSeparator());
	}
	

	public static void main(String[] args){
		// GUI client
		Mini_cllient gui = new Mini_cllient("fe80::d26e:742:ea82:b1e6", CoapConstants.COAP_DEFAULT_PORT);
	}
	
	
}
