Simple helper for the youtube-dl executable.

Table of contents
=================

  * [General](#general)
  * [What it does](#what-it-does)
  * [How to use](#how-to-use)
  * [License](#license)
  * [Author](#author)

General
=======
I don't own any rights of youtube-dl (https://rg3.github.io/youtube-dl/), I just built a command-line wrapper for it so it's easier to keep track of multiple YouTube channels / playlists / whatever.

What it does
============
The command-line application is able to keep track of multiple YouTube Links. You can simply add them via the application or edit the configuration file manually.

The format of the file is simple:
```
# Enter here the YouTube channels you want to download in the following format, one channel per row:
# The description field is for visibility only and will not be used by the downloader.
# <DESCRIPTION>;<URL>;<LOCAL_PATH>
```

Just add the URLs and the local file system paths to the ```/config/channels.txt``` file and start the download.

How to use
==========
1. download latest .jar file
2. open command line
3. navigate to jar file
4. execute ```java -jar <NAME_OF_JAR_FILE>```

License
=======
MIT (https://github.com/Kaishiyoku/youtube-download-helper/blob/master/LICENSE)


Author
======
Twitter: [@kaishiyoku](https://twitter.com/kaishiyoku)  
Website: www.andreas-wiedel.de
