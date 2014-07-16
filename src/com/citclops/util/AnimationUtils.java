package com.citclops.util;

import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

public class AnimationUtils {
	public static Animation inFromRightAnimation(int millisecondsDuration) {
		Animation inFromRight = new TranslateAnimation(
			Animation.RELATIVE_TO_PARENT,  +1.0f, Animation.RELATIVE_TO_PARENT,  0.0f,
			Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,   0.0f);
		inFromRight.setDuration(millisecondsDuration);
		inFromRight.setInterpolator(new AccelerateInterpolator());
		return inFromRight;
	}
	public static Animation outToLeftAnimation(int millisecondsDuration) {
		Animation outtoLeft = new TranslateAnimation(
			Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,  -1.0f,
			Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,   0.0f);
		outtoLeft.setDuration(millisecondsDuration);
		outtoLeft.setInterpolator(new AccelerateInterpolator());
		return outtoLeft;
	}	
	public static Animation inFromLeftAnimation(int millisecondsDuration) {
		Animation inFromLeft = new TranslateAnimation(
			Animation.RELATIVE_TO_PARENT,  -1.0f, Animation.RELATIVE_TO_PARENT,  0.0f,
			Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,   0.0f);
		inFromLeft.setDuration(millisecondsDuration);
		inFromLeft.setInterpolator(new AccelerateInterpolator());
		return inFromLeft;
	}
	public static Animation outToRightAnimation(int millisecondsDuration) {
		Animation outtoRight = new TranslateAnimation(
			Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,  +1.0f,
			Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,   0.0f);
		outtoRight.setDuration(millisecondsDuration);
		outtoRight.setInterpolator(new AccelerateInterpolator());
		return outtoRight;
	}
}
