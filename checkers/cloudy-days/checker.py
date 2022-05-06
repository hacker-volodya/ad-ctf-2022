import inspect
import os
import random
import string
from enum import Enum
from sys import argv
import sys
from typing import List

from fakesession import FakeSession

"""
    Config
"""

# SERVICE INFO
PORT = 8080

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
        assert r.status_code == 200, f"Bad response: {r.text}"
        return r.json()

    # get droplet logs
    # {"name": "<name>", "created": "<date>", "logs": ["log1", "log2"]}
    def get(self, name: str):
        r = self.s.get(f"/droplets/{name}")
        assert r.status_code == 200, f"Bad response: {r.text}"
        return r.json()

    # upload new droplet
    # jar names: check1, flagstore1
    # returns droplet as in `get`
    def upload(self, name: str, jar_name: str):
        base = os.path.abspath(os.path.dirname(__file__))
        r = self.s.put(f"/droplets/{name}", files={
            'file': open(f'{base}/checker-droplets/{jar_name}/build/libs/{jar_name}-0.0.1-SNAPSHOT.jar', 'rb')
        })
        assert r.status_code == 200, f"Bad response: {r.text}"
        return r.json()

    # execute
    # returns string from droplet
    def execute(self, name: str, arguments: List[str]):
        r = self.s.post(f"/droplets/{name}", data={"arguments": arguments})
        assert r.status_code == 200, f"Bad response: {r.text}"
        return r.text


class Droplet:
    def __init__(self, name: str, api: CloudyApi, jar_name: str):
        self.name = name
        self.api = api
        self.jar_name = jar_name

    def is_deployed(self):
        return self.name in self.api.list() and self.api.get(self.name).get("name") == self.name

    def deploy(self):
        if self.is_deployed():
            return
        droplet = self.api.upload(self.name, self.jar_name)
        assert droplet.get("name") == self.name, "Deploy error"
        assert self.is_deployed(), "Deploy error"

    def logs(self):
        assert self.is_deployed(), "Fetching logs, but droplet not deployed"
        droplet = self.api.get(self.name)
        assert droplet.get("logs") is not None, "Log fetching error"
        return droplet["logs"]

    def execute(self, arguments: List[str]):
        assert self.is_deployed(), "Executing, but droplet not deployed"
        return self.api.execute(self.name, arguments)


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
            assert self.save(new_data) in expected_logs, f"Iteration {i} failed"
            expected_logs.append(new_data)
        print(expected_logs)
        print(self.logs())
        assert set(expected_logs[:-2]) <= set(self.logs()), "Logs not found"


class Flagstore(Droplet):
    def __init__(self, name: str, api: CloudyApi, vuln: str):
        super(Flagstore, self).__init__(name, api, "flagstore" + vuln)

    def put(self, key: str, flag: str):
        return self.execute(["put", key, flag])

    def get(self, key):
        return self.execute(["get", key])

    def assert_get(self, key, expected_flag):
        assert self.get(key) == str(java_string_hashcode(expected_flag)), "Flag mismatch"

    def do_check(self):
        key = rand_string()
        flag = rand_string()
        self.put(key, flag)
        self.assert_get(key, flag)
        assert "ok" in self.logs()


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


def die(code: ExitStatus, msg: str):
    if msg:
        print(msg, file=sys.stderr)
    exit(code.value)


def _main():
    try:
        cmd = argv[1]
        hostname = argv[2]
        if cmd == "get":
            fid, flag, vuln = argv[3], argv[4], argv[5]
            get(hostname, fid, flag, vuln)
        elif cmd == "put":
            fid, flag, vuln = argv[3], argv[4], argv[5]
            put(hostname, fid, flag, vuln)
        elif cmd == "check":
            check(hostname)
        elif cmd == "info":
            info()
        else:
            raise IndexError
    except IndexError:
        die(
            ExitStatus.CHECKER_ERROR,
            f"Usage: {argv[0]} check|put|get IP FLAGID FLAG",
        )


if __name__ == "__main__":
    _main()
