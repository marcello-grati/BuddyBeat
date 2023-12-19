import argparse
from pythonosc.dispatcher import Dispatcher
from pythonosc import osc_server
import threading

initial_bio_value = 60
initial_sport_value = 100
initial_bpm_value = 100
ALPHA = 0.2
BETA = 0.9
bio_weight = 0.4
sport_weight = 0.6

class BPM_computer:
  def __init__(self):

    self.bpm = initial_bpm_value
    self.bio_value = initial_bio_value
    self.sport_value = initial_sport_value
    self.thread = threading.Thread(target=self.thread_function)
    self.thread.start()
    
  def thread_function(self):
    parser = argparse.ArgumentParser()
    parser.add_argument("--ip", default="127.0.0.1", help="The ip to listen on")
    parser.add_argument("--port", type=int, default=57120, help="The port to listen on")
    args = parser.parse_args()

    dispatcher = Dispatcher()
    dispatcher.map("/sensors", self.update_sensors)

    server = osc_server.ThreadingOSCUDPServer(
        (args.ip, args.port), dispatcher)
    print("Serving on {}".format(server.server_address))
    server.serve_forever()

  def update_bpm(self):
    new_bpm = (bio_weight * self.bio_value) + (sport_weight * self.sport_value)
    self.bpm = int(ALPHA * new_bpm + (1-ALPHA) * self.bpm)
    # print(self.bpm)

  def update_sensors(self, a, b, c):
    self.bio_value = int(BETA * float(b) + (1-BETA) * self.bio_value)
    self.sport_value = int(BETA * float(c) + (1-BETA) * self.sport_value)
    self.update_bpm()
    # print(self.bio_value, self.sport_value)

  def get_ideal_bpm(self):
    print(self.bio_value, self.sport_value)
    return self.bpm
  
  
