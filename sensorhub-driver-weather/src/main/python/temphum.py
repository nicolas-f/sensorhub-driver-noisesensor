# BSD 3-Clause License
#
# Copyright (c) 2018, Ifsttar and Wi6labs
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
#
#  Redistributions of source code must retain the above copyright notice, this
#   list of conditions and the following disclaimer.
#
#  Redistributions in binary form must reproduce the above copyright notice,
#   this list of conditions and the following disclaimer in the documentation
#   and/or other materials provided with the distribution.
#
#  Neither the name of the copyright holder nor the names of its
#   contributors may be used to endorse or promote products derived from
#   this software without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
# AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
# DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
# FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
# DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
# SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
# CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
# OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
# OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

import fcntl
import io

try:
    from http.server import HTTPServer, BaseHTTPServer
    from http.server import BaseHTTPRequestHandler
    from http import HTTPStatus
except ImportError:
    from BaseHTTPServer import BaseHTTPRequestHandler, HTTPServer

import getopt
import sys
import os
import threading
import struct
import time
import datetime
import json
import ftplib
import uuid
import collections


class SensorServer(HTTPServer):
    def __init__(self, *args, **kwargs):
        # Because HTTPServer is an old-style class, super() can't be used.
        HTTPServer.__init__(self, *args, **kwargs)
        self.daemon = True
        self.fast = []

        ##
        # Init sensor link

    def unix_time_millis(self):
        return (datetime.datetime.utcnow() - datetime.datetime.utcfromtimestamp(0)).total_seconds()

    def read_sensor(self):

        bus = 1

        ioctl_i2c_slave = 0x0703
        sht30_slave_addr = 0x44

        address = ioctl_i2c_slave
        device = sht30_slave_addr

        fr = io.open("/dev/i2c-" + str(bus), "rb", buffering=0)
        fw = io.open("/dev/i2c-" + str(bus), "wb", buffering=0)

        res = None
        try:
            length = 6

            # set device address
            fcntl.ioctl(fr, address, device)
            fcntl.ioctl(fw, address, device)

            if fw.write(b'\x2c\x06') != 2:
                raise ValueError("Failed to write to the i2c bus.")

            res = fr.read(length)
        finally:
            fr.close()
            fw.close()

        if len(res) != length:
            raise ValueError("Failed to read from the i2c bus.")

        res = map(ord, res)

        temp = res[0] * 256 + res[1]

        c_temp = -45 + (175 * temp / 65535.0)

        system_temp = -9999
        try:
            system_temp = float(open('/sys/class/thermal/thermal_zone0/temp').read()) / 1000.
        except IOError:
            pass

        humidity = 100 * (res[3] * 256 + res[4]) / 65535.0

        return int(self.unix_time_millis()), system_temp, c_temp, humidity

    def push_data(self, line):
        self.fast.insert(0, line)
        self.fast = self.fast[:100]


class SensorHttpServe(BaseHTTPRequestHandler):

    def do_HEAD(self):
        self.send_response(200)
        self.send_header('Content-type', 'text/plain;charset=UTF-8')
        self.end_headers()

    def do_GET(self):
        self.send_response(200)
        self.send_header('Content-type', 'text/plain;charset=UTF-8')
        self.end_headers()
        self.wfile.write(b'%d,%.2f,%.2f,%.1f\n' % self.server.read_sensor())
        return


class HttpServer(threading.Thread):
    def __init__(self, server_class=SensorServer, handler_class=SensorHttpServe,
                 port=8000):
        threading.Thread.__init__(self)
        self.daemon = True
        self.port = port
        server_address = ('localhost', port)
        self.httpd = server_class(server_address, handler_class)

    def run(self):
        try:
            print("Server works on http://localhost:%d" % self.port)
            self.httpd.serve_forever()
        except KeyboardInterrupt:
            print("Stop the server on http://localhost:%d" % self.port)
            self.httpd.socket.close()


def main():
    # parse command line options
    port = 8000
    try:
        for opt, value in getopt.getopt(sys.argv[1:], "p:")[0]:
            if opt == "-p":
                port = int(value)
    except getopt.error as msg:
        usage()
        exit(-1)

    # Http server
    httpserver = HttpServer(port=port)
    httpserver.run()


if __name__ == "__main__":
    main()







