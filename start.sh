#!/bin/bash

# Running server
cd server; pm2 start index.js --name military-sports-matchmaker

# Building Semantic UI
cd ../semantic/; echo "Rebuilding Semantic UI ..."
gulp build &> /dev/null

# Show logs
echo "Build complete. showing out logs ... (Ctrl+C to Escape)"
pm2 logs
