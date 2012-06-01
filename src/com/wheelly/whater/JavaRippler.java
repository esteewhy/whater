package com.wheelly.whater;

public class JavaRippler implements Rippler {
	/* (non-Javadoc)
	 * @see com.wheelly.whater.Rippler#transformRipples(int, int, short[], short[], int[], int[], boolean)
	 */
	@Override
	public void transformRipples(final int _height, final int _width,
			short[] rippleMap, short[] lastMap,
			final int[] textureBitmap, int[] rippleBitmap,
			final boolean flip) {
		int i = 0;
		final int half_width = _width / 2;
		final int half_height = _height / 2;
		
		final int mapIndex = flip ? _width : _width * (_height + 3);
		final int newIndex = !flip ? _width : _width * (_height + 3);
		
		for (int y = _height; y > 0; y--) {
            for (int x = _width; x > 0; x--) {
            	int _mapIndex = mapIndex + i;
            	int _newIndex = newIndex + i;
                int data = (
                    rippleMap[_mapIndex - _width] + 
                    rippleMap[_mapIndex + _width] + 
                    rippleMap[_mapIndex - 1] + 
                    rippleMap[_mapIndex + 1]) >> 1;
                    
                data -= rippleMap[_newIndex];
                data -= data >> 5;
                
        		rippleMap[_newIndex] = (short)data;
        		
                //where data=0 then still, where data>0 then wave
                data = 1024 - data;
                
                int old_data = lastMap[i];
                lastMap[i] = (short)data;
                
                if (old_data != data) {
                    //offsets
                    int a = (((x - half_width) * data / 1024) << 0) + half_width;
                    int b = (((y - half_height) * data / 1024) << 0) + half_height;
    
                    //bounds check
                    if (a >= _width) a = _width - 1;
                    if (a < 0) a = 0;
                    if (b >= _height) b = _height - 1;
                    if (b < 0) b = 0;
    
                    int new_pixel = (a + (b * _width));
                    int cur_pixel = i;
                    
                    rippleBitmap[cur_pixel] = textureBitmap[new_pixel];
                }
                ++i;
            }
        }
	}
	
	@Override
	public void disturb(int dx, int dy,
			final int width, final int height, final short riprad,
			short[] ripplemap, final boolean flip) {
        dx <<= 0;
        dy <<= 0;
        int offset = !flip ? width : width * (height + 3);
        for (int j = dy - riprad; j < dy + riprad; j++) {
            for (int k = dx - riprad; k < dx + riprad; k++) {
                ripplemap[offset + (j * width) + k] += 128;
            }
        }
    }
}