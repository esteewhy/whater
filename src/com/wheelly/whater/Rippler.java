package com.wheelly.whater;

public interface Rippler {

	public abstract void transformRipples(final int _height, final int _width,
			short[] rippleMap, short[] lastMap, final int[] textureBitmap,
			int[] rippleBitmap, final boolean flip);
	
	public abstract void disturb(int dx, int dy,
			final int width, final int height, final short riprad,
			short[] ripplemap, final boolean flip);
}