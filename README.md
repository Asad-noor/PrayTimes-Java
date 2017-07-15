# PrayTime
Java Port of PrayTime Library from http://praytimes.org/

## Description (from praytimes.org)
Pray Times provides a set of handy functions to calculate prayer times for any location around the world, based on a variety of calculation methods currently used in Muslim communities.

---
For more information about how the calculation works you can visit [this page](http://praytimes.org/wiki/Prayer_Times_Calculation).

Changes/Additions compared to the original JS-Library:
- added Zawal Time/Solar Noon (slightly before Dhuhr)
- added Qibla Time Calculation

#### What is Qibla Time?
The qibla time helps you to determine the exact direction to qibla based on the time and sun position.

This Library contains 4 types of Qibla time:
- Front qibla time: if you turn yourself to sun at that time, you are on qibla qirection
- Left qibla time: if you take the sun to your left at that time, you are on qibla direction
- Right qibla time: if you take the sun to your right at that time, you are on qibla direction
- Back qibla Time: if you turn yourself away from the sun at that time you are in qibla direction

## License

PrayTimes-Java: Prayer Times Java Calculator (ver 0.9)

Copyright (C) 2007-2011 PrayTimes.org (JS Code ver 2.3)

Copyright (C) 2017 Metin Kale (Java Code)

Developer JS: Hamid Zarrabi-Zadeh

Developer Java: Metin Kale

License: GNU LGPL v3.0

TERMS OF USE:
	Permission is granted to use this code, with or
	without modification, in any website or application
	provided that credit is given to the original work
	with a link back to PrayTimes.org.

This program is distributed in the hope that it will
be useful, but WITHOUT ANY WARRANTY.

PLEASE DO NOT REMOVE THIS COPYRIGHT BLOCK.

#####Other Libraries used
SunCalc Java Port (Author Java-Port: Nolan Caudill)
Original License: BSD

(c) 2011-2015, Vladimir Agafonkin

SunCalc is a JavaScript library for calculating sun/moon position and light phases.
