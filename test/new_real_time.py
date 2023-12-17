import numpy as np
import librosa
import librosa.display
import matplotlib.pyplot as plt
from collections import Counter
import scipy.io.wavfile
import pyrubberband
import scipy

# parametri
file_path = "percorso_del_tuo_file_audio.wav"
y, sr = librosa.load(file_path, sr=None)  # sr=None restituisce il sample rate originale
sample_start = 0
sample_end = len(y) - 1
delta_sample = 44100 # 1 secondo

def stretch_function(input, sr, bpm_start, bpm_arrive):
    input_np = np.array(input)

    out_stretch = pyrubberband.pyrb.time_stretch(input_np, int(sr), rate=(bpm_arrive/bpm_start))
    #scipy.io.wavfile.write("stretcheder.wav", int(sr), out_stretch)
    return out_stretch.tolist()


def live_stretch(y, sr, bpm_start, bpm_stretched, sample_start, sample_end):
    sample_start=int(sample_start)
    sample_end=int(sample_end)
    bpm_stretched_arrive = bpm_stretched

    out1 = np.array([])
    hop_range = y[sample_start:sample_end]
    out1 = np.append(out1,stretch_function(hop_range,int(sr),bpm_start,bpm_stretched))

    current_sample = sample_end
    for i in range(0,round(bpm_stretched-bpm_start)+1):
        if(round(bpm_start)!=round(bpm_stretched_arrive)):
            #current_hop = i*delta_sample
            #next_hop = current_hop+delta_sample
            hop_range = y[current_sample:current_sample+delta_sample] #sample in un secondo (44100)
            bpm_stretched_arrive = bpm_stretched_arrive-1
            #stretch = stretch_func(hop_range,sr,bpm_stretched,round(bpm_stretched_arrive)-1)
            out1=np.append(out1, stretch_function(hop_range,sr,bpm_stretched,round(bpm_stretched_arrive)-1))
            current_sample = current_sample + delta_sample
        else:
            hop_range = y[current_sample:]
            out1=np.append(out1, hop_range)
    return out1.tolist()