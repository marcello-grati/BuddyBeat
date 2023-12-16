import argparse
from pythonosc.dispatcher import Dispatcher
from pythonosc import osc_server

if __name__ == "__main__":
  parser = argparse.ArgumentParser()
  parser.add_argument("--ip", default="127.0.0.1", help="The ip to listen on")
  parser.add_argument("--port", type=int, default=57120, help="The port to listen on")
  args = parser.parse_args()

  dispatcher = Dispatcher()
  dispatcher.map("/sensors", print)

  server = osc_server.ThreadingOSCUDPServer(
      (args.ip, args.port), dispatcher)
  print("Serving on {}".format(server.server_address))
  server.serve_forever()