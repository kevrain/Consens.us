package com.kevrain.consensus.support;

import com.kevrain.consensus.R;

import java.util.Random;

/**
 * Created by kfarst on 9/7/16.
 */
public class BackgroundGradientUntil {
    static int[] gradients = new int[] {
            R.drawable.background_green_gradient ,
            R.drawable.background_blue_gradient ,
            R.drawable.background_orange_gradient ,
            R.drawable.background_purple_gradient ,
            R.drawable.background_red_gradient ,
    };

    public static int randomGradient() {
        Random rand = new Random();
        return gradients[rand.nextInt(gradients.length - 1)];
    }
}
