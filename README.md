Atlas Utilities

This project is a few different things in one place/monolith:

1. Utilities for creating program files for https://github.com/atlas-tuning/arduino, a lightweight C++ "firmware" for an ESP32 that aims to add vehicle functions in various projects I'm working on (DCCD controller, blipper module, and so on).
2. Code to proof-of-concept pieces of software in the Subaru ecosystem as I reverse engineer them (tuning suites, Subaru Select Monitor 3 & 4, FlashWrite, Tactrix, and so on).
3. Code to interact with the modern Subaru "DIT" ECU used in the 2015+ VA and VB WRX models, including absolutely anything I find interesting but mostly centered around true flashing capability.

This is mostly a personal repository for personal goals, and at the moment is not intended for consumption but you may well find use from it -- it is written in a way that you might be able to leverage it as an API in your own project(s).  Ultimately my hope is others reverse engineering the VA/VB WRX platform can find some use out of this effort.

If you want to adapt the logic in here to your own vehicle(s) or ECU(s), be mindful that I am being somewhat careless in my efforts to not brick any of my ECUs.  Therefore, you should take caution as there are keys in this repository that can and will allow you to perform operations that damage your ECU. Be careful!
