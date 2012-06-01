LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := rippler
LOCAL_SRC_FILES := rippler.c

include $(BUILD_SHARED_LIBRARY)	