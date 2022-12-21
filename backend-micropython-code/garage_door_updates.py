import machine
import time


#Input of all the LEDs that will blink when the door opens or closed or is stopped
led = machine.Pin("LED", machine.Pin.OUT)
pin_down = machine.Pin(11, machine.Pin.OUT, 0)
pin_up = machine.Pin(15, machine.Pin.OUT, 0)
pin_stop = machine.Pin(18, machine.Pin.OUT, 0)

#commented since sensor not working due to wiring issues
doorsensor=0
#doorsensor=machine.Pin(26,Pin.IN)g

#commented since sensor not working due to wiring issues
proximitysensor=0
#proximitysensor=machine.Pin(15,pin.IN)#commented since sensor not working

#approximate time it takes for garrage door to close
closing_time=10
#openingtime=50

string = "Last state was %s"

def blink_led(frequency , num_blinks):
    for _ in range(num_blinks):
        led.on()
        time.sleep(frequency)
        led.off()
        time.sleep(frequency)


#main functionality to serve the client i.e. to open the door and close the door according to the instruction received by the remote application
def serve_client(data):
    print(data)
    print('Client connected')
    
    stateis = "" # Keeps track of the last command issued
            
    if data == '1':
        stateis = "Door: UP"
        print(stateis)
        control_door('up')
        
    elif data == '2':
        stateis = "Door: DOWN"
        print(stateis)
        control_door('down')
    
    response = string % stateis

#Each command and the blinking of light according to the command received from the app
def control_door(cmd):
    #condition when door is stopped by sensor
    if cmd == 'stop':
        pin_stop.on()
        blink_led(0.1, 1)
        pin_stop.off()
    
    #condition when door is opened by user
    if cmd == 'up':
        #pin_up.on()
        blink_led(0.5, 3)
        #pin_down.off()
    
    #condition when door is closed by user
    #Code also includes all the functionalities involving sensor
    if cmd == 'down':
        for i in range(closing_time) :
#             if doorsensor.value()==0:
            if doorsensor==0:     
                print('door closing...' + str(i))
#                if proximitysensor.value()==1:
                if proximitysensor==1:
                    print('Obstruction detected!!')
                    control_door(stop)
                    time.sleep(0.5)
                    control_door(up)
                    time.sleep(0.5)
                    control_door(down)
            else:
                print('door closed...')
                break;
        print('door now closed...')
        #pin_down.on()
        blink_led(0.5, 6)
        #pin_down.off()
    return 1
        

def main():
    # Ensure that you have your keys ("@alice_key.atKeys") inside of the "keys" folder.
    # Add it now if you have not already.

    from lib.at_client import io_util
    from lib.at_client import keys_util

    s, p, atSign = io_util.read_settings()
    del s, p
    
    keys_util.initialize_keys(atSign)
    pass

    import sys
    shouldRun = input('Run (y/n): ')
    if(shouldRun != 'y'):
        sys.exit(1)
    del sys

    # read settings.json
    from lib.at_client import io_util
    ssid, password, atSign = io_util.read_settings()
    del io_util  # make space in memory

    # connect to wifi
    from lib import wifi
    print('Connecting to WiFi %s...' % ssid)
    wifi.init_wlan(ssid, password)
    del ssid, password, wifi  # make space in memory

    # connect and pkam authenticate into secondary 
    from lib.at_client import at_client
    atClient = at_client.AtClient(atSign, writeKeys=True)
    atClient.pkam_authenticate(verbose=True)
    del at_client

    #connecting to the app atsign
    key = 'led'
    appAtSign = '@atSign'
    #will keep checking for signal to open and close the doors from the application
    while True:
        key = 'instructions'
        appAtSign = '@acidrock20'
        data = atClient.get_public(key, appAtSign)
        #data = int(data)
        print(data)
        #print(type(data))
        if data == '1' or data == '2':
            print('inside if')
            serve_client(data)
            break;
        del data #make space in memory
        print('loop continue')
    print('Execution Complete.')


if __name__ == '__main__':
    main()
