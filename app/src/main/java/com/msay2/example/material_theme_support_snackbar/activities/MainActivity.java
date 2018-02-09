package com.msay2.example.material_theme_support_snackbar.activities;

/**
 * Author: Meclot Yoann and The Android Open Source Project
 * Created and Modified on: 02/08/2018
 * Github: https://github.com/MSay2
 */

/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Copyright MSay2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.msay2.example.material_theme_support_snackbar.R;
import com.msay2.example.material_theme_support_snackbar.widget.Snackbar;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

/**
 * Support of compatibility for the {@Theme.Material} theme
 * 
 * by MSay2 (Meclot Yoann)
 */
 
/**
 * This is a Example of use
 */

public class MainActivity extends Activity 
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
		
		final LinearLayout root = (LinearLayout)findViewById(R.id.main_root);
		final LinearLayout layout = (LinearLayout)findViewById(R.id.layout_view);
		
		Button button1 = (Button)findViewById(R.id.button1);
		button1.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View viewBtn)
			{
				Snackbar.make(root, "Hello !", Snackbar.LENGTH_LONG).setAction("Toast", new View.OnClickListener()
				{
					@Override
					public void onClick(View viewSb)
					{
						Toast.makeText(MainActivity.this, "I'm a Toast, the view is not Above on the Snackbar :@", Toast.LENGTH_LONG).show();
					}
				}).show();
			}
		});
		
		Button button2 = (Button)findViewById(R.id.button2);
		button2.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				Snackbar.make(root, "Hello :D !", Snackbar.LENGTH_LONG).setAction("Above", new View.OnClickListener()
				{
					@Override
					public void onClick(View mView)
					{
						Toast.makeText(MainActivity.this, "I'm a Toast, the view is Above on the Snackbar :D", Toast.LENGTH_LONG).show();
					}
				}).above(layout).show();
			}
		});
    }
}
