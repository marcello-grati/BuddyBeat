
from pydub import AudioSegment
from pygame import mixer
import threading
import time
import tempfile
from music_player import Play_Pause
from BPM_extractor import get_bpm 

# Variabile globale per memorizzare il BPM precedente
previous_input_bpm = get_bpm('percorso/della/traccia/audio.mp3')

def update_bpm(new_input_bpm, previous_input_bpm):
    # Carica la traccia audio
    audio = AudioSegment.from_file('percorso/della/traccia/audio.mp3')

    # Calcola il rapporto di velocità necessario
    speed_ratio = new_input_bpm / previous_input_bpm

    # Modifica la velocità senza alterare la tonalità
    modified_audio = audio.speedup(playback_speed=speed_ratio)

    # Salva il brano modificato in un file temporaneo
    _, temp_path = tempfile.mkstemp(suffix=".mp3")
    modified_audio.export(temp_path, format="mp3")

    # Riproduci la traccia modificata
    mixer.music.stop()
    mixer.music.load(temp_path)
    mixer.music.play()

    # Rimuovi il file temporaneo dopo la riproduzione
    mixer.music.queue(lambda: os.remove(temp_path))


def update_bpm_continuously(new_input_bpm):
    global previous_input_bpm
    while True:

        # Aggiorna il BPM solo se è diverso dal precedente input
        if new_input_bpm != previous_input_bpm:
            update_bpm(new_input_bpm, previous_input_bpm)
            previous_input_bpm = new_input_bpm

        time.sleep(1)


# Avvia il thread per l'aggiornamento continuo del BPM
update_thread = threading.Thread(target=update_bpm_continuously)
update_thread.start()
