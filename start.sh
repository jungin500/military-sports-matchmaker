#!/bin/bash

# Running server
cd server
pm2 start index.js --name military-sports-matchmaker

# Building Semantic UI
cd public/semantic
gulp build
