from pydub import AudioSegment
from pydub.utils import get_array_type
import sounddevice as sd
import soundfile as sf
import threading

import numpy as np
import pyrubberband
import bpm_computing as bc

def stretch_function(input, sr, bpm_start, bpm_arrive):

    out_stretch = pyrubberband.pyrb.time_stretch(input, int(sr), rate=(bpm_arrive/bpm_start))
    #scipy.io.wavfile.write("stretcheder.wav", int(sr), out_stretch)
    return out_stretch

class DynamicPlayer:
    def __init__(self, file_path, bpm_comp, original_bpm=None):
        self.file_path = file_path
        self.original_bpm = original_bpm
        self.data, self.fs = sf.read(file_path, always_2d=True)
        self.bpm_comp = bpm_comp
        self.current_frame = 0
        self.event = threading.Event()

    def callback(self, outdata, frames, time, status):

        id_bpm = self.bpm_comp.get_ideal_bpm()
        print("ideal BPM: ", id_bpm)
        if status:
            print(status)
        chunksize = min(len(self.data) - self.current_frame, frames)
        #print("chunksize: ", chunksize)
        processed_chunk = stretch_function(
            self.data[self.current_frame:self.current_frame + round(chunksize*id_bpm/self.original_bpm)+1], 
            self.fs, 
            self.original_bpm, 
            id_bpm)
        #print("processed chunk: ",processed_chunk.shape)
        if len(processed_chunk) < frames:
            outdata[chunksize:] = 0
            raise sd.CallbackStop()
        outdata[:chunksize] = processed_chunk[:chunksize]        
        self.current_frame += round(chunksize*id_bpm/self.original_bpm)
        #print("current_frame: ", current_frame)

    def play(self):
        stream = sd.OutputStream(
        samplerate=self.fs,
        callback=self.callback, channels=2, finished_callback=self.event.set, blocksize=4096 * 8, latency="high")
        with stream:
            self.event.wait()  # Wait until playback is finished

    def change(self, file_path, original_bpm=None):
        self.file_path = file_path
        self.original_bpm = original_bpm
        self.data, self.fs = sf.read(file_path, always_2d=True)
        self.current_frame = 0
        self.event.clear()
        print("cambiato canzone \|T|/")
        

""" ratio = 107/100

data, fs = sf.read("src/whenever.mp3", always_2d=True)

bpm_comp = bc.BPM_computer()

event = threading.Event()

current_frame = 0 """

""" def callback(outdata, frames, time, status):
    global current_frame
    id_bpm = bpm_comp.get_ideal_bpm()
    print("ideal BPM: ", id_bpm)
    if status:
        print(status)
    chunksize = min(len(data) - current_frame, frames)
    #print("chunksize: ", chunksize)
    processed_chunk = stretch_function(data[current_frame:current_frame + round(chunksize*id_bpm/107)+1], fs, 107, id_bpm)
    #print("processed chunck: ",processed_chunk.shape)
    if len(processed_chunk) < frames:
        outdata[chunksize:] = 0
        raise sd.CallbackStop()
    outdata[:chunksize] = processed_chunk[:chunksize]        
    current_frame += round(chunksize*id_bpm/107) -1
    #print("current_frame: ", current_frame) """


""" stream = sd.OutputStream(
    samplerate=fs,
    callback=callback, channels=2, finished_callback=event.set, blocksize=4096 * 8, latency="high")
with stream:
    event.wait()  # Wait until playback is finished """
