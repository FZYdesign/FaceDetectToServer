# Copyright (C) 2009 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := facetemp
LOCAL_SRC_FILES := libFiFacial.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)

LOCAL_MODULE := jpegtemp
LOCAL_SRC_FILES := libjpeg.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := iskaer
LOCAL_SRC_FILES := libiskaer.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := iskaershare
LOCAL_SRC_FILES := libiskaershare.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := wlt2bmp
LOCAL_SRC_FILES := libwlt2bmp.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)

#LOCAL_SRC_TCP	:= ./tcpSock/maintcp.cpp ./tcpSock/CommLayerTcp.cpp ./tcpSock/NetAuth.cpp ./tcpSock/NetDeal.cpp ./tcpSock/NetPublic.cpp
#LOCAL_SRC_TCP	:= maintcp.cpp CommLayerTcp.cpp NetAuth.cpp NetDeal.cpp NetPublic.cpp

LOCAL_MODULE    := face_identify
LOCAL_SRC_FILES := facelib.cpp Public.cpp  maintcp.cpp CommLayerTcp.cpp NetAuth.cpp NetDeal.cpp NetPublic.cpp gb2312.cpp ftpclient.cpp
#LOCAL_SRC_FILES := cn_runvision_utils_FaceNative.cpp Public.cpp  maintcp.cpp CommLayerTcp.cpp NetAuth.cpp NetDeal.cpp NetPublic.cpp gb2312.cpp ftpclient.cpp
LOCAL_MODULE_FILENAME = libface_identify
LOCAL_SHARED_LIBRARIES := jpegtemp
LOCAL_STATIC_LIBRARIES := facetemp
#LOCAL_SHARED_LIBRARIES := iskaer
#LOCAL_SHARED_LIBRARIES := wlt2bmp
LOCAL_LDLIBS := -llog -lstdc++
include $(BUILD_SHARED_LIBRARY)

