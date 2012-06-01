package com.wheelly.whater;

public class NativeRippler implements Rippler {
	static {
		System.loadLibrary("rippler");
	}
	
	@Override
	public native void transformRipples(final int _height, final int _width,
			short[] rippleMap, short[] lastMap,
			final int[] textureBitmap, int[] rippleBitmap,
			final boolean flip);
	
	@Override
	public native void disturb(int dx, int dy,
			final int width, final int height, final short riprad,
			short[] ripplemap, final boolean flip);
}