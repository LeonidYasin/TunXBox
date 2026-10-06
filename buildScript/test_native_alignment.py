import pathlib,struct,tempfile,unittest,zipfile
from verify_native_alignment import verify_elf,verify_archive

def elf(alignment=16384,relro_end=16384,bits=2):
    b=bytearray(176);b[:4]=b'\x7fELF';b[4]=bits;b[5]=1
    struct.pack_into('<Q',b,32,64);struct.pack_into('<HH',b,54,56,2)
    struct.pack_into('<IIQQQQQQ',b,64,1,4,0,0,0,64,64,alignment)
    struct.pack_into('<IIQQQQQQ',b,120,0x6474e552,4,0,0,0,relro_end,relro_end,1)
    return b
class NativeAlignmentTest(unittest.TestCase):
 def test_16kb_load_and_relro_accepted(self):self.assertTrue(verify_elf(elf()))
 def test_old_4kb_load_rejected(self):
  with self.assertRaises(ValueError):verify_elf(elf(4096))
 def test_unaligned_relro_rejected(self):
  with self.assertRaises(ValueError):verify_elf(elf(relro_end=4096))
 def test_invalid_program_headers_rejected(self):
  with self.assertRaises(ValueError):verify_elf(elf()[:80])
 def test_32bit_not_subject_to_64bit_requirement(self):self.assertFalse(verify_elf(elf(bits=1)))
 def test_compressed_apk_native_passes(self):
  with tempfile.TemporaryDirectory() as d:
   p=pathlib.Path(d)/'app.apk'
   with zipfile.ZipFile(p,'w',compression=zipfile.ZIP_DEFLATED) as z:z.writestr('lib/arm64-v8a/libgojni.so',elf())
   self.assertEqual(verify_archive(p,True),['lib/arm64-v8a/libgojni.so'])
 def test_unaligned_uncompressed_apk_rejected(self):
  with tempfile.TemporaryDirectory() as d:
   p=pathlib.Path(d)/'app.apk'
   with zipfile.ZipFile(p,'w',compression=zipfile.ZIP_STORED) as z:z.writestr('lib/arm64-v8a/libgojni.so',elf())
   with self.assertRaises(ValueError):verify_archive(p,True)
if __name__=='__main__':unittest.main()
