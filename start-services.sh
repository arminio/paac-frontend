#!/bin/bash

sm --start "MONGO" -f
sm --start "KEYSTORE" -f
sm --start "ASSETS_FRONTEND" -f
