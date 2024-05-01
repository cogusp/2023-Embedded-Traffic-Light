package Project;

import java.util.List;

import org.ws4d.coap.core.enumerations.CoapMediaType;
import org.ws4d.coap.core.rest.BasicCoapResource;
import org.ws4d.coap.core.rest.CoapData;
import org.ws4d.coap.core.tools.Encoder;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

public class LCD_display extends BasicCoapResource{
	private String msg = "null";
	I2CDevice _device = null;
	I2CLCD _lcd = null;
    
	private LCD_display(String path, String value, CoapMediaType mediaType) {
		super(path, value, mediaType);
	    try {
	    	I2CBus bus = I2CFactory.getInstance(I2CBus.BUS_1);
	        _device = bus.getDevice(0x27);
	        _lcd = new I2CLCD(_device);
	        _lcd.init();
	        _lcd.backlight(true);
	        _lcd.clear();
	        _lcd.display_string("Press The Button", 1);
	        _lcd.display_string(value, 1);
	    } catch (Exception ex) {
			System.out.println(ex.toString());
	    }
	}

	public LCD_display() {
		this("/lcd", "null", CoapMediaType.text_plain);
	}

	@Override
	public synchronized CoapData get(List<String> query, List<CoapMediaType> mediaTypesAccepted) {
		return get(mediaTypesAccepted);
	}
	
	@Override
	public synchronized CoapData get(List<CoapMediaType> mediaTypesAccepted) {
		return new CoapData(Encoder.StringToByte(this.msg), CoapMediaType.text_plain);
	}

	@Override
	public synchronized boolean setValue(byte[] value) {
		this.msg = Encoder.ByteToString(value);
		_lcd.clear();
        _lcd.display_string("Press The Button", 1);
        _lcd.clear();
		_lcd.display_string(msg, 1);
		return true;
	}
	
	@Override
	public synchronized boolean put(byte[] data, CoapMediaType type) {
		return this.setValue(data);
	}
	
	
	@Override
	public synchronized boolean post(byte[] data, CoapMediaType type) {
		return this.setValue(data);
	}



	@Override
	public synchronized String getResourceType() {
		return "Raspberry pi 4 LCD Display";
	}

}