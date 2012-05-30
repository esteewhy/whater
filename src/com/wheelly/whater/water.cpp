#define PIN(array) env->GetShortArrayElements(array, NULL)
#define UNPIN(array, data)  env->ReleaseByteArrayElements(array, data, JNI_ABORT);

JNIEXPORT void JNICALL Java_com_dniprofoil_whater_transformRipples(
    JNIEnv *env, jobject obj,
    jint _height,
    jint _width,
    jshortArray rippleMap,
    jshortArray lastMap) {
  int i = 0;
  jshort* ripple_map = PIN(rippleMap);
  jshort* last_map = PIN(lastMap);
  int half_width = _width >> 1, half_height = _height >> 1;
  
  for (int y = _height; y > 0; y--) {
    for (int x = _width; x > 0; x--) {
      int data = (ripple_map[mapind - _width] + ripple_map[mapind + _width] + ripple_map[mapind - 1] + ripple_map[mapind + 1]) >> 1;
      data -= ripple_map[newind + i];
      data -= data >> 5;
      ripple_map[newind + i] = (short)data;
      
      //where data=0 then still, where data>0 then wave
      data = 1024 - data;
      
      int old_data = last_map[i];
      last_map[i] = (short)data;
      
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
        
        _rd[cur_pixel] = _td[new_pixel];
      }
      
      ++mapind;
      ++i;
    }
  }
  
  UNPIN(rippleMap, ripple_map);
  UNPIN(lastMap, last_map);
}