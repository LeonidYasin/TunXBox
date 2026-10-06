#!/usr/bin/env python3
"""Reject 4KB-only 64-bit Android natives; checks actual ELF LOAD/RELRO segments.
Compressed natives are extracted by Android. Uncompressed APK entries need 16KB ZIP alignment too.
"""
import argparse,pathlib,struct,zipfile
PAGE=16384

def verify_elf(data):
    if len(data)<64 or data[:4]!=b'\x7fELF': raise ValueError('Invalid ELF header')
    if data[4]!=2: return False # 16KB Android page-size requirement is for 64-bit ABIs.
    if data[5] not in (1,2): raise ValueError('Invalid ELF byte order')
    order='<' if data[5]==1 else '>'
    offset=struct.unpack_from(order+'Q',data,32)[0]
    entry_size,count=struct.unpack_from(order+'HH',data,54)
    if entry_size<56 or not count or offset+count*entry_size>len(data): raise ValueError('Invalid program header table')
    loads=0
    for i in range(count):
        pos=offset+i*entry_size
        kind=struct.unpack_from(order+'I',data,pos)[0]
        file_offset,virtual=struct.unpack_from(order+'QQ',data,pos+8)
        mem_size,alignment=struct.unpack_from(order+'QQ',data,pos+40)
        if kind==1:
            loads+=1
            if alignment<PAGE or (virtual-file_offset)%PAGE: raise ValueError('64-bit LOAD segment is not 16KB aligned')
        if kind==0x6474e552 and (virtual+mem_size)%PAGE: raise ValueError('64-bit RELRO end is not 16KB aligned')
    if not loads: raise ValueError('No LOAD segments')
    return True

def verify_archive(path,require_64bit=False):
    checked=[]
    with zipfile.ZipFile(path) as archive:
        entries=[i for i in archive.infolist() if i.filename.startswith(('lib/','jni/')) and i.filename.endswith('.so')]
        if not entries: raise ValueError('No native libraries in archive')
        for item in entries:
            data=archive.read(item)
            if verify_elf(data):
                if item.filename.startswith('lib/') and item.compress_type==zipfile.ZIP_STORED:
                    with open(path,'rb') as f:
                        f.seek(item.header_offset+26);name_length,extra_length=struct.unpack('<HH',f.read(4))
                    if (item.header_offset+30+name_length+extra_length)%PAGE: raise ValueError('Uncompressed native is not 16KB ZIP aligned')
                checked.append(item.filename)
    if require_64bit and not checked: raise ValueError('Expected 64-bit native libraries')
    return checked

if __name__=='__main__':
    p=argparse.ArgumentParser(description=__doc__);p.add_argument('--require-64bit',action='store_true');p.add_argument('archives',nargs='+',type=pathlib.Path);a=p.parse_args()
    for path in a.archives:
        names=verify_archive(path,a.require_64bit)
        print(f'{path.name}: 16KB aligned 64-bit natives verified: '+(', '.join(names) if names else '32-bit-only APK'))
