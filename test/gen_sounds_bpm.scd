(
var bpm = 120;  // default bpm
var dur = 60.0 / bpm;  // duration of a quarter note

// Define the selected frequencies and their probabilities
var degrees = [0, 2, 4, 5, 7, 9, 11, 12];  // degrees of a major scale
var probs = [1/8, 1/8, 1/8, 1/8, 1/8, 1/8, 1/8, 1/8];  // equal probabilities for each degree

// Create a Pbind that plays a sequence of notes
var pattern = Pbind(
    \instrument, \default,
    \dur, Pfunc({ dur }),  // use the current value of dur
	\degree, Prand(Pwrand(degrees, probs, 6).asStream.all, inf) // choose a random frequency for each note; the sequence has a complete duration of delta_t seconds (see correspondent python file)
);

// OSCdef updates bpm and dur when a message is received (default port 57120)
OSCdef(\myOSCListener, { |msg, time, addr, recvPort|
    bpm = msg[1];
    dur = 60.0 / bpm;
    "Received BPM value".postln;
    bpm.postln;
}, '/bpm');

// Play the pattern
pattern.play;
)