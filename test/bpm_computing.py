import argparse
from pythonosc.dispatcher import Dispatcher
from pythonosc import osc_server

initial_bio_value = 60
initial_sport_value = 100
initial_bpm_value = 100
alpha = 0.05
bio_weight = 0.3
sport_weight = 0.7

bpm = initial_bpm_value
bio_value = initial_bio_value
sport_value = initial_sport_value

def update_sensors(a, b, c):
  global bio_value, sport_value, alpha

  bio_value = alpha * float(b) + (1-alpha)*bio_value
  sport_value = alpha * float(c) + (1-alpha)*sport_value
  update_bpm()
  #print(bio_value, sport_value)

def update_bpm():
  global bpm, bio_value, sport_value, alpha
  new_bpm = bio_weight * bio_value + sport_weight * sport_value
  bpm = int(alpha * new_bpm + (1-alpha)*bpm)
  print(bpm)

def get_ideal_bpm():
  return bpm


def bpm_computing_startup():
  parser = argparse.ArgumentParser()
  parser.add_argument("--ip", default="127.0.0.1", help="The ip to listen on")
  parser.add_argument("--port", type=int, default=57120, help="The port to listen on")
  args = parser.parse_args()

  dispatcher = Dispatcher()
  dispatcher.map("/sensors", update_sensors)

  server = osc_server.ThreadingOSCUDPServer(
      (args.ip, args.port), dispatcher)
  print("Serving on {}".format(server.server_address))
  server.serve_forever()