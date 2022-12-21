# blink.py

import machine
import time

pin_down = machine.Pin(11, machine.Pin.OUT,1)
led = machine.Pin("LED", machine.Pin.OUT) # "LED" is the on board LED

# blink ten times
for i in range(4):
    print('Blinking... %s' %str(i+1))
    led.toggle()
#     pin_down.on()
    time.sleep(0.5)
#     pin_down.off()