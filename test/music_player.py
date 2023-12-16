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
    tmp = filedialog.askdirectory(initialdir="/Users")
    for file in os.listdir(tmp):
        #bpm = BPM_extractor.get_bpm(str(tmp)+'/'+str(file))
        bpm=0
        songs_list.insert(END,os.path.splitext(file)[0])
        songlist.update({str(os.path.splitext(file)[0]): [str(tmp)+'/'+str(file), bpm]}) #full path
    print(songs_list)
    print(songlist)
    
    #temp_song=filedialog.askopenfilenames(initialdir="/Users",title="Choose a song", filetypes=[(".mp3 files","*.mp3"), (".wav files", "*.wav"),("all files", "*.*")])
    #for s in temp_song:
    #    tmp = s
    #    x=(os.path.splitext(s)[0]).split('/')[-1]
    #    songlist.update({str(x): str(tmp)}) #full path
    #    songs_list.insert(END,x) #name on list
        
            
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
    
def play_song(song):
    global is_playing
    global paused
    title=songlist.get(song)[0]
    mixer.music.load(title)
    mixer.music.play()
    is_playing = True
    paused = False
    
def Play():
    play_button.config(image=photo_pause)
    song=songs_list.get(ACTIVE)
    play_song(song)
    

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
    #to get the selected song index
    previous_one=songs_list.curselection()
    #to get the previous song index
    previous_one = (previous_one[0]-1+len(songlist))%len(songlist)
    songs_list.selection_clear(0,END)
    #activate new song
    songs_list.activate(previous_one)
    #set the next song
    songs_list.selection_set(previous_one)
    songs_list.see(previous_one)
    play_button.config(image=photo_pause)
    #to get the previous song
    temp=songs_list.get(previous_one)
    play_song(temp)
    
    

def Next():
    #to get the selected song index
    next_one=songs_list.curselection()
    #to get the next song index
    next_one=(next_one[0]+1)%len(songlist)
    songs_list.selection_clear(0,END)
    #activate newsong
    songs_list.activate(next_one)
     #set the next song
    songs_list.selection_set(next_one)
    songs_list.see(next_one)
    play_button.config(image=photo_pause)
    #to get the next song 
    temp=songs_list.get(next_one)
    play_song(temp)

#GUI

#creating the root window 
root=Tk()
root.title('BuddyBeat')
root.configure(background='#fcfaf2', width=450, height=300)

frA = Frame()
frA.pack()

frB = Frame()
frB.pack()

#initialize mixer 
mixer.init()

#create the listbox to contain songs
songs_list=Listbox(frA,selectmode=BROWSE,bg="#fcfaf2",fg="black",font=('arial',15),height=12,width=45,
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
play_button=Button(frB,width=50, height=60, command=Play_Pause, image=photo_play, bg="#fcfaf2", borderwidth=0)
play_button['font']=defined_font
play_button.grid(row=1,column=149)

#stop button
stop_button=Button(frB,width =50,height=60,command=Stop, image=photo_stop, bg="#fcfaf2", borderwidth=0)
stop_button['font']=defined_font
stop_button.grid(row=1,column=150)

#previous button
previous_button=Button(frB,width =50,height=60,command=Previous, image=photo_previous, bg="#fcfaf2", borderwidth=0)
previous_button['font']=defined_font
previous_button.grid(row=1,column=148)

#nextbutton
next_button=Button(frB,width =50,height=60,command=Next, image=photo_next, bg="#fcfaf2",borderwidth=0)
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
