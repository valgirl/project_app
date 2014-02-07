package com.example.speedtracker;

import com.google.android.gms.maps.*;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.*;
import com.google.android.gms.maps.SupportMapFragment;

public class Run extends FragmentActivity{

	private GoogleMap map_view;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.run);

		map_view =  ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
		
		map_view.setMyLocationEnabled(true);
	}

	
}
