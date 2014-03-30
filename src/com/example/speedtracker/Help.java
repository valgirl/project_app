package com.example.speedtracker;


import android.app.Activity;
import android.content.res.Resources;
import android.opengl.Visibility;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class Help extends Activity implements OnClickListener{

	private TextView disp;
	private TextView title;
	private int index;
	private Button next;
	private Button back;
	private Spanned docs[];
	private Spanned titles[];
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.help);
		Resources res = getResources();
		
		next = (Button)findViewById(R.id.next);
		back = (Button)findViewById(R.id.back);
		disp = (TextView)findViewById(R.id.txt1);
		title = (TextView)findViewById(R.id.title_box);
		index = 0;
		
		next.setOnClickListener(this);
		back.setOnClickListener(this);
		docs = new Spanned[6];
		titles = new Spanned[6];
		
		docs[0] = Html.fromHtml(res.getString(R.string.help_main));
		docs[1] = Html.fromHtml(res.getString(R.string.jump_help));
		docs[2] = Html.fromHtml(res.getString(R.string.run_help));
		docs[3] = Html.fromHtml(res.getString(R.string.speed_help));
		docs[4] = Html.fromHtml(res.getString(R.string.beep_help));
		
		titles[1] = Html.fromHtml(res.getString(R.string.power_test));
		titles[2] = Html.fromHtml(res.getString(R.string.run_test));
		titles[3] = Html.fromHtml(res.getString(R.string.sprint_test));
		titles[4] = Html.fromHtml(res.getString(R.string.beep_test));
		titles[0] = Html.fromHtml("");
		
		disp.setText( Html.fromHtml(res.getString(R.string.help_main)));
		title.setText(titles[index]);
		title.setBackgroundResource(R.drawable.runnning_man_android);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		
			case R.id.next: index++;
							if(index == 4){
								next.setClickable(false);
								next.setVisibility(View.INVISIBLE);
							}
							else{
								next.setClickable(true);
								next.setVisibility(View.VISIBLE);
								back.setClickable(true);
								back.setVisibility(View.VISIBLE);
							}
							disp.setText(docs[index]);
							title.setText(titles[index]);									
							break;
							
			case R.id.back: if(index != 0) index--;
							if(index == 0){
								back.setClickable(false);
								back.setVisibility(View.INVISIBLE);
							}
							else{				
								back.setClickable(true);
								back.setVisibility(View.VISIBLE);
								next.setClickable(true);
								next.setVisibility(View.VISIBLE);
							}
								disp.setText(docs[index]);
								title.setText(titles[index]);	
							break;
				
		}
	}
}
