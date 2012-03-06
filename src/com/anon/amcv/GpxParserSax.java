package com.anon.amcv;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.net.URL;
import javax.xml.parsers.SAXParser;
import java.net.MalformedURLException;
import javax.xml.parsers.SAXParserFactory;
import org.osmdroid.util.GeoPoint;

public class GpxParserSax {
	 private URL gpxUrl;
	 
	    public GpxParserSax(String url)
	    {
	        try
	        {
	            this.gpxUrl = new URL(url);
	        }
	        catch (MalformedURLException e)
	        {
	            throw new RuntimeException(e);
	        }
	    }
	 
	    public List<GeoPoint> parse()
	    {
	        SAXParserFactory factory = SAXParserFactory.newInstance();
	 
	        try
	        {
	            SAXParser parser = factory.newSAXParser();
	            GpxHandler handler = new GpxHandler();////
	            parser.parse(this.getInputStream(), handler);
	            return handler.getListaCoordenadas(); ////
	        }
	        catch (Exception e)
	        {
	            throw new RuntimeException(e);
	        }
	    }
	 
	    private InputStream getInputStream()
	    {
	        try
	        {
	            return gpxUrl.openConnection().getInputStream();
	        }
	        catch (IOException e)
	        {
	            throw new RuntimeException(e);
	        }
	    }

}
