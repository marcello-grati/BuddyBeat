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
    def __init__(self, media_player, bpm_comp):
        self.file_path = None
        self.original_bpm = None
        self.data = None
        self.fs = None
        self.bpm_comp = bpm_comp
        self.current_frame = 0
        self.event = threading.Event()
        self.stream = None
        self.isPlaying = False
        self.reproduction = None
        self.media_player = media_player

    def callback(self, outdata, frames, time, status):

        id_bpm = self.bpm_comp.get_ideal_bpm()
        print("ideal BPM: ", id_bpm)
        if status:
            print(status)
        chunksize = min(len(self.data) - self.current_frame, frames)
        #print("chunksize: ", chunksize)
        if (self.original_bpm!=None):
            processed_chunk = stretch_function(
                self.data[self.current_frame:self.current_frame + round(chunksize*id_bpm/self.original_bpm)+1], 
                self.fs, 
                self.original_bpm, 
                id_bpm)
        else :
            processed_chunk = self.data[self.current_frame:self.current_frame + chunksize +1]

        #print("processed chunk: ",processed_chunk.shape)
        if len(processed_chunk) < frames:
            outdata[chunksize:] = 0
            self.isPlaying=False
            raise sd.CallbackStop()
        outdata[:chunksize] = processed_chunk[:chunksize] 
        if (self.original_bpm!=None):       
            self.current_frame += round(chunksize*id_bpm/self.original_bpm)
        else :
            self.current_frame += chunksize
        #print("current_frame: ", current_frame)

    def play(self):
        if ((self.file_path!=None) & (not self.isPlaying)):
            self.isPlaying = True
            print("play")
            self.reproduction = threading.Thread(target=self.reproduce)
            self.reproduction.start()
            
        else : 
            print("add song before playing")

    def add_song(self, file_path, original_bpm=None):
        print("change")
        self.file_path = file_path
        self.original_bpm = original_bpm
        self.data, self.fs = sf.read(file_path, always_2d=True)
        self.current_frame = 0
        #self.event.clear()
        # print("cambiato canzone \|T|/")

    def stop(self):
        if (self.isPlaying):
            print("stop")
            self.stream.stop()
            self.current_frame = 0
            self.event.clear()
            self.isPlaying=False
        else : 
            print("no streaming playing")

    def pause(self):
        if (self.isPlaying):
            print("pause")
            self.stream.stop()
            self.event.clear()
            self.isPlaying=False
        else : 
            print("no streaming playing")

    def add_bpm(self, bpm):
        self.original_bpm = bpm

    def reproduce(self):
        self.stream = sd.OutputStream(
            samplerate=self.fs,
            callback=self.callback, channels=2, finished_callback=self.event.set, blocksize=4096 * 8, latency="high")
        with self.stream:
            self.event.wait()  # Wait until playback is finished
