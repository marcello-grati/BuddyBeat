from pydub import AudioSegment
from pygame import mixer
import threading
import time
import tempfile
import os
from music_player import Play_Pause
from BPM_extractor import get_bpm 

song_path = 'percorso/della/traccia/audio.mp3'
original_bpm = get_bpm(song_path)
window_size_ms = 5000  # Larghezza di finestra in millisecondi

def obtain_next_buffer():
    # Carica la traccia audio
    audio = AudioSegment.from_file(song_path, format="mp3")
    return next_buffer

def update_bpm(new_input_bpm, previous_input_bpm, next_buffer):
    speed_ratio = new_input_bpm / previous_input_bpm

    # Modifica la velocità senza alterare la tonalità
    modified_buffer = audio.speedup(playback_speed=speed_ratio)

    # Salva il brano modificato in un file temporaneo
    _, temp_path = tempfile.mkstemp(suffix=".mp3")
    modified_buffer.export(temp_path, format="mp3")

    # Riproduci la traccia modificata
    mixer.music.stop()
    mixer.music.load(temp_path)
    mixer.music.play()

    # Rimuovi il file temporaneo dopo la riproduzione
    mixer.music.queue(lambda: os.remove(temp_path))


def extract_and_modify_buffers(song_path, window_size_ms, new_input_bpm_bpm):
    # Carica la canzone
    song = AudioSegment.from_file(song_path)

    # Calcola il numero di frame in base alla larghezza di finestra
    frame_size = int(window_size_ms * song.frame_rate / 1000)

    # Estrai i buffer con la larghezza di finestra specificata
    buffers = [song[i:i+frame_size] for i in range(0, len(song), frame_size)]

    # Modifica la velocità di ciascun buffer in base ai BPM desiderati
    modified_buffers = [
        buffer.speedup(playback_speed=new_input_bpm / (len(buffer) / 1000 * 60))
        for buffer in buffers
    ]

    return modified_buffers


# Salva i buffer modificati in file audio separati
# for i, buffer in enumerate(modified_buffers):
#     buffer.export(f"percorso/buffer_modificato_{i + 1}.wav", format="wav")



def update_bpm_continuously(new_input_bpm):
    previous_input_bpm = original_bpm
    while True:
        # Aggiorna il BPM solo se è diverso dal precedente input
        if new_input_bpm != previous_input_bpm:
            update_bpm(new_input_bpm, previous_input_bpm)
            previous_input_bpm = new_input_bpm
        time.sleep(1)


# Avvia il thread per l'aggiornamento continuo del BPM
update_thread = threading.Thread(target=update_bpm_continuously)
update_thread.start()
