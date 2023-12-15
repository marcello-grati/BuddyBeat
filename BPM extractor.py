import librosa
import os

def get_bpm(audio_file):
    # Carica il file audio
    y, sr = librosa.load(audio_file)

    # Estrai il tempo di battito
    tempo, _ = librosa.beat.beat_track(y=y, sr=sr)

    return tempo

# Sostituisci con il percorso del tuo file audio
audio_file_path = 'inserire/percorso/file/audio'
if not os.path.exists(audio_file_path):
    print(f"Errore: Il file '{audio_file_path}' non esiste.")

bpm = get_bpm(audio_file_path)

print(f'Tempo di battito: {bpm} bpm')
