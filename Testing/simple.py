#!/usr/bin/python
import nxt.locator
import nxt.brick
import nxt.error
import sys
# 
# # Find the brick and chat with NXT.
# sock = nxt.locator.find_one_brick ()
# if sock:
#     b = sock.connect ()
#     # Send my name :).
#     b.message_write (0, "Ni")
#     # Retrieve the response.
#     (inbox, message) = b.message_read (10, 0, True)
#     print inbox, message
#     # What happen if no response is available?
#     try:
#         (inbox, message) = b.message_read (10, 0, True)
#     except nxt.error.DirProtError, e:
#         print "error: ", e.message
#     # Bye bye NXT.
#     sock.close ()


sock = nxt.locator.find_one_brick()
if not sock:
    print "ERROR!"
    sys.exit(1)
b = sock.connect()
b.message_write(0, "Hello World")
