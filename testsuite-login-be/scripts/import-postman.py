#!/usr/bin/env python3
"""Importa escenarios desde Asteci.postman_collection.json al estándar JSON del proyecto."""

import json
import re
import sys
from pathlib import Path

POSTMAN = Path("/Users/aarmentah/Desktop/Asteci.postman_collection.json")
OUT = Path(__file__).resolve().parent.parent / "src/test/resources/scenarios"
CATALOG = OUT / "catalog"

HU_CONFIG = {
    "HU-006101 Login": {
        "folder": "authentication/login",
        "catalog": "login-scenarios.txt",
        "defaults": {
            "Content-Type": "application/json",
            "country-id": "MX",
            "client": "SCOTIABANK-APP",
            "client-type": "MOBILE",
            "application": "AsTecIApp",
            "remote-host": "192.168.1.100",
            "log-date": "2026-05-20T19:41:40.360Z",
            "time-zone": "America/Mexico_City",
            "isp": "Telmex",
            "city": "Pachuca",
            "longitude": "-98.7591",
            "latitude": "20.1011",
        },
    },
    "HU-006102 Logout": {
        "folder": "authentication/logout",
        "catalog": "logout-scenarios.txt",
        "defaults": {
            "Content-Type": "application/json",
            "country-id": "MX",
            "client": "SCOTIABANK-APP",
            "client-type": "MOBILE",
            "application": "AsTecIApp",
            "remote-host": "192.168.1.100",
            "log-date": "2026-05-20T19:41:40.360Z",
            "time-zone": "America/Mexico_City",
            "isp": "Telmex",
            "city": "Pachuca",
            "longitude": "-98.7591",
            "latitude": "20.1011",
        },
    },
    "HU-006103_Autenticacion_Contraseña": {
        "folder": "authentication/password",
        "catalog": "password-scenarios.txt",
        "defaults": {
            "Content-Type": "application/json",
            "country-id": "MX",
            "client": "SCOTIABANK-APP",
            "client-type": "MOBILE",
            "application": "AsTecIApp",
            "remote-host": "192.168.1.100",
            "log-date": "2026-05-20T19:41:40.360Z",
            "time-zone": "America/Mexico_City",
            "isp": "Telmex",
            "city": "Ciudad de Mexico",
            "longitude": "-99.1332",
            "latitude": "19.4326",
        },
    },
}


def slug(name: str) -> str:
    s = name.lower()
    s = re.sub(r"[—–]", "-", s)
    s = re.sub(r"[^a-z0-9]+", "-", s)
    return s.strip("-")[:90]


def walk_items(items):
    for item in items:
        if "request" in item:
            yield item
        elif "item" in item:
            yield from walk_items(item["item"])


def test_script(item) -> str:
    for event in item.get("event", []):
        if event.get("listen") == "test":
            return "\n".join(event.get("script", {}).get("exec") or [])
    return ""


def build_expected(script: str) -> dict:
    status_codes = [int(c) for c in re.findall(r"\.have\.status\((\d+)\)", script)]
    status = status_codes[0] if status_codes else 200

    expected = {
        "statusCode": status,
        "expect_json": status not in (500, 502, 503),
    }
    if len(set(status_codes)) > 1:
        expected["status_code_any_of"] = sorted(set(status_codes))

    assertions = []

    for match in re.finditer(
        r"res\.status\.status_code\)\.to\.eql\((\d+)\)", script
    ):
        assertions.append({"path": "status.status_code", "equals": int(match.group(1))})

    for match in re.finditer(
        r"res\.data\.type_case\)\.to\.eql\(\"([^\"]+)\"\)", script
    ):
        assertions.append({"path": "data.type_case", "equals": match.group(1)})

    for match in re.finditer(
        r"res\.data\.user_id\)\.to\.eql\(\"([^\"]+)\"\)", script
    ):
        assertions.append({"path": "data.user_id", "equals": match.group(1)})

    for match in re.finditer(
        r"res\.data\.subcase\.type_subcase\)\.to\.eql\(\"([^\"]+)\"\)", script
    ):
        assertions.append(
            {"path": "data.subcase.type_subcase", "equals": match.group(1)}
        )

    for match in re.finditer(
        r"res\.status\.message\)\.to\.eql\(\"([^\"]+)\"\)", script
    ):
        expected["status_message"] = match.group(1)

    for match in re.finditer(
        r"res\.status\.description\)\.to\.eql\(\"([^\"]+)\"\)", script
    ):
        expected["status_description_contains"] = match.group(1)

    for match in re.finditer(
        r"status\.description.*contains\([\"']([^\"']+)[\"']\)", script
    ):
        expected["status_description_contains"] = match.group(1)

    if "res.data.subcase).to.be.null" in script.replace(" ", ""):
        assertions.append({"path": "data.subcase", "is_null": True})
    if "res.data).to.be.null" in script.replace(" ", "") or 'res.data, is(nullValue())' in script:
        assertions.append({"path": "data", "is_null": True})
    if "user_id" in script and "not.empty" in script:
        assertions.append({"path": "data.user_id", "not_empty": True})
    if "session_key" in script and ("notNull" in script or "not.null" in script):
        assertions.append({"path": "data.session_key", "not_empty": True})
    if "avatar.file_name" in script and "not.empty" in script:
        assertions.append({"path": "data.avatar.file_name", "not_empty": True})
    if "DENIED" in script and "contains" in script:
        assertions.append({"path": "data.type_case", "any_of_contains": ["DENIED"]})
    if "UNIDENTIFIED" in script and "contains" in script and "type_case" in script:
        assertions.append({"path": "data.type_case", "contains": "UNIDENTIFIED"})
    if "IDENTIFIED" in script and "INACTIVE" in script and "contains" in script:
        for part in ("IDENTIFIED", "INACTIVE"):
            assertions.append({"path": "data.type_case", "contains": part})

    if assertions:
        expected["body_assertions"] = assertions
    return expected


def resolve_headers(request_headers, defaults: dict) -> dict:
    resolved = {}
    for header in request_headers:
        if header.get("disabled"):
            continue
        key = header.get("key")
        value = header.get("value")
        if not key:
            continue
        if value and value.startswith("{{"):
            resolved[key] = defaults.get(key, value)
        elif value is not None:
            resolved[key] = value
    return resolved


def import_hu(top_item, config):
    folder = OUT / config["folder"]
    folder.mkdir(parents=True, exist_ok=True)

    # limpiar JSON previos en carpeta (excepto se regeneran todos)
    for old in folder.glob("*.json"):
        old.unlink()

    catalog_lines = []
    defaults = config["defaults"]

    for index, item in enumerate(walk_items(top_item.get("item", [])), start=1):
        name = item["name"]
        req = item["request"]
        method = req.get("method", "POST")
        url = req.get("url", {})
        path_parts = url.get("path", []) if isinstance(url, dict) else []
        api_path = "/" + "/".join(path_parts) if path_parts else ""

        body_raw = req.get("body", {}).get("raw", "{}")
        try:
            body = json.loads(body_raw) if body_raw else {}
        except json.JSONDecodeError:
            body = body_raw

        headers = resolve_headers(req.get("header", []), defaults)
        script = test_script(item)
        expected = build_expected(script)

        file_slug = slug(name)
        scenario = {
            "name": file_slug,
            "method": method,
            "path": api_path,
            "headers": headers,
            "body": body,
            "expected": expected,
        }

        out_file = folder / f"{file_slug}.json"
        with open(out_file, "w", encoding="utf-8") as handle:
            json.dump(scenario, handle, indent=2, ensure_ascii=False)
            handle.write("\n")

        tag = "smoke" if index == 1 else "regression"
        rel = f"{config['folder']}/{file_slug}"
        catalog_lines.append(f"{rel}|{name}|{tag}")

    with open(CATALOG / config["catalog"], "w", encoding="utf-8") as catalog:
        catalog.write("\n".join(catalog_lines) + "\n")

    return len(catalog_lines)


def main():
    with open(POSTMAN, encoding="utf-8") as handle:
        collection = json.load(handle)

    CATALOG.mkdir(parents=True, exist_ok=True)
    totals = {}
    for top in collection["item"]:
        name = top.get("name", "")
        if name not in HU_CONFIG:
            continue
        totals[name] = import_hu(top, HU_CONFIG[name])
        print(f"{name}: {totals[name]} escenarios")

    print("Importación completada.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
