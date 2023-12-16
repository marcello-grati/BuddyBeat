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
is_playing=False
paused=False

def addsongs():
    #a list of songs is returned 
    temp_song=filedialog.askopenfilenames(initialdir="/Users",title="Choose a song", filetypes=[(".mp3 files","*.mp3"), (".wav files", "*.wav"),("all files", "*.*")])
    #temp_song=filedialog.askopenfilenames(initialdir="/Music",title="Choose a song", filetypes=[(".mp3 files","*.mp3"), (".wav files", "*.wav"),("all files", "*.*")])
    for s in temp_song:
        tmp = s
        x=(os.path.splitext(s)[0]).split('/')[-1]
        songlist.update({str(x): str(tmp)}) #full path
        songs_list.insert(END,x) #name on list
        
            
def deletesong():
    curr_song=songs_list.curselection()
    songs_list.delete(curr_song[0])
    songlist.popitem(curr_song[0])
    
def Play_Pause():
    
    if(is_playing and not paused):
        Pause()
        
    elif(not is_playing and paused):
        Resume()
    
    elif(not is_playing and not paused):
        Play()
    
   
    
def Play():
    global is_playing
    global paused
    play_button.config(image=photo_pause)
    song=songs_list.get(ACTIVE)
    title=songlist.get(song)
    mixer.music.load(title)
    mixer.music.play()
    is_playing = True
    paused = False
    BPM_extractor.print_bpm(title)
    

#to pause the song 
def Pause():
    global is_playing
    global paused
    play_button.config(image=photo_play)
    mixer.music.pause()
    paused = True
    is_playing = False

#to stop the  song 
def Stop():
    global is_playing
    global paused
    songs_list.selection_clear(ACTIVE)
    play_button.config(image=photo_play)
    mixer.music.stop()
    paused = False
    is_playing = False

#to resume the song

def Resume():
    global is_playing
    global paused
    play_button.config(image=photo_pause)
    mixer.music.unpause()
    paused = False
    is_playing = True

#Function to navigate from the current song
def Previous():
    global is_playing
    global paused
    #to get the selected song index
    previous_one=songs_list.curselection()
    #to get the previous song index
    previous_one = (previous_one[0]-1+len(songlist))%len(songlist)
    songs_list.selection_clear(0,END)
    #activate new song
    songs_list.activate(previous_one)
    #set the next song
    songs_list.selection_set(previous_one)
    play_button.config(image=photo_pause)
    #to get the previous song
    temp2=songs_list.get(previous_one)
    title=songlist.get(temp2)
    mixer.music.load(title)
    mixer.music.play()
    BPM_extractor.print_bpm(title)
    is_playing = True
    paused = False
    

def Next():
    global is_playing
    global paused
    #to get the selected song index
    next_one=songs_list.curselection()
    #to get the next song index
    next_one=(next_one[0]+1)%len(songlist)
    songs_list.selection_clear(0,END)
    #activate newsong
    songs_list.activate(next_one)
     #set the next song
    songs_list.selection_set(next_one)
    play_button.config(image=photo_pause)
    #to get the next song 
    temp=songs_list.get(next_one)
    title=songlist.get(temp)
    mixer.music.load(title)
    mixer.music.play()
    is_playing = True
    paused = False
    BPM_extractor.print_bpm(title)
    

#creating the root window 
root=Tk()
root.title('BuddyBeat')
root.configure(background='#fcfaf2')

#initialize mixer 
mixer.init()

#create the listbox to contain songs
songs_list=Listbox(root,selectmode=BROWSE,bg="#fcfaf2",fg="black",font=('arial',15),height=12,width=45,
                   selectbackground="#c9ffe8", selectforeground="black", activestyle='none', border=3)
songs_list.grid(columnspan=300)

#font is defined which is to be used for the button font 
defined_font = font.Font(family='Helvetica')

photo_play = PhotoImage(file = r"src/play.png").subsample(10,10)
photo_pause = PhotoImage(file = r"src/pause.png").subsample(10, 10) 
photo_stop = PhotoImage(file = r"src/stop.png").subsample(10, 10) 
photo_next = PhotoImage(file = r"src/next.png").subsample(10, 10) 
photo_previous = PhotoImage(file = r"src/back.png").subsample(10, 10) 

#play button
play_button=Button(root,width=50, height=50, command=Play_Pause, image=photo_play, bg="#fcfaf2")
play_button['font']=defined_font
play_button.grid(row=1,column=148)

#stop button
stop_button=Button(root,width =50,height=50,command=Stop, image=photo_stop, bg="#fcfaf2")
stop_button['font']=defined_font
stop_button.grid(row=1,column=149)

#previous button
previous_button=Button(root,width =50,height=50,command=Previous, image=photo_previous, bg="#fcfaf2")
previous_button['font']=defined_font
previous_button.grid(row=1,column=150)

#nextbutton
next_button=Button(root,width =50,height=50,command=Next, image=photo_next, bg="#fcfaf2")
next_button['font']=defined_font
next_button.grid(row=1,column=151)

#menu 
my_menu=Menu(root)
root.config(menu=my_menu)
add_song_menu=Menu(my_menu)
my_menu.add_cascade(label="Menu",menu=add_song_menu)
add_song_menu.add_command(label="Add songs",command=addsongs)
add_song_menu.add_command(label="Delete song",command=deletesong)


mainloop()
