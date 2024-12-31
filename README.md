# CodePad

High Performance, Monospaced Code Text Editor for JavaFX.

[ screenshot ]



## Goals

The goals of this project is to deliver a high performance text editor JavaFX component,
targeting applications such as code editors or log viewers, which supports:

- large documents up to 2 billion paragraphs
- very long (millions of characters) paragraphs
- syntax highlighting

It is currently not a goal to support proportional fonts, right-to-left orientation, or bidirectional text.

For these features,
[demand](https://bugs.openjdk.org/browse/JDK-8301121) integration of the
[RichTextArea (Incubator)](https://github.com/andy-goryachev-oracle/Test/blob/main/doc/RichTextArea/RichTextArea.md)
project into the JavaFX core.



## Warning

This project is currently in the early development stage.



## Software Requirements

Requires JDK 23+ with JavaFX 23+.


## License

This project and its source code is licensed under the [BSD 2-Clause "Simplified" License](LICENSE).

