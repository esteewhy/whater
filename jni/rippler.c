#include "com_wheelly_whater_NativeRippler.h"

#include <errno.h>
#include <jni.h>
#include <sys/time.h>
#include <time.h>
#include <android/log.h>

#include <stdio.h>
#include <stdlib.h>
#include <math.h>

#define PIN(array) (*env)->GetShortArrayElements(env, array, NULL)
#define UNPIN(array, data)  (*env)->ReleaseShortArrayElements(env, array, data, JNI_ABORT)

#define PIN_INT(array) (*env)->GetIntArrayElements(env, array, NULL)
#define UNPIN_INT(array, data)  (*env)->ReleaseIntArrayElements(env, array, data, JNI_ABORT)

JNIEXPORT void JNICALL Java_com_wheelly_whater_NativeRippler_transformRipples(
    JNIEnv *env, jobject obj,
    jint _height,
    jint _width,
    jshortArray rippleMap,
    jshortArray lastMap,
    jintArray td,
    jintArray rd,
    jboolean flip) {
  jshort* ripple_map = PIN(rippleMap);
  jshort* last_map = PIN(lastMap);
  jint* _td = PIN_INT(td);
  jint* _rd = PIN_INT(rd);
  int half_width = _width >> 1, half_height = _height >> 1;
  
  int mapind = flip ? _width : _width * (_height + 3);
  int newind = !flip ? _width : _width * (_height + 3);
  int x, y, i =0;
  for (y = 0; y < _height; y++) {
    for (x = 0; x < _width; x++) {
      int _mapind = mapind + i;
      int _newind = newind + i;
      int data = (
          ripple_map[_mapind - _width]
          + ripple_map[_mapind + _width]
          + ripple_map[_mapind - 1]
          + ripple_map[_mapind + 1]) >> 1;
      data -= ripple_map[_newind];
      data -= data >> 5;
      ripple_map[_newind] = (short)data;
      
      //where data=0 then still, where data>0 then wave
      data = 1024 - data;
      
      short old_data = last_map[i];
      last_map[i] = data;
      
      if (old_data != data) {
        //offsets
        int a = (x - half_width) * data >> 10 + half_width;
        int b = (y - half_height) * data >> 10 + half_height;
        
        //bounds check
        if (a >= _width) a = _width - 1;
        if (a < 0) a = 0;
        if (b >= _height) b = _height - 1;
        if (b < 0) b = 0;
    
        int new_pixel = a + (b * _width);
        _rd[i] = _td[new_pixel];
      }
      
      ++i;
    }
  }
  
  UNPIN_INT(td, _td);
  UNPIN_INT(rd, _rd);
  UNPIN(rippleMap, ripple_map);
  UNPIN(lastMap, last_map);
}

JNIEXPORT void JNICALL Java_com_wheelly_whater_NativeRippler_disturb(
    JNIEnv *env, jobject obj,
    jint dx, jint dy,
    jint width, jint height, jshort riprad,
    jshortArray rippleMap, jboolean flip) {
  dx <<= 0;
  dy <<= 0;
  int offset = !flip ? width : width * (height + 3);
  jshort* ripple_map = PIN(rippleMap);

  int j, k;
  for (j = dy - riprad; j < dy + riprad; j++) {
    for (k = dx - riprad; k < dx + riprad; k++) {
    	ripple_map[offset + (j * width) + k] += 512;
    }
  }
  UNPIN(rippleMap, ripple_map);
}
