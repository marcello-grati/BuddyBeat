from tkinter import *
import tkinter.font as font
from pygame import mixer
from tkinter import filedialog
import os
import ctypes
import bpm_computing

class MediaPlayer():
    
    def __init__(self):
        #initialize mixer 
        self.bpm_comp= bpm_computing.BPM_computer()
        mixer.init()
        self.is_playing=False
        self.paused=False

    def Play_Pause(self):
        if(self.is_playing and not self.paused):
            self.Pause()
            
        elif(not self.is_playing and self.paused):
            self.Resume()
        
        elif(not self.is_playing and not self.paused):
            self.Play()
     
    def play_song(self, song):
        title=root.songlist.get(song)[0]
        mixer.music.load(title)
        mixer.music.play()
        self.is_playing = True
        self.paused = False
        
    def Play(self):
        root.play_button.config(image=root.photo_pause)
        root.songs_list.selection_set(ACTIVE)
        song=root.songs_list.get(ACTIVE)
        self.play_song(song)
        
    def Pause(self):
        root.play_button.config(image=root.photo_play)
        mixer.music.pause()
        self.paused = True
        self.is_playing = False

    #to stop the  song 
    def Stop(self):
        root.songs_list.selection_clear(ACTIVE)
        root.play_button.config(image=root.photo_play)
        mixer.music.stop()
        self.paused = False
        self.is_playing = False

    #to resume the song
    def Resume(self):
        root.play_button.config(image=root.photo_pause)
        mixer.music.unpause()
        self.paused = False
        self.is_playing = True

    #Function to navigate from the current song
    def Previous(self):
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

class Gui(Tk):
    
    def __init__(self):
        ctypes.windll.shcore.SetProcessDpiAwareness(1) # per migliorare risoluzione schermo
        Tk.__init__(self)
        self.title('BuddyBeat')
        self.configure(background='#fcfaf2', width=450, height=300)
        self.mp = MediaPlayer()
        self.initialize_gui(self.mp)
        self.songlist={}
        
    def initialize_gui(self, mp):

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
        self.play_button=Button(frB,width=50, height=60, image=self.photo_play, bg="#fcfaf2", borderwidth=0, command=mp.Play_Pause)
        self.play_button['font']=defined_font
        self.play_button.grid(row=1,column=149)

        #stop button
        self.stop_button=Button(frB,width =50,height=60, image=self.photo_stop, bg="#fcfaf2", borderwidth=0, command=mp.Stop)
        self.stop_button['font']=defined_font
        self.stop_button.grid(row=1,column=150)

        #previous button
        self.previous_button=Button(frB,width =50,height=60, image=self.photo_previous, bg="#fcfaf2", borderwidth=0, command=mp.Previous)
        self.previous_button['font']=defined_font
        self.previous_button.grid(row=1,column=148)

        #nextbutton
        self.next_button=Button(frB,width =50,height=60,image=self.photo_next, bg="#fcfaf2",borderwidth=0, command=mp.Next)
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
        for file in os.listdir(tmp):
        #bpm = BPM_extractor.get_bpm(str(tmp)+'/'+str(file))
        # do some stuff
            #download_thread = threading.Thread(target=BPM_extractor.get_bpm, name="BPM_extractor")
            #download_thread.start()
        # continue doing stuff
            bpm=0
            self.songs_list.insert(END,os.path.splitext(file)[0])
            self.songlist.update({str(os.path.splitext(file)[0]): [str(tmp)+'/'+str(file), bpm]}) #full path
        #print(songs_list)
        #print(songlist)
            
    def deletesong(self):
        curr_song=self.songs_list.curselection()
        self.songs_list.delete(curr_song[0])
        self.songlist.popitem(curr_song[0])

if __name__ == '__main__':
    
    root = Gui()
    root.mainloop()    


