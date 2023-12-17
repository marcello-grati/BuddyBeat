import array
from pydub import AudioSegment
from pydub.utils import get_array_type
import sounddevice as sd
import soundfile as sf
import threading
import time

import numpy as np
import pyrubberband

# sr = 44100

# def stretch_function(input, sr, bpm_start, bpm_arrive):
#     input_np = np.array(input)

#     out_stretch = pyrubberband.pyrb.time_stretch(input_np, int(sr), rate=(bpm_arrive/bpm_start))
#     #scipy.io.wavfile.write("stretcheder.wav", int(sr), out_stretch)
#     return out_stretch.tolist()

# def play_block(block_samples):
#     sd.play(block_samples)
#     sd.wait()

# sound = AudioSegment.from_file(file="src/whenever.mp3")
# left = sound.split_to_mono()[0]
# right = sound.split_to_mono()[1]

# bit_depth = left.sample_width * 8
# array_type = get_array_type(bit_depth)

# raw_array_left = array.array(array_type, left._data)
# raw_array_right = array.array(array_type, right._data)

# raw_array_left = np.array(raw_array_left)
# raw_array_right = np.array(raw_array_right)

# numeric_array = np.stack([raw_array_left, raw_array_right])

# numeric_array = np.transpose(numeric_array)

#numeric_array = np.stack([raw_array_left, raw_array_right])

# print(numeric_array.shape)

# short_song_1 = numeric_array[0 : sr*5]
# short_song_2 = numeric_array[sr*5 : 2 * sr*5]

# t1 = threading.Thread(target=play_block,args=(short_song_1,))
# t2 = threading.Thread(target=play_block,args=(short_song_2,))
# t1.start()
# time.sleep(len(short_song_1)/sr - 1)
# t2.start()

#stretched_song = stretch_function(short_song, 44100, 107, 150)

# sounddevice.play(short_song_1)
# sounddevice.wait()
# sounddevice.play(short_song_2)
# sounddevice.wait()

def stretch_function(input, sr, bpm_start, bpm_arrive):

    out_stretch = pyrubberband.pyrb.time_stretch(input, int(sr), rate=(bpm_arrive/bpm_start))
    #scipy.io.wavfile.write("stretcheder.wav", int(sr), out_stretch)
    return out_stretch

data, fs = sf.read("src/whenever.mp3", always_2d=True)

# new_data = stretch_function(data, fs, 107, 150)

event = threading.Event()

current_frame = 0

def callback(outdata, frames, time, status):
    global current_frame
    if status:
        print(status)
    chunksize = min(len(data) - current_frame, frames)
    #processed_chunk = stretch_function(data[current_frame:current_frame + chunksize], fs, 107, 110)
    processed_chunk = 0.5 * data[current_frame:current_frame + chunksize]
    outdata[:chunksize] = processed_chunk
    if chunksize < frames:
        outdata[chunksize:] = 0
        raise sd.CallbackStop()
    current_frame += chunksize

stream = sd.OutputStream(
    samplerate=fs,
    callback=callback, channels=data.shape[1], finished_callback=event.set)
with stream:
    event.wait()  # Wait until playback is finished
