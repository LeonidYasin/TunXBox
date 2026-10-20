#!/usr/bin/env python3
"""Rasterize the existing TunXBox launcher polygon, without third-party Python packages."""
import pathlib,struct,zlib,xml.etree.ElementTree as ET
ROOT=pathlib.Path(__file__).resolve().parents[2]
def main():
    xml=ET.parse(ROOT/'app/src/main/res/drawable/tunxbox_launcher.xml').getroot();ns='{http://schemas.android.com/apk/res/android}'
    paths=xml.findall('path');assert paths[0].get(ns+'fillColor')=='#0F172A'
    # Refuse stale branding: this polygon is the same path as the current Android vector.
    expected='M 34,30 L 74,30 L 74,38 L 58,38 L 58,54 L 72,68 L 66,74 L 54,62 L 42,74 L 36,68 L 50,54 L 50,38 L 34,38 Z'
    assert paths[1].get(ns+'pathData')==expected,'Launcher changed; update catalog rasterizer'
    polygon=[(34,30),(74,30),(74,38),(58,38),(58,54),(72,68),(66,74),(54,62),(42,74),(36,68),(50,54),(50,38),(34,38)]
    def inside(x,y):
        result=False;j=len(polygon)-1
        for i,(xi,yi) in enumerate(polygon):
            xj,yj=polygon[j]
            if (yi>y)!=(yj>y) and x<(xj-xi)*(y-yi)/(yj-yi)+xi:result=not result
            j=i
        return result
    size=512;bg=(15,23,42);fg=(103,232,249);raw=bytearray()
    for y in range(size):
        raw.append(0)
        for x in range(size):
            a=sum(inside((x+dx)*108/size,(y+dy)*108/size) for dx,dy in [(0.25,0.25),(.75,.25),(.25,.75),(.75,.75)])/4
            raw.extend(round(b+(f-b)*a) for b,f in zip(bg,fg))
    def chunk(t,b):return struct.pack('!I',len(b))+t+b+struct.pack('!I',zlib.crc32(t+b)&0xffffffff)
    png=b'\x89PNG\r\n\x1a\n'+chunk(b'IHDR',struct.pack('!2I5B',size,size,8,2,0,0,0))+chunk(b'IDAT',zlib.compress(raw,9))+chunk(b'IEND',b'')
    for lang in ['en-US','ru-RU']:
        p=ROOT/'fastlane/metadata/android'/lang/'images/icon.png';p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(png);print(p)
if __name__=='__main__':main()
