cmake_minimum_required(VERSION 3.6.0)
add_library(groestl SHARED groestl.c hashblock.cpp)
set_target_properties(groestl PROPERTIES LINK_FLAGS -Wl,--build-id=none)
include_directories(.)
