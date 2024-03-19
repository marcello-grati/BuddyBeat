# file di prova: generazione bpm casuali ogni delta_t secondi
import random
import time
from pythonosc import udp_client
from time import sleep

high_range = 40   #80
low_range = 200   #140
random.uniform(low_range, high_range)

# intervallo di tempo tra due letture consecutive
delta_t = 2.0

# Configura il client OSC per comunicare con SuperCollider
client = udp_client.SimpleUDPClient("127.0.0.1", 57120)

while True:
    # Genera il valore casuale
    bpm = random.uniform(low_range, high_range)

    # Esegui l'azione associata al valore casuale (es. elaborazione o stampa)
    print(f"Valore casuale generato: {bpm}")
    client.send_message("/bpm", bpm)
    print(f"Valore di BPM inviato: {bpm}")

    time.sleep(delta_t)
