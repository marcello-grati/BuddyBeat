#importing libraries 
from pygame import mixer
from tkinter import *
import tkinter.font as font
from tkinter import filedialog
import BPM_extractor
import os
import ctypes
 
ctypes.windll.shcore.SetProcessDpiAwareness(1) # per migliorare risoluzione schermo


songlist={}

def addsongs():
    #a list of songs is returned 
    temp_song=filedialog.askopenfilenames(initialdir="/Users",title="Choose a song", filetypes=[(".mp3 files","*.mp3"), (".wav files", "*.wav"),("all files", "*.*")])
    for s in temp_song:
        tmp = s
        x=(os.path.splitext(s)[0]).split('/')[-1]
        songlist.update({str(x): str(tmp)}) #full path
        songs_list.insert(END,x) #name on list
        
            
def deletesong():
    curr_song=songs_list.curselection()
    songs_list.delete(curr_song[0])
    songlist.popitem(curr_song[0])
    
    
def Play():
    song=songs_list.get(ACTIVE)
    title=songlist.get(song)
    BPM_extractor.print_bpm(title)
    mixer.music.load(title)
    mixer.music.play()

#to pause the song 
def Pause():
    mixer.music.pause()

#to stop the  song 
def Stop():
    mixer.music.stop()
    songs_list.selection_clear(ACTIVE)

#to resume the song

def Resume():
    mixer.music.unpause()

#Function to navigate from the current song
def Previous():
    #to get the selected song index
    previous_one=songs_list.curselection()
    #to get the previous song index
    previous_one=previous_one[0]-1
    #to get the previous song
    temp2=songs_list.get(previous_one)
    title=songlist.get(temp2)
    mixer.music.load(title)
    mixer.music.play()
    BPM_extractor.print_bpm(title)
    songs_list.selection_clear(0,END)
    #activate new song
    songs_list.activate(previous_one)
    #set the next song
    songs_list.selection_set(previous_one)

def Next():
    #to get the selected song index
    next_one=songs_list.curselection()
    #to get the next song index
    next_one=next_one[0]+1
    #to get the next song 
    temp=songs_list.get(next_one)
    title=songlist.get(temp)
    mixer.music.load(title)
    mixer.music.play()
    BPM_extractor.print_bpm(title)
    songs_list.selection_clear(0,END)
    #activate newsong
    songs_list.activate(next_one)
     #set the next song
    songs_list.selection_set(next_one)

#creating the root window 
root=Tk()
root.title('BuddyBeat')

#initialize mixer 
mixer.init()

#create the listbox to contain songs
songs_list=Listbox(root,selectmode=SINGLE,bg="black",fg="white",font=('arial',15),height=12,width=47,selectbackground="gray",selectforeground="black")
songs_list.grid(columnspan=18)

#font is defined which is to be used for the button font 
defined_font = font.Font(family='Helvetica')

photo_play = PhotoImage(file = r"src/play.png").subsample(10,10)
photo_pause = PhotoImage(file = r"src/pause.png").subsample(10, 10) 
photo_stop = PhotoImage(file = r"src/stop.png").subsample(10, 10) 
photo_next = PhotoImage(file = r"src/next.png").subsample(10, 10) 
photo_previous = PhotoImage(file = r"src/back.png").subsample(10, 10) 

#play button
play_button=Button(root,text="Play",width=50, height=50, command=Play, image=photo_play)
play_button['font']=defined_font
play_button.grid(row=1,column=0, columnspan=3)

#pause button 
pause_button=Button(root,text="Pause",width =50,height=50,command=Pause, image= photo_pause)
pause_button['font']=defined_font
pause_button.grid(row=1,column=3,columnspan=3)

#stop button
stop_button=Button(root,text="Stop",width =50,height=50,command=Stop, image=photo_stop)
stop_button['font']=defined_font
stop_button.grid(row=1,column=6,columnspan=3)

#resume button
Resume_button=Button(root,text="Resume",width =50,height=50,command=Resume, image=photo_play)
Resume_button['font']=defined_font
Resume_button.grid(row=1,column=9,columnspan=3)

#previous button
previous_button=Button(root,text="Prev",width =50,height=50,command=Previous, image=photo_previous)
previous_button['font']=defined_font
previous_button.grid(row=1,column=12,columnspan=3)

#nextbutton
next_button=Button(root,text="Next",width =50,height=50,command=Next, image=photo_next)
next_button['font']=defined_font
next_button.grid(row=1,column=15,columnspan=3)

#menu 
my_menu=Menu(root)
root.config(menu=my_menu)
add_song_menu=Menu(my_menu)
my_menu.add_cascade(label="Menu",menu=add_song_menu)
add_song_menu.add_command(label="Add songs",command=addsongs)
add_song_menu.add_command(label="Delete song",command=deletesong)


mainloop()
