set JAVA_HOME="C:\Users\procsl\.jdks\graalvm-jdk-22.0.2+9.1"
set PATH="C:\Windows\system32;C:\Windows;C:\Windows\System32\Wbem;C:\Windows\System32\WindowsPowerShell\v1.0\;C:\Windows\System32\OpenSSH\;C:\Program Files\nodejs\;C:\Program Files\Git\cmd;C:\Program Files (x86)\Windows Kits\10\Windows Performance Toolkit\;C:\Users\procsl\AppData\Local\Programs\Python\Launcher\;C:\Users\procsl\AppData\Local\Microsoft\WindowsApps;C:\Users\procsl\AppData\Roaming\npm;C:\Program Files\Bandizip\;C:\Users\procsl\.jdks\graalvm-jdk-22.0.2+9.1\bin;C:\Users\procsl\.m2\apache-maven-3.8.6-bin\apache-maven-3.8.6\bin;C:\Users\procsl\AppData\Local\Microsoft\WinGet\Packages\Google.PlatformTools_Microsoft.Winget.Source_8wekyb3d8bbwe\platform-tools;C:\Program Files\JetBrains\PyCharm 2023.3.3\bin;;C:\Users\procsl\AppData\Local\Packages\PythonSoftwareFoundation.Python.3.12_qbz5n2kfra8p0\LocalCache\local-packages\Python312\Scripts;"
set CLASSPATH="C:\Users\procsl\.jdks\graalvm-jdk-22.0.2+9.1\lib"

"C:\Program Files\Microsoft Visual Studio\2022\Community\VC\Auxiliary\Build\vcvars64.bat"

echo "success"

chcp 65001

echo "success2"
mvn  clean install -Ph2,monitor,native,openapi,rdc native:compile -DskipTests
