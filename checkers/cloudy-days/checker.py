#!/usr/bin/env python3
import inspect
import os
import random
import string
from enum import Enum
from sys import argv
import sys
from typing import List
import traceback

from fakesession import FakeSession

"""
    Config
"""

# SERVICE INFO
PORT = 2

# DEBUG -- logs to stderr, TRACE -- log HTTP requests
DEBUG = os.getenv("DEBUG", True)

"""
    Methods
"""


def info():
    print('{"vulns": 2, "timeout": 30, "attack_data": ""}', flush=True, end="")
    exit(101)


def check(host: str):
    api = CloudyApi(host)
    droplet = Check1(rand_string(), api)
    droplet.deploy()
    droplet.do_check()


def put(host: str, flag_id: str, flag: str, vuln: str):
    api = CloudyApi(host)
    random.seed(int.from_bytes(flag_id.encode(), 'big'))
    name = rand_string()
    key = rand_string()
    droplet = Flagstore(vuln + "_" + name, api, vuln)
    droplet.deploy()
    droplet.put(key, flag)


def get(host: str, flag_id: str, flag: str, vuln: str):
    api = CloudyApi(host)
    random.seed(int.from_bytes(flag_id.encode(), 'big'))
    name = rand_string()
    key = rand_string()
    droplet = Flagstore(vuln + "_" + name, api, vuln)
    droplet.assert_get(key, flag)


"""
    Helpers
"""


class CloudyApi:
    def __init__(self, host: str):
        self.s = FakeSession(host, PORT)

    # list droplets, returns list of names
    def list(self):
        r = self.s.get("/droplets")
        if r.status_code != 200:
            raise MumbleError(f"Bad response: {r.text}")
        x = r.json()
        if not isinstance(x, list):
            raise MumbleError("List is not array")
        if any([not isinstance(s, str) for s in x]):
            raise MumbleError("List is not of strings")
        return x

    # get droplet logs
    # {"name": "<name>", "created": "<date>", "logs": ["log1", "log2"]}
    def get(self, name: str):
        r = self.s.get(f"/droplets/{name}")
        if r.status_code != 200:
            raise MumbleError(f"Bad response: {r.text}")
        x = r.json()
        Droplet.check_structure(x)
        return x

    # upload new droplet
    # jar names: check1, flagstore1
    # returns droplet as in `get`
    def upload(self, name: str, jar_name: str):
        base = os.path.abspath(os.path.dirname(__file__))
        r = self.s.put(f"/droplets/{name}", files={
            'file': open(f'{base}/checker-droplets/{jar_name}/build/libs/{jar_name}-0.0.1-SNAPSHOT.jar', 'rb')
        })
        if r.status_code != 200:
            raise MumbleError(f"Bad response: {r.text}")
        x = r.json()
        Droplet.check_structure(x)
        return x

    # execute
    # returns string from droplet
    def execute(self, name: str, arguments: List[str]):
        r = self.s.post(f"/droplets/{name}", data={"arguments": arguments})
        if r.status_code != 200:
            raise MumbleError(f"Bad response: {r.text}")
        return r.text


class Droplet:
    def __init__(self, name: str, api: CloudyApi, jar_name: str):
        self.name = name
        self.api = api
        self.jar_name = jar_name

    def is_deployed(self):
        return self.name in self.api.list() and self.api.get(self.name)["name"] == self.name

    def deploy(self):
        if self.is_deployed():
            return
        droplet = self.api.upload(self.name, self.jar_name)
        if droplet["name"] != self.name:
            raise MumbleError("Deploy error")
        if not self.is_deployed():
            raise MumbleError("Deploy error")

    def logs(self):
        if not self.is_deployed():
            raise MumbleError("Fetching logs, but droplet not deployed")
        return self.api.get(self.name)["logs"]

    def execute(self, arguments: List[str]):
        if not self.is_deployed():
            raise MumbleError("Executing, but droplet not deployed")
        return self.api.execute(self.name, arguments)

    @classmethod
    def check_structure(cls, droplet):
        if not isinstance(droplet, dict):
            raise MumbleError("Droplet is not an object")
        if list(droplet.keys()) != ["name", "created", "logs"]:
            raise MumbleError("Wrong droplet structure")
        if not isinstance(droplet["name"], str):
            raise MumbleError("Field 'name' is not str")
        if not isinstance(droplet["created"], str):
            raise MumbleError("Field 'created' is not str")
        if not isinstance(droplet["logs"], list):
            raise MumbleError("Field 'logs' is not list")
        if any([not isinstance(s, str) for s in droplet["logs"]]):
            raise MumbleError("Logs are not of strings")


class Check1(Droplet):
    def __init__(self, name: str, api: CloudyApi):
        super(Check1, self).__init__(name, api, "check1")

    def save(self, data: str):
        return self.execute(["save", data])

    def do_check(self):
        last_data = "<no data>"
        expected_logs = [last_data]
        for i in range(7):
            new_data = rand_string()
            if self.save(new_data) not in expected_logs:
                raise MumbleError(f"Iteration {i} failed")
            expected_logs.append(new_data)
        if not set(expected_logs[:-2]) <= set(self.logs()):
            raise MumbleError("Logs not found")


class Flagstore(Droplet):
    def __init__(self, name: str, api: CloudyApi, vuln: str):
        super(Flagstore, self).__init__(name, api, "flagstore" + vuln)

    def put(self, key: str, flag: str):
        return self.execute(["put", key, flag])

    def get(self, key):
        return self.execute(["get", key])

    def assert_get(self, key, expected_flag):
        if self.get(key) != str(java_string_hashcode(expected_flag)):
            raise CorruptError("Flag mismatch")

    def do_check(self):
        key = rand_string()
        flag = rand_string()
        self.put(key, flag)
        self.assert_get(key, flag)
        if "ok" not in self.logs():
            raise MumbleError("Check failed")


"""
    Utils
"""


def java_string_hashcode(s: str):
    h = 0
    for c in s:
        h = int((((31 * h + ord(c)) ^ 0x80000000) & 0xFFFFFFFF) - 0x80000000)
    return h


def rand_string(n=12, alphabet=string.ascii_uppercase + string.ascii_lowercase + string.digits):
    return ''.join(random.choice(alphabet) for _ in range(n))


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
    except CorruptError as e:
        die(ExitStatus.CORRUPT, traceback.format_exc())
    except MumbleError as e:
        die(ExitStatus.MUMBLE, traceback.format_exc())
    except DownError | IOError as e:
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
