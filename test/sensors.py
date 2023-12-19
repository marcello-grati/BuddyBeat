from tkinter import *
import argparse
from pythonosc import udp_client
import os
import ctypes

first_value = 60
min_first = 50
max_first = 170
first_label = "Heart Rate"

second_value = 100
min_second = 70
max_second = 200
second_label = "Steps / min"


def start_osc_communication():
    # argparse helps writing user-friendly commandline interfaces
    parser = argparse.ArgumentParser()
    # OSC server ip
    parser.add_argument("--ip", default='127.0.0.1', help="The ip of the OSC server")
    # OSC server port (check on SuperCollider)
    parser.add_argument("--port", type=int, default=57120, help="The port the OSC server is listening on")

    # Parse the arguments
    args = parser.parse_args()

    # Start the UDP Client
    client = udp_client.SimpleUDPClient(args.ip, args.port)

    return client

client = start_osc_communication()

#creating the root window 
root=Tk()
if os.name == 'nt':
            ctypes.windll.shcore.SetProcessDpiAwareness(1) # per migliorare risoluzione schermo
root.title('Sensors')
root.geometry('500x300') 

def update_first_value(v):
    global first_value
    first_value = v
    
def update_second_value(v):
    global second_value
    second_value = v

def send_values():
    client.send_message("/sensors", [first_value, second_value])
    #print(first_value, second_value)
    root.after(100, send_values)

first_slider = Scale(root, from_=min_first, to_=max_first, orient=HORIZONTAL, length=500, width=30, label=first_label, command=update_first_value)
first_slider.set(first_value)
first_slider.pack()

second_slider = Scale(root, from_=min_second, to_=max_second, orient=HORIZONTAL, length=500, width=30, label=second_label, command=update_second_value)
second_slider.set(second_value)
second_slider.pack()

root.after(1000, send_values)
mainloop()