from tkinter import *
import tkinter.font as font
from tkinter import filedialog
import os
import ctypes
import bpm_computing
import librosa
from threading import Thread
import time as t

import dynamic_player

class MediaPlayer():
    
    def __init__(self):
        # to get ideal bpm
        self.bpm_comp= bpm_computing.BPM_computer()
        self.dplayer = dynamic_player.DynamicPlayer(self, self.bpm_comp)
        self.is_playing=False
        self.paused=False
        self.queue = []
        self.already_played = []
        self.extraction_completed=False
        self.current_song = None
        
    def Play_Pause(self):
        if(self.is_playing and not self.paused):
            self.Pause()
            
        elif(not self.is_playing and self.paused):
            self.Resume()
        
        elif(not self.is_playing and not self.paused):
            self.Play()
     
    def play_song(self, title):
        if title in self.already_played:
            self.already_played.remove(title)
        else:
            self.queue.remove(title)
        self.queue.insert(0, title) #current_song as first element of the queue
        self.current_song = title
        print(self.current_song)
        self.update_queue() #update queue
        path=root.songlist.get(title)[0]
        self.dplayer.add_song(path, root.songlist[title][1])
        self.dplayer.play() # play in dynamic player
        self.is_playing = True
        self.paused = False
        
    def update_queue(self):
        root.songs_list.delete(0, END)
        root.songs_list.insert(0, self.current_song)
        for item in root.mp.queue[1:]:
                root.songs_list.insert('end', item)
        for item in root.mp.already_played:
                root.songs_list.insert('end', item)
        root.songs_list.selection_set(0)
        if(root.mp.extraction_completed==True):
            current_bpm = root.mp.bpm_comp.get_ideal_bpm()
            tmp_dict={}
            for title in root.mp.queue[1:]:
                bpm_song = root.songlist[title][1]
                dist = abs(bpm_song-current_bpm)
                tmp_dict.update({title: dist})
            tmp_dict = dict(sorted(tmp_dict.items(), key=lambda x:x[1]))
            #print(tmp_dict)    
            root.mp.queue[1:] = [key for key, _ in tmp_dict.items()]
            print(root.mp.queue)
            root.songs_list.delete(0, END)
            for item in root.mp.queue:
                root.songs_list.insert('end', item)
            for item in root.mp.already_played:
                root.songs_list.insert('end', item)
            root.songs_list.selection_set(0)
        
    def next_on_queue(self):
        if len(root.songlist) != 0:
            #if queue is empty start again with the already played
            if len(self.queue)-1 == 0:
                self.queue[1:] = self.already_played
                self.already_played = []
            next_one = self.queue[1]
            root.play_button.config(image=root.photo_pause)
            #to get the next song 
            #print(self.queue)
            #self.already_played.append(self.queue.pop(0))
            if self.current_song is not None:
                self.already_played.append(self.queue.pop(0))
            self.play_song(next_one) 
            
    def get_bpm_from_dict(self, title):
        return root.songlist[title][1]
    
    def Play(self):
        if len(root.songlist) != 0:
            root.play_button.config(image=root.photo_pause)
            #root.songs_list.selection_set(ACTIVE)
            song=root.songs_list.get(ACTIVE)
            if self.current_song is not None:
                self.already_played.append(self.queue.pop(0))
            if self.current_song is None:
                self.current_song = song
            self.play_song(song)
        
    def Pause(self):
        if len(root.songlist) != 0:
            root.play_button.config(image=root.photo_play)
            self.dplayer.pause() #pause in dinamic player
            self.paused = True
            self.is_playing = False

    #to stop the  song 
    def Stop(self):
        if len(root.songlist) != 0:
            root.songs_list.selection_clear(ACTIVE)
            root.play_button.config(image=root.photo_play)
            self.dplayer.stop() #stop in dinamic player
            self.paused = False
            self.is_playing = False

    #to resume the song
    def Resume(self):
        if len(root.songlist) != 0:
            root.play_button.config(image=root.photo_pause)
            self.dplayer.play() #play in dynamic player
            self.paused = False
            self.is_playing = True

    #previous song
    def Previous(self):
        if len(root.songlist) != 0:
            #to get the selected song index
            previous_one=root.songs_list.curselection()
            #to get the previous song index
            previous_one = (previous_one[0]-1+len(root.songlist))%len(root.songlist)
            root.songs_list.selection_clear(0,END)
            #activate new song
            root.songs_list.activate(previous_one)
            #set the next song
            root.songs_list.selection_set(previous_one)
            root.songs_list.see(previous_one)
            root.play_button.config(image=root.photo_pause)
            #to get the previous song
            song=root.songs_list.get(previous_one)
            self.play_song(song)

    def Next(self):
        if len(root.songlist) != 0:
            #to get the selected song index
            next_one=root.songs_list.curselection()
            #to get the next song index
            next_one=(next_one[0]+1)%len(root.songlist)
            root.songs_list.selection_clear(0,END)
            #activate newsong
            root.songs_list.activate(next_one)
            #set the next song
            root.songs_list.selection_set(next_one)
            root.songs_list.see(next_one)
            root.play_button.config(image=root.photo_pause)
            #to get the next song 
            song=root.songs_list.get(next_one)
            self.play_song(song)

#extractor of bpm
class BPM_extractor(Thread):
    # constructor
    def __init__(self, dict_path):
        # execute the base constructor
        Thread.__init__(self)
        self.toAdd = dict_path
 
    # function executed in a new thread
    def run(self):
        for title, path in self.toAdd.items():
            y, sr = librosa.load(path)
            # Estrai il tempo di battito
            bpm, _ = librosa.beat.beat_track(y=y, sr=sr)
            root.songlist[title][1] = bpm
        root.mp.extraction_completed = True
  
#queue manager, every 10 seconds update the queue of songs according to ideal bpm          
class Queue(Thread):
    # constructor
    def __init__(self):
        # execute the base constructor
        Thread.__init__(self)
 
    # function executed in a new thread
    def run(self):
        while(True):
            t.sleep(1)
            if(root.mp.extraction_completed==True):
                root.mp.update_queue()
                t.sleep(50)

#GUI
class Gui(Tk):
    
    def __init__(self):
        if os.name == 'nt':
            ctypes.windll.shcore.SetProcessDpiAwareness(1) # per migliorare risoluzione schermo
        
        Tk.__init__(self)
        self.title('BuddyBeat')
        self.configure(background='#fcfaf2', width=450, height=300)
        self.mp = MediaPlayer()
        self.initialize_gui()
        self.songlist={} #dictionary with all songs
        
    def initialize_gui(self):

        frA = Frame()
        frA.pack()

        frB = Frame()
        frB.pack()

        #create the listbox to contain songs
        self.songs_list=Listbox(frA,selectmode=BROWSE,bg="#fcfaf2",fg="black",font=('arial',15),height=12,width=45,
                        selectbackground="#c9ffe8", selectforeground="black", activestyle='none', border=3)
        self.songs_list.grid(columnspan=300)

        #font is defined which is to be used for the button font 
        defined_font = font.Font(family='Helvetica')

        self.photo_play = PhotoImage(file = r"src/play.png").subsample(10,10)
        self.photo_pause = PhotoImage(file = r"src/pause.png").subsample(10, 10) 
        self.photo_stop = PhotoImage(file = r"src/stop.png").subsample(10, 10) 
        self.photo_next = PhotoImage(file = r"src/next.png").subsample(10, 10) 
        self.photo_previous = PhotoImage(file = r"src/back.png").subsample(10, 10) 

        #play button
        self.play_button=Button(frB,width=50, height=60, image=self.photo_play, bg="#fcfaf2", borderwidth=0, command=self.mp.Play_Pause)
        self.play_button['font']=defined_font
        self.play_button.grid(row=1,column=149)

        #stop button
        self.stop_button=Button(frB,width =50,height=60, image=self.photo_stop, bg="#fcfaf2", borderwidth=0, command=self.mp.Stop)
        self.stop_button['font']=defined_font
        self.stop_button.grid(row=1,column=150)

        #previous button
        self.previous_button=Button(frB,width =50,height=60, image=self.photo_previous, bg="#fcfaf2", borderwidth=0, command=self.mp.Previous)
        self.previous_button['font']=defined_font
        self.previous_button.grid(row=1,column=148)

        #nextbutton
        self.next_button=Button(frB,width =50,height=60,image=self.photo_next, bg="#fcfaf2",borderwidth=0, command=self.mp.next_on_queue)
        self.next_button['font']=defined_font
        self.next_button.grid(row=1,column=151)

        #menu 
        my_menu=Menu(self)
        self.config(menu=my_menu)
        add_song_menu=Menu(my_menu)
        my_menu.add_cascade(label="Menu",menu=add_song_menu)
        add_song_menu.add_command(label="Add songs", command=self.addsongs)
        add_song_menu.add_command(label="Delete song", command=self.deletesong)
        
    def addsongs(self):
        tmp = filedialog.askdirectory(initialdir="/Users")
        if tmp is not None:
            
            new_add={os.path.splitext(file)[0]: str(tmp)+'/'+str(file) for file in os.listdir(tmp) if (os.path.splitext(file)[-1].lower()== ".mp3" or os.path.splitext(file)[-1].lower()== ".wav")}
            for title, path in new_add.items():
                bpm=None
                self.songs_list.insert(END,title) #listbox
                self.songlist.update({title: [path, bpm]}) #updating dictionary with all songs and information
                self.mp.queue.append(title) #queue of songs
            BPM_extractor(new_add).start()   #starting extraction of bpm of added songs   
            
    def deletesong(self):
        curr_song=self.songs_list.curselection()
        self.songs_list.delete(curr_song[0])
        self.songlist.popitem(curr_song[0])

if __name__ == '__main__':
    
    root = Gui()
    queue = Queue()
    queue.start()
    root.mainloop()  