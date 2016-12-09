# Music Player (Android)
Play music under third-party app folder in background

## Version
1.2.0

## Preface
There are many music players for free. However, none of them (that I've tried) can play music storing in third party folder (e.g. under Dropbox folder).
The built-in player on Samsung Android can play regular music files in the background, but it cannot play music files in third party folder in the background (It will not scan those folders, nor user can explicitly add a folder).

## Scope
Personal usage to solve the problem in [Preface](#preface).

## Goal
Play any supported audio format music in the background without keeping the screen on.

## Features
 - Play music under any folder
 - Continue playing when working on other app / screen off
 - Show meta song name (limited detection on garbled code, show filename as fallback)
 - Custom share text

## New Features
 - Search songs in playlist by filename / meta title

## Todo
 - Save "delete" option on songs without slowing down the launching speed
 - Auto pause when media volume is changed to zero
 - Auto pause when bluetooth is disconnected (iff bluetooth was connected)

## Contact / Feedback
Feel free to create feature requests or report bugs on https://github.com/beenotung/music-player

## License
GPL-3.0 (GNU GENERAL PUBLIC LICENSE Version 3, 29 June 2007)

## Distribution
 - [Google Play](https://play.google.com/store/apps/details?id=com.github.beenotung.musicplayer)
 - [Direct APK](https://github.com/beenotung/music-player/blob/master/release/music-player-release.apk?raw=1)
