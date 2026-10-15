# -*- coding: utf-8 -*-
import olefile, sys

PATH = r"C:/Users/ac010/Documents/Downloads/2026-2027学年第一学期-Java Web开发期末考试报告单.doc"

def u16(b, o): return b[o] | (b[o+1] << 8)
def u32(b, o): return b[o] | (b[o+1] << 8) | (b[o+2] << 16) | (b[o+3] << 24)

ole = olefile.OleFileIO(PATH)
wd = ole.openstream('WordDocument').read()
table_name = '1Table' if (u16(wd, 0x000A) & 0x0200) else '0Table'
tbl = ole.openstream(table_name).read()

# FIB: fcClx at 0x01A2, lcbClx at 0x01A6
fcClx = u32(wd, 0x01A2)
lcbClx = u32(wd, 0x01A6)
clx = tbl[fcClx: fcClx + lcbClx]

# parse CLX: skip Prc entries (2-byte size) until Pcdt (0x0001)
i = 0
while i < len(clx):
    if clx[i] == 1:  # Pcdt
        i += 1
        pcdt_len = u32(clx, i)
        i += 4
        pcd_start = i
        break
    else:  # Prc: type(1) + cbGrpprl(1) + grpprl
        i += 1
        cb = clx[i]
        i += 1 + cb

# Pcdt contains PlcPcd: array of CPs (4 bytes each) then PCDs (8 bytes each)
# Number of pieces = (len-4)/12
data = clx[pcd_start: pcd_start + pcdt_len]
n = (len(data) - 4) // 12
cps = [u32(data, k*4) for k in range(n+1)]
pieces = []
for k in range(n):
    off = 4 + k*8
    pcd = u32(data, off+2)  # fc in low 30 bits
    fc = pcd & 0x3FFFFFFF
    enc = (pcd >> 30) & 0x03  # 1=ansi, 2=unicode
    pieces.append((cps[k], cps[k+1], fc, enc))

out = []
for (cpStart, cpEnd, fc, enc) in pieces:
    length = cpEnd - cpStart
    if length <= 0: continue
    if enc == 2:  # UTF-16
        raw = wd[fc: fc + length*2]
        try:
            txt = raw.decode('utf-16-le', errors='ignore')
        except Exception:
            txt = raw.decode('utf-16-le', errors='replace')
    else:
        raw = wd[fc: fc + length]
        txt = raw.decode('cp1252', errors='ignore')
    out.append(txt)

full = "".join(out)
# Word separators: 0x07=cell, 0x0B/0x0C=row/section, 0x0D=para
full = full.replace('\x07', ' | ').replace('\x0b', '\n').replace('\x0c', '\n').replace('\r', '\n')
lines = [l.rstrip() for l in full.split('\n')]
clean = [l for l in lines if l.strip()]
for l in clean:
    print(l)
