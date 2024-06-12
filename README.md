# Capjoy

![Capjoy Logo](https://github.com/tokuhirom/capjoy/assets/21084/83703ef3-e27d-4d48-a0c1-b0e9ab361d39)

## Description

Capjoy is a simple CLI application for capturing images, videos, and audio on your Mac.

## Use Cases

- Record microphone audio to a file.
- Record the screen as a video.
- Capture the screen as an image.
- Record audio from applications and the microphone, and mix them.

## Installation

Download the binary from the [GitHub releases page](https://github.com/tokuhirom/capjoy/releases).

### Homebrew

Capjoy supports installation via Homebrew. Run the following command in your terminal:

```shell
brew install tokuhirom/formulae/capjoy
```

See [this repository](https://github.com/tokuhirom/homebrew-formulae) for more information.

## Usage

```
Usage: capjoy [<options>] <command> [<args>]...

Options:
  -h, --help  Show this message and exit

Commands:
  list-applications  List all running applications
  list-devices       List all capture devices
  list-displays      List all displays
  list-windows       List all windows
  capture-audio      Capture audio from the screen
  capture-image      Capture an image of a window or the entire display
  capture-mic        Capture audio from the default input device
  capture-mix        Capture and mix mic audio and screen audio into a single
                     file
  capture-video      Capture video and audio from the screen
  mix                Mix audio files
  version            Print the version of Capjoy.
```

## Permissions

This application requires permissions to capture the screen and/or audio. Please allow these permissions when
prompted.

## License

```
The MIT License (MIT)

Copyright © 2024 Tokuhiro Matsuno, http://64p.org/ <tokuhirom@gmail.com>

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the “Software”), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
```
