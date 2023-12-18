import array
from pydub import AudioSegment
from pydub.utils import get_array_type
import sounddevice as sd
import soundfile as sf
import threading
import time
import rubberband

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

# def stretch_function2(input, sr, ratio):

#     return rubberband.stretch(input, rate=sr, ratio=ratio, crispness=5, precise=True)


ratio = 107/100

data, fs = sf.read("src/whenever.mp3", always_2d=True)

# data = data[:,0]
# data = data.tolist()

#new_data = stretch_function(data, fs, 107, 107*ratio)

event = threading.Event()

current_frame = 0
current_frame_dist = 0

""" def callback(outdata, frames, time, status):
    global current_frame, current_frame_dist, ratio
    if status:
        print(status)
    chunksize = min(len(data) - current_frame, int(frames / ratio))
    chunksize_dist = min(frames, (len(data) - current_frame)*ratio)
    print(chunksize_dist, int(chunksize*ratio +1))
    processed_chunk = stretch_function(data[current_frame:current_frame + chunksize + 1], fs, 107, int(107/ratio))
    #processed_chunk = 0.5 * data[current_frame:current_frame + chunksize]
    #processed_chunk = data[current_frame:current_frame + 5 * chunksize]
    #new_data = stretch_function(data, fs, 107, 107*ratio)
    #processed_chunk = new_data[current_frame_dist:current_frame_dist + 5 * chunksize_dist]
    #processed_chunk = stretch_function2(data[current_frame:current_frame + chunksize +1], fs, ratio)
    #processed_chunk = np.expand_dims(processed_chunk, axis=1)
    outdata[:chunksize_dist] = processed_chunk[:chunksize_dist]
    if chunksize_dist < frames:
        outdata[chunksize_dist:] = 0
        print("AAHAAAAAAAAAH")
        raise sd.CallbackStop()
    
    current_frame += chunksize
    current_frame_dist += chunksize_dist
    print((processed_chunk[:chunksize_dist]).shape) """

def callback(outdata, frames, time, status):
    global current_frame
    if status:
        print(status)
    chunksize = min(len(data) - current_frame, frames)
    print("chunksize: ", chunksize)
    processed_chunk = stretch_function(data[current_frame:current_frame + round(chunksize*120/107)+1], fs, 107, 120)
    #processed_chunk = 0.5 * data[current_frame:current_frame + chunksize]
    print("processed chunck: ",processed_chunk.shape)
    if len(processed_chunk) < frames:
        outdata[chunksize:] = 0
        raise sd.CallbackStop()
    outdata[:chunksize] = processed_chunk[:chunksize]        
    current_frame += round(chunksize*120/107) -1
    print("current_frame: ", current_frame)


stream = sd.OutputStream(
    samplerate=fs,
    callback=callback, channels=2, finished_callback=event.set, blocksize=4096 * 4, latency="high")
with stream:
    event.wait()  # Wait until playback is finished
