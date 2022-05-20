#!/usr/bin/env python3
import datetime
import inspect
import math
import os
import random
import string
from enum import Enum
from sys import argv
import sys
import struct
import traceback
from typing import Optional

from fakesession import FakeSession

"""
    Config
"""

# SERVICE INFO
PORT = 3

# DEBUG -- logs to stderr, TRACE -- log HTTP requests
DEBUG = os.getenv("DEBUG", True)

"""
    Methods
"""


def info():
    print('{"vulns": 1, "timeout": 30, "attack_data": ""}', flush=True, end="")
    exit(101)


def check(host: str):
    api = LocatorApi(host)
    name = rand_string(12, string.hexdigits).lower()
    token = api.create(name)
    if name not in api.list():
        raise MumbleError("Beacon is not in list")
    report = Report(str(datetime.datetime.now()), rand_loc(), rand_string())
    report.put_in(api, name)
    if report.timestamp not in api.get_public(name):
        raise MumbleError("Timestamp is not found for beacon")
    try:
        report.check_in(api, name, token)
    except CorruptError as e:
        raise MumbleError("No data found") from e


def put(host: str, flag_id: str, flag: str, vuln: str):
    api = LocatorApi(host)
    random.seed(int.from_bytes(flag_id.encode(), 'big'))
    name = rand_string(12, string.hexdigits).lower()
    loc = rand_loc()
    token = api.create(name)
    report = Report(str(datetime.datetime.now()), loc, flag)
    report.put_in(api, name)
    die(ExitStatus.OK, f"{flag_id};{token}")


def get(host: str, flag_id: str, flag: str, vuln: str):
    flag_id, token = flag_id.split(";")
    api = LocatorApi(host)
    random.seed(int.from_bytes(flag_id.encode(), 'big'))
    name = rand_string(12, string.hexdigits).lower()
    loc = rand_loc()
    report = Report(None, loc, flag)
    report.check_in(api, name, token)


"""
    Helpers
"""


class LocatorApi:
    def __init__(self, host: str):
        self.s = FakeSession(host, PORT)

    # list beacons, returns list of ids
    def list(self):
        _log("list()")
        r = self.s.get("/beacons")
        _log(f"-> [{r.status_code}] {r.text}")
        if r.status_code != 200:
            raise MumbleError(f"Bad response: {r.text}")
        x = r.json()
        if not isinstance(x, list):
            raise MumbleError("List is not array")
        if any([not isinstance(s, str) for s in x]):
            raise MumbleError("List is not of strings")
        return x

    # get beacon public info (last timestamp)
    def get_public(self, name: str):
        _log(f"get_public({name})")
        r = self.s.get(f"/beacons/{name}")
        _log(f"-> [{r.status_code}] {r.text}")
        if r.status_code != 200:
            raise MumbleError(f"Bad response: {r.text}")
        x = r.json()
        if not isinstance(x, list):
            raise MumbleError("List is not array")
        if any([not isinstance(s, str) for s in x]):
            raise MumbleError("List is not of strings")
        return x

    # create new beacon
    # returns jwt token to access beacon info
    def create(self, name: str):
        _log(f"create({name})")
        r = self.s.put(f"/beacons/{name}")
        _log(f"-> [{r.status_code}] {r.text}")
        if r.status_code != 200:
            raise MumbleError(f"Bad response: {r.text}")
        return r.text

    def get_private(self, name: str, token: str):
        _log(f"get_private({name}, {token})")
        r = self.s.get(f"/beacons/{name}/private", headers={"X-Auth": token})
        _log(f"-> [{r.status_code}] {r.text}")
        if r.status_code != 200:
            raise MumbleError(f"Bad response: {r.text}")
        entries = r.json()
        if not isinstance(entries, list):
            raise MumbleError("List is not array")
        for entry in entries:
            if not isinstance(entry, dict):
                raise MumbleError("Entry is not dict")
            if list(entry.keys()) != ["timestamp", "location", "comment"]:
                raise MumbleError("Wrong entry keys")
            if not isinstance(entry["timestamp"], str):
                raise MumbleError("Wrong timestamp")
            if not isinstance(entry["location"], str):
                raise MumbleError("Wrong location")
            if not isinstance(entry["comment"], str):
                raise MumbleError("Wrong comment")
        return entries

    def report(self, name: str, data: bytes):
        _log(f"report({name}, {data})")
        r = self.s.post(f"/beacons/{name}", data=data)
        _log(f"-> [{r.status_code}] {r.text}")
        if r.status_code != 200:
            raise MumbleError(f"Bad response: {r.text}")
        if r.text != "ok":
            raise MumbleError("Report is not ok")


class Location:
    def __init__(self, lat, lon):
        self.lat = lat
        self.lon = lon


class Report:
    def __init__(self, timestamp: Optional[str], location: Location, comment: str):
        self.timestamp = timestamp
        self.location = location
        self.comment = comment

    def put_in(self, api: LocatorApi, name: str):
        _log(f"Report is {self}")
        api.report(name, self.encode_report("#{lat} #{lon}"))

    def check_in(self, api: LocatorApi, name: str, token: str):
        _log(f"Report is {self}")
        for entry in api.get_private(name, token):
            location = self.decode_location(entry["location"])
            if (self.timestamp is None or entry["timestamp"] == self.timestamp) \
                    and math.isclose(self.location.lat, location.lat, abs_tol=0.1) \
                    and math.isclose(self.location.lon, location.lon, abs_tol=0.1) \
                    and entry["comment"] == self.comment:
                break
        else:
            raise CorruptError("Entry not found")

    def __repr__(self):
        return f"Report(timestamp='{self.timestamp}', location=({self.location.lat}, {self.location.lon}), comment='{self.comment}')"

    @classmethod
    def decode_location(cls, encoded: str):
        locat = encoded.split(" ")
        if len(locat) != 2:
            raise MumbleError("Wrong location")
        try:
            lat, lon = float(locat[0]), float(locat[1])
        except ValueError as e:
            raise MumbleError("Wrong location")
        return Location(lat, lon)

    def encode_report(self, location_format: str):
        return encode_string(self.timestamp) \
               + encode_float(self.location.lat) + encode_float(self.location.lon) + encode_string(location_format) \
               + encode_string(self.comment)


def encode_string(s: str):
    return bytes([len(s)]) + s.encode()


def encode_float(f: float):
    return struct.pack("<f", f)


def rand_string(n=12, alphabet=string.ascii_uppercase + string.ascii_lowercase + string.digits):
    return ''.join(random.choice(alphabet) for _ in range(n))


def rand_loc():
    lat = random.uniform(-90, 90)
    lon = random.uniform(-180, 180)
    return Location(lat, lon)


def _log(obj):
    if DEBUG and obj:
        caller = inspect.stack()[1].function
        print(f"[{caller}] {obj}", file=sys.stderr)
    return obj


class ExitStatus(Enum):
    OK = 101
    CORRUPT = 102
    MUMBLE = 103
    DOWN = 104
    CHECKER_ERROR = 110


class CheckerError(RuntimeError):
    def __init__(self, *args: object):
        super().__init__(*args)


class CorruptError(CheckerError):
    def __init__(self, *args: object):
        super().__init__(*args)


class MumbleError(CheckerError):
    def __init__(self, *args: object):
        super().__init__(*args)


class DownError(CheckerError):
    def __init__(self, *args: object):
        super().__init__(*args)


class WrongArgumentsError(CheckerError):
    def __init__(self, *args: object):
        super().__init__(*args)


def die(code: ExitStatus, msg: str):
    if msg:
        print(msg, file=sys.stderr)
    exit(code.value)


def _main():
    try:
        if len(argv) < 3:
            raise WrongArgumentsError()
        cmd = argv[1]
        hostname = argv[2]
        if cmd == "get":
            if len(argv) < 6:
                raise WrongArgumentsError()
            fid, flag, vuln = argv[3], argv[4], argv[5]
            get(hostname, fid, flag, vuln)
        elif cmd == "put":
            if len(argv) < 6:
                raise WrongArgumentsError()
            fid, flag, vuln = argv[3], argv[4], argv[5]
            put(hostname, fid, flag, vuln)
        elif cmd == "check":
            check(hostname)
        elif cmd == "info":
            info()
        else:
            raise WrongArgumentsError()
        die(ExitStatus.OK, "OK")
    except CorruptError as e:
        die(ExitStatus.CORRUPT, traceback.format_exc())
    except MumbleError as e:
        die(ExitStatus.MUMBLE, traceback.format_exc())
    except (DownError, IOError) as e:
        die(ExitStatus.DOWN, traceback.format_exc())
    except WrongArgumentsError as e:
        die(
            ExitStatus.CHECKER_ERROR,
            f"Usage: {argv[0]} check|put|get IP FLAGID FLAG",
        )
    except Exception as e:
        die(ExitStatus.CHECKER_ERROR, traceback.format_exc())


if __name__ == "__main__":
    _main()
