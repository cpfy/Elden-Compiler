import os
import shutil
if not os.path.exists("../compiler"):
    os.mkdir("../compiler")
    os.mkdir("../compiler/output")
if not os.path.exists("../compiler/txt"):
    shutil.move('txt/', "../compiler/")
    shutil.move('testcases/', "../compiler/")
os.system("sh scripts/compile.sh")
os.system("sh scripts/run.sh")