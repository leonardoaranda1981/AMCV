package com.anon.amcv;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import java.lang.StringBuilder;
import java.util.List;
import java.util.ArrayList;
import org.osmdroid.util.GeoPoint;

public class GpxHandler extends DefaultHandler{
		private List<GeoPoint> coordenadas;
	    private GeoPoint coordenadaActual;
	    private StringBuilder sbTexto;
	 
	    public List<GeoPoint> getListaCoordenadas(){
	        return coordenadas;
	    }
	 
	    @Override
	    public void characters(char[] ch, int start, int length)
	                   throws SAXException {
	 
	        super.characters(ch, start, length);
	 
	        if (this.coordenadaActual != null)
	           sbTexto.append(ch, start, length);
	    }
	 
	    @Override
	    public void endElement(String uri, String localName, String name)
	                   throws SAXException {
	 
	        super.endElement(uri, localName, name);
	        if (this.coordenadaActual != null) {
		       	 
	            if (localName.equals("time")) {
	         //   	coordenadaActual.setTiempo(sbTexto.toString());
	            }else if(localName.equals("name")){
	      //      	coordenadaActual.setNombre(sbTexto.toString());
	            }else if (localName.equals("wpt")) {
	                coordenadas.add(coordenadaActual);
	            }
	            sbTexto.setLength(0);
	        }
	    }
	 
	    @Override
	    public void startDocument() throws SAXException {
	 
	        super.startDocument();
	 
	        coordenadas = new ArrayList<GeoPoint>();
	        sbTexto = new StringBuilder();
	    }
	 
	    @Override
	    public void startElement(String uri, String localName,
	                   String name, Attributes atts) throws SAXException {
	 
	        super.startElement(uri, localName, name, atts);
	 
	        if (localName.equals("wpt")) {
	        	String attrValueLat = atts.getValue("lat");
	        	String attrValueLon = atts.getValue("lon");
	        	double  la = Double.parseDouble(attrValueLat);
	        	double  lo = Double.parseDouble(attrValueLon);
	        	coordenadaActual = new GeoPoint(la, lo);
	        	sbTexto.setLength(0);
	        }
	        
	       
	        
	    }

}
