"""
patch_xsb.py

Post-process the shaded JAR to fix XMLBeans .xsb binary files.

maven-shade-plugin relocates .class files and directory paths,
but does NOT update class name references embedded inside .xsb
binary metadata files. This causes NoClassDefFoundError at runtime.

This script scans all .xsb files in the shaded JAR and replaces
unshaded class name references with their shaded equivalents.
"""

import struct
import zipfile
import shutil
import sys
import os

SHADE_PREFIX = b'poi4shaded.'

# Must match the <relocations> in pom.xml
# Sorted longest-first to avoid partial matches
RELOCATIONS = sorted([
    b'org.apache.poi',
    b'org.apache.xmlbeans',
    b'org.openxmlformats.schemas',
    b'com.microsoft.schemas',
    b'schemaorg_apache_xmlbeans',
    b'org.apache.commons.compress',
    b'org.apache.commons.collections4',
    b'org.apache.commons.codec',
    b'org.apache.commons.math3',
    b'com.graphbuilder',
    b'org.etsi.uri',
    b'org.w3.x2006',
    b'org.w3.x2000',
    b'org.apache.commons.logging',
], key=len, reverse=True)


def patch_xsb_data(data):
    """
    Patch a single .xsb file's binary data.

    .xsb files store strings in Java's DataOutput.writeUTF() format:
      [2-byte big-endian length][modified UTF-8 bytes]

    We find strings starting with a relocation pattern, then:
      1. Read the full string using the length prefix
      2. Prepend 'poi4shaded.' to the string
      3. Update the 2-byte length prefix
    """
    patches = []  # (offset, old_total_bytes, new_bytes)

    i = 0
    while i < len(data):
        matched = False
        for pattern in RELOCATIONS:
            plen = len(pattern)
            if data[i:i + plen] == pattern:
                # Check for writeUTF length prefix (2 bytes before)
                if i >= 2:
                    declared_len = struct.unpack('>H', data[i - 2:i])[0]
                    if declared_len >= plen and declared_len < 1000 and (i - 2 + 2 + declared_len) <= len(data):
                        full_str = data[i:i + declared_len]
                        new_str = SHADE_PREFIX + full_str
                        new_bytes = struct.pack('>H', len(new_str)) + new_str
                        patches.append((i - 2, 2 + declared_len, new_bytes))
                        i += declared_len
                        matched = True
                        break
        if not matched:
            i += 1

    if not patches:
        return data, 0

    # Build new byte array with patches applied
    result = bytearray()
    prev_end = 0
    for offset, old_len, new_bytes in patches:
        result.extend(data[prev_end:offset])
        result.extend(new_bytes)
        prev_end = offset + old_len
    result.extend(data[prev_end:])

    return bytes(result), len(patches)


def patch_jar(jar_path):
    """Patch all .xsb files in the shaded JAR."""
    tmp_path = jar_path + '.tmp'
    total_patches = 0
    patched_files = 0

    with zipfile.ZipFile(jar_path, 'r') as zin:
        with zipfile.ZipFile(tmp_path, 'w', zipfile.ZIP_DEFLATED) as zout:
            for item in zin.infolist():
                data = zin.read(item.filename)

                if item.filename.endswith('.xsb'):
                    patched_data, count = patch_xsb_data(data)
                    if count > 0:
                        data = patched_data
                        total_patches += count
                        patched_files += 1

                zout.writestr(item, data)

    # Replace original JAR with patched version
    shutil.move(tmp_path, jar_path)

    print(f'Patched {total_patches} references in {patched_files} .xsb files.')


if __name__ == '__main__':
    if len(sys.argv) < 2:
        jar = os.path.join('target', 'poi4-shaded-4.1.1.jar')
    else:
        jar = sys.argv[1]

    if not os.path.exists(jar):
        print(f'ERROR: JAR not found: {jar}')
        sys.exit(1)

    print(f'Patching: {jar}')
    patch_jar(jar)
    print('Done.')
