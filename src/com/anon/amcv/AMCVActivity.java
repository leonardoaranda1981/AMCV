package com.anon.amcv;

import android.app.Activity;
import android.app.AlertDialog;
//import android.app.Notification;
//import android.app.NotificationManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.SlidingDrawer;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import org.osmdroid.ResourceProxy;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.SimpleLocationOverlay;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlay;


import org.osmdroid.views.overlay.OverlayItem;
import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.io.InputStream;


import com.anon.amcv.ConnectivityReceiver;
import com.anon.amcv.GpxParserSax;


public class AMCVActivity extends Activity {
    /** Called when the activity is first created. */
	private LocationManager locManager;
	private LocationListener locListener;
	private LocationListener locListener2;
	List<GeoPoint> camaras;
	private boolean conectividad = false; 
	ConnectivityManager connectivity;
	ConnectivityReceiver connection;
	NetworkInfo wifiInfo, mobileInfo;
	GeoPoint locActual; 
	Location locActualLoc;
	private TextView lblTituloData; 
	private TextView lblLat; 
	private TextView lblLon; 
	private TextView textStatus;
	private TextView connectionType;
	private TextView lblCamaras;
	private TextView lblDistancia;
	private final int zoomInic = 17;
	private MapView mOsmv;
    private MapController mOsmvController;
   
    private ItemizedOverlay<OverlayItem> camerasOverlay;
  
    private SimpleLocationOverlay miLocOverlay;
    private ResourceProxy mResourceProxy;
    
    private static final int MENU_ADD_ID = Menu.FIRST;
    private static final int MENU_REFRESH_ID = MENU_ADD_ID + 1;
    private Drawable addIcon; 
    private Drawable refreshIcon; 
    private Drawable cameraIcon; 
    
    SlidingDrawer dataPanel;
  
    private Context context;
    
    private boolean GPSactivo = false;
    private boolean alarmaEncendida = false;
    AlertDialog.Builder dialogo;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        connection = new ConnectivityReceiver(getApplicationContext());
        context = getApplicationContext();

        //conectividad = connection.hasConnection();
        mResourceProxy = new DefaultResourceProxyImpl(context);

        final RelativeLayout rl = new RelativeLayout(this);
        
        this.setContentView(R.layout.mapview);
       
        Resources res = getResources();
        addIcon = res.getDrawable(R.drawable.add);
        refreshIcon = res.getDrawable(R.drawable.refresh);
        cameraIcon = res.getDrawable(R.drawable.camara);
       
        miLocOverlay = new SimpleLocationOverlay(this, mResourceProxy); 
        
        dialogo =  new AlertDialog.Builder(this);
        dialogo.setCancelable(true);
        dialogo.setTitle(R.string.btnAdd);
        dialogo.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                postData();
            }
        });
        dialogo.setNegativeButton(R.string.btnAddCancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            	dialog.cancel();            }
        });
        lblTituloData = (TextView)findViewById(R.id.dataTitulo);
        lblTituloData.setTextColor(Color.rgb(255,255,255));
        lblTituloData.setText(R.string.lblTituloData);
        
        textStatus = (TextView)findViewById(R.id.connectStat);
        textStatus.setTextColor(Color.rgb(255,255,255));
        
        connectionType = (TextView)findViewById(R.id.connectTipo);
        connectionType.setTextColor(Color.rgb(255,255,255));
      
        lblLat = (TextView)findViewById(R.id.lat);
        lblLat.setTextColor(Color.rgb(255,255,255));
      
        lblLon = (TextView)findViewById(R.id.lon);
         lblLon.setTextColor(Color.rgb(255,255,255));

         lblCamaras = (TextView)findViewById(R.id.cam);
        lblCamaras.setTextColor(Color.rgb(255,255,255));
    
        lblDistancia = (TextView)findViewById(R.id.dist);
        lblDistancia.setTextColor(Color.rgb(255,255,255));
        String t = res.getString(R.string.camaraProxima);
        String n = res.getString(R.string.noData);
        lblDistancia.setText(t+n);
 
        this.mOsmv = (MapView)findViewById(R.id.map);
        this.mOsmv.setBuiltInZoomControls(true);
        this.mOsmv.setMultiTouchControls(true);
        this.mOsmvController = this.mOsmv.getController();
        this.mOsmvController.setZoom(zoomInic);
        
      
        
      
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
        conectividad = connection.hasConnection();
        if(conectividad == true){
        	 inicarParsing();
        	 lblCamaras.setText(R.string.camarasEncontrada);
        	 lblCamaras.append(Integer.toString(camaras.size()));
       }else{
    	   
    	   //CharSequence text = R.string.msgNoConection;
    	   int duration = Toast.LENGTH_LONG;
    	   Toast toast = Toast.makeText(context, R.string.msgNoConection, duration);
    	   toast.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL, 0, 0);
    	   toast.show();
    	   
       }
       
    }
    @Override
    public void onResume(){
    	 super.onResume();
    	 connection.bind(getApplicationContext());
    	 
    	 comenzarLocalizacion();
    	 if(locActual != null){
    		 this.mOsmvController.setCenter(locActual); 		 
    	 }
    	
    	 textStatus.setText(R.string.conDisp);
    	 textStatus.append(Boolean.toString(conectividad));
    	 connectionType.setText(R.string.conType);
    	 connectionType.append(connection.type());
    	
    	 
    	
    }
    public void onPause(){
		  super.onPause();
		  connection.unbind(getApplicationContext());
		
	 }
    @Override
    public boolean onCreateOptionsMenu(final Menu pMenu) {
   
            MenuItem addButton = pMenu.add(0, MENU_ADD_ID, Menu.NONE, R.string.btnAdd);
            MenuItem refreshButton = pMenu.add(0, MENU_REFRESH_ID, Menu.NONE, R.string.btnRefresh);
            
            addButton.setIcon(addIcon);
            refreshButton.setIcon(refreshIcon);
           

            return true;
    }
    public boolean onMenuItemSelected(final int featureId, final MenuItem item) {
        switch (item.getItemId()) {
        case MENU_ADD_ID:
        		if(GPSactivo == true && locActualLoc != null){
        		
        			dialogoPostData(locActualLoc);
        		}else{
        	    	   int duration = Toast.LENGTH_LONG;
        	    	   Toast toast = Toast.makeText(getApplicationContext(), R.string.msgNoGpS, duration);
        	    	   toast.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL, 0, 0);
        	    	   toast.show();
        		}
        		
                return true;

        case MENU_REFRESH_ID:
        		refresh();
                return true;
        }
        return false;
    }
    private void comenzarLocalizacion()
    {
    	
    	locManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
    	
    	Location loc = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    
    	
    	if(loc == null){
    		loc = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
    	}
    	
    	  if(loc != null){
    		  actualizarPosicion(loc);
    	  }
    	mostrarPosicion(loc);
    	
    	//Nos registramos para recibir actualizaciones de la posición
    	  locListener2 = new LocationListener(){
    	    public void onLocationChanged(Location location){
    	    	
    	    	actualizarPosicion(location);
    	    	removeNetworkUpdate(location);
    	    }
    	    public void onProviderDisabled(String provider){}
    	    public void onProviderEnabled(String provider){}
    	    public void onStatusChanged(String provider, int status, Bundle extras){}
    	}; 
    	
    	
    	locListener = new LocationListener() {
	    	public void onLocationChanged(Location location) {
	    		GPSactivo = true;
	    		actualizarPosicion(location);
	    		mostrarPosicion(location);
	    		checaDistanciaCamaras(location);
	    	
	    	}
	    	public void onProviderDisabled(String provider){
	    		GPSactivo = false;
	    	}
	    	public void onProviderEnabled(String provider){
	    		
	    	}
	    	public void onStatusChanged(String provider, int status, Bundle extras){
	    		Log.i("", "Provider Status: " + status);
	    
	    	}
    	};
    	
    	locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locListener2);
    	
    	locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locListener);
    }
    private void removeNetworkUpdate(Location loc){
    	if (loc != null){
    		locManager.removeUpdates(locListener2);
    	}
    	
    }
    private void actualizarPosicion(Location loc) {
	    if(loc != null){
	    
	    	GeoPoint temporal = new GeoPoint(loc.getLatitude(), loc.getLongitude());
	    	
	    	locActual = temporal; 
	    	locActualLoc = loc;
	    	
	    	
	    	miLocOverlay.setLocation(locActual);
	    	mOsmv.getOverlays().add(miLocOverlay);
	    	this.mOsmvController.setCenter(locActual); 
	    	
	   	}
    }
    private void inicarParsing(){
    	 GpxParserSax parser = new GpxParserSax("http://contra-vigilancia.net/gpx/camera.php");
         camaras = parser.parse();
         if(camaras != null){
        	 creaOverlayCamaras();
        	 
         }
         
         
    }
    private void creaOverlayCamaras(){
    	
    	if(this.mOsmv.getOverlays().contains(camerasOverlay) == true){
    		
    		this.mOsmv.getOverlays().remove(camerasOverlay);
    	}
    	
    	
    	final ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
    	
    	Iterator<GeoPoint> litr = camaras.iterator();
    	while (litr.hasNext()) {    	
    		GeoPoint element = litr.next();
    		items.add(new OverlayItem("camara", "SampleCamera", element));
    	
    	}
    	
    	this.camerasOverlay = new ItemizedIconOverlay<OverlayItem>(items, cameraIcon, 
    				new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
    					
    					public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
    						//Toast.makeText(SampleWithMinimapItemizedoverlay.this, "Item '" + item.mTitle + "' (index=" + index
                          //                          + ") got single tapped up", Toast.LENGTH_LONG).show();
    						
    						return true; // We 'handled' this event.
    						
    					}
    					
                         public boolean onItemLongPress(final int index, final OverlayItem item) {
                                 //Toast.makeText(SampleWithMinimapItemizedoverlay.this,
                                 //              "Item '" + item.mTitle + "' (index=" + index
                                 //                            + ") got long pressed", Toast.LENGTH_LONG).show();
                            return false;
                         }

    				}
    	, mResourceProxy);
    	
    	this.mOsmv.getOverlays().add(this.camerasOverlay);

    	

    	
    }
    private void refresh(){
    	if(connection.hasConnection()== true){
    		conectividad = connection.hasConnection();
    		inicarParsing();
    		 lblCamaras.setText(R.string.camarasEncontrada);
        	 lblCamaras.append(Integer.toString(camaras.size()));
  
    		 textStatus.setText(R.string.conDisp);
        	 textStatus.append(Boolean.toString(conectividad));
        	 connectionType.setText(R.string.conType);
        	 connectionType.append(connection.type());
        }else{
     	 
     	   int duration = Toast.LENGTH_LONG;
     	   Toast toast = Toast.makeText(context, R.string.msgNoConection, duration);
     	   toast.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL, 0, 0);
     	   toast.show();
     	  
     	   
        }
    	 
    }
    private void mostrarPosicion(Location loc) {
    	if(loc != null)
    	{
    		//lblCoords.setText("Latitud: " + String.valueOf(loc.getLatitude())+"\n"+"Longitud: " + String.valueOf(loc.getLongitude()));
    		
    		lblLat.setText(R.string.lblLatitud);
    		lblLat.append(":"+String.valueOf(loc.getLatitude()));
    		
    		lblLon.setText(R.string.lblLongitud);
    		lblLon.append(":"+String.valueOf(loc.getLongitude()));
    		
    		Log.i("", String.valueOf(loc.getLatitude() + " - " + String.valueOf(loc.getLongitude())));
    	}
    	else
    	{
    		Resources res = getResources();
    		String la = res.getString(R.string.lblLatitud);
    		String lo = res.getString(R.string.lblLongitud);
    		String nodata = res.getString(R.string.noData);
    		lblLat.setText(la+":"+ nodata);
    		lblLon.setText(lo+":"+ nodata);		
    	}
    }
    private void checaDistanciaCamaras(Location loc){
        if(loc != null){
        	if(camaras != null){
        		
        		
		    	double distInic = 5000;
		    	Iterator<GeoPoint> litr = camaras.iterator();
		    	while (litr.hasNext()) {  
		    		GeoPoint temploc = new GeoPoint(loc.getLatitude(), loc.getLongitude());
		    		GeoPoint element = litr.next();
		    		int dist = element.distanceTo(temploc);
		    	   
		    	    if(distInic > dist ){
		    	    	
		    	    	distInic = dist;
		    	    	
		    	    }  
		    	}
		    	lblDistancia.setText(R.string.camaraProxima);
		    	lblDistancia.append(distInic+"mts.");
		    	if(distInic < 25){
		    		 if (alarmaEncendida == false){
		    			 alarmaEncendida = true;
		    			 alarma(distInic);	
		    		 }	
		    	}else{
		    		
		    		alarmaEncendida = false;
		    	}
	    	}
        }
    }
    private void alarma(double dist){
    	if (GPSactivo == true){
    		int distancia = (int)dist;
        	
        	
        	Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        	long[] pattern = { 500, 300, 500, 300 };
        	v.vibrate(pattern, 1);
        	
        	Resources res = getResources();
    		String cam = res.getString(R.string.camaraProxima);

        	
        	CharSequence text = cam+distancia+".mts";
     	   int duration = Toast.LENGTH_LONG;
     	   Toast toast = Toast.makeText(getApplicationContext(), text, duration);
     	   toast.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL, 0, 0);
     	   toast.show();
    		
    	}
    	
    	
    }
   
   
    private void dialogoPostData(Location loc){
    	
    	AlertDialog dialogoPostCamara  = dialogo.create();
    	
    	Resources res = getResources();
		String dialogMsg = res.getString(R.string.DialogoAdd);

    	dialogoPostCamara.setMessage(dialogMsg+"\nLat:"+loc.getLatitude() +"\nLon:"+loc.getLongitude());
    	dialogoPostCamara.show();
    	
    }
    public void postData(){
    	if(locActualLoc != null){
	    	Location loc = locActualLoc;
	    	ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	
	        nameValuePairs.add(new BasicNameValuePair("gps_latitud",String.valueOf(loc.getLatitude())));
	        nameValuePairs.add(new BasicNameValuePair("gps_longitud",String.valueOf(loc.getLongitude())));
	    	
	    	
	        //http post
	        try{
	            HttpClient httpclient = new DefaultHttpClient();
	            HttpPost httppost = new HttpPost("http://imagen-movimiento.org/update.php");
	            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
	            HttpResponse response = httpclient.execute(httppost);
	            HttpEntity entity = response.getEntity();
	            InputStream is = entity.getContent();
	            Log.i("Connection made", response.getStatusLine().toString());
	       
	            camaras.add(locActual);
	            creaOverlayCamaras();
	            
	           
	      	   int duration = Toast.LENGTH_LONG;
	      	   Toast toast = Toast.makeText(getApplicationContext(), R.string.msgExitoAdd, duration);
	      	   toast.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL, 0, 0);
	      	   toast.show();
	            
	        }
	
	        catch(Exception e)
	        {
	            Log.e("log_tag", "Error in http connection "+e.toString());
	            
		      	int duration = Toast.LENGTH_LONG;
		        Toast toast = Toast.makeText(getApplicationContext(), R.string.msgFailAdd, duration);
		        toast.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL, 0, 0);
		      	toast.show();
	        }  
        }         
    }
   
    
}