package Project;

import java.util.List;

import org.ws4d.coap.core.enumerations.CoapMediaType;
import org.ws4d.coap.core.rest.BasicCoapResource;
import org.ws4d.coap.core.rest.CoapData;
import org.ws4d.coap.core.tools.Encoder;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

import Project.I2CLCD;

public class PIR_sensor extends BasicCoapResource{
	private String state = "Green Light";
	
	GpioController gpio = GpioFactory.getInstance();
	// PIR Sensor
	GpioPinDigitalInput pir = gpio.provisionDigitalInputPin(RaspiPin.GPIO_00);
	// Button
	GpioPinDigitalInput btn = gpio.provisionDigitalInputPin(RaspiPin.GPIO_29);
	// LED
	GpioPinDigitalOutput r_led = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_23, PinState.LOW);
	GpioPinDigitalOutput g_led = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_24, PinState.LOW);
	GpioPinDigitalOutput b_led = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_25, PinState.LOW);
	
	private PIR_sensor(String path, String value, CoapMediaType mediaType) {
		super(path, value, mediaType);
	}

	public PIR_sensor() {
		this("/pir", "Green Light", CoapMediaType.text_plain);
	}

	@Override
	public synchronized CoapData get(List<String> query, List<CoapMediaType> mediaTypesAccepted) {
		return get(mediaTypesAccepted);
	}
	
	@Override
	public synchronized CoapData get(List<CoapMediaType> mediaTypesAccepted) {
		boolean pir_state = pir.isHigh();	// PIR 센서 감지를 확인하는 변수
		boolean btn_pressed = btn.isHigh();	// 버튼 눌림을 확인하는 변수
		
		if (pir_state == true) {
			System.out.println("Person O");
			if (btn_pressed == true) {
				try {
					// Blue Light --> 3 Sec
					this.state = "Yellow Light";
					this.changed(this.state);
					SetLED();
					Thread.sleep(3000);
				
					// Red Light --> 5 Sec
					this.state = "Red Light";
					this.changed(this.state);
					SetLED();
					Thread.sleep(5000);
				
					this.state = "Green Light";
					this.changed(this.state);
					SetLED();
				} catch (Exception e) {
				
				}
			}
		}
		else {
			System.out.println("Person X");
			this.state = "Green Light";
			this.changed(this.state);
			SetLED();
		}
		
		return new CoapData(Encoder.StringToByte(this.state), CoapMediaType.text_plain);
	}
	
	public void SetLED() {

		if (this.state.equals("Red Light")) {
			r_led.high();
			g_led.low();
			b_led.low();
		} else if (this.state.equals("Green Light")) {
			r_led.low();
			g_led.high();
			b_led.low();
		} else if (this.state.equals("Yellow Light")) {
			r_led.high();
			g_led.high();
			b_led.low();
		}
	}
	
	@Override
	public synchronized boolean setValue(byte[] value) {
		this.state = Encoder.ByteToString(value);
		return true;
	}	
	
	@Override
	public synchronized boolean post(byte[] data, CoapMediaType type) {
		return this.setValue(data);
	}

	@Override
	public synchronized boolean put(byte[] data, CoapMediaType type) {
		return this.setValue(data);
	}
	
	@Override
	public synchronized String getResourceType() {
		return "Raspberry pi 4 PIR Sensor";
	}
}