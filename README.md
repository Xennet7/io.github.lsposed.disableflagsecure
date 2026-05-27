# XENCALL
Mute selected callers and provide peace of mind.

**Unofficial LSPosed versions are not supported.**

## Supported OSes
- Android 12-16 (Custom ROMs are **not** supported)
- Xiaomi Hyper OS

## Usage
1. Enable the module
2. Select **ONLY** System Framework 
3. Reboot

## Parameters(To be entered after su)
1. Enable : settings put global silentcaller_disabled 0
2. Disable : settings put global silentcaller_disabled 1
3. Set Silenced Numbers : settings put global silentcaller_numbers "+91xxxxxxxxxx,+91xxxxxxxxxx"
4. Check : settings get global silentcaller_numbers and settings get global silentcaller_disabled
5. Delete : settings delete global silentcaller_numbers and settings delete global silentcaller_disabled
