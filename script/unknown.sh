#!/bin/bash
# Please add this script to "Startup Applications" and use bash to execute it.
# Please make sure that the path is correct.
cd ~/Workspace/dalle-playground/backend/;
# Please make sure that the virtual environment does exist.
source dalle_venv/bin/activate;
python3 app.py --port 8080&

sleep 10 # wait for dall-e service completing start up

# Please make sure that the path is correct.
cd ~/Workspace/unknown-local-server/;
./gradlew run&