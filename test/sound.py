import array
from pydub import AudioSegment
from pydub.utils import get_array_type
import sounddevice

sound = AudioSegment.from_file(file="src/whenever.mp3")
left = sound.split_to_mono()[0]

bit_depth = left.sample_width * 8
array_type = get_array_type(bit_depth)

numeric_array = array.array(array_type, left._data)

sounddevice.play(numeric_array)
sounddevice.wait()
